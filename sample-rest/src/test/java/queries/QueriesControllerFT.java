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

package queries;

import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import test_support.AbstractRestControllerFT;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static test_support.RestTestUtils.*;

/**
 *
 */
public class QueriesControllerFT extends AbstractRestControllerFT {

    private List<UUID> coloursUuids = new ArrayList<>();
    private UUID adminId;

    @Override
    public void prepareDb() throws Exception {
        for (int i = 1; i < 10; i++) {
            UUID colourId = dirtyData.createColourUuid();
            coloursUuids.add(colourId);
            executePrepared("insert into ref_colour(id, name, version) values (?, ?, 1)",
                    colourId,
                    "Colour " + i);
        }

        UUID companyGroupId = dirtyData.createGroupUuid();
        executePrepared("insert into sample_rest_sec_group(id, version, name) " +
                        "values(?, ?, ?)",
                companyGroupId,
                1l,
                "Company"
        );

        String bobLogin = "bob";
        UUID bobId = dirtyData.createUserUuid();
        executePrepared("insert into sample_rest_sec_user(id, version, login, group_id, login_lc) " +
                        "values(?, ?, ?, ?, ?)",
                bobId,
                1l,
                bobLogin,
                companyGroupId, //"Company" group
                bobLogin.toLowerCase()
        );

        String johnLogin = "john";
        UUID johnId = dirtyData.createUserUuid();
        executePrepared("insert into sample_rest_sec_user(id, version, login, group_id, login_lc) " +
                        "values(?, ?, ?, ?, ?)",
                johnId,
                1l,
                johnLogin,
                companyGroupId, //"Company" group
                johnLogin.toLowerCase()
        );

        String adminLogin = "admin";
        adminId = dirtyData.createUserUuid();
        executePrepared("insert into sample_rest_sec_user(id, version, login, group_id, login_lc) " +
                        "values(?, ?, ?, ?, ?)",
                adminId,
                1l,
                adminLogin,
                companyGroupId, //"Company" group
                adminLogin.toLowerCase()
        );

        UUID modelId = dirtyData.createModelUuid();
        executePrepared("insert into ref_model(id, name, version, dtype) values (?, ?, 1, 'ref$ExtModel')",
                modelId,
                "Audi TT");

        UUID carId = dirtyData.createCarUuid();
        executePrepared("insert into ref_car(id, vin, model_id, version) values (?, ?, ?, 1)",
                carId,
                "001",
                modelId);
    }

