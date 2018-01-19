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

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import de.maggu2810.shk.propertymanager.utils.PropertyManagerImpl;

@Component(immediate = true)
public class SystemStateManager {

    private final PropertyManagerImpl manager;

    public SystemStateManager() {
        manager = new PropertyManagerImpl("systemstate");
    }

    @Activate
    protected void activate(final BundleContext bc) {
        manager.open(bc);
    }

    @Deactivate
    protected void deactivate() {
        manager.close();
    }

}
