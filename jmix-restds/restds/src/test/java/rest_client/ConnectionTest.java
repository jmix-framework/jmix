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

package rest_client;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import test_support.SampleServiceConnection;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class ConnectionTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    private RestClient restClient;

    @Test
    void testAuthentication() throws JsonProcessingException {
        restClient = RestClient.builder()
                .baseUrl(SampleServiceConnection.getInstance().getBaseUrl())
                .build();

        ResponseEntity<String> authResponse = restClient.post()
                .uri("/oauth2/token")
                .header("Authorization", "Basic " + getBasicAuthCredentials())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body("grant_type=client_credentials")
                .retrieve()
                .toEntity(String.class);

        JsonNode rootNode = objectMapper.readTree(authResponse.getBody());
        String authToken = rootNode.get("access_token").asText();

        assertThat(authToken).isNotNull();
    }

    private static String getBasicAuthCredentials() {
        return Base64.getEncoder().encodeToString(
                (SampleServiceConnection.CLIENT_ID + ":" + SampleServiceConnection.CLIENT_SECRET).getBytes()
        );
    }
}
