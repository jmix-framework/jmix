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

package test_support;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;

import java.io.IOException;

import static io.restassured.RestAssured.given;

public class RestSpecsUtils {

    static {
        RestAssured.basePath = "/rest";
    }

    public static void setReportsBasePath() {
        RestAssured.basePath = "/app/rest/reports/v1";
    }

    public static void clearBasePath() {
        RestAssured.basePath = "/rest";
    }

    public static void setBasePort(int port) {
        RestAssured.port = port;
    }

    public static RequestSpecification createRequest(String authToken) {
        return given().header("Authorization", "Bearer " + authToken);
    }

    public static RequestSpecification createRequest() {
        return given();
    }

    public static String getAuthToken(String url, String login, String password) throws IOException {
        return RestTestUtils.getAuthToken(url, login, password);
    }
}
