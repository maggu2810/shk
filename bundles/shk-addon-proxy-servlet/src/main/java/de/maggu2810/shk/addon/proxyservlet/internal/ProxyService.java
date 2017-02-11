
package de.maggu2810.shk.addon.proxyservlet.internal;

import javax.servlet.ServletException;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import de.maggu2810.shk.addon.proxyservlet.internal.thirdparty.proxy.ConnectionManager;
import de.maggu2810.shk.addon.proxyservlet.internal.thirdparty.proxy.WebSocketProxyServlet;

@Component(immediate = true)
public class ProxyService {

    private static final String WS_PROXY_ALIAS = "/DUMMY/rest/events";
    private static final String ALIAS = "/";

    // private final Logger logger = LoggerFactory.getLogger(getClass());

    @Reference
    private HttpService httpService;

    private @Nullable SimpleProxyServlet proxyServlet;

    @Activate
    protected void activate() throws ServletException, NamespaceException {
        final ConnectionManager connectionManager = new ConnectionManager();
        final WebSocketProxyServlet wsProxyServlet = new WebSocketProxyServlet(connectionManager);
        httpService.registerServlet(WS_PROXY_ALIAS, wsProxyServlet, null, null);

        proxyServlet = new SimpleProxyServlet();
        httpService.registerServlet(ALIAS, proxyServlet, null, null);
    }

    @Deactivate
    protected void deactivate() {
        httpService.unregister(ALIAS);
        httpService.unregister(WS_PROXY_ALIAS);
    }
}
