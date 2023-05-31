/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowuidata.action.genericfilter;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.Messages;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.FlowuiComponentProperties;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.genericfilter.GenericFilterAction;
import io.jmix.flowui.app.filter.condition.LogicalFilterConditionDetailView;
import io.jmix.flowui.app.inputdialog.DialogActions;
import io.jmix.flowui.app.inputdialog.DialogOutcome;
import io.jmix.flowui.app.inputdialog.InputDialog;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.genericfilter.Configuration;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.genericfilter.GenericFilterSupport;
import io.jmix.flowui.component.genericfilter.configuration.FilterConfigurationDetail;
import io.jmix.flowui.component.genericfilter.configuration.RunTimeConfiguration;
import io.jmix.flowui.component.genericfilter.converter.FilterConverter;
import io.jmix.flowui.component.genericfilter.registration.FilterComponents;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.entity.filter.LogicalFilterCondition;
import io.jmix.flowui.kit.component.FlowuiComponentUtils;
import io.jmix.flowui.view.View;
import io.jmix.flowuidata.entity.FilterConfiguration;
import io.jmix.flowuidata.genericfilter.FlowuiDataGenericFilterSupport;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.stream.Collectors;

import static io.jmix.flowui.app.inputdialog.InputParameter.booleanParameter;
import static io.jmix.flowui.app.inputdialog.InputParameter.stringParameter;
import static io.jmix.flowui.component.genericfilter.FilterUtils.generateConfigurationId;

