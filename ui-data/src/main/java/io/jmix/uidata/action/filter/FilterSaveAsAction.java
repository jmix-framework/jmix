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
import io.jmix.ui.action.ActionType;
import io.jmix.ui.action.filter.FilterAction;
import io.jmix.ui.app.inputdialog.DialogActions;
import io.jmix.ui.app.inputdialog.DialogOutcome;
import io.jmix.ui.app.inputdialog.InputParameter;
import io.jmix.ui.component.Filter;
import io.jmix.ui.component.LogicalFilterComponent;
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
import io.jmix.uidata.filter.DataFilterSupport;
import org.springframework.beans.factory.annotation.Autowired;

@StudioAction(category = "Filter Actions",
        description = "Saves current filter configuration under a new code and caption")
@ActionType(FilterSaveAsAction.ID)
public class FilterSaveAsAction extends FilterAction {

    public static final String ID = "filter_save_as";

    protected Messages messages;
    protected Dialogs dialogs;
    protected FilterSupport filterSupport;
    protected FilterComponents filterComponents;

    public FilterSaveAsAction() {
        this(ID);
    }

    public FilterSaveAsAction(String id) {
        super(id);
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

    @Override
    protected boolean isApplicable() {
        return super.isApplicable()
                && !(filter.getCurrentConfiguration() instanceof DesignTimeConfiguration);
    }

    @Override
    public void execute() {
        openInputDialog();
    }

    protected void openInputDialog() {
        if (filter.getFrame() == null) {
            throw new IllegalStateException("Filter component is not attached to the Frame");
        }

        Filter.Configuration configuration = filter.getCurrentConfiguration();
        dialogs.createInputDialog(filter.getFrame().getFrameOwner())
                .withCaption(messages.getMessage(FilterSaveAction.class,
                        "saveFilterConfigurationInputDialog.caption"))
                .withParameters(
                        InputParameter.stringParameter("codeField")
                                .withRequired(true)
                                .withCaption(messages.getMessage(FilterSaveAction.class,
                                        "saveFilterConfigurationInputDialog.codeField")),
                        InputParameter.stringParameter("captionField")
                                .withCaption(messages.getMessage(FilterSaveAction.class,
                                        "saveFilterConfigurationInputDialog.captionField"))
                                .withDefaultValue(configuration.getCaption())
                )
                .withActions(DialogActions.OK_CANCEL)
                .withValidator(validationContext -> {
                    String code = validationContext.getValue("codeField");
                    if (Strings.isNullOrEmpty(code)) {
                        return ValidationErrors.of(messages.getMessage(FilterSaveAction.class,
                                "saveFilterConfigurationInputDialog.codeField.emptyValue"));
                    }

                    if (filterSupport.filterConfigurationExists(code, filter)) {
                        return ValidationErrors.of(messages.getMessage(
                                "saveFilterConfigurationInputDialog.codeField.uniqueValue"));
                    }

                    return ValidationErrors.none();
                })
                .withCloseListener(inputDialogCloseEvent -> {
                    if (inputDialogCloseEvent.closedWith(DialogOutcome.OK)) {
                        String code = inputDialogCloseEvent.getValue("codeField");
                        if (code != null) {
                            Filter.Configuration copy = copyConfiguration(code, configuration);
                            copy.setCaption(inputDialogCloseEvent.getValue("captionField"));
                            collectConfiguration(copy);
                        }
                    }
                })
                .show();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Filter.Configuration copyConfiguration(String newCode, Filter.Configuration existingConfiguration) {
        LogicalFilterComponent rootLogicalFilterComponent = existingConfiguration.getRootLogicalFilterComponent();
        FilterConverter converter =
                filterComponents.getConverterByComponentClass(rootLogicalFilterComponent.getClass(), filter);
        LogicalFilterCondition logicalFilterCondition =
                (LogicalFilterCondition) converter.convertToModel(rootLogicalFilterComponent);
        Filter owner = existingConfiguration.getOwner();

        LogicalFilterComponent logicalFilterComponent =
                (LogicalFilterComponent) converter.convertToComponent(logicalFilterCondition);

        return new RunTimeConfiguration(newCode, logicalFilterComponent, owner);
    }

    protected void collectConfiguration(Filter.Configuration configuration) {
        ((DataFilterSupport) filterSupport).saveFilterConfiguration(configuration);
        filter.addConfiguration(configuration);
        setCurrentFilterConfiguration(configuration);
        filter.getEmptyConfiguration().getRootLogicalFilterComponent().removeAll();
    }

    protected void setCurrentFilterConfiguration(Filter.Configuration configuration) {
        configuration.setModified(false);
        filter.setCurrentConfiguration(configuration);
    }
}
