/*-
 * #%L
 * shk - Bundles - IO - Web Util
 * %%
 * Copyright (C) 2015 - 2018 maggu2810
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */

package de.maggu2810.shk.web.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.maggu2810.shk.web.HttpServiceInfo;
import de.maggu2810.shk.web.HttpServiceInfoProvider;
import de.maggu2810.shk.web.HttpServiceListener;

@Component
public class HttpServiceInfoProviderImpl implements HttpServiceListener, HttpServiceInfoProvider {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // Pax-Web implements also the interface WebContainer
    // groupId: org.ops4j.pax.web
    // artifactId: pax-web-api
    private @Nullable ServiceTracker<HttpService, HttpService> serviceTrackerHttpService;
    private final Collection<HttpServiceListener> listeners = new LinkedList<>();

    private final Map<HttpService, HttpServiceInfo> services = new HashMap<>();

    /**
     * Start the service.
     *
     * @param context the bundle context
     */
    @Activate
    @EnsuresNonNull("serviceTrackerHttpService")
    public void start(final BundleContext context) {
        serviceTrackerHttpService = new ServiceTracker<>(context, org.osgi.service.http.HttpService.class,
                new HttpServiceServiceTrackerCustomizer(context, this));
        serviceTrackerHttpService.open();
    }

    /**
     * Stop the service.
     *
     */
    @Deactivate
    public void stop() {
        if (serviceTrackerHttpService != null) {
            serviceTrackerHttpService.close();
        }
    }

    @Override
    public synchronized void addHttpService(final HttpService httpService, final HttpServiceInfo httpServiceInfo) {
        logger.trace("add http service: {}, {}", httpService, httpServiceInfo);
        if (services.containsKey(httpService)) {
            logger.warn("Received the same service multiple times.");
            return;
        }

        services.put(httpService, httpServiceInfo);
        listeners.parallelStream().forEach((listener) -> listener.addHttpService(httpService, httpServiceInfo));

    }

    @Override
    public synchronized void removeHttpService(final HttpService httpService) {
        logger.trace("remove http service: {}", httpService);
        if (!services.containsKey(httpService)) {
            logger.warn("An unknown service is removed.");
            return;
        }

        listeners.parallelStream().forEach((listener) -> listener.removeHttpService(httpService));
        services.remove(httpService);
    }

    @Override
    public synchronized void addHttpServiceInfoListener(final HttpServiceListener listener) {
        listeners.add(listener);
    }

    @Override
    public synchronized void removeHttpServiceInfoListener(final HttpServiceListener listener) {
        listeners.remove(listener);
    }

    @Override
    public synchronized Collection<HttpService> getHttpServices() {
        return new HashSet<>(services.keySet());
    }

    @Override
    public synchronized HttpServiceInfo getHttpServiceInfo(final HttpService service) {
        if (services.containsKey(service)) {
            return services.get(service);
        } else {
            throw new IllegalArgumentException(String.format("Unknown service: %s", service));
        }
    }

}
