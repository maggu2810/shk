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

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.RewindFastforwardType;
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
import su.litvak.chromecast.api.v2.ChromeCastConnectionEvent;
import su.litvak.chromecast.api.v2.ChromeCastConnectionEventListener;
import su.litvak.chromecast.api.v2.ChromeCastSpontaneousEvent;
import su.litvak.chromecast.api.v2.ChromeCastSpontaneousEventListener;
import su.litvak.chromecast.api.v2.MediaStatus;
import su.litvak.chromecast.api.v2.Status;
import su.litvak.chromecast.api.v2.Volume;

public class ThingHandlerChromecast extends BaseThingHandler
        implements ChromeCastConnectionEventListener, ChromeCastSpontaneousEventListener {

    private static final String MEDIA_PLAYER = "CC1AD845";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ChromeCast chromecast;
    private ScheduledFuture<?> futureConnect;
    private PercentType volume;

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
        chromecast.registerConnectionListener(this);
        chromecast.registerMessageListener(this);
    }

    private void destroyChromecast() {
        chromecast.unregisterMessageListener(this);
        chromecast.unregisterConnectionListener(this);
        try {
            chromecast.disconnect();
        } catch (final IOException ex) {
            logger.debug("Disconnect failed", ex);
        }
        chromecast = null;
    }

    private void scheduleConnect(final boolean immediate) {
        final long delay = immediate ? 0 : 10;
        if (futureConnect != null) {
            futureConnect.cancel(true);
            futureConnect = null;
        }
        futureConnect = scheduler.schedule(() -> {
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
    public void initialize() {
        final Object obj = getConfig().get(BindingConstants.Config.HOST);
        if (!(obj instanceof String)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Cannot connect to chromecats. IP address configuration corrupt");
            return;
        }
        final String host = (String) obj;
        if (StringUtils.isBlank(host)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Cannot connect to chromecats. IP address not set.");
            return;
        }

        if (chromecast != null && !chromecast.getAddress().equals(host)) {
            destroyChromecast();
        }
        if (chromecast == null) {
            createChromecast(host);
        }

        scheduleConnect(true);
    }

    @Override
    public void dispose() {
        if (chromecast != null) {
            destroyChromecast();
        }
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        if (chromecast == null) {
            return;
        }

        switch (channelUID.getId()) {
            case BindingConstants.Channel.CONTROL:
                handleCommandControl(command);
                break;
            case BindingConstants.Channel.PLAY:
                handleCommandPlay(command);
                break;
            case BindingConstants.Channel.PLAYURI:
                if (command instanceof StringType) {
                    playUri(((StringType) command).toString(), null, null);
                }
                break;
            case BindingConstants.Channel.VOLUME:
                if (command instanceof PercentType) {
                    setVolume(((PercentType) command).intValue());
                }
                break;
            default:
                logger.debug("unhandled channel: {}", channelUID);
                break;
        }
    }

    private void handleCommandControl(final Command command) {
        if (command instanceof PlayPauseType) {
            final PlayPauseType playPause = (PlayPauseType) command;
            try {
                if (playPause == PlayPauseType.PLAY) {
                    chromecast.play();
                } else {
                    chromecast.pause();
                }
            } catch (final IOException ex) {
                logger.debug("Cannot handle play / pause control command.", ex);
            }
        }
        if (command instanceof RewindFastforwardType) {
            logger.info("Seek takes one argument that consists (at least if my tests are correct) of the number of "
                    + "seconds in the stream that should be jumped to. So we have to use the current time and add"
                    + "or subtract the seek time.");
            // final RewindFastforwardType rewFwd = (RewindFastforwardType) command;
            // try {
            // if (rewFwd == RewindFastforwardType.FASTFORWARD) {
            // chromecast.seek(SEEK_TIME);
            // } else {
            // chromecast.seek(-SEEK_TIME);
            // }
            // } catch (final IOException ex) {
            // logger.debug("Cannot handle rewind / fast forward control command.", ex);
            // }
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

    void playUri(final String url, final String title, final String mimeType) {
        // Test URI: http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4

        final String customTitle;
        if (title == null) {
            customTitle = url.substring(url.lastIndexOf('/') + 1, url.lastIndexOf('.'));
        } else {
            customTitle = title;
        }

        try {
            final Status status = chromecast.getStatus();
            if (chromecast.isAppAvailable(MEDIA_PLAYER)) {
                if (!status.isAppRunning(MEDIA_PLAYER)) {
                    final Application app = chromecast.launchApp(MEDIA_PLAYER);
                    logger.debug("Application launched: {}", app);
                }

                chromecast.load(customTitle, null, url, mimeType);
                updateState(BindingConstants.Channel.PLAYURI, new StringType(url));
            } else {
                logger.error("Missing media player app");
            }
        } catch (final IOException ex) {
            logger.debug("Play URI failed.", ex);
        }
    }

    void setVolume(final int percent) {
        try {
            chromecast.setVolume(percent / 100f);
            updateState(BindingConstants.Channel.VOLUME, new PercentType(percent));
        } catch (final IOException ex) {
            logger.debug("Set volume failed.", ex);
        }
    }

    PercentType getVolume() {
        return volume;
    }

    private void handleCcConnected() {
        updateStatus(ThingStatus.ONLINE);
        try {
            handleCcStatus(chromecast.getStatus());
        } catch (final IOException ex) {
            logger.debug("Cannot fetch status.", ex);
        }
    }

    private void handleCcDisconnected() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Connection has been closed.");
        scheduleConnect(false);
    }

    private void handleCcStatus(final Status status) {
        handleCcVolume(status.volume);
    }

    private void handleCcMediaStatus(final MediaStatus mediaStatus) {
        switch (mediaStatus.playerState) {
            case IDLE:
            case PAUSED:
                updateState(BindingConstants.Channel.PLAY, OnOffType.OFF);
                break;
            case BUFFERING:
            case PLAYING:
                updateState(BindingConstants.Channel.PLAY, OnOffType.ON);
                break;
            default:
                break;
        }
        // Seems incorrect. I get a 1 (100%) if I start to play an URI.
        // handleCcVolume(mediaStatus.volume);
    }

    private void handleCcVolume(final Volume volume) {
        this.volume = new PercentType((int) (volume.level * 100));
        updateState(BindingConstants.Channel.VOLUME, this.volume);
    }

    @Override
    public void spontaneousEventReceived(final ChromeCastSpontaneousEvent event) {
        switch (event.getType()) {
            case MEDIA_STATUS:
                final MediaStatus mediaStatus = event.getData(MediaStatus.class);
                handleCcMediaStatus(mediaStatus);
                break;
            case STATUS:
                final Status status = event.getData(Status.class);
                handleCcStatus(status);
                break;
            case UNKNOWN:
                // final JsonNode jsonNode = event.getData(JsonNode.class);
                // logger.info("json node: {}", jsonNode);
                logger.warn("Received an 'UNKNOWN' event (class={})", event.getType().getDataClass());
                break;
            default:
                logger.info("unhandled event type: {}", event.getData());
                break;

        }
    }

    @Override
    public void connectionEventReceived(final ChromeCastConnectionEvent event) {
        if (event.isConnected()) {
            handleCcConnected();
        } else {
            handleCcDisconnected();
        }
    }

}
