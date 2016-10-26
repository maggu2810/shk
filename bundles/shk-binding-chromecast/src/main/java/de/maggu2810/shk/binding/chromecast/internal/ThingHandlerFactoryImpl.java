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

    @Reference
    private AudioHTTPServer audioHttpServer;

    private final Map<String, ServiceRegistration<AudioSink>> audioSinkRegistrations = new ConcurrentHashMap<>();

    // private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(new ThingTypeUID[] { BindingConstants.ThingType.CHROMECAST })));

    @Override
    public boolean supportsThingType(final ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(final Thing thing) {
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(BindingConstants.ThingType.CHROMECAST)) {
            final ThingHandlerChromecast handler = new ThingHandlerChromecast(thing);

            // register the speaker as an audio sink
            final ChromecastAudioSink audioSink = new ChromecastAudioSink(handler, audioHttpServer);
            final ServiceRegistration<AudioSink> reg = bundleContext.registerService(AudioSink.class, audioSink, null);
            audioSinkRegistrations.put(thing.getUID().toString(), reg);
            return handler;
        } else {
            return null;
        }
    }

    @Override
    public void unregisterHandler(final Thing thing) {
        super.unregisterHandler(thing);
        final ServiceRegistration<AudioSink> reg = audioSinkRegistrations.get(thing.getUID().toString());
        if (reg != null) {
            reg.unregister();
        }
    }

}
