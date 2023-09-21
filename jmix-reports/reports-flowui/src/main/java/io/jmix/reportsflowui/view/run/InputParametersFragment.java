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
import io.jmix.core.FetchPlan;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.SupportsValidation;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.DataComponents;
import io.jmix.reports.ParameterClassResolver;
import io.jmix.reports.ReportPrintHelper;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportInputParameter;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reports.util.ReportsUtils;
import io.jmix.reports.yarg.util.converter.ObjectToStringConverter;
import io.jmix.reportsflowui.view.validators.ReportCollectionValidator;
import io.jmix.reportsflowui.view.validators.ReportParamFieldValidator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nullable;
import java.util.*;

import static io.jmix.reports.util.ReportTemplateUtils.containsAlterableTemplate;
import static io.jmix.reports.util.ReportTemplateUtils.supportAlterableForTemplate;

public class InputParametersFragment extends Composite<FormLayout>
        implements ApplicationContextAware, HasSize, HasEnabled, InitializingBean {

    //Components
    protected CollectionContainer<ReportTemplate> templateReportsDc;
    protected EntityComboBox<ReportTemplate> templateComboBox;
    protected JmixComboBox<ReportOutputType> outputTypeComboBox;
    protected FormLayout formLayout;

    // Autowired
    private ReportsUtils reportsUtils;
    protected UiComponents uiComponents;
    protected Messages messages;
    protected Metadata metadata;
    protected ParameterComponentGenerationStrategy parameterComponentGenerationStrategy;
    protected ParameterClassResolver parameterClassResolver;
    protected ObjectToStringConverter objectToStringConverter;
    protected DataComponents dataComponents;
    protected ApplicationContext applicationContext;

    protected Report report;
    protected Map<String, Object> parameters;
    protected boolean bulkPrint;
    protected ReportInputParameter inputParameter;
    protected HashMap<String, AbstractField> parameterComponents = new HashMap<>();

    private void initComponent() {
        this.uiComponents = applicationContext.getBean(UiComponents.class);
        this.messages = applicationContext.getBean(Messages.class);
        this.reportsUtils = applicationContext.getBean(ReportsUtils.class);
        this.parameterComponentGenerationStrategy = applicationContext.getBean(ParameterComponentGenerationStrategy.class);
        this.parameterClassResolver = applicationContext.getBean(ParameterClassResolver.class);
        this.objectToStringConverter = applicationContext.getBean(ObjectToStringConverter.class);
        this.dataComponents = applicationContext.getBean(DataComponents.class);
        this.metadata = applicationContext.getBean(Metadata.class);
    }

    protected void updateOutputTypes() {
        if (!containsAlterableTemplate(report)) {
            outputTypeComboBox.setVisible(false);
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
                outputTypeComboBox.setItems(outputTypes);
                if (outputTypeComboBox.getValue() == null) {
                    outputTypeComboBox.setValue(template.getReportOutputType());
                }
                outputTypeComboBox.setVisible(true);
            } else {
                outputTypeComboBox.clear();
                outputTypeComboBox.setVisible(false);
            }
        } else {
            outputTypeComboBox.clear();
            outputTypeComboBox.setVisible(false);
        }
    }

    @Override
    protected FormLayout initContent() {
        templateComboBox = uiComponents.create(EntityComboBox.class);
        templateComboBox.setVisible(false);
        templateComboBox.setMetaClass(metadata.getClass(ReportTemplate.class));
        templateComboBox.setItems(report.getTemplates());
        templateComboBox.setLabel(messages.getMessage(getClass(), "reportTemplate.label"));
        templateComboBox.addValueChangeListener(e -> updateOutputTypes());

        outputTypeComboBox = uiComponents.create(JmixComboBox.class);
        outputTypeComboBox.setLabel(messages.getMessage(getClass(), "reportOutputType.label"));

        formLayout = uiComponents.create(FormLayout.class);
        formLayout.add(templateComboBox);
        formLayout.add(outputTypeComboBox);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("32em", 2),
                new FormLayout.ResponsiveStep("50em", 3)
        );

        createTemplateReportDc();

        updateOutputTypes();

        onInit();

        return formLayout;
    }

    private void createTemplateReportDc() {
        templateReportsDc = dataComponents.createCollectionContainer(ReportTemplate.class);
        CollectionLoader<ReportTemplate> loader = dataComponents.createCollectionLoader();

        loader.setQuery("select e from report_ReportTemplate e");
        loader.setFetchPlan(FetchPlan.INSTANCE_NAME);
        loader.setContainer(templateReportsDc);
        loader.load();
    }

    @Override
    public void afterPropertiesSet() {
        initComponent();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
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

    protected void onInit() {
        initLayout();
    }

    protected void initLayout() {
        if (report != null) {
            report = reportsUtils.reloadReportIfNeeded(report, "report.edit");
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
            if (report.getTemplates() != null && report.getTemplates().size() > 1) {
                templateReportsDc.getMutableItems().addAll(report.getTemplates());
            }
        }
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
        if (value == null && parameter.getDefaultValue() != null) {
            Class parameterClass = parameterClassResolver.resolveClass(parameter);
            if (parameterClass != null) {
                value = objectToStringConverter.convertFromString(parameterClass, parameter.getDefaultValue());
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

    public void initTemplateAndOutputSelect() {
        if (report != null) {
            if (report.getTemplates() != null && report.getTemplates().size() > 1) {
                templateComboBox.setValue(report.getDefaultTemplate());
                templateComboBox.setVisible(true);
            }
            templateComboBox.addValueChangeListener(e -> updateOutputTypes());
            updateOutputTypes();
        }
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    @Nullable
    public ReportTemplate getReportTemplate() {
        return templateComboBox.getValue();
    }

    public ReportOutputType getOutputType() {
        return outputTypeComboBox.getValue();
    }
}