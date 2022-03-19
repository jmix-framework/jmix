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

import com.haulmont.yarg.util.converter.ObjectToStringConverter;
import io.jmix.core.DataManager;
import io.jmix.core.Id;
import io.jmix.reports.ParameterClassResolver;
import io.jmix.reports.ReportPrintHelper;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportInputParameter;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reportsui.screen.report.validators.ReportCollectionValidator;
import io.jmix.reportsui.screen.report.validators.ReportParamFieldValidator;
import io.jmix.ui.component.*;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nullable;
import java.util.*;

import static io.jmix.reports.util.ReportTemplateUtils.containsAlterableTemplate;
import static io.jmix.reports.util.ReportTemplateUtils.supportAlterableForTemplate;

@UiController("report_InputParameters.fragment")
@UiDescriptor("input-parameters-fragment.xml")
public class InputParametersFragment extends ScreenFragment {
    public static final String REPORT_PARAMETER = "report";
    public static final String PARAMETERS_PARAMETER = "parameters";

    protected Report report;
    protected Map<String, Object> parameters;
    protected boolean bulkPrint;
    protected ReportInputParameter inputParameter;

    @Autowired
    protected ComboBox<ReportTemplate> templateComboBox;

    @Autowired
    protected ComboBox<ReportOutputType> outputTypeComboBox;

    @Autowired
    protected Label<String> outputTypeLabel;

    @Autowired
    protected Label<String> templateLabel;

    @Autowired
    protected GridLayout parametersGrid;

    @Autowired
    protected CollectionContainer<ReportTemplate> templateReportsDc;

    @Autowired
    protected ObjectToStringConverter objectToStringConverter;

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected ParameterClassResolver parameterClassResolver;

    @Autowired
    private ApplicationContext applicationContext;

    protected HashMap<String, Field> parameterComponents = new HashMap<>();

    @Autowired
    protected ParameterFieldCreator parameterFieldCreator;

    public void setReport(Report report) {
        this.report = report;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public void setBulkPrint(boolean bulkPrint) {
        this.bulkPrint = bulkPrint;
    }

    public void setInputParameter(ReportInputParameter inputParameter) {
        this.inputParameter = inputParameter;
    }

    @Subscribe
    protected void onInit(InitEvent event) {
        ScreenOptions options = event.getOptions();

        if (options instanceof MapScreenOptions) {
            MapScreenOptions mapScreenOptions = (MapScreenOptions) options;

            report = (Report) mapScreenOptions.getParams().get(REPORT_PARAMETER);
            parameters = (Map<String, Object>) mapScreenOptions.getParams().get(PARAMETERS_PARAMETER);
//            bulkPrint = BooleanUtils.isTrue((Boolean) mapScreenOptions.getParams().get(BULK_PRINT));
//            inputParameter = (ReportInputParameter) mapScreenOptions.getParams().get(INPUT_PARAMETER);
        }

        initLayout();
    }

    protected void initLayout() {
        if (report != null && !report.getIsTmp()) {
            report = dataManager.load(Id.of(report))
                    .fetchPlan("report.edit")
                    .one();
        }
        if (parameters == null) {
            parameters = Collections.emptyMap();
        }
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
                templateReportsDc.getMutableItems().addAll(report.getTemplates());
            }
        }
    }

    public Map<String, Object> collectParameters() {
        Map<String, Object> parameters = new HashMap<>();
        for (Map.Entry<String, Field> parameterEntry : parameterComponents.entrySet()) {
            Field parameterField = parameterEntry.getValue();
            Object value = parameterField.getValue();
            parameters.put(parameterEntry.getKey(), value);
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
                value = objectToStringConverter.convertFromString(parameterClass, parameter.getDefaultValue());
            }
        }

        field.setValue(value);

        if (BooleanUtils.isTrue(parameter.getValidationOn())) {
            field.addValidator(applicationContext.getBean(ReportParamFieldValidator.class, parameter));
        }

        if (BooleanUtils.isTrue(field.isRequired())) {
            field.addValidator(applicationContext.getBean(ReportCollectionValidator.class, field));
        }

        Label<String> label = parameterFieldCreator.createLabel(parameter, field);
        label.setStyleName("jmix-report-parameter-caption");

        if (currentGridRow == 2) {
            if (field instanceof Component.Focusable) {
                ((Component.Focusable) field).focus();
            }
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
                templateComboBox.setValue(report.getDefaultTemplate());
                setTemplateVisible(true);
            }
            templateComboBox.addValueChangeListener(e -> updateOutputTypes());
            updateOutputTypes();
        }
    }

    protected void updateOutputTypes() {
        if (!containsAlterableTemplate(report)) {
            setOutputTypeVisible(false);
            return;
        }

        ReportTemplate template;
        if (report.getTemplates() != null && report.getTemplates().size() > 1) {
            template = templateComboBox.getValue();
        } else {
            template = report.getDefaultTemplate();
        }

        if (template != null && supportAlterableForTemplate(template)) {
            List<ReportOutputType> outputTypes = ReportPrintHelper.getInputOutputTypesMapping().get(template.getExt());
            if (outputTypes != null && !outputTypes.isEmpty()) {
                outputTypeComboBox.setOptionsList(outputTypes);
                if (outputTypeComboBox.getValue() == null) {
                    outputTypeComboBox.setValue(template.getReportOutputType());
                }
                setOutputTypeVisible(true);
            } else {
                outputTypeComboBox.setValue(null);
                setOutputTypeVisible(false);
            }
        } else {
            outputTypeComboBox.setValue(null);
            setOutputTypeVisible(false);
        }
    }

    protected void setOutputTypeVisible(boolean visible) {
        outputTypeLabel.setVisible(visible);
        outputTypeComboBox.setVisible(visible);
    }

    protected void setTemplateVisible(boolean visible) {
        templateLabel.setVisible(visible);
        templateComboBox.setVisible(visible);
    }

    public Report getReport() {
        return report;
    }

    @Nullable
    public ReportTemplate getReportTemplate() {
        return templateComboBox.getValue();
    }

    public ReportOutputType getOutputType() {
        return outputTypeComboBox.getValue();
    }
}