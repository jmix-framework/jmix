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

package execution;

import io.jmix.aitools.dataload.execution.JpqlExecutionParameter;
import io.jmix.aitools.dataload.execution.JpqlParameterConversionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JpqlParameterConversionServiceTest {

    JpqlParameterConversionService service = new JpqlParameterConversionService();

    @Test
    @DisplayName("Converts each string element of an IN collection to the declared type")
    void testConvertsCollectionElements() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();

        Object converted = service.convert(new JpqlExecutionParameter(
                "ids", "UUID", List.of(first.toString(), second.toString())));

        assertEquals(List.of(first, second), converted);
    }

    @Test
    @DisplayName("Leaves already-typed IN collection elements unchanged")
    void testKeepsAlreadyTypedCollectionElements() {
        Object converted = service.convert(new JpqlExecutionParameter(
                "statuses", "Integer", List.of(20, 30)));

        assertEquals(List.of(20, 30), converted);
    }
}
