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

package bean_validation;

import com.jayway.jsonpath.ReadContext;
import io.jmix.samples.rest.service.RestTestService;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test_support.AbstractRestControllerFT;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static test_support.RestTestUtils.*;

public class BeanValidationFT extends AbstractRestControllerFT {

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat dateFormatResponse = new SimpleDateFormat("dd/MM/yyyy");

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        Locale.setDefault(Locale.ENGLISH);
    }

    @Test
    public void commitValidData() throws Exception {
        String json = getFileContent("currency-valid.json", null);
        String url = baseUrl + "/entities/ref$Currency";

        try (CloseableHttpResponse response = sendPost(url, oauthToken, json, null)) {
            assertEquals(HttpStatus.SC_CREATED, statusCode(response));
            Header[] locationHeaders = response.getHeaders("Location");
            assertEquals(1, locationHeaders.length);
            String location = locationHeaders[0].getValue();
            String expectedUrl = baseUrl + "/entities/ref$Currency";

            assertTrue(location.startsWith(expectedUrl));
            String idString = location.substring(location.lastIndexOf("/") + 1);

            dirtyData.addCurrencyId(idString);
        }
    }

    @Test
    public void commitInvalidData() throws Exception {
        String json = getFileContent("currency-invalid-name.json", null);
        String url = baseUrl + "/entities/ref$Currency";

        try (CloseableHttpResponse response = sendPost(url, oauthToken, json, null)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode(response));

            ReadContext ctx = parseResponse(response);

            assertEquals("may not be null", ctx.read("$[0].message"));
            assertEquals("name", ctx.read("$[0].path"));
        }
    }

    @Test
    public void commitInvalidRecursiveWithOneToManyEntities() throws Exception {
        String json = getFileContent("recursiveEntities.json", null);
        String url = baseUrl + "/entities/rest_RecursiveEntity";

        try (CloseableHttpResponse response = sendPost(url, oauthToken, json, null)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode(response));

            ReadContext ctx = parseResponse(response);

            List<LinkedHashMap<String, String>> jsonArray = ctx.json();

            assertEquals(3, jsonArray.size());
            assertTrue(jsonArray.stream().allMatch(obj -> obj.get("message").equals("may not be null")));
            assertEquals(1, jsonArray.stream().filter(obj -> obj.get("path").equals("entities[0].children[1].name")).count());
            assertEquals(1, jsonArray.stream().filter(obj -> obj.get("path").equals("entities[1].children[0].name")).count());
            assertEquals(1, jsonArray.stream().filter(obj -> obj.get("path").equals("entities[1].name")).count());
        }
    }

    @Test
    public void commitInvalidWithOneToManyEntities() throws Exception {
        String json = getFileContent("order.json", null);
        String url = baseUrl + "/entities/rest_Order";

        try (CloseableHttpResponse response = sendPost(url, oauthToken, json, null)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode(response));

            ReadContext ctx = parseResponse(response);

            List<LinkedHashMap<String, String>> jsonArray = ctx.json();

            assertEquals(2, jsonArray.size());
            assertTrue(jsonArray.stream().allMatch(obj -> obj.get("message").equals("may not be null")));
            assertEquals(1, jsonArray.stream().filter(obj -> obj.get("path").equals("entities[0].products[0].name")).count());
            assertEquals(1, jsonArray.stream().filter(obj -> obj.get("path").equals("entities[0].name")).count());
        }
    }

    @Test
    public void updateWithMissingRequiredFields() throws Exception {
        String currencyCode = "USD";
        executePrepared("insert into REF_CURRENCY (CODE, NAME, UUID, VERSION) values (?, ?, ?, 1)",
                currencyCode,
                "Dollar-1",
                UUID.randomUUID());

        dirtyData.addCurrencyId(currencyCode);

        String json = getFileContent("currency-missing-name.json", null);
        String url = baseUrl + "/entities/ref$Currency/" + currencyCode;

        Map<String, String> params = new HashMap<>();
        params.put("responseView", "currencyWithName");

        try (CloseableHttpResponse response = sendPut(url, oauthToken, json, params)) {

            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals("ref$Currency", ctx.read("$._entityName"));
            assertEquals(currencyCode, ctx.read("$.id"));
            assertEquals("Dollar-1", ctx.read("$.name"));
        }
    }

    @Test
    public void commitInvalidCustomValidationMessage() throws Exception {
        String json = getFileContent("currency-invalid-name-length.json", null);
        String url = baseUrl + "/entities/ref$Currency";

        try (CloseableHttpResponse response = sendPost(url, oauthToken, json, null)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode(response));

            ReadContext ctx = parseResponse(response);

            assertEquals("Epic fail", ctx.read("$[0].message"));
            assertEquals("name", ctx.read("$[0].path"));
            assertEquals("O", ctx.read("$[0].invalidValue"));
        }
    }

    @Test
    public void commitInvalidClassLevelValidators() throws Exception {
        String json = getFileContent("currency-invalid-code-ban.json", null);
        String url = baseUrl + "/entities/ref$Currency";

        try (CloseableHttpResponse response = sendPost(url, oauthToken, json, null)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode(response));

            ReadContext ctx = parseResponse(response);

            assertEquals("Invalid currency", ctx.read("$[0].message"));
            assertEquals("", ctx.read("$[0].path"));
        }
    }

    @Test
    public void commitValidClassLevelValidators() throws Exception {
        String json = getFileContent("currency-valid-code.json", null);
        String url = baseUrl + "/entities/ref$Currency";

        try (CloseableHttpResponse response = sendPost(url, oauthToken, json, null)) {
            assertEquals(HttpStatus.SC_CREATED, statusCode(response));
            Header[] locationHeaders = response.getHeaders("Location");
            assertEquals(1, locationHeaders.length);
            String location = locationHeaders[0].getValue();
            String expectedUrl = baseUrl + "/entities/ref$Currency";

            assertTrue(location.startsWith(expectedUrl));
            String idString = location.substring(location.lastIndexOf("/") + 1);

            dirtyData.addCurrencyId(idString);
        }
    }

    @Test
    public void callValidService() throws Exception {
        String requestBody = getFileContent("service-valid-call.json", null);
        String url = baseUrl + "/services/" + RestTestService.NAME + "/validatedMethod";

        try (CloseableHttpResponse response = sendPost(url, oauthToken, requestBody, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(Integer.valueOf(0), ctx.read("$"));
        }
    }

    @Test
    public void callInvalidService() throws Exception {
        String requestBody = getFileContent("service-invalid-call.json", null);
        String url = baseUrl + "/services/" + RestTestService.NAME + "/validatedMethod";

        try (CloseableHttpResponse response = sendPost(url, oauthToken, requestBody, null)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode(response));
            ReadContext ctx = parseResponse(response);

            assertEquals("must match \"\\d+\"", ctx.read("$[0].message"));
            assertEquals("validatedMethod.code", ctx.read("$[0].path"));
            assertEquals("AA", ctx.read("$[0].invalidValue"));
        }
    }

    @Test
    public void callInvalidServiceResult() throws Exception {
        String requestBody = getFileContent("service-valid-call.json", null);
        String url = baseUrl + "/services/" + RestTestService.NAME + "/validatedMethodResult";

        try (CloseableHttpResponse response = sendPost(url, oauthToken, requestBody, null)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode(response));
        }
    }

    @Test
    public void callInvalidServiceCustomException() throws Exception {
        String requestBody = getFileContent("service-custom-invalid-call.json", null);
        String url = baseUrl + "/services/" + RestTestService.NAME + "/validatedMethodResult";

        try (CloseableHttpResponse response = sendPost(url,
                oauthToken, requestBody, null)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode(response));
            ReadContext ctx = parseResponse(response);

            assertEquals("{io.jmix.core.validation.CustomValidationException}", ctx.read("$[0].messageTemplate"));
            assertEquals("Epic fail!", ctx.read("$[0].message"));
        }
    }


    @Test
    public void createSellerWithInvalidPastDate() throws Exception {
        String url = baseUrl + "/entities/ref$Seller";
        Date futureDate = getDateWithDifferentDay(5);

        Map<String, String> replacement = new HashMap<>();
        replacement.put("$NAME$", "Bob");
        replacement.put("$CONTRACT_START_DATE$", dateFormat.format(futureDate));
        replacement.put("$CONTRACT_END_DATE$", dateFormat.format(futureDate));

        String json = getFileContent("createNewSeller.json", replacement);
        try (CloseableHttpResponse response = sendPost(url, oauthToken, json, null)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode(response));

            ReadContext ctx = parseResponse(response);

            assertEquals(1, (int) ctx.read("$.length()"));
            assertEquals("Must be in the past", ctx.read("$[0].message"));
            assertEquals("contractStartDate", ctx.read("$[0].path"));
            assertEquals(dateFormatResponse.format(futureDate), ctx.read("$[0].invalidValue"));
        }
    }

    @Test
    public void createSellerWithInvalidFutureDate() throws Exception {
        String url = baseUrl + "/entities/ref$Seller";
        Date pastDate = getDateWithDifferentDay(-5);

        Map<String, String> replacement = new HashMap<>();
        replacement.put("$NAME$", "Dorian Green");
        replacement.put("$CONTRACT_START_DATE$", dateFormat.format(pastDate));
        replacement.put("$CONTRACT_END_DATE$", dateFormat.format(pastDate));

        String json = getFileContent("createNewSeller.json", replacement);
        try (CloseableHttpResponse response = sendPost(url, oauthToken, json, null)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode(response));

            ReadContext ctx = parseResponse(response);

            assertEquals(1, (int) ctx.read("$.length()"));
            assertEquals("Must be in the future", ctx.read("$[0].message"));
            assertEquals("contractEndDate", ctx.read("$[0].path"));
            assertEquals(dateFormatResponse.format(pastDate), ctx.read("$[0].invalidValue"));
        }
    }

    private Date getDateWithDifferentDay(int differentDay) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, differentDay);
        return calendar.getTime();
    }

    private void executePrepared(String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            stmt.executeUpdate();
        }
    }
}
