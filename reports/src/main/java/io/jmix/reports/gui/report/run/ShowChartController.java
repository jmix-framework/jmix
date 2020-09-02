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

package io.jmix.reports.gui.report.run;

import io.jmix.core.common.util.ParamsMap;
import com.haulmont.cuba.gui.components.*;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reports.gui.ReportGuiManager;
import com.haulmont.yarg.reporting.ReportOutputDocument;

import io.jmix.ui.WindowConfig;
import io.jmix.ui.component.BoxLayout;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.HBoxLayout;
import io.jmix.ui.theme.ThemeConstants;
import io.jmix.ui.xml.layout.ComponentsFactory;
import org.springframework.beans.factory.annotation.Autowired;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ShowChartController extends AbstractWindow {
    public static final String JSON_CHART_SCREEN_ID = "chart$jsonChart";

    public static final String CHART_JSON_PARAMETER = "chartJson";
    public static final String REPORT_PARAMETER = "report";
    public static final String TEMPLATE_CODE_PARAMETER = "templateCode";
    public static final String PARAMS_PARAMETER = "reportParams";

    @Autowired
    protected GroupBoxLayout reportParamsBox;

    @Autowired
    protected GroupBoxLayout chartBox;

    @Autowired
    protected ReportGuiManager reportGuiManager;

    @Autowired
    protected ThemeConstants themeConstants;

    @Autowired
    protected LookupField<Report> reportLookup;

    @Autowired
    protected ComponentsFactory componentsFactory;

    @Autowired
    protected Button printReportBtn;

    @Autowired
    protected BoxLayout parametersFrameHolder;

    @Autowired
    protected HBoxLayout reportSelectorBox;

    @Autowired
    protected WindowConfig windowConfig;

    protected InputParametersFrame inputParametersFrame;

    protected Report report;

    protected String templateCode;

    @Override
    public void init(final Map<String, Object> params) {
        super.init(params);

        //TODO get dialog options
//        getDialogOptions()
//                .setWidth(themeConstants.get("cuba.gui.report.ShowChartController.width"))
//                .setHeight(themeConstants.get("cuba.gui.report.ShowChartController.height"))
//                .setResizable(true);

        String chartJson = (String) params.get(CHART_JSON_PARAMETER);
        report = (Report) params.get(REPORT_PARAMETER);
        templateCode = (String) params.get(TEMPLATE_CODE_PARAMETER);
        @SuppressWarnings("unchecked")
        Map<String, Object> reportParameters = (Map<String, Object>) params.get(PARAMS_PARAMETER);

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

        reportLookup.addValueChangeListener(e -> {
            report = (Report) e.getValue();
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
                    InputParametersFrame.REPORT_PARAMETER, report,
                    InputParametersFrame.PARAMETERS_PARAMETER, reportParameters
            );

            inputParametersFrame = (InputParametersFrame) openFrame(parametersFrameHolder,
                    "report$inputParametersFrame", params);

            reportParamsBox.setVisible(true);
        } else {
            reportParamsBox.setVisible(false);
        }
    }

    protected void openChart(String chartJson) {
        chartBox.removeAll();
        if (chartJson != null) {
            openFrame(chartBox, JSON_CHART_SCREEN_ID, ParamsMap.of(CHART_JSON_PARAMETER, chartJson));
        }

        showDiagramStubText();
    }

    protected void showDiagramStubText() {
        if (chartBox.getOwnComponents().isEmpty()) {
            Label label = componentsFactory.createComponent(Label.class);
            label.setValue(getMessage("showChart.caption"));
            label.setAlignment(Alignment.MIDDLE_CENTER);
            label.setStyleName("h1");
            chartBox.add(label);
        }
    }

    protected void showChartsNotIncluded() {
        reportLookup.setEditable(false);
        chartBox.removeAll();
        Label label = componentsFactory.createComponent(Label.class);
        label.setValue(getMessage("showChart.noChartComponent"));
        label.setAlignment(Alignment.MIDDLE_CENTER);
        label.setStyleName("h1");
        chartBox.add(label);
    }

    public void printReport() {
        if (inputParametersFrame != null && inputParametersFrame.getReport() != null) {
            if (validateAll()) {
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
            }
        }
    }
}