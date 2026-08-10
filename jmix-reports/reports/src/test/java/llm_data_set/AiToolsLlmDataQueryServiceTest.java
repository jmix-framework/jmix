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
import io.jmix.reports.llm.LlmDataQuery;
import io.jmix.reports.llm.LlmDataQueryException;
import io.jmix.reports.llm.LlmQueryExecutionRequest;
import io.jmix.reports.llm.LlmQueryGenerationRequest;
import io.jmix.reports.llm.LlmQueryParameter;
import io.jmix.reports.llm.impl.AiToolsLlmDataQueryService;
import llm_data_set.test_support.TestEntityDataLoadGenerationService;
import llm_data_set.test_support.TestJpqlExecutionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
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
    protected AiToolsLlmDataQueryService service;

    @BeforeEach
    void setUp() {
        generationService = new TestEntityDataLoadGenerationService();
        executionService = new TestJpqlExecutionService();
        service = new AiToolsLlmDataQueryService();
        ReflectionTestUtils.setField(service, "entityDataLoadGenerationService", generationService);
        ReflectionTestUtils.setField(service, "jpqlExecutionService", executionService);
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
    void testListValuedParameterIsDescribedAsAListToUseWithIn() {
        service.generate(new LlmQueryGenerationRequest(PROMPT, List.of(
                new LlmQueryParameter("revenue_dynamic_header_year", "java.lang.Integer", List.of(2025, 2026), true),
                new LlmQueryParameter("dateFrom", "java.time.LocalDate", LocalDate.of(2026, 8, 1))), null));

        String userText = generationService.getLastUserText();
        assertThat(userText).contains("several values of this type, matched with IN");
        assertThat(userText).contains("no parentheses around the parameter name");
        assertThat(userText).containsPattern(":dateFrom \\(java\\.time\\.LocalDate\\)");
        assertThat(userText).contains("REQUIRED RESULT COLUMNS");
        assertThat(userText).contains("\n- revenue_dynamic_header_year");
        assertThat(userText).doesNotContain("\n- dateFrom");
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

        List<Map<String, Object>> rows = service.execute(
                new LlmQueryExecutionRequest(PROMPT, storedQuery(), List.of(), null));

        assertThat(rows).containsExactly(Map.of("orderNumber", "A-1"));
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

    protected LlmDataQuery storedQuery() {
        return new LlmDataQuery("select o.number as orderNumber from sales_Order o where o.date >= :dateFrom",
                List.of("orderNumber"),
                List.of(new LlmQueryParameter("dateFrom", "java.time.LocalDate", null)),
                "Orders since the given date", List.of(), 150);
    }
}
