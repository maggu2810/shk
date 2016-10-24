
package su.litvak.chromecast.api.v2;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import su.litvak.chromecast.api.v2.Media;
import su.litvak.chromecast.api.v2.Media.StreamType;

public class MediaTest {
    final ObjectMapper jsonMapper = new ObjectMapper();

    @Test
    public void itIncludesOptionalFieldsWhenSet() throws Exception {
        final Map<String, Object> customData = new HashMap<>();
        customData.put("a", "b");
        final Map<String, Object> metadata = new HashMap<>();
        metadata.put("1", "2");
        final Media m = new Media(null, null, 123.456d, StreamType.BUFFERED, customData, metadata, null, null);

        final String json = jsonMapper.writeValueAsString(m);

        assertThat(json, containsString("\"duration\":123.456"));
        assertThat(json, containsString("\"streamType\":\"BUFFERED\""));
        assertThat(json, containsString("\"customData\":{\"a\":\"b\"}"));
        assertThat(json, containsString("\"metadata\":{\"1\":\"2\"}"));
    }

    @Test
    public void itDoseNotContainOptionalFieldsWhenNotSet() throws Exception {
        final Media m = new Media(null, null, null, null, null, null, null, null);

        final String json = jsonMapper.writeValueAsString(m);

        assertThat(json, not(containsString("duration")));
        assertThat(json, not(containsString("streamType")));
        assertThat(json, not(containsString("customData")));
        assertThat(json, not(containsString("metadata")));
    }

}
