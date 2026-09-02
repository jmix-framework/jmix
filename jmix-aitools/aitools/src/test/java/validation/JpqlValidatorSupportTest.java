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

package validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.jmix.aitools.dataload.validation.validator.JpqlValidatorSupport.referencedParameters;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JpqlValidatorSupportTest {

    @Test
    @DisplayName("Reads named parameters in order of appearance")
    void testReadsParametersInOrder() {
        String jpql = "select e from aitls_Order e where e.customer.name like :customerName and e.number = :number";

        assertEquals(List.of("customerName", "number"), List.copyOf(referencedParameters(jpql)));
    }

    @Test
    @DisplayName("Reports a repeated parameter once")
    void testDeduplicatesRepeatedParameter() {
        String jpql = "select e from aitls_Order e where e.from >= :date and e.to <= :date";

        assertEquals(List.of("date"), List.copyOf(referencedParameters(jpql)));
    }

    @Test
    @DisplayName("Ignores a colon-prefixed word inside a string literal")
    void testIgnoresParameterLikeStringLiteral() {
        String jpql = "select e from aitls_Order e where e.urn like 'urn:isbn%' and e.number = :number";

        assertEquals(List.of("number"), List.copyOf(referencedParameters(jpql)));
    }

    @Test
    @DisplayName("Reads no parameters when there are none")
    void testReadsNoParameters() {
        assertTrue(referencedParameters("select e from aitls_Order e").isEmpty());
    }

    @Test
    @DisplayName("Reads no parameters from blank text")
    void testReadsNoParametersFromBlankText() {
        assertTrue(referencedParameters("").isEmpty());
        assertTrue(referencedParameters("   ").isEmpty());
    }
}
