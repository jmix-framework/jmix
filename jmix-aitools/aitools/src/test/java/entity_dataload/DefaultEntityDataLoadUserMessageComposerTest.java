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
import io.jmix.aitools.dataload.generation.impl.DefaultEntityDataLoadUserMessageComposer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultEntityDataLoadUserMessageComposerTest {

    DefaultEntityDataLoadUserMessageComposer composer = new DefaultEntityDataLoadUserMessageComposer();

    @Test
    void testARequestWithoutConstraintsComposesToThePromptAlone() {
        EntityDataLoadGenerationRequest request = new EntityDataLoadGenerationRequest("list orders");

        assertTrue(composer.composeConstraints(request).isEmpty());
        assertEquals("list orders", composer.compose(request));
    }

    @Test
    void testTheMessageIsThePromptFollowedByTheConstraints() {
        EntityDataLoadGenerationRequest request = new EntityDataLoadGenerationRequest("list orders")
                .setAvailableParameters(List.of(new EntityDataLoadQueryParameter("dateFrom", "java.time.LocalDate")));

        String message = composer.compose(request);

        assertTrue(message.startsWith("list orders"));
        assertEquals("list orders" + composer.composeConstraints(request), message);
    }

    @Test
    void testAScalarParameterRendersNameAndTypeOnly() {
        String text = composer.composeConstraints(new EntityDataLoadGenerationRequest("p")
                .setAvailableParameters(List.of(new EntityDataLoadQueryParameter("dateFrom", "java.time.LocalDate"))));

        assertTrue(text.contains("AVAILABLE PARAMETERS:"));
        assertTrue(text.contains(":dateFrom (java.time.LocalDate)"));
        assertFalse(text.contains("may be null"));
        assertFalse(text.contains("matched with IN"));
    }

    @Test
    void testAMultiValuedParameterAsksForInWithoutParentheses() {
        String text = composer.composeConstraints(new EntityDataLoadGenerationRequest("p")
                .setAvailableParameters(List.of(
                        new EntityDataLoadQueryParameter("ids", "java.util.UUID").setMultiValued(true))));

        assertTrue(text.contains("several values of this type, matched with IN"));
        assertTrue(text.contains("no parentheses around the parameter name"));
    }

    @Test
    void testAnOptionalParameterIsMarkedAndTheGuardIsSpelledOut() {
        String text = composer.composeConstraints(new EntityDataLoadGenerationRequest("p")
                .setAvailableParameters(List.of(
                        new EntityDataLoadQueryParameter("city", "java.lang.String").setOptional(true))));

        assertTrue(text.contains(":city (java.lang.String, may be null)"));
        assertTrue(text.contains("(:name is null or e.attribute = :name)"));
    }

    @Test
    void testRequiredResultPropertiesAreListedNeutrally() {
        String text = composer.composeConstraints(new EntityDataLoadGenerationRequest("p")
                .setRequiredResultProperties(List.of("year", "month")));

        assertTrue(text.contains("REQUIRED RESULT COLUMNS"));
        assertTrue(text.contains("\n- year"));
        assertTrue(text.contains("\n- month"));
        assertFalse(text.contains("cross-tab"));
        assertFalse(text.contains("matching that parameter with IN"));
    }
}
