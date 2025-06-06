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

import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.reports.test_support.entity.UserRegistration;
import io.jmix.reports.test_support.report.RevenueByGameReport;
import io.jmix.reports.test_support.report.UserProfileReport;
import io.jmix.reports.yarg.reporting.ReportOutputDocument;
import io.jmix.reports.yarg.structure.ReportOutputType;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
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

    @Test
    public void testCustomTemplate() throws DocumentException {
        // given
        String reportCode = UserProfileReport.CODE;
        String username = "lola18";

        // when
        UserRegistration user = unconstrainedDataManager.load(UserRegistration.class)
                .condition(PropertyCondition.equal("username", username))
                .one();
        ReportOutputDocument outputDocument = reportRunner.byReportCode(reportCode)
                .addParam(UserProfileReport.PARAM_USER, user)
                // default template
                .run();

        // then
        assertThat(outputDocument.getDocumentName()).endsWith(".xml");
        assertThat(outputDocument.getReportOutputType()).isEqualTo(ReportOutputType.custom);
        assertThat(outputDocument.getContent()).isNotNull();

        // few basic checks of content, more checks in other tests
        String xml = new String(outputDocument.getContent(), StandardCharsets.UTF_8);
        Document document = DocumentHelper.parseText(xml); // valid xml
        Element root = document.getRootElement();
        assertThat(root.getName()).isEqualTo("Profile");
    }

    private Date parseDate(String isoDateString) {
        return Date.from(
                LocalDate.parse(isoDateString, DateTimeFormatter.ISO_LOCAL_DATE)
                        .atTime(0, 0)
                        .toInstant(ZoneOffset.UTC)
        );
    }
}