public abstract class AbstractGenericFilterSaveAction<A extends AbstractGenericFilterSaveAction<A>>
        extends GenericFilterAction<A> {

    protected Messages messages;
    protected Dialogs dialogs;
    protected GenericFilterSupport genericFilterSupport;
    protected FilterComponents filterComponents;
    protected UiComponents uiComponents;
    protected FlowuiComponentProperties flowuiComponentProperties;

    protected Registration configurationRefreshedRegistration;

    protected ComponentEventListener<InputDialog.InputDialogCloseEvent> inputDialogCloseListener;

    public AbstractGenericFilterSaveAction(String id) {
        super(id);
    }

    @Autowired
    public void setDialogs(Dialogs dialogs) {
        this.dialogs = dialogs;
    }

    @Autowired
    public void setGenericFilterSupport(GenericFilterSupport genericFilterSupport) {
        this.genericFilterSupport = genericFilterSupport;
    }

    @Autowired
    public void setFilterComponents(FilterComponents filterComponents) {
        this.filterComponents = filterComponents;
    }

    @Autowired
    public void setUiComponents(UiComponents uiComponents) {
        this.uiComponents = uiComponents;
    }

    @Autowired
    public void setFlowuiComponentProperties(FlowuiComponentProperties flowuiComponentProperties) {
        this.flowuiComponentProperties = flowuiComponentProperties;
    }

    @Override
    protected void initAction() {
        super.initAction();

        this.icon = FlowuiComponentUtils.convertToIcon(VaadinIcon.ARCHIVE);
        this.inputDialogCloseListener = inputDialogCloseEvent -> {
            if (inputDialogCloseEvent.closedWith(DialogOutcome.OK)) {
                applyDefaultInputDialogOkAction(inputDialogCloseEvent);
            }
        };
    }

    @Override
    protected void bindListeners(GenericFilter target) {
        super.bindListeners(target);
        bindConfigurationRefreshedListener(target);
    }

    protected void bindConfigurationRefreshedListener(GenericFilter target) {
        configurationRefreshedRegistration = target.addConfigurationRefreshListener(this::onConfigurationRefreshed);
    }

    @Override
    protected void unbindListeners() {
        super.unbindListeners();
        unbindConfigurationRefreshed();
    }

    protected void unbindConfigurationRefreshed() {
        if (configurationRefreshedRegistration != null) {
            configurationRefreshedRegistration.remove();
            configurationRefreshedRegistration = null;
        }
    }

    protected void onConfigurationRefreshed(GenericFilter.ConfigurationRefreshEvent event) {
        refreshState();
    }

    protected void applyDefaultInputDialogOkAction(InputDialog.InputDialogCloseEvent inputDialogCloseEvent) {
        String id = inputDialogCloseEvent.getValue("idField");

        if (id != null) {
            Configuration configuration = target.getCurrentConfiguration();
            Configuration copy = copyConfiguration(id, configuration);

            copy.setName(inputDialogCloseEvent.getValue("nameField"));
            saveNewConfigurationModel(copy);
        }
    }

    protected void openInputDialog() {
        checkTarget();

        View<?> parent = findParentView();

        if (parent == null) {
            throw new IllegalStateException(String.format("A component '%s' is not attached to a view",
                    target.getClass().getSimpleName()));
        }

        String header = messages.getMessage(getClass(), "saveFilterConfigurationInputDialog.header");

        String nameFieldLabel = messages.getMessage(FilterConfiguration.class, "FilterConfiguration.name");
        String generatedIdField = messages.getMessage(FilterConfigurationDetail.class,
                "filterConfigurationDetail.generatedIdField.label");
        String idFieldLabel = messages.getMessage(FilterConfiguration.class, "FilterConfiguration.configurationId");

        InputDialog inputDialog = dialogs.createInputDialog(parent)
                .withHeader(header)
                .withLabelsPosition(Dialogs.InputDialogBuilder.LabelsPosition.TOP)
                .withParameters(
                        stringParameter("nameField")
                                .withLabel(nameFieldLabel)
                                .withRequired(true),
                        booleanParameter("generatedIdField")
                                .withLabel(generatedIdField)
                                .withDefaultValue(true),
                        stringParameter("idField")
                                .withLabel(idFieldLabel)
                                .withRequired(true)
                )
                .withActions(DialogActions.OK_CANCEL)
                .withValidator(this::inputDialogValidator)
                .withCloseListener(inputDialogCloseListener)
                .open();

        FormLayout formLayout = inputDialog.getFormLayout();
        initInputDialogFormFields(formLayout);
    }

    @SuppressWarnings("unchecked")
    protected void initInputDialogFormFields(FormLayout formLayout) {
        Map<String, Component> childrenMap = formLayout.getChildren()
                .filter(component -> component instanceof FormLayout.FormItem)
                .map(this::formItemMapper)
                .collect(Collectors.toMap(component -> component.getId().orElseThrow(), component -> component));

        JmixCheckbox generatedIdField = (JmixCheckbox) childrenMap.get("generatedIdField");
        TypedTextField<String> idField = (TypedTextField<String>) childrenMap.get("idField");
        TypedTextField<String> nameField = (TypedTextField<String>) childrenMap.get("nameField");

        idField.setEnabled(!generatedIdField.getValue());

        generatedIdField.addValueChangeListener(event -> {
            idField.setEnabled(!event.getValue());

            if (event.getValue()) {
                idField.setTypedValue(generateConfigurationId(nameField.getTypedValue()));
            }
        });

        nameField.addTypedValueChangeListener(event -> {
            if (generatedIdField.getValue()) {
                idField.setTypedValue(generateConfigurationId(event.getValue()));
            }
        });

        generatedIdField.getParent()
                .ifPresent(formItem ->
                        formItem.setVisible(
                                flowuiComponentProperties.isFilterShowConfigurationIdField()));
        idField.getParent()
                .ifPresent(formItem -> formItem.setVisible(
                        flowuiComponentProperties.isFilterShowConfigurationIdField()));
    }

    protected Component formItemMapper(Component formItem) {
        return formItem.getChildren()
                .filter(component -> !(component instanceof Label))
                .findAny()
                .orElseThrow();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Configuration copyConfiguration(String newId, Configuration existingConfiguration) {
        LogicalFilterComponent rootLogicalFilterComponent = existingConfiguration.getRootLogicalFilterComponent();
        FilterConverter converter =
                filterComponents.getConverterByComponentClass(rootLogicalFilterComponent.getClass(), target);
        Map<String, Object> valuesMap = genericFilterSupport.initConfigurationValuesMap(existingConfiguration);
        LogicalFilterCondition logicalFilterCondition =
                (LogicalFilterCondition) converter.convertToModel(rootLogicalFilterComponent);

        genericFilterSupport.resetConfigurationValuesMap(existingConfiguration, valuesMap);

        LogicalFilterComponent logicalFilterComponent =
                (LogicalFilterComponent) converter.convertToComponent(logicalFilterCondition);
        Configuration newConfiguration = new RunTimeConfiguration(newId, logicalFilterComponent,
                existingConfiguration.getOwner());


        genericFilterSupport.refreshConfigurationDefaultValues(newConfiguration);
        genericFilterSupport.resetConfigurationValuesMap(newConfiguration, valuesMap);

        return newConfiguration;
    }

    protected void saveNewConfigurationModel(Configuration configuration) {
        Map<String, Object> valuesMap = genericFilterSupport.initConfigurationValuesMap(configuration);
        ((FlowuiDataGenericFilterSupport) genericFilterSupport).saveConfigurationModel(configuration, null);

        genericFilterSupport.resetConfigurationValuesMap(configuration, valuesMap);
        target.addConfiguration(configuration);

        setCurrentFilterConfiguration(configuration);
        target.getEmptyConfiguration().getRootLogicalFilterComponent().removeAll();
    }

    protected void setCurrentFilterConfiguration(Configuration configuration) {
        configuration.setModified(false);
        target.setCurrentConfiguration(configuration);
    }

    protected ValidationErrors inputDialogValidator(InputDialog.ValidationContext validationContext) {
        String id = validationContext.getValue("idField");
        if (Strings.isNullOrEmpty(id)) {
            return ValidationErrors.of(messages.getMessage(GenericFilterSaveAsAction.class,
                    "saveFilterConfigurationInputDialog.idField.emptyValue"));
        }

        if (target.getConfiguration(id) != null) {
            return ValidationErrors.of(messages.getMessage(LogicalFilterConditionDetailView.class,
                    "logicalFilterConditionDetailView.uniqueConfigurationId"));
        }

        return ValidationErrors.none();
    }

    protected void saveExistedConfigurationModel(Configuration configuration,
                                                 @Nullable FilterConfiguration existedConfigurationModel) {
        Map<String, Object> valuesMap = genericFilterSupport.initConfigurationValuesMap(configuration);
        ((FlowuiDataGenericFilterSupport) genericFilterSupport).saveConfigurationModel(configuration,
                existedConfigurationModel);

        genericFilterSupport.resetConfigurationValuesMap(configuration, valuesMap);

        setCurrentFilterConfiguration(configuration);
    }

    protected boolean isCurrentConfigurationAvailableForAll() {
        Configuration currentConfiguration = target.getCurrentConfiguration();
        FilterConfiguration model = ((FlowuiDataGenericFilterSupport) genericFilterSupport)
                .loadFilterConfigurationModel(target, currentConfiguration.getId());

        return model != null && Strings.isNullOrEmpty(model.getUsername());
    }
}
