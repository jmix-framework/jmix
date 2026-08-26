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

package io.jmix.reportsflowui.impl.annotated;

import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.download.DownloadFormat;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reports.exception.MissingDefaultTemplateException;
import io.jmix.reportsflowui.runner.FluentUiReportRunner;
import io.jmix.reportsflowui.runner.ParametersDialogShowMode;
import io.jmix.reportsflowui.runner.UiReportRunContext;
import io.jmix.reportsflowui.test_support.TestReportDownloader;
import io.jmix.reportsflowui.test_support.report.DifferentOutputTypesReport;
import io.jmix.reportsflowui.test_support.report.FixedOutputTypeDialogReport;
import io.jmix.reportsflowui.test_support.report.FixedOutputTypeReport;
import io.jmix.reportsflowui.test_support.report.MixedOutputTypeReport;
import io.jmix.reportsflowui.test_support.report.NoDefaultTemplateReport;
import io.jmix.reportsflowui.test_support.report.SingleAlterableTemplateReport;
import io.jmix.reportsflowui.view.run.InputParametersDialog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OutputTypeUiTest extends BaseRunReportUiTest {

    @Autowired
    TestReportDownloader reportDownloader;

    @Autowired
    DialogWindows dialogWindows;

    @BeforeEach
    void resetDownloader() {
        reportDownloader.reset();
    }

    @Test
    void testProgrammaticOutputTypeOverridesTemplateWithoutAlterableOutput() {
        uiReportRunner.byReportCode(FixedOutputTypeReport.CODE)
                .withOutputType(ReportOutputType.PDF)
                .runAndShow();

        assertThat(reportDownloader.getLastFormat()).isEqualTo(DownloadFormat.PDF);
        assertThat(reportDownloader.getLastResourceName()).endsWith(".pdf");
    }

    @Test
    void testProgrammaticOutputTypeOverridesTemplateWithoutAlterableOutputOnBulkPrint() {
        uiReportRunner.byReportCode(FixedOutputTypeReport.CODE)
                .withOutputType(ReportOutputType.PDF)
                .runMultipleReports(FixedOutputTypeReport.PARAM_TITLE, List.of("first", "second"));

        assertThat(reportDownloader.getLastFormat()).isEqualTo(DownloadFormat.ZIP);
        assertThat(getZipEntryNames(reportDownloader.getLastBytes()))
                .isNotEmpty()
                .allSatisfy(name -> assertThat(name).endsWith(".pdf"));
    }

    @Test
    void testOutputTypeSelectionIsResetWhenTemplateWithoutAlterableOutputIsSelected() {
        launchReportFromRunView(MixedOutputTypeReport.CODE);

        InputParametersDialog parametersDialog = (InputParametersDialog) dialogWindows.getOpenedDialogWindows()
                .getCurrentDialog().orElse(null);
        assertThat(parametersDialog).isNotNull();

        EntityComboBox<ReportTemplate> templateComboBox =
                findInputParametersComponent(parametersDialog, "templateComboBox");
        JmixComboBox<ReportOutputType> outputTypeComboBox =
                findInputParametersComponent(parametersDialog, "outputTypeComboBox");

        outputTypeComboBox.setValue(ReportOutputType.PDF);
        templateComboBox.setValue(getTemplate(templateComboBox, MixedOutputTypeReport.FIXED_TEMPLATE));

        assertThat(outputTypeComboBox.getValue()).isEqualTo(ReportOutputType.HTML);

        JmixButton runButton = findComponent(parametersDialog, "printReportButton");
        runButton.click();

        assertThat(reportDownloader.getLastFormat()).isEqualTo(DownloadFormat.HTML);
    }

    @Test
    void testProgrammaticOutputTypeIsAppliedWhenInputParametersDialogIsShown() {
        uiReportRunner.byReportCode(FixedOutputTypeDialogReport.CODE)
                .withOutputType(ReportOutputType.PDF)
                .runAndShow();

        InputParametersDialog parametersDialog = (InputParametersDialog) dialogWindows.getOpenedDialogWindows()
                .getCurrentDialog().orElse(null);
        assertThat(parametersDialog).isNotNull();

        JmixButton runButton = findComponent(parametersDialog, "printReportButton");
        runButton.click();

        assertThat(reportDownloader.getLastFormat()).isEqualTo(DownloadFormat.PDF);
    }

    @Test
    void testProgrammaticOutputTypeIsPreselectedInInputParametersDialog() {
        uiReportRunner.byReportCode(MixedOutputTypeReport.CODE)
                .withOutputType(ReportOutputType.PDF)
                .runAndShow();

        InputParametersDialog parametersDialog = (InputParametersDialog) dialogWindows.getOpenedDialogWindows()
                .getCurrentDialog().orElse(null);
        assertThat(parametersDialog).isNotNull();

        JmixComboBox<ReportOutputType> outputTypeComboBox =
                findInputParametersComponent(parametersDialog, "outputTypeComboBox");
        assertThat(outputTypeComboBox.getValue()).isEqualTo(ReportOutputType.PDF);

        JmixButton runButton = findComponent(parametersDialog, "printReportButton");
        runButton.click();

        assertThat(reportDownloader.getLastFormat()).isEqualTo(DownloadFormat.PDF);
    }

    @Test
    void testProgrammaticOutputTypeOverridesTemplateWithAlterableOutput() {
        uiReportRunner.byReportCode(MixedOutputTypeReport.CODE)
                .withOutputType(ReportOutputType.CSV)
                .withParametersDialogShowMode(ParametersDialogShowMode.NO)
                .runAndShow();

        assertThat(reportDownloader.getLastFormat()).isEqualTo(DownloadFormat.CSV);
        assertThat(reportDownloader.getLastResourceName()).endsWith(".csv");
    }

    @Test
    void testSelectedOutputTypeIsKeptWhenAnotherAlterableTemplateIsSelected() {
        launchReportFromRunView(MixedOutputTypeReport.CODE);

        InputParametersDialog parametersDialog = (InputParametersDialog) dialogWindows.getOpenedDialogWindows()
                .getCurrentDialog().orElse(null);
        assertThat(parametersDialog).isNotNull();

        EntityComboBox<ReportTemplate> templateComboBox =
                findInputParametersComponent(parametersDialog, "templateComboBox");
        JmixComboBox<ReportOutputType> outputTypeComboBox =
                findInputParametersComponent(parametersDialog, "outputTypeComboBox");

        outputTypeComboBox.setValue(ReportOutputType.PDF);
        templateComboBox.setValue(getTemplate(templateComboBox, MixedOutputTypeReport.SECOND_ALTERABLE_TEMPLATE));

        assertThat(outputTypeComboBox.getValue()).isEqualTo(ReportOutputType.PDF);
    }

    @Test
    void testRequestedOutputTypeIsUsedWhenSelectedOneIsNotAvailableForAnotherTemplate() {
        uiReportRunner.byReportCode(DifferentOutputTypesReport.CODE)
                .withOutputType(ReportOutputType.PDF)
                .runAndShow();

        InputParametersDialog parametersDialog = (InputParametersDialog) dialogWindows.getOpenedDialogWindows()
                .getCurrentDialog().orElse(null);
        assertThat(parametersDialog).isNotNull();

        EntityComboBox<ReportTemplate> templateComboBox =
                findInputParametersComponent(parametersDialog, "templateComboBox");
        JmixComboBox<ReportOutputType> outputTypeComboBox =
                findInputParametersComponent(parametersDialog, "outputTypeComboBox");

        // XLS is offered by the default template only
        outputTypeComboBox.setValue(ReportOutputType.XLS);
        templateComboBox.setValue(getTemplate(templateComboBox, DifferentOutputTypesReport.NARROW_TEMPLATE));

        assertThat(outputTypeComboBox.getValue()).isEqualTo(ReportOutputType.PDF);
    }

    @Test
    void testProgrammaticOutputTypeIsAppliedOnBulkPrintWhenInputParametersDialogIsShown() {
        uiReportRunner.byReportCode(FixedOutputTypeDialogReport.CODE)
                .withOutputType(ReportOutputType.PDF)
                .runMultipleReports(FixedOutputTypeDialogReport.PARAM_TITLE, List.of("first", "second"));

        InputParametersDialog parametersDialog = (InputParametersDialog) dialogWindows.getOpenedDialogWindows()
                .getCurrentDialog().orElse(null);
        assertThat(parametersDialog).isNotNull();

        JmixButton runButton = findComponent(parametersDialog, "printReportButton");
        runButton.click();

        assertThat(reportDownloader.getLastFormat()).isEqualTo(DownloadFormat.ZIP);
        assertThat(getZipEntryNames(reportDownloader.getLastBytes()))
                .isNotEmpty()
                .allSatisfy(name -> assertThat(name).endsWith(".pdf"));
    }

    @Test
    void testInputParametersDialogIsNotShownWhenThereIsNothingLeftToChoose() {
        uiReportRunner.byReportCode(SingleAlterableTemplateReport.CODE)
                .withOutputType(ReportOutputType.PDF)
                .runAndShow();

        assertThat(dialogWindows.getOpenedDialogWindows().getCurrentDialog()).isEmpty();
        assertThat(reportDownloader.getLastFormat()).isEqualTo(DownloadFormat.PDF);
    }

    @Test
    void testInputParametersDialogIsShownWhenOutputTypeIsNotSpecified() {
        launchReportFromRunView(SingleAlterableTemplateReport.CODE);

        assertThat(dialogWindows.getOpenedDialogWindows().getCurrentDialog()).isPresent();
        assertThat(reportDownloader.getLastFormat()).isNull();
    }

    @Test
    void testReportWithoutDefaultTemplateIsPrintedWithTheOnlyTemplate() {
        Report report = reportRepository.loadForRunningByCode(NoDefaultTemplateReport.CODE);
        assertThat(report).isNotNull();

        uiReportRunner.runAndShow(new UiReportRunContext(report)
                .setOutputType(ReportOutputType.PDF));

        assertThat(reportDownloader.getLastFormat()).isEqualTo(DownloadFormat.PDF);
    }

    @Test
    void testFluentRunnerRequiresDefaultTemplate() {
        FluentUiReportRunner fluentRunner = uiReportRunner.byReportCode(NoDefaultTemplateReport.CODE)
                .withOutputType(ReportOutputType.PDF);

        assertThatThrownBy(fluentRunner::runAndShow)
                .isInstanceOf(MissingDefaultTemplateException.class);
    }

    protected ReportTemplate getTemplate(EntityComboBox<ReportTemplate> templateComboBox, String templateCode) {
        return templateComboBox.getGenericDataView().getItems()
                .filter(template -> templateCode.equals(template.getCode()))
                .findFirst()
                .orElseThrow();
    }

    protected List<String> getZipEntryNames(byte[] zipContent) {
        List<String> names = new ArrayList<>();
        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(zipContent))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                names.add(entry.getName());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return names;
    }
}
