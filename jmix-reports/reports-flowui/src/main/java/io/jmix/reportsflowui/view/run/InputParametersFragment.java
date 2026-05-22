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

package io.jmix.reportsflowui.view.run;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.formlayout.FormLayout;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.SupportsValidation;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.reports.ParameterClassResolver;
import io.jmix.reports.ReportRepository;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportInputParameter;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reports.yarg.util.converter.ObjectToStringConverter;
import io.jmix.reportsflowui.runner.ReportExecutionPresentation;
import io.jmix.reportsflowui.runner.ReportExecutionPresentationIds;
import io.jmix.reportsflowui.runner.ReportPresentationRegistry;
import io.jmix.reportsflowui.view.validators.ReportCollectionValidator;
import io.jmix.reportsflowui.view.validators.ReportParamFieldValidator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class InputParametersFragment extends Composite<FormLayout>
        implements ApplicationContextAware, HasSize, HasEnabled, InitializingBean {

    protected EntityComboBox<ReportTemplate> templateComboBox;
    protected JmixComboBox<ReportOutputType> outputTypeComboBox;
    protected FormLayout formLayout;

    protected ReportRepository reportRepository;
    protected UiComponents uiComponents;
    protected Messages messages;
    protected Metadata metadata;
    protected ParameterComponentGenerationStrategy parameterComponentGenerationStrategy;
    protected ParameterClassResolver parameterClassResolver;
    protected ObjectToStringConverter objectToStringConverter;
    protected ApplicationContext applicationContext;
    protected ReportPresentationRegistry reportPresentationRegistry;

    protected Report report;
    protected Map<String, Object> parameters;
    protected boolean bulkPrint;
    protected String presentationId = ReportExecutionPresentationIds.DEFAULT;
    protected ReportInputParameter inputParameter;
    protected HashMap<String, AbstractField> parameterComponents = new HashMap<>();

    @Override
    public void afterPropertiesSet() {
        initComponent();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    protected void initComponent() {
        this.uiComponents = applicationContext.getBean(UiComponents.class);
        this.messages = applicationContext.getBean(Messages.class);
        this.reportRepository = applicationContext.getBean(ReportRepository.class);
        this.parameterComponentGenerationStrategy = applicationContext.getBean(ParameterComponentGenerationStrategy.class);
        this.parameterClassResolver = applicationContext.getBean(ParameterClassResolver.class);
        this.objectToStringConverter = applicationContext.getBean(ObjectToStringConverter.class);
        this.metadata = applicationContext.getBean(Metadata.class);
        this.reportPresentationRegistry = applicationContext.getBean(ReportPresentationRegistry.class);
    }

    @Override
    protected FormLayout initContent() {
        templateComboBox = uiComponents.create(EntityComboBox.class);
        templateComboBox.setId("templateComboBox");
        templateComboBox.setVisible(false);
        templateComboBox.setMetaClass(metadata.getClass(ReportTemplate.class));
        templateComboBox.setItems(Collections.emptyList());
        templateComboBox.setLabel(messages.getMessage(getClass(), "reportTemplate.label"));
        templateComboBox.addValueChangeListener(e -> updateOutputTypes());

        outputTypeComboBox = uiComponents.create(JmixComboBox.class);
        outputTypeComboBox.setId("outputTypeComboBox");
        outputTypeComboBox.setLabel(messages.getMessage(getClass(), "reportOutputType.label"));

        formLayout = uiComponents.create(FormLayout.class);
        formLayout.add(templateComboBox);
        formLayout.add(outputTypeComboBox);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("32em", 2),
                new FormLayout.ResponsiveStep("50em", 3)
        );

        updateOutputTypes();
        onInit();

        return formLayout;
    }

    protected void onInit() {
        initLayout();
    }

    protected void initLayout() {
        if (report != null) {
            report = reportRepository.reloadForRunning(report);
            if (parameters == null) {
                parameters = Collections.emptyMap();
            }

            if (CollectionUtils.isNotEmpty(report.getInputParameters())) {
                for (ReportInputParameter parameter : report.getInputParameters()) {
                    if (bulkPrint && Objects.equals(inputParameter, parameter)) {
                        continue;
                    }
                    createComponent(parameter, BooleanUtils.isNotTrue(parameter.getHidden()));
                }
            }
        }
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public void setBulkPrint(boolean bulkPrint) {
        this.bulkPrint = bulkPrint;
    }

    public void setPresentationId(String presentationId) {
        this.presentationId = presentationId;
    }

    public void setInputParameter(ReportInputParameter inputParameter) {
        this.inputParameter = inputParameter;
    }

    public void initTemplateAndOutputSelect() {
        if (report != null) {
            if (parameterComponents.isEmpty()) {
                initLayout();
            }
            ReportExecutionPresentation presentation = getPresentation();
            var availableTemplates = presentation.getAvailableTemplates(report);
            templateComboBox.setItems(availableTemplates);

            ReportTemplate template = presentation.resolveDefaultTemplate(report, templateComboBox.getValue());
            if (template != null && availableTemplates.contains(template)) {
                templateComboBox.setValue(template);
            } else {
                templateComboBox.clear();
            }

            boolean templateSelectionVisible = availableTemplates.size() > 1;
            templateComboBox.setVisible(templateSelectionVisible);
            templateComboBox.setEnabled(templateSelectionVisible);
            updateOutputTypes();
        }
    }

    protected void updateOutputTypes() {
        if (report == null) {
            outputTypeComboBox.clear();
            outputTypeComboBox.setVisible(false);
            outputTypeComboBox.setEnabled(false);
            return;
        }

        ReportExecutionPresentation presentation = getPresentation();
        ReportTemplate template = getEffectiveTemplate();
        var outputTypes = presentation.getAvailableOutputTypes(report, template);
        if (outputTypes.isEmpty()) {
            ReportOutputType outputType = presentation.resolveDefaultOutputType(report, template, outputTypeComboBox.getValue());
            if (outputType != null) {
                outputTypeComboBox.setItems(outputType);
                outputTypeComboBox.setValue(outputType);
            } else {
                outputTypeComboBox.clear();
            }
            outputTypeComboBox.setVisible(false);
            outputTypeComboBox.setEnabled(false);
            return;
        }

        outputTypeComboBox.setItems(outputTypes);
        ReportOutputType outputType = presentation.resolveDefaultOutputType(report, template, outputTypeComboBox.getValue());
        if (outputType != null && outputTypes.contains(outputType)) {
            outputTypeComboBox.setValue(outputType);
        } else {
            outputTypeComboBox.clear();
        }

        outputTypeComboBox.setVisible(true);
        outputTypeComboBox.setEnabled(outputTypes.size() > 1);
    }

    public Map<String, Object> collectParameters() {
        Map<String, Object> parameters = new HashMap<>();
        for (Map.Entry<String, AbstractField> parameterEntry : parameterComponents.entrySet()) {
            AbstractField parameterField = parameterEntry.getValue();
            Object value = UiComponentUtils.getValue(parameterField);
            parameters.put(parameterEntry.getKey(), value);
        }
        return parameters;
    }

    protected void createComponent(ReportInputParameter parameter, boolean visible) {
        AbstractField field = parameterComponentGenerationStrategy.createField(parameter);

        Object value = parameters.get(parameter.getAlias());
        if (value == null) {
            if (parameter.getDefaultValueProvider() != null) {
                value = parameter.getDefaultValueProvider().getDefaultValue(parameter);
            } else if (parameter.getDefaultValue() != null) {
                Class parameterClass = parameterClassResolver.resolveClass(parameter);
                if (parameterClass != null) {
                    value = objectToStringConverter.convertFromString(parameterClass, parameter.getDefaultValue());
                }
            }
        }

        if (value != null) {
            UiComponentUtils.setValue(field, value);
        }

        if (BooleanUtils.isTrue(parameter.getValidationOn())) {
            ((SupportsValidation) field).addValidator(
                    applicationContext.getBean(ReportParamFieldValidator.class, field, parameter)
            );
        }

        if (BooleanUtils.isTrue(parameter.getRequired())) {
            ((SupportsValidation) field).addValidator(
                    applicationContext.getBean(ReportCollectionValidator.class, field)
            );
        }

        field.setVisible(visible);

        parameterComponents.put(parameter.getAlias(), field);
        formLayout.add(field);
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    @Nullable
    public ReportTemplate getReportTemplate() {
        return getEffectiveTemplate();
    }

    @Nullable
    public ReportOutputType getOutputType() {
        return outputTypeComboBox.getValue();
    }

    @Nullable
    protected ReportTemplate getEffectiveTemplate() {
        if (report == null) {
            return null;
        }

        ReportTemplate selectedTemplate = templateComboBox.getValue();
        return selectedTemplate != null
                ? selectedTemplate
                : getPresentation().resolveDefaultTemplate(report, null);
    }

    protected ReportExecutionPresentation getPresentation() {
        return reportPresentationRegistry.getPresentation(presentationId);
    }
}
