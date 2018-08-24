package de.maggu2810.shk.addon.das.impl.automationcore;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true)
public class CheckAutomationRequirements {

  @Reference
  ThingRegistry thingRegistry;

  @Reference
  ItemRegistry itemRegistry;

  private BundleContext bc;
  private ScheduledFuture<?> future;

  private final Object sync = new Object();
  private ServiceRegistration<ReadyForAutomation> srvReg;
  private boolean opened;

  @Activate
  protected void activate(final BundleContext bc) {
    this.bc = bc;
    opened = true;
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    future = scheduler.schedule(() -> {
      synchronized (sync) {
        if (!opened) {
          return;
        }
        srvReg = bc.registerService(ReadyForAutomation.class, new ReadyForAutomation() {
        }, null);
      }
    },
            15, TimeUnit.SECONDS
    );
    scheduler.shutdown();

  }

  @Deactivate
  protected void deactivate() {
    synchronized (sync) {
      opened = false;
      if (future != null) {
        future.cancel(false);
        future = null;
      }
      if (srvReg != null) {
        srvReg.unregister();
        srvReg = null;
      }
    }

  }

}
