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
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import io.jmix.reports.ReportSecurityManager;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reports.runner.ReportRunner;
import io.jmix.ui.Fragments;
import io.jmix.ui.UiComponents;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.component.*;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import io.jmix.ui.theme.ThemeConstants;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UiController("report_ShowChart.screen")
@UiDescriptor("show-chart-screen.xml")
public class ShowChartScreen extends Screen {
    public static final String JSON_CHART_SCREEN_ID = "ui_JsonChartFragment";

    public static final String CHART_JSON_PARAMETER = "chartJson";

    @Autowired
    protected GroupBoxLayout reportParamsBox;

    @Autowired
    protected GroupBoxLayout chartBox;

    @Autowired
    protected ReportSecurityManager reportSecurityManager;

    @Autowired
    protected ThemeConstants themeConstants;

    @Autowired
    protected EntityComboBox<Report> reportEntityComboBox;

    @Autowired
    protected UiComponents uiComponents;

    @Autowired
    protected Button printReportBtn;

    @Autowired
    protected BoxLayout parametersFragmentHolder;

    @Autowired
    protected HBoxLayout reportSelectorBox;

    @Autowired
    protected WindowConfig windowConfig;

    @Autowired
    protected MessageBundle messageBundle;

    @Autowired
    protected Fragments fragments;

    @Autowired
    protected ScreenValidation screenValidation;

    protected InputParametersFragment inputParametersFragment;

    @Autowired
    private HBoxLayout reportTemplateSelectorBox;

    @Autowired
    private EntityComboBox<ReportTemplate> reportTemplateComboBox;

    @Autowired
    private CurrentUserSubstitution currentUserSubstitution;

    @Autowired
    private CollectionContainer<Report> reportsDc;

    @Autowired
    protected ReportRunner reportRunner;

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

    public void setTemplateCode(@Nullable String templateCode) {
        this.templateCode = templateCode;
    }

    public void setReportParameters(Map<String, Object> reportParameters) {
        this.reportParameters = reportParameters;
    }

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        if (!windowConfig.hasWindow(JSON_CHART_SCREEN_ID)) {
            showChartsNotIncluded();
            return;
        }

        if (report != null) {
            reportSelectorBox.setVisible(false);
            initFragments(chartJson, reportParameters);
        } else {
            showDiagramStubText();
        }

        reportEntityComboBox.addValueChangeListener(e -> {
            report = e.getValue();
            initFragments(null, null);
            initReportTemplatesComboBox();
        });
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

    protected void initReportTemplatesComboBox() {
        if (report != null) {
            List<ReportTemplate> chartTemplates = report.getTemplates().stream()
                    .filter(rt -> ReportOutputType.CHART == rt.getReportOutputType())
                    .collect(Collectors.toList());
            if (chartTemplates.size() > 1) {
                reportTemplateSelectorBox.setVisible(true);
                reportTemplateComboBox.setOptionsList(chartTemplates);
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

    protected void initFragments(@Nullable String chartJson, @Nullable Map<String, Object> reportParameters) {
        openChart(chartJson);
        openReportParameters(reportParameters);
    }

    private void openReportParameters(@Nullable Map<String, Object> reportParameters) {
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

    protected void openChart(@Nullable String chartJson) {
        chartBox.removeAll();
        if (chartJson != null) {
            Map<String, Object> params = ParamsMap.of(
                    CHART_JSON_PARAMETER,
                    chartJson);

            ScreenFragment chartFragment = fragments.create(this, JSON_CHART_SCREEN_ID, new MapScreenOptions(params))
                    .init();
            chartBox.add(chartFragment.getFragment());
        }

        showDiagramStubText();
    }

    protected void showDiagramStubText() {
        if (chartBox.getOwnComponents().isEmpty()) {
            chartBox.add(createLabel(messageBundle.getMessage("showChart.caption")));
        }
    }

    protected void showChartsNotIncluded() {
        reportEntityComboBox.setEditable(false);
        chartBox.removeAll();
        chartBox.add(createLabel(messageBundle.getMessage("showChart.noChartComponent")));
    }

    protected Label<String> createLabel(String caption) {
        Label<String> label = uiComponents.create(Label.NAME);
        label.setValue(caption);
        label.setAlignment(Component.Alignment.MIDDLE_CENTER);
        label.setStyleName("h1");
        return label;
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
                                .filter(template -> template.getReportOutputType() == ReportOutputType.CHART)
                                .findFirst()
                                .map(ReportTemplate::getCode).orElse(null);
                    }
                }

                ReportOutputDocument reportResult = reportRunner.byReportEntity(report)
                        .withParams(parameters)
                        .withTemplateCode(resultTemplateCode)
                        .run();
                openChart(new String(reportResult.getContent(), StandardCharsets.UTF_8));
            } else {
                screenValidation.showValidationErrors(this, validationErrors);
            }
        }
    }
}