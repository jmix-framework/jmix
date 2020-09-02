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

import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.gui.components.GroupBoxLayout;
import com.haulmont.cuba.gui.components.Label;
import com.haulmont.cuba.gui.components.LookupField;
import com.haulmont.yarg.reporting.ReportOutputDocument;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.impl.StandardSerialization;
import io.jmix.reports.entity.PivotTableData;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reports.gui.ReportGuiManager;
import io.jmix.ui.WindowParam;
import io.jmix.ui.component.BoxLayout;
import io.jmix.ui.component.HBoxLayout;
import io.jmix.ui.theme.ThemeConstants;
import io.jmix.ui.xml.layout.ComponentsFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

public class ShowPivotTableController extends AbstractWindow {
    public static final String PIVOT_TABLE_SCREEN_ID = "chart$pivotTable";

    public static final String REPORT_PARAMETER = "report";
    public static final String PIVOT_TABLE_DATA_PARAMETER = "pivotTableData";
    public static final String TEMPLATE_CODE_PARAMETER = "templateCode";
    public static final String PARAMS_PARAMETER = "reportParams";

    @Autowired
    protected ReportGuiManager reportGuiManager;

    @Autowired
    protected ComponentsFactory componentsFactory;

    @Autowired
    protected ThemeConstants themeConstants;

    @Autowired
    protected GroupBoxLayout reportBox;

    @Autowired
    protected GroupBoxLayout reportParamsBox;

    @Autowired
    protected BoxLayout parametersFrameHolder;

    @Autowired
    protected LookupField<Report> reportLookup;

    @Autowired
    protected HBoxLayout reportSelectorBox;

    @Autowired
    protected StandardSerialization serialization;

    @WindowParam(name = REPORT_PARAMETER)
    protected Report report;

    @WindowParam(name = PARAMS_PARAMETER)
    protected Map<String, Object> params;

    @WindowParam(name = TEMPLATE_CODE_PARAMETER)
    protected String templateCode;

    @WindowParam(name = PIVOT_TABLE_DATA_PARAMETER)
    protected byte[] pivotTableData;

    protected InputParametersFrame inputParametersFrame;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        //TODO dialog options
//        getDialogOptions()
//                .setWidth(themeConstants.get("cuba.gui.report.ShowPivotTable.width"))
//                .setHeight(themeConstants.get("cuba.gui.report.ShowPivotTable.height"))
//                .setResizable(true)
//                .center();

        if (report != null) {
            reportSelectorBox.setVisible(false);
            if (pivotTableData != null) {
                PivotTableData result = (PivotTableData) serialization.deserialize(pivotTableData);
                initFrames(result.getPivotTableJson(), result.getValues(), params);
            }
        } else {
            showStubText();
        }

        reportLookup.addValueChangeListener(e -> {
            report = (Report) e.getValue();
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
            Map<String, Object> params = ParamsMap.of(InputParametersFrame.REPORT_PARAMETER, report, InputParametersFrame.PARAMETERS_PARAMETER, reportParameters);
            inputParametersFrame = (InputParametersFrame) openFrame(parametersFrameHolder, "report$inputParametersFrame", params);
            reportParamsBox.setVisible(true);
        } else {
            reportParamsBox.setVisible(false);
        }
    }

    public void printReport() {
        if (inputParametersFrame != null && inputParametersFrame.getReport() != null) {
            if (validateAll()) {
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
            }
        }
    }

    protected void openPivotTable(String pivotTableJson, List<KeyValueEntity> values) {
        reportBox.removeAll();
        if (pivotTableJson != null) {
            Map<String, Object> screenParams = ParamsMap.of(
                    "pivotTableJson", pivotTableJson,
                    "values", values);
            openFrame(reportBox, PIVOT_TABLE_SCREEN_ID, screenParams);
        }
        showStubText();
    }

    protected void showStubText() {
        if (reportBox.getOwnComponents().isEmpty()) {
            Label label = componentsFactory.createComponent(Label.class);
            label.setValue(getMessage("showPivotTable.caption"));
            label.setAlignment(Alignment.MIDDLE_CENTER);
            label.setStyleName("h1");
            reportBox.add(label);
        }
    }
}
