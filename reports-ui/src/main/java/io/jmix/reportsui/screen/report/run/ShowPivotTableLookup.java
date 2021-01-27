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
import io.jmix.core.LoadContext;
import io.jmix.core.Messages;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.impl.StandardSerialization;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.reports.entity.PivotTableData;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reportsui.screen.ReportGuiManager;
import io.jmix.ui.Fragments;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.*;
import io.jmix.ui.screen.*;
import io.jmix.ui.theme.ThemeConstants;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;


@UiController("report_ShowPivotTable.lookup")
@UiDescriptor("show-pivot-table.xml")
public class ShowPivotTableLookup extends StandardLookup {
    public static final String PIVOT_TABLE_SCREEN_ID = "chart$pivotTable";

    @Autowired
    protected ReportGuiManager reportGuiManager;

    @Autowired
    protected UiComponents uiComponents;

    @Autowired
    protected ThemeConstants themeConstants;

    @Autowired
    protected GroupBoxLayout reportBox;

    @Autowired
    protected GroupBoxLayout reportParamsBox;

    @Autowired
    protected BoxLayout parametersFrameHolder;

    @Autowired
    protected EntityComboBox<Report> reportEntityComboBox;

    @Autowired
    protected HBoxLayout reportSelectorBox;

    @Autowired
    protected StandardSerialization serialization;

    @Autowired
    protected Messages messages;

    @Autowired
    protected Fragments fragments;

    @Autowired
    protected ScreenValidation screenValidation;

    @Autowired
    protected CurrentAuthentication currentAuthentication;

    protected Report report;

    protected Map<String, Object> params;

    protected String templateCode;

    protected byte[] pivotTableData;

    protected InputParametersFragment inputParametersFrame;

    public void setReport(Report report) {
        this.report = report;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public void setPivotTableData(byte[] pivotTableData) {
        this.pivotTableData = pivotTableData;
    }

    @Install(to = "reportsDl", target = Target.DATA_LOADER)
    protected List<Report> reportsDlLoadDelegate(LoadContext<Report> loadContext) {
        return reportGuiManager.getAvailableReports(null, currentAuthentication.getUser(), null);
    }


    @Subscribe
    protected void onInit(InitEvent event) {
        if (report != null) {
            reportSelectorBox.setVisible(false);
            if (pivotTableData != null) {
                PivotTableData result = (PivotTableData) serialization.deserialize(pivotTableData);
                initFrames(result.getPivotTableJson(), result.getValues(), params);
            }
        } else {
            showStubText();
        }

        reportEntityComboBox.addValueChangeListener(e -> {
            report = e.getValue();
            initFrames(null, null, null);
        });

    }

    protected void initFrames(String pivotTableJson, List<KeyValueEntity> values, Map<String, Object> reportParameters) {
        openPivotTable(pivotTableJson, values);
        openReportParameters(reportParameters);
    }

    protected void openReportParameters(Map<String, Object> reportParameters) {
        parametersFrameHolder.removeAll();
        if (report != null) {
            Map<String, Object> params = ParamsMap.of(
                    InputParametersFragment.REPORT_PARAMETER, report,
                    InputParametersFragment.PARAMETERS_PARAMETER, reportParameters
            );

            inputParametersFrame = (InputParametersFragment) fragments.create(this,
                    "report_InputParameters.fragment",
                    new MapScreenOptions(params))
                    .init();
            parametersFrameHolder.add(inputParametersFrame.getFragment());

            reportParamsBox.setVisible(true);
        } else {
            reportParamsBox.setVisible(false);
        }
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
                            .filter(template -> template.getReportOutputType() == ReportOutputType.PIVOT_TABLE)
                            .findFirst()
                            .map(ReportTemplate::getCode).orElse(null);
                }

                ReportOutputDocument document = reportGuiManager.getReportResult(report, parameters, templateCode);
                PivotTableData result = (PivotTableData) serialization.deserialize(document.getContent());
                openPivotTable(result.getPivotTableJson(), result.getValues());
            } else {
                screenValidation.showValidationErrors(this, validationErrors);
            }
        }
    }

    protected void openPivotTable(String pivotTableJson, List<KeyValueEntity> values) {
        reportBox.removeAll();
        if (pivotTableJson != null) {
            Map<String, Object> screenParams = ParamsMap.of(
                    "pivotTableJson", pivotTableJson,
                    "values", values);

            Fragment fragment = fragments.create(this, PIVOT_TABLE_SCREEN_ID, new MapScreenOptions(screenParams))
                    .init()
                    .getFragment();

            reportBox.add(fragment);
        }
        showStubText();
    }

    protected void showStubText() {
        if (reportBox.getOwnComponents().isEmpty()) {
            Label<String> label = uiComponents.create(Label.class);
            label.setValue(messages.getMessage(getClass(), "showPivotTable.caption"));
            label.setAlignment(Component.Alignment.MIDDLE_CENTER);
            label.setStyleName("h1");
            reportBox.add(label);
        }
    }
}
