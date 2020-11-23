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

package io.jmix.samples.rest.tests;

import com.jayway.jsonpath.ReadContext;
import io.jmix.core.security.CoreUser;
import io.jmix.samples.rest.JmixRestTestAnonymousConfiguration;
import io.jmix.samples.rest.security.AnonymousAccessRole;
import io.jmix.samples.rest.service.app.RestTestService;
import io.jmix.security.authentication.RoleGrantedAuthority;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import javax.annotation.Nullable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import static io.jmix.samples.rest.tools.RestTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestPropertySource(properties = {"jmix.rest.anonymousUrlPatterns=/rest/services/" + RestTestService.NAME + "/sum," +
        "/rest/queries/sec$User/currentUser"})
@ContextConfiguration(classes = {
        JmixRestTestAnonymousConfiguration.class})
public class AnonymousServiceAndQueryAccessFT extends AbstractRestControllerFT {

    protected Map<String, String> serviceParams = new HashMap<String, String>() {{
        put("number1", "2");
        put("number2", "3");
    }};

    @Test
    public void executeServiceMethodWithAnonymousAllowed() throws Exception {
        String url = baseUrl + "/services/" + RestTestService.NAME + "/sum";
        try (CloseableHttpResponse response = sendGet(url, serviceParams)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            assertEquals("5", responseToString(response));
        }
    }

    @Test
    public void executeServiceWithoutAnonymousAllowed() throws Exception {
        String url = baseUrl + "/services/" + RestTestService.NAME + "/emptyMethod";
        try (CloseableHttpResponse response = sendGet(url, null)) {
            assertEquals(HttpStatus.SC_UNAUTHORIZED, statusCode(response));
        }
    }

    @Test
    public void loadEntitiesWithoutPermissionAnonymous() throws Exception {
        String url = baseUrl + "/entities/sec$Group";
        try (CloseableHttpResponse response = sendGet(url, null)) {
            assertEquals(HttpStatus.SC_UNAUTHORIZED, statusCode(response));
        }
    }

    @Test
    public void executeQueryWithAnonymousAllowed() throws Exception {
        String url = baseUrl + "/queries/sec$User/currentUser";
        try (CloseableHttpResponse response = sendGet(url, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            assertEquals("application/json;charset=utf-8", responseContentType(response).toLowerCase());
            ReadContext ctx = parseResponse(response);
            assertEquals(1, ctx.<Collection>read("$").size());
            assertEquals("anonymous", ctx.read("$.[0].login"));
        }
    }

    @Test
    public void executeQueryWithoutAnonymousAllowed() throws Exception {
        String url = baseUrl + "/queries/sec$User/userByLogin";
        try (CloseableHttpResponse response = sendGet(url, null)) {
            assertEquals(HttpStatus.SC_UNAUTHORIZED, statusCode(response));
        }
    }

    @Test
    public void loadEntitiesWithPermissionAnonymous() throws Exception {
        String url = baseUrl + "/entities/sec$User";
        try (CloseableHttpResponse response = sendGet(url, null)) {
            assertEquals(HttpStatus.SC_UNAUTHORIZED, statusCode(response));
        }
    }

    public static CloseableHttpResponse sendGet(String url, @Nullable Map<String, String> params) throws Exception {
        URIBuilder uriBuilder = new URIBuilder(url);
        if (params != null) {
            params.forEach(uriBuilder::addParameter);
        }
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(uriBuilder.build());
        httpGet.setHeader("Accept-Language", "en");
        return httpClient.execute(httpGet);
    }

    @Override
    public void prepareDb() throws Exception {
        UUID companyGroupId = dirtyData.createGroupUuid();
        executePrepared("insert into sample_rest_sec_group(id, version, name) " +
                        "values(?, ?, ?)",
                companyGroupId,
                1l,
                "Company"
        );

        String adminLogin = "admin";
        UUID adminId = dirtyData.createUserUuid();
        executePrepared("insert into sample_rest_sec_user(id, version, login, group_id, login_lc) " +
                        "values(?, ?, ?, ?, ?)",
                adminId,
                1l,
                adminLogin,
                companyGroupId, //"Company" group
                adminLogin.toLowerCase()
        );

        String anonymousLogin = "anonymous";
        UUID anonymousId = dirtyData.createUserUuid();
        executePrepared("insert into sample_rest_sec_user(id, version, login, group_id, login_lc) " +
                        "values(?, ?, ?, ?, ?)",
                anonymousId,
                1l,
                anonymousLogin,
                companyGroupId, //"Company" group
                anonymousLogin.toLowerCase()
        );
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
