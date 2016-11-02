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

import java.net.URI;

import org.eclipse.smarthome.config.discovery.UpnpDiscoveryParticipant;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.ManufacturerDetails;
import org.jupnp.model.meta.ModelDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.model.meta.RemoteDeviceIdentity;
import org.jupnp.model.meta.RemoteService;
import org.osgi.service.component.annotations.Component;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Component
@SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE", justification = "RemoteDevice extends Device and specify return type of getIdentity.")
public class ChromeCastDiscoveryUpnp extends AbstractDiscovery<RemoteDevice> implements UpnpDiscoveryParticipant {

    // private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected boolean use(final RemoteDevice device) {
        final DeviceDetails deviceDetails = device.getDetails();
        final ManufacturerDetails manufacturerDetails = deviceDetails.getManufacturerDetails();
        final ModelDetails modelDetails = deviceDetails.getModelDetails();

        if (!manufacturerDetails.getManufacturer().equals("Google Inc.")) {
            return false;
        }

        if (!modelDetails.getModelName().equals("Eureka Dongle")) {
            return false;
        }

        boolean serviceAvailable = false;
        for (final RemoteService remoteService : device.getServices()) {
            final URI descriptorUri = remoteService.getDescriptorURI();
            if (descriptorUri.getSchemeSpecificPart().equals("//www.google.com/cast")) {
                serviceAvailable = true;
            }
        }
        if (!serviceAvailable) {
            return false;
        }

        return true;
    }

    @Override
    protected String getUuidNoHyphens(final RemoteDevice device) {
        final RemoteDeviceIdentity identity = device.getIdentity();
        final String id = identity.getUdn().getIdentifierString();
        return id.replaceAll("-", "");
    }

    @Override
    protected String getFriendlyName(final RemoteDevice device) {
        return device.getDetails().getFriendlyName();
    }

    @Override
    protected String getHost(final RemoteDevice device) {
        return device.getDetails().getBaseURL().getHost();
    }

    @Override
    protected String getSerialNumber(final RemoteDevice device) {
        return device.getDetails().getSerialNumber();
    }

}
