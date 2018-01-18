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

import java.util.Arrays;
import java.util.stream.Collectors;

import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.maggu2810.shk.systemstate.api.SystemStateProvider;

@Component(immediate = true)
public class SystemStateMonitor {

    private final Logger logger = LoggerFactory.getLogger(SystemStateMonitor.class);

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC,
            policyOption = ReferencePolicyOption.GREEDY)
    protected void addSystemStateProvider(final ServiceReference<SystemStateProvider> srv) {
        logger.info("add state provider '{}'; props: {}", srv, propsToString(srv));
    }

    protected void updatedSystemStateProvider(final ServiceReference<SystemStateProvider> srv) {
        logger.info("updated state provider '{}'; props: {}", srv, propsToString(srv));
    }

    protected void removeSystemStateProvider(final ServiceReference<SystemStateProvider> srv) {
        logger.info("remove state provider '{}'; props: {}", srv, propsToString(srv));
    }

    final String propsToString(final ServiceReference<SystemStateProvider> srv) {
        return String.join(";", Arrays.stream(srv.getPropertyKeys())
                .map(key -> String.format("%s=%s", key, srv.getProperty(key))).collect(Collectors.toList()));
    }
}
