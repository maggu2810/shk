package de.maggu2810.shk.addon.das.impl.automationcore;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.osgi.framework.BundleContext;

public class DelayedAutomationStart implements AutoCloseable {

  private final BundleContext bc;
  private final BundleListenerImpl bundleListener;
  private ScheduledFuture<?> future;

  public DelayedAutomationStart(final BundleContext bc) {
    this.bc = bc;
    bundleListener = new BundleListenerImpl(bc);
  }

  public void open() {
    bc.addBundleListener(bundleListener);
    bundleListener.setReady(false);
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    future = scheduler.schedule(() -> bundleListener.setReady(true), 15, TimeUnit.SECONDS);
    scheduler.shutdown();
  }

  @Override
  public void close() {
    if (future != null) {
      future.cancel(false);
      future = null;
    }
  }

}
