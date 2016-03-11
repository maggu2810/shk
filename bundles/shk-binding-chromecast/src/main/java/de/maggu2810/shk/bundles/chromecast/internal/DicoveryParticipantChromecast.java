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

package de.maggu2810.shk.bundles.chromecast.internal;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.ManufacturerDetails;
import org.jupnp.model.meta.ModelDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.model.meta.RemoteDeviceIdentity;
import org.jupnp.model.meta.RemoteService;
import org.osgi.service.component.annotations.Component;

@Component
public class DicoveryParticipantChromecast implements UpnpDiscoveryParticipant {

    // private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(BindingConstants.THING_TYPE_CHROMECAST);
    }

    @Override
    public DiscoveryResult createResult(final RemoteDevice device) {
        final ThingUID uid = getThingUID(device);
        if (uid == null) {
            return null;
        }

        Map<String, Object> properties = new HashMap<>(2);
        properties.put(BindingConstants.HOST, device.getDetails().getBaseURL().getHost());
        // properties.put(SERIAL_NUMBER, device.getDetails().getSerialNumber());

        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                .withLabel(device.getDetails().getFriendlyName()).build();
        // .withRepresentationProperty(SERIAL_NUMBER)
        return result;
    }

    @Override
    public ThingUID getThingUID(final RemoteDevice device) {
        final RemoteDeviceIdentity identity = device.getIdentity();
        final DeviceDetails deviceDetails = device.getDetails();
        final ManufacturerDetails manufacturerDetails = deviceDetails.getManufacturerDetails();
        final ModelDetails modelDetails = deviceDetails.getModelDetails();

        if (!manufacturerDetails.getManufacturer().equals("Google Inc.")) {
            return null;
        }

        if (!modelDetails.getModelName().equals("Eureka Dongle")) {
            return null;
        }

        boolean serviceAvailable = false;
        for (final RemoteService remoteService : device.getServices()) {
            final URI descriptorUri = remoteService.getDescriptorURI();
            if (descriptorUri.getSchemeSpecificPart().equals("//www.google.com/cast")) {
                serviceAvailable = true;
            }
        }
        if (!serviceAvailable) {
            return null;
        }

        return new ThingUID(BindingConstants.THING_TYPE_CHROMECAST, identity.getUdn().getIdentifierString());
    }

}
