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

import io.jmix.reports.llm.LlmDataQuery;
import io.jmix.reports.llm.LlmDataQueryException;
import io.jmix.reports.llm.LlmQueryParameter;
import io.jmix.reports.llm.impl.LlmDataQuerySerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

class LlmDataQuerySerializerTest {

    protected final LlmDataQuerySerializer serializer = new LlmDataQuerySerializer();

    @Test
    void testRowCountSurvivesRoundTrip() {
        LlmDataQuery query = new LlmDataQuery("select o.number as orderNumber from sales_Order o",
                List.of("orderNumber"), List.of(), null, List.of(), 5, 10);

        LlmDataQuery restored = serializer.fromJson(serializer.toJson(query));

        assertThat(restored).isNotNull();
        assertThat(restored.getMaxResults()).isEqualTo(5);
        assertThat(restored.getFirstResult()).isEqualTo(10);
    }

    @Test
    void testStoredRowCountThatIsNotACountReadsAsUnlimited() {
        // A document written by hand can carry a zero or a negative number, and neither says "return this many
        // rows": a run reads such a query as unlimited instead of emptying the band over it.
        LlmDataQuery restored = serializer.fromJson("""
                {"jpql":"select o.number as orderNumber from sales_Order o",\
                "resultProperties":["orderNumber"],"maxResults":0,"firstResult":-3}""");

        assertThat(restored).isNotNull();
        assertThat(restored.getMaxResults()).isNull();
        assertThat(restored.getFirstResult()).isNull();
    }

    @Test
    void testEditedQueryKeepsTheRowCountOfThePreviousDocument() {
        // An edit is about the text, and the count was never in the text to be edited out of it.
        LlmDataQuery previous = new LlmDataQuery("select o.number as orderNumber from sales_Order o",
                List.of("orderNumber"), List.of(), null, List.of(), 5, null);

        LlmDataQuery edited = serializer.assemble("select o.amount as total from sales_Order o",
                List.of("total"), previous);

        assertThat(edited.getMaxResults()).isEqualTo(5);
    }

    @Test
    void testQuerySurvivesRoundTrip() {
        LlmDataQuery query = new LlmDataQuery(
                "select o.number as orderNumber from sales_Order o where o.date >= :dateFrom",
                List.of("orderNumber"),
                List.of(new LlmQueryParameter("dateFrom", "java.time.LocalDate")),
                "Orders from the given date",
                List.of("Time zone is not taken into account"));

        LlmDataQuery restored = serializer.fromJson(serializer.toJson(query));

        assertThat(restored).isNotNull();
        assertThat(restored.getJpql()).isEqualTo(query.getJpql());
        assertThat(restored.getResultProperties()).containsExactly("orderNumber");
        assertThat(restored.getParameters()).hasSize(1);
        assertThat(restored.getParameters().get(0).getName()).isEqualTo("dateFrom");
        assertThat(restored.getParameters().get(0).getJavaType()).isEqualTo("java.time.LocalDate");
        assertThat(restored.getExplanation()).isEqualTo("Orders from the given date");
        assertThat(restored.getWarnings()).containsExactly("Time zone is not taken into account");
    }

    @Test
    void testStoredDocumentCarriesNothingButTheQueryContract() {
        // Both flags describe what a run offers, not what the query declares, so neither may travel in the
        // report XML: a document read back elsewhere must not claim anything about the report's parameters.
        String json = serializer.toJson(new LlmDataQuery("select o.id as id from sales_Order o", List.of("id"),
                List.of(new LlmQueryParameter("years", "java.lang.Integer", true, true)),
                null, List.of()));

        assertThat(json).doesNotContain("multiValued").doesNotContain("optional");
    }


