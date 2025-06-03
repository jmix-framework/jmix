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
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.outside_reports.CorrectReportGroup;
import io.jmix.outside_reports.SimpleReport;
import io.jmix.reports.ReportsTestConfiguration;
import io.jmix.reports.entity.*;
import io.jmix.reports.impl.AnnotatedReportGroupProvider;
import io.jmix.reports.test_support.AuthenticatedAsSystem;
import io.jmix.reports.test_support.CurrentAuthenticationMock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.convention.TestBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collection;
import java.util.Locale;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({SpringExtension.class, AuthenticatedAsSystem.class})
@ContextConfiguration(classes = {ReportsTestConfiguration.class})
public class AnnotatedReportBuilderTest {

    @Autowired
    private AnnotatedReportBuilder annotatedReportBuilder;

    @Autowired
    private MetadataTools metadataTools;

    // test stub for AnnotatedReportGroupProvider which is requested to provide ReportGroup when report is imported
    @TestBean(name = "reports_AnnotatedReportGroupProvider", methodName = "groupProviderMockBean")
    private AnnotatedReportGroupProvider annotatedReportGroupProvider;

    @TestBean(name = "core_CurrentAuthentication", methodName = "currentAuthenticationMock")
    private CurrentAuthentication currentAuthentication;

    static AnnotatedReportGroupProvider groupProviderMockBean() {
        return new MockingAnnotatedReportGroupProvider();
    }

    static CurrentAuthentication currentAuthenticationMock() {
        return new CurrentAuthenticationMock();
    }

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

        // then: group
        assertThat(report.getGroup().getCode()).isEqualTo(CorrectReportGroup.CODE);

        // then: input parameters
        assertThat(report.getInputParameters()).hasSize(1);
        ReportInputParameter inputParameter = report.getInputParameters().get(0);
        assertThat(inputParameter.getAlias()).isEqualTo("afterDate");
        assertThat(inputParameter.getRequired()).isEqualTo(Boolean.TRUE);

        // then: bands
        assertThat(report.getBands()).hasSize(2);

        assertThat(report.getRootBandDefinition()).isNotNull();
        assertThat(report.getRootBandDefinition().getName()).isEqualTo("Root");
        assertThat(report.getRootBandDefinition().getDataSets()).isEmpty();
        assertThat(report.getRootBandDefinition().getChildrenBandDefinitions()).hasSize(1);

        var childBand = report.getRootBandDefinition().getChildrenBandDefinitions().get(0);
        assertThat(childBand.getName()).isEqualTo("title");
        assertThat(childBand.getChildrenBandDefinitions()).isEmpty();
        assertThat(childBand.getDataSets()).hasSize(1);

        DataSet dataSet = childBand.getDataSets().get(0);
        assertThat(dataSet.getName()).isEqualTo("title");
        assertThat(dataSet.getType()).isEqualTo(DataSetType.DELEGATE);
        assertThat(dataSet.getLoaderDelegate()).isNotNull();

        // then: template
        assertThat(report.getTemplates()).hasSize(1);
        var template = report.getTemplates().get(0);
        assertThat(template.getCode()).isEqualTo("default");
        assertThat(template.getReportOutputType()).isEqualTo(ReportOutputType.CSV);
        assertThat(template.getOutputNamePattern()).isEqualTo("${title.caption}.csv");
        assertThat(template.getContent()).isNotNull();
        assertThat(template.getCustom()).isFalse();

        // then: value format
        assertThat(report.getValuesFormats()).hasSize(1);
        var valueFormat = report.getValuesFormats().get(0);
        assertThat(valueFormat.getValueName()).isEqualTo("title.date");
        assertThat(valueFormat.getFormatString()).isEqualTo("dd.MM.yyyy HH:mm:ss");
    }

    @ParameterizedTest
    @CsvSource({"en,After", "fr,Apres"})
    public void testInputParameterLocalization(String localeCode, String expectedCaption) {
        // given
        ((CurrentAuthenticationMock) currentAuthentication).setLocale(Locale.forLanguageTag(localeCode));
        SimpleReport definition = new SimpleReport();

        // when
        Report report = annotatedReportBuilder.createReportFromDefinition(definition);
        ReportInputParameter reportParameter = report.getInputParameters().get(0);

        // then
        assertThat(metadataTools.getInstanceName(reportParameter)).isEqualTo(expectedCaption);
    }

    private static class MockingAnnotatedReportGroupProvider implements AnnotatedReportGroupProvider {

        @Override
        public Collection<ReportGroup> getAllGroups() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ReportGroup getGroupByCode(String code) {
            // support just one test group
            if (code.equals(CorrectReportGroup.CODE)) {
                ReportGroup group = new ReportGroup();
                group.setCode(code);
                return group;
            }
            return null;
        }

        @Override
        public void importGroupDefinitions() {
            throw new UnsupportedOperationException();
        }
    }
}
