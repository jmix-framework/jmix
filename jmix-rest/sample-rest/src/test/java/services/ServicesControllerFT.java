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

package services;

import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import io.jmix.samples.rest.service.RestTestService;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import test_support.AbstractRestControllerFT;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static test_support.RestTestUtils.*;

public class ServicesControllerFT extends AbstractRestControllerFT {

    private String carUuidString;
    private String secondCarUuidString;
    private String colourUuidString;

    @Test
    public void serviceWithNoParamsPOST() throws Exception {
        try (CloseableHttpResponse response = sendPost(baseUrl + "/services/" + RestTestService.NAME + "/emptyMethod", oauthToken, "", null)) {
            assertEquals(HttpStatus.SC_NO_CONTENT, statusCode(response));
            assertNull(response.getEntity());
        }
    }

    @Test
    public void serviceWithNoParamsGET() throws Exception {
        try (CloseableHttpResponse response = sendGet(baseUrl + "/services/" + RestTestService.NAME + "/emptyMethod", oauthToken, null)) {
            assertEquals(HttpStatus.SC_NO_CONTENT, statusCode(response));
            assertNull(response.getEntity());
        }
    }

    @Test
    public void serviceWithParamsPOST() throws Exception {
        String requestBody = getFileContent("serviceWithParams.json", null);
        try (CloseableHttpResponse response = sendPost(baseUrl + "/services/" + RestTestService.NAME + "/sum", oauthToken, requestBody, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            assertEquals("5", responseToString(response));
        }
    }

    @Test
    public void serviceWithParamsGET() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("number1", "2");
        params.put("number2", "3");
        try (CloseableHttpResponse response = sendGet(baseUrl + "/services/" + RestTestService.NAME + "/sum", oauthToken, params)) {
            assertEquals("text/plain;charset=UTF-8", responseContentType(response));
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            assertEquals("5", responseToString(response));
        }
    }

