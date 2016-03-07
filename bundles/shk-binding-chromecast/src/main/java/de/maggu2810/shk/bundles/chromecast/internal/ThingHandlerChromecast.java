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

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import su.litvak.chromecast.api.v2.ChromeCast;

public class ThingHandlerChromecast extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ChromeCast chromecast;

    /**
     * Constructor.
     *
     * @param thing the thing the handler should be created for
     */
    public ThingHandlerChromecast(final Thing thing) {
        super(thing);
    }

    @Override
    public void dispose() {
        if (chromecast != null) {
            try {
                chromecast.disconnect();
            } catch (final IOException ex) {
                logger.debug("Disconnect failed", ex);
            }
            chromecast = null;
        }
    }

    @Override
    public void initialize() {
        if (getConfig().get(BindingConstants.HOST) != null) {
            if (chromecast == null) {
                chromecast = new ChromeCast((String) getConfig().get(BindingConstants.HOST));
                try {
                    chromecast.connect();
                    updateStatus(ThingStatus.ONLINE);
                } catch (final IOException | GeneralSecurityException ex) {
                    logger.debug("Connect failed", ex);
                }
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Cannot connect to chromecats. IP address not set.");
        }
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        logger.info("TODO: handle command");
    }

}
