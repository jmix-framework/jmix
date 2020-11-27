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

package anonymous_access;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.Test;
import test_support.AbstractRestControllerFT;

import java.io.IOException;
import java.net.URISyntaxException;

import static test_support.RestTestUtils.statusCode;
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
