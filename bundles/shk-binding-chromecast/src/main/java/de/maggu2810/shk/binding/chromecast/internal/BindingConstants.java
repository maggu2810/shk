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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

public class BindingConstants {

    private BindingConstants() {
    }

    public static final @NonNull String BINDING_ID = "chromecast";

    public static class ThingType {
        private ThingType() {
        }

        public static final @NonNull ThingTypeUID CHROMECAST = new ThingTypeUID(BINDING_ID, "chromecast");
    }

    // channel IDs
    public static class Channel {
        private Channel() {
        }

        public static final @NonNull String CONTROL = "control";
        public static final @NonNull String PLAY = "play";
        public static final @NonNull String PLAYURI = "playuri";
        public static final @NonNull String VOLUME = "volume";
    }

    // config parameters
    public static class Config {
        private Config() {
        }

        public static final @NonNull String HOST = "ipAddress";
    }

    // properties
    public static class Properties {
        private Properties() {
        }

        public static final @NonNull String SERIAL_NUMBER = "serialNumber";
    }
}
