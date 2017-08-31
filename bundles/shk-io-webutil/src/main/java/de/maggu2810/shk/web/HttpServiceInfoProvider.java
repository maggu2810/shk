/*-
 * #%L
 * shk :: Bundles :: IO :: Web Util
 * %%
 * Copyright (C) 2015 - 2017 maggu2810
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */

package de.maggu2810.shk.web;

import java.util.Collection;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.osgi.service.http.HttpService;

public interface HttpServiceInfoProvider {

    /**
     * Add a listener to keep informed about new http services.
     *
     * @param listener the listener to add
     */
    void addHttpServiceInfoListener(@NonNull HttpServiceListener listener);

    /**
     * Remove a listener.
     *
     * @param listener the listener to be removed
     */
    void removeHttpServiceInfoListener(@NonNull HttpServiceListener listener);

    /**
     * Get currently known http services.
     *
     * @return the currently known http services
     */
    @NonNull
    Collection<@NonNull HttpService> getHttpServices();

    /**
     * Get the service info for a http service.
     *
     * @param service the service the info is requested for
     * @return the service info for given service
     */
    @NonNull
    HttpServiceInfo getHttpServiceInfo(@NonNull HttpService service);

}
