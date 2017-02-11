
package de.maggu2810.shk.addon.proxyservlet.internal;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.servlet.ReadListener;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Response.Listener;
import org.eclipse.jetty.client.util.DeferredContentProvider;
import org.eclipse.jetty.proxy.AsyncProxyServlet;
import org.eclipse.jetty.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleProxyServlet extends AsyncProxyServlet {

    private static final long serialVersionUID = 1L;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected ReadListener newReadListener(final HttpServletRequest request, final HttpServletResponse response,
            final Request proxyRequest, final DeferredContentProvider provider) {
        logger.trace("newReadListener");
        return super.newReadListener(request, response, proxyRequest, provider);
    }

    @Override
    protected StreamWriter newWriteListener(final HttpServletRequest request, final Response proxyResponse) {
        logger.trace("newWriteListener");
        return super.newWriteListener(request, proxyResponse);
    }

    @Override
    public void init(final ServletConfig config) throws ServletException {
        logger.trace("init");
        // super.init(wrapConfig(config));
        super.init(config);
        this.setTimeout(TimeUnit.MINUTES.toMillis(60));
        final HttpClient client = getHttpClient();
        client.setConnectTimeout(TimeUnit.MINUTES.toMillis(60));
        client.setIdleTimeout(TimeUnit.MINUTES.toMillis(60));
        client.setStopTimeout(TimeUnit.MINUTES.toMillis(60));
    }

    // private ServletConfig wrapConfig(final ServletConfig config) {
    // final ServletConfigImpl configNew = new ServletConfigImpl(config);
    // configNew.put("maxThreads", "20");
    // return configNew;
    // }

    @Override
    protected String rewriteTarget(final HttpServletRequest clientRequest) {
        logger.trace("rewriteTarget (req: {})", getUrl(clientRequest));
        final String scheme = clientRequest.getScheme();
        final String serverName = clientRequest.getServerName();
        // final int serverPort = clientRequest.getServerPort();
        final String requestUri = clientRequest.getRequestURI();

        final StringBuffer target = clientRequest.getRequestURL();
        final String query = clientRequest.getQueryString();
        if (query != null) {
            target.append("?").append(query);
        }

        final int port = 8181;
        final String rewrittenTarget = String.format("%s://%s:%d%s%s", scheme, serverName, port, requestUri,
                query != null ? "?" + query : "");

        logger.info("Rewrite target: {} => {}", target, rewrittenTarget);
        return rewrittenTarget;
    }

    @Override
    protected void copyRequestHeaders(final HttpServletRequest clientRequest, final Request proxyRequest) {
        logger.trace("copyRequestHeaders (req: {})", getUrl(clientRequest));
        super.copyRequestHeaders(clientRequest, proxyRequest);
    }

    @Override
    protected void onServerResponseHeaders(final HttpServletRequest clientRequest,
            final HttpServletResponse proxyResponse, final Response serverResponse) {
        logger.trace("onServerResponseHeaders (req: {})", getUrl(clientRequest));
        super.onServerResponseHeaders(clientRequest, proxyResponse, serverResponse);

        // Flush the proxy final response header
        try {
            proxyResponse.flushBuffer();
        } catch (final IOException ex) {
            logger.warn("Flush buffer for proxy response failed.", ex);
        }
    }

    @Override
    protected ContentProvider proxyRequestContent(final HttpServletRequest request, final HttpServletResponse response,
            final Request proxyRequest) throws IOException {
        logger.trace("proxyRequestContent (req: {})", getUrl(request));
        return super.proxyRequestContent(request, response, proxyRequest);
    }

    @Override
    protected void onResponseContent(final HttpServletRequest request, final HttpServletResponse response,
            final Response proxyResponse, final byte[] buffer, final int offset, final int length,
            final Callback callback) {
        logger.trace("onResponseContent (req: {})", getUrl(request));
        super.onResponseContent(request, response, proxyResponse, buffer, offset, length, callback);
        logger.trace("onResponseContent: super returned");

        // try {
        // response.flushBuffer();
        // } catch (final IOException ex) {
        // logger.warn("Flush reponse output stream failed.", ex);
        // }
    }

    @Override
    protected Listener newProxyResponseListener(final HttpServletRequest request, final HttpServletResponse response) {
        logger.trace("newProxyResponseListener (req: {})", getUrl(request));
        return super.newProxyResponseListener(request, response);
    }

    @Override
    protected void service(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        logger.trace("service (req: {})", getUrl(request));
        super.service(request, response);
    }

    @Override
    protected void onContinue(final HttpServletRequest clientRequest, final Request proxyRequest) {
        logger.trace("onContinue (req: {})", getUrl(clientRequest));
        super.onContinue(clientRequest, proxyRequest);
    }

    @Override
    public void init() throws ServletException {
        logger.trace("init");
        super.init();
    }

    @Override
    public void destroy() {
        logger.trace("destroy");
        super.destroy();
    }

    @Override
    public String getHostHeader() {
        logger.trace("getHostHeader");
        return super.getHostHeader();
    }

    @Override
    public String getViaHost() {
        logger.trace("getViaHost");
        return super.getViaHost();
    }

    @Override
    public long getTimeout() {
        logger.trace("getTimeout");
        return super.getTimeout();
    }

    @Override
    public void setTimeout(final long timeout) {
        logger.trace("setTimeout");
        super.setTimeout(timeout);
    }

    @Override
    public Set<String> getWhiteListHosts() {
        logger.trace("getWhiteListHosts");
        return super.getWhiteListHosts();
    }

    @Override
    public Set<String> getBlackListHosts() {
        logger.trace("getBlackListHosts");
        return super.getBlackListHosts();
    }

    @Override
    protected org.eclipse.jetty.util.log.Logger createLogger() {
        logger.trace("createLogger");
        return super.createLogger();
    }

    @Override
    protected HttpClient createHttpClient() throws ServletException {
        logger.trace("createHttpClient");
        return super.createHttpClient();
    }

    @Override
    protected HttpClient newHttpClient() {
        logger.trace("newHttpClient");
        return super.newHttpClient();
    }

    @Override
    protected HttpClient getHttpClient() {
        logger.trace("getHttpClient");
        return super.getHttpClient();
    }

    @Override
    public boolean validateDestination(final String host, final int port) {
        logger.trace("validateDestination");
        return super.validateDestination(host, port);
    }

    @Override
    protected void onProxyRewriteFailed(final HttpServletRequest clientRequest,
            final HttpServletResponse proxyResponse) {
        logger.trace("onProxyRewriteFailed (req: {})", getUrl(clientRequest));
        super.onProxyRewriteFailed(clientRequest, proxyResponse);
    }

    @Override
    protected boolean hasContent(final HttpServletRequest clientRequest) {
        logger.trace("hasContent (req: {})", getUrl(clientRequest));
        return super.hasContent(clientRequest);
    }

    @Override
    protected boolean expects100Continue(final HttpServletRequest request) {
        logger.trace("expects100Continue (req: {})", getUrl(request));
        return super.expects100Continue(request);
    }

    @Override
    protected Set<String> findConnectionHeaders(final HttpServletRequest clientRequest) {
        logger.trace("findConnectionHeaders (req: {})", getUrl(clientRequest));
        return super.findConnectionHeaders(clientRequest);
    }

    @Override
    protected void addProxyHeaders(final HttpServletRequest clientRequest, final Request proxyRequest) {
        logger.trace("addProxyHeaders (req: {})", getUrl(clientRequest));
        super.addProxyHeaders(clientRequest, proxyRequest);
    }

    @Override
    protected void addViaHeader(final Request proxyRequest) {
        logger.trace("addViaHeader");
        super.addViaHeader(proxyRequest);
    }

    @Override
    protected void addXForwardedHeaders(final HttpServletRequest clientRequest, final Request proxyRequest) {
        logger.trace("addXForwardedHeaders (req: {})", getUrl(clientRequest));
        super.addXForwardedHeaders(clientRequest, proxyRequest);
    }

    @Override
    protected void sendProxyRequest(final HttpServletRequest clientRequest, final HttpServletResponse proxyResponse,
            final Request proxyRequest) {
        logger.trace("sendProxyRequest (req: {})", getUrl(clientRequest));
        super.sendProxyRequest(clientRequest, proxyResponse, proxyRequest);
    }

    @Override
    protected void onClientRequestFailure(final HttpServletRequest clientRequest, final Request proxyRequest,
            final HttpServletResponse proxyResponse, final Throwable failure) {
        logger.trace("onClientRequestFailure (req: {})", getUrl(clientRequest));
        super.onClientRequestFailure(clientRequest, proxyRequest, proxyResponse, failure);
    }

    @Override
    protected String filterServerResponseHeader(final HttpServletRequest clientRequest, final Response serverResponse,
            final String headerName, final String headerValue) {
        logger.trace("filterServerResponseHeader (req: {})", getUrl(clientRequest));
        return super.filterServerResponseHeader(clientRequest, serverResponse, headerName, headerValue);
    }

    @Override
    protected void onProxyResponseSuccess(final HttpServletRequest clientRequest,
            final HttpServletResponse proxyResponse, final Response serverResponse) {
        logger.trace("onProxyResponseSuccess (req: {})", getUrl(clientRequest));
        super.onProxyResponseSuccess(clientRequest, proxyResponse, serverResponse);
    }

    @Override
    protected void onProxyResponseFailure(final HttpServletRequest clientRequest,
            final HttpServletResponse proxyResponse, final Response serverResponse, final Throwable failure) {
        logger.trace("onProxyResponseFailure (req: {})", getUrl(clientRequest));
        super.onProxyResponseFailure(clientRequest, proxyResponse, serverResponse, failure);
        logger.trace("onProxyResponseFailure: super returned");
    }

    @Override
    protected int getRequestId(final HttpServletRequest clientRequest) {
        logger.trace("getRequestId (req: {})", getUrl(clientRequest));
        return super.getRequestId(clientRequest);
    }

    @Override
    protected void sendProxyResponseError(final HttpServletRequest clientRequest,
            final HttpServletResponse proxyResponse, final int status) {
        logger.trace("sendProxyResponseError (req: {})", getUrl(clientRequest));
        super.sendProxyResponseError(clientRequest, proxyResponse, status);
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        logger.trace("doGet (req: {})", getUrl(req));
        super.doGet(req, resp);
    }

    @Override
    protected long getLastModified(final HttpServletRequest req) {
        logger.trace("getLastModified (req: {})", getUrl(req));
        return super.getLastModified(req);
    }

    @Override
    protected void doHead(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        logger.trace("doHead (req: {})", getUrl(req));
        super.doHead(req, resp);
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        logger.trace("doPost (req: {})", getUrl(req));
        super.doPost(req, resp);
    }

    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        logger.trace("doPut (req: {})", getUrl(req));
        super.doPut(req, resp);
    }

    @Override
    protected void doDelete(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        logger.trace("doDelete (req: {})", getUrl(req));
        super.doDelete(req, resp);
    }

    @Override
    protected void doOptions(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        logger.trace("doOptions (req: {})", getUrl(req));
        super.doOptions(req, resp);
    }

    @Override
    protected void doTrace(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        logger.trace("doTrace (req: {})", getUrl(req));
        super.doTrace(req, resp);
    }

    @Override
    public void service(final ServletRequest req, final ServletResponse res) throws ServletException, IOException {
        // Use a small buffer so data gets be written as soon as possible.
        res.setBufferSize(0);

        logger.trace("service");
        super.service(req, res);
    }

    private String getUrl(final HttpServletRequest req) {
        try {
            final StringBuffer sb = req.getRequestURL();
            final String query = req.getQueryString();
            if (query != null) {
                return sb.toString() + "?" + query;
            } else {
                return sb.toString();
            }
        } catch (final RuntimeException ex) {
            return "get url failed";
        }
    }

}
