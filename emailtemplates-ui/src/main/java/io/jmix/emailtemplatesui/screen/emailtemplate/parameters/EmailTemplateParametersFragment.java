/*
 * Copyright 2020 Haulmont.
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

package io.jmix.emailtemplatesui.screen.emailtemplate.parameters;

import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.emailtemplates.TemplateParametersExtractor;
import io.jmix.emailtemplates.dto.ReportWithParams;
import io.jmix.emailtemplates.entity.ParameterValue;
import io.jmix.emailtemplates.entity.TemplateReport;
import io.jmix.emailtemplates.exception.ReportParameterTypeChangedException;
import io.jmix.emailtemplatesui.dto.ReportWithParamField;
import io.jmix.reports.ParameterClassResolver;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportInputParameter;
import io.jmix.reportsui.screen.report.run.ParameterFieldCreator;
import io.jmix.reportsui.screen.report.validators.ReportParamFieldValidator;
import io.jmix.ui.Actions;
import io.jmix.ui.Notifications;
import io.jmix.ui.UiComponents;
import io.jmix.ui.WindowParam;
import io.jmix.ui.component.*;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

@UiController("emltmp_EmailTemplateParametersFragment")
@UiDescriptor("email-template-parameters-fragment.xml")
public class EmailTemplateParametersFragment extends ScreenFragment {
    public static final String IS_DEFAULT_PARAM_VALUES = "isDefault";
    public static final String HIDE_REPORT_CAPTION = "hideReportCaption";
    public static final String TEMPLATE_REPORTS = "templateReports";
    public static final String TEMPLATE_REPORT = "templateReport";

    @WindowParam(name = IS_DEFAULT_PARAM_VALUES)
    protected Boolean isDefaultValues = false;

    @WindowParam(name = HIDE_REPORT_CAPTION)
    protected Boolean hideReportCaption = false;

    @Autowired
    protected GridLayout parametersGrid;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    private UiComponents componentsFactory;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected Actions actions;

    @Autowired
    private TemplateParametersExtractor templateParametersExtractor;

    @Autowired
    protected ParameterClassResolver parameterClassResolver;

    @WindowParam
    private List<TemplateReport> templateReports = new ArrayList<>();

    @WindowParam
    private TemplateReport templateReport;

    protected List<ReportWithParamField> parameterComponents = new ArrayList<>();

    @Autowired
    protected ParameterFieldCreator parameterFieldCreator;

    @Subscribe
    public void onAttachEvent(AttachEvent event) {
        if (templateReport != null) {
            setTemplateReport(templateReport);
        }
    }

    public EmailTemplateParametersFragment setHideReportCaption(Boolean hideReportCaption) {
        this.hideReportCaption = hideReportCaption;
        return this;
    }

    public EmailTemplateParametersFragment setTemplateReports(List<TemplateReport> templateReports) {
        this.templateReports = templateReports;
        return this;
    }

    public EmailTemplateParametersFragment setIsDefaultValues(Boolean defaultValues) {
        isDefaultValues = defaultValues;
        return this;
    }

    private List<ReportWithParams> getTemplateDefaultValues() throws ReportParameterTypeChangedException {
        List<ReportWithParams> reportWithParams = new ArrayList<>();
        for (TemplateReport templateReport : templateReports) {
            reportWithParams.add(templateParametersExtractor.getReportDefaultValues(templateReport.getReport(),
                    templateReport.getParameterValues()));
        }
        return reportWithParams;
    }

    public EmailTemplateParametersFragment setTemplateReport(TemplateReport templateReport) {
        if (templateReport != null) {
            this.templateReports = Collections.singletonList(templateReport);
        }
        return this;
    }

    public EmailTemplateParametersFragment createComponents() {
        clearComponents();

        try {
            List<ReportWithParams> parameters = getTemplateDefaultValues();

            List<Report> reports = parameters.stream()
                    .map(ReportWithParams::getReport)
                    .collect(Collectors.toList());

            parametersGrid.setRows(getRowCountForParameters(reports));

            int currentGridRow = 0;
            for (ReportWithParams reportData : parameters) {
                Report report = reportData.getReport();
                if (report != null && !report.getIsTmp()) {
                    report = dataManager.load(Report.class)
                            .id(report.getId())
                            .fetchPlan("report.edit")
                            .one();
                }

                if (report != null) {
                    if (CollectionUtils.isNotEmpty(report.getInputParameters())) {
                        if (BooleanUtils.isNotTrue(hideReportCaption)) {
                            createReportNameLabel(report, currentGridRow);
                            currentGridRow++;
                        }
                        Map<String, Field> componentsMap = new HashMap<>();
                        for (ReportInputParameter parameter : report.getInputParameters()) {
                            if (BooleanUtils.isNotTrue(parameter.getHidden())) {
                                componentsMap.put(parameter.getAlias(), createComponent(parameter, reportData.getParams(), currentGridRow));
                                currentGridRow++;
                            }
                        }
                        parameterComponents.add(new ReportWithParamField(report, componentsMap));
                    }
                }
            }
        } catch (ReportParameterTypeChangedException e) {
            notifications.create(Notifications.NotificationType.ERROR)
                    .withCaption(e.getMessage())
                    .show();
        }
        return this;
    }

    public void clearComponents() {
        parameterComponents.clear();
        parametersGrid.removeAll();
    }

    protected int getRowCountForParameters(List<Report> reports) {
        int rowsCount = 0;
        for (Report report : reports) {
            if (report != null && !report.getIsTmp()) {
                report = dataManager.load(Report.class)
                        .id(report.getId())
                        .fetchPlan("report.edit")
                        .one();
            }
            if (report != null) {
                if (CollectionUtils.isNotEmpty(report.getInputParameters())) {
                    rowsCount++;
                    for (ReportInputParameter parameter : report.getInputParameters()) {
                        if (BooleanUtils.isNotTrue(parameter.getHidden())) {
                            rowsCount++;
                        }
                    }
                }
            }
        }
        return rowsCount == 0 ? 1 : rowsCount;
    }

    public List<ReportWithParams> collectParameters() {
        List<ReportWithParams> reportDataList = new ArrayList<>();
        for (ReportWithParamField fieldValue : parameterComponents) {
            ReportWithParams reportData = new ReportWithParams(fieldValue.getReport());
            for (String paramName : fieldValue.getFields().keySet()) {
                Field parameterField = fieldValue.getFields().get(paramName);
                Object value = parameterField.getValue();
                reportData.put(paramName, value);
            }
            reportDataList.add(reportData);
        }
        return reportDataList;
    }

    protected void createReportNameLabel(Report report, int currentGridRow) {
        Label label = componentsFactory.create(Label.class);
        label.setWidth(Component.AUTO_SIZE);
        label.setValue(report.getName());
        parametersGrid.add(label, 0, currentGridRow, 1, currentGridRow);
    }

    protected Field createComponent(ReportInputParameter parameter, Map<String, Object> values, int currentGridRow) {
        Field<Object> field = parameterFieldCreator.createField(parameter);
        field.setFrame(getFragment());
        if (BooleanUtils.isTrue(isDefaultValues)) {
            field.setRequired(false);
        }

        Object value = null;
        if (MapUtils.isNotEmpty(values)) {
            value = values.get(parameter.getAlias());
        }

        if (value == null && parameter.getDefaultValue() != null) {
            Class parameterClass = parameterClassResolver.resolveClass(parameter);
            if (parameterClass != null) {
                value = templateParametersExtractor.convertFromString(parameter.getType(), parameterClass, parameter.getDefaultValue());
            }
        }

        field.setValue(value);

        if (BooleanUtils.isTrue(parameter.getValidationOn())) {
            field.addValidator(new ReportParamFieldValidator(parameter));
        }

        Label label = parameterFieldCreator.createLabel(parameter, field);
        label.setStyleName("jmix-report-parameter-caption");

        if (currentGridRow == 0) {
            if (field instanceof Component.Focusable) {
                ((Component.Focusable) field).focus();
            }
        }

        parametersGrid.add(label, 0, currentGridRow);
        parametersGrid.add(field, 1, currentGridRow);

        field.addValueChangeListener(e -> {
            Object fieldValue = e.getValue();
            updateDefaultValue(parameter, fieldValue);
        });

        if (field instanceof TagPicker) {
            TagPicker<Object> tagPicker = (TagPicker) field;
            tagPicker.addValueChangeListener(e -> {
                updateDefaultValue(parameter, e.getValue());
            });
        }

        return field;
    }

    private void updateDefaultValue(ReportInputParameter parameter, Object fieldValue) {
        String alias = parameter.getAlias();
        Report report = parameter.getReport();
        TemplateReport templateReport = templateReports.stream()
                .filter(e -> e.getReport().equals(report))
                .findFirst()
                .orElse(null);
        if (fieldValue == null && templateReport == null) {
            return;
        }
        ParameterValue parameterValue = templateReport.getParameterValues().stream()
                .filter(pv -> pv.getAlias().equals(alias))
                .findFirst()
                .orElse(null);
        if (parameterValue == null) {
            parameterValue = metadata.create(ParameterValue.class);
            parameterValue.setAlias(alias);
            parameterValue.setParameterType(parameter.getType());
            parameterValue.setTemplateReport(templateReport);
            templateReport.getParameterValues().add(parameterValue);
        }
        Class parameterClass = parameterClassResolver.resolveClass(parameter);
        String stringValue = templateParametersExtractor.convertToString(parameter.getType(), parameterClass, fieldValue);
        parameterValue.setDefaultValue(stringValue);
    }

}