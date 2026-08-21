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

import io.jmix.aitools.dataload.execution.GeneratedJpqlParameter;
import io.jmix.aitools.dataload.execution.GeneratedJpqlResult;
import io.jmix.aitools.dataload.execution.JpqlExecutionRequest;
import io.jmix.reports.llm.LlmDataQuery;
import io.jmix.reports.llm.LlmDataQueryException;
import io.jmix.reports.llm.LlmQueryGenerationRequest;
import io.jmix.reports.llm.LlmQueryParameter;
import io.jmix.reports.llm.impl.AiToolsLlmDataQueryService;
import llm_data_set.test_support.TestEntityDataLoadGenerationService;
import llm_data_set.test_support.TestJpqlValidationAndRepairService;
import llm_data_set.test_support.TestJpqlValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

/**
 * The mapping between the Reports-side seam and the AI Tools data-load services.
 */
class AiToolsLlmDataQueryServiceTest {

    protected static final String PROMPT = "Orders placed since the given date";

    protected TestEntityDataLoadGenerationService generationService;
    protected TestJpqlValidationService validationService;
    protected TestJpqlValidationAndRepairService validationAndRepairService;
    protected AiToolsLlmDataQueryService service;

    @BeforeEach
    void setUp() {
        generationService = new TestEntityDataLoadGenerationService();
        validationService = new TestJpqlValidationService();
        validationAndRepairService = new TestJpqlValidationAndRepairService();
        service = new AiToolsLlmDataQueryService();
        ReflectionTestUtils.setField(service, "entityDataLoadGenerationService", generationService);
        ReflectionTestUtils.setField(service, "jpqlValidationService", validationService);
        ReflectionTestUtils.setField(service, "jpqlValidationAndRepairService", validationAndRepairService);
    }

    @Test
    void testGeneratedQueryIsOfferedForRepairWithTheRequestItAnswers() {
        service.generate(new LlmQueryGenerationRequest(PROMPT,
                List.of(new LlmQueryParameter("dateFrom", "java.time.LocalDate", null)), List.of()));

        JpqlExecutionRequest offered = validationAndRepairService.getLastRequest();
        // Repair rewrites the query to satisfy the request it was generated from, so it is given both.
        assertThat(offered.getUserText()).contains(PROMPT);
        assertThat(offered.getJpql()).isEqualTo(generationService.getJpql());
        assertThat(offered.getResultProperties()).containsExactly("orderNumber");
        assertThat(offered.getMaxResults()).isNull();
    }

    @Test
    void testQueryThatNeededNoRepairIsReturnedAsGenerated() {
        LlmDataQuery query = service.generate(generationRequest());

        assertThat(query.getJpql()).isEqualTo(generationService.getJpql());
        assertThat(query.getResultProperties()).containsExactly("orderNumber");
    }

    @Test
    void testRepairedQueryReplacesTheGeneratedOne() {
        validationAndRepairService.setRepairedResult(new GeneratedJpqlResult(
                "select o.number as orderNumber from sales_Order o where o.date >= :dateFrom",
                List.of(new GeneratedJpqlParameter("dateFrom", "java.time.LocalDate", null)),
                "Order numbers since the given date", List.of("the date is compared as a date")));

        LlmDataQuery query = service.generate(generationRequest());

        assertThat(query.getJpql()).contains(":dateFrom");
        assertThat(query.getParameters())
                .extracting(LlmQueryParameter::getName, LlmQueryParameter::getJavaType)
                .containsExactly(tuple("dateFrom", "java.time.LocalDate"));
        assertThat(query.getExplanation()).isEqualTo("Order numbers since the given date");
        assertThat(query.getWarnings()).containsExactly("the date is compared as a date");
    }

    @Test
    void testColumnsOfARepairedQueryAreReadOffItsSelectClause() {
        // Repair answers with a query text alone, and renaming a column is exactly what it does with an alias
        // that turned out to be a reserved word.
        validationAndRepairService.setRepairedResult(new GeneratedJpqlResult(
                "select o.number as orderNo, o.amount as amount from sales_Order o",
                List.of(), "", List.of()));

        LlmDataQuery query = service.generate(generationRequest());

        assertThat(query.getResultProperties()).containsExactly("orderNo", "amount");
    }

    @Test
    void testColumnsOfASubqueryAreNotTakenForColumnsOfTheQuery() {
        validationAndRepairService.setRepairedResult(new GeneratedJpqlResult(
                "select o.number as orderNo from sales_Order o where o.amount > "
                        + "(select avg(p.amount) as average from sales_Order p)",
                List.of(), "", List.of()));

        LlmDataQuery query = service.generate(generationRequest());

        assertThat(query.getResultProperties()).containsExactly("orderNo");
    }

