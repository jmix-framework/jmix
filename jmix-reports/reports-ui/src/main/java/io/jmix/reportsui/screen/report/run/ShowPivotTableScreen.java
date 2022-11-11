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

package io.jmix.reportsui.screen.report.run;

import com.haulmont.yarg.reporting.ReportOutputDocument;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.impl.StandardSerialization;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import io.jmix.reports.ReportSecurityManager;
import io.jmix.reports.entity.PivotTableData;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reports.runner.ReportRunner;
import io.jmix.ui.Fragments;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.*;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import io.jmix.ui.theme.ThemeConstants;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@UiController("report_ShowPivotTable.screen")
@UiDescriptor("show-pivot-table-screen.xml")
public class ShowPivotTableScreen extends Screen {
    public static final String PIVOT_TABLE_SCREEN_ID = "ui_PivotTableFragment";

    @Autowired
    protected ReportSecurityManager reportSecurityManager;

    @Autowired
    protected UiComponents uiComponents;

    @Autowired
    protected ThemeConstants themeConstants;

    @Autowired
    protected GroupBoxLayout reportBox;

    @Autowired
    protected GroupBoxLayout reportParamsBox;

    @Autowired
    protected BoxLayout parametersFragmentHolder;

    @Autowired
    protected EntityComboBox<Report> reportEntityComboBox;

    @Autowired
    protected HBoxLayout reportSelectorBox;

    @Autowired
    protected StandardSerialization serialization;

    @Autowired
    protected MessageBundle messageBundle;

    @Autowired
    protected Fragments fragments;

    @Autowired
    protected ScreenValidation screenValidation;

    @Autowired
    protected CurrentUserSubstitution currentUserSubstitution;

    @Autowired
    private CollectionContainer<Report> reportsDc;

    @Autowired
    private HBoxLayout reportTemplateSelectorBox;

    @Autowired
    private EntityComboBox<ReportTemplate> reportTemplateComboBox;

    protected Report report;

    protected Map<String, Object> params;

    protected String templateCode;

    protected byte[] pivotTableData;

    protected InputParametersFragment inputParametersFragment;

    @Autowired
    protected ReportRunner reportRunner;

    public void setReport(Report report) {
        this.report = report;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public void setTemplateCode(@Nullable String templateCode) {
        this.templateCode = templateCode;
    }

    public void setPivotTableData(byte[] pivotTableData) {
        this.pivotTableData = pivotTableData;
    }

    @Subscribe(id = "reportsDl", target = Target.DATA_LOADER)
    public void onReportsDlPostLoad(CollectionLoader.PostLoadEvent<Report> event) {
        List<Report> entities = event.getLoadedEntities();
        List<Report> availableReports = reportSecurityManager.getAvailableReports(
                null,
                currentUserSubstitution.getEffectiveUser(),
                null);
        entities.retainAll(availableReports);
        reportsDc.setItems(entities);
    }

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        if (report != null) {
            reportSelectorBox.setVisible(false);
            if (pivotTableData != null) {
                PivotTableData result = (PivotTableData) serialization.deserialize(pivotTableData);
                initFragments(result.getPivotTableJson(), result.getValues(), params);
            }
        } else {
            showStubText();
        }

        reportEntityComboBox.addValueChangeListener(e -> {
            report = e.getValue();
            initFragments(null, null, null);
            initReportTemplatesComboBox();
        });

    }

    protected void initFragments(@Nullable String pivotTableJson, @Nullable List<KeyValueEntity> values, @Nullable Map<String, Object> reportParameters) {
        openPivotTable(pivotTableJson, values);
        openReportParameters(reportParameters);
    }

    protected void openReportParameters(@Nullable Map<String, Object> reportParameters) {
        parametersFragmentHolder.removeAll();
        if (report != null) {
            Map<String, Object> params = ParamsMap.of(
                    InputParametersFragment.REPORT_PARAMETER, report,
                    InputParametersFragment.PARAMETERS_PARAMETER, reportParameters
            );

            inputParametersFragment = (InputParametersFragment) fragments.create(this,
                    "report_InputParameters.fragment",
                    new MapScreenOptions(params))
                    .init();
            parametersFragmentHolder.add(inputParametersFragment.getFragment());

            reportParamsBox.setVisible(true);
        } else {
            reportParamsBox.setVisible(false);
        }
    }

    @Subscribe("printReportBtn")
    protected void printReport(Button.ClickEvent event) {
        if (inputParametersFragment != null && inputParametersFragment.getReport() != null) {
            ValidationErrors validationErrors = screenValidation.validateUiComponents(getWindow());
            if (validationErrors.isEmpty()) {
                Map<String, Object> parameters = inputParametersFragment.collectParameters();
                Report report = inputParametersFragment.getReport();

                String resultTemplateCode = templateCode;
                if (templateCode == null) {
                    if (reportTemplateComboBox.getValue() != null) {
                        resultTemplateCode = reportTemplateComboBox.getValue().getCode();
                    } else {
                        resultTemplateCode = report.getTemplates().stream()
                                .filter(template -> template.getReportOutputType() == ReportOutputType.PIVOT_TABLE)
                                .findFirst()
                                .map(ReportTemplate::getCode).orElse(null);
                    }
                }

                ReportOutputDocument document = reportRunner.byReportEntity(report)
                        .withParams(parameters)
                        .withTemplateCode(resultTemplateCode)
                        .run();
                PivotTableData result = (PivotTableData) serialization.deserialize(document.getContent());
                openPivotTable(result.getPivotTableJson(), result.getValues());
            } else {
                screenValidation.showValidationErrors(this, validationErrors);
            }
        }
    }

    protected void openPivotTable(@Nullable String pivotTableJson, @Nullable List<KeyValueEntity> values) {
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

    protected void initReportTemplatesComboBox() {
        if (report != null) {
            List<ReportTemplate> pivotTableTemplates = report.getTemplates().stream()
                    .filter(rt -> ReportOutputType.PIVOT_TABLE == rt.getReportOutputType())
                    .collect(Collectors.toList());
            if (pivotTableTemplates.size() > 1) {
                reportTemplateSelectorBox.setVisible(true);
                reportTemplateComboBox.setOptionsList(pivotTableTemplates);
            } else {
                resetReportTemplate();
            }
        } else {
            resetReportTemplate();
        }
    }

    private void resetReportTemplate() {
        reportTemplateSelectorBox.setVisible(false);
        reportTemplateComboBox.setOptionsList(new ArrayList<>());
        reportTemplateComboBox.setValue(null);
    }

    protected void showStubText() {
        if (reportBox.getOwnComponents().isEmpty()) {
            Label<String> label = uiComponents.create(Label.TYPE_STRING);
            label.setValue(messageBundle.getMessage("showPivotTable.caption"));
            label.setAlignment(Component.Alignment.MIDDLE_CENTER);
            label.setStyleName("h1");
            reportBox.add(label);
        }
    }
}
