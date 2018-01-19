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

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.maggu2810.shk.propertymanager.api.PropertyProvider;

@Component(immediate = true)
public class NeedRules {

    private final Logger logger = LoggerFactory.getLogger(NeedRules.class);

    @Reference(target = "(&(id=systemstate)(rules=ready))")
    protected PropertyProvider ssp;

    protected void activate() {
        logger.info("rules are ready");
    }

    protected void deactivate() {
        logger.info("rules not ready");
    }
}
