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

package anonymous_access;

import io.jmix.samples.rest.service.RestTestService;
import test_support.AbstractRestControllerFT;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static test_support.RestTestUtils.statusCode;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AnonymousDisabledForServiceAndQueryAccessFT extends AbstractRestControllerFT {

    protected Map<String, String> serviceParams = new HashMap<String, String>() {{
        put("number1", "2");
        put("number2", "3");
    }};

    @Test
    public void executeServiceMethodWithAnonymousEnabled() throws Exception {
        String url = baseUrl + "/services/" + RestTestService.NAME + "/sum";
        try (CloseableHttpResponse response = sendGet(url, serviceParams)) {
            assertEquals(HttpStatus.SC_UNAUTHORIZED, statusCode(response));
        }
    }

    @Test
    public void executeQueryWithAnonymousEnabled() throws Exception {
        String url = baseUrl + "/queries/sec$User/currentUser";
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

    @Test
    public void loadEntitiesWithoutPermissionAnonymous() throws Exception {
        String url = baseUrl + "/entities/sec$Group";
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
