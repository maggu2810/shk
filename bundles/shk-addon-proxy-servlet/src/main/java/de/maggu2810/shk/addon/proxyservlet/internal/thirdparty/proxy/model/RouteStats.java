
package de.maggu2810.shk.addon.proxyservlet.internal.thirdparty.proxy.model;

public class RouteStats {

    private Route route;
    private long requestCount;
    private long openConnectionCount;
    private long requestsPerSecond;

    public Route getRoute() {
        return route;
    }

    public void setRoute(final Route route) {
        this.route = route;
    }

    public long getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(final long requestCount) {
        this.requestCount = requestCount;
    }

    public long getOpenConnectionCount() {
        return openConnectionCount;
    }

    public void setOpenConnectionCount(final long openConnectionCount) {
        this.openConnectionCount = openConnectionCount;
    }

    public long getRequestsPerSecond() {
        return requestsPerSecond;
    }

    public void setRequestsPerSecond(final long requestsPerSecond) {
        this.requestsPerSecond = requestsPerSecond;
    }
}
