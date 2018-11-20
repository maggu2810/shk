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

import org.eclipse.smarthome.model.sitemap.Default;
import org.eclipse.smarthome.model.sitemap.Frame;
import org.eclipse.smarthome.model.sitemap.Sitemap;
import org.eclipse.smarthome.model.sitemap.SitemapFactory;

public class SitemapGeneratorItems implements SitemapGenerator {

    @Override
    public Sitemap generate(final SitemapProviderImpl generator, final SitemapFactory sitemapFactory,
            final Sitemap sitemap) {
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

}
