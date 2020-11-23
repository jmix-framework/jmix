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

import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import io.jmix.core.Id;
import io.jmix.core.security.CoreUser;
import io.jmix.samples.rest.entity.driver.Car;
import io.jmix.samples.rest.entity.driver.Colour;
import io.jmix.samples.rest.entity.driver.Model;
import io.jmix.samples.rest.security.*;
import io.jmix.samples.rest.service.app.RestTestService;
import io.jmix.security.authentication.RoleGrantedAuthority;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static io.jmix.samples.rest.tools.RestTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
class EntitiesControllerSecurityFT extends AbstractRestControllerFT {

    /**
     * Entitites ids
     */
    private String carUuidString;
    private String colourUuidString;
    private Colour colour;
    private Car car;
    private Model model;
    /**
     * User ids
     */
    private CoreUser colorRead;
    private CoreUser colorUpdate;
    private CoreUser colorCreate;
    private CoreUser colorDelete;
    private CoreUser carRead;
    /**
     * Logins
     */
    private final String colorReadUserLogin = "colorReadUser";
    private final String colorUpdateUserLogin = "colorUpdateUser";
    private final String colorCreateUserLogin = "colorCreateUser";
    private final String colorDeleteUserLogin = "colorDeleteUser";
    private final String carReadUserLogin = "carReadUser";
    private final String colorReadUserPassword = "colorReadUser";
    private final String colorUpdateUserPassword = "colorUpdateUser";
    private final String colorCreateUserPassword = "colorCreateUser";
    private final String colorDeleteUserPassword = "colorDeleteUser";
    private final String carReadUserPassword = "carReadUser";

    /**
     * User OAuth tokens
     */
    private String colorReadUserToken;
    private String colorUpdateUserToken;
    private String colorCreateUserToken;
    private String colorDeleteUserToken;
    private String carReadUserToken;

    private UUID groupUuid = UUID.fromString("0fa2b1a5-1d68-4d69-9fbd-dff348347f93");
    private String modelUuidString;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        createUsers();

