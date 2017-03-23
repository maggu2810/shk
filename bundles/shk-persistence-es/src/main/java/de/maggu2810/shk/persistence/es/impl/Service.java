
package de.maggu2810.shk.persistence.es.impl;

import java.io.IOException;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Component(immediate = true)
public class Service {

    private Test test;

    @Activate
    protected void activate() throws IOException {
        test = new Test(Test.getSocketTransportAddress());

        test.test();
    }

    @Deactivate
    protected void deactivate() {
        if (test != null) {
            test.close();
        }
    }

}
