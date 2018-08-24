package de.maggu2810.shk.addon.das.impl.automationcore;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DelayedAutomationStart implements AutoCloseable {

  private static class ReadyForAutomationServiceTracker extends ServiceTracker<ReadyForAutomation, ReadyForAutomation> {

    private final Logger logger = LoggerFactory.getLogger(DelayedAutomationStart.class);
    private final AtomicInteger cnt = new AtomicInteger();
    private final Consumer<Boolean> setReady;

    public ReadyForAutomationServiceTracker(final BundleContext bc, Consumer<Boolean> setReady) {
      super(bc, ReadyForAutomation.class, null);
      this.setReady = setReady;
    }

    @Override
    public ReadyForAutomation addingService(ServiceReference<ReadyForAutomation> reference) {
      final ReadyForAutomation result = super.addingService(reference);
      final int cur = cnt.incrementAndGet();
      logger.info("added ready for automation: cnt={}", cnt);
      if (cur == 1) {
        setReady.accept(true);
      }
      return result;
    }

    @Override
    public void removedService(ServiceReference<ReadyForAutomation> reference,
            ReadyForAutomation service) {
      super.removedService(reference, service);
      final int cur = cnt.decrementAndGet();
      logger.info("removed ready for automation: cnt={}", cnt);
      if (cur == 0) {
        setReady.accept(false);
      }
    }

  }

  private final BundleContext bc;
  private final BundleListenerImpl bundleListener;
  private final ReadyForAutomationServiceTracker readyForAutomationServiceTracker;

  public DelayedAutomationStart(final BundleContext bc) {
    this.bc = bc;
    bundleListener = new BundleListenerImpl(bc);
    readyForAutomationServiceTracker = new ReadyForAutomationServiceTracker(bc, bundleListener::setReady);
  }

  public void open() {
    bc.addBundleListener(bundleListener);
    bundleListener.setReady(false);
    readyForAutomationServiceTracker.open();
  }

  @Override
  public void close() {
    readyForAutomationServiceTracker.close();
  }

}
