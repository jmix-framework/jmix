/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package io.jmix.samples.rest.tests;

import com.jayway.jsonpath.ReadContext;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static io.jmix.samples.rest.tools.RestTestUtils.parseResponse;
import static io.jmix.samples.rest.tools.RestTestUtils.statusCode;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
@Disabled
public class FilesControllerFT extends AbstractRestControllerFT {

    protected static final String URI_BASE = "http://localhost:8080/app/rest/v2";

    protected static final String AUTHORIZATION = "Authorization";

    protected static final String NON_UUID_VALUE = "nonUuidValue";

    protected static final String EXT_TXT = "txt";
    protected static final String FILE_TO_UPLOAD_TXT = "fileToUpload.txt";
    protected static final String TEST_FILE_PDF = "test-file.pdf";

    protected InputStreamEntity entity;

    @BeforeEach
    public void initEntity() throws URISyntaxException, FileNotFoundException {
        URL fileUrl = FilesControllerFT.class.getResource("data/" + FILE_TO_UPLOAD_TXT);
        entity = new InputStreamEntity(new FileInputStream(new File(fileUrl.toURI())));
    }

    @Test
    public void uploadFile() throws Exception {
        URIBuilder uriBuilder = new URIBuilder(URI_BASE + "/files");
        uriBuilder.addParameter("name", FILE_TO_UPLOAD_TXT);

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = getHttpPost(uriBuilder, entity);

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            assertEquals(HttpStatus.SC_CREATED, response.getStatusLine().getStatusCode());

            ReadContext ctx = parseResponse(response);

            String fileDescriptorId = ctx.read("$.id");

            assertEquals(FILE_TO_UPLOAD_TXT, ctx.read("$.name"));
            assertEquals(0, (int) ctx.read("$.size"));
            assertNotNull(fileDescriptorId);

            checkLocation(response, fileDescriptorId);
            checkRecordInDb(fileDescriptorId, FILE_TO_UPLOAD_TXT, "txt", 0);
        }
    }

    @Test
    public void uploadFileWithoutName() throws Exception {
        URIBuilder uriBuilder = new URIBuilder(URI_BASE + "/files");

        URL fileUrl = FilesControllerFT.class.getResource("data/" + FILE_TO_UPLOAD_TXT);
        InputStreamEntity entity = new InputStreamEntity(new FileInputStream(new File(fileUrl.toURI())));

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = getHttpPost(uriBuilder, entity);

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            ReadContext ctx = parseResponse(response);

            String fileDescriptorId = ctx.read("$.id");

            assertNotNull(fileDescriptorId);
            assertEquals(fileDescriptorId, ctx.read("$.name"));
            assertEquals(0, (int) ctx.read("$.size"));

            checkLocation(response, fileDescriptorId);
            checkRecordInDb(fileDescriptorId, fileDescriptorId, "", 0);
        }
    }

    @Test
    public void uploadFileWithId() throws Exception {
        String id = UUID.randomUUID().toString();

        URIBuilder uriBuilder = new URIBuilder(URI_BASE + "/files");
        uriBuilder.addParameter("name", FILE_TO_UPLOAD_TXT);
        uriBuilder.addParameter("id", id);

        URL fileUrl = FilesControllerFT.class.getResource("data/" + FILE_TO_UPLOAD_TXT);
        InputStreamEntity entity = new InputStreamEntity(new FileInputStream(new File(fileUrl.toURI())));

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = getHttpPost(uriBuilder, entity);

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            assertEquals(HttpStatus.SC_CREATED, response.getStatusLine().getStatusCode());

            ReadContext ctx = parseResponse(response);
            String fileDescriptorId = ctx.read("$.id");

            assertEquals(id, fileDescriptorId);
            assertEquals(FILE_TO_UPLOAD_TXT, ctx.read("$.name"));
            assertEquals(0, (int) ctx.read("$.size"));

            checkLocation(response, id);
            checkRecordInDb(fileDescriptorId, FILE_TO_UPLOAD_TXT, EXT_TXT, 0);
        }
    }

    @Test
    public void downloadFile() throws Exception {
        String fileId = _uploadFile();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = getHttpGet(fileId);

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
            assertEquals("application/pdf", response.getFirstHeader("Content-Type").getValue());
            assertEquals("no-cache", response.getFirstHeader("Cache-Control").getValue());
            assertEquals("inline; filename=\"test-file.pdf\"", response.getFirstHeader("Content-Disposition").getValue());
//            assertEquals("123", response.getFirstHeader("Content-Length").getValue());
            byte[] fileContent = EntityUtils.toByteArray(response.getEntity());
            assertTrue(fileContent.length > 0);
//            assertEquals("Test data", fileContent);
        }
    }

    @Test
    public void downloadMissingFile() throws Exception {
        String fileId = UUID.randomUUID().toString();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = getHttpGet(fileId);
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
            ReadContext ctx = parseResponse(response);
            assertEquals("File not found", ctx.read("$.error"));
        }
    }

    @Test
    public void downloadFileWithInvalidId() throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = getHttpGet(NON_UUID_VALUE);
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals("Invalid entity ID", ctx.read("$.error"));
            assertEquals(String.format("Cannot convert %s into valid entity ID", NON_UUID_VALUE), ctx.read("$.details"));
        }
    }

    protected String _uploadFile() throws URISyntaxException, IOException {
        URIBuilder uriBuilder = new URIBuilder(URI_BASE + "/files");
        uriBuilder.addParameter("name", TEST_FILE_PDF);

        URL fileUrl = FilesControllerFT.class.getResource("data/" + FILE_TO_UPLOAD_TXT);
        InputStreamEntity entity = new InputStreamEntity(new FileInputStream(new File(fileUrl.toURI())));

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = getHttpPost(uriBuilder, entity);

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            ReadContext ctx = parseResponse(response);
            return ctx.read("$.id");
        }
    }

    @Test
    public void uploadFileMultipart() throws Exception {
        URIBuilder uriBuilder = new URIBuilder(URI_BASE + "/files");

        URL fileUrl = FilesControllerFT.class.getResource("data/" + FILE_TO_UPLOAD_TXT);
        FileBody fileBody = new FileBody(new File(fileUrl.toURI()));

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addPart("file", fileBody);
        HttpEntity entity = builder.build();

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = getHttpPost(uriBuilder, entity);
//        httpPost.setHeader("Content-Type", "multipart/form-data");
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            assertEquals(HttpStatus.SC_CREATED, response.getStatusLine().getStatusCode());
            ReadContext ctx = parseResponse(response);

            String fileDescriptorId = ctx.read("$.id");

            assertNotNull(fileDescriptorId);
            assertEquals(FILE_TO_UPLOAD_TXT, ctx.read("$.name"));
            assertTrue(ctx.read("$.size", Integer.class) > 0);

            checkLocation(response, fileDescriptorId);
            checkRecordInDb(fileDescriptorId, FILE_TO_UPLOAD_TXT, EXT_TXT, 9);
        }
    }

    protected void checkLocation(CloseableHttpResponse response, String fileDescriptorId) {
        Header location = response.getFirstHeader("Location");
        assertEquals(URI_BASE + "/files/" + fileDescriptorId, location.getValue());
    }


    protected void checkRecordInDb(String id, String name, String ext, long fileSize) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("select NAME, EXT, FILE_SIZE from SYS_FILE where ID = ?")) {
            stmt.setObject(1, UUID.fromString(id));
            ResultSet rs = stmt.executeQuery();

            assertTrue(rs.next());
            assertEquals(name, rs.getString("NAME"));
            assertEquals(ext, rs.getString("EXT"));
            assertEquals(fileSize, rs.getLong("FILE_SIZE"));
            assertFalse(rs.next());
        }
    }

    protected HttpPost getHttpPost(URIBuilder uriBuilder, HttpEntity entity) throws URISyntaxException {
        HttpPost httpPost = new HttpPost(uriBuilder.build());
        httpPost.setEntity(entity);
        httpPost.setHeader(AUTHORIZATION, "Bearer " + oauthToken);
        return httpPost;
    }

    protected HttpGet getHttpGet(String fileId) {
        HttpGet httpGet = new HttpGet(URI_BASE + "/files/" + fileId);
        httpGet.setHeader(AUTHORIZATION, "Bearer " + oauthToken);
        return httpGet;
    }
}
