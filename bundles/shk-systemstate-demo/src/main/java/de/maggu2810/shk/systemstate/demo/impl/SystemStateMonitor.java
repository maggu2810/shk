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

import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.maggu2810.shk.propertymanager.api.PropertyProvider;

@Component(immediate = true)
public class SystemStateMonitor {

    private final Logger logger = LoggerFactory.getLogger(SystemStateMonitor.class);

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    protected void addSystemStateProvider(final ServiceReference<PropertyProvider> srvRef) {
        log("add", srvRef);
    }

    protected void updatedSystemStateProvider(final ServiceReference<PropertyProvider> srvRef) {
        log("updated", srvRef);
    }

    protected void removeSystemStateProvider(final ServiceReference<PropertyProvider> srvRef) {
        log("remove", srvRef);
    }

    private void log(final String action, final ServiceReference<PropertyProvider> srvRef) {
        // It is still possible that we has not been activated yet, so let's use FrameworkUtil to get the bundle
        // context.
        final PropertyProvider srv = FrameworkUtil.getBundle(getClass()).getBundleContext().getService(srvRef);
        logger.info("{} property provider '{}'\n{}", action, srv, propsToString(srvRef));

    }

    private static <T> String propsToString(final ServiceReference<T> srvRef) {
        return propsToString(srvRef, "    ", "\n");
    }

    private static <T> String propsToString(final ServiceReference<T> srvRef, final String prefix, final String sep) {
        return String.join(sep,
                Arrays.stream(srvRef.getPropertyKeys())
                        .map(key -> String.format("%s%s=%s", prefix, key, srvRef.getProperty(key)))
                        .collect(Collectors.toList()));
    }
}
