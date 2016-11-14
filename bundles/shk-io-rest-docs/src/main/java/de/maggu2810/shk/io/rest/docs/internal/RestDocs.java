/*-
 * #%L
 * shk :: Bundles :: IO :: REST Docs
 * %%
 * Copyright (C) 2015 - 2016 maggu2810
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package de.maggu2810.shk.io.rest.docs.internal;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
public class RestDocs {

    private static final String ALIAS = "/doc";

    private final Logger logger = LoggerFactory.getLogger(RestDocs.class);

    @Reference
    @SuppressWarnings("initialization.fields.uninitialized")
    private @NonNull HttpService httpService;

    @Activate
    protected void activate() {
        try {
            httpService.registerResources(ALIAS, "swagger", httpService.createDefaultHttpContext());
        } catch (final NamespaceException ex) {
            logger.error("Could not start up REST documentation service.", ex);
        }
    }

    @Deactivate
    protected void deactivate() {
        httpService.unregister(ALIAS);
    }

}
