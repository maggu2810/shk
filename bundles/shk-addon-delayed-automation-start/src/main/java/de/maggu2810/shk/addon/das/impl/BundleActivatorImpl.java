package de.maggu2810.shk.addon.das.impl;

import de.maggu2810.shk.addon.das.impl.automationcore.DelayedAutomationStart;
import de.maggu2810.shk.addon.das.impl.demo.TestBundleListener;
import de.maggu2810.shk.addon.das.impl.demo.TestBundleTracker;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class BundleActivatorImpl implements BundleActivator {

  // BEG - Demo
  private TestBundleListener bundleListener;
  private TestBundleTracker bundleTracker;
  // END - Demo

  private DelayedAutomationStart das;

  @Override
  public void start(BundleContext context) throws Exception {
    // BEG - Demo
    bundleListener = new TestBundleListener();
    context.addBundleListener(bundleListener);
    bundleTracker = new TestBundleTracker(context);
    bundleTracker.open();
    // END - Demo

    das = new DelayedAutomationStart(context);
    das.open();
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    das.close();

    // BEG - Demo
    bundleTracker.close();
    bundleTracker = null;
    context.removeBundleListener(bundleListener);
    bundleListener = null;
    // END - Demo
  }

}
