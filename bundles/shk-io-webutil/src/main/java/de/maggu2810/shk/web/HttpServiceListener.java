/*-
 * #%L
 * shk - Bundles - IO - Web Util
 * %%
 * Copyright (C) 2015 - 2018 maggu2810
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */

package de.maggu2810.shk.web;

import org.osgi.service.http.HttpService;

public interface HttpServiceListener {

    /**
     * Inform that a new http service has been added.
     *
     * @param httpService the added http service
     * @param httpServiceInfo the information about the added http service
     */
    void addHttpService(HttpService httpService, HttpServiceInfo httpServiceInfo);

    /**
     * Inform that a http service has been removed.
     *
     * @param httpService the service that has been removed
     */
    void removeHttpService(HttpService httpService);
}
