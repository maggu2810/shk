
package de.maggu2810.shk.addon.proxyservlet.internal.thirdparty.proxy;

import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Servlet;

import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.maggu2810.shk.addon.proxyservlet.internal.thirdparty.proxy.model.Connection;
import de.maggu2810.shk.addon.proxyservlet.internal.thirdparty.proxy.model.Route;
import de.maggu2810.shk.addon.proxyservlet.internal.thirdparty.proxy.model.RouteStats;

/**
 * Switchover proxy
 *
 * Proxy service for proxying HTTP and websocket traffic. The proxy routes can be
 * added and removed on the fly. When an existing proxy route is changed, the remaining
 * connections stay connected to the previous target, but all new connections will be
 * routed to the new target.
 *
 * Implemented on top of Jetty's ProxyServlet and Jetty's WebSocket API. The switchover
 * functionality is based on servlet mappings. A new servlet is created for each route and
 * requests to the route's path are mapped to that servlet. On switchover, a new servlet is
 * created, and the mapping is update to relay new connections to the new servlet. The old
 * servlet continues to relay existing connections and is removed when all of them are closed.
 *
 * Limitations:
 * - HTTP errors on websocket upgrade request won't cause the original upgrade request to
 * fail with a HTTP error, but the error is converted to a websocket error
 * - the same proxy path can't relay both HTTP and websocket traffic
 *
 * @author klemela
 *
 */
public class ProxyServer {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final String PREFIX = "prefix";
    public static final String PROXY_TO = "proxyTo";

    private final Server jetty;
    private final ServletContextHandler context;

    private final ConcurrentHashMap<ServletHolder, String> servletsToRemove = new ConcurrentHashMap<>();

    private final ConnectionManager connectionManager;

    public static void main(final String[] args) throws Exception {

        // bind to localhost port 8000
        final ProxyServer proxy = new ProxyServer(new URI("http://127.0.0.1:8000"));

        // proxy requests from localhost:8000/test to chipster.csc.fi
        proxy.addRoute("test", "http://chipster.csc.fi");

        // proxying websockets is as easy
        // proxy.addRoute("websocket-path-on-proxy", "http://websocket-server-host");

        proxy.startServer();
        LoggerFactory.getLogger(ProxyServer.class).info("proxy up and running");
    }

    public ProxyServer(final URI baseUri) {

        this.connectionManager = new ConnectionManager();
        this.connectionManager.setListener(connection -> removeUnusedServlets());

        this.jetty = new Server();

        final ServerConnector connector = new ServerConnector(jetty);
        connector.setPort(baseUri.getPort());
        connector.setHost(baseUri.getHost());
        jetty.addConnector(connector);

        final HandlerCollection handlers = new HandlerCollection();

        final boolean enableJMX = true;
        if (enableJMX) {
            // enable JMX to investigate Jetty internals with jconsole
            // on OS X set JVM argument -Djava.rmi.server.hostname=localhost to be able to connect
            final MBeanContainer mbContainer = new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
            jetty.addBean(mbContainer);
            final StatisticsHandler statisticsHandler = new StatisticsHandler();

            // settings as root handler will give only the overall statistics
            statisticsHandler.setHandler(handlers);
            jetty.setHandler(statisticsHandler);

        } else {
            jetty.setHandler(handlers);
        }

        this.context = new ServletContextHandler(handlers, "/", ServletContextHandler.SESSIONS);
    }

    public void startServer() {
        try {
            jetty.start();
        } catch (final Exception e) {
            logger.error("failed to start proxy", e);
        }
    }

    public void close() {
        try {
            this.jetty.stop();
        } catch (final Exception e) {
            logger.warn("failed to stop the proxy", e);
        }
    }

    /**
     * Add a new route. The remaining connections will stay connected to the old target.
     *
     * @param proxyPath
     * @param targetUri
     * @throws URISyntaxException
     */
    public void addRoute(final String proxyPath, final String targetUri) throws URISyntaxException {

        logger.info("add route " + proxyPath + " -> " + targetUri);
        ServletHolder routeServlet;
        final String scheme = new URI(targetUri).getScheme().toLowerCase();
        if ("ws".equals(scheme) || "wss".equals(scheme)) {
            final Servlet servlet = new WebSocketProxyServlet(connectionManager);
            routeServlet = new ServletHolder(servlet);
        } else {
            final Servlet servlet = new HttpProxyServlet(connectionManager);
            routeServlet = new ServletHolder(servlet);
        }
        routeServlet.setInitParameter(PROXY_TO, targetUri);
        routeServlet.setInitParameter(PREFIX, "/" + proxyPath);

        context.getServletHandler().addServlet(routeServlet);
        updateMapping(proxyPath, routeServlet);
    }

