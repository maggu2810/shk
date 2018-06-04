/*-
 * #%L
 * shk :: Bundles :: Addon :: Auto Update Configurator
 * %%
 * Copyright (C) 2015 - 2018 maggu2810
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */

package de.maggu2810.shk.addon.autoupdate.configurator;

import org.eclipse.smarthome.core.thing.ChannelUID;

/**
 * This class defines an interface to configure an auto-update service programmatically.
 *
 * @author Markus Rathgeb - Initial contribution
 */
public interface AutoUpdateConfigurator {

    /**
     * Add an auto-update configuration for a binding ID.
     *
     * @param bindingId the ID of the binding
     * @param autoUpdate the auto-update setting
     * @throws IllegalArgumentException if there is already a configuration for the given binding ID
     * @throws IllegalStateException if there is already a configuration by channel UID of that binding ID
     */
    void addAutoUpdateByBindingIdConfig(String bindingId, boolean autoUpdate)
            throws IllegalArgumentException, IllegalStateException;

    /**
     * Remove an auto-update configuration for a binding ID.
     *
     * @param bindingId the ID of the binding
     * @throws IllegalArgumentException if there is no configuration for the given binding ID
     */
    void removeAutoUpdateByBindingIdConfig(String bindingId) throws IllegalArgumentException;

    /**
     * Add an auto-update configuration for a channel UID.
     *
     * @param channelUid the channel UID
     * @param autoUpdate the auto-update setting
     * @throws IllegalArgumentException if there is already a configuration for the given channel UID
     * @throws IllegalStateException if there is already a configuration for the binding ID of that channel
     */
    void addAutoUpdateByChannelUidConfig(ChannelUID channelUid, boolean autoUpdate)
            throws IllegalArgumentException, IllegalStateException;

    /**
     * Remove an auto-update configuration for a channel UID.
     *
     * @param channelUid the channel UID
     * @throws IllegalArgumentException if there is no configuration for the given channel UID
     */
    void removeAutoUpdateByChannelUidConfig(ChannelUID channelUid) throws IllegalArgumentException;

}
