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

package test_support;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import static org.apache.http.HttpStatus.SC_OK;

/**
 * Utility class for the REST API functional tests
 */
public class RestTestUtils {

    public static final String CLIENT_ID = "client";
    public static final String CLIENT_SECRET = "secret";

    public static CloseableHttpResponse sendGet(String url, String token, @Nullable Map<String, String> params) throws Exception {
        URIBuilder uriBuilder = new URIBuilder(url);
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                uriBuilder.addParameter(entry.getKey(), entry.getValue());
            }
        }
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(uriBuilder.build());
        httpGet.setHeader("Authorization", "Bearer " + token);
        httpGet.setHeader("Accept-Language", "en");
        return httpClient.execute(httpGet);
    }

    public static CloseableHttpResponse sendGetWithHeaders(String url, String token, @Nullable Map<String, String> params, Map<String, String> headers) throws Exception {
        URIBuilder uriBuilder = new URIBuilder(url);
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                uriBuilder.addParameter(entry.getKey(), entry.getValue());
            }
        }
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(uriBuilder.build());
        httpGet.setHeader("Authorization", "Bearer " + token);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            httpGet.setHeader(entry.getKey(), entry.getValue());
        }
        return httpClient.execute(httpGet);
    }

    public static CloseableHttpResponse sendPost(String url, String token, String body, @Nullable Map<String, String> params) throws Exception {
        URIBuilder uriBuilder = new URIBuilder(url);
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                uriBuilder.addParameter(entry.getKey(), entry.getValue());
            }
        }
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(uriBuilder.build());
        StringEntity stringEntity = new StringEntity(body);
        httpPost.setEntity(stringEntity);
        httpPost.setHeader("Authorization", "Bearer " + token);
        return httpClient.execute(httpPost);
    }

    public static CloseableHttpResponse sendPut(String url, String token, String body, @Nullable Map<String, String> params) throws Exception {
        URIBuilder uriBuilder = new URIBuilder(url);
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                uriBuilder.addParameter(entry.getKey(), entry.getValue());
            }
        }
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPut httpPut = new HttpPut(uriBuilder.build());
        StringEntity stringEntity = new StringEntity(body);
        httpPut.setEntity(stringEntity);
        httpPut.setHeader("Authorization", "Bearer " + token);
        return httpClient.execute(httpPut);
    }

    public static CloseableHttpResponse sendPutWithHeaders(String url, String token, String body, @Nullable Map<String, String> params, Map<String, String> headers) throws Exception {
        URIBuilder uriBuilder = new URIBuilder(url);
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                uriBuilder.addParameter(entry.getKey(), entry.getValue());
            }
        }
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPut httpPut = new HttpPut(uriBuilder.build());
        StringEntity stringEntity = new StringEntity(body);
        httpPut.setEntity(stringEntity);
        httpPut.setHeader("Authorization", "Bearer " + token);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            httpPut.setHeader(entry.getKey(), entry.getValue());
        }
        return httpClient.execute(httpPut);
    }

    public static CloseableHttpResponse sendDelete(String url, String token, @Nullable Map<String, String> params) throws Exception {
        URIBuilder uriBuilder = new URIBuilder(url);
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                uriBuilder.addParameter(entry.getKey(), entry.getValue());
            }
        }
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpDelete httpDelete = new HttpDelete(uriBuilder.build());
        httpDelete.setHeader("Authorization", "Bearer " + token);
        return httpClient.execute(httpDelete);
    }

    public static String getAuthToken(String baseUrl, String login, String password) throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept-Language", "en");
        return getAuthToken(baseUrl, login, password, headers);
    }

    public static String getAuthToken(String baseUrl, String login, String password, Map<String, String> headers) throws IOException {
        String uri = baseUrl + "/oauth/token";

        CloseableHttpClient httpClient = HttpClients.createDefault();
        String encoding = Base64.getEncoder().encodeToString((CLIENT_ID + ":" + CLIENT_SECRET).getBytes());
        HttpPost httpPost = new HttpPost(uri);
        httpPost.setHeader("Authorization", "Basic " + encoding);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            httpPost.setHeader(entry.getKey(), entry.getValue());
        }

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("grant_type", "password"));
        urlParameters.add(new BasicNameValuePair("username", login));
        urlParameters.add(new BasicNameValuePair("password", password));

        httpPost.setEntity(new UrlEncodedFormEntity(urlParameters));

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != SC_OK) {
                throw new AuthException(statusCode);
            }
            ReadContext ctx = parseResponse(response);
            return ctx.read("$.access_token");
        }
    }

    public static ReadContext parseResponse(CloseableHttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        String s = EntityUtils.toString(entity);
        return JsonPath.parse(s);
    }

    public static String responseToString(CloseableHttpResponse response) throws IOException {
        return EntityUtils.toString(response.getEntity());
    }

    public static int statusCode(CloseableHttpResponse response) {
        return response.getStatusLine().getStatusCode();
    }

    public static String responseContentType(CloseableHttpResponse response) {
        return response.getEntity().getContentType().getValue();
    }

    public static String getFileContent(String fileName, @Nullable Map<String, String> replacements) throws IOException {
        File resource = new ClassPathResource("test_support/data/service/" + fileName).getFile();
        String fileContent = new String(Files.readAllBytes(resource.toPath()));
        if (replacements != null) {
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                fileContent = fileContent.replace(entry.getKey(), entry.getValue());
            }
        }
        return fileContent;
    }


    public static class AuthException extends RuntimeException {

        private int statusCode;

        public AuthException(int statusCode) {
            this.statusCode = statusCode;
        }
    }
}
