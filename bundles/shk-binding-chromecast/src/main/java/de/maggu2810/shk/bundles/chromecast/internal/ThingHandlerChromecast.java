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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.codehaus.jackson.JsonNode;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import su.litvak.chromecast.api.v2.Application;
import su.litvak.chromecast.api.v2.ChromeCast;
import su.litvak.chromecast.api.v2.ChromeCastSpontaneousEvent;
import su.litvak.chromecast.api.v2.ChromeCastSpontaneousEventListener;
import su.litvak.chromecast.api.v2.MediaStatus;
import su.litvak.chromecast.api.v2.Status;

public class ThingHandlerChromecast extends BaseThingHandler implements ChromeCastSpontaneousEventListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ChromeCast chromecast;
    private ScheduledFuture<?> futureConnect;

    private final String MEDIA_PLAYER = "CC1AD845";

    /**
     * Constructor.
     *
     * @param thing the thing the handler should be created for
     */
    public ThingHandlerChromecast(final Thing thing) {
        super(thing);
    }

    private void createChromecast(final String address) {
        chromecast = new ChromeCast(address);
        chromecast.registerListener(this);
    }

    private void destroyChromecast() {
        chromecast.unregisterListener(this);
        try {
            chromecast.disconnect();
        } catch (final IOException ex) {
            logger.debug("Disconnect failed", ex);
        }
        chromecast = null;
    }

    private void scheduleConnect(boolean immediate) {
        final long delay = immediate ? 0 : 10;
        if (futureConnect != null) {
            futureConnect.cancel(true);
            futureConnect = null;
        }
        futureConnect = scheduler.schedule(
                () -> {
                    try {
                        chromecast.connect();
                    } catch (final Exception ex) {
                        logger.debug("Cannot connect chromecast.", ex);
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                                "Cannot connect to chromecast.");
                        scheduleConnect(false);
                    }
                }, delay, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        if (chromecast != null) {
            destroyChromecast();
        }
    }

    @Override
    public void initialize() {
        final Object obj = getConfig().get(BindingConstants.HOST);
        if (obj == null || !(obj instanceof String)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Cannot connect to chromecats. " + obj == null ? "IP address not set."
                            : "IP address configuration corrupt");
            return;
        }

        final String host = (String) obj;
        if (chromecast != null && !chromecast.getAddress().equals(host)) {
            destroyChromecast();
        }
        if (chromecast == null) {
            createChromecast(host);
        }

        scheduleConnect(true);
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        if (chromecast == null) {
            return;
        }

        switch (channelUID.getId()) {
            case BindingConstants.CHANNEL_PLAY:
                handleCommandPlay(command);
                break;
            case BindingConstants.CHANNEL_PLAYURI:
                handleCommandPlayUri(command);
                break;
            case BindingConstants.CHANNEL_VOLUME:
                handleCommandVolume(command);
                break;
            default:
                logger.debug("unhandled channel: {}", channelUID);
                break;
        }
    }

    private void handleCommandPlay(final Command command) {
        if (command instanceof OnOffType) {
            final OnOffType onOff = (OnOffType) command;
            try {
                if (onOff == OnOffType.ON) {
                    chromecast.play();
                } else {
                    chromecast.pause();
                }
            } catch (final IOException ex) {
                logger.debug("Play / pause failed.", ex);
            }
        }
    }

    private void handleCommandPlayUri(final Command command) {
        if (command instanceof StringType) {
            // Test URI: http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4
            final StringType uri = (StringType) command;
            try {
                final Status status = chromecast.getStatus();
                if (chromecast.isAppAvailable(MEDIA_PLAYER)) {
                    if (!status.isAppRunning(MEDIA_PLAYER)) {
                        final Application app = chromecast.launchApp(MEDIA_PLAYER);
                        logger.debug("Application launched: {}", app);
                    }
                    chromecast.load(uri.toString());
                } else {
                    logger.error("Missing media player app");
                }
            } catch (final IOException ex) {
                logger.debug("Play URI failed.", ex);
            }
        }
    }

    private void handleCommandVolume(final Command command) {
        if (command instanceof PercentType) {
            final PercentType num = (PercentType) command;
            try {
                chromecast.setVolume(num.floatValue() / 100);
            } catch (final IOException ex) {
                logger.debug("Set volume failed.", ex);
            }
        }
    }

    @Override
    public void spontaneousEventReceived(final ChromeCastSpontaneousEvent event) {
        switch (event.getType()) {
            case CONNECTION_STATUS:
                final boolean connected = event.getData(Boolean.class);
                if (connected) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                            "Connection has been closed.");
                    scheduleConnect(false);
                }
                logger.info("connected: {}", connected);
                break;
            case MEDIA_STATUS:
                final MediaStatus mediaStatus = event.getData(MediaStatus.class);
                logger.info("media status: {}", mediaStatus);
                break;
            case STATUS:
                final Status status = event.getData(Status.class);
                logger.info("status: {}", status);
                break;
            case UNKNOWN:
                final JsonNode jsonNode = event.getData(JsonNode.class);
                logger.info("json node: {}", jsonNode);
                break;
            default:
                logger.info("unhandled event type: {}", event.getData());
                break;

        }
    }

}
