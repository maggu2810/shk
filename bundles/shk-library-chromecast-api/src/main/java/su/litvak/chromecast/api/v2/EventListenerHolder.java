
package su.litvak.chromecast.api.v2;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import su.litvak.chromecast.api.v2.ChromeCastMessageEvent.SpontaneousEventType;

class EventListenerHolder implements ChromeCastConnectionEventListener, ChromeCastMessageEventListener {

    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final Set<ChromeCastConnectionEventListener> eventListenersConnection = new CopyOnWriteArraySet<ChromeCastConnectionEventListener>();
    private final Set<ChromeCastMessageEventListener> eventListenersMessage = new CopyOnWriteArraySet<ChromeCastMessageEventListener>();

    public EventListenerHolder() {
    }

    public void registerConnectionListener(final ChromeCastConnectionEventListener listener) {
        if (listener != null) {
            this.eventListenersConnection.add(listener);
        }
    }

    public void unregisterConnectionListener(final ChromeCastConnectionEventListener listener) {
        if (listener != null) {
            this.eventListenersConnection.remove(listener);
        }
    }

    public void registerMessageListener(final ChromeCastMessageEventListener listener) {
        if (listener != null) {
            this.eventListenersMessage.add(listener);
        }
    }

    public void unregisterMessageListener(final ChromeCastMessageEventListener listener) {
        if (listener != null) {
            this.eventListenersMessage.remove(listener);
        }
    }

    public void deliverConnectionEvent(final boolean connected) {
        connectionEventReceived(new ChromeCastConnectionEvent(connected));
    }

    public void deliverMessageEvent(final boolean spontaneous, final JsonNode json) throws IOException {
        if (json == null) {
            return;
        }
        if (this.eventListenersMessage.size() < 1) {
            return;
        }

        final StandardResponse resp = this.jsonMapper.readValue(json, StandardResponse.class);

        /*
         * The documentation only mentions MEDIA_STATUS as being a possible spontaneous event.
         * Though RECEIVER_STATUS has also been observed.
         * If others are observed, they should be added here.
         * see: https://developers.google.com/cast/docs/reference/messages#MediaMess
         */
        if (resp instanceof StandardResponse.MediaStatus) {
            for (final MediaStatus ms : ((StandardResponse.MediaStatus) resp).statuses) {
                messageEventReceived(new ChromeCastMessageEvent(SpontaneousEventType.MEDIA_STATUS, ms));
            }
        } else if (resp instanceof StandardResponse.Status) {
            messageEventReceived(new ChromeCastMessageEvent(SpontaneousEventType.STATUS,
                    ((StandardResponse.Status) resp).status));
        } else {
            messageEventReceived(new ChromeCastMessageEvent(SpontaneousEventType.UNKNOWN, json));
        }
    }

    @Override
    public void connectionEventReceived(ChromeCastConnectionEvent event) {
        for (final ChromeCastConnectionEventListener listener : this.eventListenersConnection) {
            listener.connectionEventReceived(event);
        }
    }

    @Override
    public void messageEventReceived(final ChromeCastMessageEvent event) {
        for (final ChromeCastMessageEventListener listener : this.eventListenersMessage) {
            listener.messageEventReceived(event);
        }
    }

}
