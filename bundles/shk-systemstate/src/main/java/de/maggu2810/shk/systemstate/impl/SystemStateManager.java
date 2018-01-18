/*-
 * #%L
 * shk :: Bundles :: System State
 * %%
 * Copyright (C) 2015 - 2018 maggu2810
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */

package de.maggu2810.shk.systemstate.impl;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import de.maggu2810.shk.systemstate.api.SystemStateInjector;
import de.maggu2810.shk.systemstate.api.SystemStateProvider;

@Component(immediate = true, service = { SystemStateInjector.class })
public class SystemStateManager implements SystemStateInjector, SystemStateProvider {

    private final Map<String, Object> states = new ConcurrentHashMap<>();
    private volatile ServiceRegistration<SystemStateProvider> reg;

    @Activate
    protected void activate(final BundleContext bc) {
        reg = bc.registerService(SystemStateProvider.class, this, new Hashtable<>(states));
    }

    @Deactivate
    protected void deactivate() {
        reg.unregister();
        reg = null;
    }

    @Override
    public Object getState(final String key) {
        return states.get(key);
    }

    @Override
    public void setState(final String key, final @Nullable Object value) {
        if (value == null) {
            states.remove(key);
        } else {
            states.put(key, value);
        }
        reg.setProperties(new Hashtable<>(states));
    }

}
