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

import java.util.Collection;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiConsumer;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.model.sitemap.Default;
import org.eclipse.smarthome.model.sitemap.Frame;
import org.eclipse.smarthome.model.sitemap.Group;
import org.eclipse.smarthome.model.sitemap.LinkableWidget;
import org.eclipse.smarthome.model.sitemap.Sitemap;
import org.eclipse.smarthome.model.sitemap.SitemapFactory;
import org.eclipse.smarthome.model.sitemap.Widget;

public class SitemapGeneratorLocations implements SitemapGenerator {

    private static final String LOCATION_DFL = "others";

    private static class ThingData {
        public final Frame frame;
        public final SortedMap<String, Default> items = new TreeMap<>();

        public ThingData(final Frame frame) {
            this.frame = frame;
        }
    }

    private static class LocationData {
        public final Group group;
        public final SortedMap<String, ThingData> things = new TreeMap<>();

        public LocationData(final Group group) {
            this.group = group;
        }
    }

    private static String createId(final @Nullable String id, final Object obj) {
        final int ihc = System.identityHashCode(obj);
        if (id != null) {
            return id + "@" + ihc;
        } else {
            return "@" + ihc;
        }
    }

    @Override
    public Sitemap generate(final SitemapProviderImpl generator, final SitemapFactory sitemapFactory,
            final Sitemap sitemap) {
        final SortedMap<String, LocationData> locations = new TreeMap<>();

        final Frame mainFrame = sitemapFactory.createFrame();

        generator.thingRegistry.getAll().forEach(thing -> {
            // Create or use already existing location data (widget, ...)
            final String location = getLocation(thing);
            final LocationData locationData = locations.computeIfAbsent(location, loc -> {
                final Group locationWidget = sitemapFactory.createGroup();
                locationWidget.setLabel(loc);
                return new LocationData(locationWidget);
            });

            // Create thing data (widget, ...) for the thing
            final ThingData thingData = new ThingData(sitemapFactory.createFrame());
            final String thingLabel = thing.getLabel();
            if (thingLabel != null) {
                thingData.frame.setLabel(thingLabel);
            }
            thingData.frame.setIcon("player");

            // For every channel of the thing ...
            thing.getChannels().forEach(channel -> {
                // ... inspect every linked item, ...
                generator.linkRegistry.getLinkedItems(channel.getUID()).forEach(item -> {
                    // create a widget for the item
                    final Default widget = sitemapFactory.createDefault();
                    widget.setItem(item.getName());

                    thingData.items.put(createId(item.getName(), item), widget);
                });
            });

            // Add thing data to location data
            locationData.things.put(createId(thingLabel, thing), thingData);
        });

        addRecursive(sitemap.getChildren(), mainFrame, this::addLocations, locations);
        return sitemap;
    }

    private <T> void addRecursive(final Collection<Widget> parent, final LinkableWidget current,
            final BiConsumer<Collection<Widget>, T> childrenFiller, final T childrenFillerArg) {
        final Collection<Widget> children = current.getChildren();

        // Fill my children
        childrenFiller.accept(children, childrenFillerArg);

        // Add myself to parent only if there are children.
        if (!children.isEmpty()) {
            parent.add(current);
        }
    }

    private void addLocations(final Collection<Widget> parent, final SortedMap<String, LocationData> locations) {
        for (final Entry<String, LocationData> locationEntry : locations.entrySet()) {
            final LocationData locationData = locationEntry.getValue();
            addRecursive(parent, locationData.group, this::addThings, locationData.things);
        }
    }

    private void addThings(final Collection<Widget> parent, final SortedMap<String, ThingData> things) {
        for (final Entry<String, ThingData> thingEntry : things.entrySet()) {
            final ThingData thingData = thingEntry.getValue();
            addRecursive(parent, thingData.frame, this::addItems, thingData.items);
        }
    }

    private void addItems(final Collection<Widget> parent, final SortedMap<String, Default> items) {
        for (final Entry<String, Default> itemEntry : items.entrySet()) {
            final Default itemWidget = itemEntry.getValue();
            parent.add(itemWidget);
        }
    }

    private static String getLocation(final Thing thing) {
        final String thingLocation = thing.getLocation();
        if (thingLocation == null || thingLocation.isEmpty()) {
            return LOCATION_DFL;
        } else {
            return thingLocation;
        }
    }

}
