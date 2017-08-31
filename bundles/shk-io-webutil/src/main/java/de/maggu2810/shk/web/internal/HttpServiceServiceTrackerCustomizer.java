/*-
 * #%L
 * shk :: Bundles :: IO :: Web Util
 * %%
 * Copyright (C) 2015 - 2017 maggu2810
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */

package de.maggu2810.shk.web.internal;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.maggu2810.shk.web.HttpServiceInfo;
import de.maggu2810.shk.web.HttpServiceListener;

public class HttpServiceServiceTrackerCustomizer implements ServiceTrackerCustomizer<HttpService, HttpService> {

    private static final String PROP_HTTP_PORT = "org.osgi.service.http.port";
    private static final String PROP_HTTP_PORT_SECURE = "org.osgi.service.http.port.secure";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final HttpServiceListener listener;

    private final BundleContext context;

    /**
     * Constructor.
     *
     * @param context the bundle context to lookup services
     * @param listener the listener to inform about added / removed services
     */
    public HttpServiceServiceTrackerCustomizer(final BundleContext context, final HttpServiceListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    public HttpService addingService(final ServiceReference<HttpService> reference) {
        logger.trace("adding service");
        final HttpService httpService = context.getService(reference);
        if (httpService == null) {
            return null;
        }
        final HttpServiceInfo httpServiceInfo = getHttpServiceInfo(reference);
        listener.addHttpService(httpService, httpServiceInfo);
        return httpService;
    }

    @Override
    public void modifiedService(final ServiceReference<HttpService> reference, final HttpService httpService) {
        logger.trace("modified service");
        if (httpService == null) {
            return;
        }

        listener.removeHttpService(httpService);

        final HttpServiceInfo httpServiceInfo = getHttpServiceInfo(reference);
        listener.addHttpService(httpService, httpServiceInfo);
    }

    @Override
    public void removedService(final ServiceReference<HttpService> reference, final HttpService httpService) {
        logger.trace("removed service");
        if (httpService == null) {
            return;
        }

        listener.removeHttpService(httpService);
    }

    private final @NonNull HttpServiceInfo getHttpServiceInfo(final ServiceReference<HttpService> reference) {
        // for (final String key : reference.getPropertyKeys()) {
        // logger.info("{}={}", key, reference.getProperty(key));
        // }

        final int httpPort = extractPort(reference, PROP_HTTP_PORT);
        final int httpPortSecure = extractPort(reference, PROP_HTTP_PORT_SECURE);
        return new HttpServiceInfo(httpPort, httpPortSecure);
    }

    private static final int extractPort(final ServiceReference<HttpService> reference, final String prop) {
        int port;

        port = parsePort(reference.getProperty(prop));
        if (port != -1) {
            return port;
        }

        port = parsePort(System.getProperty(prop));
        return port;
    }

    private static final int parsePort(final @Nullable Object obj) {
        if (obj != null) {
            try {
                return Integer.parseInt(obj.toString());
            } catch (final NumberFormatException ex) {
                return -1;
            }
        } else {
            return -1;
        }
    }

}
