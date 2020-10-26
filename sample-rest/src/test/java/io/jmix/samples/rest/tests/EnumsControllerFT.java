/*
 * Copyright 2019 Haulmont.
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

package io.jmix.samples.rest.tests;

import com.jayway.jsonpath.ReadContext;
import net.minidev.json.JSONArray;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.jmix.samples.rest.tools.RestTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 */
public class EnumsControllerFT extends AbstractRestControllerFT {

    @Test
    public void getAllEnums() throws Exception {
        String url = baseUrl + "/metadata/enums";
        try (CloseableHttpResponse response = sendGet(url, oauthToken, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertTrue(ctx.read("$.length()", Integer.class) > 1);

            assertEquals(2, (int) ctx.read("$[?(@.name == 'io.jmix.samples.rest.entity.driver.DriverStatus')].values.length()", List.class).get(0));
            Map<String, Object> value1 = (Map<String, Object>) ctx.read("$[?(@.name == 'io.jmix.samples.rest.entity.driver.DriverStatus')].values[0]", JSONArray.class).get(0);
            assertEquals("ACTIVE", value1.get("name"));
            assertEquals("Active", value1.get("caption"));
            assertEquals(10, value1.get("id"));
        }
    }

    @Test
    public void getEnum() throws Exception {
        String url = baseUrl + "/metadata/enums/io.jmix.samples.rest.entity.driver.DriverStatus";
        try (CloseableHttpResponse response = sendGet(url, oauthToken, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);

            assertEquals("io.jmix.samples.rest.entity.driver.DriverStatus", ctx.read("$.name"));
            assertEquals(2, (int) ctx.read("$.values.length()", Integer.class));
            assertEquals("ACTIVE", ctx.read("$.values[0].name"));
            assertEquals("Active", ctx.read("$.values[0].caption"));
            assertEquals(10, (int) ctx.read("$.values[0].id", Integer.class));
        }
    }

    @Test
    public void getNonExistingEnum() throws Exception {
        String url = baseUrl + "/metadata/enums/io.jmix.samples.rest.entity.driver.NonExistingEnum";
        try (CloseableHttpResponse response = sendGet(url, oauthToken, null)) {
            assertEquals(HttpStatus.SC_NOT_FOUND, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals("Enum not found", ctx.read("$.error"));
        }
    }
}
