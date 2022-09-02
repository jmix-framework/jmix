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

package files;

import com.jayway.jsonpath.ReadContext;
import io.jmix.core.common.util.URLEncodeUtils;
import test_support.AbstractRestControllerFT;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

import static test_support.RestTestUtils.parseResponse;
import static test_support.RestTestUtils.statusCode;
import static org.junit.jupiter.api.Assertions.*;

public class FilesControllerFT extends AbstractRestControllerFT {

    protected static final String AUTHORIZATION = "Authorization";

    protected static final String INVALID_FILE_REFERENCE = "invalid_file_reference";

    protected static final String FILE_TO_UPLOAD_TXT = "fileToUpload.txt";
    protected static final String TEST_FILE_PDF = "test-file.pdf";

    @Test
    public void uploadFile() throws Exception {
        URIBuilder uriBuilder = new URIBuilder(baseUrl + "/files");
        uriBuilder.addParameter("name", FILE_TO_UPLOAD_TXT);

        InputStreamEntity entity = new InputStreamEntity(getFileStream(FILE_TO_UPLOAD_TXT));

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = getHttpPost(uriBuilder, entity);

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            assertEquals(HttpStatus.SC_CREATED, response.getStatusLine().getStatusCode());

            ReadContext ctx = parseResponse(response);

            String fileURI = ctx.read("$.fileRef");

            assertEquals(FILE_TO_UPLOAD_TXT, ctx.read("$.name"));
            assertEquals(0, (int) ctx.read("$.size"));
            assertNotNull(fileURI);

            checkLocation(response, fileURI);
        }
    }

    @Test
    public void uploadFileWithoutName() throws Exception {
        URIBuilder uriBuilder = new URIBuilder(baseUrl + "/files");

        InputStreamEntity entity = new InputStreamEntity(getFileStream(FILE_TO_UPLOAD_TXT));

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = getHttpPost(uriBuilder, entity);

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            ReadContext ctx = parseResponse(response);

            String fileURI = ctx.read("$.fileRef");

            assertNotNull(fileURI);
            assertEquals(0, (int) ctx.read("$.size"));

            checkLocation(response, fileURI);
        }
    }

    @Test
    @Disabled
    public void uploadFileWithId() throws Exception {
        String id = UUID.randomUUID().toString();

        URIBuilder uriBuilder = new URIBuilder(baseUrl + "/files");
        uriBuilder.addParameter("name", FILE_TO_UPLOAD_TXT);
        uriBuilder.addParameter("id", id);

        InputStreamEntity entity = new InputStreamEntity(getFileStream(FILE_TO_UPLOAD_TXT));

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = getHttpPost(uriBuilder, entity);

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            assertEquals(HttpStatus.SC_CREATED, response.getStatusLine().getStatusCode());

            ReadContext ctx = parseResponse(response);
            String fileURI = ctx.read("$.fileRef");

            assertEquals(id, fileURI);
            assertEquals(FILE_TO_UPLOAD_TXT, ctx.read("$.name"));
            assertEquals(0, (int) ctx.read("$.size"));

            checkLocation(response, id);
        }
    }

    @Test
    public void downloadFile() throws Exception {
        String fileRef = _uploadFile();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = getHttpGet(fileRef);

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
            assertEquals("application/pdf", response.getFirstHeader("Content-Type").getValue());
            assertEquals("no-cache", response.getFirstHeader("Cache-Control").getValue());
            assertEquals("inline; filename=\"test%2dfile.pdf\"; filename*=utf-8''test%2dfile.pdf",
                    response.getFirstHeader("Content-Disposition").getValue());
            byte[] fileContent = EntityUtils.toByteArray(response.getEntity());
            assertTrue(fileContent.length > 0);
        }
    }

    @Test
    public void downloadMissingFile() throws Exception {
        String fileId = UUID.randomUUID().toString();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = getHttpGet(String.format("fs://2020/11/16/%s.txt?name=fileToUpload.txt", fileId));
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
            ReadContext ctx = parseResponse(response);
            assertEquals("File not found", ctx.read("$.error"));
        }
    }

    @Test
    public void downloadFileWithInvalidId() throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = getHttpGet(INVALID_FILE_REFERENCE);
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals("Invalid file reference", ctx.read("$.error"));
            assertEquals(String.format("Cannot convert '%s' into valid file reference", INVALID_FILE_REFERENCE), ctx.read("$.details"));
        }
    }

    protected String _uploadFile() throws URISyntaxException, IOException {
        URIBuilder uriBuilder = new URIBuilder(baseUrl + "/files");
        uriBuilder.addParameter("name", TEST_FILE_PDF);

        InputStreamEntity entity = new InputStreamEntity(getFileStream(TEST_FILE_PDF));

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = getHttpPost(uriBuilder, entity);

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            ReadContext ctx = parseResponse(response);
            return ctx.read("$.fileRef");
        }
    }

    @Test
    public void uploadFileMultipart() throws Exception {
        URIBuilder uriBuilder = new URIBuilder(baseUrl + "/files");

        FileBody fileBody = new FileBody(getFile(FILE_TO_UPLOAD_TXT));

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addPart("file", fileBody);
        HttpEntity entity = builder.build();

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = getHttpPost(uriBuilder, entity);

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            assertEquals(HttpStatus.SC_CREATED, response.getStatusLine().getStatusCode());
            ReadContext ctx = parseResponse(response);

            String fileURI = ctx.read("$.fileRef");

            assertNotNull(fileURI);
            assertEquals(FILE_TO_UPLOAD_TXT, ctx.read("$.name"));
            assertTrue(ctx.read("$.size", Integer.class) > 0);

            checkLocation(response, fileURI);
        }
    }

    protected void checkLocation(CloseableHttpResponse response, String fileRef) {
        Header location = response.getFirstHeader("Location");
        assertEquals(baseUrl + "/files?fileRef=" + URLEncodeUtils.encodeUtf8(fileRef), location.getValue());
    }

    protected HttpPost getHttpPost(URIBuilder uriBuilder, HttpEntity entity) throws URISyntaxException {
        HttpPost httpPost = new HttpPost(uriBuilder.build());
        httpPost.setEntity(entity);
        httpPost.setHeader(AUTHORIZATION, "Bearer " + oauthToken);
        return httpPost;
    }

    protected HttpGet getHttpGet(String fileRef) {
        HttpGet httpGet = new HttpGet(baseUrl + "/files?fileRef=" + fileRef);
        httpGet.setHeader(AUTHORIZATION, "Bearer " + oauthToken);
        return httpGet;
    }

    protected InputStream getFileStream(String fileName) {
        try {
            return new FileInputStream(getFile(fileName));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Unable to load file " + fileName, e);
        }
    }

    protected File getFile(String fileName) {
        try {
            URL url = Thread.currentThread().getContextClassLoader().getResource("test_support/data/files/" + fileName);
            if (url != null) {
                return new File(url.toURI());
            } else {
                throw new RuntimeException("Unable to load file " + fileName);
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException("Unable to load file " + fileName, e);
        }
    }
}
