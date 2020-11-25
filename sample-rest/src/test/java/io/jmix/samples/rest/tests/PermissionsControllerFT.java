/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package io.jmix.samples.rest.tests;


import com.jayway.jsonpath.ReadContext;
import io.jmix.samples.rest.security.PermissionRole;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

import static io.jmix.samples.rest.tools.RestSpecsUtils.getAuthToken;
import static io.jmix.samples.rest.tools.RestTestUtils.*;
import static io.jmix.security.authentication.RoleGrantedAuthority.ofRoles;
import static org.junit.jupiter.api.Assertions.assertEquals;


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
                .authorities(ofRoles(roleRepository::getRoleByCode, PermissionRole.NAME))
                .build();

        userRepository.addUser(user);

        oauthToken = getAuthToken(baseUrl, userLogin, userPassword);
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