    @Test
    void testRepairedQueryWithoutColumnsKeepsTheGeneratedOnes() {
        validationAndRepairService.setRepairedResult(new GeneratedJpqlResult(
                "select o.number from sales_Order o", List.of(), "", List.of()));

        LlmDataQuery query = service.generate(generationRequest());

        assertThat(query.getResultProperties()).containsExactly("orderNumber");
    }

    @Test
    void testRepairThatCannotBeCarriedOutLeavesTheGeneratedQuery() {
        // Repair asks the model too, and a query an author can correct by hand is worth more than a failure.
        validationAndRepairService.setFailure(new IllegalStateException("LLM returned an empty response"));

        LlmDataQuery query = service.generate(generationRequest());

        assertThat(query.getJpql()).isEqualTo(generationService.getJpql());
    }

    @Test
    void testQueryStillFaultyAfterRepairIsReturnedForTheDesignerToReportOn() {
        validationAndRepairService.setRemainingIssues(List.of("usedEntities.unknown: unknown entity"));
        validationAndRepairService.setRepairedResult(new GeneratedJpqlResult(
                "select o.number as orderNo from sales_Order o", List.of(), "", List.of()));

        LlmDataQuery query = service.generate(generationRequest());

        assertThat(query.getResultProperties()).containsExactly("orderNo");
    }

    protected LlmQueryGenerationRequest generationRequest() {
        return new LlmQueryGenerationRequest(PROMPT, List.of(), List.of());
    }

    @Test
    void testPromptAndAvailableParametersReachGeneration() {
        service.generate(new LlmQueryGenerationRequest(PROMPT,
                List.of(new LlmQueryParameter("dateFrom", "java.time.LocalDate", null)), List.of()));

        String userText = generationService.getLastUserText();
        assertThat(userText).contains(PROMPT);
        assertThat(userText).contains("dateFrom");
        assertThat(userText).contains("java.time.LocalDate");
        assertThat(userText).contains(":dateFrom");
    }

    @Test
    void testListValuedParameterUsesInWithoutBecomingACrossTabAxis() {
        service.generate(new LlmQueryGenerationRequest(PROMPT, List.of(
                new LlmQueryParameter("customerIds", "java.util.UUID", List.of("1", "2"), true),
                new LlmQueryParameter("dateFrom", "java.time.LocalDate", LocalDate.of(2026, 8, 1))), List.of()));

        String userText = generationService.getLastUserText();
        assertThat(userText).contains("several values of this type, matched with IN");
        assertThat(userText).contains("no parentheses around the parameter name");
        assertThat(userText).containsPattern(":dateFrom \\(java\\.time\\.LocalDate\\)");
        assertThat(userText).doesNotContain("REQUIRED RESULT COLUMNS");
    }

    @Test
    void testCrossTabRequiredResultColumnsAreDescribedExplicitly() {
        service.generate(new LlmQueryGenerationRequest(PROMPT, List.of(
                new LlmQueryParameter("revenue_dynamic_header_year", "java.lang.Integer",
                        List.of(2025, 2026), true)),
                List.of("revenue_dynamic_header_year")));

        String userText = generationService.getLastUserText();
        assertThat(userText).contains("REQUIRED RESULT COLUMNS");
        assertThat(userText).contains("\n- revenue_dynamic_header_year");
        assertThat(userText).contains("matching that parameter with IN");
    }

    @Test
    void testScalarParameterShadowingRequiredColumnDoesNotUseIn() {
        service.generate(new LlmQueryGenerationRequest(PROMPT, List.of(
                new LlmQueryParameter("revenue_dynamic_header_year", "java.lang.Integer", 2026)),
                List.of("revenue_dynamic_header_year")));

        String userText = generationService.getLastUserText();
        assertThat(userText).contains("REQUIRED RESULT COLUMNS");
        assertThat(userText).contains("\n- revenue_dynamic_header_year");
        assertThat(userText).doesNotContain("matching that parameter with IN");
    }

    @Test
    void testRequiredResultColumnsDoNotDependOnAvailableParameters() {
        service.generate(new LlmQueryGenerationRequest(PROMPT, List.of(),
                List.of("revenue_dynamic_header_year")));

        String userText = generationService.getLastUserText();
        assertThat(userText).doesNotContain("AVAILABLE REPORT PARAMETERS");
        assertThat(userText).contains("REQUIRED RESULT COLUMNS");
        assertThat(userText).contains("\n- revenue_dynamic_header_year");
    }