        colorReadUserToken = getAuthToken(baseUrl, colorReadUserLogin, colorReadUserPassword);
        colorUpdateUserToken = getAuthToken(baseUrl, colorUpdateUserLogin, colorUpdateUserPassword);
        colorCreateUserToken = getAuthToken(baseUrl, colorCreateUserLogin, colorCreateUserPassword);
        colorDeleteUserToken = getAuthToken(baseUrl, colorDeleteUserLogin, colorDeleteUserPassword);
        carReadUserToken = getAuthToken(baseUrl, carReadUserLogin, carReadUserPassword);
    }

    @AfterEach
    public void tearDown() throws SQLException {
        dirtyData.cleanup(conn);
        dataManager.remove(Id.of(car));
        dataManager.remove(Id.of(model));
        dataManager.remove(Id.of(colour));
        if (conn != null)
            conn.close();
    }

    @Test
    void findPermitted() throws Exception {
        //trying to get entity with permitted read access
        String url = baseUrl + "/entities/ref$Colour/" + colourUuidString;
        try (CloseableHttpResponse response = sendGet(url, colorReadUserToken, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(colourUuidString, ctx.read("$.id"));
        }
    }

    @Test
    void findForbidden() throws Exception {
        //trying to get entity with forbidden read access
        String url = baseUrl + "/entities/ref_Car/" + carUuidString;
        try (CloseableHttpResponse response = sendGet(url, colorReadUserToken, null)) {
            assertEquals(HttpStatus.SC_FORBIDDEN, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals("Reading forbidden", ctx.read("$.error"));
        }
    }

    @Test
    void findAttributes() throws Exception {
        //checks that forbidden attributes aren't included to the result JSON
        String url = baseUrl + "/entities/ref_Car/" + carUuidString + "?view=carEdit";
        try (CloseableHttpResponse response = sendGet(url, carReadUserToken, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(carUuidString, ctx.read("$.id"));
            assertNotNull(ctx.read("$.vin"));
            assertNotNull(ctx.read("$.model"));

            assertThrows(PathNotFoundException.class, () -> ctx.read("$.colour"));

            assertThrows(PathNotFoundException.class, () -> ctx.read("$.driverAllocations"));
        }
    }

    @Test
    void unavailableAttributesMustBeHiddenInQueryResult() throws Exception {
        String url = baseUrl + "/queries/ref_Car/carByVin?vin=VWV000";

        try (CloseableHttpResponse response = sendGet(url, carReadUserToken, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(carUuidString, ctx.read("$.[0].id"));
            assertNotNull(ctx.read("$.[0].vin"));
            assertNotNull(ctx.read("$.[0].model"));

            assertThrows(PathNotFoundException.class, () -> ctx.read("$.[0].colour"));
        }
    }

    @Test
    public void unavailableAttributesMustBeHiddenInServiceResult() throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("carId", carUuidString);
        params.put("viewName", "carEdit");
        try (CloseableHttpResponse response = sendGet(baseUrl + "/services/" + RestTestService.NAME + "/findCar", carReadUserToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            assertEquals("application/json;charset=UTF-8", responseContentType(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(carUuidString, ctx.read("$.id"));
            assertEquals("ref_Car", ctx.read("$._entityName"));
            assertNotNull(ctx.read("$.model"));
            assertThrows(PathNotFoundException.class, () -> ctx.read("$.colour"));
        }
    }

    @Test
    public void createForbidden() throws Exception {
        String url = baseUrl + "/entities/ref$Colour";
        String json = getFileContent("colour.json", null);
        try (CloseableHttpResponse response = sendPost(url, colorReadUserToken, json, null)) {
            assertEquals(HttpStatus.SC_FORBIDDEN, statusCode(response));
        }
    }

    @Test
    public void updateForbiddenEntity() throws Exception {
        String url = baseUrl + "/entities/ref$Colour/" + colourUuidString;
        String json = getFileContent("colour.json", null);
        try (CloseableHttpResponse response = sendPut(url, colorReadUserToken, json, null)) {
            assertEquals(HttpStatus.SC_FORBIDDEN, statusCode(response));
        }
    }

    @Test
    public void updateForbiddenAttribute() throws Exception {
        //checks that forbidden attribute won't be updated
        String url = baseUrl + "/entities/ref$Colour/" + colourUuidString;
        Map<String, String> replacements = new HashMap<>();
        replacements.put("$COLOUR_ID$", colourUuidString);
        String json = getFileContent("updateColorWithForbiddenAttr.json", replacements);
        try (CloseableHttpResponse response = sendPut(url, colorUpdateUserToken, json, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
        }

        try (PreparedStatement stmt = conn.prepareStatement("select DESCRIPTION from REF_COLOUR where ID = ?")) {
            stmt.setObject(1, UUID.fromString(colourUuidString));
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            String description = rs.getString("DESCRIPTION");
            assertEquals("Description", description);
        }
    }

    @Test
    public void updatePermittedAttribute() throws Exception {
        //checks that permitted attribute will be updated
        String url = baseUrl + "/entities/ref$Colour/" + colourUuidString;
        Map<String, String> replacements = new HashMap<>();
        replacements.put("$COLOUR_ID$", colourUuidString);
        String json = getFileContent("updateColorWithPermittedAttr.json", replacements);

        try (CloseableHttpResponse response = sendPut(url, colorUpdateUserToken, json, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
        }

        try (PreparedStatement stmt = conn.prepareStatement("select NAME from REF_COLOUR where ID = ?")) {
            stmt.setObject(1, UUID.fromString(colourUuidString));
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            String name = rs.getString("NAME");
            assertEquals("Red 2", name);
        }
    }

    @Test
    public void deleteForbidden() throws Exception {
        String url = baseUrl + "/entities/ref$Colour/" + colourUuidString;

        try (CloseableHttpResponse response = sendDelete(url, colorUpdateUserToken, null)) {
            assertEquals(HttpStatus.SC_FORBIDDEN, statusCode(response));
        }

        try (PreparedStatement stmt = conn.prepareStatement("select NAME from REF_COLOUR where ID = ?")) {
            stmt.setObject(1, UUID.fromString(colourUuidString));
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
        }
    }

    public void prepareDb() throws Exception {
        Class.forName("org.hsqldb.jdbc.JDBCDriver");
        conn = DriverManager.getConnection(DB_URL, "sa", "");
        createDbData();
    }

    private void createDbData() {
        colour = dataManager.create(Colour.class);
        colour.setName("Red");
        colour.setDescription("Description");
        colour = dataManager.save(colour);
        colourUuidString = colour.getId().toString();

        model = dataManager.create(Model.class);
        model.setName("Audi");
        model = dataManager.save(model);
        modelUuidString = model.getId().toString();

        car = dataManager.create(Car.class);
        car.setVin("VWV000");
        car.setColour(colour);
        car.setModel(model);
        car = dataManager.save(car);
        carUuidString = car.getId().toString();
    }

    private void createUsers() {
        //noinspection ConstantConditions
        colorRead = new CoreUser(colorReadUserLogin, "{noop}" + colorReadUserPassword,
                Collections.singleton(new RoleGrantedAuthority(roleRepository.getRoleByCode(ColorReadRole.NAME))));
        userRepository.addUser(colorRead);

        //noinspection ConstantConditions
        colorUpdate = new CoreUser(colorUpdateUserLogin, "{noop}" + colorUpdateUserPassword,
                Collections.singleton(new RoleGrantedAuthority(roleRepository.getRoleByCode(ColorUpdateRole.NAME))));
        userRepository.addUser(colorUpdate);

        //noinspection ConstantConditions
        colorCreate = new CoreUser(colorCreateUserLogin, "{noop}" + colorCreateUserPassword,
                Collections.singleton(new RoleGrantedAuthority(roleRepository.getRoleByCode(ColorCreateRole.NAME))));
        userRepository.addUser(colorCreate);

        //noinspection ConstantConditions
        colorDelete = new CoreUser(colorDeleteUserLogin, "{noop}" + colorDeleteUserPassword,
                Collections.singleton(new RoleGrantedAuthority(roleRepository.getRoleByCode(ColorDeleteRole.NAME))));
        userRepository.addUser(colorDelete);

        //noinspection ConstantConditions
        carRead = new CoreUser(carReadUserLogin, "{noop}" + carReadUserPassword,
                Collections.singleton(new RoleGrantedAuthority(roleRepository.getRoleByCode(CarReadRole.NAME))));
        userRepository.addUser(carRead);
    }
}
