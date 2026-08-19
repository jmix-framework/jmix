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

import io.jmix.outside_reports.OrderNumbersLlmReport;
import io.jmix.reports.ReportsTestConfiguration;
import io.jmix.reports.impl.AnnotatedReportHolder;
import io.jmix.reports.impl.AnnotatedReportScanner;
import io.jmix.reports.llm.LlmQueryGenerationRequest;
import io.jmix.reports.runner.ReportRunner;
import io.jmix.reports.test_support.AuthenticatedAsSystem;
import io.jmix.reports.yarg.reporting.ReportOutputDocument;
import llm_data_set.test_support.LlmDataSetTestConfiguration;
import llm_data_set.test_support.TestLlmDataQueryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An LLM data set declared with {@code @DataSetDef} rather than built in the designer: the prompt and the row
 * limit have to reach the run, and the query has to be generated for it, because such a report stores none.
 */
@ExtendWith({SpringExtension.class, AuthenticatedAsSystem.class})
@ContextConfiguration(classes = {ReportsTestConfiguration.class, LlmDataSetTestConfiguration.class})
class AnnotatedLlmReportRunTest {

    @Autowired
    protected ReportRunner reportRunner;

    @Autowired
    protected AnnotatedReportScanner annotatedReportScanner;

    @Autowired
    protected AnnotatedReportHolder annotatedReportHolder;

    @Autowired
    protected TestLlmDataQueryService queryService;

    @BeforeEach
    void setUp() {
        queryService.reset();
        // Startup events do not fire in tests, so the definition is loaded by hand. Only this one: it lives
        // outside the scanned package, where it cannot disturb the tests that count the scanned reports.
        if (annotatedReportHolder.getByCode(OrderNumbersLlmReport.CODE) == null) {
            annotatedReportScanner.loadReportClass(OrderNumbersLlmReport.class.getName());
        }
    }

    @AfterEach
    void tearDown() {
        // The holder is shared with every other test declaring this context; leaving a definition in it is
        // what makes a suite order-dependent.
        annotatedReportHolder.clear();
    }

    @Test
    void testAnnotatedLlmBandIsGeneratedAndRendered() {
        queryService.setRows(List.of(Map.of("orderNumber", "A-1"), Map.of("orderNumber", "A-2")));

        ReportOutputDocument document = reportRunner.byReportCode(OrderNumbersLlmReport.CODE).run();

        assertThat(new String(document.getContent(), StandardCharsets.UTF_8))
                .contains("\"A-1\"")
                .contains("\"A-2\"");
    }

    @Test
    void testThePromptAndTheRowLimitOfTheAnnotationReachGeneration() {
        reportRunner.byReportCode(OrderNumbersLlmReport.CODE).run();

        LlmQueryGenerationRequest request = queryService.getLastGenerationRequest();
        assertThat(request.getPrompt()).isEqualTo("Order numbers of this month");
        assertThat(request.getMaxResults()).isEqualTo(50);
    }
}
