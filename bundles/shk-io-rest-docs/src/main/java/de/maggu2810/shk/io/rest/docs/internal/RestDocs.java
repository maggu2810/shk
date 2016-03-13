
package de.maggu2810.shk.io.rest.docs.internal;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
public class RestDocs {

    private static final String ALIAS = "/doc";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Reference
    private HttpService httpService;

    @Activate
    protected void activate() {
        try {
            httpService.registerResources(ALIAS, "swagger", httpService.createDefaultHttpContext());
        } catch (final NamespaceException ex) {
            logger.error("Could not start up REST documentation service: {}", ex.getMessage());
        }
    }

    @Deactivate
    protected void deactivate() {
        httpService.unregister(ALIAS);
    }

}
