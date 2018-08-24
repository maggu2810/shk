package de.maggu2810.shk.addon.das.impl.demo;

import de.maggu2810.shk.addon.das.impl.BundleUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;

public class TestBundleTracker extends BundleTracker {

  public TestBundleTracker(BundleContext context) {
    super(context,
            Bundle.STARTING | Bundle.STOPPING | Bundle.RESOLVED | Bundle.INSTALLED | Bundle.UNINSTALLED,
            null);
  }

  @Override
  public Object addingBundle(Bundle bundle, BundleEvent event) {
    // Typically we would inspect bundle, to figure out if we want to
    // track it or not. If we don't want to track return null, otherwise
    // return an object.
    print(bundle, event);
    return bundle;
  }

  private void print(Bundle bundle, BundleEvent event) {
    System.out.println(
            String.format("[BundleTracker] BundleChanged: %s, state: %s, event.type: %s",
                    bundle.getSymbolicName(),
                    BundleUtils.bundleStateAsString(bundle),
                    BundleUtils.bundleEventTypeAsString(event)
            )
    );
  }

  @Override
  public void removedBundle(Bundle bundle, BundleEvent event, Object object) {
    print(bundle, event);
  }

  @Override
  public void modifiedBundle(Bundle bundle, BundleEvent event, Object object) {
    print(bundle, event);
  }

}
