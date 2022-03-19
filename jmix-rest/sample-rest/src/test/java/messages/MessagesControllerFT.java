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

package messages;

import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import test_support.AbstractRestControllerFT;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static test_support.RestTestUtils.*;
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

    @Test
    public void testLocalizationUsingLocaleFromLogin() throws Exception {
        String url = baseUrl + "/messages/entities/sec$User";

        String ruToken = getAuthToken(oauthUrl, "admin", "admin123", Collections.singletonMap("Accept-Language", "ru"));
        try (CloseableHttpResponse response = sendGetWithHeaders(url, ruToken, null, Collections.emptyMap())) {
            ReadContext ctx = parseResponse(response);
            assertEquals("Пользователь", ctx.read("$['sec$User']"));
            assertEquals("Логин", ctx.read("$['sec$User.login']"));
        }

        String enToken = getAuthToken(oauthUrl, "admin", "admin123", Collections.singletonMap("Accept-Language", "en"));
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
        }
    }
}
