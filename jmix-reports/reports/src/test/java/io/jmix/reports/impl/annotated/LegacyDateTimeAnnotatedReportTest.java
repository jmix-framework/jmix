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

package io.jmix.reports.impl.annotated;

import io.jmix.reports.test_support.report.RevenueByPublisherReport;
import io.jmix.reports.yarg.reporting.ReportOutputDocument;
import io.jmix.reports.yarg.structure.ReportOutputType;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = "jmix.reports.use-legacy-date-time-types=true")
class LegacyDateTimeAnnotatedReportTest extends BaseAnnotatedReportExecutionTest {

    @Test
    void testDefaultDateParametersArePassedAsDates() {
        ReportOutputDocument outputDocument = reportRunner.byReportCode(RevenueByPublisherReport.CODE)
                .run();

        assertThat(outputDocument.getReportOutputType()).isEqualTo(ReportOutputType.xlsx);
        assertThat(outputDocument.getContent()).isNotEmpty();

        Sheet firstSheet = readFirstSheetFromBytes(outputDocument.getContent());
        assertThat(stringCellValue(firstSheet, 0, 0)).isEqualTo("Revenue");
    }
}
