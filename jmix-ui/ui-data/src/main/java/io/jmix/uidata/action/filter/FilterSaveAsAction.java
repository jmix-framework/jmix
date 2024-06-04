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

package io.jmix.uidata.action.filter;

import com.google.common.base.Strings;
import io.jmix.core.Messages;
import io.jmix.ui.Dialogs;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.action.filter.FilterAction;
import io.jmix.ui.app.filter.condition.LogicalFilterConditionEdit;
import io.jmix.ui.app.inputdialog.DialogActions;
import io.jmix.ui.app.inputdialog.DialogOutcome;
import io.jmix.ui.app.inputdialog.InputDialog;
import io.jmix.ui.app.inputdialog.InputParameter;
import io.jmix.ui.component.CheckBox;
import io.jmix.ui.component.Filter;
import io.jmix.ui.component.Form;
import io.jmix.ui.component.LogicalFilterComponent;
import io.jmix.ui.component.TextField;
import io.jmix.ui.component.ValidationErrors;
import io.jmix.ui.component.filter.FilterSupport;
import io.jmix.ui.component.filter.configuration.DesignTimeConfiguration;
import io.jmix.ui.component.filter.configuration.RunTimeConfiguration;
import io.jmix.ui.component.filter.converter.FilterConverter;
import io.jmix.ui.component.filter.registration.FilterComponents;
import io.jmix.ui.entity.LogicalFilterCondition;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.meta.StudioAction;
import io.jmix.ui.UiComponentProperties;
import io.jmix.uidata.app.filter.configuration.UiDataFilterConfigurationModelFragment;
import io.jmix.uidata.entity.FilterConfiguration;
import io.jmix.uidata.filter.UiDataFilterSupport;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static io.jmix.ui.component.filter.FilterUtils.generateConfigurationId;

@StudioAction(
        target = "io.jmix.ui.component.Filter",
        description = "Saves current filter configuration under a new id and name")
@ActionType(FilterSaveAsAction.ID)
public class FilterSaveAsAction extends FilterAction {

    public static final String ID = "filter_saveAs";

    protected Messages messages;
    protected Dialogs dialogs;
    protected FilterSupport filterSupport;
    protected FilterComponents filterComponents;
    protected UiComponents uiComponents;
    protected UiComponentProperties componentProperties;

    protected Consumer<InputDialog.InputDialogCloseEvent> inputDialogCloseListener;

    public FilterSaveAsAction() {
        this(ID);
    }

    public FilterSaveAsAction(String id) {
        super(id);

        initDefaultInputDialogCloseListener();
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.caption = messages.getMessage("actions.Filter.SaveAs");
        this.messages = messages;
    }

    @Autowired
    protected void setIcons(Icons icons) {
        this.icon = icons.get(JmixIcon.SAVE);
    }

    @Autowired
    public void setDialogs(Dialogs dialogs) {
        this.dialogs = dialogs;
    }

