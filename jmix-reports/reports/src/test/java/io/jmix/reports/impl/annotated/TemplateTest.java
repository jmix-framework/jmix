/*
 * Copyright 2025 Haulmont.
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

import io.jmix.reports.test_support.report.RevenueByGameReport;
import io.jmix.reports.yarg.reporting.ReportOutputDocument;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class TemplateTest extends BaseAnnotatedReportExecutionTest {

    @Test
    public void testOutputNamePattern() {
        // given
        String reportCode = RevenueByGameReport.CODE;
        Date startDate = parseDate("2025-01-15");
        Date endDate = parseDate("2025-05-08");

        // when
        ReportOutputDocument outputDocument = reportRunner.byReportCode(reportCode)
                .addParam(RevenueByGameReport.PARAM_START_DATE, startDate)
                .addParam(RevenueByGameReport.PARAM_END_DATE, endDate)
                .run();

        // then
        assertThat(outputDocument.getDocumentName()).isEqualTo("Revenue 15.01 - 08.05.csv");
    }

    private Date parseDate(String isoDateString) {
        return Date.from(
                LocalDate.parse(isoDateString, DateTimeFormatter.ISO_LOCAL_DATE)
                        .atTime(0, 0)
                        .toInstant(ZoneOffset.UTC)
        );
    }
}
