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
import io.jmix.aitools.dataload.execution.JpqlExecutionParameter;
import io.jmix.aitools.dataload.execution.JpqlExecutionRequest;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.reports.llm.LlmDataQuery;
import io.jmix.reports.llm.LlmDataQueryException;
import io.jmix.reports.llm.LlmQueryExecutionRequest;
import io.jmix.reports.llm.LlmQueryExecutionResult;
import io.jmix.reports.llm.LlmQueryGenerationRequest;
import io.jmix.reports.llm.LlmQueryParameter;
import io.jmix.reports.llm.impl.AiToolsLlmDataQueryService;
import llm_data_set.test_support.TestEntityDataLoadGenerationService;
import llm_data_set.test_support.TestJpqlExecutionService;
import llm_data_set.test_support.TestJpqlValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

/**
 * The mapping between the Reports-side seam and the AI Tools data-load services.
 */
class AiToolsLlmDataQueryServiceTest {

    protected static final String PROMPT = "Orders placed since the given date";

    protected TestEntityDataLoadGenerationService generationService;
    protected TestJpqlExecutionService executionService;
    protected TestJpqlValidationService validationService;
    protected AiToolsLlmDataQueryService service;

    @BeforeEach
    void setUp() {
        generationService = new TestEntityDataLoadGenerationService();
        executionService = new TestJpqlExecutionService();
        validationService = new TestJpqlValidationService();
        service = new AiToolsLlmDataQueryService();
        ReflectionTestUtils.setField(service, "entityDataLoadGenerationService", generationService);
        ReflectionTestUtils.setField(service, "jpqlExecutionService", executionService);
        ReflectionTestUtils.setField(service, "jpqlValidationService", validationService);
    }

    @Test
    void testPromptAndAvailableParametersReachGeneration() {
        service.generate(new LlmQueryGenerationRequest(PROMPT,
                List.of(new LlmQueryParameter("dateFrom", "java.time.LocalDate", null)), 300));

        String userText = generationService.getLastUserText();
        assertThat(userText).contains(PROMPT);
        assertThat(userText).contains("dateFrom");
        assertThat(userText).contains("java.time.LocalDate");
        assertThat(userText).contains(":dateFrom");
        assertThat(userText).contains("300");
    }

