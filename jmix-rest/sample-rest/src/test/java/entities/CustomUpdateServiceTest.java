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

package entities;

import com.jayway.jsonpath.ReadContext;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.jupiter.api.Test;
import test_support.AbstractRestControllerFT;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static test_support.RestTestUtils.*;

public class CustomUpdateServiceTest extends AbstractRestControllerFT {

    @Test
    void testCreateUpdateDelete() throws Exception {
        UUID customerId;

        String url = baseUrl + "/entities/sales_Customer";
        String json = getFileContent("custom-update-service-create-customer.json", null);
        try (CloseableHttpResponse response = sendPost(url, oauthToken, json, new HashMap<>())) {
            assertEquals(HttpStatus.SC_CREATED, statusCode(response));
            ReadContext ctx = parseResponse(response);
            customerId = UUID.fromString(ctx.read("$.id"));
            dirtyData.addCustomerId(customerId);

            // Check that io.jmix.samples.rest.service.CustomerService has updated the `comments` attribute
            Map<String, Object> customerRecord = jdbcTemplate.queryForMap("select NAME, COMMENTS from SALES_CUSTOMER where ID = ?", customerId);
            String comments = (String) customerRecord.get("COMMENTS");
            assertTrue(comments != null && comments.contains("New customer") && !comments.contains("Customer updated"));
        }

        url = baseUrl + "/entities/sales_Customer/" + customerId;
        json = getFileContent("custom-update-service-update-customer.json", null);
        try (CloseableHttpResponse response = sendPut(url, oauthToken, json, new HashMap<>())) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));

            // Check that io.jmix.samples.rest.service.CustomerService has updated the `comments` attribute
            Map<String, Object> customerRecord = jdbcTemplate.queryForMap("select NAME, COMMENTS from SALES_CUSTOMER where ID = ?", customerId);
            String comments = (String) customerRecord.get("COMMENTS");
            assertTrue(comments != null && comments.contains("Customer updated"));
        }

        try (CloseableHttpResponse response = sendDelete(url, oauthToken, new HashMap<>())) {
            assertEquals(HttpStatus.SC_NO_CONTENT, statusCode(response));

            // Check that io.jmix.samples.rest.service.CustomerService has updated the `comments` attribute instead of removing entity
            Map<String, Object> customerRecord = jdbcTemplate.queryForMap("select NAME, COMMENTS from SALES_CUSTOMER where ID = ?", customerId);
            String comments = (String) customerRecord.get("COMMENTS");
            assertTrue(comments != null && comments.contains("Customer removed"));
        }
    }
}
