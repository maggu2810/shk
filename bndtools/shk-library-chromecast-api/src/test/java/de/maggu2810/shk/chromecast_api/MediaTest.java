/*
 * #%L
 * shk :: Bundles :: Library :: Chromecast API
 * %%
 * Copyright (C) 2014 Vitaly Litvak (vitavaque@gmail.com) and others
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

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import de.maggu2810.shk.chromecast_api.Media.StreamType;

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
