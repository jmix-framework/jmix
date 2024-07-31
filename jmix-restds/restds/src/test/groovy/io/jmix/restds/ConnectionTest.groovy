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

package io.jmix.restds


import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestClient
import spock.lang.Specification
import test_support.SampleServiceConnection

class ConnectionTest extends Specification {

    private ObjectMapper objectMapper = new ObjectMapper()

    private RestClient restClient

    def "test authentication"() {
        when:

        restClient = RestClient.builder()
                .baseUrl("http://%s:%d".formatted(SampleServiceConnection.getInstance().getHost(), SampleServiceConnection.getInstance().getPort()))
                .build()

        ResponseEntity<String> authResponse = restClient.post()
                .uri("/oauth2/token")
                .header("Authorization", "Basic " + getBasicAuthCredentials())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body("grant_type=client_credentials")
                .retrieve()
                .toEntity(String.class)

            JsonNode rootNode = objectMapper.readTree(authResponse.getBody())
            String authToken = rootNode.get("access_token").asText()

        then:

        authToken != null
    }

    private static String getBasicAuthCredentials() {
        return Base64.getEncoder().encodeToString("myclient:mysecret".getBytes());
    }
}
