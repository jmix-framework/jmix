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

package io.jmix.reportsui.screen.report.run;

import com.haulmont.yarg.reporting.ReportOutputDocument;
import io.jmix.core.Messages;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reportsui.screen.ReportGuiManager;
import io.jmix.ui.Fragments;
import io.jmix.ui.UiComponents;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.component.*;
import io.jmix.ui.screen.*;
import io.jmix.ui.theme.ThemeConstants;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@UiController("report_ShowChart.lookup")
@UiDescriptor("show-chart.xml")
public class ShowChartLookup extends StandardLookup {
    public static final String JSON_CHART_SCREEN_ID = "chart$jsonChart";

    public static final String CHART_JSON_PARAMETER = "chartJson";

    @Autowired
    protected GroupBoxLayout reportParamsBox;

    @Autowired
    protected GroupBoxLayout chartBox;

    @Autowired
    protected ReportGuiManager reportGuiManager;

    @Autowired
    protected ThemeConstants themeConstants;

    @Autowired
    protected EntityComboBox<Report> reportEntityComboBox;

    @Autowired
    protected UiComponents uiComponents;

    @Autowired
    protected Button printReportBtn;

    @Autowired
    protected BoxLayout parametersFrameHolder;

    @Autowired
    protected HBoxLayout reportSelectorBox;

    @Autowired
    protected WindowConfig windowConfig;

    @Autowired
    protected Messages messages;

    @Autowired
    protected Fragments fragments;

    @Autowired
    protected ScreenValidation screenValidation;

    protected InputParametersFragment inputParametersFrame;

    protected Report report;

    protected String templateCode;

    protected String chartJson;

    protected Map<String, Object> reportParameters;

    public void setChartJson(String chartJson) {
        this.chartJson = chartJson;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public void setReportParameters(Map<String, Object> reportParameters) {
        this.reportParameters = reportParameters;
    }

    @Subscribe
    protected void onInit(InitEvent event) {
        if (!windowConfig.hasWindow(JSON_CHART_SCREEN_ID)) {
            showChartsNotIncluded();
            return;
        }

        if (report != null) {
            reportSelectorBox.setVisible(false);
            initFrames(chartJson, reportParameters);
        } else {
            showDiagramStubText();
        }

        reportEntityComboBox.addValueChangeListener(e -> {
            report = e.getValue();
            initFrames(null, null);
        });
    }

    protected void initFrames(String chartJson, Map<String, Object> reportParameters) {
        openChart(chartJson);
        openReportParameters(reportParameters);
    }

    private void openReportParameters(Map<String, Object> reportParameters) {
        parametersFrameHolder.removeAll();

        if (report != null) {
            Map<String, Object> params = ParamsMap.of(
                    InputParametersFragment.REPORT_PARAMETER, report,
                    InputParametersFragment.PARAMETERS_PARAMETER, reportParameters
            );

            fragments.create(this,
                    "report_InputParameters.fragment",
                    new MapScreenOptions(params))
                    .init();

            parametersFrameHolder.add(inputParametersFrame.getFragment());
            reportParamsBox.setVisible(true);
        } else {
            reportParamsBox.setVisible(false);
        }
    }

    protected void openChart(String chartJson) {
        chartBox.removeAll();
        if (chartJson != null) {
            Map<String, Object> params = ParamsMap.of(
                    CHART_JSON_PARAMETER,
                    chartJson);

            //TODO chart
//            fragments.create(this, JSON_CHART_SCREEN_ID, new MapScreenOptions(params))
//                    .init();
        }

        showDiagramStubText();
    }

    protected void showDiagramStubText() {
        if (chartBox.getOwnComponents().isEmpty()) {
            chartBox.add(createLabel(messages.getMessage("showChart.caption")));
        }
    }

    protected void showChartsNotIncluded() {
        reportEntityComboBox.setEditable(false);
        chartBox.removeAll();
        chartBox.add(createLabel(messages.getMessage(getClass(), "showChart.noChartComponent")));
    }

    protected Label<String> createLabel(String caption) {
        Label<String> label = uiComponents.create(Label.NAME);
        label.setValue(caption);
        label.setAlignment(Component.Alignment.MIDDLE_CENTER);
        label.setStyleName("h1");
        return label;
    }

    @Subscribe("printReportBtn")
    protected void printReport() {
        if (inputParametersFrame != null && inputParametersFrame.getReport() != null) {
            ValidationErrors validationErrors = screenValidation.validateUiComponents(getWindow());
            if (validationErrors.isEmpty()) {
                Map<String, Object> parameters = inputParametersFrame.collectParameters();
                Report report = inputParametersFrame.getReport();

                if (templateCode == null) {
                    templateCode = report.getTemplates().stream()
                            .filter(template -> template.getReportOutputType() == ReportOutputType.CHART)
                            .findFirst()
                            .map(ReportTemplate::getCode).orElse(null);
                }

                ReportOutputDocument reportResult = reportGuiManager.getReportResult(report, parameters, templateCode);
                openChart(new String(reportResult.getContent(), StandardCharsets.UTF_8));
            } else {
                screenValidation.showValidationErrors(this, validationErrors);
            }
        }
    }
}