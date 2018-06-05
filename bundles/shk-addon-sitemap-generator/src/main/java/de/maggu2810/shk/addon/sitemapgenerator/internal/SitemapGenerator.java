/*-
 * #%L
 * shk :: Bundles :: Addon :: Sitemap Generator
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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.model.core.ModelRepositoryChangeListener;
import org.eclipse.smarthome.model.sitemap.Default;
import org.eclipse.smarthome.model.sitemap.Frame;
import org.eclipse.smarthome.model.sitemap.Group;
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
public class SitemapGenerator implements SitemapProvider {

    @FunctionalInterface
    private interface SimpleSitemapGenerator {
        /**
         * Fill / generate a sitemap.
         *
         * @param generator the generator to use
         * @param sitemapFactory the sitemap factory to use
         * @param sitemap a pre-set-upped sitemap to use
         * @return a set upped sitemap
         */
        @NonNull
        Sitemap generate(@NonNull SitemapGenerator generator, @NonNull SitemapFactory sitemapFactory,
                @NonNull Sitemap sitemap);
    }

    private static final String LOCATION_DFL = "others";

    private final Logger logger = LoggerFactory.getLogger(SitemapGenerator.class);

    private final Map<String, SimpleSitemapGenerator> generators = new HashMap<>();

    @Reference
    @SuppressWarnings("initialization.fields.uninitialized")
    private ThingRegistry thingRegistry;

    @Reference
    @SuppressWarnings("initialization.fields.uninitialized")
    private ItemChannelLinkRegistry linkRegistry;

    @Reference
    @SuppressWarnings("initialization.fields.uninitialized")
    private ItemRegistry itemRegistry;

    /**
     * Create a new sitemap generator instance.
     */
    public SitemapGenerator() {
        generators.put("_locations", SitemapGenerator::getSitemapLocation);
        generators.put("_items", SitemapGenerator::getSitemapItems);
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

    private static @NonNull Sitemap getSitemapLocation(final @NonNull SitemapGenerator generator,
            final @NonNull SitemapFactory sitemapFactory, final @NonNull Sitemap sitemap) {
        final Map<String, Group> locations = new HashMap<>();

        final Frame mainFrame = sitemapFactory.createFrame();

        generator.thingRegistry.getAll().forEach(thing -> {
            // Create a widget for the thing
            final Frame thingWidget = sitemapFactory.createFrame();
            final String thingLabel = thing.getLabel();
            if (thingLabel != null) {
                thingWidget.setLabel(thingLabel);
            }
            thingWidget.setIcon("player");

            // Create or use already existing location widget
            final String location = getLocation(thing);
            final Group locationWidget;
            if (locations.containsKey(location)) {
                // Location widget already exists
                locationWidget = locations.get(location);
            } else {
                // Create a new location widget
                locationWidget = sitemapFactory.createGroup();
                locationWidget.setLabel(location);
                locations.put(location, locationWidget);
            }

            // For every channel of the thing ...
            thing.getChannels().forEach(channel -> {
                // ... inspect every linked item, ...
                generator.linkRegistry.getLinkedItems(channel.getUID()).forEach(item -> {
                    // create a widget for the item
                    final Default widget = sitemapFactory.createDefault();
                    widget.setItem(item.getName());

                    // and add it to the thing widget
                    thingWidget.getChildren().add(widget);
                });
            });

            // Add thing widget (if not empty) to the location widget
            if (!thingWidget.getChildren().isEmpty()) {
                locationWidget.getChildren().add(thingWidget);
            }
        });

        // Add all non-empty location widgets to the main widget
        locations.forEach((name, widget) -> {
            if (!widget.getChildren().isEmpty()) {
                mainFrame.getChildren().add(widget);
            }
        });

        // Add main widget (if not empty) to the sitemap
        if (!mainFrame.getChildren().isEmpty()) {
            sitemap.getChildren().add(mainFrame);
        }

        return sitemap;
    }

    private static @NonNull Sitemap getSitemapItems(final @NonNull SitemapGenerator generator,
            final @NonNull SitemapFactory sitemapFactory, final @NonNull Sitemap sitemap) {
        final Frame mainFrame = sitemapFactory.createFrame();

        generator.itemRegistry.getAll().forEach(item -> {
            // create a widget for the item
            final Default widget = sitemapFactory.createDefault();
            widget.setItem(item.getName());
            mainFrame.getChildren().add(widget);
        });

        // Add main widget (if not empty) to the sitemap
        if (!mainFrame.getChildren().isEmpty()) {
            sitemap.getChildren().add(mainFrame);
        }

        return sitemap;
    }

    @Override
    public Set<String> getSitemapNames() {
        return Collections.unmodifiableSet(generators.keySet());
    }

    private static String getLocation(final Thing thing) {
        final String thingLocation = thing.getLocation();
        if (thingLocation == null || thingLocation.isEmpty()) {
            return LOCATION_DFL;
        } else {
            return thingLocation;
        }
    }

    public void addModelChangeListener(final ModelRepositoryChangeListener listener) {
    }

    public void removeModelChangeListener(final ModelRepositoryChangeListener listener) {
    }

}
