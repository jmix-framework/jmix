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

package llm_data_set;

import io.jmix.aitools.dataload.validation.validator.JpqlValidatorSupport;
import io.jmix.reports.llm.LlmQueryParameterNames;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pins {@link LlmQueryParameterNames#referencedIn} to the add-on's parameter-reference reading
 * ({@link JpqlValidatorSupport#referencedParameters}). Reports keeps its own copy so the derivation works
 * without the add-on, but the two must read a query the same way; a divergence turns this test red instead
 * of silently invalidating stored report queries.
 */
class LlmQueryParameterNamesParityTest {

    private static final List<String> CORPUS = List.of(
            "select e from report_Order e where e.customer.name like :customerName and e.number = :number",
            "select e from report_Order e where e.from >= :date and e.to <= :date",
            "select e from report_Order e where e.urn like 'urn:isbn%' and e.number = :number",
            "select e from report_Order e where e.name = 'O''Brien' and e.city = :city",
            "select e from report_Order e where e.note = ':deadline reached'",
            "select e from report_Order e",
            "",
            "   "
    );

    @Test
    @DisplayName("Reports' reading matches the add-on's over a corpus of queries")
    void testReadingMatchesAddOn() {
        for (String jpql : CORPUS) {
            assertEquals(JpqlValidatorSupport.referencedParameters(jpql), LlmQueryParameterNames.referencedIn(jpql),
                    "Divergent parameter reading for: " + jpql);
        }
    }
}
