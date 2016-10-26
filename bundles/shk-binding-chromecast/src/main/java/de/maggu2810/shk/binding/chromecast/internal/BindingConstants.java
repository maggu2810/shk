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

    public static class ThingType {
        private ThingType() {
        }

        public static final ThingTypeUID CHROMECAST = new ThingTypeUID(BINDING_ID, "chromecast");
    }

    // channel IDs
    public static class Channel {
        private Channel() {
        }

        public static final String PLAY = "play";
        public static final String PLAYURI = "playuri";
        public static final String VOLUME = "volume";
    }

    // config properties
    public static class Config {
        private Config() {
        }

        public static final String HOST = "ipAddress";
    }
}
