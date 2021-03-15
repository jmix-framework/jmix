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

package permissions;


import com.jayway.jsonpath.ReadContext;
import io.jmix.samples.rest.security.PermissionRole;
import io.jmix.security.authentication.RoleGrantedAuthority;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import test_support.AbstractRestControllerFT;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static test_support.RestTestUtils.*;


/**
 *
 */
public class PermissionsControllerFT extends AbstractRestControllerFT {

    private String userLogin = "testUser";
    private String userPassword = "test";
    private UserDetails user;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        user = User.builder()
                .username(userLogin)
                .password("{noop}" + userPassword)
                .authorities(
                        RoleGrantedAuthority.withResourceRoleProvider(resourceRoleRepository::getRoleByCode)
                                .withResourceRoles(PermissionRole.NAME)
                                .build())
                .build();

        userRepository.addUser(user);

        oauthToken = getAuthToken(oauthUrl, userLogin, userPassword);
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
        userRepository.removeUser(user);
    }

    @Test
    public void getEntitiesPermissions() throws Exception {
        String url = baseUrl + "/permissions?entities=true";
        try (CloseableHttpResponse response = sendGet(url, oauthToken, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(2, ctx.<Collection>read("$.entities").size());
            assertEquals(1, (int) ctx.read("$.entities[?(@.target == 'ref_Car:update')].value", List.class).get(0));
            assertEquals(1, (int) ctx.read("$.entities[?(@.target == 'ref$Currency:read')].value", List.class).get(0));
        }
    }

    @Test
    public void getEntityAttributesPermissions() throws Exception {
        String url = baseUrl + "/permissions?entityAttributes=true";
        try (CloseableHttpResponse response = sendGet(url, oauthToken, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(1, ctx.<Collection>read("$.entityAttributes").size());
            assertEquals("ref$Currency:name", ctx.read("$.entityAttributes[0].target"));
            assertEquals(2, (int) ctx.read("$.entityAttributes[0].value"));
        }
    }
}
