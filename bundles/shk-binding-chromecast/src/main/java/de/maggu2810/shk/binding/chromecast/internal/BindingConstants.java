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

import org.eclipse.smarthome.core.thing.ThingTypeUID;

public class BindingConstants {

    private BindingConstants() {
    }

    public static final String BINDING_ID = "chromecast";

    public static final ThingTypeUID THING_TYPE_CHROMECAST = new ThingTypeUID(BINDING_ID, "chromecast");

    // channel IDs
    public static final String CHANNEL_PLAY = "play";
    public static final String CHANNEL_PLAYURI = "playuri";
    public static final String CHANNEL_VOLUME = "volume";

    // config properties
    public static final String HOST = "ipAddress";
}
