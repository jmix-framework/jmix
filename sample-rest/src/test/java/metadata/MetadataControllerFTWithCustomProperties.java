/*
 * Copyright 2020 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package metadata;

import com.jayway.jsonpath.ReadContext;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import test_support.AbstractRestControllerFT;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static test_support.RestTestUtils.parseResponse;
import static test_support.RestTestUtils.sendGet;

@TestPropertySource(properties = "jmix.core.legacy-fetch-plan-serialization-attribute-name=true")
public class MetadataControllerFTWithCustomProperties extends AbstractRestControllerFT {

    @Test
    public void getViewWithViewInsteadFetchPlanAttributeName() throws Exception {
        String url = baseUrl + "/metadata/entities/ref_Car/views/carEdit";
        try (CloseableHttpResponse response = sendGet(url, oauthToken, null)) {
            ReadContext ctx = parseResponse(response);
            assertEquals("ref_Car", ctx.read("$.entity"));
            Map<String, Object> viewProperties = (Map<String, Object>) ctx.read("$.properties[?(@.name == 'model')].view", List.class).get(0);
            assertEquals("_local", viewProperties.get("name"));

            Map<String, Object> repairsViewProperties = (Map<String, Object>) ctx.read("$.properties[?(@.name == 'repairs')].view", List.class).get(0);
            assertEquals("repairEdit", repairsViewProperties.get("name"));
            assertTrue(((Collection) repairsViewProperties.get("properties")).size() > 0);
        }
    }

}
