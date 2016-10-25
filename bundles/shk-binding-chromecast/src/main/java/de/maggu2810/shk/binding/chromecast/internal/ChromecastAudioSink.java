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

public class ChromecastAudioSink implements AudioSink {

    private static class SupportedAudioFormats extends HashSet<AudioFormat> {
        private static final long serialVersionUID = 1L;

        public SupportedAudioFormats() {
            add(AudioFormat.WAV);
            add(AudioFormat.MP3);
        }
    }

    private static final Set<AudioFormat> SUPPORTED_FORMATS = Collections.unmodifiableSet(new SupportedAudioFormats());

    private final AudioHTTPServer audioHttpServer;
    private final ThingHandlerChromecast handler;

    /**
     * Create a new ChromeCast audio sink.
     *
     * @param handler chromecast thing handler
     * @param audioHttpServer audio server
     */
    public ChromecastAudioSink(final ThingHandlerChromecast handler, final AudioHTTPServer audioHttpServer) {
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
        if (audioStream instanceof URLAudioStream) {
            // it is an external URL, the speaker can access it itself and play it.
            final URLAudioStream urlAudioStream = (URLAudioStream) audioStream;
            handler.playUri(urlAudioStream.getURL());
            IOUtils.closeQuietly(audioStream);
        } else {
            // we serve it on our own HTTP server and treat it as a notification
            if (!(audioStream instanceof FixedLengthAudioStream)) {
                // Note that we have to pass a FixedLengthAudioStream, since Sonos does multiple concurrent requests to
                // the AudioServlet, so a one time serving won't work.
                throw new UnsupportedAudioFormatException("Sonos can only handle FixedLengthAudioStreams.", null);
                // TODO: Instead of throwing an exception, we could ourselves try to wrap it into a
                // FixedLengthAudioStream, but this might be dangerous as we have no clue, how much data to expect from
                // the stream.
            } else {
                final String url = audioHttpServer.serve((FixedLengthAudioStream) audioStream, 10).toString();
                final AudioFormat format = audioStream.getFormat();
                if (AudioFormat.WAV.isCompatible(format)) {
                    handler.playUri(url + ".wav");
                } else if (AudioFormat.MP3.isCompatible(format)) {
                    handler.playUri(url + ".mp3");
                } else {
                    throw new UnsupportedAudioFormatException("Sonos only supports MP3 or WAV.", format);
                }
            }
        }
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return SUPPORTED_FORMATS;
    }

    @Override
    public PercentType getVolume() {
        // TODO
        return PercentType.HUNDRED;
    }

    @Override
    public void setVolume(final PercentType volume) {
        handler.setVolume(volume.intValue());
    }

}
