/*-
 * #%L
 * shk :: Bundles :: Addon :: Auto Update Configurator
 * %%
 * Copyright (C) 2015 - 2017 maggu2810
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */

package de.maggu2810.shk.addon.autoupdate.configurator.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.smarthome.core.autoupdate.AutoUpdateBindingConfigProvider;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import de.maggu2810.shk.addon.autoupdate.configurator.AutoUpdateConfigurator;

@Component
public class AutoUpdateConfiguratorImpl implements AutoUpdateBindingConfigProvider, AutoUpdateConfigurator {

    private static final Map<ChannelUID, Boolean> BINDING_GLOBAL_TRUE = new HashMap<>();
    private static final Map<ChannelUID, Boolean> BINDING_GLOBAL_FALSE = new HashMap<>();

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    protected @Nullable ItemChannelLinkRegistry itemChannelLinkRegistry;

    // Binding specific settings
    protected Map<String, Map<ChannelUID, Boolean>> bindingSpecificSettings = new ConcurrentHashMap<>();

    @Override
    public void addAutoUpdateByBindingIdConfig(final String bindingId, final boolean autoUpdate)
            throws IllegalArgumentException, IllegalStateException {
        final Map<ChannelUID, Boolean> settings = bindingSpecificSettings.get(bindingId);
        if (settings == null) {
            bindingSpecificSettings.put(bindingId, autoUpdate ? BINDING_GLOBAL_TRUE : BINDING_GLOBAL_FALSE);
        } else {
            if (settings == BINDING_GLOBAL_TRUE || settings == BINDING_GLOBAL_FALSE) {
                throw new IllegalArgumentException(
                        String.format("There is already an configuration for given binding ID '%s'.", bindingId));
            } else {
                throw new IllegalStateException(String.format(
                        "There is already a channel specific configuration for the given binding ID '%s'.", bindingId));
            }
        }
    }

    @Override
    public void removeAutoUpdateByBindingIdConfig(final String bindingId) throws IllegalArgumentException {
        final Map<ChannelUID, Boolean> settings = bindingSpecificSettings.get(bindingId);
        if (settings == null || settings != BINDING_GLOBAL_TRUE && settings != BINDING_GLOBAL_FALSE) {
            throw new IllegalArgumentException(
                    String.format("There is no configuration for given binding ID '%s'.", bindingId));
        } else {
            bindingSpecificSettings.remove(bindingId);
        }
    }

    @Override
    public void addAutoUpdateByChannelUidConfig(final ChannelUID channelUid, final boolean autoUpdate)
            throws IllegalArgumentException, IllegalStateException {
        final String bindingId = channelUid.getBindingId();
        final Map<ChannelUID, Boolean> settings = bindingSpecificSettings.get(bindingId);
        if (settings == null) {
            final Map<ChannelUID, Boolean> newSettings = new ConcurrentHashMap<>();
            newSettings.put(channelUid, autoUpdate);
            bindingSpecificSettings.put(bindingId, newSettings);
        } else {
            if (settings == BINDING_GLOBAL_TRUE || settings == BINDING_GLOBAL_FALSE) {
                throw new IllegalArgumentException(
                        String.format("There is already an configuration for given binding ID '%s'.", bindingId));
            } else {
                if (settings.containsKey(channelUid)) {
                    throw new IllegalStateException(String.format(
                            "There is already a channel specific configuration for the given binding ID '%s'.",
                            bindingId));
                } else {
                    settings.put(channelUid, autoUpdate);
                }
            }
        }
    }

    @Override
    public void removeAutoUpdateByChannelUidConfig(final ChannelUID channelUid) throws IllegalArgumentException {
        final Map<ChannelUID, Boolean> settings = bindingSpecificSettings.get(channelUid.getBindingId());
        if (settings == null || !settings.containsKey(channelUid)) {
            throw new IllegalArgumentException(
                    String.format("There is no configuration for given channel UID '%s'.", channelUid));
        } else {
            settings.remove(channelUid);
        }
    }

    @Override
    public @Nullable Boolean autoUpdate(final String itemName) {
        Boolean autoUpdate = null;

        // Check auto update by configuration for binding ID
        final ItemChannelLinkRegistry itemChannelLinkRegistry = this.itemChannelLinkRegistry;
        if (itemChannelLinkRegistry != null) {
            final Set<ChannelUID> channelUIDs = itemChannelLinkRegistry.getBoundChannels(itemName);

            for (final ChannelUID channelUID : channelUIDs) {
                final Boolean au;

                final Map<ChannelUID, Boolean> settings = bindingSpecificSettings.get(channelUID.getBindingId());
                if (settings == null) {
                    au = null;
                } else {
                    if (settings == BINDING_GLOBAL_TRUE) {
                        au = true;
                    } else if (settings == BINDING_GLOBAL_FALSE) {
                        au = false;
                    } else {
                        final Boolean channelSettings = settings.get(channelUID);
                        if (channelSettings == null) {
                            au = null;
                        } else if (channelSettings) {
                            au = true;
                        } else {
                            au = false;
                        }
                    }
                }

                // Now 'au' is set and we need to handle the different options: unset, set to true, set to false
                if (au == null) {
                    // There is no setting for the binding ID, keep value unchanged.
                    continue;
                } else {
                    // There is an entry for the binding ID.
                    if (au) {
                        // setting is true, we could stop.
                        return true;
                    } else {
                        // setting is false, set it if autoupdate is not set
                        if (autoUpdate == null) {
                            autoUpdate = false;
                        }
                    }
                }
            }
        }

        return autoUpdate;
    }

}