    @Test
    void testGeneratedQueryIsMappedToTheSeamType() {
        generationService.setJpql("select o.number as orderNumber from sales_Order o where o.date >= :dateFrom");
        generationService.setParameters(List.of(new GeneratedJpqlParameter("dateFrom", "java.lang.String", "x")));
        generationService.setResultProperties(List.of("orderNumber"));
        generationService.setExplanation("Orders since the given date");
        generationService.setWarnings(List.of("Time zone ignored"));

        LlmDataQuery query = service.generate(new LlmQueryGenerationRequest(PROMPT, List.of(), List.of()));

        assertThat(query.getJpql()).contains("select o.number as orderNumber");
        assertThat(query.getResultProperties()).containsExactly("orderNumber");
        assertThat(query.getParameters())
                .extracting(LlmQueryParameter::getName, LlmQueryParameter::getJavaType)
                .containsExactly(tuple("dateFrom", "java.lang.String"));
        assertThat(query.getParameters().get(0).getValue()).isNull();
        assertThat(query.getExplanation()).isEqualTo("Orders since the given date");
        assertThat(query.getWarnings()).containsExactly("Time zone ignored");
    }

    @Test
    void testGenerationFailureBecomesLlmDataQueryException() {
        generationService.setFailure(new IllegalStateException("LLM is not configured"));

        assertThatThrownBy(() -> service.generate(new LlmQueryGenerationRequest(PROMPT, List.of(), List.of())))
                .isInstanceOf(LlmDataQueryException.class)
                .hasMessageContaining("generate");
    }

    @Test
    void testBlankGeneratedQueryBecomesLlmDataQueryException() {
        generationService.setJpql("  ");

        assertThatThrownBy(() -> service.generate(new LlmQueryGenerationRequest(PROMPT, List.of(), List.of())))
                .isInstanceOf(LlmDataQueryException.class);
    }

    @Test
    void testNullElementsOfAGeneratedListAreDropped() {
        // Nothing between the model and the query rejects a null element, and the query itself refuses one.
        //noinspection NullableProblems
        generationService.setResultProperties(Arrays.asList("orderNumber", null));
        //noinspection NullableProblems
        generationService.setWarnings(Arrays.asList(null, "Amounts are not converted"));

        LlmDataQuery query = service.generate(new LlmQueryGenerationRequest(PROMPT, List.of(), List.of()));

        assertThat(query.getResultProperties()).containsExactly("orderNumber");
        assertThat(query.getWarnings()).containsExactly("Amounts are not converted");
    }

    @Test
    void testGeneratedQueryDeclaresTheParametersItsTextReferences() {
        // The add-on rejects a query whose declared parameters differ from the ones its text uses, and a stored
        // query is rejected on every run rather than once here.
        generationService.setJpql("select o.number as orderNumber from sales_Order o "
                + "where o.date >= :dateFrom and o.date < :dateTo");
        generationService.setParameters(List.of(
                new GeneratedJpqlParameter("dateFrom", "java.time.LocalDate", LocalDate.of(2026, 8, 1)),
                new GeneratedJpqlParameter("customerName", "java.lang.String", "Acme")));

        LlmDataQuery query = service.generate(new LlmQueryGenerationRequest(PROMPT, List.of(), List.of()));

        assertThat(query.getParameters())
                .extracting(LlmQueryParameter::getName, LlmQueryParameter::getJavaType)
                .containsExactly(tuple("dateFrom", "java.time.LocalDate"), tuple("dateTo", ""));
    }

    @Test
    void testAColonInAGeneratedLiteralDeclaresNoParameter() {
        generationService.setJpql("select o.number as orderNumber from sales_Order o where o.code like 'urn:isbn%'");
        generationService.setParameters(List.of());

        LlmDataQuery query = service.generate(new LlmQueryGenerationRequest(PROMPT, List.of(), List.of()));

        assertThat(query.getParameters()).isEmpty();
    }

    @Test
    void testCheckingAQueryTellsItsProblemsWithoutRunningIt() {
        validationService.setIssueMessages(List.of("Unknown entity: sales_Ordr"));

        assertThat(service.validate(storedQuery())).containsExactly("Unknown entity: sales_Ordr");
        assertThat(validationService.getLastValidated().getJpql()).isEqualTo(storedQuery().getJpql());
    }

    @Test
    void testCheckedQueryCarriesNoArgumentValues() {
        service.validate(new LlmDataQuery(storedQuery().getJpql(), List.of("orderNumber"),
                List.of(new LlmQueryParameter("dateFrom", "java.time.LocalDate", LocalDate.of(2026, 8, 1))),
                null, List.of()));

        assertThat(validationService.getLastValidated().getParameters())
                .extracting(GeneratedJpqlParameter::getName, GeneratedJpqlParameter::getValue)
                .containsExactly(tuple("dateFrom", null));
    }

    protected LlmDataQuery storedQuery() {
        return new LlmDataQuery("select o.number as orderNumber from sales_Order o where o.date >= :dateFrom",
                List.of("orderNumber"),
                List.of(new LlmQueryParameter("dateFrom", "java.time.LocalDate", null)),
                "Orders since the given date", List.of());
    }
}
