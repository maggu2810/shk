package de.maggu2810.shk.addon.das.impl.automationcore;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundleListenerImpl implements BundleListener {

  private final static String BSN_AUTOMATION_CORE = "org.eclipse.smarthome.automation.core";

  private final Logger logger = LoggerFactory.getLogger(BundleListenerImpl.class);
  private final BundleContext bc;
  private boolean ready = false;

  public BundleListenerImpl(final BundleContext bc) {
    this.bc = bc;
  }

  @Override
  public synchronized void bundleChanged(BundleEvent event) {
    final Bundle bundle = event.getBundle();
    if (isAutomationCore(bundle)) {
      if ((event.getType() & (BundleEvent.STARTED | BundleEvent.STARTING)) != 0 && !ready) {
        stopAutomationCore();
      }
    }
  }

  public synchronized void setReady(final boolean ready) {
    logger.info("set ready: {}", ready);
    this.ready = ready;
    if (ready) {
      startAutomationCore();
    } else {
      stopAutomationCore();
    }
  }

  private boolean isAutomationCore(final Bundle bundle) {
    return bundle.getSymbolicName().equals(BSN_AUTOMATION_CORE);
  }

  private void startAutomationCore() {
    for (final Bundle bundle : bc.getBundles()) {
      if (isAutomationCore(bundle)) {
        try {
          bundle.start();
        } catch (BundleException ex) {
          logger.debug("Something went wrong on stopping automation core bundle.", ex);
        }
      }
    }
  }

  private void stopAutomationCore() {
    for (final Bundle bundle : bc.getBundles()) {
      if (isAutomationCore(bundle)) {
        try {
          bundle.stop();
        } catch (BundleException ex) {
          logger.debug("Something went wrong on stopping automation core bundle.", ex);
        }
      }
    }
  }

}
