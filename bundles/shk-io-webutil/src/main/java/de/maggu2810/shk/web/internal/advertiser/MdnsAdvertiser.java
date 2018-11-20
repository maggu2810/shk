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

package de.maggu2810.shk.web.internal.advertiser;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.smarthome.io.transport.mdns.MDNSService;
import org.eclipse.smarthome.io.transport.mdns.ServiceDescription;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import de.maggu2810.shk.web.HttpServiceInfo;
import de.maggu2810.shk.web.HttpServiceInfoProvider;
import de.maggu2810.shk.web.HttpServiceListener;

@ObjectClassDefinition(name = "REST mDNS advertiser")
@interface MdnsAdvertiserConfig {

    String mdnsName() default MdnsAdvertiser.MDNS_NAME_FALLBACK;
}

@Component(immediate = true)
@Designate(ocd = MdnsAdvertiserConfig.class)
public class MdnsAdvertiser implements HttpServiceListener {

    protected static final @NonNull String MDNS_NAME_FALLBACK = "smarthome";

    // private final Logger logger = LoggerFactory.getLogger(getClass());

    @Reference
    @SuppressWarnings("initialization.fields.uninitialized")
    private MDNSService mdnsService;

    private @Nullable HttpServiceInfoProvider provider;

    private @NonNull String mdnsName = MDNS_NAME_FALLBACK;

    private final @NonNull Map<@NonNull Integer, @NonNull Integer> ports = new HashMap<>();
    private final @NonNull Map<@NonNull Integer, @NonNull Integer> portsSecure = new HashMap<>();

    @Activate
    protected void activate(final MdnsAdvertiserConfig config) {
        final String mdnsName = config.mdnsName();
        if (mdnsName != null) {
            this.mdnsName = mdnsName;
        }

        assert provider != null : "@AssumeAssertion(nullness)";
        addHttpServices(provider);
    }

    @Deactivate
    protected void deactivate() {
        assert provider != null : "@AssumeAssertion(nullness)";
        removeHttpServices(provider);
    }

    @Reference
    @EnsuresNonNull("this.provider")
    protected void setHttpServiceInfoProvider(final @NonNull HttpServiceInfoProvider provider) {
        provider.addHttpServiceInfoListener(this);
        this.provider = provider;
    }

    protected void unsetHttpServiceInfoProvider(final HttpServiceInfoProvider provider) {
        this.provider = null;
        provider.removeHttpServiceInfoListener(this);
    }

    private void addHttpServices(final HttpServiceInfoProvider provider) {
        provider.getHttpServices().stream()
                .forEach((service) -> addHttpService(service, provider.getHttpServiceInfo(service)));
    }

    @Override
    public void addHttpService(final @NonNull HttpService httpService, final @NonNull HttpServiceInfo info) {
        if (info.getHttpPort() != -1 && increase(ports, info.getHttpPort())) {
            // advertise non-ssl port
            mdnsService.registerService(getHttpPortServiceDescription(info.getHttpPort()));
        }
        if (info.getHttpPortSecure() != -1 && increase(portsSecure, info.getHttpPortSecure())) {
            // advertise ssl-port
            mdnsService.registerService(getHttpPortSecureServiceDescription(info.getHttpPortSecure()));
        }
    }

    private void removeHttpServices(final HttpServiceInfoProvider provider) {
        provider.getHttpServices().stream().forEach((service) -> removeHttpService(service));
    }

    @Override
    public void removeHttpService(final @NonNull HttpService httpService) {
        assert provider != null : "@AssumeAssertion(nullness)";

        final HttpServiceInfo info = provider.getHttpServiceInfo(httpService);
        if (info.getHttpPort() != -1 && decrease(ports, info.getHttpPort())) {
            // remove advertised non-ssl port
            mdnsService.unregisterService(getHttpPortServiceDescription(info.getHttpPort()));
        }
        if (info.getHttpPortSecure() != -1 && decrease(portsSecure, info.getHttpPortSecure())) {
            // remove advertised ssl-port
            mdnsService.unregisterService(getHttpPortSecureServiceDescription(info.getHttpPortSecure()));
        }
    }

    private static boolean increase(final Map<Integer, Integer> map, final int key) {
        Integer cur = map.get(key);
        if (cur == null) {
            cur = 0;
        }
        map.put(key, cur + 1);
        return cur == 0;
    }

    private static boolean decrease(final Map<Integer, Integer> map, final int key) {
        final Integer cur = map.get(key);
        if (cur == null || cur == 1) {
            map.remove(key);
            return true;
        } else {
            map.put(key, cur - 1);
            return false;
        }
    }

    private static @NonNull Hashtable<String, String> getMdnsServiceProperties() {
        final Hashtable<String, String> serviceProperties = new Hashtable<>();
        // TODO: Check if we can use the CM to get the rest servlet alias.
        serviceProperties.put("uri", "/rest");
        return serviceProperties;
    }

    private ServiceDescription getHttpPortServiceDescription(final int port) {
        final @NonNull String serviceType = "_" + mdnsName + "-server._tcp.local.";
        final @NonNull String serviceName = mdnsName;
        return new ServiceDescription(serviceType, serviceName, port, getMdnsServiceProperties());
    }

    private ServiceDescription getHttpPortSecureServiceDescription(final int port) {
        final String serviceType = "_" + mdnsName + "-server-ssl._tcp.local.";
        final String serviceName = mdnsName + "-ssl";
        return new ServiceDescription(serviceType, serviceName, port, getMdnsServiceProperties());
    }

}
