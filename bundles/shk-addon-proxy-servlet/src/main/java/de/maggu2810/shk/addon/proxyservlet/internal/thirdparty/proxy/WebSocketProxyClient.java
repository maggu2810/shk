
package de.maggu2810.shk.addon.proxyservlet.internal.thirdparty.proxy;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler.Whole;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client side of the websocket proxy
 *
 * Based on the Java WebSocket standard JSR 356.
 *
 * @author klemela
 *
 */
public class WebSocketProxyClient extends Endpoint {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final WebSocketProxySocket proxySocket;
    private Session clientSession;
    private final String targetUri;
    private final CountDownLatch connectLatch;

    public WebSocketProxyClient(final WebSocketProxySocket jettyWebSocketSourceEndpoint, final CountDownLatch openLatch,
            final String targetUri) {
        this.proxySocket = jettyWebSocketSourceEndpoint;
        this.connectLatch = openLatch;
        this.targetUri = targetUri;
    }

    @Override
    public void onOpen(final Session targetSession, final EndpointConfig config) {
        this.clientSession = targetSession;

        targetSession.addMessageHandler((Whole<String>) message -> proxySocket.sendText(message));
        connectLatch.countDown();
    }

    @Override
    public void onClose(final Session session, final CloseReason reason) {
        connectLatch.countDown();
        proxySocket.closeSocketSession(reason);
    }

    @Override
    public void onError(final Session session, final Throwable thr) {
        connectLatch.countDown();
        proxySocket.closeSocketSession(WebSocketProxyServlet.toCloseReason(thr));
    }

    public void sendText(final String message) {
        try {
            clientSession.getBasicRemote().sendText(message);
        } catch (final IOException e) {
            logger.error("failed to send a message to " + targetUri, e);
            proxySocket.closeSocketSession(WebSocketProxyServlet.toCloseReason(e));
            closeClientSession(WebSocketProxyServlet.toCloseReason(e));
        }
    }

    public void closeClientSession(final CloseReason closeReason) {
        try {
            if (clientSession != null) {
                clientSession.close(closeReason);
            }
        } catch (final IOException e) {
            logger.error("failed to close the target websocket to " + targetUri, e);
        }
    }
}
