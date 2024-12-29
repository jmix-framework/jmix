/*
 * Copyright 2024 Haulmont.
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

package entities;

import com.jayway.jsonpath.ReadContext;
import io.jmix.rest.RestProperties;
import io.jmix.samples.rest.entity.driver.Car;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import test_support.AbstractRestControllerFT;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static test_support.RestTestUtils.*;

@TestPropertySource(properties = "jmix.rest.inline-fetch-plan-enabled=false")
public class NamedFetchPlansOnlyTest extends AbstractRestControllerFT {

    @Autowired
    private RestProperties restProperties;

    @Test
    void testPropertyOverride() {
        assertThat(restProperties.isInlineFetchPlanEnabled()).isEqualTo(false);
    }

    @Test
    void errorLoadEntitiesListWithInlineFetchPlan() throws Exception {
        String url = baseUrl + "/entities/ref_Car";
        Map<String, String> params = new HashMap<>();
        String fpJson = fetchPlanSerialization.toJson(fetchPlanRepository.getFetchPlan(Car.class, "carEdit"));
        params.put("fetchPlan", fpJson);
        params.put("sort", "vin");
        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals("Inline fetch plans are disabled", ctx.read("$.error"));
            assertEquals("Inline fetch plans are disabled. Use only named fetch plans.", ctx.read("$.details"));
        }
    }
}
