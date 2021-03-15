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

package entities;

import com.jayway.jsonpath.ReadContext;
import io.jmix.samples.rest.security.FullAccessRole;
import io.jmix.samples.rest.security.InMemoryRowLevelRole;
import io.jmix.security.authentication.RoleGrantedAuthority;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.TestPropertySource;
import test_support.AbstractRestControllerFT;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static test_support.RestTestUtils.*;

/**
 *
 */
@TestPropertySource(properties = {
        "jmix.core.entitySerializationTokenRequired = true"
})
public class RowLevelSecurityFT extends AbstractRestControllerFT {

    private UUID carId, newCarId;
    private UUID insuranceCase1Id;
    private UUID insuranceCase2Id;
    private UUID plantId;
    private UUID model1Id;
    private UUID model2Id;

    private String userPassword = "rowLevelUser123";
    private String userLogin = "rowLevelUser";
    private String userToken;
    private UserDetails user;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        prepareDb();
        createUsers();

        userToken = getAuthToken(oauthUrl, userLogin, userPassword);
    }

    @AfterEach
    public void tearDown() throws SQLException {
        userRepository.removeUser(user);
        dirtyData.cleanup(conn);
        if (conn != null)
            conn.close();
    }

    @Test
    public void testCreateAndUpdateCarWithToken() throws Exception {
        newCarId = dirtyData.createCarUuid();

        Map<String, String> replacements = new HashMap<>();
        replacements.put("$CAR_ID$", newCarId.toString());

        String createJson = getFileContent("rowLevelSecCreateCar.json", replacements);
        String createUrl = baseUrl + "/entities/ref_Car/";
        try (CloseableHttpResponse response = sendPost(createUrl, userToken, createJson, null)) {
            assertEquals(HttpStatus.SC_CREATED, statusCode(response));
        }

        try (PreparedStatement stmt = conn.prepareStatement("select count(*) from REF_CAR where id = ? and delete_ts is null")) {
            stmt.setObject(1, newCarId);
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            int count = rs.getInt(1);
            assertEquals(1, count);
        }

        String updateJson = getFileContent("rowLevelSecUpdateCar.json", replacements);
        String url = baseUrl + "/entities/ref_Car/" + newCarId.toString();
        try (CloseableHttpResponse response = sendPut(url, userToken, updateJson, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
        }

        try (PreparedStatement stmt = conn.prepareStatement("select vin from REF_CAR where id = ? and delete_ts is null")) {
            stmt.setObject(1, newCarId);
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            assertEquals("vin123_1", rs.getString(1));
        }
    }

    /**
     * If entity has a composition collection and one of the items is hidden by the row-level security,
     * it must not be deleted if it is not passed in JSON for update
     */
    @Test
    public void testOneToManyComposition() throws Exception {
        String url = baseUrl + "/entities/ref_Car/" + carId.toString();
        Map<String, String> params = new HashMap<>();
        params.put("view", "carWithInsuranceCases");
        String securityToken;
        try (CloseableHttpResponse response = sendGet(url, userToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            //only one driver should be returned
            assertEquals((int) 1, (int) ctx.read("$.insuranceCases.length()", Integer.class));
            assertEquals("AAA", ctx.read("$.insuranceCases[0].description"));
            securityToken = ctx.read("$.__securityToken");
            assertNotNull(securityToken);
        }

        Map<String, String> replacements = new HashMap<>();
        replacements.put("$CAR_ID$", carId.toString());
        replacements.put("$INSURANCE_CASE_ID$", insuranceCase1Id.toString());
        replacements.put("$SECURITY_TOKEN$", securityToken);

        //we pass only 1 item for update
        String updateJson = getFileContent("rowLevelSecOneToManyComposition.json", replacements);
        String updateUrl = baseUrl + "/entities/ref_Car/" + carId;
        try (CloseableHttpResponse response = sendPut(updateUrl, userToken, updateJson, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
        }

        //the second element of insuranceCases collection must not be deleted
        try (PreparedStatement stmt = conn.prepareStatement("select count(*) from REF_INSURANCE_CASE where CAR_ID = ? and DELETE_TS is null")) {
            stmt.setObject(1, carId);
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            int count = rs.getInt(1);
            assertEquals(2, count);
        }

        try (PreparedStatement stmt = conn.prepareStatement("select DESCRIPTION, DELETED_BY, CAR_ID from REF_INSURANCE_CASE where CAR_ID = ? order by DESCRIPTION")) {
            stmt.setObject(1, carId);
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            String description = rs.getString("DESCRIPTION");
            String deletedBy = rs.getString("DELETED_BY");
            UUID _carId = UUID.fromString(rs.getString("CAR_ID"));
            assertEquals("AAA", description);
            assertNull(deletedBy);
            assertEquals(carId, _carId);

            assertTrue(rs.next());
            description = rs.getString("DESCRIPTION");
            deletedBy = rs.getString("DELETED_BY");
            _carId = UUID.fromString(rs.getString("CAR_ID"));
            assertEquals("BBB", description);
            assertNull(deletedBy);
            assertEquals(carId, _carId);
        }
    }


    /**
     * If entity has a composition collection and one of the items is hidden by the row-level security,
     * it must not be deleted if it is not passed in JSON for update
     */
    @Test
    public void testOneToManyCompositionWithoutSecurityToken() throws Exception {
        String url = baseUrl + "/entities/ref_Car/" + carId.toString();
        Map<String, String> params = new HashMap<>();
        params.put("view", "carWithInsuranceCases");
        String securityToken;
        try (CloseableHttpResponse response = sendGet(url, userToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            //only one driver should be returned
            assertEquals((int) 1, (int) ctx.read("$.insuranceCases.length()", Integer.class));
            assertEquals("AAA", ctx.read("$.insuranceCases[0].description"));
            securityToken = ctx.read("$.__securityToken");
            assertNotNull(securityToken);
        }

        Map<String, String> replacements = new HashMap<>();
        replacements.put("$CAR_ID$", carId.toString());
        replacements.put("$INSURANCE_CASE_ID$", insuranceCase1Id.toString());
        replacements.put("$SECURITY_TOKEN$", securityToken);

        //we pass only 1 item for update
        String updateJson = getFileContent("rowLevelSecOneToManyCompositionWithoutSecurityToken.json", replacements);
        String updateUrl = baseUrl + "/entities/ref_Car/" + carId;
        try (CloseableHttpResponse response = sendPut(updateUrl, userToken, updateJson, null)) {
            assertEquals(HttpStatus.SC_FORBIDDEN, statusCode(response));
        }
    }

    /**
     * If entity has a many-to-many collection and one of the items is hidden by the row-level security,
     * it must not be deleted if it is not passed in JSON for update
     */
    @Test
    public void testManyToMany() throws Exception {
        String url = baseUrl + "/entities/ref$Plant/" + plantId;
        Map<String, String> params = new HashMap<>();
        params.put("view", "plantWithModels");
        String securityToken;
        try (CloseableHttpResponse response = sendGet(url, userToken, params)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            //only one driver should be returned
            assertEquals((int) 1, (int) ctx.read("$.models.length()", Integer.class));
            assertEquals("AAA", ctx.read("$.models[0].name"));
            securityToken = ctx.read("$.__securityToken");
            assertNotNull(securityToken);
        }

        Map<String, String> replacements = new HashMap<>();
        replacements.put("$PLANT_ID$", plantId.toString());
        replacements.put("$MODEL_ID$", model1Id.toString());
        replacements.put("$SECURITY_TOKEN$", securityToken);

        //we pass only 1 item for update
        String updateJson = getFileContent("rowLevelSecManyToMany.json", replacements);
        String updateUrl = baseUrl + "/entities/ref$Plant/" + plantId;
        try (CloseableHttpResponse response = sendPut(updateUrl, userToken, updateJson, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
        }

        //the second element of models collection must not be deleted
        try (PreparedStatement stmt = conn.prepareStatement("select count(*) from REF_PLANT_MODEL_LINK where plant_id = ?")) {
            stmt.setObject(1, plantId);
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            int count = rs.getInt(1);
            assertEquals(2, count);
        }

        try (PreparedStatement stmt = conn.prepareStatement("select count (*) from REF_MODEL m join REF_PLANT_MODEL_LINK l on l.model_id = m.id where l.plant_id = ? and delete_ts is null")) {
            stmt.setObject(1, plantId);
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            int count = rs.getInt(1);
            assertEquals(2, count);
        }

    }

    protected void createUsers() {
        user = User.builder()
                .username(userLogin)
                .password("{noop}" + userPassword)
                .authorities(RoleGrantedAuthority.withResourceRoleProvider(resourceRoleRepository::getRoleByCode)
                        .withRowLevelRoleProvider(rowLevelRoleRepository::getRoleByCode)
                        .withResourceRoles(FullAccessRole.NAME)
                        .withRowLevelRoles(InMemoryRowLevelRole.NAME)
                        .build())
                .build();

        userRepository.addUser(user);
    }

    public void prepareDb() throws Exception {
        Class.forName("org.hsqldb.jdbc.JDBCDriver");
        conn = DriverManager.getConnection(DB_URL, "sa", "");

        carId = dirtyData.createCarUuid();
        executePrepared("insert into ref_car(id, version, vin) " +
                        "values(?, ?, ?)",
                carId,
                1l,
                "001"
        );

        insuranceCase1Id = dirtyData.createInsuranceCaseUuid();
        executePrepared("insert into ref_insurance_case(id, version, description, car_id) " +
                        "values(?, ?, ?, ?)",
                insuranceCase1Id,
                1l,
                "AAA",
                carId
        );

        insuranceCase2Id = dirtyData.createInsuranceCaseUuid();
        executePrepared("insert into ref_insurance_case(id, version, description, car_id) " +
                        "values(?, ?, ?, ?)",
                insuranceCase2Id,
                1l,
                "BBB",
                carId
        );

        plantId = dirtyData.createPlantUuid();
        executePrepared("insert into ref_plant(id, version, name) " +
                        "values(?, ?, ?)",
                plantId,
                1l,
                "Plant1"
        );

        model1Id = dirtyData.createModelUuid();
        executePrepared("insert into ref_model(id, version, name, dtype) " +
                        "values(?, ?, ?, ?)",
                model1Id,
                1l,
                "AAA",
                "ref$ExtModel"
        );

        model2Id = dirtyData.createModelUuid();
        executePrepared("insert into ref_model(id, version, name, dtype) " +
                        "values(?, ?, ?, ?)",
                model2Id,
                1l,
                "BBB",
                "ref$ExtModel"
        );

        executePrepared("insert into ref_plant_model_link(plant_id, model_id) " +
                        "values(?, ?)",
                plantId,
                model1Id
        );

        executePrepared("insert into ref_plant_model_link(plant_id, model_id) " +
                        "values(?, ?)",
                plantId,
                model2Id
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
