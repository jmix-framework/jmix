/*
 * Copyright (c) 2008-2018 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package io.jmix.samples.rest;

import io.jmix.samples.rest.tools.RestTestUtils;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;

import java.io.IOException;

import static io.restassured.RestAssured.given;

public class RestSpecsUtils {

    static {
        RestAssured.basePath = "/rest/v2";
    }

    public static void setReportsBasePath() {
        RestAssured.basePath = "/app/rest/reports/v1";
    }

    public static void clearBasePath() {
        RestAssured.basePath = "/app/rest/v2";
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
