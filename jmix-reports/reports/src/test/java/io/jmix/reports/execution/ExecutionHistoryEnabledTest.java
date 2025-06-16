/*
 * Copyright (c) 2008-2019 Haulmont.
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

package io.jmix.reports.execution;

import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportExecution;
import io.jmix.reports.impl.builder.AnnotatedReportBuilder;
import io.jmix.reports.runner.ReportRunner;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestPropertySource(properties = {
        "jmix.reports.history-recording-enabled=true",
        "jmix.reports.save-output-documents-to-history=false"
})
public class ExecutionHistoryEnabledTest extends BaseExecutionHistoryTest {

    @Autowired
    ReportRunner reportRunner;
    @Autowired
    AnnotatedReportBuilder annotatedReportBuilder;

    @Test
    public void testSuccess() {
        // given
        Report report = annotatedReportBuilder.createReportFromDefinition(new ReportForHistory());
        Double input = 15.0;

        // when
        reportRunner.byReportEntity(report)
                .addParam(ReportForHistory.PARAM_INPUT, input)
                .run();

        // then
        ReportExecution execution = loadExecution(unconstrainedDataManager, ReportForHistory.CODE);
        assertThat(execution).isNotNull();
        assertThat(execution.getFinishTime()).isNotNull();
        assertThat(execution.getReportName()).isEqualTo(ReportForHistory.NAME);
        assertThat(execution.getReport()).isNull();
        assertThat(execution.getSuccess()).isTrue();
        assertThat(execution.getCancelled()).isFalse();
        assertThat(execution.getParams()).contains("key: input, value: 15.0");
        assertThat(execution.getErrorMessage()).isNull();
    }

    @Test
    public void testFailure() {
        // given
        Report report = annotatedReportBuilder.createReportFromDefinition(new ReportForHistory());
        Double input = 0.0; // causes division by zero

        // when
        assertThatThrownBy(() ->
                reportRunner.byReportEntity(report)
                        .addParam(ReportForHistory.PARAM_INPUT, input)
                        .run()
        );

        // then
        ReportExecution execution = loadExecution(unconstrainedDataManager, ReportForHistory.CODE);
        assertThat(execution).isNotNull();
        assertThat(execution.getFinishTime()).isNotNull();
        assertThat(execution.getSuccess()).isFalse();
        assertThat(execution.getCancelled()).isFalse();
        assertThat(execution.getErrorMessage()).contains("/ by zero");
    }

    @Test
    public void testSaveDocumentDisabled() {
        // given
        Report report = annotatedReportBuilder.createReportFromDefinition(new ReportForHistory());

        // when
        reportRunner.byReportEntity(report)
                .run();

        // then
        ReportExecution execution = loadExecution(unconstrainedDataManager, ReportForHistory.CODE);
        assertThat(execution).isNotNull();
        assertThat(execution.getSuccess()).isTrue();
        assertThat(execution.getOutputDocument()).isNull();
    }
}
