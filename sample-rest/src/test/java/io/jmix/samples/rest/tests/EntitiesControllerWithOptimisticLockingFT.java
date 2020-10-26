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

package io.jmix.samples.rest.tests;

import com.jayway.jsonpath.ReadContext;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.jmix.samples.rest.tools.RestTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource(properties = "jmix.rest.optimisticLockingEnabled=true")
class EntitiesControllerWithOptimisticLockingFT extends EntitiesControllerFT {
    @Test
    void updateCarWithVersion() throws Exception {

        Map<String, String> replacements = new HashMap<>();
        replacements.put("$CAR_ID$", carUuidString);
        replacements.put("$MODEL_ID$", model2UuidString);
        String json = getFileContent("updateCar.json", replacements);

        String url = baseUrl + "/entities/ref_Car/" + carUuidString;
        Map<String, String> params = new HashMap<>();
        params.put("responseView", "carWithModel");

        try (CloseableHttpResponse response = sendPut(url, oauthToken, json, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));

            ReadContext ctx = parseResponse(response);
            assertEquals("ref_Car", ctx.read("$._entityName"));
            assertEquals(carUuidString, ctx.read("$.id"));
            assertEquals(model2UuidString, ctx.read("$.model.id"));
            assertNotNull(ctx.read("$.updateTs"));
            assertNotNull(ctx.read("$.version"));

        }

        json = getFileContent("updateCarWithVersion.json", replacements);
        try (CloseableHttpResponse response = sendPut(url, oauthToken, json, null)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode(response));

            ReadContext ctx = parseResponse(response);
            assertEquals("Optimistic lock", ctx.read("$.error"));
        }

        try (PreparedStatement stmt = conn.prepareStatement("select VIN, MODEL_ID from REF_CAR where ID = ?")) {
            stmt.setObject(1, UUID.fromString(carUuidString));
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            String vin = rs.getString("VIN");
            assertEquals("Modified vin", vin);
            Object modelId = rs.getObject("MODEL_ID");
            assertEquals(model2UuidString, modelId);
        }

    }
}