    @Autowired
    public void setFilterSupport(FilterSupport filterSupport) {
        this.filterSupport = filterSupport;
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
    public void setUiComponentProperties(UiComponentProperties componentProperties) {
        this.componentProperties = componentProperties;
    }

    public void setInputDialogCloseListener(Consumer<InputDialog.InputDialogCloseEvent> inputDialogCloseListener) {
        this.inputDialogCloseListener = inputDialogCloseListener;
    }

    @Override
    protected boolean isApplicable() {
        return super.isApplicable()
                && !(filter.getCurrentConfiguration() instanceof DesignTimeConfiguration);
    }

    @Override
    public void execute() {
        openInputDialog();
    }

    protected void initDefaultInputDialogCloseListener() {
        inputDialogCloseListener = inputDialogCloseEvent -> {
            if (inputDialogCloseEvent.closedWith(DialogOutcome.OK)) {
                applyDefaultInputDialogOkAction(inputDialogCloseEvent);
            }
        };
    }

    protected void applyDefaultInputDialogOkAction(InputDialog.InputDialogCloseEvent inputDialogCloseEvent) {
        String id = inputDialogCloseEvent.getValue("idField");
        if (id != null) {
            Filter.Configuration configuration = filter.getCurrentConfiguration();
            Filter.Configuration copy = copyConfiguration(id, configuration);
            copy.setName(inputDialogCloseEvent.getValue("nameField"));
            saveNewConfigurationModel(copy);
        }
    }

    protected void openInputDialog() {
        if (filter.getFrame() == null) {
            throw new IllegalStateException("Filter component is not attached to the Frame");
        }

        InputDialog inputDialog = dialogs.createInputDialog(filter.getFrame().getFrameOwner())
                .withCaption(messages.getMessage(FilterSaveAction.class,
                        "saveFilterConfigurationInputDialog.caption"))
                .withParameters(
                        InputParameter.stringParameter("nameField")
                                .withCaption(messages.getMessage(FilterConfiguration.class,
                                        "FilterConfiguration.name"))
                                .withRequired(true),
                        InputParameter.booleanParameter("generatedIdField")
                                .withCaption(messages.getMessage(UiDataFilterConfigurationModelFragment.class,
                                        "uiDataFilterConfigurationModelFragment.generatedIdField"))
                                .withDefaultValue(true),
                        InputParameter.stringParameter("idField")
                                .withRequired(true)
                                .withCaption(messages.getMessage(FilterConfiguration.class,
                                        "FilterConfiguration.configurationId"))
                )
                .withActions(DialogActions.OK_CANCEL)
                .withValidator(validationContext -> {
                    String id = validationContext.getValue("idField");
                    if (Strings.isNullOrEmpty(id)) {
                        return ValidationErrors.of(messages.getMessage(FilterSaveAsAction.class,
                                "saveFilterConfigurationInputDialog.idField.emptyValue"));
                    }

                    if (filter.getConfiguration(id) != null) {
                        return ValidationErrors.of(messages.getMessage(LogicalFilterConditionEdit.class,
                                "logicalFilterConditionEdit.uniqueConfigurationId"));
                    }

                    if (componentProperties.isFilterConfigurationUniqueNamesEnabled()) {
                        String name = validationContext.getValue("nameField");
                        boolean configurationWithSameNameExists = filter.getConfigurations().stream()
                                .anyMatch(conf -> Objects.equals(name, conf.getName()) && !conf.isAvailableForAllUsers());
                        if (configurationWithSameNameExists) {
                            return ValidationErrors.of(messages.getMessage(UiDataFilterConfigurationModelFragment.class,
                                    "uiDataFilterConfigurationModelFragment.nameField.nonUniqueUserName"));
                        }
                    }

                    return ValidationErrors.none();
                })
                .withCloseListener(inputDialogCloseListener)
                .show();

        Form form = (Form) inputDialog.getWindow().getComponentNN("form");
        initInputDialogFormFields(form);
    }

    @SuppressWarnings("unchecked")
    protected void initInputDialogFormFields(Form form) {
        CheckBox generatedIdField = (CheckBox) form.getComponentNN("generatedIdField");
        TextField<String> idField = (TextField<String>) form.getComponentNN("idField");
        idField.setEnabled(!generatedIdField.isChecked());

        generatedIdField.addValueChangeListener(valueChangeEvent -> {
            boolean checked = BooleanUtils.isTrue(valueChangeEvent.getValue());
            idField.setEnabled(!checked);
        });

        TextField<String> nameField = (TextField<String>) form.getComponentNN("nameField");
        nameField.addValueChangeListener(valueChangeEvent -> {
            if (generatedIdField.isChecked()) {
                idField.setValue(generateConfigurationId(valueChangeEvent.getValue()));
            }
        });

        generatedIdField.setVisible(componentProperties.isFilterShowConfigurationIdField());
        idField.setVisible(componentProperties.isFilterShowConfigurationIdField());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Filter.Configuration copyConfiguration(String newId, Filter.Configuration existingConfiguration) {
        LogicalFilterComponent rootLogicalFilterComponent = existingConfiguration.getRootLogicalFilterComponent();
        FilterConverter converter =
                filterComponents.getConverterByComponentClass(rootLogicalFilterComponent.getClass(), filter);
        Map<String, Object> valuesMap = filterSupport.initConfigurationValuesMap(existingConfiguration);
        LogicalFilterCondition logicalFilterCondition =
                (LogicalFilterCondition) converter.convertToModel(rootLogicalFilterComponent);
        filterSupport.resetConfigurationValuesMap(existingConfiguration, valuesMap);

        LogicalFilterComponent logicalFilterComponent =
                (LogicalFilterComponent) converter.convertToComponent(logicalFilterCondition);
        Filter.Configuration newConfiguration = new RunTimeConfiguration(newId, logicalFilterComponent,
                existingConfiguration.getOwner());
        filterSupport.refreshConfigurationDefaultValues(newConfiguration);
        filterSupport.resetConfigurationValuesMap(newConfiguration, valuesMap);
        return newConfiguration;
    }

    protected void saveNewConfigurationModel(Filter.Configuration configuration) {
        Map<String, Object> valuesMap = filterSupport.initConfigurationValuesMap(configuration);
        ((UiDataFilterSupport) filterSupport).saveConfigurationModel(configuration, null);
        filterSupport.resetConfigurationValuesMap(configuration, valuesMap);
        filter.addConfiguration(configuration);
        setCurrentFilterConfiguration(configuration);
        filter.getEmptyConfiguration().getRootLogicalFilterComponent().removeAll();
    }

    protected void setCurrentFilterConfiguration(Filter.Configuration configuration) {
        configuration.setModified(false);
        filter.setCurrentConfiguration(configuration);
    }
}
