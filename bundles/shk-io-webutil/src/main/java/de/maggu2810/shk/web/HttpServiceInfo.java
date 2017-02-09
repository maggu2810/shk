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

public class HttpServiceInfo {
    private final int httpPort;
    private final int httpPortSecure;

    /**
     * Create a new service information.
     *
     * @param httpPort the HTTP port, -1 if unknown
     * @param httpPortSecure the HTTPS port, -1 if unknown
     */
    public HttpServiceInfo(final int httpPort, final int httpPortSecure) {
        this.httpPort = httpPort;
        this.httpPortSecure = httpPortSecure;
    }

    /**
     * Get the HTTP port.
     *
     * @return HTTP port
     */
    public int getHttpPort() {
        return httpPort;
    }

    /**
     * Get the HTTPS port.
     *
     * @return HTTPS port
     */
    public int getHttpPortSecure() {
        return httpPortSecure;
    }

    @Override
    public String toString() {
        return "HttpServiceInfo{" + "httpPort=" + httpPort + ", httpPortSecure=" + httpPortSecure + '}';
    }

}
