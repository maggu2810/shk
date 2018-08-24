package de.maggu2810.shk.addon.das.impl.demo;

import de.maggu2810.shk.addon.das.impl.BundleUtils;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

public class TestBundleListener implements BundleListener {

  @Override
  public void bundleChanged(BundleEvent event) {
    System.out.println(
            String.format("[BundleListener] BundleChanged: %s, state: %s, event.type: %s",
                    event.getBundle().getSymbolicName(),
                    BundleUtils.bundleStateAsString(event.getBundle()),
                    BundleUtils.bundleEventTypeAsString(event)
            )
    );
  }

}
