/*-
 * #%L
 * shk :: Bundles :: IO :: Web Util
 * %%
 * Copyright (C) 2015 - 2016 maggu2810
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

    String mdnsName() default "smarthome";
}

@Component(immediate = true)
@Designate(ocd = MdnsAdvertiserConfig.class)
public class MdnsAdvertiser implements HttpServiceListener {

    // private final Logger logger = LoggerFactory.getLogger(getClass());

    @Reference
    private MDNSService mdnsService;
    private HttpServiceInfoProvider provider;
    private String mdnsName;

    private final Map<Integer, Integer> ports = new HashMap<>();
    private final Map<Integer, Integer> portsSecure = new HashMap<>();

    @Activate
    protected void activate(final MdnsAdvertiserConfig config) {
        mdnsName = config.mdnsName();
        provider.getHttpServices().stream()
                .forEach((service) -> addHttpService(service, provider.getHttpServiceInfo(service)));
    }

    @Deactivate
    protected void deactivate() {
        provider.getHttpServices().stream().forEach((service) -> removeHttpService(service));
    }

    @Reference
    protected void setHttpServiceInfoProvider(final HttpServiceInfoProvider provider) {
        this.provider = provider;
        provider.addHttpServiceInfoListener(this);
    }

    protected void unsetHttpServiceInfoProvider(final HttpServiceInfoProvider provider) {
        this.provider = null;
        provider.removeHttpServiceInfoListener(this);
    }

    @Override
    public void addHttpService(final HttpService httpService, final HttpServiceInfo info) {
        if (info.getHttpPort() != -1 && increase(ports, info.getHttpPort())) {
            // advertise non-ssl port
            mdnsService.registerService(getHttpPortServiceDescription(info.getHttpPort()));
        }
        if (info.getHttpPortSecure() != -1 && increase(portsSecure, info.getHttpPortSecure())) {
            // advertise ssl-port
            mdnsService.registerService(getHttpPortSecureServiceDescription(info.getHttpPortSecure()));
        }
    }

    @Override
    public void removeHttpService(final HttpService httpService) {
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

    private boolean increase(final Map<Integer, Integer> map, final int key) {
        final int cur;
        if (map.containsKey(key)) {
            cur = map.get(key);
        } else {
            cur = 0;
        }
        map.put(key, cur + 1);
        return cur == 0;
    }

    private boolean decrease(final Map<Integer, Integer> map, final int key) {
        final int cur = map.get(key);
        if (cur == 1) {
            map.remove(key);
            return true;
        } else {
            map.put(key, cur - 1);
            return false;
        }
    }

    private static Hashtable<String, String> getMdnsServiceProperties() {
        final Hashtable<String, String> serviceProperties = new Hashtable<>();
        // TODO: Check if we can use the CM to get the rest servlet alias.
        serviceProperties.put("uri", "/rest");
        return serviceProperties;
    }

    private ServiceDescription getHttpPortServiceDescription(final int port) {
        final String serviceType = "_" + mdnsName + "-server._tcp.local.";
        final String serviceName = mdnsName;
        return new ServiceDescription(serviceType, serviceName, port, getMdnsServiceProperties());
    }

    private ServiceDescription getHttpPortSecureServiceDescription(final int port) {
        final String serviceType = "_" + mdnsName + "-server-ssl._tcp.local.";
        final String serviceName = mdnsName + "-ssl";
        return new ServiceDescription(serviceType, serviceName, port, getMdnsServiceProperties());
    }

}