    @Test
    public void executeQuery() throws Exception {
        String url = baseUrl + "/queries/sec$User/userByLogin";
        Map<String, String> params = new HashMap<>();
        params.put("login", "bob");
        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            assertEquals(MediaType.APPLICATION_JSON_UTF8_VALUE, responseContentType(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(1, ctx.<Collection>read("$").size());
            assertEquals("Company", ctx.read("$.[0].group.name"));

            Header[] totalCountHeaders = response.getHeaders("x-total-count");
            assertEquals(0, totalCountHeaders.length);

            //check that nulls aren't serialized by default
            assertThrows(PathNotFoundException.class, () -> ctx.read("&.[0].middleName"));

        }
    }

    @Test
    public void executeQueryWithSessionParameter() throws Exception {
        String url = baseUrl + "/queries/sec$User/currentUser";
        Map<String, String> params = new HashMap<>();
        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            assertEquals(MediaType.APPLICATION_JSON_UTF8_VALUE, responseContentType(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(1, ctx.<Collection>read("$").size());
            assertEquals("admin", ctx.read("$.[0].login"));
            assertEquals(adminId.toString(), ctx.read("$.[0].id"));
        }
    }

    @Test
    public void executeQueryWithStringCollectionParameter() throws Exception {
        String url = baseUrl + "/queries/ref$Colour/coloursByNames";
        Map<String, String> params = new HashMap<>();
        params.put("returnCount", "true");
        String json = getFileContent("coloursByNames.json", null);
        try (CloseableHttpResponse response = sendPost(url, oauthToken, json, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            assertEquals(MediaType.APPLICATION_JSON_UTF8_VALUE, responseContentType(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(2, ctx.<Collection>read("$").size());
            assertEquals("Colour 1", ctx.read("$.[0].name"));
            assertEquals("Colour 2", ctx.read("$.[1].name"));

            Header[] totalCountHeaders = response.getHeaders("X-Total-Count");
            assertEquals("2", totalCountHeaders[0].getValue());
        }
    }

    @Test
    public void executeQueryWithUuidCollectionParameter() throws Exception {
        String url = baseUrl + "/queries/ref$Colour/coloursByIds";
        Map<String, String> params = new HashMap<>();
        params.put("returnCount", "true");
        Map<String, String> replacements = new HashMap<>();
        replacements.put("$ID0$", coloursUuids.get(0).toString());
        replacements.put("$ID1$", coloursUuids.get(1).toString());
        String json = getFileContent("coloursByIds.json", replacements);
        try (CloseableHttpResponse response = sendPost(url, oauthToken, json, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            assertEquals(MediaType.APPLICATION_JSON_UTF8_VALUE, responseContentType(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(2, ctx.<Collection>read("$").size());
            assertEquals(coloursUuids.get(0).toString(), ctx.read("$.[0].id"));
            assertEquals("Colour 1", ctx.read("$.[0].name"));
            assertEquals(coloursUuids.get(1).toString(), ctx.read("$.[1].id"));
            assertEquals("Colour 2", ctx.read("$.[1].name"));

            Header[] totalCountHeaders = response.getHeaders("X-Total-Count");
            assertEquals("2", totalCountHeaders[0].getValue());
        }
    }

    @Test
    public void executeQueryWithCountHeader() throws Exception {
        String url = baseUrl + "/queries/sec$User/userByLogin";
        Map<String, String> params = new HashMap<>();
        params.put("login", "bob");
        params.put("returnCount", "true");
        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            assertEquals(MediaType.APPLICATION_JSON_UTF8_VALUE, responseContentType(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(1, ctx.<Collection>read("$").size());

            Header[] totalCountHeaders = response.getHeaders("X-Total-Count");
            assertEquals(1, totalCountHeaders.length);

            assertEquals("1", totalCountHeaders[0].getValue());
        }
    }

    @Test
    public void executeQueryWithExplicitView() throws Exception {
        String url = baseUrl + "/queries/sec$User/userByLogin?view=_local";
        Map<String, String> params = new HashMap<>();
        params.put("login", "bob");
        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            assertEquals(MediaType.APPLICATION_JSON_UTF8_VALUE, responseContentType(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(1, ctx.<Collection>read("$").size());

            assertThrows(PathNotFoundException.class, () -> ctx.read("$.[0].group"));
        }
    }

    @Test
    public void executeQueryWithExplicitFetchPlan() throws Exception {
        String url = baseUrl + "/queries/sec$User/userByLogin?fetchPlan=_local";
        Map<String, String> params = new HashMap<>();
        params.put("login", "bob");
        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            assertEquals(MediaType.APPLICATION_JSON_UTF8_VALUE, responseContentType(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(1, ctx.<Collection>read("$").size());

            assertThrows(PathNotFoundException.class, () -> ctx.read("$.[0].group"));
        }
    }

    @Test
    public void executeQueryWithExplicitViewThatIsMissing() throws Exception {
        String missingViewName = "missingView";
        String url = baseUrl + "/queries/sec$User/userByLogin?view=" + missingViewName;
        Map<String, String> params = new HashMap<>();
        params.put("login", "bob");
        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals("Fetch plan not found", ctx.read("$.error"));
            assertEquals(String.format("Fetch plan %s for entity sec$User not found", missingViewName), ctx.read("$.details"));
        }
    }

    @Test
    public void executeQueryWithExplicitFetchPlanThatIsMissing() throws Exception {
        String missingViewName = "missingView";
        String url = baseUrl + "/queries/sec$User/userByLogin?fetchPlan=" + missingViewName;
        Map<String, String> params = new HashMap<>();
        params.put("login", "bob");
        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals("Fetch plan not found", ctx.read("$.error"));
            assertEquals(String.format("Fetch plan %s for entity sec$User not found", missingViewName), ctx.read("$.details"));
        }
    }

    @Test
    public void executeQueryWithNullsSerialization() throws Exception {
        String url = baseUrl + "/queries/sec$User/userByLogin?returnNulls=true";
        Map<String, String> params = new HashMap<>();
        params.put("login", "bob");
        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            assertEquals(MediaType.APPLICATION_JSON_UTF8_VALUE, responseContentType(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(1, ctx.<Collection>read("$").size());

            assertNotNull(ctx.read("$.[0].login"));
            assertNull(ctx.read("$.[0].middleName"));
        }
    }

    @Test
    public void getCountForQuery() throws Exception {
        String url = baseUrl + "/queries/sec$User/userByLogin/count";
        Map<String, String> params = new HashMap<>();
        params.put("login", "bob");
        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            assertEquals("application/json;charset=UTF-8", responseContentType(response));
            assertEquals("1", responseToString(response));
        }
    }

    @Test
    public void getCountForQueryText() throws Exception {
        String url = baseUrl + "/queries/sec$User/userByLogin/count";
        Map<String, String> params = new HashMap<>();
        params.put("login", "bob");
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "text/plain");
        headers.put("Accept-Language", "en");
        try (CloseableHttpResponse response = sendGetWithHeaders(url, oauthToken, params, headers)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            assertEquals("text/plain;charset=UTF-8", responseContentType(response));
            assertEquals("1", responseToString(response));
        }
    }

    @Test
    public void getCountForEntitiesList() throws Exception {
        String url = baseUrl + "/queries/sec$User/all/count";
        try (CloseableHttpResponse response = sendGet(url, oauthToken, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            assertEquals("application/json;charset=UTF-8", responseContentType(response));
            assertEquals("3", responseToString(response));
        }
    }

    @Test
    public void executeQueryMissingName() throws Exception {
        String url = baseUrl + "/queries/sec$User/missingQueryName";
        Map<String, String> params = new HashMap<>();
        params.put("login", "bob");
        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_NOT_FOUND, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals("Query not found", ctx.read("$.error"));
        }
    }

    @Test
    public void executeQueryMissingParameter() throws Exception {
        String url = baseUrl + "/queries/sec$User/userByLogin";
        Map<String, String> params = new HashMap<>();
        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals("Query parameter not found", ctx.read("$.error"));
        }
    }

    @Test
    public void loadQueriesList() throws Exception {
        String url = baseUrl + "/queries/sec$User";
        Map<String, String> params = new HashMap<>();
        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(2, ctx.<Collection>read("$").size());
            assertEquals("userByLogin", ctx.read("$.[0].name"));
            assertEquals("sec$User", ctx.read("$.[0].entityName"));
            assertEquals("user-with-group", ctx.read("$.[0].viewName"));
            assertEquals(1, ctx.<Collection>read("$.[0].params").size());
            assertEquals("login", ctx.read("$.[0].params.[0].name"));
            assertEquals("java.lang.String", ctx.read("$.[0].params.[0].type"));
        }
    }

    @Test
    public void executeQueryWithLimitAndOffset() throws Exception {
        String url = baseUrl + "/queries/ref$Colour/allColours?limit=5&offset=3";
        Map<String, String> params = new HashMap<>();
        params.put("returnCount", "true");
        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(5, ctx.<Collection>read("$").size());
            assertEquals("Colour 4", ctx.read("$.[0].name"));

            Header[] totalCountHeaders = response.getHeaders("X-Total-Count");
            assertEquals(1, totalCountHeaders.length);

            int count;
            try (PreparedStatement stmt = conn.prepareStatement("select count(*) from REF_COLOUR where delete_ts is null")) {
                ResultSet rs = stmt.executeQuery();
                assertTrue(rs.next());
                count = rs.getInt(1);
            }
            assertEquals(String.valueOf(count), totalCountHeaders[0].getValue());

        }
    }

    @Test
    public void executeQueryWithPredefinedLimitAndOffset() throws Exception {
        String url = baseUrl + "/queries/ref$Colour/coloursFromThreeToFive";

        try (CloseableHttpResponse response = sendGet(url, oauthToken, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(3, ctx.<Collection>read("$").size());
            assertEquals("Colour 3", ctx.read("$.[0].name"));
            assertEquals("Colour 4", ctx.read("$.[1].name"));
            assertEquals("Colour 5", ctx.read("$.[2].name"));
        }
    }

    @Test
    public void executeQueryWithTransform() throws Exception {
        String url = baseUrl + "/queries/ref$OldCar/carByVin";
        Map<String, String> params = new HashMap<>();
        params.put("vin", "001");
        params.put("modelVersion", "1.0");
        params.put("returnCount", "true");
        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            assertEquals(MediaType.APPLICATION_JSON_UTF8_VALUE, responseContentType(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(1, ctx.<Collection>read("$").size());
            assertEquals("001", ctx.read("$.[0].oldVin"));

            Header[] totalCountHeaders = response.getHeaders("x-total-count");
            assertEquals(1, totalCountHeaders.length);

            //check that nulls aren't serialized by default
            assertThrows(PathNotFoundException.class, () -> ctx.read("&.[0].vin"));
        }
    }

    @Test
    public void getCountForQueryWithTransform() throws Exception {
        String url = baseUrl + "/queries/ref$OldCar/carByVin/count";
        Map<String, String> params = new HashMap<>();
        params.put("vin", "001");
        params.put("modelVersion", "1.0");
        try (CloseableHttpResponse response = sendGet(url, oauthToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            assertEquals("application/json;charset=UTF-8", responseContentType(response));
            assertEquals("1", responseToString(response));
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
}
