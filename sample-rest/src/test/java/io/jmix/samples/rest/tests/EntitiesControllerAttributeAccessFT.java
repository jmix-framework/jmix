/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package io.jmix.samples.rest.tests;

import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import io.jmix.core.security.impl.CoreUser;
import io.jmix.samples.rest.security.FullAccessRole;
import io.jmix.security.role.assignment.RoleAssignment;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.jmix.samples.rest.tools.RestTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
@TestPropertySource(properties = {
        "cuba.rest.requiresSecurityToken = true"
})
@Disabled
public class EntitiesControllerAttributeAccessFT extends AbstractRestControllerFT {

    /**
     * Entitites ids
     */
    private String driver1UuidString, driver2UuidString, car1UuidString, car2UuidString;


    /**
     * User ids
     */
    private UUID driverReadUserId;
    /**
     * Logins
     */
    private String driverReadUserLogin = "driverReadUser";
    private String driverReadUserPassword = "driverReadUser";

    /**
     * User OAuth tokens
     */
    private String driverReadUserToken;

    private UUID groupUuid = UUID.fromString("0fa2b1a5-1d68-4d69-9fbd-dff348347f93");

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        driverReadUserToken = getAuthToken(baseUrl, driverReadUserLogin, driverReadUserPassword);
    }

    @AfterEach
    public void tearDown() throws SQLException {
        dirtyData.cleanup(conn);
        if (conn != null)
            conn.close();
    }

    @Test
    public void createNewEntity() throws Exception {
        Map<String, String> replacements = new HashMap<>();
        String json = getFileContent("attributeAccess_createDriver.json", replacements);

        UUID driverId;
        String url = baseUrl + "/entities/ref$Driver/";

        Map<String, String> params = new HashMap<>();
        params.put("responseView", "driverWithVersionAndCreateTs");

        try (CloseableHttpResponse response = sendPost(url, oauthToken, json, params)) {
            assertEquals(HttpStatus.SC_CREATED, statusCode(response));
            Header[] locationHeaders = response.getHeaders("Location");
            assertEquals(1, locationHeaders.length);
            String location = locationHeaders[0].getValue();

            String expectedLink = baseUrl + "/entities/ref$Driver";
            assertTrue(location.startsWith(expectedLink));
            String idString = location.substring(location.lastIndexOf("/") + 1);
            driverId = UUID.fromString(idString);

            ReadContext ctx = parseResponse(response);
            assertEquals("ref$Driver", ctx.read("$._entityName"));
            assertEquals(driverId.toString(), ctx.read("$.id"));
            assertNotNull(ctx.read("$.createTs"));
            assertNotNull(ctx.read("$.version"));

            //to delete the created objects in the @After method
            dirtyData.addDriverId(driverId);
        }

        try (PreparedStatement stmt = conn.prepareStatement("select NAME from REF_DRIVER where ID = ?")) {
            stmt.setObject(1, driverId);
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            String name = rs.getString("NAME");
            assertEquals("Driver#2 NEW NAME", name);
        }
    }

    @Test
    public void findDriverWithNotPermittedName() throws Exception {
        String url = baseUrl + "/entities/ref$Driver/" + driver1UuidString;
        try (CloseableHttpResponse response = sendGet(url, driverReadUserToken, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(driver1UuidString, ctx.read("$.id"));

            assertThrows(PathNotFoundException.class, () -> ctx.read("$.name"));
        }
    }

    @Test
    public void findDriverWithPermittedName() throws Exception {
        String url = baseUrl + "/entities/ref$Driver/" + driver2UuidString;
        try (CloseableHttpResponse response = sendGet(url, driverReadUserToken, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(driver2UuidString, ctx.read("$.id"));

            assertEquals("Driver#2", ctx.read("$.name"));
        }
    }

    @Test
    public void updateDriverNameNotPermitted() throws Exception {
        String securityToken;
        String url = baseUrl + "/entities/ref$Driver/" + driver1UuidString;
        Map<String, String> params = new HashMap<>();
        params.put("responseView", "driverWithStatusAndName");

        try (CloseableHttpResponse response = sendGet(url, driverReadUserToken, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(driver1UuidString, ctx.read("$.id"));

            securityToken = ctx.read("$.__securityToken");
            assertNotNull(securityToken);
        }

        url = "/entities/ref$Driver/" + driver1UuidString;
        Map<String, String> replacements = new HashMap<>();
        replacements.put("$DRIVER_ID$", driver1UuidString);
        replacements.put("$SECURITY_TOKEN$", securityToken);

        String json = getFileContent("attributeAccess_updateDriverNameForbidden.json", replacements);
        try (CloseableHttpResponse response = sendPut(url, driverReadUserToken, json, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);

            assertThrows(PathNotFoundException.class, () -> ctx.read("$.name"));
        }

        try (PreparedStatement stmt = conn.prepareStatement("select NAME from REF_DRIVER where ID = ?")) {
            stmt.setObject(1, UUID.fromString(driver1UuidString));
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            String name = rs.getString("NAME");
            assertEquals("Driver#1", name);
        }
    }

    @Test
    public void updateDriverNameNotPermittedWithEnabledAttributeSecurity() throws Exception {
//        Connectors.jmx(SampleJmxService.class)
//                .setEntityAttributePermissionChecking(true);
        try {
            String securityToken;
            String url = baseUrl + "/entities/ref$Driver/" + driver1UuidString;
            try (CloseableHttpResponse response = sendGet(url, driverReadUserToken, null)) {
                assertEquals(HttpStatus.SC_OK, statusCode(response));
                ReadContext ctx = parseResponse(response);
                assertEquals(driver1UuidString, ctx.read("$.id"));

                securityToken = ctx.read("$.__securityToken");
                assertNotNull(securityToken);
            }

            url = baseUrl + "/entities/ref$Driver/" + driver1UuidString;
            Map<String, String> replacements = new HashMap<>();
            replacements.put("$DRIVER_ID$", driver1UuidString);
            replacements.put("$SECURITY_TOKEN$", securityToken);

            Map<String, String> params = new HashMap<>();
            params.put("responseView", "driverWithStatusAndName");

            String json = getFileContent("attributeAccess_updateDriverNameForbidden.json", replacements);
            try (CloseableHttpResponse response = sendPut(url, driverReadUserToken, json, params)) {
                assertEquals(HttpStatus.SC_OK, statusCode(response));
                ReadContext ctx = parseResponse(response);

                assertThrows(PathNotFoundException.class, () -> ctx.read("$.name"));
            }

            try (PreparedStatement stmt = conn.prepareStatement("select NAME from REF_DRIVER where ID = ?")) {
                stmt.setObject(1, UUID.fromString(driver1UuidString));
                ResultSet rs = stmt.executeQuery();
                assertTrue(rs.next());
                String name = rs.getString("NAME");
                assertEquals("Driver#1", name);
            }
        } finally {
//            Connectors.jmx(SampleJmxService.class)
//                    .setEntityAttributePermissionChecking(false);
        }
    }

    @Test
    public void updateDriverStatusRequired() throws Exception {
        String securityToken;
        String url = baseUrl + "/entities/ref$Driver/" + driver1UuidString;
        try (CloseableHttpResponse response = sendGet(url, driverReadUserToken, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(driver1UuidString, ctx.read("$.id"));

            securityToken = ctx.read("$.__securityToken");
            assertNotNull(securityToken);
        }

        url = "/entities/ref$Driver/" + driver1UuidString;
        Map<String, String> replacements = new HashMap<>();
        replacements.put("$DRIVER_ID$", driver1UuidString);
        replacements.put("$SECURITY_TOKEN$", securityToken);
        String json = getFileContent("attributeAccess_updateDriverStatusRequired.json", replacements);
        try (CloseableHttpResponse response = sendPut(url, driverReadUserToken, json, null)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode(response));
        }
    }

    @Test
    public void findDriverWithNonPermittedCountryInAddress() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("view", "driverEdit");
        String url = baseUrl + "/entities/ref$Driver/" + driver1UuidString;
        try (CloseableHttpResponse response = sendGet(url, driverReadUserToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(driver1UuidString, ctx.read("$.id"));

            assertEquals("City#1", ctx.read("$.address.city"));

            assertThrows(PathNotFoundException.class, () -> ctx.read("$.address.country"));
        }
    }

    @Test
    public void updateDriverAddress() throws Exception {
        String securityToken;
        String addressSecurityToken;
        Map<String, String> params = new HashMap<>();
        params.put("view", "driverEdit");
        String url = baseUrl + "/entities/ref$Driver/" + driver1UuidString;
        try (CloseableHttpResponse response = sendGet(url, driverReadUserToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(driver1UuidString, ctx.read("$.id"));

            securityToken = ctx.read("$.__securityToken");
            assertNotNull(securityToken);

            addressSecurityToken = ctx.read("$.address.__securityToken");
            assertNotNull(addressSecurityToken);
        }

        url = "/entities/ref$Driver/" + driver1UuidString;
        Map<String, String> replacements = new HashMap<>();
        replacements.put("$DRIVER_ID$", driver1UuidString);
        replacements.put("$SECURITY_TOKEN$", securityToken);
        replacements.put("$ADDRESS_SECURITY_TOKEN$", addressSecurityToken);
        String json = getFileContent("attributeAccess_updateDriverAddressForbidden.json", replacements);
        try (CloseableHttpResponse response = sendPut(url, driverReadUserToken, json, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
        }

        try (PreparedStatement stmt = conn.prepareStatement("select CITY, COUNTRY from REF_DRIVER where ID = ?")) {
            stmt.setObject(1, UUID.fromString(driver1UuidString));
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            String city = rs.getString("CITY");
            assertEquals("City#1", city);

            String country = rs.getString("COUNTRY");
            assertEquals("Country#1", country);
        }


        url = "/entities/ref$Driver/" + driver1UuidString;
        replacements = new HashMap<>();
        replacements.put("$DRIVER_ID$", driver1UuidString);
        replacements.put("$SECURITY_TOKEN$", securityToken);
        replacements.put("$ADDRESS_SECURITY_TOKEN$", addressSecurityToken);
        json = getFileContent("attributeAccess_updateDriverAddressRequired.json", replacements);
        try (CloseableHttpResponse response = sendPut(url, driverReadUserToken, json, null)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode(response));
        }
    }


    @Test
    public void findDriverWithNonPermittedOneToManyAllocation() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("view", "driverWithAllocations");
        String url = baseUrl + "/entities/ref$Driver/" + driver1UuidString;
        try (CloseableHttpResponse response = sendGet(url, driverReadUserToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(driver1UuidString, ctx.read("$.id"));

            assertThrows(PathNotFoundException.class, () -> ctx.read("$.allocations[0].car"));
        }
    }

    @Test
    public void findDriverWithPermittedOneToManyAllocation() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("view", "driverWithAllocations");
        String url = baseUrl + "/entities/ref$Driver/" + driver2UuidString;
        try (CloseableHttpResponse response = sendGet(url, driverReadUserToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(driver2UuidString, ctx.read("$.id"));

            assertEquals("001", ctx.read("$.allocations[0].car.vin"));
        }
    }


    @Test
    public void updateCarNotPermitted() throws Exception {
        String securityToken;
        String allocationSecurityToken;
        String allocationId;
        Map<String, String> params = new HashMap<>();
        params.put("view", "driverWithAllocations");
        String url = baseUrl + "/entities/ref$Driver/" + driver1UuidString;
        try (CloseableHttpResponse response = sendGet(url, driverReadUserToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(driver1UuidString, ctx.read("$.id"));

            securityToken = ctx.read("$.__securityToken");
            assertNotNull(securityToken);

            allocationSecurityToken = ctx.read("$.allocations[0].__securityToken");
            allocationId = ctx.read("$.allocations[0].id");
            assertNotNull(allocationSecurityToken);
        }

        url = "/entities/ref$Driver/" + driver1UuidString;
        Map<String, String> replacements = new HashMap<>();
        replacements.put("$DRIVER_ID$", driver1UuidString);
        replacements.put("$SECURITY_TOKEN$", securityToken);
        replacements.put("$ALLOCATION_SECURITY_TOKEN$", allocationSecurityToken);
        replacements.put("$ALLOCATION_ID$", allocationId);
        replacements.put("$CAR_ID$", car2UuidString);
        String json = getFileContent("attributeAccess_updateDriverAllocationForbidden.json", replacements);
        try (CloseableHttpResponse response = sendPut(url, driverReadUserToken, json, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
        }

        try (PreparedStatement stmt = conn.prepareStatement("select CAR_ID from REF_DRIVER_ALLOC where DRIVER_ID = ?")) {
            stmt.setObject(1, UUID.fromString(driver1UuidString));
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            String carId = (String) rs.getObject("CAR_ID");
            assertEquals(car1UuidString, carId);
        }
    }

    @Test
    public void updateCarPermitted() throws Exception {
        String securityToken;
        String allocationSecurityToken;
        String allocationId;
        Map<String, String> params = new HashMap<>();
        params.put("view", "driverWithAllocations");
        String url = baseUrl + "/entities/ref$Driver/" + driver2UuidString;
        try (CloseableHttpResponse response = sendGet(url, driverReadUserToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(driver2UuidString, ctx.read("$.id"));
            allocationId = ctx.read("$.allocations[0].id");

            securityToken = ctx.read("$.__securityToken");
            assertNotNull(securityToken);

            allocationSecurityToken = ctx.read("$.allocations[0].__securityToken");
            assertNotNull(allocationSecurityToken);
        }

        url = "/entities/ref$Driver/" + driver2UuidString;
        Map<String, String> replacements = new HashMap<>();
        replacements.put("$DRIVER_ID$", driver2UuidString);
        replacements.put("$ALLOCATION_ID$", allocationId);
        replacements.put("$CAR_ID$", car2UuidString);
        replacements.put("$SECURITY_TOKEN$", securityToken);
        replacements.put("$ALLOCATION_SECURITY_TOKEN$", allocationSecurityToken);
        String json = getFileContent("attributeAccess_updateDriverAllocationAllowed.json", replacements);
        try (CloseableHttpResponse response = sendPut(url, driverReadUserToken, json, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
        }

        try (PreparedStatement stmt = conn.prepareStatement("select CAR_ID from REF_DRIVER_ALLOC where DRIVER_ID = ?")) {
            stmt.setObject(1, UUID.fromString(driver2UuidString));
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            String carId = (String) rs.getObject("CAR_ID");
            assertEquals(car2UuidString, carId);
        }
    }

    @Test
    public void updateDriverWithoutSecurityToken() throws Exception {
        String url = baseUrl + "/entities/ref$Driver/" + driver2UuidString;
        Map<String, String> replacements = new HashMap<>();
        replacements.put("$DRIVER_ID$", driver2UuidString);
        String json = getFileContent("attributeAccess_updateDriverWithoutSecurityToken.json", replacements);
        try (CloseableHttpResponse response = sendPut(url, driverReadUserToken, json, null)) {
            assertEquals(HttpStatus.SC_FORBIDDEN, statusCode(response));
        }
    }


    public void prepareDb() throws Exception {
        Class.forName("org.hsqldb.jdbc.JDBCDriver");
        createDbData();
        createDbUsers();
    }

    private void createDbData() throws SQLException {
        UUID driverUuid = dirtyData.createDriverUuid();
        driver1UuidString = driverUuid.toString();
        executePrepared("insert into ref_driver(id, version, name, status, city, state, country, DTYPE) values(?, ?, ?, ?, ?, ?, ?,'ref$ExtDriver')",
                driverUuid,
                1l,
                "Driver#1",
                10,
                "City#1",
                "State#1",
                "Country#1"
        );

        driverUuid = dirtyData.createDriverUuid();
        driver2UuidString = driverUuid.toString();
        executePrepared("insert into ref_driver(id, version, name, status, DTYPE) values(?, ?, ?, ?, 'ref$ExtDriver')",
                driverUuid,
                1l,
                "Driver#2",
                20
        );

        UUID carUuid = dirtyData.createCarUuid();
        car1UuidString = carUuid.toString();
        executePrepared("insert into ref_car(id, version, vin) " +
                        "values(?, ?, ?)",
                carUuid,
                1l,
                "001"
        );

        carUuid = dirtyData.createCarUuid();
        car2UuidString = carUuid.toString();
        executePrepared("insert into ref_car(id, version, vin) " +
                        "values(?, ?, ?)",
                carUuid,
                1l,
                "002"
        );

        UUID driverAllocUuid = dirtyData.createDriverAllocUuid();

        executePrepared("insert into ref_driver_alloc(id, car_id, driver_id) " +
                        "values(?, ?, ?)",
                driverAllocUuid,
                UUID.fromString(car1UuidString),
                UUID.fromString(driver1UuidString)
        );

        driverAllocUuid = dirtyData.createDriverAllocUuid();
        executePrepared("insert into ref_driver_alloc(id, car_id, driver_id) " +
                        "values(?, ?, ?)",
                driverAllocUuid,
                UUID.fromString(car1UuidString),
                UUID.fromString(driver2UuidString)
        );
    }

    private void createDbUsers() throws SQLException {
        UUID companyGroupId = dirtyData.createGroupUuid();
        executePrepared("insert into sample_rest_sec_group(id, version, name) " +
                        "values(?, ?, ?)",
                companyGroupId,
                1l,
                "Company"
        );

//        //can read cars, used for attributes access testing
//        driverReadUserId = dirtyData.createUserUuid();
//        executePrepared("insert into sample_rest_sec_user(id, version, login, group_id, login_lc) " +
//                        "values(?, ?, ?, ?, ?)",
//                driverReadUserId,
//                1l,
//                driverReadUserLogin,
//                companyGroupId, //"Company" group
//                driverReadUserLogin.toLowerCase()
//        );
//        executePrepared("insert into sample_rest_sec_user_role(id, version, user_id, role_name) " +
//                        "values(?, ?, ?, ?)",
//                UUID.randomUUID(),
//                1l,
//                driverReadUserId,
//                "rest-full-access"
//        );

        CoreUser driverRead = new CoreUser(driverReadUserLogin, "{noop}" + driverReadUserPassword, driverReadUserLogin.toLowerCase());
        userRepository.addUser(driverRead);
        RoleAssignmentEntity roleAssignmentEntity = metadata.create(RoleAssignmentEntity.class);
        roleAssignmentEntity.setRoleCode("system-full-access");
        roleAssignmentEntity.setUsername(driverRead.getUsername());
        roleAssignmentProvider.addAssignment(new RoleAssignment(driverRead.getUsername(), FullAccessRole.NAME));
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
