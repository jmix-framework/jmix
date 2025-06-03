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
import io.jmix.outside_reports.*;
import io.jmix.reports.ReportsTestConfiguration;
import io.jmix.reports.entity.*;
import io.jmix.reports.impl.AnnotatedReportGroupHolder;
import io.jmix.reports.test_support.AuthenticatedAsSystem;
import io.jmix.reports.test_support.CurrentAuthenticationMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.convention.TestBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Locale;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith({SpringExtension.class, AuthenticatedAsSystem.class})
@ContextConfiguration(classes = {ReportsTestConfiguration.class})
public class AnnotatedReportBuilderTest {

    @Autowired
    private AnnotatedReportBuilder annotatedReportBuilder;

    @Autowired
    private AnnotatedReportGroupHolder reportGroupHolder;

    @Autowired
    private MetadataTools metadataTools;

    @TestBean(name = "core_CurrentAuthentication", methodName = "currentAuthenticationMock")
    private CurrentAuthentication currentAuthentication;

    private ReportGroup correctGroup;

    static CurrentAuthentication currentAuthenticationMock() {
        return new CurrentAuthenticationMock();
    }

    @BeforeEach
    void setUp() {
        correctGroup = new ReportGroup();
        correctGroup.setCode(CorrectReportGroup.CODE);
        reportGroupHolder.put(correctGroup);
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
        assertThat(report.getGroup()).isEqualTo(correctGroup);

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

    @Test
    public void testWrongUuid() {
        // given
        WrongUuidReport definition = new WrongUuidReport();

        // when+then
        assertThatThrownBy(() -> annotatedReportBuilder.createReportFromDefinition(definition))
                .isInstanceOf(InvalidReportDefinitionException.class)
                .hasMessageContaining("Invalid UUID");
    }

    @Test
    public void testWrongGroupClass() {
        // given
        WrongGroupReport definition = new WrongGroupReport();

        // when+then
        assertThatThrownBy(() -> annotatedReportBuilder.createReportFromDefinition(definition))
                .isInstanceOf(InvalidReportDefinitionException.class)
                .hasMessageContaining("Report group");
    }

    @Test
    public void testNoBands() {
        // given
        NoBandsReport definition = new NoBandsReport();

        // when+then
        assertThatThrownBy(() -> annotatedReportBuilder.createReportFromDefinition(definition))
                .isInstanceOf(InvalidReportDefinitionException.class)
                .hasMessageContaining("at least one band");
    }

    @Test
    public void testMissingRootBand() {
        // given
        MissingRootBandReport definition = new MissingRootBandReport();

        // when+then
        assertThatThrownBy(() -> annotatedReportBuilder.createReportFromDefinition(definition))
                .isInstanceOf(InvalidReportDefinitionException.class)
                .hasMessageContaining("root band");
    }

    @Test
    public void testTwoRootBands() {
        // given
        TwoRootBandsReport definition = new TwoRootBandsReport();

        // when+then
        assertThatThrownBy(() -> annotatedReportBuilder.createReportFromDefinition(definition))
                .isInstanceOf(InvalidReportDefinitionException.class)
                .hasMessageContaining("More than one root band");
    }

    @Test
    public void testInvalidBandParent() {
        // given
        InvalidBandParentReport definition = new InvalidBandParentReport();

        // when+then
        assertThatThrownBy(() -> annotatedReportBuilder.createReportFromDefinition(definition))
                .isInstanceOf(InvalidReportDefinitionException.class)
                .hasMessageContaining("Parent band")
                .hasMessageContaining("is not defined");
    }

    @Test
    public void testMissingDataSetDelegate() {
        // given
        MissingDataSetDelegateReport definition = new MissingDataSetDelegateReport();

        // when+then
        assertThatThrownBy(() -> annotatedReportBuilder.createReportFromDefinition(definition))
                .isInstanceOf(InvalidReportDefinitionException.class)
                .hasMessageContaining("Dataset must have")
                .hasMessageContaining("delegate");
    }

    @Test
    public void testWrongDataSetNameInDelegate() {
        // given
        WrongDataSetNameInDelegateReport definition = new WrongDataSetNameInDelegateReport();

        // when+then
        assertThatThrownBy(() -> annotatedReportBuilder.createReportFromDefinition(definition))
                .isInstanceOf(InvalidReportDefinitionException.class)
                .hasMessageContaining("doesn't contain data set with name");
    }

    @Test
    public void testWrongDelegateResultType() {
        // given
        WrongDelegateResultTypeReport definition = new WrongDelegateResultTypeReport();

        // when+then
        assertThatThrownBy(() -> annotatedReportBuilder.createReportFromDefinition(definition))
                .isInstanceOf(InvalidReportDefinitionException.class)
                .hasMessageContaining("Unsupported result type");
    }

    @Test
    public void testWrongDataSetTypeForLoaderDelegate() {
        // given
        WrongDataSetTypeForDataLoaderReport definition = new WrongDataSetTypeForDataLoaderReport();

        // when+then
        assertThatThrownBy(() -> annotatedReportBuilder.createReportFromDefinition(definition))
                .isInstanceOf(InvalidReportDefinitionException.class)
                .hasMessageContaining("Dataset type")
                .hasMessageContaining("expected: DELEGATE");
    }

    @Test
    public void testWrongEntityParameterAlias() {
        // given
        WrongEntityParameterAliasReport definition = new WrongEntityParameterAliasReport();

        // when+then
        assertThatThrownBy(() -> annotatedReportBuilder.createReportFromDefinition(definition))
                .isInstanceOf(InvalidReportDefinitionException.class)
                .hasMessageContaining("doesn't contain input parameter with alias");
    }

    @Test
    public void testDuplicateParameterAlias() {
        // given
        DuplicateParameterAliasReport definition = new DuplicateParameterAliasReport();

        // when+then
        assertThatThrownBy(() -> annotatedReportBuilder.createReportFromDefinition(definition))
                .isInstanceOf(InvalidReportDefinitionException.class)
                .hasMessageContaining("Duplicate input parameter alias");
    }

    @Test
    public void testNoTemplates() {
        // given
        NoTemplatesReport definition = new NoTemplatesReport();

        // when+then
        assertThatThrownBy(() -> annotatedReportBuilder.createReportFromDefinition(definition))
                .isInstanceOf(InvalidReportDefinitionException.class)
                .hasMessageContaining("at least one template");
    }

    @Test
    public void testMissingTemplateContent() {
        // given
        MissingTemplateContentReport definition = new MissingTemplateContentReport();

        // when+then
        assertThatThrownBy(() -> annotatedReportBuilder.createReportFromDefinition(definition))
                .isInstanceOf(InvalidReportDefinitionException.class)
                .hasMessageContaining("filePath is mandatory");
    }

    @Test
    public void testWrongTemplateFilePath() {
        // given
        WrongTemplateFilePathReport definition = new WrongTemplateFilePathReport();

        // when+then
        assertThatThrownBy(() -> annotatedReportBuilder.createReportFromDefinition(definition))
                .isInstanceOf(InvalidReportDefinitionException.class)
                .hasMessageContaining("file does not exist");
    }

    @Test
    public void testGroovyScriptTemplate() {
        // given
        GroovyScriptTemplateReport  definition = new GroovyScriptTemplateReport();

        // when+then
        assertThatThrownBy(() -> annotatedReportBuilder.createReportFromDefinition(definition))
                .isInstanceOf(InvalidReportDefinitionException.class)
                .hasMessageContaining("SCRIPT")
                .hasMessageContaining("not supported");
    }

    @Test
    public void testWrongValueFormatBand() {
        // given
        WrongValueFormatBandReport  definition = new WrongValueFormatBandReport();

        // when+then
        assertThatThrownBy(() -> annotatedReportBuilder.createReportFromDefinition(definition))
                .isInstanceOf(InvalidReportDefinitionException.class)
                .hasMessageContaining("doesn't contain band");
    }

    @Test
    public void testMissingFormatString() {
        // given
        MissingFormatStringReport  definition = new MissingFormatStringReport();

        // when+then
        assertThatThrownBy(() -> annotatedReportBuilder.createReportFromDefinition(definition))
                .isInstanceOf(InvalidReportDefinitionException.class)
                .hasMessageContaining("either format string or formatter delegate");
    }

    @AfterEach
    void tearDown() {
        reportGroupHolder.clear();
    }
}
