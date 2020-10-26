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


import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import io.jmix.core.Id;
import io.jmix.samples.rest.entity.driver.Model;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static io.jmix.samples.rest.tools.RestTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;


class EntitiesControllerFT extends AbstractRestControllerFT {

    protected String carUuidString;
    private String carDocumentationUuidString;
    private String secondCarUuidString;
    private String colourUuidString;
    private String repairUuidString;
    private String modelUuidString;
    protected String model2UuidString;
    private String repair2UuidString;
    private String model3UuidString;
    private String driverUuidString;
    private String debtorUuidString;
    private String plantUuidString;

    private String modelName = "Audi A3";
    private String model2Name = "BMW X5";
    private int modelNumberOfSeats = 5;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private UUID numberOfSeatsCategoryAttrValueId;

    /**
     * Should load an entity with _local attributes only
     */
    @Test
    void loadEntityById() throws Exception {
        String url = baseUrl + "/entities/ref_Car/" + carUuidString;
        try (CloseableHttpResponse response = sendGet(url, oauthToken, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(carUuidString, ctx.read("$.id"));
            assertEquals("VWV000", ctx.read("$._instanceName"));

            try {
                ctx.read("&.colour");
                fail();
            } catch (PathNotFoundException ignored) {
            }

            try {
                ctx.read("&.model");
                fail();
            } catch (PathNotFoundException ignored) {
            }
        }
    }

    @Test
    void loadEntityWithDynamicAttributes() throws Exception {
        String url = baseUrl + "/entities/ref_Car/" + carUuidString;
        Map<String, String> params = new HashMap<>();
        params.put("dynamicAttributes", "true");
        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(carUuidString, ctx.read("$.id"));
            assertEquals("VWV000", ctx.read("$._instanceName"));
            //TODO Dynamic attribute
//            assertEquals("10", ctx.read("$.+numberOfSeatsAttr"));
        }
    }

