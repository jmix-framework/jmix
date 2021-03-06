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

import com.jayway.jsonpath.ReadContext;
import io.jmix.core.security.impl.CoreUser;
import io.jmix.core.security.impl.InMemoryUserRepository;
import io.jmix.samples.rest.SampleRestApplication;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static io.jmix.samples.rest.tools.RestTestUtils.*;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SampleRestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OAuthTokenFT {

    @LocalServerPort
    private int port;

    private String baseUrl;

    @Autowired
    protected InMemoryUserRepository userRepository;

    protected CoreUser admin;

    @Before
    public void setUp() {
        admin = new CoreUser("admin", "{noop}admin123", "Admin");
        userRepository.createUser(admin);
        baseUrl = "http://localhost:" + port + "/rest";
    }

    @After
    public void tearDown() {
        userRepository.removeUser(admin);
    }

    @Test
    public void getToken() throws IOException {
        String token = getAuthToken(baseUrl, "admin", "admin123");
        assertNotNull(token);
    }

    @Test
    public void requestTokenWithoutAuthentication() throws Exception {
        String uri = baseUrl + "/oauth/token";

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
        String uri = baseUrl + "/oauth/token";

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
//            assertEquals("Bad credentials", ctx.read("$.error_description"));
        }
    }

    @Test
    public void requestTokenWithInvalidUserCredentials() throws Exception {
        String uri = baseUrl + "/oauth/token";

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
            //assertEquals("Bad credentials", ctx.read("$.error_description"));
        }
    }

//    @Test
//    public void revokeToken() throws Exception {
//        String oauthToken = getAuthToken("admin", "admin");
//        String resourceUrl = "/entities/ref_Car";
//        try (CloseableHttpResponse response = sendGet(resourceUrl, oauthToken, null)) {
//            assertEquals(HttpStatus.SC_OK, statusCode(response));
//        }
//
//        String revokeUrl = URI_BASE + "/oauth/revoke";
//
//        CloseableHttpClient httpClient = HttpClients.createDefault();
//        String encoding = Base64.getEncoder().encodeToString((CLIENT_ID + ":" + CLIENT_SECRET).getBytes());
//        HttpPost httpPost = new HttpPost(revokeUrl);
//        httpPost.setHeader("Authorization", "Basic " + encoding);
//
//        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
//        urlParameters.add(new BasicNameValuePair("token", oauthToken));
//
//        httpPost.setEntity(new UrlEncodedFormEntity(urlParameters));
//
//        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
//            int statusCode = statusCode(response);
//            assertEquals(SC_OK, statusCode);
//        }
//
//        try (CloseableHttpResponse response = sendGet(resourceUrl, oauthToken, null)) {
//            assertEquals(SC_UNAUTHORIZED, statusCode(response));
//        }
//    }

//    @Test
//    public void revokeTokenWithoutAuthorization() throws Exception {
//        String oauthToken = getAuthToken(getBaseUrl(), "admin", "admin123");
//        String resourceUrl = "/entities/ref_Car";
//        try (CloseableHttpResponse response = sendGet(resourceUrl, oauthToken, null)) {
//            assertEquals(HttpStatus.SC_OK, statusCode(response));
//        }
//
//        String revokeUrl = URI_BASE + "/oauth/revoke";
//
//        CloseableHttpClient httpClient = HttpClients.createDefault();
//        HttpPost httpPostRevoke = new HttpPost(revokeUrl);
//
//        List<NameValuePair> urlParameters = new ArrayList<>();
//        urlParameters.add(new BasicNameValuePair("token", oauthToken));
//
//        httpPostRevoke.setEntity(new UrlEncodedFormEntity(urlParameters));
//
//        try (CloseableHttpResponse response = httpClient.execute(httpPostRevoke)) {
//            int statusCode = statusCode(response);
//            assertEquals(SC_UNAUTHORIZED, statusCode);
//        }
//
//        try (CloseableHttpResponse response = sendGet(resourceUrl, oauthToken, null)) {
//            assertEquals(HttpStatus.SC_OK, statusCode(response));
//        }
//    }
}
