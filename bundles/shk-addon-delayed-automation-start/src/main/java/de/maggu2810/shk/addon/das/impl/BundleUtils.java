package de.maggu2810.shk.addon.das.impl;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;

public class BundleUtils {

  public static String bundleStateAsString(final @Nullable Bundle bundle) {
    if (bundle == null) {
      return "null";
    }
    int state = bundle.getState();
    switch (state) {
      case Bundle.UNINSTALLED:
        return "UNINSTALLED";
      case Bundle.INSTALLED:
        return "INSTALLED";
      case Bundle.RESOLVED:
        return "RESOLVED";
      case Bundle.STARTING:
        return "STARTING";
      case Bundle.STOPPING:
        return "STOPPING";
      case Bundle.ACTIVE:
        return "ACTIVE";
      default:
        return "unknown bundle state: " + state;
    }
  }

  public static String bundleEventTypeAsString(final @Nullable BundleEvent event) {
    if (event == null) {
      return "null";
    }
    int type = event.getType();
    switch (type) {
      case BundleEvent.INSTALLED:
        return "INSTALLED";
      case BundleEvent.STARTED:
        return "STARTED";
      case BundleEvent.STOPPED:
        return "STOPPED";
      case BundleEvent.UPDATED:
        return "UPDATED";
      case BundleEvent.UNINSTALLED:
        return "UNINSTALLED";
      case BundleEvent.RESOLVED:
        return "RESOLVED";
      case BundleEvent.UNRESOLVED:
        return "UNRESOLVED";
      case BundleEvent.STARTING:
        return "Starting";
      case BundleEvent.STOPPING:
        return "STOPPING";
      case BundleEvent.LAZY_ACTIVATION:
        return "LAZY_ACTIVATION";
      default:
        return "unknown event type: " + type;
    }
  }

  private BundleUtils() {
  }
}
