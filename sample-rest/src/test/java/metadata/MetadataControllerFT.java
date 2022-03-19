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

package metadata;

import com.jayway.jsonpath.ReadContext;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.jupiter.api.Test;
import test_support.AbstractRestControllerFT;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static test_support.RestTestUtils.parseResponse;
import static test_support.RestTestUtils.sendGet;

/**
 *
 */
public class MetadataControllerFT extends AbstractRestControllerFT {

    static {
        Locale.setDefault(Locale.ENGLISH);
    }

    @Test
    public void getUserMetadata() throws Exception {
        String url = baseUrl + "/metadata/entities/sec$User";
        try (CloseableHttpResponse response = sendGet(url, oauthToken, null)) {
            ReadContext ctx = parseResponse(response);
            assertEquals("sec$User", ctx.read("$.entityName"));
            assertEquals("sys$StandardEntity", ctx.read("$.ancestor"));

            Map<String, Object> groupFields = (Map<String, Object>) ctx.read("$.properties[?(@.name == 'group')]", List.class).get(0);
            assertEquals("group", groupFields.get("name"));
            assertEquals("MANY_TO_ONE", groupFields.get("cardinality"));
            assertEquals("sec$Group", groupFields.get("type"));
            assertEquals("ASSOCIATION", groupFields.get("attributeType"));
            assertEquals(true, groupFields.get("mandatory"));
            assertEquals(false, groupFields.get("readOnly"));
            assertEquals(true, groupFields.get("persistent"));

            Map<String, Object> userRolesFields = (Map<String, Object>) ctx.read("$.properties[?(@.name == 'userRoles')]", List.class).get(0);
            assertEquals("ONE_TO_MANY", userRolesFields.get("cardinality"));
            assertEquals("sec$UserRole", userRolesFields.get("type"));
            assertEquals("COMPOSITION", userRolesFields.get("attributeType"));
            assertEquals("User Roles", userRolesFields.get("description"));

            assertEquals("string", ctx.read("$.properties[?(@.name == 'login')].type", List.class).get(0));
            assertEquals("int", ctx.read("$.properties[?(@.name == 'version')].type", List.class).get(0));
            assertEquals("dateTime", ctx.read("$.properties[?(@.name == 'updateTs')].type", List.class).get(0));
        }
    }

    @Test
    public void getDriverMetadata() throws Exception {
        String url = baseUrl + "/metadata/entities/ref$Driver";
        try (CloseableHttpResponse response = sendGet(url, oauthToken, null)) {
            ReadContext ctx = parseResponse(response);
            assertEquals("ref$ExtDriver", ctx.read("$.entityName"));

            assertEquals("io.jmix.samples.rest.entity.driver.DriverStatus", ctx.read("$.properties[?(@.name == 'status')].type", List.class).get(0));
            assertEquals("ENUM", ctx.read("$.properties[?(@.name == 'status')].attributeType", List.class).get(0));
        }
    }

    @Test
    public void getAllEntitiesMetadata() throws Exception {
        String url = baseUrl + "/metadata/entities";
        try (CloseableHttpResponse response = sendGet(url, oauthToken, null)) {
            ReadContext ctx = parseResponse(response);
            assertTrue(ctx.read("$.length()", Integer.class) > 1);
            assertEquals(1, ctx.read("$[?(@.entityName == 'sec$User')]", List.class).size());
            assertEquals(1, ctx.read("$[?(@.entityName == 'sec$User')].properties[?(@.name == 'login')]", List.class).size());
        }
    }

    @Test
    public void getView() throws Exception {
        String url = baseUrl + "/metadata/entities/ref_Car/views/carEdit";
        try (CloseableHttpResponse response = sendGet(url, oauthToken, null)) {
            ReadContext ctx = parseResponse(response);
            assertEquals("ref_Car", ctx.read("$.entity"));
            Map<String, Object> viewProperties = (Map<String, Object>) ctx.read("$.properties[?(@.name == 'model')].fetchPlan", List.class).get(0);
            assertEquals("_local", viewProperties.get("name"));

            Map<String, Object> repairsViewProperties = (Map<String, Object>) ctx.read("$.properties[?(@.name == 'repairs')].fetchPlan", List.class).get(0);
            assertEquals("repairEdit", repairsViewProperties.get("name"));
            assertTrue(((Collection) repairsViewProperties.get("properties")).size() > 0);
        }
    }

    @Test
    public void getAllViews() throws Exception {
        String url = baseUrl + "/metadata/entities/ref_Car/views";
        try (CloseableHttpResponse response = sendGet(url, oauthToken, null)) {
            ReadContext ctx = parseResponse(response);
            assertTrue(ctx.read("$.length()", Integer.class) > 1);
            assertEquals(1, ctx.read("$[?(@.name == 'carEdit')]", List.class).size());
        }
    }

    @Test
    public void getFetchPlan() throws Exception {
        String url = baseUrl + "/metadata/entities/ref_Car/fetchPlans/carEdit";
        try (CloseableHttpResponse response = sendGet(url, oauthToken, null)) {
            ReadContext ctx = parseResponse(response);
            assertEquals("ref_Car", ctx.read("$.entity"));
            Map<String, Object> viewProperties = (Map<String, Object>) ctx.read("$.properties[?(@.name == 'model')].fetchPlan", List.class).get(0);
            assertEquals("_local", viewProperties.get("name"));

            Map<String, Object> repairsViewProperties = (Map<String, Object>) ctx.read("$.properties[?(@.name == 'repairs')].fetchPlan", List.class).get(0);
            assertEquals("repairEdit", repairsViewProperties.get("name"));
            assertTrue(((Collection) repairsViewProperties.get("properties")).size() > 0);
        }
    }

    @Test
    public void getAllFetchPlans() throws Exception {
        String url = baseUrl + "/metadata/entities/ref_Car/fetchPlans";
        try (CloseableHttpResponse response = sendGet(url, oauthToken, null)) {
            ReadContext ctx = parseResponse(response);
            assertTrue(ctx.read("$.length()", Integer.class) > 1);
            assertEquals(1, ctx.read("$[?(@.name == 'carEdit')]", List.class).size());
        }
    }
}
