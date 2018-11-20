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

import org.eclipse.smarthome.model.sitemap.Sitemap;
import org.eclipse.smarthome.model.sitemap.SitemapFactory;

@FunctionalInterface
public interface SitemapGenerator {

    /**
     * Fill / generate a sitemap.
     *
     * @param generator the generator to use
     * @param sitemapFactory the sitemap factory to use
     * @param sitemap a pre-set-upped sitemap to use
     * @return a set upped sitemap
     */
    Sitemap generate(SitemapProviderImpl generator, SitemapFactory sitemapFactory, Sitemap sitemap);

}
