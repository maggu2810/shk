/*-
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;

import de.maggu2810.shk.binding.chromecast.internal.BindingConstants;

public abstract class AbstractDiscovery<T> {

    protected final @NonNull String FRIENDLY_NAME_LAST_RESORT = "?";

    /**
     * Defines the list of thing types that this participant can identify.
     *
     * @return a set of thing type UIDs for which results can be created
     */
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(BindingConstants.ThingType.CHROMECAST);
    }

    /**
     * Creates a discovery result.
     *
     * @param info the information of the remote device
     * @return the according discovery result or {@code null}, if device is not supported
     */
    public @Nullable DiscoveryResult createResult(final @NonNull T info) {
        if (!use(info)) {
            return null;
        }

        final ThingUID thingUid = getThingUID(info);
        if (thingUid == null) {
            return null;
        }

        final Map<String, Object> properties = new HashMap<>(2);
        final String host = getHost(info);
        if (host != null) {
            properties.put(BindingConstants.Config.HOST, host);
        }
        final String serialNumber = getSerialNumber(info);
        if (serialNumber != null) {
            properties.put(BindingConstants.Properties.SERIAL_NUMBER, serialNumber);
        }

        final DiscoveryResultBuilder builder = DiscoveryResultBuilder.create(thingUid).withLabel(getFriendlyName(info))
                .withProperties(properties);
        if (serialNumber != null) {
            builder.withRepresentationProperty(BindingConstants.Properties.SERIAL_NUMBER);
        }
        return builder.build();
    }

    /**
     * Returns the thing UID.
     *
     * @param info the information of the remote device
     * @return a thing UID on success, null if no UID could be built
     */
    public @Nullable ThingUID getThingUID(final @NonNull T info) {
        final String uuidNoHyphens = getUuidNoHyphens(info);
        if (uuidNoHyphens == null) {
            return null;
        }

        return new ThingUID(BindingConstants.ThingType.CHROMECAST, uuidNoHyphens);
    }

    /**
     * Flag if the information should be proceed.
     *
     * <p>
     * This function could be overriden by a sub-class to stop processing of the result.
     *
     * @param info the information of the remote device
     * @return true if the information should be processed, false if the processing should be canceled
     */
    protected boolean use(final @NonNull T info) {
        return true;
    }

    protected abstract @Nullable String getUuidNoHyphens(final @NonNull T info);

    protected abstract @NonNull String getFriendlyName(final @NonNull T info);

    protected abstract @Nullable String getHost(final @NonNull T info);

    protected abstract @Nullable String getSerialNumber(final @NonNull T info);
}
