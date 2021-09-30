/*
 * Copyright 2021 Haulmont.
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

import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import io.jmix.samples.rest.service.RestTestService;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import test_support.AbstractRestControllerFT;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static test_support.RestTestUtils.*;

@TestPropertySource(properties = {"jmix.security.oauth2.dev-mode=true", "jmix.security.oauth2.dev-username=admin"})
public class EntitiesControllerDevModeFT extends AbstractRestControllerFT {
    protected String carUuidString;
    protected String colourUuidString;
    protected String modelUuidString;
    protected String carDocumentationUuidString;

    @Test
    void loadEntityByIdInTestMode() throws Exception {
        String url = baseUrl + "/entities/ref_Car/" + carUuidString;
        try (CloseableHttpResponse response = sendGet(url, null, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(carUuidString, ctx.read("$.id"));
            assertEquals("VWV000", ctx.read("$._instanceName"));

            try {
                ctx.read("&.colour");
                fail();
            } catch (PathNotFoundException ignored) {
            }

            try {
                ctx.read("&.model");
                fail();
            } catch (PathNotFoundException ignored) {
            }
        }
    }

    public void prepareDb() throws Exception {
        UUID colourId = dirtyData.createColourUuid();
        colourUuidString = colourId.toString();
        executePrepared("insert into ref_colour(id, name, description, version) values (?, ?, ?, 1)",
                colourId,
                "Colour 1",
                "Description 1");

        UUID modelId = dirtyData.createModelUuid();
        modelUuidString = modelId.toString();
        String modelName = "Audi A3";
        executePrepared("insert into ref_model(id, name, number_of_seats, DTYPE, version) values (?, ?, ?, ?, 1)",
                modelId, "Audi A3", 5, "ref$ExtModel");

        UUID carDocumentationUuid = dirtyData.createCarDocumentationUuid();
        carDocumentationUuidString = carDocumentationUuid.toString();
        executePrepared("insert into ref_car_documentation(id, version, title, create_ts) values(?, ?, ?, ?)",
                carDocumentationUuid,
                1L,
                "Car Doc 1",
                Timestamp.valueOf("2016-06-17 10:53:01")
        );

        UUID carUuid = dirtyData.createCarUuid();
        carUuidString = carUuid.toString();
        executePrepared("insert into ref_car(id, version, vin, colour_id, model_id, car_documentation_id, create_ts) values(?, ?, ?, ?, ?, ?, ?)",
                carUuid,
                1L,
                "VWV000",
                colourId,
                modelId,
                carDocumentationUuid,
                Timestamp.valueOf("2016-06-17 10:53:01")
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
