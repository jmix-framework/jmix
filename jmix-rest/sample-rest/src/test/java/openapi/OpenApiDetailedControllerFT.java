/*
 * Copyright 2026 Haulmont.
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

package openapi;

import com.jayway.jsonpath.ReadContext;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.jupiter.api.Test;
import test_support.AbstractRestControllerFT;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static test_support.RestTestUtils.parseResponse;
import static test_support.RestTestUtils.sendGet;
import static test_support.RestTestUtils.statusCode;

public class OpenApiDetailedControllerFT extends AbstractRestControllerFT {

    @Test
    public void openApiDetailedContainsSchemas() throws Exception {
        String url = baseUrl + "/docs/openapiDetailed.json";
        try (CloseableHttpResponse response = sendGet(url, oauthToken, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));

            ReadContext ctx = parseResponse(response);
            assertNotNull(ctx.read("$.openapi"));
            assertTrue(ctx.read("$.paths", Map.class).size() > 0);

            Map<String, Object> schemas = ctx.read("$.components.schemas", Map.class);
            assertTrue(schemas.containsKey("entity_ref_Car"));
            assertTrue(schemas.containsKey("entity_rest_ModelEntity"));

            Map<String, Object> modelEntitySchema = ctx.read("$.components.schemas.entity_rest_ModelEntity.properties", Map.class);
            assertTrue(modelEntitySchema.containsKey("stencilId"));
            assertTrue(modelEntitySchema.containsKey("title"));
        }
    }
}
