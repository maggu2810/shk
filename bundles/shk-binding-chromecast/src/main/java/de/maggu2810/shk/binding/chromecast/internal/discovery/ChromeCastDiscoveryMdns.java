/*
 * #%L
 * shk :: Bundles :: Binding :: Chromecast
 * %%
 * Copyright (C) 2015 - 2016 maggu2810
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */

package de.maggu2810.shk.binding.chromecast.internal.discovery;

import java.util.Arrays;
import java.util.Enumeration;

import javax.jmdns.ServiceInfo;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.smarthome.io.transport.mdns.discovery.MDNSDiscoveryParticipant;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;

import su.litvak.chromecast.api.v2.ChromeCast;

@Component
public class ChromeCastDiscoveryMdns extends AbstractDiscovery<ServiceInfo> implements MDNSDiscoveryParticipant {

    @Override
    public String getServiceType() {
        return ChromeCast.SERVICE_TYPE;
    }

    /**
     * Log all the details of a mDNS service information object.
     *
     * @param service the service that information should be logged
     * @param logger the logger that should be used for logging
     */
    public static void logServiceInfo(final @NonNull ServiceInfo service, final @NonNull Logger logger) {
        logger.info("mDNS discovery result: {}", service);
        logger.info("hasData: {}", service.hasData());
        logger.info("getType: {}", service.getType());
        logger.info("getTypeWithSubtype: {}", service.getTypeWithSubtype());
        logger.info("getName: {}", service.getName());
        logger.info("getKey: {}", service.getKey());
        logger.info("getQualifiedName: {}", service.getQualifiedName());
        logger.info("getServer: {}", service.getServer());
        logger.info("getHostAddresses: {}", Arrays.toString(service.getHostAddresses()));
        logger.info("getInetAddresses: {}", Arrays.toString(service.getInetAddresses()));
        logger.info("getInet4Addresses: {}", Arrays.toString(service.getInet4Addresses()));
        logger.info("getInet6Addresses: {}", Arrays.toString(service.getInet6Addresses()));
        logger.info("getPort: {}", service.getPort());
        logger.info("getPriority: {}", service.getPriority());
        logger.info("getWeight: {}", service.getWeight());
        logger.info("getDomain: {}", service.getDomain());
        logger.info("getProtocol: {}", service.getProtocol());
        logger.info("getApplication: {}", service.getApplication());
        logger.info("getSubtype: {}", service.getSubtype());
        logger.info("isPersistent: {}", service.isPersistent());
        logger.info("getURLs: {}", Arrays.toString(service.getURLs()));

        logger.info("getPropertyNames: {}", service.getPropertyNames());
        final Enumeration<String> e = service.getPropertyNames();
        while (e.hasMoreElements()) {
            final String propName = e.nextElement();
            final String value = service.getPropertyString(propName);
            if (value != null) {
                logger.info("property: name: {}, value: {}", propName, value);
            } else {
                logger.info("property: name: {}: value: {}", propName, service.getPropertyBytes(propName));
            }
        }

        logger.info("getQualifiedNameMap: {}", service.getQualifiedNameMap());
        // public abstract final byte[] getTextBytes();
        // public abstract final String[] getURLs(final String protocol);
        // public abstract String getNiceTextString();
    }

    @Override
    protected @Nullable String getUuidNoHyphens(final @NonNull ServiceInfo service) {
        return service.getPropertyString("id");
    }

    @Override
    protected @NonNull String getFriendlyName(final @NonNull ServiceInfo service) {
        String friendlyName;
        friendlyName = service.getPropertyString("fn");
        if (friendlyName != null) {
            return friendlyName;
        }
        friendlyName = getUuidNoHyphens(service);
        if (friendlyName != null) {
            return friendlyName;
        }
        return FRIENDLY_NAME_LAST_RESORT;
    }

    @Override
    protected @Nullable String getHost(final @NonNull ServiceInfo service) {
        final String[] hostAddresses = service.getHostAddresses();
        if (hostAddresses != null && hostAddresses.length > 0) {
            return hostAddresses[0];
        } else {
            return null;
        }
    }

    @Override
    protected @Nullable String getSerialNumber(final @NonNull ServiceInfo service) {
        // TODO: Is this really the serial number?
        return service.getPropertyString("bs");
    }

}
