/*
 * #%L
 * shk :: Bundles :: Binding :: Chromecast
 * %%
 * Copyright (C) 2015 - 2017 maggu2810
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */

package de.maggu2810.shk.binding.chromecast.internal;

import java.io.Closeable;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
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
        implements ChromeCastConnectionEventListener, ChromeCastSpontaneousEventListener, Closeable {

    private static final String MEDIA_PLAYER = "CC1AD845";

    private final Logger logger = LoggerFactory.getLogger(ThingHandlerChromecast.class);

    private @Nullable ChromeCast chromecast;
    private @Nullable ScheduledFuture<?> futureConnect;
    private @Nullable PercentType volume;

    /**
     * Constructor.
     *
     * @param thing the thing the handler should be created for
     */
    public ThingHandlerChromecast(final @NonNull Thing thing) {
        super(thing);
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

        this.chromecast = getInitializedChromeCast(host, this.chromecast);
    }

    @Override
    public void dispose() {
        close();
    }

    @Override
    public void close() {
        closeChromeCast(this.chromecast);
        this.chromecast = null;
    }

    private @NonNull ChromeCast getInitializedChromeCast(final @NonNull String host,
            final @Nullable ChromeCast oldChromeCast) {
        if (oldChromeCast != null) {
            if (oldChromeCast.getAddress().equals(host)) {
                return oldChromeCast;
            } else {
                closeChromeCast(oldChromeCast);
            }
        }

        final ChromeCast chromecast = new ChromeCast(host);
        chromecast.registerConnectionListener(this);
        chromecast.registerMessageListener(this);
        scheduleConnect(true);
        return chromecast;
    }

    private void closeChromeCast(final @Nullable ChromeCast chromecast) {
        if (chromecast == null) {
            return;
        }

        cancelScheduledConnect();

        chromecast.unregisterMessageListener(this);
        chromecast.unregisterConnectionListener(this);

        try {
            chromecast.disconnect();
        } catch (final IOException ex) {
            logger.debug("Disconnect failed", ex);
        }
    }

    protected @NonNull ChromeCast getChromeCast() {
        if (chromecast != null) {
            return chromecast;
        } else {
            throw new IllegalStateException("This function must not be called if the handler is not initialized.");
        }
    }

    private void scheduleConnect(final boolean immediate) {
        cancelScheduledConnect();

        final long delay = immediate ? 0 : 10;
        futureConnect = scheduler.schedule(() -> {
            try {
                getChromeCast().connect();
            } catch (final IOException | GeneralSecurityException ex) {
                logger.debug("Cannot connect chromecast.", ex);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        "Cannot connect to chromecast.");
                scheduleConnect(false);
            }
        }, delay, TimeUnit.SECONDS);
    }

    private void cancelScheduledConnect() {
        if (futureConnect != null) {
            futureConnect.cancel(true);
            futureConnect = null;
        }
    }

    @Override
    public void handleCommand(final @NonNull ChannelUID channelUID, final @NonNull Command command) {
        switch (channelUID.getId()) {
            case BindingConstants.Channel.CONTROL:
                handleCommandControl(command);
                break;
            case BindingConstants.Channel.PLAY:
                handleCommandPlay(command);
                break;
            case BindingConstants.Channel.PLAYURI:
                if (command instanceof StringType) {
                    final String url = ((StringType) command).toString();
                    playUri(url, null, null);
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

    private void handleCommandControl(final @NonNull Command command) {
        if (command instanceof PlayPauseType) {
            final PlayPauseType playPause = (PlayPauseType) command;
            try {
                if (playPause == PlayPauseType.PLAY) {
                    getChromeCast().play();
                } else {
                    getChromeCast().pause();
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
            // getChromeCast().seek(SEEK_TIME);
            // } else {
            // getChromeCast().seek(-SEEK_TIME);
            // }
            // } catch (final IOException ex) {
            // logger.debug("Cannot handle rewind / fast forward control command.", ex);
            // }
        }
    }

    private void handleCommandPlay(final @NonNull Command command) {
        if (command instanceof OnOffType) {
            final OnOffType onOff = (OnOffType) command;
            try {
                if (onOff == OnOffType.ON) {
                    getChromeCast().play();
                } else {
                    getChromeCast().pause();
                }
            } catch (final IOException ex) {
                logger.debug("Play / pause failed.", ex);
            }
        }
    }

    void playUri(final @NonNull String url, final @Nullable String title, final @Nullable String mimeType) {
        // Test URI: http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4

        final @NonNull String customTitle;
        if (title == null) {
            customTitle = url.substring(url.lastIndexOf('/') + 1, url.lastIndexOf('.'));
        } else {
            customTitle = title;
        }

        try {
            final Status status = getChromeCast().getStatus();
            if (getChromeCast().isAppAvailable(MEDIA_PLAYER)) {
                if (!status.isAppRunning(MEDIA_PLAYER)) {
                    final Application app = getChromeCast().launchApp(MEDIA_PLAYER);
                    logger.debug("Application launched: {}", app);
                }
                getChromeCast().load(customTitle, null, url, mimeType);
                updateState(BindingConstants.Channel.PLAYURI, new StringType(url));
            } else {
                logger.error("Missing media player app");
            }
        } catch (final IOException ex) {
            logger.debug("Play URI failed.", ex);
        }
    }

    protected void setVolume(final int percent) {
        try {
            getChromeCast().setVolume(percent / 100f);
            updateState(BindingConstants.Channel.VOLUME, new PercentType(percent));
        } catch (final IOException ex) {
            logger.debug("Set volume failed.", ex);
        }
    }

    protected @Nullable PercentType getVolume() {
        return volume;
    }

    private void handleCcConnected() {
        updateStatus(ThingStatus.ONLINE);
        try {
            final Status status = getChromeCast().getStatus();
            handleCcStatus(status);
        } catch (final IOException ex) {
            logger.debug("Cannot fetch status.", ex);
        }
    }

    private void handleCcDisconnected() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Connection has been closed.");
        scheduleConnect(false);
    }

    private void handleCcStatus(final @NonNull Status status) {
        handleCcVolume(status.volume);
    }

    private void handleCcMediaStatus(final @NonNull MediaStatus mediaStatus) {
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

    private void handleCcVolume(final @NonNull Volume volume) {
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
