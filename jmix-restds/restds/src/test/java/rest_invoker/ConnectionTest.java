/*
 * Copyright 2024 Haulmont.
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

package rest_invoker;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import test_support.SampleServiceConnection;

import static org.assertj.core.api.Assertions.assertThat;

class ConnectionTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    private RestClient restClient;

    @Test
    void testClientCredentialsAuthentication() throws JsonProcessingException {
        restClient = RestClient.builder()
                .baseUrl(SampleServiceConnection.getInstance().getBaseUrl())
                .build();

        ResponseEntity<String> authResponse = restClient.post()
                .uri("/oauth2/token")
                .headers(headers -> {
                    headers.setBasicAuth(SampleServiceConnection.CLIENT_ID, SampleServiceConnection.CLIENT_SECRET);
                    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                })
                .body("grant_type=client_credentials")
                .retrieve()
                .toEntity(String.class);

        JsonNode rootNode = objectMapper.readTree(authResponse.getBody());
        String authToken = rootNode.get("access_token").asText();

        assertThat(authToken).isNotNull();
    }

    @Test
    void testPasswordAuthentication() throws JsonProcessingException {
        restClient = RestClient.builder()
                .baseUrl(SampleServiceConnection.getInstance().getBaseUrl())
                .build();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "password");
        params.add("username", "admin");
        params.add("password", "admin");

        ResponseEntity<String> authResponse = restClient.post()
                .uri("/oauth2/token")
                .headers(headers -> {
                    headers.setBasicAuth("myclient2", "mysecret2");
                    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                })
                .body(params)
                .retrieve()
                .toEntity(String.class);

        JsonNode rootNode = objectMapper.readTree(authResponse.getBody());
        String authToken = rootNode.get("access_token").asText();

        assertThat(authToken).isNotNull();
    }
}
