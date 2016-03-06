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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ThingHandlerFactoryImpl extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays
            .asList(new ThingTypeUID[] { BindingConstants.THING_TYPE_CHROMECAST })));

    @Override
    protected ThingHandler createHandler(final Thing thing) {
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(BindingConstants.THING_TYPE_CHROMECAST)) {
            return new ThingHandlerChromecast(thing);
        } else {
            return null;
        }
    }

    @Override
    public boolean supportsThingType(final ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }
}
