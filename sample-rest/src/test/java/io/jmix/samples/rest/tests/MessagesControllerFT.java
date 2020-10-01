/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package io.jmix.samples.rest.tests;

import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static io.jmix.samples.rest.tools.RestTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 */
public class MessagesControllerFT extends AbstractRestControllerFT {

    static {
        Locale.setDefault(Locale.ENGLISH);
    }

    @Test
    public void testLocalizationForEntity() throws Exception {
        String url = baseUrl + "/messages/entities/sec$User";
        try (CloseableHttpResponse response = sendGet(url, oauthToken, null)) {
            ReadContext ctx = parseResponse(response);
            assertEquals("User", ctx.read("$['sec$User']"));
            assertEquals("Login", ctx.read("$['sec$User.login']"));
            assertThrows(PathNotFoundException.class, () -> ctx.read("$['sec$Role']"));
        }
    }

    //todo locale
    @Disabled
    @Test
    public void testLocalizationForEntityUsingRuLanguage() throws Exception {
        String url = baseUrl + "/messages/entities/sec$User";
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.ACCEPT_LANGUAGE, "ru");
        try (CloseableHttpResponse response = sendGetWithHeaders(url, oauthToken, null, headers)) {
            ReadContext ctx = parseResponse(response);
            assertEquals("Пользователь", ctx.read("$['sec$User']"));
            assertEquals("Логин", ctx.read("$['sec$User.login']"));

            assertThrows(PathNotFoundException.class, () -> ctx.read("$['sec$Role']"));
        }
    }

    @Test
    public void testLocalizationForEntityWhenNoAcceptLanguageSpecified() throws Exception {
        String url = baseUrl + "/messages/entities/sec$User";
        Map<String, String> headers = new HashMap<>();
        try (CloseableHttpResponse response = sendGetWithHeaders(url, oauthToken, null, headers)) {
            ReadContext ctx = parseResponse(response);
            assertEquals("User", ctx.read("$['sec$User']"));
            assertEquals("Login", ctx.read("$['sec$User.login']"));
        }
    }

    //todo locale
    @Disabled
    @Test
    public void testLocalizationUsingLocaleFromLogin() throws Exception {
        String url = baseUrl + "/messages/entities/sec$User";

        String ruToken = getAuthToken(baseUrl, "admin", "admin123", Collections.singletonMap("Accept-Language", "ru"));
        try (CloseableHttpResponse response = sendGetWithHeaders(url, ruToken, null, Collections.emptyMap())) {
            ReadContext ctx = parseResponse(response);
            assertEquals("Пользователь", ctx.read("$['sec$User']"));
            assertEquals("Логин", ctx.read("$['sec$User.login']"));
        }

        String enToken = getAuthToken(baseUrl, "admin", "admin123", Collections.singletonMap("Accept-Language", "en"));
        try (CloseableHttpResponse response = sendGetWithHeaders(url, enToken, null, Collections.emptyMap())) {
            ReadContext ctx = parseResponse(response);
            assertEquals("User", ctx.read("$['sec$User']"));
            assertEquals("Login", ctx.read("$['sec$User.login']"));
        }
    }

    @Test
    public void testLocalizationForAllEntities() throws Exception {
        String url = baseUrl + "/messages/entities";
        try (CloseableHttpResponse response = sendGet(url, oauthToken, null)) {
            ReadContext ctx = parseResponse(response);
            assertEquals("User", ctx.read("$['sec$User']"));
            assertEquals("Login", ctx.read("$['sec$User.login']"));
            assertEquals("Role", ctx.read("$['sec$Role']"));
        }
    }

    @Test
    public void testLocalizationForEnum() throws Exception {
        String url = baseUrl + "/messages/enums/io.jmix.samples.rest.entity.sec.RoleType";
        try (CloseableHttpResponse response = sendGet(url, oauthToken, null)) {
            ReadContext ctx = parseResponse(response);
            assertEquals("Role Type", ctx.read("$['io.jmix.samples.rest.entity.sec.RoleType']"));
            assertEquals("Standard", ctx.read("$['io.jmix.samples.rest.entity.sec.RoleType.STANDARD']"));
            assertThrows(PathNotFoundException.class, () -> ctx.read("$['io.jmix.core.security.ConstraintOperationType.CREATE']"));
        }
    }

    @Test
    public void testLocalizationForAllEnums() throws Exception {
        String url = baseUrl + "/messages/enums";
        try (CloseableHttpResponse response = sendGet(url, oauthToken, null)) {
            ReadContext ctx = parseResponse(response);
            assertEquals("Role Type", ctx.read("$['io.jmix.samples.rest.entity.sec.RoleType']"));
            assertEquals("Standard", ctx.read("$['io.jmix.samples.rest.entity.sec.RoleType.STANDARD']"));
//            assertEquals("Create", ctx.read("$['io.jmix.core.security.ConstraintOperationType.CREATE']"));
        }
    }
}
