
package su.litvak.chromecast.api.v2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

public class StatusTest {
    final ObjectMapper jsonMapper = new ObjectMapper();

    @Test
    public void testDeserializationBackdrop1_18() throws Exception {
        final String jsonMSG = FixtureHelper.fixtureAsString("/status-backdrop-1.18.json").replaceFirst("\"type\"",
                "\"responseType\"");
        final StandardResponse.Status response = jsonMapper.readValue(jsonMSG, StandardResponse.Status.class);

        final Status status = response.status;
        assertNotNull(status);
        assertTrue(status.activeInput);
        assertFalse(status.standBy);

        assertEquals(1, status.applications.size());
        final Application app = status.getRunningApp();
        assertFalse(app.isIdleScreen);

        final Volume volume = status.volume;
        assertNotNull(volume);
        assertEquals(1.0, volume.level, 0.1);
        assertFalse(volume.muted);
        assertNull(volume.controlType);
        assertEquals(Volume.default_increment, volume.increment, 0.001);
        assertEquals(Volume.default_increment, volume.stepInterval, 0.001);
    }

    @Test
    public void testDeserializationBackdrop1_19() throws Exception {
        final String jsonMSG = FixtureHelper.fixtureAsString("/status-backdrop-1.19.json").replaceFirst("\"type\"",
                "\"responseType\"");
        final StandardResponse.Status response = jsonMapper.readValue(jsonMSG, StandardResponse.Status.class);

        final Status status = response.status;
        assertNotNull(status);
        assertFalse(status.activeInput);
        assertFalse(status.standBy);

        assertEquals(1, status.applications.size());
        final Application app = status.getRunningApp();
        assertTrue(app.isIdleScreen);

        final Volume volume = status.volume;
        assertNotNull(volume);
        assertEquals(1.0, volume.level, 0.1);
        assertFalse(volume.muted);
        assertEquals("attenuation", volume.controlType);
        assertEquals(Volume.default_increment, volume.increment, 0.001);
        assertEquals(0.04, volume.stepInterval, 0.001);
    }

    @Test
    public void testDeserializationChromeMirroring() throws Exception {
        final String jsonMSG = FixtureHelper.fixtureAsString("/status-chrome-mirroring-1.19.json")
                .replaceFirst("\"type\"", "\"responseType\"");
        final StandardResponse.Status response = jsonMapper.readValue(jsonMSG, StandardResponse.Status.class);

        final Status status = response.status;
        assertNotNull(status);
        assertFalse(status.activeInput);
        assertFalse(status.standBy);

        assertEquals(1, status.applications.size());
        final Application app = status.getRunningApp();
        assertFalse(app.isIdleScreen);

        final Volume volume = status.volume;
        assertNotNull(volume);
        assertEquals(1.0, volume.level, 0.1);
        assertFalse(volume.muted);
        assertEquals("attenuation", volume.controlType);
        assertEquals(Volume.default_increment, volume.increment, 0.001);
        assertEquals(0.04, volume.stepInterval, 0.001);
    }
}