    @Test
    void testListValuedParameterUsesInWithoutBecomingACrossTabAxis() {
        service.generate(new LlmQueryGenerationRequest(PROMPT, List.of(
                new LlmQueryParameter("customerIds", "java.util.UUID", List.of("1", "2"), true),
                new LlmQueryParameter("dateFrom", "java.time.LocalDate", LocalDate.of(2026, 8, 1))), null));

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
                List.of("revenue_dynamic_header_year"), null));

        String userText = generationService.getLastUserText();
        assertThat(userText).contains("REQUIRED RESULT COLUMNS");
        assertThat(userText).contains("\n- revenue_dynamic_header_year");
        assertThat(userText).contains("matching that parameter with IN");
    }

    @Test
    void testScalarParameterShadowingRequiredColumnDoesNotUseIn() {
        service.generate(new LlmQueryGenerationRequest(PROMPT, List.of(
                new LlmQueryParameter("revenue_dynamic_header_year", "java.lang.Integer", 2026)),
                List.of("revenue_dynamic_header_year"), null));

        String userText = generationService.getLastUserText();
        assertThat(userText).contains("REQUIRED RESULT COLUMNS");
        assertThat(userText).contains("\n- revenue_dynamic_header_year");
        assertThat(userText).doesNotContain("matching that parameter with IN");
    }

    @Test
    void testRequiredResultColumnsDoNotDependOnAvailableParameters() {
        service.generate(new LlmQueryGenerationRequest(PROMPT, List.of(),
                List.of("revenue_dynamic_header_year"), null));

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
        generationService.setMaxResults(120);

        LlmDataQuery query = service.generate(new LlmQueryGenerationRequest(PROMPT, List.of(), null));

        assertThat(query.getJpql()).contains("select o.number as orderNumber");
        assertThat(query.getResultProperties()).containsExactly("orderNumber");
        assertThat(query.getParameters())
                .extracting(LlmQueryParameter::getName, LlmQueryParameter::getJavaType)
                .containsExactly(tuple("dateFrom", "java.lang.String"));
        assertThat(query.getParameters().get(0).getValue()).isNull();
        assertThat(query.getExplanation()).isEqualTo("Orders since the given date");
        assertThat(query.getWarnings()).containsExactly("Time zone ignored");
        assertThat(query.getMaxResults()).isEqualTo(120);
    }

    @Test
    void testGenerationFailureBecomesLlmDataQueryException() {
        generationService.setFailure(new IllegalStateException("LLM is not configured"));

        assertThatThrownBy(() -> service.generate(new LlmQueryGenerationRequest(PROMPT, List.of(), null)))
                .isInstanceOf(LlmDataQueryException.class)
                .hasMessageContaining("generate");
    }

    @Test
    void testBlankGeneratedQueryBecomesLlmDataQueryException() {
        generationService.setJpql("  ");

        assertThatThrownBy(() -> service.generate(new LlmQueryGenerationRequest(PROMPT, List.of(), null)))
                .isInstanceOf(LlmDataQueryException.class);
    }

    @Test
    void testQueryPromptAndArgumentsReachExecution() {
        LocalDate dateFrom = LocalDate.of(2026, 8, 1);

        service.execute(new LlmQueryExecutionRequest(PROMPT, storedQuery(),
                List.of(new LlmQueryParameter("dateFrom", "java.time.LocalDate", dateFrom)), 700));

        JpqlExecutionRequest request = executionService.getLastRequest();
        assertThat(request.getUserText()).isEqualTo(PROMPT);
        assertThat(request.getJpql()).isEqualTo(storedQuery().getJpql());
        assertThat(request.getResultProperties()).containsExactly("orderNumber");
        assertThat(request.getMaxResults()).isEqualTo(700);
        assertThat(request.getParameters())
                .extracting(JpqlExecutionParameter::getName, JpqlExecutionParameter::getType,
                        JpqlExecutionParameter::getValue)
                .containsExactly(tuple("dateFrom", "java.time.LocalDate", dateFrom));
    }

    @Test
    void testRowLimitFallsBackToTheQueryLimit() {
        service.execute(new LlmQueryExecutionRequest(PROMPT, storedQuery(), List.of(), null));

        assertThat(executionService.getLastRequest().getMaxResults()).isEqualTo(150);
    }

    @Test
    void testFetchedRowsAreReturned() {
        executionService.setRows(List.of(Map.of("orderNumber", "A-1")));

        LlmQueryExecutionResult result = service.execute(
                new LlmQueryExecutionRequest(PROMPT, storedQuery(), List.of(), null));

        assertThat(result.getRows()).containsExactly(Map.of("orderNumber", "A-1"));
        assertThat(result.isTruncated()).isFalse();
    }

    @Test
    void testRowsCutShortByTheLimitAreReportedAsTruncated() {
        executionService.setRows(List.of(Map.of("orderNumber", "A-1")));
        executionService.setHasMore(true);

        LlmQueryExecutionResult result = service.execute(
                new LlmQueryExecutionRequest(PROMPT, storedQuery(), List.of(), null));

        assertThat(result.isTruncated()).isTrue();
    }

    @Test
    void testNotExecutedResultBecomesLlmDataQueryException() {
        executionService.setExecuted(false);
        executionService.setExecutionError("table not found");

        assertThatThrownBy(() -> service.execute(new LlmQueryExecutionRequest(PROMPT, storedQuery(), List.of(), null)))
                .isInstanceOf(LlmDataQueryException.class)
                .hasMessageContaining("table not found");
    }

    @Test
    void testInvalidQueryReportsItsValidationIssues() {
        executionService.setExecuted(false);
        executionService.setValid(false);
        executionService.setIssueMessage("parameter.missingInDto: dateFrom");

        assertThatThrownBy(() -> service.execute(new LlmQueryExecutionRequest(PROMPT, storedQuery(), List.of(), null)))
                .isInstanceOf(LlmDataQueryException.class)
                .hasMessageContaining("dateFrom");
    }

    @Test
    void testFailureBeforeTheAddOnsOwnHandlingBecomesLlmDataQueryException() {
        // The add-on validates, repairs and converts parameters outside its own try block, so a failure there
        // would otherwise leave the seam as an exception of an unrelated kind.
        executionService.setFailure(new IllegalStateException("LLM returned an empty response"));

        assertThatThrownBy(() -> service.execute(new LlmQueryExecutionRequest(PROMPT, storedQuery(), List.of(), null)))
                .isInstanceOf(LlmDataQueryException.class)
                .cause()
                .hasMessageContaining("empty response");
    }

    @Test
    void testAccessDeniedIsReportedAsItIs() {
        // Being refused the data is not a failure of the seam, and the run says so in its own words.
        executionService.setFailure(new AccessDeniedException("entity", "sales_Order"));

        assertThatThrownBy(() -> service.execute(new LlmQueryExecutionRequest(PROMPT, storedQuery(), List.of(), null)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void testNullElementsOfAGeneratedListAreDropped() {
        // Nothing between the model and the query rejects a null element, and the query itself refuses one.
        generationService.setResultProperties(Arrays.asList("orderNumber", null));
        generationService.setWarnings(Arrays.asList(null, "Amounts are not converted"));

        LlmDataQuery query = service.generate(new LlmQueryGenerationRequest(PROMPT, List.of(), null));

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

        LlmDataQuery query = service.generate(new LlmQueryGenerationRequest(PROMPT, List.of(), null));

        assertThat(query.getParameters())
                .extracting(LlmQueryParameter::getName, LlmQueryParameter::getJavaType)
                .containsExactly(tuple("dateFrom", "java.time.LocalDate"), tuple("dateTo", ""));
    }

    @Test
    void testAColonInAGeneratedLiteralDeclaresNoParameter() {
        generationService.setJpql("select o.number as orderNumber from sales_Order o where o.code like 'urn:isbn%'");
        generationService.setParameters(List.of());

        LlmDataQuery query = service.generate(new LlmQueryGenerationRequest(PROMPT, List.of(), null));

        assertThat(query.getParameters()).isEmpty();
    }

    @Test
    void testExecutionDoesNotCheckTheQueryAgain() {
        // A query is checked once per run, by LlmDataLoader, and executing it for every row of a parent band
        // must not repeat a check that parses the text and resolves it against the data model.
        service.execute(new LlmQueryExecutionRequest(PROMPT, storedQuery(), List.of(), null));

        assertThat(validationService.getValidations()).isZero();
    }

    @Test
    void testCheckingAQueryTellsItsProblemsWithoutRunningIt() {
        validationService.setIssueMessages(List.of("Unknown entity: sales_Ordr"));

        assertThat(service.validate(storedQuery())).containsExactly("Unknown entity: sales_Ordr");
        assertThat(validationService.getLastValidated().getJpql()).isEqualTo(storedQuery().getJpql());
        assertThat(executionService.getLastRequest().getJpql()).isNull();
    }

    @Test
    void testCheckedQueryCarriesNoArgumentValues() {
        service.validate(new LlmDataQuery(storedQuery().getJpql(), List.of("orderNumber"),
                List.of(new LlmQueryParameter("dateFrom", "java.time.LocalDate", LocalDate.of(2026, 8, 1))),
                null, List.of(), null));

        assertThat(validationService.getLastValidated().getParameters())
                .extracting(GeneratedJpqlParameter::getName, GeneratedJpqlParameter::getValue)
                .containsExactly(tuple("dateFrom", null));
    }

    @Test
    void testAQueryOfColumnsTheUserMayNotReadSaysSo() {
        // The add-on reports this one by executing nothing at all: valid, no error, nothing returned.
        executionService.setExecuted(false);

        assertThatThrownBy(() -> service.execute(new LlmQueryExecutionRequest(PROMPT, storedQuery(), List.of(), null)))
                .isInstanceOf(LlmDataQueryException.class)
                .hasMessageContaining("read none of the columns");
    }

    protected LlmDataQuery storedQuery() {
        return new LlmDataQuery("select o.number as orderNumber from sales_Order o where o.date >= :dateFrom",
                List.of("orderNumber"),
                List.of(new LlmQueryParameter("dateFrom", "java.time.LocalDate", null)),
                "Orders since the given date", List.of(), 150);
    }
}