    @Test
    void loadEntityWithInvalidInstanceName() throws Exception {
        String url = baseUrl + "/entities/ref_Car/" + carUuidString;
        Map<String, String> params = new HashMap<>();
        params.put("view", "car-without-vin");
        params.put("returnNulls", "true");
        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            ReadContext ctx = parseResponse(response);
            assertEquals(carUuidString, ctx.read("$.id"));
            assertNull(ctx.read("$._instanceName"));
        }
    }

    /**
     * Should load an entity with attributes defined in the view
     */
    @Test
    void loadEntityByIdWithView() throws Exception {
        String url = baseUrl + "/entities/ref_Car/" + carUuidString;
        Map<String, String> params = new HashMap<>();
        params.put("view", "carEdit");
        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(carUuidString, ctx.read("$.id"));
            assertEquals("VWV000", ctx.read("$.vin"));
            assertEquals(modelUuidString, ctx.read("$.model.id"));
            assertEquals(2, ctx.<Collection>read("$.repairs").size());

            assertEquals(colourUuidString, ctx.read("$.colour.id"));

            assertThrows(PathNotFoundException.class, () -> ctx.read("$.seller"));
        }
    }

    @Test
    void loadEntityByIdWithMissingView() throws Exception {
        String url = baseUrl + "/entities/ref_Car/" + carUuidString;
        Map<String, String> params = new HashMap<>();
        params.put("view", "missingViewName");
        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals("View not found", ctx.read("$.error"));
            assertEquals(String.format("View %s for entity ref_Car not found", "missingViewName"), ctx.read("$.details"));
        }
    }

    @Test
    void loadEntityByIdWithInvalidId() throws Exception {
        String invalidId = "A";
        String url = baseUrl + "/entities/ref_Car/" + invalidId;
        Map<String, String> params = new HashMap<>();
        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals("Invalid entity ID", ctx.read("$.error"));
            assertEquals(String.format("Cannot convert %s into valid entity ID", invalidId), ctx.read("$.details"));
        }
    }

    @Test
    void loadEntityWithLobFieldByIdWithView() throws Exception {
        String url = baseUrl + "/entities/ref$ExtDriver/" + driverUuidString;
        Map<String, String> params = new HashMap<>();
        params.put("view", "test1");
        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(driverUuidString, ctx.read("$.id"));
            assertEquals("The notes", ctx.read("$.notes"));
        }
    }

    @Test
    void loadEntitiesWithLobFieldWithView() throws Exception {
        String url = baseUrl + "/entities/ref$ExtDriver";
        Map<String, String> params = new HashMap<>();
        params.put("view", "test1");
        params.put("sort", "createTs");
        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(driverUuidString, ctx.read("$[0].id"));
            assertEquals("The notes", ctx.read("$[0].notes"));
        }
    }

    @Test
    void loadEntityByIdWithViewAndReturnNullsOption() throws Exception {
        String url = baseUrl + "/entities/ref_Car/" + carUuidString;
        Map<String, String> params = new HashMap<>();
        params.put("view", "carEdit");
        params.put("returnNulls", "true");
        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(carUuidString, ctx.read("$.id"));
            assertEquals(modelUuidString, ctx.read("$.model.id"));
            assertEquals(2, ctx.<Collection>read("$.repairs").size());

            //colour property has null value and this value is in the JSON
            assertNull(ctx.read("$.seller"));
        }
    }

    @Test
    void loadEntityWithNonExistingId() throws Exception {
        UUID randomId = UUID.randomUUID();
        String url = baseUrl + "/entities/ref_Car/" + randomId;
        try (CloseableHttpResponse response = sendGet(url, oauthToken, null)) {
            assertEquals(HttpStatus.SC_NOT_FOUND, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals("Entity not found", ctx.read("$.error"));
            assertEquals(String.format("Entity ref_Car with id %s not found", randomId), ctx.read("$.details"));
        }
    }

    @Test
    void loadEntityWithNonExistingMetaClass() throws Exception {
        UUID randomId = UUID.randomUUID();
        String url = baseUrl + "/entities/ref$NonExistingMetaClass/" + randomId;
        try (CloseableHttpResponse response = sendGet(url, oauthToken, null)) {
            assertEquals(HttpStatus.SC_NOT_FOUND, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals("MetaClass not found", ctx.read("$.error"));
            assertEquals("MetaClass ref$NonExistingMetaClass not found", ctx.read("$.details"));
        }
    }


    @Test
    void loadEntitiesList() throws Exception {
        String url = baseUrl + "/entities/ref$Colour";
        Map<String, String> params = new HashMap<>();
        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            Header[] totalCountHeaders = response.getHeaders("x-total-count");
            assertEquals(0, totalCountHeaders.length);
            ReadContext ctx = parseResponse(response);

            int count;
            try (PreparedStatement stmt = conn.prepareStatement("select count(*) from REF_COLOUR where delete_ts is null")) {
                ResultSet rs = stmt.executeQuery();
                assertTrue(rs.next());
                count = rs.getInt(1);
            }
            assertEquals(count, ctx.<Collection>read("$").size());
            assertNotNull(ctx.read("$[0]._instanceName"));
        }
    }

    @Test
    void loadEntitiesListWithCountHeader() throws Exception {
        String url = baseUrl + "/entities/ref$Colour";
        Map<String, String> params = new HashMap<>();
        params.put("returnCount", "true");
        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));

            ReadContext ctx = parseResponse(response);
            int count;
            try (PreparedStatement stmt = conn.prepareStatement("select count(*) from REF_COLOUR where delete_ts is null")) {
                ResultSet rs = stmt.executeQuery();
                assertTrue(rs.next());
                count = rs.getInt(1);
            }
            assertEquals(count, ctx.<Collection>read("$").size());
            assertNotNull(ctx.read("$[0]._instanceName"));

            Header[] totalCountHeaders = response.getHeaders("X-Total-Count");
            assertEquals(1, totalCountHeaders.length);
            assertEquals(String.valueOf(count), totalCountHeaders[0].getValue());
        }
    }

    @Test
    void loadEntitiesListWithOrder() throws Exception {
        String url = baseUrl + "/entities/ref$Colour";
        Map<String, String> params = new HashMap<>();
        params.put("sort", "name");
        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals("Colour 1", ctx.read("$.[0].name"));
        }

        params.put("sort", "+name");
        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals("Colour 1", ctx.read("$.[0].name"));
        }

        params.put("sort", "-name");
        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals("Colour 5", ctx.read("$.[0].name"));
        }
    }

    @Test
    void loadEntitiesListWithMultipleOrder() throws Exception {
        String url = baseUrl + "/entities/ref$Colour";
        Map<String, String> params = new HashMap<>();

        params.put("sort", "description, -name");
        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals("Description 1", ctx.read("$.[0].description"));
            assertEquals("Colour 3", ctx.read("$.[0].name"));
        }

        params.put("sort", "-description, +name");
        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals("Description 2", ctx.read("$.[0].description"));
            assertEquals("Colour 4", ctx.read("$.[0].name"));
        }
    }

    @Test
    void loadEntitiesListWithLimitAndOffset() throws Exception {
        String url = baseUrl + "/entities/ref$Colour";
        Map<String, String> params = new HashMap<>();
        params.put("limit", "3");
        params.put("offset", "2");
        params.put("sort", "name");
        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(3, ctx.<Collection>read("$").size());
            assertEquals("Colour 3", ctx.read("$.[0].name"));
        }
    }

    @Test
    void loadEntitiesListWithView() throws Exception {
        String url = baseUrl + "/entities/ref_Car";
        Map<String, String> params = new HashMap<>();
        params.put("view", "carEdit");
        params.put("sort", "vin");
        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertNotNull(ctx.read("$.[0].model.id"));
            assertTrue(ctx.<Collection>read("$.[0].repairs").size() > 0);

            assertThrows(PathNotFoundException.class, () -> ctx.read("$.[0].seller"));
        }
    }

    @Test
    void loadEntitiesListWithViewAndReturnNulls() throws Exception {
        String url = baseUrl + "/entities/ref_Car";
        Map<String, String> params = new HashMap<>();
        params.put("view", "carEdit");
        params.put("sort", "vin");
        params.put("returnNulls", "true");
        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertNotNull(ctx.read("$.[0].model.id"));
            assertTrue(ctx.<Collection>read("$.[0].repairs").size() > 0);

            assertNull(ctx.read("$.[0].seller"));
        }
    }

    @Test
    void loadEntitiesListWithFilterGet() throws Exception {
        String url = baseUrl + "/entities/ref$Colour/search";
        String json = getFileContent("entitiesFilter.json", null);
        Map<String, String> params = new HashMap<>();
        params.put("filter", json);
        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            Header[] totalCountHeaders = response.getHeaders("x-total-count");
            assertEquals(0, totalCountHeaders.length);
            ReadContext ctx = parseResponse(response);
            assertEquals(1, ctx.<Collection>read("$").size());
            assertEquals("Colour 1", ctx.read("$[0].name"));
        }
    }

    @Test
    void loadEntitiesListWithFilterPost() throws Exception {
        String url = baseUrl + "/entities/ref$Colour/search";
        String json = getFileContent("entitiesFilterPost1.json", null);
        Map<String, String> params = new HashMap<>();
        try (CloseableHttpResponse response = sendPost(url, oauthToken, json, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            Header[] totalCountHeaders = response.getHeaders("x-total-count");
            assertEquals(0, totalCountHeaders.length);
            ReadContext ctx = parseResponse(response);
            assertEquals(1, ctx.<Collection>read("$").size());
            assertEquals("Colour 1", ctx.read("$[0].name"));
        }
    }

    @Test
    void loadEntitiesListWithFilterAndCountGet() throws Exception {
        String url = baseUrl + "/entities/ref$Colour/search";
        String json = getFileContent("entitiesFilter.json", null);
        Map<String, String> params = new HashMap<>();
        params.put("returnCount", "true");
        params.put("filter", json);
        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            Header[] totalCountHeaders = response.getHeaders("x-total-count");
            assertEquals(1, totalCountHeaders.length);
            assertEquals("1", totalCountHeaders[0].getValue());
            ReadContext ctx = parseResponse(response);
            assertEquals(1, ctx.<Collection>read("$").size());
            assertEquals("Colour 1", ctx.read("$[0].name"));
        }
    }

    @Test
    void loadEntitiesFilterIsNull() throws Exception {
        String url = baseUrl + "/entities/ref_Car/search";
        String json = getFileContent("entitiesFilterIsNull.json", null);
        Map<String, String> params = new HashMap<>();
        params.put("filter", json);
        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(2, ctx.<Collection>read("$").size());
        }
    }

    @Test
    void loadEntitiesListWithFilterAndCountPost() throws Exception {
        String url = baseUrl + "/entities/ref$Colour/search";
        String json = getFileContent("entitiesFilterPost2.json", null);
        Map<String, String> params = new HashMap<>();
        params.put("returnCount", "true");
        try (CloseableHttpResponse response = sendPost(url, oauthToken, json, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            Header[] totalCountHeaders = response.getHeaders("x-total-count");
            assertEquals(1, totalCountHeaders.length);
            assertEquals("1", totalCountHeaders[0].getValue());
            ReadContext ctx = parseResponse(response);
            assertEquals(1, ctx.<Collection>read("$").size());
            assertEquals("Colour 1", ctx.read("$[0].name"));
        }
    }

    @Test
    void loadEntitiesListWithInvalidFilterGet() throws Exception {
        String url = baseUrl + "/entities/ref$Colour/search";
        String json = getFileContent("entitiesFilterInvalid.json", null);
        Map<String, String> params = new HashMap<>();
        params.put("filter", json);
        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals("Cannot parse entities filter", ctx.read("$.error"));
            assertEquals("Operator > is not available for java type java.lang.String", ctx.read("$.details"));
        }
    }

    @Test
    void loadEntitiesListWithInvalidFilterPost() throws Exception {
        String url = baseUrl + "/entities/ref$Colour/search";
        String json = getFileContent("entitiesFilterInvalidPost.json", null);
        Map<String, String> params = new HashMap<>();
        try (CloseableHttpResponse response = sendPost(url, oauthToken, json, params)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals("Cannot parse entities filter", ctx.read("$.error"));
            assertEquals("Operator > is not available for java type java.lang.String", ctx.read("$.details"));
        }
    }

    @Test
    void loadEntitiesListWithInvalidFilterPost2() throws Exception {
        String url = baseUrl + "/entities/ref$Colour/search";
        String json = getFileContent("entitiesFilterInvalidPost2.json", null);
        Map<String, String> params = new HashMap<>();
        try (CloseableHttpResponse response = sendPost(url, oauthToken, json, params)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals("Cannot parse entities filter", ctx.read("$.error"));
            assertEquals("Entities filter cannot be null", ctx.read("$.details"));
        }
    }

    @Test
    void loadEntitiesFilterStartsWith() throws Exception {
        String url = baseUrl + "/entities/ref_Car/search";
        String json = getFileContent("entitiesFilterStartsWith.json", null);
        Map<String, String> params = new HashMap<>();
        params.put("filter", json);
        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(2, ctx.<Collection>read("$").size());
            assertTrue(((String) ctx.read("$[0].vin")).startsWith("VW"));
            assertTrue(((String) ctx.read("$[1].vin")).startsWith("VW"));
        }
    }

    @Test
    void loadEntitiesFilterWithViewPost() throws Exception {
        String url = baseUrl + "/entities/ref_Car/search";
        String json = getFileContent("entitiesFilterWithView.json", null);
        Map<String, String> params = new HashMap<>();
        try (CloseableHttpResponse response = sendPost(url, oauthToken, json, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(2, ctx.<Collection>read("$").size());
            assertTrue(((String) ctx.read("$[0].vin")).startsWith("VW"));
            assertEquals(colourUuidString, ctx.read("$.[0]colour.id"));
        }
    }

    @Test
    /**
     * viewName is supported fo backward compatibility
     */
    void loadEntitiesFilterWithViewNamePost() throws Exception {
        String url = baseUrl + "/entities/ref_Car/search";
        String json = getFileContent("entitiesFilterWithViewName.json", null);
        Map<String, String> params = new HashMap<>();
        try (CloseableHttpResponse response = sendPost(url, oauthToken, json, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(2, ctx.<Collection>read("$").size());
            assertTrue(((String) ctx.read("$[0].vin")).startsWith("VW"));
            assertEquals(colourUuidString, ctx.read("$.[0]colour.id"));
        }
    }

    @Test
    void createNewEntity() throws Exception {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("$MODEL_ID$", modelUuidString);
        String json = getFileContent("car.json", replacements);

        UUID carId;
        String url = baseUrl + "/entities/ref_Car";

        Map<String, String> params = new HashMap<>();
        params.put("responseView", "carWithTransform");

        try (CloseableHttpResponse response = sendPost(url, oauthToken, json, params)) {
            assertEquals(HttpStatus.SC_CREATED, statusCode(response));
            Header[] locationHeaders = response.getHeaders("Location");
            assertEquals(1, locationHeaders.length);
            String location = locationHeaders[0].getValue();
            assertTrue(location.startsWith("http://localhost:" + port + "/rest/entities/ref_Car"));
            String idString = location.substring(location.lastIndexOf("/") + 1);
            carId = UUID.fromString(idString);

            ReadContext ctx = parseResponse(response);
            assertEquals("ref_Car", ctx.read("$._entityName"));
            assertEquals(carId.toString(), ctx.read("$.id"));
            assertEquals(2, (int) ctx.read("$.repairs.length()"));
            assertNotNull(ctx.read("$.createTs"));
            assertNotNull(ctx.read("$.version"));

            //to delete the created objects in the @After method
            dirtyData.addCarId(carId);
            try (PreparedStatement stmt = conn.prepareStatement("select ID from REF_REPAIR where CAR_ID = ?")) {
                stmt.setObject(1, carId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    dirtyData.addRepairId((UUID.fromString(rs.getString("ID"))));
                }
            }
        }

        try (PreparedStatement stmt = conn.prepareStatement("select VIN, MODEL_ID from REF_CAR where ID = ?")) {
            stmt.setObject(1, carId);
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            String vin = rs.getString("VIN");
            assertEquals("123", vin);
            Object modelId = rs.getObject("MODEL_ID");
            assertEquals(modelUuidString, modelId);
        }

        try (PreparedStatement stmt = conn.prepareStatement("select DESCRIPTION, REPAIR_DATE from REF_REPAIR where CAR_ID = ? order by DESCRIPTION")) {
            stmt.setObject(1, carId);
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            String description1 = rs.getString("DESCRIPTION");
            assertEquals("Repair 1", description1);
            Date date1 = rs.getDate("REPAIR_DATE");
            assertEquals("2016-06-08", sdf.format(date1));
            assertTrue(rs.next());
            String description2 = rs.getString("DESCRIPTION");
            assertEquals("Repair 2", description2);
            Date date2 = rs.getDate("REPAIR_DATE");
            assertEquals("2016-06-20", sdf.format(date2));
        }
    }

    @Test
    @Disabled
    void createNewEntityWithDynamicAttribute() throws Exception {
        Map<String, String> replacements = new HashMap<>();
        String numberOfSeats = "10";
        replacements.put("$NUMBER_OF_SEATS$", numberOfSeats);
        String json = getFileContent("carWithDynamicAttribute.json", replacements);

        UUID carId;
        String url = baseUrl + "/entities/ref_Car";

        try (CloseableHttpResponse response = sendPost(url, oauthToken, json, null)) {
            assertEquals(HttpStatus.SC_CREATED, statusCode(response));
            Header[] locationHeaders = response.getHeaders("Location");
            assertEquals(1, locationHeaders.length);
            String location = locationHeaders[0].getValue();
            assertTrue(location.startsWith("http://localhost:" + port + "/rest/entities/ref_Car"));
            String idString = location.substring(location.lastIndexOf("/") + 1);
            carId = UUID.fromString(idString);

            ReadContext ctx = parseResponse(response);
            assertEquals("ref_Car", ctx.read("$._entityName"));
            assertEquals(carId.toString(), ctx.read("$.id"));

            //to delete the created objects in the @After method
            dirtyData.addCarId(carId);
            try (PreparedStatement stmt = conn.prepareStatement("select ID, STRING_VALUE from SYS_ATTR_VALUE where ENTITY_ID = ?")) {
                stmt.setObject(1, carId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    UUID categoryAttrValueId = UUID.fromString(rs.getString("ID"));
                    dirtyData.addCategoryAttributeValueId(categoryAttrValueId);
                    String dynamicAttributeValue = rs.getString("STRING_VALUE");
                    assertEquals(numberOfSeats, dynamicAttributeValue);
                }
            }
        }
    }

    @Test
    void createNewEntityWithStringKey() throws Exception {
        Map<String, String> replacements = new HashMap<>();
        String json = getFileContent("newCurrency.json", replacements);

        String url = baseUrl + "/entities/ref$Currency";

        String currencyId;
        Map<String, String> params = new HashMap<>();
        params.put("responseView", "currencyWithCodeAndName");

        try (CloseableHttpResponse response = sendPost(url, oauthToken, json, params)) {
            assertEquals(HttpStatus.SC_CREATED, statusCode(response));
            Header[] locationHeaders = response.getHeaders("Location");
            assertEquals(1, locationHeaders.length);
            String location = locationHeaders[0].getValue();
            assertTrue(location.startsWith("http://localhost:" + port + "/rest/entities/ref$Currency"));
            currencyId = location.substring(location.lastIndexOf("/") + 1);

            ReadContext ctx = parseResponse(response);
            assertEquals("ref$Currency", ctx.read("$._entityName"));
            assertEquals("RUB", currencyId);
            assertEquals(currencyId, ctx.read("$.id"));
            assertEquals("RUB", ctx.read("$.code"));
            assertEquals("Ruble", ctx.read("$.name"));

            //to delete the created objects in the @After method
            dirtyData.addCurrencyId(currencyId);
        }

        try (PreparedStatement stmt = conn.prepareStatement("select NAME from REF_CURRENCY where CODE = ?")) {
            stmt.setString(1, currencyId);
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            String name = rs.getString("NAME");
            assertEquals("Ruble", name);
        }
    }

    @Test
    void createNewEntityWithoutResponseView() throws Exception {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("$MODEL_ID$", model3UuidString);
        String json = getFileContent("carWithModel.json", replacements);
        UUID carId;
        String url = baseUrl + "/entities/ref_Car";

        try (CloseableHttpResponse response = sendPost(url, oauthToken, json, null)) {
            assertEquals(HttpStatus.SC_CREATED, statusCode(response));
            Header[] locationHeaders = response.getHeaders("Location");
            assertEquals(1, locationHeaders.length);
            String location = locationHeaders[0].getValue();
            assertTrue(location.startsWith("http://localhost:" + port + "/rest/entities/ref_Car"));
            String idString = location.substring(location.lastIndexOf("/") + 1);
            carId = UUID.fromString(idString);

            ReadContext ctx = parseResponse(response);

            assertEquals(3, (int) ctx.read("$.length()"));
            assertEquals(carId.toString(), ctx.read("$.id"));
            assertEquals("123", ctx.read("$._instanceName"));
            assertEquals("ref_Car", ctx.read("$._entityName"));

            //to delete the created objects in the @After method
            dirtyData.addCarId(carId);
        }
    }

    @Test
    void createNewEntityWithResponseView() throws Exception {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("$MODEL_ID$", model3UuidString);

        String json = getFileContent("carWithModel.json", replacements);

        UUID carId;
        String url = baseUrl + "/entities/ref_Car";

        Map<String, String> params = new HashMap<>();
        params.put("responseView", "_local");

        try (CloseableHttpResponse response = sendPost(url, oauthToken, json, params)) {
            assertEquals(HttpStatus.SC_CREATED, statusCode(response));
            Header[] locationHeaders = response.getHeaders("Location");
            assertEquals(1, locationHeaders.length);
            String location = locationHeaders[0].getValue();
            assertTrue(location.startsWith("http://localhost:" + port + "/rest/entities/ref_Car"));
            String idString = location.substring(location.lastIndexOf("/") + 1);
            carId = UUID.fromString(idString);

            ReadContext ctx = parseResponse(response);

            assertEquals(8, (int) ctx.read("$.length()"));
            assertEquals(carId.toString(), ctx.read("$.id"));
            assertEquals("123", ctx.read("$._instanceName"));
            assertEquals("ref_Car", ctx.read("$._entityName"));
            assertEquals("123", ctx.read("$.vin"));

            //to delete the created objects in the @After method
            dirtyData.addCarId(carId);
        }
    }

    @Test
    void createNewEntityWithInvalidJSON() throws Exception {
        Map<String, String> replacements = new HashMap<>();
        String json = getFileContent("invalidDriver.json", replacements);
        String url = baseUrl + "/entities/ref$Driver";
        try (CloseableHttpResponse response = sendPost(url, oauthToken, json, null)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals("Cannot deserialize an entity from JSON", ctx.read("$.error"));
            assertEquals("", ctx.read("$.details"));
        }
    }

    @Test
    void createNewEntityWithNonExistingReference() throws Exception {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("$MODEL_ID$", UUID.randomUUID().toString());
        String json = getFileContent("car.json", replacements);
        String url = baseUrl + "/entities/ref_Car";
        try (CloseableHttpResponse response = sendPost(url, oauthToken, json, null)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertTrue(ctx.<String>read("$.error").startsWith("Entity creation failed"));
        }
    }

    @Test
    @Disabled
        //TODO An attempt to save an entity with reference to some not persisted entity.
    void createNewEntityWithDeletedReference() throws Exception {
        dataManager.remove(Id.of(UUID.fromString(model3UuidString), Model.class));
        Map<String, String> replacements = new HashMap<>();
        replacements.put("$MODEL_ID$", model3UuidString);
        String json = getFileContent("carWithModel.json", replacements);
        String url = baseUrl + "/entities/ref_Car";
        try (CloseableHttpResponse response = sendPost(url, oauthToken, json, null)) {
            assertEquals(HttpStatus.SC_CREATED, statusCode(response));

            Header[] locationHeaders = response.getHeaders("Location");
            assertEquals(1, locationHeaders.length);
            String location = locationHeaders[0].getValue();
            assertTrue(location.startsWith("http://localhost:" + port + "/rest/entities/ref_Car"));
            String idString = location.substring(location.lastIndexOf("/") + 1);
            UUID carId = UUID.fromString(idString);
            dirtyData.addCarId(carId);
        }
    }

    @Test
    void createEntityWithEnum() throws Exception {
        String json = getFileContent("createEntityWithEnum.json", null);
        String url = baseUrl + "/entities/ref$Driver";

        Map<String, String> params = new HashMap<>();
        params.put("responseView", "driverWithStatusAndName");

        try (CloseableHttpResponse response = sendPost(url, oauthToken, json, params)) {
            assertEquals(HttpStatus.SC_CREATED, statusCode(response));
            ReadContext ctx = parseResponse(response);

            assertEquals("ACTIVE", ctx.read("$.status"));
            assertEquals("John Smith", ctx.read("$.name"));

            String strDriverId = ctx.read("$.id");
            UUID driverId = UUID.fromString(strDriverId);
            //to delete the created objects in the @After method

            dirtyData.addDriverId(driverId);

            try (PreparedStatement stmt = conn.prepareStatement("select NAME, STATUS from REF_DRIVER where ID = ?")) {
                stmt.setObject(1, driverId);
                ResultSet rs = stmt.executeQuery();
                assertTrue(rs.next());
                assertEquals("John Smith", rs.getString("NAME"));
                assertEquals(10, rs.getInt("STATUS"));
            }
        }
    }

    @Test
    void createEntityWithTransientProperty() throws Exception {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("$DEBTOR_ID$", debtorUuidString);

        String json = getFileContent("createEntityWithTransientProperty.json", replacements);
        String url = baseUrl + "/entities/debt$Case";

        Map<String, String> params = new HashMap<>();
        params.put("responseView", "caseWithTransientProperty");

        try (CloseableHttpResponse response = sendPost(url, oauthToken, json, params)) {
            assertEquals(HttpStatus.SC_CREATED, statusCode(response));
            ReadContext ctx = parseResponse(response);

            assertEquals("test1", ctx.read("$.test1"));
            assertEquals("np1", ctx.read("$.nonPersistent1"));

            String caseId = ctx.read("$.id");
            UUID caseUUID = UUID.fromString(caseId);
            //to delete the created objects in the @After method

            dirtyData.addCaseId(caseUUID);

            try (PreparedStatement stmt = conn.prepareStatement("select TEST1 from DEBT_CASE where ID = ?")) {
                stmt.setObject(1, caseUUID);
                ResultSet rs = stmt.executeQuery();
                assertTrue(rs.next());
                String test1 = rs.getString("TEST1");
                assertEquals("test1", test1);
            }
        }
    }

    @Test
    void createAndReadNewEntityFromCustomDataStore() throws Exception {
        Map<String, String> replacements = new HashMap<>();
        String json = getFileContent("createMem1Customer.json", replacements);

        UUID customerId;
        String url = baseUrl + "/entities/ref$Mem1Customer";

        Map<String, String> params = new HashMap<>();
        params.put("responseView", "mem1CustomerWithName");

        try (CloseableHttpResponse response = sendPost(url, oauthToken, json, params)) {
            assertEquals(HttpStatus.SC_CREATED, statusCode(response));
            Header[] locationHeaders = response.getHeaders("Location");
            assertEquals(1, locationHeaders.length);
            String location = locationHeaders[0].getValue();
            assertTrue(location.startsWith("http://localhost:" + port + "/rest/entities/ref$Mem1Customer"));
            String idString = location.substring(location.lastIndexOf("/") + 1);
            customerId = UUID.fromString(idString);

            ReadContext ctx = parseResponse(response);
            assertEquals("ref$Mem1Customer", ctx.read("$._entityName"));
            assertEquals(customerId.toString(), ctx.read("$.id"));
            assertEquals("Bob", ctx.read("$.name"));
        }

        url = baseUrl + "/entities/ref$Mem1Customer/" + customerId.toString();
        try (CloseableHttpResponse response = sendGet(url, oauthToken, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(customerId.toString(), ctx.read("$.id"));
            assertEquals("Bob", ctx.read("$._instanceName"));
            assertEquals("Bob", ctx.read("$.name"));
        }
    }


    @Test
    void updateCar() throws Exception {
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

    @Test
    @Disabled
    void updateCarWithExistingDynamicAttribute() throws Exception {
        Map<String, String> replacements = new HashMap<>();
        String dynamicAttributeValue = "ZZZ";
        replacements.put("$DYNAMIC_ATTRIBUTE_VALUE$", dynamicAttributeValue);
        String json = getFileContent("updateCarWithDynamicAttribute.json", replacements);

        String url = baseUrl + "/entities/ref_Car/" + carUuidString;
        try (CloseableHttpResponse response = sendPut(url, oauthToken, json, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(carUuidString, ctx.read("$.id"));
            //TODO Dynamic attribute
//            assertEquals(dynamicAttributeValue, ctx.read("$.+numberOfSeatsAttr"));
        }

        try (PreparedStatement stmt = conn.prepareStatement("select STRING_VALUE from SYS_ATTR_VALUE where ID = ?")) {
            stmt.setObject(1, numberOfSeatsCategoryAttrValueId);
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            String value = rs.getString("STRING_VALUE");
            assertEquals(dynamicAttributeValue, value);
        }
    }

    @Test
    @Disabled
    void updateCarWithNullDynamicAttribute() throws Exception {
        Map<String, String> replacements = new HashMap<>();
        String json = getFileContent("updateCarWithNullDynamicAttribute.json", replacements);

        String url = baseUrl + "/entities/ref_Car/" + carUuidString;
        try (CloseableHttpResponse response = sendPut(url, oauthToken, json, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(carUuidString, ctx.read("$.id"));
            //TODO Dynamic attribute
//            assertNull(ctx.read("$.+numberOfSeatsAttr"));
        }

        try (PreparedStatement stmt = conn.prepareStatement("select STRING_VALUE from SYS_ATTR_VALUE where ID = ?")) {
            stmt.setObject(1, numberOfSeatsCategoryAttrValueId);
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            String value = rs.getString("STRING_VALUE");
            assertNull(value);
        }
    }

    @Test
    @Disabled
    void updateCarWithNonExistingDynamicAttribute() throws Exception {
        Map<String, String> replacements = new HashMap<>();
        String dynamicAttributeValue = "ZZZ";
        replacements.put("$DYNAMIC_ATTRIBUTE_VALUE$", dynamicAttributeValue);
        String json = getFileContent("updateCarWithDynamicAttribute.json", replacements);

        String url = baseUrl + "/entities/ref_Car/" + secondCarUuidString;
        try (CloseableHttpResponse response = sendPut(url, oauthToken, json, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(secondCarUuidString, ctx.read("$.id"));
            //TODO Dynamic attribute
//            assertEquals(dynamicAttributeValue, ctx.read("$.+numberOfSeatsAttr"));
        }

        try (PreparedStatement stmt = conn.prepareStatement("select ID, STRING_VALUE from SYS_ATTR_VALUE where ENTITY_ID = ?")) {
            stmt.setObject(1, UUID.fromString(secondCarUuidString));
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            UUID attrValueId = (UUID.fromString(rs.getString("ID")));
            dirtyData.addCategoryAttributeValueId(attrValueId);
            String value = rs.getString("STRING_VALUE");
            assertEquals(dynamicAttributeValue, value);
        }
    }

    @Test
    void updateCarWithNullReference() throws Exception {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("$CAR_ID$", carUuidString);
        String json = getFileContent("updateCarWithNullReference.json", replacements);

        String url = baseUrl + "/entities/ref_Car/" + carUuidString;
        try (CloseableHttpResponse response = sendPut(url, oauthToken, json, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
        }

        try (PreparedStatement stmt = conn.prepareStatement("select VIN, MODEL_ID, COLOUR_ID from REF_CAR where ID = ?")) {
            stmt.setObject(1, UUID.fromString(carUuidString));
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            String vin = rs.getString("VIN");
            assertEquals("Modified vin", vin);
            Object modelId = rs.getObject("MODEL_ID");
            assertNull(modelId);
            Object colourId = rs.getObject("COLOUR_ID");
            assertEquals(colourUuidString, colourId);
        }
    }

    @Test
    void updateCarWithNullReferenceOneToOneComposition() throws Exception {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("$CAR_ID$", carUuidString);
        String json = getFileContent("updateCarWithNullReferenceOneToOneComposition.json", replacements);

        try (PreparedStatement stmt = conn.prepareStatement("select VIN, CAR_DOCUMENTATION_ID from REF_CAR where ID = ?")) {
            stmt.setObject(1, UUID.fromString(carUuidString));
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            Object carDocumentationId = rs.getObject("CAR_DOCUMENTATION_ID");
            assertEquals(carDocumentationUuidString, carDocumentationId.toString());
        }

        String url = baseUrl + "/entities/ref_Car/" + carUuidString;
        try (CloseableHttpResponse response = sendPut(url, oauthToken, json, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
        }

        try (PreparedStatement stmt = conn.prepareStatement("select VIN, CAR_DOCUMENTATION_ID from REF_CAR where ID = ?")) {
            stmt.setObject(1, UUID.fromString(carUuidString));
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            String vin = rs.getString("VIN");
            assertEquals("Modified vin", vin);
            Object carDocumentationId = rs.getObject("CAR_DOCUMENTATION_ID");
            assertNull(carDocumentationId);
        }

        try (PreparedStatement stmt = conn.prepareStatement("select DELETE_TS from REF_CAR_DOCUMENTATION where ID = ?")) {
            stmt.setObject(1, UUID.fromString(carDocumentationUuidString));
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            Date deleteTs = rs.getDate("DELETE_TS");
            assertNotNull(deleteTs);
        }
    }

    @Test
    void createCarWithTwoLevelComposition() throws Exception {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("$CAR_ID$", carUuidString);
        String json = getFileContent("createCarWithTwoLevelComposition.json", replacements);

        UUID carId;
        String url = baseUrl + "/entities/ref_Car";

        Map<String, String> params = new HashMap<>();
        params.put("responseView", "carWithTwoLevelComposition");

        try (CloseableHttpResponse response = sendPost(url, oauthToken, json, params)) {
            assertEquals(HttpStatus.SC_CREATED, statusCode(response));
            Header[] locationHeaders = response.getHeaders("Location");
            assertEquals(1, locationHeaders.length);
            String location = locationHeaders[0].getValue();
            assertTrue(location.startsWith("http://localhost:" + port + "/rest/entities/ref_Car"));
            String idString = location.substring(location.lastIndexOf("/") + 1);
            carId = UUID.fromString(idString);

            ReadContext ctx = parseResponse(response);
            assertEquals("ref_Car", ctx.read("$._entityName"));
            assertEquals(carId.toString(), ctx.read("$.id"));
            assertEquals(2, (int) ctx.read("$.repairs.length()"));
            assertNotNull(ctx.read("$.createTs"));
            assertNotNull(ctx.read("$.version"));

            //to delete the created objects in the @After method
            dirtyData.addCarId(carId);
            try (PreparedStatement stmt = conn.prepareStatement("select ID from REF_REPAIR where CAR_ID = ?")) {
                stmt.setObject(1, carId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    dirtyData.addRepairId(UUID.fromString(rs.getString("ID")));
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement("select t.ID from REF_CAR_TOKEN t join REF_REPAIR r on r.id = t.repair_id where r.CAR_ID = ?")) {
                stmt.setObject(1, carId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    dirtyData.addCarTokenId(UUID.fromString(rs.getString("ID")));
                }
            }
        }

        UUID repair2Id;
        try (PreparedStatement stmt = conn.prepareStatement("select ID, DESCRIPTION from REF_REPAIR where CAR_ID = ? order by DESCRIPTION")) {
            stmt.setObject(1, carId);
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            String description1 = rs.getString("DESCRIPTION");
            assertEquals("Repair 1", description1);
            assertTrue(rs.next());
            String description2 = rs.getString("DESCRIPTION");
            repair2Id = UUID.fromString(rs.getString("ID"));
            assertEquals("Repair 2", description2);
        }

        try (PreparedStatement stmt = conn.prepareStatement("select TOKEN from REF_CAR_TOKEN where REPAIR_ID = ?")) {
            stmt.setObject(1, repair2Id);
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            String token = rs.getString("TOKEN");
            assertEquals("Token 2", token);
        }
    }

    /**
     * Tests that items of composition collection may be both updated and removed
     */
    @Test
    void updateCarRepairs() throws Exception {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("$CAR_ID$", carUuidString);
        replacements.put("$REPAIR_ID$", repairUuidString);
        String repair1Descr = "New repair description";
        replacements.put("$REPAIR_1_DESCRIPTION$", repair1Descr);
        String json = getFileContent("updateCarWithRepairs.json", replacements);

        String url = baseUrl + "/entities/ref_Car/" + carUuidString;
        try (CloseableHttpResponse response = sendPut(url, oauthToken, json, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
        }

        try (PreparedStatement stmt = conn.prepareStatement("select DESCRIPTION from REF_REPAIR where CAR_ID = ? and DELETE_TS is null")) {
            stmt.setObject(1, UUID.fromString(carUuidString));
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            String description = rs.getString("DESCRIPTION");
            assertEquals(repair1Descr, description);
            assertFalse(rs.next());
        }
    }

    @Test
    void updateNonExistingCar() throws Exception {
        Map<String, String> replacements = new HashMap<>();
        String carId = UUID.randomUUID().toString();
        replacements.put("$CAR_ID$", carId);
        replacements.put("$MODEL_ID$", model2UuidString);
        String json = getFileContent("updateCar.json", replacements);

        String url = baseUrl + "/entities/ref_Car/" + carId;
        try (CloseableHttpResponse response = sendPut(url, oauthToken, json, null)) {
            assertEquals(HttpStatus.SC_NOT_FOUND, statusCode(response));
        }
    }

    @Test
    void updateEntityWithInvalidJson() throws Exception {
        Map<String, String> replacements = new HashMap<>();
        String json = getFileContent("invalidDriver.json", replacements);
        String url = baseUrl + "/entities/ref$Driver/" + driverUuidString;
        try (CloseableHttpResponse response = sendPut(url, oauthToken, json, null)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals("Cannot deserialize an entity from JSON", ctx.read("$.error"));
            assertEquals("", ctx.read("$.details"));
        }
    }

    /**
     * Tests update of embedded attribute. The "address.city' attribute is in the json.
     * 'address.country' should remain the same
     */
    @Test
    void updateDriverWithAddress() throws Exception {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("$DRIVER_ID$", driverUuidString);
        String json = getFileContent("driverWithAddress.json", replacements);

        String url = baseUrl + "/entities/ref$Driver/" + driverUuidString;
        try (CloseableHttpResponse response = sendPut(url, oauthToken, json, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
        }

        try (PreparedStatement stmt = conn.prepareStatement("select COUNTRY, CITY from REF_DRIVER where ID = ?")) {
            stmt.setObject(1, UUID.fromString(driverUuidString));
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            String country = rs.getString("COUNTRY");
            assertEquals("Russia", country);
            String city = rs.getString("CITY");
            assertEquals("Moscow", city);
        }
    }

    @Test
    void createEntityWithManyToManyAssociation() throws Exception {
        Map<String, String> replacements = new HashMap<>();
        String plantName = "Plant-3";
        replacements.put("$PLANT_NAME$", plantName);
        replacements.put("$MODEL_1_ID$", modelUuidString);
        String json = getFileContent("createEntityWithManyToManyAssociation.json", replacements);

        String url = baseUrl + "/entities/ref$Plant";
        String newPlantId = null;
        try (CloseableHttpResponse response = sendPost(url, oauthToken, json, null)) {
            assertEquals(HttpStatus.SC_CREATED, statusCode(response));
            ReadContext ctx = parseResponse(response);
            newPlantId = ctx.read("$.id");
            dirtyData.addPlantId(UUID.fromString(newPlantId));

        }

        try (PreparedStatement stmt = conn.prepareStatement("select NAME from REF_PLANT where ID = ?")) {
            stmt.setObject(1, UUID.fromString(newPlantId));
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            String plantNameFromDb = rs.getString("NAME");
            assertEquals(plantName, plantNameFromDb);
        }

        //model2 must be removed from the models collection
        try (PreparedStatement stmt = conn.prepareStatement("select MODEL_ID from REF_PLANT_MODEL_LINK where PLANT_ID = ?")) {
            stmt.setObject(1, UUID.fromString(newPlantId));
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            String modelId = rs.getString("MODEL_ID");
            assertEquals(this.modelUuidString, modelId);
            assertFalse(rs.next());
        }

//        //but model2 must remain in the database
//        try (PreparedStatement stmt = conn.prepareStatement("select DELETE_TS from REF_MODEL where ID = ?")) {
//            stmt.setObject(1, new PostgresUUID(UUID.fromString(model2UuidString)));
//            ResultSet rs = stmt.executeQuery();
//            assertTrue(rs.next());
//            Date deleteTs = rs.getDate("DELETE_TS");
//            assertNull(deleteTs);
//        }

    }


    @Test
    void updateManyToManyAssociationCheckRemoveAbsent() throws Exception {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("$PLANT_ID$", plantUuidString);
        replacements.put("$MODEL_1_ID$", modelUuidString);
        String json = getFileContent("updateManyToManyAssociation.json", replacements);

        String url = baseUrl + "/entities/ref$Plant/" + plantUuidString;
        try (CloseableHttpResponse response = sendPut(url, oauthToken, json, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
        }

        //model2 must be removed from the models collection
        try (PreparedStatement stmt = conn.prepareStatement("select MODEL_ID from REF_PLANT_MODEL_LINK where PLANT_ID = ?")) {
            stmt.setObject(1, UUID.fromString(plantUuidString));
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            String modelId = rs.getString("MODEL_ID");
            assertEquals(modelUuidString, modelId);
            assertFalse(rs.next());
        }

        //but model2 must remain in the database
        try (PreparedStatement stmt = conn.prepareStatement("select DELETE_TS from REF_MODEL where ID = ?")) {
            stmt.setObject(1, UUID.fromString(model2UuidString));
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            Date deleteTs = rs.getDate("DELETE_TS");
            assertNull(deleteTs);
        }

    }

    @Test
    void updateManyToManyAssociationErrorOnMissing() throws Exception {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("$PLANT_ID$", plantUuidString);
        replacements.put("$MODEL_1_ID$", modelUuidString);
        replacements.put("$MODEL_2_ID$", UUID.randomUUID().toString());
        String json = getFileContent("updateManyToManyAssociationErrorOnMissing.json", replacements);

        String url = baseUrl + "/entities/ref$Plant/" + plantUuidString;
        try (CloseableHttpResponse response = sendPut(url, oauthToken, json, null)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode(response));
        }
    }

    @Test
    void updateBaseDbGeneratedIdEntity() throws Exception {
        Map<String, String> replacements = new HashMap<>();
        String json = getFileContent("createIdentityCustomer.json", replacements);

        String url = baseUrl + "/entities/ref$IdentityCustomer";
        Long customerId;
        try (CloseableHttpResponse response = sendPost(url, oauthToken, json, null)) {
            assertEquals(HttpStatus.SC_CREATED, statusCode(response));
            ReadContext ctx = parseResponse(response);
            customerId = Long.valueOf(ctx.read("$.id"));
            assertNotNull(customerId);
        }

        try (PreparedStatement stmt = conn.prepareStatement("select ID from REF_IK_CUSTOMER where NAME = ?")) {
            stmt.setString(1, "Bob");
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            customerId = rs.getLong("ID");
            assertNotNull(customerId);
        }

        url = baseUrl + "/entities/ref$IdentityCustomer/" + customerId;
        json = getFileContent("updateBaseDbGeneratedIdEntity.json", replacements);
        Map<String, String> params = new HashMap<>();
        params.put("responseView", "identityCustomerWithName");

        try (CloseableHttpResponse response = sendPut(url, oauthToken, json, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals("John", ctx.read("$.name"));
            assertEquals(customerId, Long.valueOf(ctx.read("$.id")));
        }

        try (PreparedStatement stmt = conn.prepareStatement("select ID, NAME from REF_IK_CUSTOMER where ID = ?")) {
            stmt.setObject(1, customerId);
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            String name = rs.getString("NAME");
            assertEquals("John", name);
        }
    }


    @Test
    void deleteCar() throws Exception {
        String url = baseUrl + "/entities/ref_Car/" + carUuidString;
        try (CloseableHttpResponse response = sendDelete(url, oauthToken, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
        }

        try (PreparedStatement stmt = conn.prepareStatement("select DELETE_TS from REF_CAR where ID = ?")) {
            stmt.setObject(1, UUID.fromString(carUuidString));
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            assertNotNull(rs.getTimestamp("DELETE_TS"));
        }
    }

    @Test
    void deleteNonExistingCar() throws Exception {
        String url = baseUrl + "/entities/ref_Car/" + UUID.randomUUID();
        try (CloseableHttpResponse response = sendDelete(url, oauthToken, null)) {
            assertEquals(HttpStatus.SC_NOT_FOUND, statusCode(response));
        }
    }

    @Test
    void loadEntityByIdWithTransform() throws Exception {
        String url = baseUrl + "/entities/ref$OldCar/" + carUuidString;
        Map<String, String> params = new HashMap<>();
        params.put("modelVersion", "1.0");
        params.put("view", "carBrowse");

        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(carUuidString, ctx.read("$.id"));
            assertEquals("VWV000", ctx.read("$._instanceName"));
            assertEquals("ref$OldCar", ctx.read("$._entityName"));
            assertEquals("VWV000", ctx.read("$.oldVin"));
            assertNotNull(ctx.read("$.model"));

            //vin must be renamed
            try {
                ctx.read("$.vin");
                fail();
            } catch (PathNotFoundException ignored) {
            }

            //colour must be removed
            try {
                ctx.read("$.colour");
                fail();
            } catch (PathNotFoundException ignored) {
            }
        }
    }

    @Test
    void loadEntitiesListWithTransform() throws Exception {
        String url = baseUrl + "/entities/ref$OldCar";
        Map<String, String> params = new HashMap<>();
        params.put("modelVersion", "1.0");
        params.put("view", "carBrowse");

        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);

            Map<String, Object> carFields = (Map<String, Object>) ctx.read("$[?(@.id=='" + carUuidString + "')]", List.class).get(0);
            assertEquals(carUuidString, carFields.get("id"));
            assertEquals("VWV000", carFields.get("_instanceName"));
            assertEquals("ref$OldCar", carFields.get("_entityName"));
            assertEquals("VWV000", carFields.get("oldVin"));

            Map<String, Object> modelFields = (Map<String, Object>) carFields.get("model");
            assertEquals("ref$OldModel", modelFields.get("_entityName"));
            assertEquals(modelName, modelFields.get("oldName"));

            //vin must be renamed
            assertNull(carFields.get("vin"));

            //colour must be removed
            assertNull(carFields.get("colour"));
        }
    }

    @Test
    void createNewEntityWithTransform() throws Exception {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("$MODEL_ID$", modelUuidString);
        String json = getFileContent("oldCar.json", replacements);

        UUID carId;
        String url = baseUrl + "/entities/ref$OldCar";

        Map<String, String> params = new HashMap<>();
        params.put("modelVersion", "1.0");
        params.put("responseView", "carWithTransform");

        try (CloseableHttpResponse response = sendPost(url, oauthToken, json, params)) {
            assertEquals(HttpStatus.SC_CREATED, statusCode(response));
            Header[] locationHeaders = response.getHeaders("Location");
            assertEquals(1, locationHeaders.length);
            String location = locationHeaders[0].getValue();
            assertTrue(location.startsWith("http://localhost:" + port + "/rest/entities/ref$OldCar"));
            String idString = location.substring(location.lastIndexOf("/") + 1);
            carId = UUID.fromString(idString);

            ReadContext ctx = parseResponse(response);
            assertEquals("ref$OldCar", ctx.read("$._entityName"));
            assertEquals(carId.toString(), ctx.read("$.id"));
            assertEquals("123", ctx.read("$.oldVin"));
            assertEquals(2, (int) ctx.read("$.repairs.length()"));
            assertNotNull(ctx.read("$.createTs"));
            assertNotNull(ctx.read("$.version"));
            assertNotNull(ctx.read("$.model"));

            //to delete the created objects in the @After method
            dirtyData.addCarId(carId);
            try (PreparedStatement stmt = conn.prepareStatement("select ID from REF_REPAIR where CAR_ID = ?")) {
                stmt.setObject(1, carId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    dirtyData.addRepairId((UUID.fromString(rs.getString("ID"))));
                }
            }
        }

        try (PreparedStatement stmt = conn.prepareStatement("select VIN, MODEL_ID from REF_CAR where ID = ?")) {
            stmt.setObject(1, carId);
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            String vin = rs.getString("VIN");
            assertEquals("123", vin);
            Object modelId = rs.getObject("MODEL_ID");
            assertEquals(modelUuidString, modelId);
        }

        try (PreparedStatement stmt = conn.prepareStatement("select DESCRIPTION, REPAIR_DATE from REF_REPAIR where CAR_ID = ? order by DESCRIPTION")) {
            stmt.setObject(1, carId);
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            String description1 = rs.getString("DESCRIPTION");
            assertEquals("Repair 1", description1);
            Date date1 = rs.getDate("REPAIR_DATE");
            assertEquals("2016-06-08", sdf.format(date1));
            assertTrue(rs.next());
            String description2 = rs.getString("DESCRIPTION");
            assertEquals("Repair 2", description2);
            Date date2 = rs.getDate("REPAIR_DATE");
            assertEquals("2016-06-20", sdf.format(date2));
        }
    }

    @Test
    void updateCarWithTransform() throws Exception {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("$CAR_ID$", carUuidString);
        replacements.put("$MODEL_ID$", model2UuidString);
        String json = getFileContent("updateOldCar.json", replacements);

        Map<String, String> params = new HashMap<>();
        params.put("modelVersion", "1.0");
        params.put("responseView", "carWithTransform");

        String url = baseUrl + "/entities/ref$OldCar/" + carUuidString;
        try (CloseableHttpResponse response = sendPut(url, oauthToken, json, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));

            ReadContext ctx = parseResponse(response);
            assertEquals("ref$OldCar", ctx.read("$._entityName"));
            assertEquals(carUuidString, ctx.read("$.id"));
            assertEquals("Modified vin", ctx.read("$.oldVin"));
            assertEquals(model2UuidString, ctx.read("$.model.id"));
            assertNotNull(ctx.read("$.updateTs"));
            assertNotNull(ctx.read("$.version"));
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

    @Test
    void deleteCarWithTransform() throws Exception {
        String url = baseUrl + "/entities/ref$OldCar/" + carUuidString;
        Map<String, String> params = new HashMap<>();
        params.put("modelVersion", "1.0");

        try (CloseableHttpResponse response = sendDelete(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
        }

        try (PreparedStatement stmt = conn.prepareStatement("select DELETE_TS from REF_CAR where ID = ?")) {
            stmt.setObject(1, UUID.fromString(carUuidString));
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            assertNotNull(rs.getTimestamp("DELETE_TS"));
        }
    }

    @Test
    void loadEntityByIdWithCustomTransform() throws Exception {
        String url = baseUrl + "/entities/ref$Repair/" + repairUuidString;
        Map<String, String> params = new HashMap<>();
        params.put("modelVersion", "1.0");

        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(repairUuidString, ctx.read("$.id"));
            assertEquals("ref$OldRepair", ctx.read("$._entityName"));
            assertEquals("2012-01-13 00:00:00.000", ctx.read("$.date"));

            //name must be renamed
            try {
                ctx.read("$.name");
                fail();
            } catch (PathNotFoundException ignored) {
            }
        }
    }

    @Test
    void createNewEntityWithCustomTransform() throws Exception {
        Map<String, String> replacements = new HashMap<>();
        String json = getFileContent("oldRepair.json", replacements);

        UUID repairId;
        String url = baseUrl + "/entities/ref$OldRepair";

        Map<String, String> params = new HashMap<>();
        params.put("modelVersion", "1.0");
        params.put("responseView", "repairWithDescription");

        try (CloseableHttpResponse response = sendPost(url, oauthToken, json, params)) {
            assertEquals(HttpStatus.SC_CREATED, statusCode(response));
            Header[] locationHeaders = response.getHeaders("Location");
            assertEquals(1, locationHeaders.length);
            String location = locationHeaders[0].getValue();
            assertTrue(location.startsWith("http://localhost:" + port + "/rest/entities/ref$OldRepair"));
            String idString = location.substring(location.lastIndexOf("/") + 1);
            repairId = UUID.fromString(idString);

            ReadContext ctx = parseResponse(response);
            assertEquals("ref$OldRepair", ctx.read("$._entityName"));
            assertEquals(repairId.toString(), ctx.read("$.id"));
            assertEquals("Repair description", ctx.read("$.description"));
            assertEquals("2017-01-01 00:00:00.000", ctx.read("$.date"));
        }

        dirtyData.addRepairId(repairId);

        try (PreparedStatement stmt = conn.prepareStatement("select DESCRIPTION from REF_REPAIR where ID = ?")) {
            stmt.setObject(1, repairId);
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            String description = rs.getString("DESCRIPTION");
            assertEquals("Repair description", description);
        }
    }

    private void executePrepared(String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            stmt.executeUpdate();
        }
    }

    public void prepareDb() throws Exception {
        UUID colourId = dirtyData.createColourUuid();
        colourUuidString = colourId.toString();
        executePrepared("insert into ref_colour(id, name, description, version) values (?, ?, ?, 1)",
                colourId,
                "Colour 1",
                "Description 1");

        UUID modelId = dirtyData.createModelUuid();
        modelUuidString = modelId.toString();
        executePrepared("insert into ref_model(id, name, number_of_seats, DTYPE, version) values (?, ?, ?, ?, 1)",
                modelId, modelName, modelNumberOfSeats, "ref$ExtModel");

        UUID model2Id = dirtyData.createModelUuid();
        model2UuidString = model2Id.toString();
        executePrepared("insert into ref_model(id, name, number_of_seats, DTYPE, version) values (?, ?, ?, ?, 1)",
                model2Id, model2Name, modelNumberOfSeats, "ref$ExtModel");

        UUID model3Id = dirtyData.createModelUuid();
        model3UuidString = model3Id.toString();
        executePrepared("insert into ref_model(id, name, number_of_seats, DTYPE, version) values (?, ?, ?, ?, 1)",
                model3Id, "model3", modelNumberOfSeats, "ref$ExtModel");

        UUID carDocumentationUuid = dirtyData.createCarDocumentationUuid();
        carDocumentationUuidString = carDocumentationUuid.toString();
        executePrepared("insert into ref_car_documentation(id, version, title, create_ts) values(?, ?, ?, ?)",
                carDocumentationUuid,
                1L,
                "Car Doc 1",
                Timestamp.valueOf("2016-06-17 10:53:01")
        );

        UUID carUuid = dirtyData.createCarUuid();
        carUuidString = carUuid.toString();
        executePrepared("insert into ref_car(id, version, vin, colour_id, model_id, car_documentation_id, create_ts) values(?, ?, ?, ?, ?, ?, ?)",
                carUuid,
                1L,
                "VWV000",
                colourId,
                modelId,
                carDocumentationUuid,
                Timestamp.valueOf("2016-06-17 10:53:01")
        );

        UUID secondCarUuid = dirtyData.createCarUuid();
        secondCarUuidString = secondCarUuid.toString();
        executePrepared("insert into ref_car(id, version, vin, colour_id) values(?, ?, ?, ?)",
                secondCarUuid,
                1L,
                "VWV002",
                colourId
        );

        UUID repairId = dirtyData.createRepairUuid();
        repairUuidString = repairId.toString();
        executePrepared("insert into ref_repair(id, car_id, repair_date, version) values (?, ?, ?, 1)",
                repairId,
                carUuid,
                Date.valueOf("2012-01-13"));

        UUID repair2Id = dirtyData.createRepairUuid();
        repair2UuidString = repair2Id.toString();
        executePrepared("insert into ref_repair(id, car_id, repair_date, version) values (?, ?, ?, 1)",
                repair2Id,
                carUuid,
                Date.valueOf("2012-01-14"));

        for (int i = 2; i < 6; i++) {
            UUID colourUuid = dirtyData.createColourUuid();
            executePrepared("insert into ref_colour(id, name, description, version) values (?, ?, ?, 1)",
                    colourUuid,
                    "Colour " + i,
                    "Description " + (i < 4 ? "1" : "2"));
        }

        UUID driverId = dirtyData.createDriverUuid();
        driverUuidString = driverId.toString();
        executePrepared("insert into ref_driver(id, name, country, city, notes, dtype, version) values (?, ?, ?, ?, ?, ?, 1)",
                driverId,
                "John",
                "Russia",
                "Samara",
                "The notes",
                "ref$ExtDriver"
        );

        UUID debtorId = dirtyData.createDebtorUuid();
        debtorUuidString = debtorId.toString();
        executePrepared("insert into debt_debtor(id, title, version) values (?, ?, 1)",
                debtorId,
                "debtor1"
        );

        UUID plantId = dirtyData.createPlantUuid();
        plantUuidString = plantId.toString();
        executePrepared("insert into REF_PLANT(id, name, version) values (?, ?, 1)",
                plantId,
                "plant1"
        );

        executePrepared("insert into REF_PLANT_MODEL_LINK(plant_id, model_id) values (?, ?)",
                plantId,
                modelId
        );

        executePrepared("insert into REF_PLANT_MODEL_LINK(plant_id, model_id) values (?, ?)",
                plantId,
                model2Id
        );

        executePrepared("insert into REF_IK_CUSTOMER(name) values (?)",
                "Bob"
        );

        UUID carCategoryId = dirtyData.createCategoryId();
//        executePrepared("insert into sys_category (id, name, entity_type, discriminator, version) values (?, ?, ?, 0, 1)",
//                carCategoryId,
//                "carCategory",
//                "ref_Car"
//        );

        UUID seatsNumberCategoryAttrId = dirtyData.createCategoryAttributeId();
//        executePrepared("insert into sys_category_attr (id, name, code, category_entity_type, category_id, data_type, " +
//                        "is_collection, version) values (?,?,?, ?, ?, ?,false, 1)",
//                seatsNumberCategoryAttrId,
//                "numberOfSeats",
//                "numberOfSeatsAttr",
//                "ref_Car",
//                carCategoryId,
//                "STRING"
//        );

        numberOfSeatsCategoryAttrValueId = dirtyData.createCategoryAttributeValueId();
//        executePrepared("insert into sys_attr_value (id, category_attr_id, code, entity_id, string_value, version) values (?, ?, ?, ?, ?, 1)",
//                numberOfSeatsCategoryAttrValueId,
//                seatsNumberCategoryAttrId,
//                "numberOfSeatsAttr",
//                carUuid,
//                "10"
//        );
    }
}
