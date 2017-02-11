
package de.maggu2810.shk.addon.proxyservlet.internal.thirdparty.proxy;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

/**
 * Websocket proxy implementation. Creates a WebSocketProxySocket for listening
 * connections, which will create a WebSocketProxyClient to relay it.
 *
 * The standard websocket API JSR 356 doesn't seem to allow dynamically adding and
 * removing sockets (called endpoint in JSR 356), so we are using Jetty's own API for the
 * socket side. This has and additional benefit of being a servlet, which makes it more similar
 * to the HTTP implementation. The JSR 356 didn't allow us to fail the initial upgrade request with
 * HTTP errors, but most likely that would be also possible now in this servlet implementation.
 *
 * @author klemela
 *
 */
public class WebSocketProxyServlet extends WebSocketServlet {
    private static final long serialVersionUID = 1L;

    private final ConnectionManager connectionManager;

    public WebSocketProxyServlet(final ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void configure(final WebSocketServletFactory factory) {
        factory.register(WebSocketProxySocket.class);
        factory.setCreator((req, resp) -> {
            final String prefix = "";
            final String proxyTo = "http://127.0.0.1:8080/rest/events";
            return new WebSocketProxySocket(prefix, proxyTo, connectionManager);
        });
    }

    public static CloseReason toCloseReason(final Throwable e) {
        String msg = "proxy error: " + e.getClass().getSimpleName() + " " + e.getMessage();
        if (e.getCause() != null) {
            msg += " Caused by: " + e.getCause().getClass().getSimpleName() + " " + e.getCause().getMessage();
        }
        return new CloseReason(CloseCodes.UNEXPECTED_CONDITION, msg);
    }

    // @Override
    // public void init() throws ServletException {
    // final Thread t = Thread.currentThread();
    // final ClassLoader tcl = t.getContextClassLoader();
    // t.setContextClassLoader(
    // new JoinClassLoader(tcl, getClass().getClassLoader(), WebSocketServerFactory.class.getClassLoader()));
    // super.init();
    // t.setContextClassLoader(tcl);
    // }

}
