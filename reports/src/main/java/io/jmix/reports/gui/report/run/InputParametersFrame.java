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
import io.jmix.core.JmixEntity;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.DataSupplier;
import io.jmix.reports.app.service.ReportService;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportInputParameter;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reports.gui.ReportGuiManager;
import io.jmix.reports.gui.ReportPrintHelper;
import io.jmix.reports.gui.report.validators.ReportCollectionValidator;
import io.jmix.reports.gui.report.validators.ReportParamFieldValidator;
import io.jmix.ui.component.GridLayout;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.*;

import static io.jmix.reports.gui.report.run.InputParametersWindow.BULK_PRINT;
import static io.jmix.reports.gui.report.run.InputParametersWindow.INPUT_PARAMETER;


public class InputParametersFrame extends AbstractFrame {
    public static final String REPORT_PARAMETER = "report";
    public static final String PARAMETERS_PARAMETER = "parameters";

    protected Report report;
    protected Map<String, Object> parameters;
    protected boolean bulkPrint;
    protected ReportInputParameter inputParameter;

    @Autowired
    protected LookupField<ReportTemplate> templateField;

    @Autowired
    protected LookupField<ReportOutputType> outputTypeField;

    @Autowired
    protected Label outputTypeLbl;

    @Autowired
    protected Label templateLbl;

    @Autowired
    protected GridLayout parametersGrid;

    @Autowired
    protected CollectionDatasource<ReportTemplate, UUID> templateReportsDs;

    @Autowired
    protected ReportService reportService;

    @Autowired
    protected DataSupplier dataSupplier;

    @Autowired
    protected ReportGuiManager reportGuiManager;

    @Autowired
    protected ParameterClassResolver parameterClassResolver;

    protected HashMap<String, Field> parameterComponents = new HashMap<>();

    protected ParameterFieldCreator parameterFieldCreator = new ParameterFieldCreator(this);

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        report = (Report) params.get(REPORT_PARAMETER);
        if (report != null && !report.getIsTmp()) {
            report = dataSupplier.reload(report, ReportService.MAIN_VIEW_NAME);
        }
        //noinspection unchecked
        parameters = (Map<String, Object>) params.get(PARAMETERS_PARAMETER);
        if (parameters == null) {
            parameters = Collections.emptyMap();
        }
        bulkPrint = BooleanUtils.isTrue((Boolean) params.get(BULK_PRINT));
        inputParameter = (ReportInputParameter) params.get(INPUT_PARAMETER);

        if (report != null) {
            if (CollectionUtils.isNotEmpty(report.getInputParameters())) {
                parametersGrid.setRows(report.getInputParameters().size() + 2);
                int currentGridRow = 2;
                for (ReportInputParameter parameter : report.getInputParameters()) {
                    if (bulkPrint && Objects.equals(inputParameter, parameter)) {
                        continue;
                    }
                    createComponent(parameter, currentGridRow, BooleanUtils.isNotTrue(parameter.getHidden()));
                    currentGridRow++;
                }
            }
            if (report.getTemplates() != null && report.getTemplates().size() > 1) {
                if (!report.getIsTmp()) {
                    templateReportsDs.refresh(ParamsMap.of("reportId", report.getId()));
                }
            }
        }
    }

    public Map<String, Object> collectParameters() {
        Map<String, Object> parameters = new HashMap<>();
        for (String paramName : parameterComponents.keySet()) {
            Field parameterField = parameterComponents.get(paramName);
            Object value = parameterField.getValue();
            parameters.put(paramName, value);
        }
        return parameters;
    }

    protected void createComponent(ReportInputParameter parameter, int currentGridRow, boolean visible) {
        Field field = parameterFieldCreator.createField(parameter);
        field.setWidth("400px");

        Object value = parameters.get(parameter.getAlias());

        if (value == null && parameter.getDefaultValue() != null) {
            Class parameterClass = parameterClassResolver.resolveClass(parameter);
            if (parameterClass != null) {
                value = reportService.convertFromString(parameterClass, parameter.getDefaultValue());
            }
        }

        if (!(field instanceof TokenList)) {
            field.setValue(value);
        } else {
            CollectionDatasource datasource = (CollectionDatasource) field.getDatasource();
            if (value instanceof Collection) {
                Collection collection = (Collection) value;
                for (Object selected : collection) {
                    datasource.includeItem((JmixEntity) selected);
                }
            }
        }

        if (BooleanUtils.isTrue(parameter.getValidationOn())) {
            field.addValidator(new ReportParamFieldValidator(parameter));
        }

        if (BooleanUtils.isTrue(field.isRequired())) {
            field.addValidator(new ReportCollectionValidator(field));
        }

        Label label = parameterFieldCreator.createLabel(parameter, field);
        label.setStyleName("c-report-parameter-caption");

        if (currentGridRow == 0) {
            //TODO request focus
//            field.requestFocus();
        }

        label.setVisible(visible);
        field.setVisible(visible);

        parameterComponents.put(parameter.getAlias(), field);
        parametersGrid.add(label, 0, currentGridRow);
        parametersGrid.add(field, 1, currentGridRow);
    }

    public void initTemplateAndOutputSelect() {
        if (report != null) {
            if (report.getTemplates() != null && report.getTemplates().size() > 1) {
                templateField.setValue(report.getDefaultTemplate());
                setTemplateVisible(true);
            }
            templateField.addValueChangeListener(e -> updateOutputTypes());
            updateOutputTypes();
        }
    }

    protected void updateOutputTypes() {
        if (!reportGuiManager.containsAlterableTemplate(report)) {
            setOutputTypeVisible(false);
            return;
        }

        ReportTemplate template;
        if (report.getTemplates() != null && report.getTemplates().size() > 1) {
            template = templateField.getValue();
        } else {
            template = report.getDefaultTemplate();
        }

        if (template != null && reportGuiManager.supportAlterableForTemplate(template)) {
            List<ReportOutputType> outputTypes = ReportPrintHelper.getInputOutputTypesMapping().get(template.getExt());
            if (outputTypes != null && !outputTypes.isEmpty()) {
                outputTypeField.setOptionsList(outputTypes);
                if (outputTypeField.getValue() == null) {
                    outputTypeField.setValue(template.getReportOutputType());
                }
                setOutputTypeVisible(true);
            } else {
                outputTypeField.setValue(null);
                setOutputTypeVisible(false);
            }
        } else {
            outputTypeField.setValue(null);
            setOutputTypeVisible(false);
        }
    }

    protected void setOutputTypeVisible(boolean visible) {
        outputTypeLbl.setVisible(visible);
        outputTypeField.setVisible(visible);
    }

    protected void setTemplateVisible(boolean visible) {
        templateLbl.setVisible(visible);
        templateField.setVisible(visible);
    }

    public Report getReport() {
        return report;
    }

    public ReportTemplate getReportTemplate() {
        return templateField.getValue();
    }

    public ReportOutputType getOutputType() {
        return outputTypeField.getValue();
    }
}