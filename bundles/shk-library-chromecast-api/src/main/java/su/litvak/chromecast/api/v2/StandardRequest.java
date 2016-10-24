/*
 * #%L
 * shk :: Bundles :: Library :: Chromecast API
 * %%
 * Copyright (C) 2014 Vitaly Litvak (vitavaque@gmail.com) and others.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package su.litvak.chromecast.api.v2;

import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Parent class for transport object representing messages sent TO ChromeCast device
 */
abstract class StandardRequest extends StandardMessage implements Request {
    Long requestId;

    @Override
    public final void setRequestId(final Long requestId) {
        this.requestId = requestId;
    }

    @Override
    public final Long getRequestId() {
        return requestId;
    }

    static class Status extends StandardRequest {
    }

    static class AppAvailability extends StandardRequest {
        @JsonProperty
        final String[] appId;

        AppAvailability(final String... appId) {
            this.appId = appId;
        }
    }

    static class Launch extends StandardRequest {
        @JsonProperty
        final String appId;

        Launch(final String appId) {
            this.appId = appId;
        }
    }

    static class Stop extends StandardRequest {
        @JsonProperty
        final String sessionId;

        Stop(final String sessionId) {
            this.sessionId = sessionId;
        }
    }

    static class Load extends StandardRequest {
        @JsonProperty
        final String sessionId;
        @JsonProperty
        final Media media;
        @JsonProperty
        final boolean autoplay;
        @JsonProperty
        final double currentTime;
        @JsonProperty
        final Object customData;

        Load(final String sessionId, final Media media, final boolean autoplay, final double currentTime,
                final Map<String, String> customData) {
            this.sessionId = sessionId;
            this.media = media;
            this.autoplay = autoplay;
            this.currentTime = currentTime;

            this.customData = customData == null ? null : new Object() {
                @JsonProperty
                Map<String, String> payload = customData;
            };
        }
    }

    abstract static class MediaRequest extends StandardRequest {
        @JsonProperty
        final long mediaSessionId;
        @JsonProperty
        final String sessionId;

        MediaRequest(final long mediaSessionId, final String sessionId) {
            this.mediaSessionId = mediaSessionId;
            this.sessionId = sessionId;
        }
    }

    static class Play extends MediaRequest {
        Play(final long mediaSessionId, final String sessionId) {
            super(mediaSessionId, sessionId);
        }
    }

    static class Pause extends MediaRequest {
        Pause(final long mediaSessionId, final String sessionId) {
            super(mediaSessionId, sessionId);
        }
    }

    static class Seek extends MediaRequest {
        @JsonProperty
        final double currentTime;

        Seek(final long mediaSessionId, final String sessionId, final double currentTime) {
            super(mediaSessionId, sessionId);
            this.currentTime = currentTime;
        }
    }

    static class SetVolume extends StandardRequest {
        @JsonProperty
        final Volume volume;

        SetVolume(final Volume volume) {
            this.volume = volume;
        }
    }

    static Status status() {
        return new Status();
    }

    static AppAvailability appAvailability(final String... appId) {
        return new AppAvailability(appId);
    }

    static Launch launch(final String appId) {
        return new Launch(appId);
    }

    static Stop stop(final String sessionId) {
        return new Stop(sessionId);
    }

    static Load load(final String sessionId, final Media media, final boolean autoplay, final double currentTime,
            final Map<String, String> customData) {
        return new Load(sessionId, media, autoplay, currentTime, customData);
    }

    static Play play(final String sessionId, final long mediaSessionId) {
        return new Play(mediaSessionId, sessionId);
    }

    static Pause pause(final String sessionId, final long mediaSessionId) {
        return new Pause(mediaSessionId, sessionId);
    }

    static Seek seek(final String sessionId, final long mediaSessionId, final double currentTime) {
        return new Seek(mediaSessionId, sessionId, currentTime);
    }

    static SetVolume setVolume(final Volume volume) {
        return new SetVolume(volume);
    }
}
