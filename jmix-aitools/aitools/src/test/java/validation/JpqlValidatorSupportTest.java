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

import java.util.Arrays;
import java.util.List;

import static io.jmix.aitools.dataload.validation.validator.JpqlValidatorSupport.referencedParameters;
import static io.jmix.aitools.dataload.validation.validator.JpqlValidatorSupport.resultAliases;
import static io.jmix.aitools.dataload.validation.validator.JpqlValidatorSupport.selectedAliases;
import static io.jmix.aitools.dataload.validation.validator.JpqlValidatorSupport.selectedExpressions;
import static io.jmix.aitools.dataload.validation.validator.JpqlValidatorSupport.selectedValueCount;
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

    @Test
    void selectedValueCount_commaInsideFunctionOrLiteral_doesNotSeparateValues() {
        String jpql = "select concat(c.name, ', ') as label, c.id as cid from aitls_Customer c";

        assertEquals(2, selectedValueCount(jpql));
    }

    @Test
    void selectedValueCount_caseColumn_countsAsOneValue() {
        String jpql = "select case when o.total > 10 then 'big' else 'small' end as orderSize, o.number as num"
                + " from aitls_Order o";

        assertEquals(2, selectedValueCount(jpql));
    }

    @Test
    void selectedValueCount_subqueryInWhere_isNotPartOfSelectClause() {
        String jpql = "select c.name as cname from aitls_Customer c"
                + " where exists (select o.id, o.number from aitls_Order o where o.customer = c)";

        assertEquals(1, selectedValueCount(jpql));
    }

    @Test
    void resultAliases_everyValueAliased_returnsAliasesInOrder() {
        String jpql = "select distinct o.number as orderNumber, o.total as orderTotal from aitls_Order o";

        assertEquals(List.of("orderNumber", "orderTotal"), resultAliases(jpql));
    }

    @Test
    void resultAliases_identificationVariableAliases_areNotColumns() {
        String jpql = "select o.number as orderNumber from aitls_Order as o join o.customer as c";

        assertEquals(List.of("orderNumber"), resultAliases(jpql));
    }

    @Test
    void resultAliases_castInsideValue_isNotAnAlias() {
        String jpql = "select cast(o.total as string) as totalText from aitls_Order o";

        assertEquals(List.of("totalText"), resultAliases(jpql));
    }

    @Test
    void resultAliases_wordFromInsideNames_doesNotEndSelectClause() {
        String jpql = "select e.from as fromDate, e.validFrom as valid_from from aitls_Order e";

        assertEquals(List.of("fromDate", "valid_from"), resultAliases(jpql));
    }

    @Test
    void selectedAliases_partiallyAliased_keepsPositionOfEveryValue() {
        String jpql = "select o.number, cast(o.total as string) as totalText, count(o) from aitls_Order o";

        assertEquals(Arrays.asList(null, "totalText", null), selectedAliases(jpql));
    }

    @Test
    void selectedExpressions_dropAliasesAndCollapseWhitespace() {
        String jpql = "select  o.number   as num,\n  concat(c.name, ', ') as label, count(o) from aitls_Order o";

        // String literals arrive blanked, so a comma inside one neither splits a value nor survives comparison.
        assertEquals(List.of("o.number", "concat(c.name, '')", "count(o)"), selectedExpressions(jpql));
    }

    @Test
    void selectedAliases_distinctAndSelectKeyword_areNotValues() {
        String jpql = "select distinct o.number as num from aitls_Order o";

        assertEquals(List.of("num"), selectedAliases(jpql));
    }

    @Test
    void resultAliases_valueWithoutAlias_returnsEmpty() {
        assertTrue(resultAliases("select o.number, o.total as orderTotal from aitls_Order o").isEmpty());
    }

    @Test
    void resultAliases_noAliases_returnsEmpty() {
        assertTrue(resultAliases("select o.number from aitls_Order o").isEmpty());
    }
}
