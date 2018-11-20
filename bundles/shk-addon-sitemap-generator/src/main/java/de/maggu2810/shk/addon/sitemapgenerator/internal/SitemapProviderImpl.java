/*-
 * #%L
 * shk - Bundles - Addon - Sitemap Generator
 * %%
 * Copyright (C) 2015 - 2018 maggu2810
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */

package de.maggu2810.shk.addon.sitemapgenerator.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.model.core.ModelRepositoryChangeListener;
import org.eclipse.smarthome.model.sitemap.Sitemap;
import org.eclipse.smarthome.model.sitemap.SitemapFactory;
import org.eclipse.smarthome.model.sitemap.SitemapProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate some sitemaps on-the-fly.
 *
 * @author Markus Rathgeb - Initial contribution
 */
@Component
public class SitemapProviderImpl implements SitemapProvider {

    private final Logger logger = LoggerFactory.getLogger(SitemapProviderImpl.class);

    private final Map<String, SitemapGenerator> generators = new HashMap<>();

    @Reference
    @SuppressWarnings("initialization.fields.uninitialized")
    ThingRegistry thingRegistry;

    @Reference
    @SuppressWarnings("initialization.fields.uninitialized")
    ItemChannelLinkRegistry linkRegistry;

    @Reference
    @SuppressWarnings("initialization.fields.uninitialized")
    ItemRegistry itemRegistry;

    /**
     * Create a new sitemap generator instance.
     */
    public SitemapProviderImpl() {
        generators.put("_locations", new SitemapGeneratorLocations());
        generators.put("_items", new SitemapGeneratorItems());
    }

    @Override
    public @Nullable Sitemap getSitemap(final String sitemapName) {
        final @Nullable SitemapFactory sitemapFactory = SitemapFactory.eINSTANCE;
        if (sitemapFactory == null) {
            logger.error("Sitemap Factory not available");
            return null;
        }
        if (!generators.containsKey(sitemapName)) {
            logger.warn("I am asked for a sitemap ({}) I do not own.", sitemapName);
            return null;
        }

        final Sitemap sitemap = sitemapFactory.createSitemap();
        sitemap.setLabel("Home");
        sitemap.setName(sitemapName);

        return generators.get(sitemapName).generate(this, sitemapFactory, sitemap);
    }

    @Override
    public Set<String> getSitemapNames() {
        return Collections.unmodifiableSet(generators.keySet());
    }

    @Override
    public void addModelChangeListener(final ModelRepositoryChangeListener listener) {
    }

    @Override
    public void removeModelChangeListener(final ModelRepositoryChangeListener listener) {
    }

}
