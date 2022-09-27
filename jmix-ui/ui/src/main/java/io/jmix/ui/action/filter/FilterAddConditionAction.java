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

package io.jmix.ui.action.filter;

import io.jmix.core.Messages;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.app.filter.condition.AddConditionScreen;
import io.jmix.ui.component.Filter;
import io.jmix.ui.component.FilterComponent;
import io.jmix.ui.component.LogicalFilterComponent;
import io.jmix.ui.component.SingleFilterComponent;
import io.jmix.ui.component.filter.builder.FilterConditionsBuilder;
import io.jmix.ui.component.filter.configuration.DesignTimeConfiguration;
import io.jmix.ui.component.filter.converter.FilterConverter;
import io.jmix.ui.component.filter.registration.FilterComponents;
import io.jmix.ui.entity.FilterCondition;
import io.jmix.ui.entity.HeaderFilterCondition;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.meta.StudioAction;
import io.jmix.ui.screen.LookupScreen;
import io.jmix.ui.screen.OpenMode;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

@StudioAction(target = "io.jmix.ui.component.Filter", description = "Adds condition to current filter configuration")
@ActionType(FilterAddConditionAction.ID)
public class FilterAddConditionAction extends FilterAction implements Action.AdjustWhenScreenReadOnly {

    public static final String ID = "filter_addCondition";

    protected FilterComponents filterComponents;
    protected FilterConditionsBuilder builder;
    protected Messages messages;
    protected Notifications notifications;
    protected ScreenBuilders screenBuilders;

    protected Predicate<LookupScreen.ValidationContext<FilterCondition>> selectValidator;
    protected Consumer<Collection<FilterCondition>> selectHandler;

    public FilterAddConditionAction() {
        this(ID);
    }

    public FilterAddConditionAction(String id) {
        super(id);

        initDefaultSelectValidator();
        initDefaultSelectHandler();
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.caption = messages.getMessage("actions.Filter.AddCondition");
        this.messages = messages;
    }

    @Autowired
    protected void setIcons(Icons icons) {
        this.icon = icons.get(JmixIcon.ADD_ACTION);
    }

    @Autowired
    public void setFilterComponents(FilterComponents filterComponents) {
        this.filterComponents = filterComponents;
    }

    @Autowired
    public void setFilterConditionsBuilder(FilterConditionsBuilder builder) {
        this.builder = builder;
    }

    @Autowired
    public void setNotifications(Notifications notifications) {
        this.notifications = notifications;
    }

    @Autowired
    public void setScreenBuilders(ScreenBuilders screenBuilders) {
        this.screenBuilders = screenBuilders;
    }

    public void setSelectValidator(Predicate<LookupScreen.ValidationContext<FilterCondition>> selectValidator) {
        this.selectValidator = selectValidator;
    }

    public void setSelectHandler(@Nullable Consumer<Collection<FilterCondition>> selectHandler) {
        this.selectHandler = selectHandler;
    }

    @Override
    protected boolean isApplicable() {
        return super.isApplicable()
                && !(filter.getCurrentConfiguration() instanceof DesignTimeConfiguration);
    }

    @Override
    public void execute() {
        List<FilterCondition> allConditions = builder.buildConditions(filter);
        openAddConditionScreen(allConditions);
    }

    protected void initDefaultSelectValidator() {
        selectValidator = context -> {
            for (FilterCondition selectedCondition : context.getSelectedItems()) {
                if (selectedCondition instanceof HeaderFilterCondition) {
                    String caption = String.format(
                            messages.getMessage(FilterAddConditionAction.class,
                                    "addConditionAction.invalidCondition"),
                            selectedCondition.getLocalizedCaption());

                    notifications.create(Notifications.NotificationType.WARNING)
                            .withCaption(caption)
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
                Filter.Configuration currentConfiguration = filter.getCurrentConfiguration();

                boolean dataLoadNeeded = false;
                for (FilterCondition selectedCondition : selectedConditions) {
                    FilterConverter converter = filterComponents.getConverterByModelClass(
                            selectedCondition.getClass(), filter);

                    FilterCondition parent = selectedCondition.getParent();
                    if (parent instanceof HeaderFilterCondition) {
                        selectedCondition.setParent(null);
                    }

                    FilterComponent filterComponent = converter.convertToComponent(selectedCondition);
                    currentConfiguration.getRootLogicalFilterComponent().add(filterComponent);
                    currentConfiguration.setFilterComponentModified(filterComponent, true);

                    boolean nonNullDefaultValue = setFilterComponentDefaultValue(filterComponent, currentConfiguration);
                    if (nonNullDefaultValue) {
                        dataLoadNeeded = true;
                    }
                }

                filter.setCurrentConfiguration(currentConfiguration);

                if (dataLoadNeeded) {
                    filter.apply();
                }
            }
        };
    }

    protected boolean setFilterComponentDefaultValue(FilterComponent filterComponent,
                                                     Filter.Configuration currentConfiguration) {
        boolean dataLoadNeeded = false;
        if (filterComponent instanceof LogicalFilterComponent) {
            for (FilterComponent childComponent : ((LogicalFilterComponent) filterComponent).getOwnFilterComponents()) {
                boolean nonNullDefaultValue = setFilterComponentDefaultValue(childComponent, currentConfiguration);
                if (nonNullDefaultValue) {
                    dataLoadNeeded = true;
                }
            }
        } else if (filterComponent instanceof SingleFilterComponent) {
            currentConfiguration.setFilterComponentDefaultValue(
                    ((SingleFilterComponent<?>) filterComponent).getParameterName(),
                    ((SingleFilterComponent<?>) filterComponent).getValue());

            if (((SingleFilterComponent<?>) filterComponent).getValue() != null) {
                dataLoadNeeded = true;
            }
        }

        return dataLoadNeeded;
    }

    protected void openAddConditionScreen(List<FilterCondition> filterConditions) {
        if (filter.getFrame() == null) {
            throw new IllegalStateException("Filter component is not attached to the Frame");
        }

        AddConditionScreen addConditionScreen = screenBuilders.lookup(FilterCondition.class,
                filter.getFrame().getFrameOwner())
                .withOpenMode(OpenMode.DIALOG)
                .withScreenClass(AddConditionScreen.class)
                .withSelectValidator(selectValidator)
                .withSelectHandler(selectHandler)
                .build();

        addConditionScreen.setConditions(filterConditions);
        addConditionScreen.setCurrentFilterConfiguration(filter.getCurrentConfiguration());

        addConditionScreen.show();
    }
}
