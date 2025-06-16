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

import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageLocator;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportExecution;
import io.jmix.reports.impl.builder.AnnotatedReportBuilder;
import io.jmix.reports.runner.ReportRunner;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = {
        "jmix.reports.history-recording-enabled=true",
        "jmix.reports.save-output-documents-to-history=true"
})
public class ExecutionHistoryWithSavingDocumentsTest extends BaseExecutionHistoryTest {
    @Autowired
    FileStorageLocator fileStorageLocator;
    @Autowired
    ReportRunner reportRunner;
    @Autowired
    AnnotatedReportBuilder annotatedReportBuilder;

    @Test
    public void testHistoryWithSaveDocument() throws Exception {
        // given
        Report report = annotatedReportBuilder.createReportFromDefinition(new ReportForHistory());

        // when
        reportRunner.byReportEntity(report)
                .run();

        // then
        ReportExecution execution = loadExecution(unconstrainedDataManager, ReportForHistory.CODE);
        assertThat(execution).isNotNull();
        assertThat(execution.getSuccess()).isTrue();
        assertThat(execution.getOutputDocument()).isNotNull();
        assertThat(execution.getOutputDocument().getFileName()).isEqualTo("ReportForHistory.csv");

        FileStorage fileStorage = fileStorageLocator.getDefault();
        byte[] bytes = IOUtils.toByteArray(fileStorage.openStream(execution.getOutputDocument()));
        assertThat(bytes).isNotEmpty();
        String csv = new String(bytes, StandardCharsets.UTF_8);
        assertThat(csv).contains("Output");
        assertThat(csv).contains("10");
    }
}
