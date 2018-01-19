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

package de.maggu2810.shk.systemstate.demo.impl;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.maggu2810.shk.propertymanager.api.PropertyInjector;
import de.maggu2810.shk.systemstate.demo.impl.fakesrv.ThingHandler;
import de.maggu2810.shk.systemstate.demo.impl.fakesrv.ThingRegistry;

@Component(immediate = true)
public class ThingsReadyProvider {

    private final Logger logger = LoggerFactory.getLogger(ThingsReadyProvider.class);

    @Reference(target = "(id=systemstate)")
    private PropertyInjector ssi;

    @Reference
    private ThingHandler thingHandler;

    @Reference
    private ThingRegistry thingRegistry;

    @Activate
    protected void activate() {
        logger.info("mark things ready");
        ssi.setProperty("things", "ready");
    }

    @Deactivate
    protected void deactivate() {
        logger.info("mark thing non-ready");
        ssi.setProperty("things", "not-ready");
    }
}
