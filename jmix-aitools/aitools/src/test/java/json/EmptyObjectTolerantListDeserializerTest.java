/*
 * Copyright 2026 Haulmont.
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

package json;

import io.jmix.aitools.dataload.execution.JpqlExecutionRequest;
import io.jmix.aitools.dataload.generation.EntityDataLoadQueryPayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The tolerance is attached to the target types via {@code @JsonDeserialize}, so it must hold with a
 * plain, unconfigured {@link ObjectMapper} — the same situation as Spring AI binding tool-call
 * arguments through its own internal mapper.
 */
class EmptyObjectTolerantListDeserializerTest {

    ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    @DisplayName("Tool-argument request: empty object for list fields becomes empty list")
    void testToolRequestEmptyObjectBecomesEmptyList() throws Exception {
        String json = """
                {
                  "jpql": "select c from aitls_Customer c",
                  "parameters": {},
                  "resultProperties": {}
                }
                """;

        JpqlExecutionRequest request = objectMapper.readValue(json, JpqlExecutionRequest.class);

        assertEquals(0, request.getParameters().size());
        assertEquals(0, request.getResultProperties().size());
    }

    @Test
    @DisplayName("Populated parameters array is preserved (delegates to standard deserialization)")
    void testPopulatedArrayIsPreserved() throws Exception {
        String json = """
                {
                  "parameters": [
                    {"name": "id", "type": "Long", "value": "1"}
                  ]
                }
                """;

        EntityDataLoadQueryPayload payload = objectMapper.readValue(json, EntityDataLoadQueryPayload.class);

        assertEquals(1, payload.getParameters().size());
        assertEquals("id", payload.getParameters().get(0).getName());
    }

    @Test
    @DisplayName("Generation payload: empty object for list fields becomes empty list")
    void testPayloadEmptyObjectBecomesEmptyList() throws Exception {
        String json = """
                {
                  "jpql": "select c from aitls_Customer c",
                  "parameters": {},
                  "warnings": {}
                }
                """;

        EntityDataLoadQueryPayload payload = objectMapper.readValue(json, EntityDataLoadQueryPayload.class);

        assertEquals(0, payload.getParameters().size());
        assertEquals(0, payload.getWarnings().size());
    }

    @Test
    @DisplayName("Still reports a non-empty object where an array is expected as a mismatch")
    void testNonEmptyObjectStillFails() {
        String json = """
                {
                  "parameters": {"name": "id"}
                }
                """;

        assertThrows(MismatchedInputException.class,
                () -> objectMapper.readValue(json, JpqlExecutionRequest.class));
    }
}