    public List<Route> getRoutes() {

        final ArrayList<Route> routes = new ArrayList<>();

        final List<ServletMapping> mappings = Arrays.asList(context.getServletHandler().getServletMappings());
        for (final ServletMapping mapping : mappings) {
            final String pathSpec = mapping.getPathSpecs()[0];
            if (!isProxyPathSpec(pathSpec)) {
                // TODO this isn't needed, if the DefaultServlet is removed
                continue;
            }
            final Route route = new Route();
            route.setProxyPath(getProxyPath(pathSpec));
            route.setProxyTo(getServlet(getProxyPath(pathSpec), mappings).getInitParameter(PROXY_TO));
            routes.add(route);
        }

        return routes;
    }

    public List<RouteStats> getRouteStats() {
        final ArrayList<RouteStats> stats = new ArrayList<>();
        for (final Route route : getRoutes()) {
            stats.add(connectionManager.getRouteStats(route));
        }
        return stats;
    }

    public void removeRoute(final String proxyPath) {
        logger.info("remove route " + proxyPath);
        updateMapping(proxyPath, null);
    }

    public List<Connection> getConnections() {

        return connectionManager.getConnections();
    }

    private String getPathSpec(final String proxyPath) {
        if (proxyPath.isEmpty()) {
            return "/*";
        } else {
            return "/" + proxyPath + "/*";
        }
    }

    private String getProxyPath(final String pathSpec) {
        if (isProxyPathSpec(pathSpec)) {
            return pathSpec.substring("/".length(), pathSpec.length() - "/*".length());
        } else {
            throw new IllegalArgumentException("illegal pathSpec");
        }
    }

    private boolean isProxyPathSpec(final String pathSpec) {
        return pathSpec.startsWith("/") && pathSpec.endsWith("/*");
    }

    private ServletMapping getServletMapping(final String proxyPath, final List<ServletMapping> mappings) {
        // find the mapping for this path
        for (final ServletMapping mapping : mappings) {
            final String pathSpec = mapping.getPathSpecs()[0];
            if (pathSpec.equals(getPathSpec(proxyPath))) {
                return mapping;
            }
        }
        return null;
    }

    private ServletHolder getServlet(final String proxyPath, final List<ServletMapping> mappings) {
        final ServletMapping mapping = getServletMapping(proxyPath, mappings);
        for (final ServletHolder holder : context.getServletHandler().getServlets()) {
            if (holder.getName().equals(mapping.getServletName())) {
                return holder;
            }
        }
        return null;
    }

    /**
     * Replace old mapping with a new one in one go
     *
     * If there is an old mapping, it will be removed and its servlet will
     * be removed when it doesn't have anymore active connections.
     *
     * and removes the mapping,
     * if the newHolder parameter is null.
     *
     * @param proxyPath
     * @param newHolder
     */
    public void updateMapping(final String proxyPath, final ServletHolder newHolder) {

        // see ServletHandler.addServletWithMapping()

        // get mappings (or create if this is the first mapping)
        ServletMapping[] mappingsArray = context.getServletHandler().getServletMappings();
        if (mappingsArray == null) {
            mappingsArray = new ServletMapping[0];
        }
        final List<ServletMapping> mappings = new ArrayList<>(Arrays.asList(mappingsArray));

        final ServletMapping oldMapping = getServletMapping(proxyPath, mappings);
        if (oldMapping != null) {
            final ServletHolder oldServlet = getServlet(proxyPath, mappings);
            logger.info("old route " + proxyPath + " -> " + oldServlet.getInitParameter(PROXY_TO)
                    + " will be removed later");
            // remove the servlet after all connections have closed
            servletsToRemove.put(oldServlet, proxyPath);
            mappings.remove(oldMapping);
        }

        if (newHolder != null) {
            final ServletMapping mapping = new ServletMapping();
            mapping.setServletName(newHolder.getName());
            mapping.setPathSpec(getPathSpec(proxyPath));
            mappings.add(mapping);
        }

        // update the server
        context.getServletHandler().setServletMappings(mappings.toArray(new ServletMapping[0]));

        removeUnusedServlets();
    }

    /**
     * This will interrupt all remaining connections served by this servlet. Remember to remove the
     * mapping also.
     *
     * @param holder
     */
    public void removeServlet(final ServletHolder holder) {
        final List<ServletHolder> holders = new ArrayList<>(Arrays.asList(context.getServletHandler().getServlets()));
        holders.remove(holder);
        context.getServletHandler().setServlets(holders.toArray(new ServletHolder[0]));
    }

    private void removeUnusedServlets() {
        // servlets are removed only when it doesn't have connections anymore
        final Iterator<ServletHolder> holdersIter = servletsToRemove.keySet().iterator();

        while (holdersIter.hasNext()) {
            final ServletHolder holder = holdersIter.next();

            final String holderProxyPath = servletsToRemove.get(holder);
            final String holderTargetURI = holder.getInitParameter(PROXY_TO);

            if (!connectionManager.hasOpenConnections(new Route(holderProxyPath, holderTargetURI))) {
                logger.info("remove unused route " + holderProxyPath + " -> " + holderTargetURI);
                removeServlet(holder);
                holdersIter.remove();
            }
        }
    }
}