    @Test
    void testEditedQueryKeepsItsTextColumnsAndDerivedParameters() {
        LlmDataQuery previous = new LlmDataQuery("select o.id as id from sales_Order o", List.of("id"),
                List.of(), "Orders", List.of("Amounts are not converted"));

        LlmDataQuery edited = serializer.assemble(
                "select o.number as num from sales_Order o where o.date >= :dateFrom and o.amount > :minAmount",
                List.of("num"), previous);

        assertThat(edited.getJpql()).contains(":dateFrom");
        assertThat(edited.getResultProperties()).containsExactly("num");
        assertThat(edited.getParameters())
                .extracting(LlmQueryParameter::getName)
                .containsExactly("dateFrom", "minAmount");
        assertThat(edited.getExplanation()).isEqualTo("Orders");
        assertThat(edited.getWarnings()).containsExactly("Amounts are not converted");
    }

    @Test
    void testColonInsideAStringLiteralIsNoParameter() {
        // A phantom parameter no run could bind would make an otherwise valid query unrunnable.
        LlmDataQuery edited = serializer.assemble(
                "select o.number as num from sales_Order o where o.code like 'urn:isbn%' and o.date >= :dateFrom",
                List.of("num"), null);

        assertThat(edited.getParameters())
                .extracting(LlmQueryParameter::getName)
                .containsExactly("dateFrom");
    }


    @Test
    void testEditedQueryKeepsTheJavaTypeOfAParameterThePreviousDocumentDeclared() {
        LlmDataQuery previous = new LlmDataQuery(
                "select o.number as orderNumber from sales_Order o where o.date >= :dateFrom",
                List.of("orderNumber"),
                List.of(new LlmQueryParameter("dateFrom", "java.time.LocalDate")),
                "Orders since the given date", List.of());

        LlmDataQuery assembled = serializer.assemble(
                "select o.number as orderNumber from sales_Order o "
                        + "where o.date >= :dateFrom and o.number like :numberPart",
                List.of("orderNumber"), previous);

        assertThat(assembled.getParameters())
                .extracting(LlmQueryParameter::getName, LlmQueryParameter::getJavaType)
                .containsExactly(tuple("dateFrom", "java.time.LocalDate"), tuple("numberPart", ""));
    }


    @Test
    void testBlankDocumentMeansNoCachedQuery() {
        assertThat(serializer.fromJson(null)).isNull();
        assertThat(serializer.fromJson("")).isNull();
        assertThat(serializer.fromJson("   ")).isNull();
    }

    @Test
    void testDocumentWithoutOptionalFieldsReadsWithEmptyCollections() {
        LlmDataQuery restored = serializer.fromJson("{\"jpql\":\"select o.number as n from sales_Order o\"}");

        assertThat(restored).isNotNull();
        assertThat(restored.getResultProperties()).isEmpty();
        assertThat(restored.getParameters()).isEmpty();
        assertThat(restored.getWarnings()).isEmpty();
        assertThat(restored.getExplanation()).isNull();
    }

    /**
     * A document that carries no usable query — whatever is wrong with it — is rejected the same way, because
     * the author can do only one thing about any of them.
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "null",
            "[1, 2]",
            "{\"jpql\": ",
            "{\"resultProperties\":[\"n\"]}"
    })
    void testUnreadableDocumentFailsWithRegenerationHint(String document) {
        assertThatThrownBy(() -> serializer.fromJson(document))
                .isInstanceOf(LlmDataQueryException.class)
                .hasMessageContaining("regenerate");
    }

    @Test
    void testParameterWithoutNameIsDropped() {
        LlmDataQuery restored = serializer.fromJson("""
                {"jpql":"select o.number as n from sales_Order o where o.id = :id",\
                "parameters":[{"javaType":"java.lang.String"},{"name":"id","javaType":"java.util.UUID"}]}""");

        assertThat(restored).isNotNull();
        assertThat(restored.getParameters())
                .extracting(LlmQueryParameter::getName)
                .containsExactly("id");
    }

    @Test
    void testNullElementsOfStoredListsAreDropped() {
        LlmDataQuery restored = serializer.fromJson("""
                {"jpql":"select o.number as n from sales_Order o",\
                "resultProperties":["n",null],\
                "parameters":[null],\
                "warnings":[null,"approximated"]}""");

        assertThat(restored).isNotNull();
        assertThat(restored.getResultProperties()).containsExactly("n");
        assertThat(restored.getParameters()).isEmpty();
        assertThat(restored.getWarnings()).containsExactly("approximated");
    }
}
