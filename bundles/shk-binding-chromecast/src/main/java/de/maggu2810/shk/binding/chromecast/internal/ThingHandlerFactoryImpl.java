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

package de.maggu2810.shk.binding.chromecast.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.smarthome.core.audio.AudioHTTPServer;
import org.eclipse.smarthome.core.audio.AudioSink;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = { ThingHandlerFactory.class })
public class ThingHandlerFactoryImpl extends BaseThingHandlerFactory {

    private static final @NonNull Set<@NonNull ThingTypeUID> SUPPORTED_THING_TYPES = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(new ThingTypeUID[] { BindingConstants.ThingType.CHROMECAST })));

    private final Map<@NonNull String, @NonNull ServiceRegistration<AudioSink>> audioSinkRegistrations = new ConcurrentHashMap<>();

    @Reference
    @SuppressWarnings("initialization.fields.uninitialized")
    private @NonNull AudioHTTPServer audioHttpServer;

    // private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean supportsThingType(final @NonNull ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(final @NonNull Thing thing) {
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(BindingConstants.ThingType.CHROMECAST)) {
            final ThingHandlerChromecast handler = new ThingHandlerChromecast(thing);

            // register the speaker as an audio sink
            addAudioSinkRegistration(thing, handler);

            // Return the handler
            return handler;
        } else {
            return null;
        }
    }

    @Override
    public void unregisterHandler(final Thing thing) {
        super.unregisterHandler(thing);
        removeAudioSinkRegistration(thing);
    }

    private void addAudioSinkRegistration(final @NonNull Thing thing, final @NonNull ThingHandlerChromecast handler) {
        final @NonNull AudioHTTPServer audioHttpServer = this.audioHttpServer;
        final ChromecastAudioSink audioSink = new ChromecastAudioSink(bundleContext, handler, audioHttpServer);
        final ServiceRegistration<AudioSink> reg = bundleContext.registerService(AudioSink.class, audioSink, null);
        final String thingUid = thing.getUID().toString();
        audioSinkRegistrations.put(thingUid, reg);
    }

    private void removeAudioSinkRegistration(final @NonNull Thing thing) {
        final @NonNull String thingUid = thing.getUID().toString();
        final ServiceRegistration<AudioSink> reg = audioSinkRegistrations.remove(thingUid);
        if (reg != null) {
            reg.unregister();
        }
    }

}
