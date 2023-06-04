/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.action.genericfilter;

import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.AccessManager;
import io.jmix.core.Messages;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.accesscontext.FlowuiFilterModifyConfigurationContext;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.AdjustWhenViewReadOnly;
import io.jmix.flowui.app.filter.condition.AddConditionView;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.filter.FilterComponent;
import io.jmix.flowui.component.filter.SingleFilterComponentBase;
import io.jmix.flowui.component.genericfilter.Configuration;
import io.jmix.flowui.component.genericfilter.FilterUtils;
import io.jmix.flowui.component.genericfilter.builder.GenericFilterConditionsBuilder;
import io.jmix.flowui.component.genericfilter.configuration.DesignTimeConfiguration;
import io.jmix.flowui.component.genericfilter.converter.FilterConverter;
import io.jmix.flowui.component.genericfilter.registration.FilterComponents;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent;
import io.jmix.flowui.entity.filter.FilterCondition;
import io.jmix.flowui.entity.filter.HeaderFilterCondition;
import io.jmix.flowui.kit.component.FlowuiComponentUtils;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.LookupView.ValidationContext;
import io.jmix.flowui.view.View;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.lang.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

@ActionType(GenericFilterAddConditionAction.ID)
public class GenericFilterAddConditionAction extends GenericFilterAction<GenericFilterAddConditionAction>
        implements AdjustWhenViewReadOnly {

    public static final String ID = "genericFilter_addCondition";

    protected Messages messages;
    protected DialogWindows dialogWindows;
    protected Notifications notifications;
    protected GenericFilterConditionsBuilder builder;
    protected FilterComponents filterComponents;

    protected Predicate<ValidationContext<FilterCondition>> selectValidator;
    protected Consumer<Collection<FilterCondition>> selectHandler;

    public GenericFilterAddConditionAction() {
        super(ID);
    }

    public GenericFilterAddConditionAction(String id) {
        super(id);
    }

    @Override
    protected void initAction() {
        super.initAction();

        this.icon = FlowuiComponentUtils.convertToIcon(VaadinIcon.PLUS);

        initDefaultSelectValidator();
        initDefaultSelectHandler();
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.messages = messages;
        this.text = messages.getMessage("actions.genericFilter.AddCondition");
    }

    @Autowired
    public void setDialogWindows(DialogWindows dialogWindows) {
        this.dialogWindows = dialogWindows;
    }

    @Autowired
    public void setNotifications(Notifications notifications) {
        this.notifications = notifications;
    }

    @Autowired
    public void setBuilder(GenericFilterConditionsBuilder builder) {
        this.builder = builder;
    }

    @Autowired
    public void setFilterComponents(FilterComponents filterComponents) {
        this.filterComponents = filterComponents;
    }

    @Autowired
    protected void setAccessManager(AccessManager accessManager) {
        FlowuiFilterModifyConfigurationContext context = new FlowuiFilterModifyConfigurationContext();
        accessManager.applyRegisteredConstraints(context);
        visibleBySpecificUiPermission = context.isPermitted();
    }

    public void setSelectValidator(@Nullable Predicate<ValidationContext<FilterCondition>> selectValidator) {
        this.selectValidator = selectValidator;
    }

    public void setSelectHandler(@Nullable Consumer<Collection<FilterCondition>> selectHandler) {
        this.selectHandler = selectHandler;
    }

    @Override
    protected boolean isApplicable() {
        return super.isApplicable()
                && !(target.getCurrentConfiguration() instanceof DesignTimeConfiguration);
    }

    @Override
    public void execute() {
        checkTarget();
        List<FilterCondition> allConditions = builder.buildConditions(target);
        openAddConditionView(allConditions);
    }

    public GenericFilterAddConditionAction withSelectValidator(
            @Nullable Predicate<ValidationContext<FilterCondition>> validator) {
        setSelectValidator(validator);
        return this;
    }

    public GenericFilterAddConditionAction withSelectHandler(@Nullable Consumer<Collection<FilterCondition>> handler) {
        setSelectHandler(handler);
        return this;
    }

    protected void initDefaultSelectValidator() {
        selectValidator = context -> {
            for (FilterCondition selectedCondition : context.getSelectedItems()) {
                if (selectedCondition instanceof HeaderFilterCondition) {
                    String text = messages.formatMessage("",
                            "actions.genericFilter.AddCondition.invalidCondition",
                            selectedCondition.getLocalizedLabel());

                    notifications.create(text)
                            .withType(Notifications.Type.WARNING)
                            .show();
                    return false;
                }
            }

            return true;
        };
    }

    protected void initDefaultSelectHandler() {
        selectHandler = selectedConditions -> {
            if (!selectedConditions.isEmpty()) {
                Configuration currentConfiguration = target.getCurrentConfiguration();

                boolean dataLoadNeeded = false;
                for (FilterCondition selectedCondition : selectedConditions) {
                    FilterConverter converter = filterComponents.getConverterByModelClass(
                            selectedCondition.getClass(), target);

                    FilterCondition parent = selectedCondition.getParent();
                    if (parent instanceof HeaderFilterCondition) {
                        selectedCondition.setParent(null);
                    }

                    FilterComponent filterComponent = converter.convertToComponent(selectedCondition);
                    currentConfiguration.setFilterComponentModified(filterComponent, true);
                    currentConfiguration.getRootLogicalFilterComponent().add(filterComponent);

                    boolean nonNullDefaultValue = setFilterComponentDefaultValue(filterComponent, currentConfiguration);
                    if (nonNullDefaultValue) {
                        dataLoadNeeded = true;
                    }
                }

                FilterUtils.setCurrentConfiguration(target, currentConfiguration, true);

                if (dataLoadNeeded) {
                    target.apply();
                }
            }
        };
    }

    protected boolean setFilterComponentDefaultValue(FilterComponent filterComponent,
                                                     Configuration currentConfiguration) {
        boolean dataLoadNeeded = false;
        if (filterComponent instanceof LogicalFilterComponent) {
            for (FilterComponent child : ((LogicalFilterComponent<?>) filterComponent).getOwnFilterComponents()) {
                boolean nonNullDefaultValue = setFilterComponentDefaultValue(child, currentConfiguration);
                if (nonNullDefaultValue) {
                    dataLoadNeeded = true;
                }
            }
        } else if (filterComponent instanceof SingleFilterComponentBase) {
            currentConfiguration.setFilterComponentDefaultValue(
                    ((SingleFilterComponentBase<?>) filterComponent).getParameterName(),
                    ((SingleFilterComponentBase<?>) filterComponent).getValue());

            if (((SingleFilterComponentBase<?>) filterComponent).getValue() != null) {
                dataLoadNeeded = true;
            }
        }

        return dataLoadNeeded;
    }

    protected void openAddConditionView(List<FilterCondition> filterConditions) {
        View<?> origin = UiComponentUtils.findView(target);
        if (origin == null) {
            throw new IllegalStateException(String.format("A component '%s' is not attached to a view",
                    target.getClass().getSimpleName()));
        }

        DialogWindow<AddConditionView> dialog = dialogWindows.lookup(origin, FilterCondition.class)
                .withViewClass(AddConditionView.class)
                .withSelectValidator(selectValidator)
                .withSelectHandler(selectHandler)
                .build();

        AddConditionView addConditionView = dialog.getView();
        addConditionView.setConditions(filterConditions);
        addConditionView.setCurrentFilterConfiguration(target.getCurrentConfiguration());

        dialog.open();
    }
}
