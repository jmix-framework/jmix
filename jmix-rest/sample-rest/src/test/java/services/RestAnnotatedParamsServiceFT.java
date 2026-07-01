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

package services;

import io.jmix.samples.rest.service.RestAnnotatedParamsTestService;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.jupiter.api.Test;
import test_support.AbstractRestControllerFT;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static test_support.RestTestUtils.responseToString;
import static test_support.RestTestUtils.sendGet;
import static test_support.RestTestUtils.sendPost;
import static test_support.RestTestUtils.statusCode;

public class RestAnnotatedParamsServiceFT extends AbstractRestControllerFT {

    @Test
    public void invokeGetWithExplicitRestParamNames() throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("first", "2");
        params.put("second", "3");

        try (CloseableHttpResponse response = sendGet(baseUrl + "/services/" + RestAnnotatedParamsTestService.NAME + "/sum",
                oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            assertEquals("5", responseToString(response));
        }
    }

    @Test
    public void invokePostWithExplicitRestParamNamesInAnyOrder() throws Exception {
        String body = "{\"second\":\"3\",\"first\":\"2\"}";
        try (CloseableHttpResponse response = sendPost(baseUrl + "/services/" + RestAnnotatedParamsTestService.NAME + "/sum",
                oauthToken, body, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            assertEquals("5", responseToString(response));
        }
    }

    @Test
    public void invokeMethodWithoutParams() throws Exception {
        try (CloseableHttpResponse response = sendGet(
                baseUrl + "/services/" + RestAnnotatedParamsTestService.NAME + "/noParamsMethod",
                oauthToken, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            assertEquals("ok", responseToString(response));
        }
    }

    @Test
    public void missingRequiredParamStillFails() throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("first", "2");

        try (CloseableHttpResponse response = sendGet(baseUrl + "/services/" + RestAnnotatedParamsTestService.NAME + "/sum",
                oauthToken, params)) {
            assertEquals(HttpStatus.SC_NOT_FOUND, statusCode(response));
            assertTrue(responseToString(response).contains("Service method not found"));
        }
    }

    @Test
    public void methodsWithInvalidRestParamAnnotationsAreNotRegistered() throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("dup", "2");

        try (CloseableHttpResponse duplicateResponse = sendGet(
                baseUrl + "/services/" + RestAnnotatedParamsTestService.NAME + "/methodWithDuplicateParams",
                oauthToken, params)) {
            assertEquals(HttpStatus.SC_NOT_FOUND, statusCode(duplicateResponse));
            assertTrue(responseToString(duplicateResponse).contains("Service method not found"));
        }

        try (CloseableHttpResponse blankResponse = sendGet(
                baseUrl + "/services/" + RestAnnotatedParamsTestService.NAME + "/methodWithBlankParam",
                oauthToken, params)) {
            assertEquals(HttpStatus.SC_NOT_FOUND, statusCode(blankResponse));
            assertTrue(responseToString(blankResponse).contains("Service method not found"));
        }
    }
}
