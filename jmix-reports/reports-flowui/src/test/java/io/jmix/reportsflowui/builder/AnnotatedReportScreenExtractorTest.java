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

package io.jmix.reportsflowui.builder;

import io.jmix.outside_reportsflowui.ReportWithViews;
import io.jmix.outside_reportsflowui.WrongViewsReport;
import io.jmix.reports.entity.Report;
import io.jmix.reports.impl.builder.AnnotatedReportBuilder;
import io.jmix.reports.impl.builder.InvalidReportDefinitionException;
import io.jmix.reportsflowui.ReportsFlowuiTestConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith({SpringExtension.class})
@ContextConfiguration(classes = {ReportsFlowuiTestConfiguration.class})
public class AnnotatedReportScreenExtractorTest {

    @Autowired
    private AnnotatedReportBuilder annotatedReportBuilder;

    @Test
    public void testAvailableInViews() {
        // given
        ReportWithViews  definition = new ReportWithViews ();

        // when
        Report report = annotatedReportBuilder.createReportFromDefinition(definition);

        // then
        assertThat(report.getReportScreens()).hasSize(2);

        assertThat(report.getReportScreens())
                .anyMatch(rs -> rs.getScreenId().equals("reports_test_View1"));

        assertThat(report.getReportScreens())
                .anyMatch(rs -> rs.getScreenId().equals("reports_test_View2"));
    }

    @Test
    public void testWrongViews() {
        // given
        WrongViewsReport definition = new WrongViewsReport();

        // when+then
        assertThatThrownBy(() -> annotatedReportBuilder.createReportFromDefinition(definition))
                .isInstanceOf(InvalidReportDefinitionException.class)
                .hasMessageContaining("Invalid value for viewClasses")
                .cause()
                    .hasMessageContaining("No @ViewController");
    }
}
