
package de.maggu2810.shk.addon.proxyservlet.internal;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

public class ServletConfigImpl implements ServletConfig {

    private final String servletName;
    private final ServletContext servletContext;
    private final Map<String, String> config = new HashMap<>();

    public ServletConfigImpl(final ServletConfig config) {
        this.servletName = config.getServletName();
        this.servletContext = config.getServletContext();
        final Enumeration<String> enumeration = config.getInitParameterNames();
        while (enumeration.hasMoreElements()) {
            final String key = enumeration.nextElement();
            final String value = getInitParameter(key);
            this.config.put(key, value);
        }
    }

    @Override
    public String getServletName() {
        return servletName;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public String getInitParameter(final String name) {
        return config.get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(config.keySet());
    }

    public void put(final String key, final String value) {
        config.put(key, value);
    }

}
