
package de.maggu2810.shk.binding.chromecast.internal;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.State;

public interface SimpleThingHandler {

    /**
     * Updates the status of the thing. The detail of the status will be 'NONE'.
     *
     * @param status the status
     * @throws IllegalStateException if handler is not initialized correctly, because no callback is present
     */
    void updateStatus(@NonNull ThingStatus status);

    /**
     * Updates the status of the thing.
     *
     * @param status the status
     * @param statusDetail the detail of the status
     * @throws IllegalStateException if handler is not initialized correctly, because no callback is present
     */
    void updateStatus(@NonNull ThingStatus status, @NonNull ThingStatusDetail statusDetail);

    /**
     * Updates the status of the thing.
     *
     * @param status the status
     * @param statusDetail the detail of the status
     * @param description the description of the status
     * @throws IllegalStateException if handler is not initialized correctly, because no callback is present
     */
    void updateStatus(@NonNull ThingStatus status, @NonNull ThingStatusDetail statusDetail,
            @NonNull String description);

    /**
     *
     * Updates the state of the thing.
     *
     * @param channelUid unique id of the channel, which was updated
     * @param state new state
     * @throws IllegalStateException if handler is not initialized correctly, because no callback is present
     */
    void updateState(@NonNull ChannelUID channelUid, @NonNull State state);

    /**
     *
     * Updates the state of the thing. Will use the thing UID to infer the unique channel UID.
     *
     * @param channel ID id of the channel, which was updated
     * @param state new state
     * @throws IllegalStateException if handler is not initialized correctly, because no callback is present
     */
    void updateState(@NonNull String channelID, @NonNull State state);

}
