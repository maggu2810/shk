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

import de.maggu2810.shk.systemstate.api.SystemStateInjector;
import de.maggu2810.shk.systemstate.demo.impl.fakesrv.RuleRegistry;

@Component(immediate = true)
public class RulesReadyProvider {

    private final Logger logger = LoggerFactory.getLogger(RulesReadyProvider.class);

    @Reference
    private SystemStateInjector ssi;

    @Reference
    private RuleRegistry ruleRegistry;

    @Activate
    protected void activate() {
        logger.info("mark rules ready");
        ssi.setState("rules", "ready");
    }

    @Deactivate
    protected void deactivate() {
        logger.info("mark rules non-ready");
        ssi.setState("rules", null);
    }
}
