package io.jmix.samples.rest.tests;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static io.jmix.samples.rest.tools.RestTestUtils.statusCode;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NotAuthenticatedAccessFT extends AbstractRestControllerFT {

    @Test
    public void requestEntitiesController() throws Exception {
        requestAndCheckUnauthorized("/entities/ref_Car");
    }

    @Test
    public void requestQueriesController() throws Exception {
        requestAndCheckUnauthorized("/queries/userByLogin");
    }

    @Test
    public void requestServicesController() throws Exception {
        requestAndCheckUnauthorized("/services/jmix_RestTestService/sum");
    }

    @Test
    public void requestPermissionsController() throws Exception {
        requestAndCheckUnauthorized("/permissions");
    }

    @Test
    public void requestUserInfoController() throws Exception {
        requestAndCheckUnauthorized("/userInfo");
    }

    @Test
    public void requestMetadataController() throws Exception {
        requestAndCheckUnauthorized("/metadata");
    }

    protected void requestAndCheckUnauthorized(String resourceUrl) throws URISyntaxException, IOException {
        URIBuilder uriBuilder = new URIBuilder(baseUrl + resourceUrl);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(uriBuilder.build());
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            assertEquals(HttpStatus.SC_UNAUTHORIZED, statusCode(response));
        }
    }
}
