/*-
 * #%L
 * shk :: Bundles :: Addon :: Sitemap Generator
 * %%
 * Copyright (C) 2015 - 2016 maggu2810
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
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
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

@Component
public class SitemapGenerator implements SitemapProvider {

    @FunctionalInterface
    private interface SimpleSitemapGenerator extends Function<Sitemap, Sitemap> {
    }

    private static final String LOCATION_DFL = "others";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, SimpleSitemapGenerator> generators = new HashMap<>();

    @Reference
    private ThingRegistry thingRegistry;

    @Reference
    private ItemChannelLinkRegistry linkRegistry;

    @Reference
    private ItemRegistry itemRegistry;

    private SitemapFactory sitemapFactory;

    /**
     * Create a new sitemap generator instance.
     */
    public SitemapGenerator() {
        generators.put("_locations", this::getSitemapLocation);
        generators.put("_items", this::getSitemapItems);
    }

    @Override
    public Sitemap getSitemap(final String sitemapName) {
        if (sitemapFactory == null && (sitemapFactory = SitemapFactory.eINSTANCE) == null) {
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

        return generators.get(sitemapName).apply(sitemap);
    }

    private Sitemap getSitemapLocation(final Sitemap sitemap) {
        final Map<String, Group> locations = new HashMap<>();

        final Frame mainFrame = sitemapFactory.createFrame();

        thingRegistry.getAll().forEach(thing -> {
            // Create a widget for the thing
                final Frame thingWidget = sitemapFactory.createFrame();
                thingWidget.setLabel(thing.getLabel());
                thingWidget.setIcon("player");

                // Create or use already existing location widget
                final String thingLocation = thing.getLocation();
                final String location = StringUtils.isEmpty(thingLocation) ? LOCATION_DFL : thingLocation;
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
                        linkRegistry.getLinkedItems(channel.getUID()).forEach(itemName -> {
                            final Item item = itemRegistry.get(itemName);
                            if (item == null) {
                                logger.warn("Linked item does not exist: {}", itemName);
                            }

                            // create a widget for the item
                                final Default widget = sitemapFactory.createDefault();
                                widget.setItem(itemName);

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

    private Sitemap getSitemapItems(final Sitemap sitemap) {
        final Frame mainFrame = sitemapFactory.createFrame();

        itemRegistry.getAll().forEach(item -> {
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

}