
package de.maggu2810.shk.propertymanager.utils;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import de.maggu2810.shk.propertymanager.api.PropertyInjector;
import de.maggu2810.shk.propertymanager.api.PropertyManager;
import de.maggu2810.shk.propertymanager.api.PropertyProvider;

public class PropertyManagerImpl implements PropertyManager, PropertyInjector, PropertyProvider {

    private static final String KEY_ID = "id";
    private static final String KEY_UUID = "uuid";

    private final Map<String, Object> props = new ConcurrentHashMap<>();
    private final String id;
    private final UUID uuid;

    private final AtomicReference<ServiceRegistration<PropertyInjector>> regInjector = new AtomicReference<>();
    private final AtomicReference<ServiceRegistration<PropertyProvider>> regProvider = new AtomicReference<>();

    public PropertyManagerImpl(final String id) {
        this.id = id;
        this.uuid = UUID.randomUUID();
        props.put(KEY_ID, this.id);
        props.put(KEY_UUID, this.uuid);
    }

    @Override
    public void open(final BundleContext bc, final boolean injector, final boolean provider) {
        final Dictionary<String, Object> props = new Hashtable<>(this.props);
        if (injector) {
            regInjector.set(bc.registerService(PropertyInjector.class, this, props));
        }
        if (provider) {
            regProvider.set(bc.registerService(PropertyProvider.class, this, props));
        }
    }

    @Override
    public void close() {
        Optional.ofNullable(regProvider.getAndSet(null)).ifPresent(reg -> reg.unregister());
        Optional.ofNullable(regInjector.getAndSet(null)).ifPresent(reg -> reg.unregister());
    }

    @Override
    public Object getProperty(final String key) {
        return props.get(key);
    }

    @Override
    public void setProperty(final String key, final @Nullable Object value) {
        if (KEY_ID.equals(key) || KEY_UUID.equals(key)) {
            throw new IllegalArgumentException(
                    String.format("The key '%s' is a static one and must not be used.", key));
        }

        if (value == null) {
            props.remove(key);
        } else {
            props.put(key, value);
        }

        final Dictionary<String, Object> props = new Hashtable<>(this.props);
        Optional.ofNullable(regProvider.get()).ifPresent(reg -> reg.setProperties(props));
        Optional.ofNullable(regInjector.get()).ifPresent(reg -> reg.setProperties(props));
    }

}
