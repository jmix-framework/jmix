/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package io.jmix.samples.rest.tests;


import org.junit.jupiter.api.Disabled;


/**
 *
 */
@Disabled
public class PermissionsControllerFT extends AbstractRestControllerFT {

//    private static final String DB_URL = "jdbc:hsqldb:hsql://localhost:9010/rest_demo";
//    private static EncryptionModule encryption = new BCryptEncryptionModule();
//    private Connection conn;
//    private DataSet dirtyData = new DataSet();
//    private String oauthToken;
//    private UUID roleId;
//    private UUID groupUuid = UUID.fromString("0fa2b1a5-1d68-4d69-9fbd-dff348347f93");
//    private String testUserLogin = "testUser";
//    private String testUserPassword = "test";
//
//
//    @Before
//    public void setUp() throws Exception {
//        Class.forName("org.hsqldb.jdbc.JDBCDriver");
//        conn = DriverManager.getConnection(DB_URL, "sa", "");
//        prepareDb();
//        oauthToken = getAuthToken(baseUrl, testUserLogin, testUserPassword);
//    }
//
//    @After
//    public void tearDown() throws SQLException {
//        dirtyData.cleanup(conn);
//
//        if (conn != null)
//            conn.close();
//    }
//
//    @Test
//    public void getPermissions() throws Exception {
//        String url = "/permissions";
//        try (CloseableHttpResponse response = sendGet(url, oauthToken, null)) {
//            assertEquals(HttpStatus.SC_OK, statusCode(response));
//            ReadContext ctx = parseResponse(response);
//            assertTrue(ctx.<Collection>read("$").size() > 0);
//            assertEquals("MODIFY", ctx.<Collection>read("$[?(@.target == 'ref$Currency:name')].value").iterator().next());
//            assertEquals("DENY", ctx.<Collection>read("$[?(@.target == 'ref_Car:update')].value").iterator().next());
//        }
//    }
//
//    @Test
//    public void getEffectiveEntitiesPermissions() throws Exception {
//        String url = "/permissions/effective?entities=true";
//        try (CloseableHttpResponse response = sendGet(url, oauthToken, null)) {
//            assertEquals(HttpStatus.SC_OK, statusCode(response));
//            ReadContext ctx = parseResponse(response);
//            assertEquals(2, ctx.<Collection>read("$.explicitPermissions.entities").size());
//            assertEquals(0, (int) ctx.read("$.explicitPermissions.entities[?(@.target == 'ref_Car:update')].value", List.class).get(0));
//            assertEquals(1, (int) ctx.read("$.explicitPermissions.entities[?(@.target == '*:read')].value", List.class).get(0));
//            assertEquals("DENY", ctx.read("$.undefinedPermissionPolicy"));
//        }
//    }
//
//    @Test
//    public void getEffectiveAttributesPermissions() throws Exception {
//        String url = "/permissions/effective?entityAttributes=true";
//        try (CloseableHttpResponse response = sendGet(url, oauthToken, null)) {
//            assertEquals(HttpStatus.SC_OK, statusCode(response));
//            ReadContext ctx = parseResponse(response);
//            assertEquals(1, ctx.<Collection>read("$.explicitPermissions.entityAttributes").size());
//            assertEquals("ref$Currency:name", ctx.read("$.explicitPermissions.entityAttributes[0].target"));
//            assertEquals(2, (int) ctx.read("$.explicitPermissions.entityAttributes[0].value"));
//            assertEquals("DENY", ctx.read("$.undefinedPermissionPolicy"));
//        }
//    }
//
//    private void prepareDb() throws SQLException {
//
//        UUID testUserId = dirtyData.createUserUuid();
//        String pwd = encryption.getPasswordHash(testUserId, testUserPassword);
//        executePrepared("insert into sec_user(id, version, login, password, password_encryption, group_id, login_lc) " +
//                        "values(?, ?, ?, ?, ?, ?, ?)",
//                testUserId,
//                1l,
//                testUserLogin,
//                pwd,
//                encryption.getHashMethod(),
//                groupUuid, //"Company" group
//                testUserLogin.toLowerCase()
//        );
//
//        roleId = dirtyData.createRoleUuid();
//        executePrepared("insert into sec_role(id, role_type, name, security_scope)" +
//                        " values(?, ?, ?, ?)",
//                roleId,
//                RoleType.READONLY.getId(),
//                "testRole",
//                "REST"
//        );
//
//        int ALLOW = 1;
//        int DENY = 0;
//        int PROPERTY_MODIFY = 2;
//
//        //rest api enabled
//        UUID restApiEnabledPrmsId = dirtyData.createPermissionUuid();
//        executePrepared("insert into sec_permission(id, role_id, permission_type, target, value_) values(?, ?, ?, ?, ?)",
//                restApiEnabledPrmsId,
//                roleId,
//                PermissionType.SPECIFIC.getId(),
//                "cuba.restApi.enabled",
//                ALLOW
//        );
//
//        //testRole forbids to update cars
//        UUID cantUpdateCarPrmsId = dirtyData.createPermissionUuid();
//        executePrepared("insert into sec_permission(id, role_id, permission_type, target, value_) values(?, ?, ?, ?, ?)",
//                cantUpdateCarPrmsId,
//                roleId,
//                PermissionType.ENTITY_OP.getId(),
//                "ref_Car:update",
//                DENY
//        );
//
//        executePrepared("insert into sec_permission(id, role_id, permission_type, target, value_) values(?, ?, ?, ?, ?)",
//                dirtyData.createPermissionUuid(),
//                roleId,
//                PermissionType.ENTITY_OP.getId(),
//                "*:read",
//                ALLOW
//        );
//
//        //testRole forbids currencies browser screen
//        UUID currenciesScreenPrmsId = dirtyData.createPermissionUuid();
//        executePrepared("insert into sec_permission(id, role_id, permission_type, target, value_) values(?, ?, ?, ?, ?)",
//                currenciesScreenPrmsId,
//                roleId,
//                PermissionType.SCREEN.getId(),
//                "ref$Currency.browse",
//                DENY
//        );
//
//        executePrepared("insert into sec_permission(id, role_id, permission_type, target, value_) values(?, ?, ?, ?, ?)",
//                dirtyData.createPermissionUuid(),
//                roleId,
//                PermissionType.ENTITY_ATTR.getId(),
//                "ref$Currency:name",
//                PROPERTY_MODIFY
//        );
//
//        UUID id = UUID.randomUUID();
//        //colorReadUser has colorReadRole role (read-only)
//        executePrepared("insert into sec_user_role(id, user_id, role_id) values(?, ?, ?)",
//                id,
//                testUserId,
//                roleId
//        );
//    }
//
//    private void executePrepared(String sql, Object... params) throws SQLException {
//        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
//            for (int i = 0; i < params.length; i++) {
//                stmt.setObject(i + 1, params[i]);
//            }
//            stmt.executeUpdate();
//        }
//    }

}