    @Test
    public void serviceWithOptionalParamsGET() throws Exception {
        //with 1 required
        Map<String, String> params = new LinkedHashMap<>();
        params.put("arg1", "1");
        try (CloseableHttpResponse response = sendGet(baseUrl + "/services/" + RestTestService.NAME + "/methodWithOptionalArgs", oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext readContext = parseResponse(response);
            assertEquals("1", readContext.read("$.arg1"));
            assertThrows(PathNotFoundException.class, () -> readContext.read("$.arg2"));
            assertThrows(PathNotFoundException.class, () -> readContext.read("$.arg3"));
        }

        //with 1 required and 1 optional
        params.clear();
        params.put("arg1", "1");
        params.put("arg2", "2");
        try (CloseableHttpResponse response = sendGet(baseUrl + "/services/" + RestTestService.NAME + "/methodWithOptionalArgs", oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext readContext = parseResponse(response);
            assertEquals("1", readContext.read("$.arg1"));
            assertEquals("2", readContext.read("$.arg2"));
            assertThrows(PathNotFoundException.class, () -> readContext.read("$.arg3"));
        }

        //with 1 required and 1 optional
        params.clear();
        params.put("arg1", "1");
        params.put("arg3", "3");
        try (CloseableHttpResponse response = sendGet(baseUrl + "/services/" + RestTestService.NAME + "/methodWithOptionalArgs", oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext readContext = parseResponse(response);
            assertEquals("1", readContext.read("$.arg1"));
            assertThrows(PathNotFoundException.class, () -> readContext.read("$.arg2"));
            assertEquals("3", readContext.read("$.arg3"));
        }

        //with 1 required and 2 optional
        params.clear();
        params.put("arg1", "1");
        params.put("arg2", "2");
        params.put("arg3", "3");
        try (CloseableHttpResponse response = sendGet(baseUrl + "/services/" + RestTestService.NAME + "/methodWithOptionalArgs", oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext readContext = parseResponse(response);
            assertEquals("1", readContext.read("$.arg1"));
            assertEquals("2", readContext.read("$.arg2"));
            assertEquals("3", readContext.read("$.arg3"));
        }

        //without params
        params.clear();
        try (CloseableHttpResponse response = sendGet(baseUrl + "/services/" + RestTestService.NAME + "/methodWithOptionalArgs", oauthToken, params)) {
            assertEquals(HttpStatus.SC_NOT_FOUND, statusCode(response));
            assertTrue(responseToString(response).contains("Service method not found"));
        }

        //1 suitable method
        params.put("arg1", "1");
        params.put("arg2", "2");
        try (CloseableHttpResponse response = sendGet(baseUrl + "/services/" + RestTestService.NAME + "/overloadedMethodWithOptionalArgs", oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            assertEquals("two args", responseToString(response));
        }

        //more than 1 suitable method
        params.clear();
        params.put("arg1", "1");
        try (CloseableHttpResponse response = sendGet(baseUrl + "/services/" + RestTestService.NAME + "/overloadedMethodWithOptionalArgs", oauthToken, params)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode(response));
            assertTrue(responseToString(response).contains("Cannot determine the service method to call"));
        }
    }

    @Test
    public void serviceWithJavaTimeParams() throws Exception {
        String requestBody = getFileContent("serviceWithJavaTimeParams.json", null);
        try (CloseableHttpResponse response = sendPost(baseUrl + "/services/" + RestTestService.NAME + "/testJavaTimeParam", oauthToken, requestBody, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            assertEquals("2021-02-24,2021-02-24T15:15:15.053,15:15:15,2021-02-24T15:15:15.053+04:00,15:15:15+04:00,15:15:15", responseToString(response));
        }
    }

    @Test
    public void serviceWithDateParameter() throws Exception {
        String requestBody = getFileContent("serviceWithDateParameter.json", null);
        try (CloseableHttpResponse response = sendPost(baseUrl + "/services/" + RestTestService.NAME + "/testDateParam", oauthToken, requestBody, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            assertEquals("2015-01-02T00:00:00", responseToString(response));
        }
    }

    @Test
    public void serviceWithDateParameterGET() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("param", "2015-01-02");
        try (CloseableHttpResponse response = sendGet(baseUrl + "/services/" + RestTestService.NAME + "/testDateParam", oauthToken, params)) {
            assertEquals("text/plain;charset=UTF-8", responseContentType(response));
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            assertEquals("2015-01-02T00:00:00", responseToString(response));
        }
    }

    @Test
    public void serviceWithDateTimeParameterPOST() throws Exception {
        String requestBody = getFileContent("serviceWithDateTimeParameter.json", null);
        try (CloseableHttpResponse response = sendPost(baseUrl + "/services/" + RestTestService.NAME + "/testDateParam", oauthToken, requestBody, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            assertEquals("2015-01-02T01:02:03.004", responseToString(response));
        }
    }

    @Test
    public void serviceWithNullParameterPOST() throws Exception {
        String requestBody = getFileContent("serviceWithNullParameter.json", null);
        try (CloseableHttpResponse response = sendPost(baseUrl + "/services/" + RestTestService.NAME + "/testDateParam", oauthToken, requestBody, null)) {
            assertEquals(HttpStatus.SC_NO_CONTENT, statusCode(response));
        }
    }

    @Test
    public void serviceWithBigDecimalParameterPOST() throws Exception {
        String requestBody = getFileContent("serviceWithBigDecimalParameter.json", null);
        try (CloseableHttpResponse response = sendPost(baseUrl + "/services/" + RestTestService.NAME + "/testBigDecimalParam", oauthToken, requestBody, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            assertEquals("1.234", responseToString(response));
        }
    }

    @Test
    public void serviceThatReturnsEntityPOST() throws Exception {
        Map<String, String> replacements = new LinkedHashMap<>();
        replacements.put("$CAR_ID$", carUuidString);
        String requestBody = getFileContent("serviceThatReturnsEntity.json", replacements);
        try (CloseableHttpResponse response = sendPost(baseUrl + "/services/" + RestTestService.NAME + "/findCar", oauthToken, requestBody, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            assertEquals("application/json;charset=UTF-8", responseContentType(response));
            ReadContext readContext = parseResponse(response);
            assertEquals(carUuidString, readContext.read("$.id"));
            assertEquals("ref_Car", readContext.read("$._entityName"));
        }
    }

    @Test
    public void serviceThatReturnsEntityGET() throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("carId", carUuidString);
        params.put("viewName", "carEdit");
        try (CloseableHttpResponse response = sendGet(baseUrl + "/services/" + RestTestService.NAME + "/findCar", oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            assertEquals("application/json;charset=UTF-8", responseContentType(response));
            ReadContext readContext = parseResponse(response);
            assertEquals(carUuidString, readContext.read("$.id"));
            assertEquals("ref_Car", readContext.read("$._entityName"));
        }
    }

    @Test
    public void serviceThatReturnsEntityWithInverseParamtersOrderGET() throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("viewName", "carEdit");
        params.put("carId", carUuidString);
        try (CloseableHttpResponse response = sendGet(baseUrl + "/services/" + RestTestService.NAME + "/findCar", oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            assertEquals("application/json;charset=UTF-8", responseContentType(response));
            ReadContext readContext = parseResponse(response);
            assertEquals(carUuidString, readContext.read("$.id"));
            assertEquals("ref_Car", readContext.read("$._entityName"));
        }
    }

    @Test
    public void serviceThatReturnsEntityWithInverseParametersOrderPOST() throws Exception {
        Map<String, String> replacements = new LinkedHashMap<>();
        replacements.put("$CAR_ID$", carUuidString);
        String requestBody = getFileContent("serviceThatReturnsEntityWithInverseParametersOrder.json", replacements);
        try (CloseableHttpResponse response = sendPost(baseUrl + "/services/" + RestTestService.NAME + "/findCar", oauthToken, requestBody, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            assertEquals("application/json;charset=UTF-8", responseContentType(response));
            ReadContext readContext = parseResponse(response);
            assertEquals(carUuidString, readContext.read("$.id"));
            assertEquals("ref_Car", readContext.read("$._entityName"));
        }
    }

    @Test
    public void serviceThatReturnsEntityWithTransformGET() throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("carId", carUuidString);
        params.put("viewName", "carEdit");
        params.put("modelVersion", "1.0");
        try (CloseableHttpResponse response = sendGet(baseUrl + "/services/" + RestTestService.NAME + "/findCar", oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            assertEquals("application/json;charset=UTF-8", responseContentType(response));
            ReadContext readContext = parseResponse(response);
            assertEquals(carUuidString, readContext.read("$.id"));
            assertEquals("ref$OldCar", readContext.read("$._entityName"));
            assertNotNull(readContext.read("$.oldVin"));
            try {
                readContext.read("$.vin");
                fail();
            } catch (PathNotFoundException ignored) {
            }
        }
    }

    @Test
    public void serviceThatReturnsEntitiesListPOST() throws Exception {
        String requestBody = getFileContent("serviceThatReturnsEntitiesList.json", null);
        try (CloseableHttpResponse response = sendPost(baseUrl + "/services/" + RestTestService.NAME + "/findAllCars", oauthToken, requestBody, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext readContext = parseResponse(response);
            assertEquals(2, readContext.<Collection>read("$").size());
            assertEquals(1, readContext.read("$.[?(@.id == '" + carUuidString + "')]", Collection.class).size());
            assertEquals("ref_Car", readContext.read("$.[0]._entityName"));
        }
    }

    @Test
    public void serviceThatReturnsEntitiesListWithTransformPOST() throws Exception {
        String requestBody = getFileContent("serviceThatReturnsEntitiesList.json", null);
        try (CloseableHttpResponse response = sendPost(baseUrl + "/services/" + RestTestService.NAME + "/findAllCars?modelVersion=1.0", oauthToken, requestBody, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext readContext = parseResponse(response);
            assertEquals(2, readContext.<Collection>read("$").size());
            assertEquals(1, readContext.read("$.[?(@.id == '" + carUuidString + "')]", Collection.class).size());
            assertEquals("ref$OldCar", readContext.read("$.[0]._entityName"));
            assertNotNull(readContext.read("$.[0].oldVin"));
            try {
                readContext.read("$.[0].vin");
                fail();
            } catch (PathNotFoundException ignored) {
            }
        }
    }

    @Test
    public void serviceThatReturnsEntitiesListGET() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("viewName", "carBrowse");
        try (CloseableHttpResponse response = sendGet(baseUrl + "/services/" + RestTestService.NAME + "/findAllCars", oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext readContext = parseResponse(response);
            assertEquals(2, readContext.<Collection>read("$").size());
            assertEquals(1, readContext.read("$.[?(@.id == '" + carUuidString + "')]", Collection.class).size());
            assertEquals("ref_Car", readContext.read("$.[0]._entityName"));
        }
    }

    @Test
    public void serviceThatReturnsEntitiesListWithTransformGET() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("viewName", "carBrowse");
        params.put("modelVersion", "1.0");
        try (CloseableHttpResponse response = sendGet(baseUrl + "/services/" + RestTestService.NAME + "/findAllCars", oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext readContext = parseResponse(response);
            assertEquals(2, readContext.<Collection>read("$").size());
            assertEquals(1, readContext.read("$.[?(@.id == '" + carUuidString + "')]", Collection.class).size());
            assertEquals("ref$OldCar", readContext.read("$.[0]._entityName"));
            assertNotNull(readContext.read("$.[0].oldVin"));
            try {
                readContext.read("$.[0].vin");
                fail();
            } catch (PathNotFoundException ignored) {
            }
        }
    }

    @Test
    public void serviceWithEntityParamPOST() throws Exception {
        Map<String, String> replacements = new HashMap<>();
        String carVin = "VF0001";
        String carNewVin = "NEW_VIN";
        String colourName = "New colour";
        String driverAllocationId1 = UUID.randomUUID().toString();
        String repairId1 = UUID.randomUUID().toString();
        String repairId2 = UUID.randomUUID().toString();
        replacements.put("$CAR_ID$", carUuidString);
        replacements.put("$CAR_VIN$", carVin);
        replacements.put("$CAR_NEW_VIN$", carNewVin);
        replacements.put("$COLOUR_ID$", colourUuidString);
        replacements.put("$COLOUR_NAME$", colourName);
        replacements.put("$DRIVER_ALLOCATION_ID_1$", driverAllocationId1);
        replacements.put("$REPAIR_ID_1$", repairId1);
        replacements.put("$REPAIR_ID_2$", repairId2);

        String requestBody = getFileContent("serviceWithEntityParam.json", replacements);
        try (CloseableHttpResponse response = sendPost(baseUrl + "/services/" + RestTestService.NAME + "/updateCarVin", oauthToken, requestBody, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext readContext = parseResponse(response);
            assertEquals(carUuidString, readContext.read("$.id"));
            assertEquals("ref_Car", readContext.read("$._entityName"));
            assertEquals(carNewVin, readContext.read("$.vin"));
        }
    }

    @Test
    public void serviceWithInvalidEntityParamPOST() throws Exception {
        Map<String, String> replacements = new HashMap<>();
        String carVin = "VF0001";
        replacements.put("$CAR_ID$", carUuidString);
        replacements.put("$CAR_VIN$", carVin);

        String requestBody = getFileContent("serviceWithInvalidEntityParam.json", replacements);
        try (CloseableHttpResponse response = sendPost(baseUrl + "/services/" + RestTestService.NAME + "/updateCarVin", oauthToken, requestBody, null)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode(response));
            ReadContext readContext = parseResponse(response);
            assertEquals("Invalid parameter value", readContext.read("$.error"));
            assertEquals("Invalid parameter value for car", readContext.read("$.details"));
        }
    }

    @Test
    public void serviceWithNullParamPOST() throws Exception {
        Map<String, String> replacements = new HashMap<>();
        String carVin = "VF0001";
        replacements.put("$CAR_ID$", carUuidString);
        replacements.put("$CAR_VIN$", carVin);

        String requestBody = getFileContent("serviceWithNullParam.json", replacements);
        try (CloseableHttpResponse response = sendPost(baseUrl + "/services/" + RestTestService.NAME + "/testNullParam", oauthToken, requestBody, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            assertEquals("true", EntityUtils.toString(response.getEntity()));
        }
    }

    @Test
    public void serviceWithEntitiesCollectionParam() throws Exception {
        Map<String, String> replacements = new HashMap<>();
        String carVin1 = "VF0001";
        String carVin2 = "VF0002";
        String carNewVin = "NEW_VIN";
        String colourName = "New colour";
        String driverAllocationId1 = UUID.randomUUID().toString();
        String driverAllocationId2 = UUID.randomUUID().toString();
        String repairId1 = UUID.randomUUID().toString();
        String repairId2 = UUID.randomUUID().toString();
        replacements.put("$CAR_ID_1$", carUuidString);
        replacements.put("$CAR_VIN_1$", carVin1);
        replacements.put("$CAR_ID_2$", secondCarUuidString);
        replacements.put("$CAR_VIN_2$", carVin2);
        replacements.put("$CAR_NEW_VIN$", carNewVin);
        replacements.put("$COLOUR_ID$", colourUuidString);
        replacements.put("$COLOUR_NAME$", colourName);
        replacements.put("$DRIVER_ALLOCATION_ID_1$", driverAllocationId1);
        replacements.put("$DRIVER_ALLOCATION_ID_2$", driverAllocationId2);
        replacements.put("$REPAIR_ID_1$", repairId1);
        replacements.put("$REPAIR_ID_2$", repairId2);

        String requestBody = getFileContent("serviceWithEntitiesCollectionParam.json", replacements);
        try (CloseableHttpResponse response = sendPost(baseUrl + "/services/" + RestTestService.NAME + "/updateCarVins", oauthToken, requestBody, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext readContext = parseResponse(response);
            assertEquals(2, readContext.<Collection>read("$").size());
            assertEquals(carUuidString, readContext.read("$.[0].id"));
            assertEquals("ref_Car", readContext.read("$.[0]._entityName"));
            assertEquals(carNewVin, readContext.read("$.[0].vin"));
        }
    }

    @Test
    public void serviceWithEntitiesCollectionParamWithTransform() throws Exception {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("$CAR_ID_1$", carUuidString);
        replacements.put("$CAR_VIN_1$", "vin1");
        replacements.put("$CAR_ID_2$", secondCarUuidString);
        replacements.put("$CAR_VIN_2$", "vin2");

        String requestBody = getFileContent("serviceWithEntitiesCollectionParamWithTransform.json", replacements);
        try (CloseableHttpResponse response = sendPost(baseUrl + "/services/" + RestTestService.NAME + "/concatCarVins?modelVersion=1.0", oauthToken, requestBody, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            assertEquals("vin1vin2", EntityUtils.toString(response.getEntity()));
        }
    }

    /**
     * Collection parameter in JSON may not contain _entityName attribute for each item. Entity type must be recognized automatically
     *
     * @throws Exception
     */
    @Test
    public void serviceWithEntitiesCollectionParamWithoutEntityName() throws Exception {
        Map<String, String> replacements = new HashMap<>();
        String carVin1 = "VF0001";
        String carVin2 = "VF0002";
        String carNewVin = "NEW_VIN";
        String colourName = "New colour";
        String driverAllocationId1 = UUID.randomUUID().toString();
        String driverAllocationId2 = UUID.randomUUID().toString();
        String repairId1 = UUID.randomUUID().toString();
        String repairId2 = UUID.randomUUID().toString();
        replacements.put("$CAR_ID_1$", carUuidString);
        replacements.put("$CAR_VIN_1$", carVin1);
        replacements.put("$CAR_ID_2$", secondCarUuidString);
        replacements.put("$CAR_VIN_2$", carVin2);
        replacements.put("$CAR_NEW_VIN$", carNewVin);
        replacements.put("$COLOUR_ID$", colourUuidString);
        replacements.put("$COLOUR_NAME$", colourName);
        replacements.put("$DRIVER_ALLOCATION_ID_1$", driverAllocationId1);
        replacements.put("$DRIVER_ALLOCATION_ID_2$", driverAllocationId2);
        replacements.put("$REPAIR_ID_1$", repairId1);
        replacements.put("$REPAIR_ID_2$", repairId2);

        String requestBody = getFileContent("serviceWithEntitiesCollectionParamWithoutEntityName.json", replacements);
        try (CloseableHttpResponse response = sendPost(baseUrl + "/services/" + RestTestService.NAME + "/updateCarVins", oauthToken, requestBody, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext readContext = parseResponse(response);
            assertEquals(2, readContext.<Collection>read("$").size());
            assertEquals(carUuidString, readContext.read("$.[0].id"));
            assertEquals("ref_Car", readContext.read("$.[0]._entityName"));
            assertEquals(carNewVin, readContext.read("$.[0].vin"));
        }
    }

    @Test
    public void serviceWithOverloadedMethodPOST() throws Exception {
        String requestBody = getFileContent("serviceWithOverloadedMethodIntParam.json", null);
        try (CloseableHttpResponse response = sendPost(baseUrl + "/services/" + RestTestService.NAME + "/overloadedMethod", oauthToken, requestBody, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext readContext = parseResponse(response);
            assertEquals("int", readContext.read("$"));
        }
    }

    @Test
    public void serviceWithOverloadedMethodGET() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("stringParam", "2");
        try (CloseableHttpResponse response = sendGet(baseUrl + "/services/" + RestTestService.NAME + "/overloadedMethod", oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext readContext = parseResponse(response);
            assertEquals("String", readContext.read("$"));
        }
    }

    @Test
    public void serviceThatReturnsPojo() throws Exception {
        try (CloseableHttpResponse response = sendPost(baseUrl + "/services/" + RestTestService.NAME + "/getPojo", oauthToken, "", null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals("field1 value", ctx.read("$.field1"));
            assertEquals("2017-01-15T17:56:00", ctx.read("$.dateField"));
            assertEquals((Integer) 2, ctx.read("$.nestedPojo.nestedField", Integer.class));
        }
    }

    @Test
    public void serviceThatReturnsPojoList() throws Exception {
        try (CloseableHttpResponse response = sendPost(baseUrl + "/services/" + RestTestService.NAME + "/getPojoList", oauthToken, "", null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(2, ctx.<Collection>read("$").size());
            assertEquals("pojo1", ctx.read("$.[0].field1"));
            assertEquals("2017-01-15T17:56:00", ctx.read("$.[0].dateField"));
            assertEquals((int) 1, (int) ctx.<Integer>read("$.[0].nestedPojo.nestedField"));
            assertEquals("pojo2", ctx.read("$.[1].field1"));
        }
    }

    @Test
    public void serviceThatReturnsPojoWithNestedEntitiesList() throws Exception {
        try (CloseableHttpResponse response = sendPost(baseUrl + "/services/" + RestTestService.NAME + "/getPojosWithNestedEntity", oauthToken, "", null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(2, ctx.<Collection>read("$").size());
            assertEquals("VWV000", ctx.read("$.[0].car.vin"));
            assertEquals(1, (int) ctx.read("$.[0].intField"));
            assertEquals("VWV002", ctx.read("$.[1].car.vin"));
            assertEquals(2, (int) ctx.read("$.[1].intField"));
        }
    }

    @Test
    public void serviceThatReturnsPojoWithNestedEntityWithView() throws Exception {
        try (CloseableHttpResponse response = sendPost(baseUrl + "/services/" + RestTestService.NAME + "/getPojosWithNestedEntityWithView", oauthToken, "", null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(2, ctx.<Collection>read("$").size());
            assertEquals("VWV000", ctx.read("$.[0].car.vin"));
            assertEquals(1, (int) ctx.read("$.[0].intField"));
            assertEquals("Red", ctx.read("$.[0].car.colour.name"));
            assertEquals("VWV002", ctx.read("$.[1].car.vin"));
            assertEquals(2, (int) ctx.read("$.[1].intField"));
        }
    }

    @Test
    public void serviceWithPojoParameterPOST() throws Exception {
        String paramsJson = getFileContent("serviceWithPojoParameter.json", null);
        try (CloseableHttpResponse response = sendPost(baseUrl + "/services/" + RestTestService.NAME + "/methodWithPojoParameter",
                oauthToken, paramsJson, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals((Integer) 2, ctx.read("$", Integer.class));
        }
    }

    @Test
    public void serviceWithPojoCollectionParameterPOST() throws Exception {
        String paramsJson = getFileContent("serviceWithPojoCollectionParameter.json", null);
        try (CloseableHttpResponse response = sendPost(baseUrl + "/services/" + RestTestService.NAME + "/methodWithPojoCollectionParameter",
                oauthToken, paramsJson, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals((Integer) 2, ctx.read("$", Integer.class));
        }
    }

    /**
     * Test that a service method parameter (collection of POJOs) is serialized correctly
     */
    @Test
    public void serviceWithPojoCollectionParameter2POST() throws Exception {
        String paramsJson = getFileContent("serviceWithPojoCollectionParameter.json", null);
        try (CloseableHttpResponse response = sendPost(baseUrl + "/services/" + RestTestService.NAME + "/methodWithPojoCollectionParameter2",
                oauthToken, paramsJson, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals("Field1 value", ctx.read("$"));
        }
    }

    @Test
    public void serviceWithPrimitiveListsParam() throws Exception {
        Map<String, String> replacements = new HashMap<>();

        String requestBody = getFileContent("methodWithPrimitiveListArguments.json", replacements);
        try (CloseableHttpResponse response = sendPost(baseUrl + "/services/" + RestTestService.NAME + "/methodWithPrimitiveListArguments", oauthToken, requestBody, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext readContext = parseResponse(response);
            assertEquals(2, readContext.<Collection>read("$").size());
        }
    }

    @Test
    public void methodWithEmptyStringListResult() throws Exception {
        Map<String, String> replacements = new HashMap<>();

        String requestBody = getFileContent("methodWithEmptyStringListResult.json", replacements);
        try (CloseableHttpResponse response = sendPost(baseUrl + "/services/" + RestTestService.NAME + "/methodWithPrimitiveListArguments", oauthToken, requestBody, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext readContext = parseResponse(response);
            assertEquals(0, readContext.<Collection>read("$").size());
        }
    }

    @Test
    public void nonExistingMethodPOST() throws Exception {
        try (CloseableHttpResponse response = sendPost(baseUrl + "/services/" + RestTestService.NAME + "/nonExistingMethod", oauthToken, "", null)) {
            assertEquals(HttpStatus.SC_NOT_FOUND, statusCode(response));
            ReadContext readContext = parseResponse(response);
            assertEquals("Service method not found", readContext.read("$.error"));
        }
    }

    @Test
    public void nonExistingMethodGET() throws Exception {
        try (CloseableHttpResponse response = sendGet(baseUrl + "/services/" + RestTestService.NAME + "/nonExistingMethod", oauthToken, null)) {
            assertEquals(HttpStatus.SC_NOT_FOUND, statusCode(response));
            ReadContext readContext = parseResponse(response);
            assertEquals("Service method not found", readContext.read("$.error"));
        }
    }

    @Test
    public void notPermittedMethodPOST() throws Exception {
        try (CloseableHttpResponse response = sendPost(baseUrl + "/services/" + RestTestService.NAME + "/notPermittedMethod", oauthToken, "", null)) {
            assertEquals(HttpStatus.SC_NOT_FOUND, statusCode(response));
            ReadContext readContext = parseResponse(response);
            assertEquals("Service method not found", readContext.read("$.error"));
        }
    }

    @Test
    public void notPermittedMethodGET() throws Exception {
        try (CloseableHttpResponse response = sendGet(baseUrl + "/services/" + RestTestService.NAME + "/notPermittedMethod", oauthToken, null)) {
            assertEquals(HttpStatus.SC_NOT_FOUND, statusCode(response));
            ReadContext readContext = parseResponse(response);
            assertEquals("Service method not found", readContext.read("$.error"));
        }
    }

    @Test
    public void invalidParamNameGET() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("invalidParamName", "zzz");
        try (CloseableHttpResponse response = sendGet(baseUrl + "/services/" + RestTestService.NAME + "/findAllCars", oauthToken, params)) {
            assertEquals(HttpStatus.SC_NOT_FOUND, statusCode(response));
            ReadContext readContext = parseResponse(response);
            assertEquals("Service method not found", readContext.read("$.error"));
        }
    }

    @Test
    public void invalidParamValueGET() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("param", "String that is not a date");
        try (CloseableHttpResponse response = sendGet(baseUrl + "/services/" + RestTestService.NAME + "/testDateParam", oauthToken, params)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode(response));
            ReadContext readContext = parseResponse(response);
            assertEquals("Invalid parameter value", readContext.read("$.error"));
            assertEquals("Invalid parameter value for param", readContext.read("$.details"));
        }
    }

    @Test
    public void invalidParamValuePOST() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("param", "String that is not a date");
        String requestBody = getFileContent("serviceWithInvalidDateParam.json", null);
        try (CloseableHttpResponse response = sendPost(baseUrl + "/services/" + RestTestService.NAME + "/testDateParam", oauthToken, requestBody, params)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode(response));
            ReadContext readContext = parseResponse(response);
            assertEquals("Invalid parameter value", readContext.read("$.error"));
            assertEquals("Invalid parameter value for param", readContext.read("$.details"));
        }
    }

    @Test
    public void getServiceInfos() throws Exception {
        try (CloseableHttpResponse response = sendGet(baseUrl + "/services", oauthToken, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext readContext = parseResponse(response);

            String[] serviceNames = {"jmix_OtherRestTestService", "jmix_RestTestService",
                    "jmix_RestTestServiceWithoutInterfaces"};

            assertEquals(serviceNames.length, readContext.<Collection>read("$").size());

            HashSet<String> servicesSet = new HashSet<>();
            Collections.addAll(servicesSet, serviceNames);

            for (int i = 0; i < serviceNames.length; i++) {
                String serviceName = readContext.read("$.[" + i + "].name", String.class);
                assertTrue(servicesSet.contains(serviceName));
            }
        }
    }

    @Test
    public void getServiceInfo() throws Exception {
        try (CloseableHttpResponse response = sendGet(baseUrl + "/services/jmix_RestTestService", oauthToken, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext readContext = parseResponse(response);
            assertEquals("jmix_RestTestService", readContext.read("$.name"));
            assertEquals(32, readContext.<Collection>read("$.methods").size());
            assertEquals(2, readContext.read("$.methods[?(@.name == 'sum')].params.length()", List.class).get(0));

            assertEquals("number1", readContext.read("$.methods[?(@.name == 'sum')].params[0].name", List.class).get(0));
            assertEquals(null, readContext.read("$.methods[?(@.name == 'sum')].params[0].type", List.class).get(0));

            assertEquals("java.lang.String", readContext.read("$.methods[?(@.name == 'overloadedMethod')].params[?(@.name == 'stringParam')].type", List.class).get(0));
        }
    }

    @Test
    public void serviceThatReturnsNotPersistedEntityGET() throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        try (CloseableHttpResponse response = sendGet(baseUrl + "/services/" + RestTestService.NAME + "/notPersistedEntity", oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            assertEquals("application/json;charset=UTF-8", responseContentType(response));
            ReadContext readContext = parseResponse(response);
            assertEquals("model 1", readContext.read("$.title"));
            assertEquals("rest_ModelEntity", readContext.read("$._entityName"));
            assertEquals("model 1", readContext.read("$._instanceName"));
            assertNotNull(readContext.read("$.id"));
        }
    }

    @Test
    public void serviceThatReturnsNotPersistedStringIdEntityGET() throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        try (CloseableHttpResponse response = sendGet(baseUrl + "/services/" + RestTestService.NAME + "/getNotPersistentStringIdEntity", oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            assertEquals("application/json;charset=UTF-8", responseContentType(response));
            ReadContext readContext = parseResponse(response);
            assertEquals("jmix$NotPersistentStringIdEntity", readContext.read("$._entityName"));
            assertEquals("1", readContext.read("$.id"));
            assertEquals("Bob", readContext.read("$.name"));
        }
    }

    @Test
    public void getServiceInfoForNonExistingService() throws Exception {
        try (CloseableHttpResponse response = sendGet(baseUrl + "/services/nonExistingService", oauthToken, null)) {
            assertEquals(HttpStatus.SC_NOT_FOUND, statusCode(response));
            ReadContext readContext = parseResponse(response);
            assertEquals("Service not found", readContext.read("$.error"));
        }
    }

    @Test
    public void methodWithListOfMapParamPOST() throws Exception {
        String paramsJson = getFileContent("methodWithListOfMapParam.json", null);
        try (CloseableHttpResponse response = sendPost(baseUrl + "/services/" + RestTestService.NAME + "/methodWithListOfMapParam",
                oauthToken, paramsJson, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals((Integer) 4, ctx.read("$", Integer.class));
        }
    }

    @Test
    public void methodReturnsListOfMapGET() throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        try (CloseableHttpResponse response = sendGet(baseUrl + "/services/" + RestTestService.NAME + "/methodReturnsListOfMap", oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            assertEquals("application/json;charset=UTF-8", responseContentType(response));
            ReadContext readContext = parseResponse(response);
            assertEquals(2, readContext.<Collection>read("$").size());
            assertEquals((Integer) 1, readContext.read("$.[0].key1", Integer.class));
            assertEquals((Integer) 2, readContext.read("$.[1].key2", Integer.class));
        }
    }

    public void prepareDb() throws SQLException {
        UUID colourId = dirtyData.createColourUuid();
        colourUuidString = colourId.toString();
        executePrepared("insert into ref_colour(id, version, name) values (?, ?, ?)",
                colourId,
                1L,
                "Red");

        UUID carUuid = dirtyData.createCarUuid();
        carUuidString = carUuid.toString();
        executePrepared("insert into ref_car(id, version, vin, colour_id) values(?, ?, ?, ?)",
                carUuid,
                1L,
                "VWV000",
                colourId
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
        executePrepared("insert into ref_repair(id, car_id, repair_date, version) values (?, ?, ?, ?)",
                repairId,
                carUuid,
                java.sql.Date.valueOf("2012-01-13"),
                1L
        );

        dirtyData.createRepairUuid();
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
