
package de.maggu2810.shk.addon.proxyservlet.internal.thirdparty.proxy;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import de.maggu2810.shk.addon.proxyservlet.internal.thirdparty.proxy.model.Connection;
import de.maggu2810.shk.addon.proxyservlet.internal.thirdparty.proxy.model.Route;
import de.maggu2810.shk.addon.proxyservlet.internal.thirdparty.proxy.model.RouteStats;

/**
 * Track open connections
 *
 * @author klemela
 *
 */
public class ConnectionManager {

    private final ConcurrentHashMap<Route, RouteConnections> routes = new ConcurrentHashMap<>();

    public static class RouteConnections {
        private final AtomicLong requestCount = new AtomicLong();
        private final ConcurrentLinkedQueue<Connection> connections = new ConcurrentLinkedQueue<>();
        private final Route route;

        public RouteConnections(final Route route) {
            this.route = route;
        }

        public void addConnection(final Connection connection) {
            connection.setOpenTime(LocalDateTime.now());
            connections.add(connection);
            requestCount.getAndIncrement();
        }

        public void removeConnection(final Connection connection) {
            connection.setCloseTime(LocalDateTime.now());
            connections.removeAll(getOldConnections());
            // System.out.println(
            // ChronoUnit.MILLIS.between(connection.getOpenTime(), connection.getCloseTime()) + "\t" +
            // connection.getMethod() + "\t" +
            // connection.getRequestURI());
        }

        private ArrayList<Connection> getOpenConnections() {
            final ArrayList<Connection> filtered = new ArrayList<>();
            for (final Connection con : connections) {
                if (con.getCloseTime() == null) {
                    filtered.add(con);
                }
            }
            return filtered;
        }

        private ArrayList<Connection> getLatestConnections() {
            final ArrayList<Connection> filtered = new ArrayList<>();
            for (final Connection con : connections) {
                if (con.getCloseTime() != null
                        && con.getCloseTime().isAfter(LocalDateTime.now().minus(1, ChronoUnit.SECONDS))) {
                    filtered.add(con);
                }
            }
            return filtered;
        }

        private ArrayList<Connection> getOldConnections() {
            final ArrayList<Connection> filtered = new ArrayList<>();
            for (final Connection con : connections) {
                if (con.getCloseTime() != null
                        && con.getCloseTime().isBefore(LocalDateTime.now().minus(1, ChronoUnit.SECONDS))) {
                    filtered.add(con);
                }
            }
            return filtered;
        }

        public RouteStats getRouteStats() {
            final RouteStats stats = new RouteStats();
            stats.setRoute(route);
            stats.setRequestCount(requestCount.get());
            stats.setOpenConnectionCount(getOpenConnections().size());
            stats.setRequestsPerSecond(getLatestConnections().size());
            return stats;
        }
    }

    public interface ConnectionListener {
        public void connectionRemoved(Connection connection);
    }

    private ConnectionListener listener;

    public void addConnection(final Connection connection) {
        get(connection.getRoute()).addConnection(connection);
    }

    public void removeConnection(final Connection connection) {
        get(connection.getRoute()).removeConnection(connection);
        if (listener != null) {
            listener.connectionRemoved(connection);
        }
    }

    public void setListener(final ConnectionListener listener) {
        this.listener = listener;
    }

    public List<Connection> getConnections() {
        final ArrayList<Connection> all = new ArrayList<>();
        for (final Route route : routes.keySet()) {
            all.addAll(routes.get(route).getOpenConnections());
        }
        return all;
    }

    public List<Connection> getConnections(final Route route) {
        return new ArrayList<>(get(route).getOpenConnections());
    }

    private RouteConnections get(final Route route) {
        if (!routes.containsKey(route)) {
            routes.put(route, new RouteConnections(route));
        }
        return routes.get(route);
    }

    public boolean hasOpenConnections(final Route route) {
        return !getConnections(route).isEmpty();
    }

    public RouteStats getRouteStats(final Route route) {
        return get(route).getRouteStats();
    }
}