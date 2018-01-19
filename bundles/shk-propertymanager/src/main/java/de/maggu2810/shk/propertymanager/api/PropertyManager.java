
package de.maggu2810.shk.propertymanager.api;

import java.io.Closeable;

import org.osgi.framework.BundleContext;

public interface PropertyManager extends Closeable {

    default void open(final BundleContext bc) {
        open(bc, true, true);
    }

    void open(final BundleContext bc, final boolean injector, final boolean provider);

    @Override
    void close();
}
