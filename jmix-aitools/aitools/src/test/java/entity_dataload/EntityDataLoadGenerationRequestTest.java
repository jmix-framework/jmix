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

package entity_dataload;

import io.jmix.aitools.dataload.generation.EntityDataLoadGenerationRequest;
import io.jmix.aitools.dataload.generation.EntityDataLoadQueryParameter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityDataLoadGenerationRequestTest {

    @Test
    void testDefaultsAreEmpty() {
        EntityDataLoadGenerationRequest request = new EntityDataLoadGenerationRequest("list orders");

        assertEquals("list orders", request.getPrompt());
        assertTrue(request.getAvailableParameters().isEmpty());
        assertTrue(request.getRequiredResultProperties().isEmpty());
    }

    @Test
    void testSettersMutateAndReturnSameInstance() {
        EntityDataLoadGenerationRequest request = new EntityDataLoadGenerationRequest("p");

        EntityDataLoadGenerationRequest returned = request
                .setAvailableParameters(List.of(new EntityDataLoadQueryParameter("dateFrom", "java.time.LocalDate")))
                .setRequiredResultProperties(List.of("year"));

        assertSame(request, returned);
        assertEquals(1, request.getAvailableParameters().size());
        assertEquals(List.of("year"), request.getRequiredResultProperties());
    }

    @Test
    void testNullListsBecomeEmpty() {
        EntityDataLoadGenerationRequest request = new EntityDataLoadGenerationRequest("p")
                .setAvailableParameters(null)
                .setRequiredResultProperties(null);

        assertTrue(request.getAvailableParameters().isEmpty());
        assertTrue(request.getRequiredResultProperties().isEmpty());
    }

    @Test
    void testNullPromptIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new EntityDataLoadGenerationRequest(null));
    }
}
