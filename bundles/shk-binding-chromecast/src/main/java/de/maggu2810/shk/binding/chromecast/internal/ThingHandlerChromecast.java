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

import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

public class ThingHandlerChromecast extends BaseThingHandler implements SimpleThingHandler {

    private @Nullable InitializedChromeCastHandler ccHandler;

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

        if (ccHandler != null && !ccHandler.getAddress().equals(host)) {
            assert ccHandler != null : "@AssumeAssertion(nullness)";
            ccHandler.close();
            ccHandler = null;
        }
        if (ccHandler == null) {
            assert scheduler != null : "@AssumeAssertion(nullness)";
            ccHandler = new InitializedChromeCastHandler(host, scheduler, this);
            ccHandler.open();
        }
    }

    @Override
    public void dispose() {
        assert ccHandler != null : "@AssumeAssertion(nullness)";
        ccHandler.close();
        ccHandler = null;
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        assert ccHandler != null : "@AssumeAssertion(nullness)";
        ccHandler.handleCommand(channelUID, command);
    }

    protected @NonNull InitializedChromeCastHandler getCcHandler() {
        if (ccHandler != null) {
            return ccHandler;
        } else {
            throw new IllegalStateException("This function must not be called if the handler is not initialized.");
        }
    }

    @Override
    public void updateStatus(@NonNull final ThingStatus status) {
        super.updateStatus(status);
    }

    @Override
    public void updateStatus(@NonNull final ThingStatus status, @NonNull final ThingStatusDetail statusDetail) {
        super.updateStatus(status, statusDetail);
    }

    @Override
    public void updateStatus(@NonNull final ThingStatus status, @NonNull final ThingStatusDetail statusDetail,
            @NonNull final String description) {
        super.updateStatus(status, statusDetail, description);
    }

    @Override
    public void updateState(@NonNull final ChannelUID channelUid, @NonNull final State state) {
        super.updateState(channelUid, state);
    }

    @Override
    public void updateState(@NonNull final String channelId, @NonNull final State state) {
        super.updateState(channelId, state);
    }

}
