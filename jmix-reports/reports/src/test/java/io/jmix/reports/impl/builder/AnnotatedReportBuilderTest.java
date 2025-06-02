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

package io.jmix.reports.impl.builder;

import io.jmix.core.MetadataTools;
import io.jmix.outside_reports.SimpleReport;
import io.jmix.reports.ReportsTestConfiguration;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportSource;
import io.jmix.reports.test_support.AuthenticatedAsSystem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({SpringExtension.class, AuthenticatedAsSystem.class})
@ContextConfiguration(classes = {ReportsTestConfiguration.class})
public class AnnotatedReportBuilderTest {

    @Autowired
    private AnnotatedReportBuilder annotatedReportBuilder;

    @Autowired
    private MetadataTools metadataTools;

    @Test
    public void testSuccessfulImport() {
        // given
        SimpleReport definition = new SimpleReport();

        // when
        Report report = annotatedReportBuilder.createReportFromDefinition(definition);

        // then
        assertThat(report).isNotNull();
        assertThat(report.getCode()).isEqualTo("simple-report");
        assertThat(report.getId()).isEqualTo(UUID.fromString("01973162-6761-71a1-9d6b-b4ef0bea42f0"));
        assertThat(report.getSource()).isEqualTo(ReportSource.ANNOTATED_CLASS);
        assertThat(report.getRestAccess()).isTrue();

        // todo assert remaining structure elements
    }

}
