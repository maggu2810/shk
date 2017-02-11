
package de.maggu2810.shk.addon.proxyservlet.internal.thirdparty.proxy;

import java.io.IOException;

import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.proxy.ProxyServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.maggu2810.shk.addon.proxyservlet.internal.thirdparty.proxy.model.Connection;
import de.maggu2810.shk.addon.proxyservlet.internal.thirdparty.proxy.model.Route;

/**
 * HTTP proxy servlet based on Jetty's transparent ProxyServlet
 *
 * @author klemela
 *
 */
public class HttpProxyServlet extends ProxyServlet.Transparent {

    private static final long serialVersionUID = 1L;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ConnectionManager connectionManager;

    public HttpProxyServlet(final ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    // collect connection information
    @Override
    protected void service(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        super.service(request, response);
        // this is singleton servlet, but each request will it it's own connection object
        final Connection connection = new Connection();
        connection.setSourceAddress(request.getRemoteAddr());
        connection.setRequestURI(request.getRequestURL().toString());
        connection.setRoute(new Route(getProxyPath(), getProxyTo()));
        connection.setMethod(request.getMethod());

        connectionManager.addConnection(connection);

        // the AsyncListener can be added only after the ProxyServlet.service() has called startAsync()
        request.getAsyncContext().addListener(new AsyncListener() {

            @Override
            public void onStartAsync(final AsyncEvent event) throws IOException {
                // async is already started
            }

            @Override
            public void onTimeout(final AsyncEvent event) throws IOException {
                connectionManager.removeConnection(connection);
            }

            @Override
            public void onError(final AsyncEvent event) throws IOException {
                connectionManager.removeConnection(connection);
            }

            @Override
            public void onComplete(final AsyncEvent event) throws IOException {
                connectionManager.removeConnection(connection);
            }
        });
    }

    // Jetty's implementation is enough, but this is a nice place for debug messages
    @Override
    protected String rewriteTarget(final HttpServletRequest request) {
        final String rewritten = super.rewriteTarget(request);

        final StringBuffer original = request.getRequestURL();
        logger.debug("proxy " + original + " \t -> " + rewritten);
        return rewritten;
    }

    public String getProxyPath() {
        return this.getInitParameter(ProxyServer.PREFIX).substring(1);
    }

    public String getProxyTo() {
        return this.getInitParameter(ProxyServer.PROXY_TO);
    }
}