
package su.litvak.chromecast.api.v2;

public class ChromeCastConnectionEvent {

    final boolean connected;

    public ChromeCastConnectionEvent(final boolean connected) {
        this.connected = connected;
    }

    public boolean isConnected() {
        return connected;
    }
}
