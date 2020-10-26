/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package io.jmix.samples.rest.tests;

import com.jayway.jsonpath.ReadContext;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static io.jmix.samples.rest.tools.RestTestUtils.parseResponse;
import static io.jmix.samples.rest.tools.RestTestUtils.sendGet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
            assertEquals(false, groupFields.get("transient"));

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
            Map<String, Object> viewProperties = (Map<String, Object>) ctx.read("$.properties[?(@.name == 'model')].view", List.class).get(0);
            assertEquals("_local", viewProperties.get("name"));

            Map<String, Object> repairsViewProperties = (Map<String, Object>) ctx.read("$.properties[?(@.name == 'repairs')].view", List.class).get(0);
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
}
