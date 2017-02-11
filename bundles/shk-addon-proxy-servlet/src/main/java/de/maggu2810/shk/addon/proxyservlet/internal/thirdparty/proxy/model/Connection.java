
package de.maggu2810.shk.addon.proxyservlet.internal.thirdparty.proxy.model;

import java.time.LocalDateTime;

import javax.ws.rs.core.UriBuilder;

public class Connection {

    private Route route;
    private String sourceAddress;
    private String requestURI;
    private LocalDateTime openTime;
    private LocalDateTime closeTime;
    private String method;

    public String getSourceAddress() {
        return sourceAddress;
    }

    public void setSourceAddress(final String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public LocalDateTime getOpenTime() {
        return openTime;
    }

    public void setOpenTime(final LocalDateTime openTime) {
        this.openTime = openTime;
    }

    public LocalDateTime getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(final LocalDateTime closeTime) {
        this.closeTime = closeTime;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public void setRequestURI(final String requestPath) {
        // remove query string to hide tokens
        final UriBuilder builder = UriBuilder.fromUri(requestPath);
        builder.replaceQuery(null);
        this.requestURI = builder.toString();
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(final Route route) {
        this.route = route;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(final String method) {
        this.method = method;
    }
}
