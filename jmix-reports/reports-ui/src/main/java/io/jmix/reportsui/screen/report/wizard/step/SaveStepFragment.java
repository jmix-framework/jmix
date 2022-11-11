/*
 * Copyright 2021 Haulmont.
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

package io.jmix.reportsui.screen.report.wizard.step;

import io.jmix.core.CoreProperties;
import io.jmix.core.Messages;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.charts.*;
import io.jmix.reports.entity.wizard.ReportData;
import io.jmix.reports.entity.wizard.TemplateFileType;
import io.jmix.reports.exception.TemplateGenerationException;
import io.jmix.reportsui.screen.report.run.ShowChartScreen;
import io.jmix.reportsui.screen.report.wizard.OutputFormatTools;
import io.jmix.reportsui.screen.report.wizard.ReportWizardCreator;
import io.jmix.reportsui.screen.report.wizard.ReportsWizard;
import io.jmix.reportsui.screen.template.edit.generator.RandomChartDataGenerator;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Fragments;
import io.jmix.ui.Notifications;
import io.jmix.ui.UiProperties;
import io.jmix.ui.component.*;
import io.jmix.ui.download.ByteArrayDataProvider;
import io.jmix.ui.download.DownloadFormat;
import io.jmix.ui.download.Downloader;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@UiController("report_SaveStep.fragment")
@UiDescriptor("save-step-fragment.xml")
public class SaveStepFragment extends StepFragment {

    @Autowired
    private InstanceContainer<ReportData> reportDataDc;

    @Autowired
    protected UiProperties uiProperties;

    @Autowired
    protected CoreProperties coreProperties;

    @Autowired
    protected Notifications notifications;

    @Autowired
    protected Dialogs dialogs;

    @Autowired
    protected Fragments fragments;

    @Autowired
    protected Downloader downloader;

    @Autowired
    protected ReportsWizard reportWizardService;

    @Autowired
    protected ComboBox<ReportOutputType> outputFileFormat;

    @Autowired
    protected TextField<String> outputFileName;

    @Autowired
    protected Button downloadTemplateFile;

    @Autowired
    protected OutputFormatTools outputFormatTools;

    @Autowired
    protected VBoxLayout chartPreviewBox;

    @Autowired
    protected ComboBox<ChartType> diagramType;

    @Autowired
    protected RandomChartDataGenerator randomChartDataGenerator;

    @Subscribe("outputFileName")
    public void onOutputFileNameValueChange(HasValue.ValueChangeEvent<String> event) {
        reportDataDc.getItem().setOutputNamePattern(event.getValue());
    }

    @Install(to = "outputFileName", subject = "contextHelpIconClickHandler")
    protected void outputFileNameContextHelpIconClickHandler(HasContextHelp.ContextHelpIconClickEvent contextHelpIconClickEvent) {
        dialogs.createMessageDialog()
                .withCaption(messages.getMessage("template.namePatternText"))
                .withMessage(messages.getMessage("template.namePatternTextHelp"))
                .withContentMode(ContentMode.HTML)
                .withModal(false)
                .withWidth("560px")
                .show();
    }

    protected void updateCorrectReportOutputType() {
        ReportOutputType outputFileFormatPrevValue = outputFileFormat.getValue();
        outputFileFormat.setValue(null);
        Map<String, ReportOutputType> optionsMap = outputFormatTools.getOutputAvailableFormats(reportDataDc.getItem().getTemplateFileType());
        outputFileFormat.setOptionsMap(optionsMap);

        if (outputFileFormatPrevValue != null) {
            if (optionsMap.containsKey(outputFileFormatPrevValue.toString())) {
                outputFileFormat.setValue(outputFileFormatPrevValue);
            }
        }
        if (outputFileFormat.getValue() == null) {
            if (optionsMap.size() > 1) {
                outputFileFormat.setValue(optionsMap.get(reportDataDc.getItem().getTemplateFileType().toString()));
            } else if (optionsMap.size() == 1) {
                outputFileFormat.setValue(optionsMap.values().iterator().next());
            }
        }
    }

    @Subscribe(id = "reportDataDc", target = Target.DATA_CONTAINER)
    public void onReportDataDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<ReportData> event) {
        if (event.getProperty().equals("entityName") || event.getProperty().equals("templateFileType")) {
            updateCorrectReportOutputType();
            updateDownloadTemplateFile();
        }
    }

    @Override
    public String getCaption() {
        return messageBundle.getMessage("saveReport");
    }

    @Override
    public String getDescription() {
        return messageBundle.getMessage("finishPrepareReport");
    }

    protected String generateOutputFileName(String fileExtension) {
        ReportData reportData = reportDataDc.getItem();
        if (StringUtils.isBlank(reportData.getName())) {
            MetaClass entityMetaClass = metadata.findClass(reportData.getEntityName());
            return entityMetaClass != null ?
                    messages.formatMessage(SaveStepFragment.class,
                            "downloadOutputFileNamePattern", messageTools.getEntityCaption(entityMetaClass), fileExtension) :
                    "";
        } else {
            return reportData.getName() + "." + fileExtension;
        }
    }

    @Override
    public void beforeShow() {
        if (StringUtils.isEmpty(outputFileName.getValue())) {
            ReportData reportData = reportDataDc.getItem();
            outputFileName.setValue(generateOutputFileName(reportData.getTemplateFileType().toString().toLowerCase()));
        }

        initChartPreview();
    }

    protected void initChartPreview() {
        boolean isChartTemplate = outputFileFormat.getValue() == ReportOutputType.CHART;
        chartPreviewBox.setVisible(isChartTemplate);
        diagramType.setVisible(isChartTemplate);
        if (isChartTemplate) {
            showChart();

            diagramType.setRequired(true);
            diagramType.setValue(ChartType.SERIAL);

            diagramType.addValueChangeListener(e -> {
                reportDataDc.getItem().setChartType(e.getValue());
                showChart();
            });
        }
    }

    public void updateDownloadTemplateFile() {
        String templateFileName = generateTemplateFileName(reportDataDc.getItem().getTemplateFileType().toString().toLowerCase());

        downloadTemplateFile.setCaption(templateFileName);
        reportDataDc.getItem().setTemplateFileName(templateFileName);
    }

    public String generateTemplateFileName(String fileExtension) {
        ReportData reportData = reportDataDc.getItem();
        MetaClass entityMetaClass = metadata.findClass(reportData.getEntityName());
        return entityMetaClass != null ?
                messageBundle.formatMessage("downloadTemplateFileNamePattern", reportData.getName(), fileExtension) :
                "";
    }

    @Subscribe("downloadTemplateFile")
    public void onDownloadTemplateFileClick(Button.ClickEvent event) {
        ReportData reportData = reportDataDc.getItem();
        try {
            TemplateFileType templateFileType = reportData.getTemplateFileType();
            byte[] newTemplate = reportWizardService.generateTemplate(reportData, templateFileType);
            downloader.download(new ByteArrayDataProvider(
                            newTemplate,
                            uiProperties.getSaveExportedByteArrayDataThresholdBytes(),
                            coreProperties.getTempDir()),
                    downloadTemplateFile.getCaption(),
                    DownloadFormat.getByExtension(templateFileType.toString().toLowerCase()));
        } catch (TemplateGenerationException e) {
            notifications.create(Notifications.NotificationType.WARNING)
                    .withCaption(messageBundle.getMessage("templateGenerationException"))
                    .show();
        }
    }


    protected void showChart() {
        chartPreviewBox.removeAll();

        ReportWizardCreator reportWizardCreator = (ReportWizardCreator) getFragment().getFrameOwner().getHostController();
        byte[] content = reportWizardCreator.buildReport(true).getDefaultTemplate().getContent();
        String chartDescriptionJson = new String(content, StandardCharsets.UTF_8);
        AbstractChartDescription chartDescription = AbstractChartDescription.fromJsonString(chartDescriptionJson);
        List<Map<String, Object>> randomChartData = randomChartDataGenerator.generateRandomChartData(chartDescription);
        ChartToJsonConverter chartToJsonConverter = new ChartToJsonConverter();
        String chartJson = null;
        if (chartDescription instanceof PieChartDescription) {
            chartJson = chartToJsonConverter.convertPieChart((PieChartDescription) chartDescription, randomChartData);
        } else if (chartDescription instanceof SerialChartDescription) {
            chartJson = chartToJsonConverter.convertSerialChart((SerialChartDescription) chartDescription, randomChartData);
        }
        chartJson = chartJson == null ? "{}" : chartJson;

        Fragment chartFragment = fragments.create(this, ShowChartScreen.JSON_CHART_SCREEN_ID,
                new MapScreenOptions(ParamsMap.of(ShowChartScreen.CHART_JSON_PARAMETER, chartJson)))
                .init()
                .getFragment();
        chartFragment.setHeight("400px");
        chartPreviewBox.add(chartFragment);
    }

}
