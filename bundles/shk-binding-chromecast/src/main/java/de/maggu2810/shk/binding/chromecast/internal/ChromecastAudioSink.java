/*-
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioHTTPServer;
import org.eclipse.smarthome.core.audio.AudioSink;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.audio.FixedLengthAudioStream;
import org.eclipse.smarthome.core.audio.URLAudioStream;
import org.eclipse.smarthome.core.audio.UnsupportedAudioFormatException;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.net.HttpServiceUtil;
import org.eclipse.smarthome.core.net.NetUtil;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChromecastAudioSink implements AudioSink {

    private static class SupportedAudioFormats extends HashSet<AudioFormat> {
        private static final long serialVersionUID = 1L;

        public SupportedAudioFormats() {
            add(AudioFormat.WAV);
            add(AudioFormat.MP3);
        }
    }

    private static final Set<AudioFormat> SUPPORTED_FORMATS = Collections.unmodifiableSet(new SupportedAudioFormats());

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final BundleContext bc;
    private final AudioHTTPServer audioHttpServer;
    private final ThingHandlerChromecast handler;

    /**
     * Create a new ChromeCast audio sink.
     *
     * @param bc the bundle context
     * @param handler chromecast thing handler
     * @param audioHttpServer audio server
     */
    public ChromecastAudioSink(final BundleContext bc, final ThingHandlerChromecast handler,
            final AudioHTTPServer audioHttpServer) {
        this.bc = bc;
        this.handler = handler;
        this.audioHttpServer = audioHttpServer;
    }

    @Override
    public String getId() {
        return handler.getThing().getUID().toString();
    }

    @Override
    public String getLabel(final Locale locale) {
        return handler.getThing().getLabel();
    }

    @Override
    public void process(final AudioStream audioStream) throws UnsupportedAudioFormatException {
        final String title = "AudioSink";

        final String mimeType;
        if (AudioFormat.MP3.isCompatible(audioStream.getFormat())) {
            mimeType = "audio/mpeg";
        } else if (AudioFormat.WAV.isCompatible(audioStream.getFormat())) {
            mimeType = "audio/wav";
        } else {
            mimeType = null;
        }

        if (audioStream instanceof URLAudioStream) {
            // it is an external URL, the speaker can access it itself and play it.
            final URLAudioStream urlAudioStream = (URLAudioStream) audioStream;
            handler.playUri(urlAudioStream.getURL(), title, mimeType);
            IOUtils.closeQuietly(audioStream);
        } else if (audioStream instanceof FixedLengthAudioStream) {
            final String relativeUrl = audioHttpServer.serve((FixedLengthAudioStream) audioStream, 10);
            final String absoluteUrl = getAbsoluteUrl(relativeUrl);
            if (absoluteUrl != null) {
                handler.playUri(absoluteUrl, title, mimeType);
            }
        } else {
            IOUtils.closeQuietly(audioStream);
            throw new UnsupportedAudioFormatException(
                    String.format("Don't know how to handle given audio stream of class %s",
                            audioStream.getClass().getSimpleName()),
                    null);
        }
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return SUPPORTED_FORMATS;
    }

    @Override
    public PercentType getVolume() {
        final PercentType volume = handler.getVolume();
        if (volume != null) {
            return volume;
        } else {
            return PercentType.HUNDRED;
        }
    }

    @Override
    public void setVolume(final PercentType volume) {
        handler.setVolume(volume.intValue());
    }

    /**
     * Get an aboslute URL for a relative one.
     *
     * @param relativeUrl the relative URL
     * @return an absolute URL on success, null if no absolute URL could be built.
     */
    private String getAbsoluteUrl(final String relativeUrl) {
        final String ipAddress = NetUtil.getLocalIpv4HostAddress();
        if (ipAddress == null) {
            logger.warn("Don't find a candidate for a local IPv4 address.");
            return null;
        }

        // Let's use the unsecure port to prevent certificate issues.
        final int port = HttpServiceUtil.getHttpServicePort(bc);
        if (port == -1) {
            logger.warn("Cannot identify the port of the HTTP service.");
            return null;
        }

        return String.format("http://%s:%d%s", ipAddress, port, relativeUrl);
    }

}
