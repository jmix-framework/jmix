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

package oauth_token;

import com.jayway.jsonpath.ReadContext;
import io.jmix.core.CoreConfiguration;
import io.jmix.core.security.InMemoryUserRepository;
import io.jmix.data.DataConfiguration;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.rest.RestConfiguration;
import io.jmix.samples.rest.SampleRestApplication;
import io.jmix.samples.rest.security.FullAccessRole;
import io.jmix.security.SecurityConfiguration;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.security.role.RoleGrantedAuthorityUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import test_support.JmixRestTestConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static test_support.RestTestUtils.*;

@ContextConfiguration(classes = {
        CoreConfiguration.class,
        DataConfiguration.class,
        EclipselinkConfiguration.class,
        SecurityConfiguration.class,
        RestConfiguration.class,
        JmixRestTestConfiguration.class})
@SpringBootTest(classes = SampleRestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Disabled //todo [jmix-framework/jmix#3758]
public class OAuthTokenFT {

    @LocalServerPort
    private int port;

    private String oauthUrl;
    private String baseUrl;

    @Autowired
    protected InMemoryUserRepository userRepository;

    @Autowired
    protected ResourceRoleRepository roleRepository;


    @Autowired
    protected RoleGrantedAuthorityUtils roleGrantedAuthorityUtils;

    protected UserDetails admin;

    @BeforeEach
    public void setUp() {
        admin = User.builder()
                .username("admin")
                .password("{noop}admin123")
                .authorities(roleGrantedAuthorityUtils.createResourceRoleGrantedAuthority(FullAccessRole.NAME))
                .build();

        userRepository.addUser(admin);

        oauthUrl = "http://localhost:" + port + "/";
        baseUrl = "http://localhost:" + port + "/rest";

    }

    @AfterEach
    public void tearDown() {
        userRepository.removeUser(admin);
    }

    @Test
    public void getToken() throws IOException {
        String token = getAuthToken(oauthUrl, "admin", "admin123");
        assertNotNull(token);
    }

    @Test
    public void requestTokenWithoutAuthentication() throws Exception {
        String uri = oauthUrl + "/oauth/token";

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(uri);

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("grant_type", "password"));
        urlParameters.add(new BasicNameValuePair("username", "admin"));
        urlParameters.add(new BasicNameValuePair("password", "admin123"));

        httpPost.setEntity(new UrlEncodedFormEntity(urlParameters));

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            int statusCode = statusCode(response);
            assertEquals(SC_UNAUTHORIZED, statusCode);
            ReadContext ctx = parseResponse(response);
            assertEquals("Unauthorized", ctx.read("$.error"));
        }
    }

    @Test
    public void requestTokenWithInvalidClientCredentials() throws Exception {
        String uri = oauthUrl + "/oauth2/token";

        CloseableHttpClient httpClient = HttpClients.createDefault();
        String encoding = Base64.getEncoder().encodeToString(("invalidClient:invalidPassword").getBytes());
        HttpPost httpPost = new HttpPost(uri);
        httpPost.setHeader("Authorization", "Basic " + encoding);

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("grant_type", "password"));
        urlParameters.add(new BasicNameValuePair("username", "admin"));
        urlParameters.add(new BasicNameValuePair("password", "admin"));

        httpPost.setEntity(new UrlEncodedFormEntity(urlParameters));

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            int statusCode = statusCode(response);
            assertEquals(SC_UNAUTHORIZED, statusCode);
            ReadContext ctx = parseResponse(response);
            assertEquals("Unauthorized", ctx.read("$.error"));
        }
    }

    @Test
    public void requestTokenWithInvalidUserCredentials() throws Exception {
        String uri = oauthUrl + "/oauth/token";

        CloseableHttpClient httpClient = HttpClients.createDefault();
        String encoding = Base64.getEncoder().encodeToString(("cuba:cuba").getBytes());
        HttpPost httpPost = new HttpPost(uri);
        httpPost.setHeader("Authorization", "Basic " + encoding);

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("grant_type", "password"));
        urlParameters.add(new BasicNameValuePair("username", "admin"));
        urlParameters.add(new BasicNameValuePair("password", "admin1"));

        httpPost.setEntity(new UrlEncodedFormEntity(urlParameters));

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            int statusCode = statusCode(response);
            assertEquals(SC_UNAUTHORIZED, statusCode);
            ReadContext ctx = parseResponse(response);
            assertEquals("Unauthorized", ctx.read("$.error"));
        }
    }

    @Test
    public void revokeToken() throws Exception {
        String oauthToken = getAuthToken(oauthUrl, "admin", "admin123");
        String resourceUrl = baseUrl + "/entities/ref_Car";
        try (CloseableHttpResponse response = sendGet(resourceUrl, oauthToken, null)) {
            assertEquals(SC_OK, statusCode(response));
        }

        String revokeUrl = oauthUrl + "/oauth/revoke";

        CloseableHttpClient httpClient = HttpClients.createDefault();
        String encoding = Base64.getEncoder().encodeToString((CLIENT_ID + ":" + CLIENT_SECRET).getBytes());
        HttpPost httpPost = new HttpPost(revokeUrl);
        httpPost.setHeader("Authorization", "Basic " + encoding);

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("token", oauthToken));

        httpPost.setEntity(new UrlEncodedFormEntity(urlParameters));

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            int statusCode = statusCode(response);
            assertEquals(SC_OK, statusCode);
        }

        try (CloseableHttpResponse response = sendGet(resourceUrl, oauthToken, null)) {
            assertEquals(SC_UNAUTHORIZED, statusCode(response));
        }
    }

    @Test
    public void revokeTokenWithoutAuthorization() throws Exception {
        String oauthToken = getAuthToken(oauthUrl, "admin", "admin123");
        String resourceUrl = baseUrl + "/entities/ref_Car";
        try (CloseableHttpResponse response = sendGet(resourceUrl, oauthToken, null)) {
            assertEquals(SC_OK, statusCode(response));
        }

        String revokeUrl = oauthUrl + "/oauth/revoke";

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPostRevoke = new HttpPost(revokeUrl);

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("token", oauthToken));

        httpPostRevoke.setEntity(new UrlEncodedFormEntity(urlParameters));

        try (CloseableHttpResponse response = httpClient.execute(httpPostRevoke)) {
            int statusCode = statusCode(response);
            assertEquals(SC_UNAUTHORIZED, statusCode);
        }

        try (CloseableHttpResponse response = sendGet(resourceUrl, oauthToken, null)) {
            assertEquals(SC_OK, statusCode(response));
        }
    }
}
