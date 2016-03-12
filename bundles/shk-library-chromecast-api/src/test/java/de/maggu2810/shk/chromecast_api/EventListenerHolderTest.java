/*
 * #%L
 * shk :: Bundles :: Library :: Chromecast API
 * %%
 * Copyright (C) 2015 - 2016 maggu2810
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package de.maggu2810.shk.chromecast_api;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import de.maggu2810.shk.chromecast_api.ChromeCastMessageEvent;
import de.maggu2810.shk.chromecast_api.ChromeCastMessageEventListener;
import de.maggu2810.shk.chromecast_api.EventListenerHolder;
import de.maggu2810.shk.chromecast_api.MediaStatus;
import de.maggu2810.shk.chromecast_api.StandardResponse;
import de.maggu2810.shk.chromecast_api.Status;
import de.maggu2810.shk.chromecast_api.Volume;
import de.maggu2810.shk.chromecast_api.ChromeCastMessageEvent.SpontaneousEventType;

public class EventListenerHolderTest {
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private List<ChromeCastMessageEvent> emittedEvents;
    private EventListenerHolder underTest;

    @Before
    public void before() throws Exception {
        this.emittedEvents = new ArrayList<ChromeCastMessageEvent>();
        this.underTest = new EventListenerHolder();
        this.underTest.registerMessageListener(new ChromeCastMessageEventListener() {
            @Override
            public void messageEventReceived(ChromeCastMessageEvent event) {
                emittedEvents.add(event);
            }
        });
    }

    @Test
    public void itHandlesMediaStatusEvent() throws Exception {
        final String json = FixtureHelper.fixtureAsString("/mediaStatus-chromecast-audio.json").replaceFirst("\"type\"",
                "\"responseType\"");
        this.underTest.deliverMessageEvent(true, jsonMapper.readTree(json));

        ChromeCastMessageEvent event = emittedEvents.get(0);

        assertEquals(SpontaneousEventType.MEDIA_STATUS, event.getType());
        // Is it roughly what we passed in? More throughly tested in MediaStatusTest.
        assertEquals(15, event.getData(MediaStatus.class).supportedMediaCommands);

        assertEquals(1, emittedEvents.size());
    }

    @Test
    public void itHandlesStatusEvent() throws Exception {
        Volume volume = new Volume(123f, false, 2f);
        StandardResponse.Status status = new StandardResponse.Status(new Status(volume, null, false, false));
        this.underTest.deliverMessageEvent(true, jsonMapper.valueToTree(status));

        ChromeCastMessageEvent event = emittedEvents.get(0);

        assertEquals(SpontaneousEventType.STATUS, event.getType());
        // Not trying to test everything, just that is basically what we passed in.
        assertEquals(volume, event.getData(Status.class).volume);

        assertEquals(1, emittedEvents.size());
    }

}
