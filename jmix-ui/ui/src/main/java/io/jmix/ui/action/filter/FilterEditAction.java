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
import io.jmix.ui.Fragments;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.app.filter.condition.FilterConditionEdit;
import io.jmix.ui.app.filter.condition.GroupFilterConditionEdit;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Filter;
import io.jmix.ui.component.Fragment;
import io.jmix.ui.component.LogicalFilterComponent;
import io.jmix.ui.component.filter.FilterSupport;
import io.jmix.ui.component.filter.configuration.DesignTimeConfiguration;
import io.jmix.ui.component.filter.converter.FilterConverter;
import io.jmix.ui.component.filter.registration.FilterComponents;
import io.jmix.ui.entity.LogicalFilterCondition;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.meta.StudioAction;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.screen.StandardOutcome;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Objects;

@StudioAction(target = "io.jmix.ui.component.Filter", description = "Edits current run-time filter configuration")
@ActionType(FilterEditAction.ID)
public class FilterEditAction extends FilterAction {

    public static final String ID = "filter_edit";

    protected FilterComponents filterComponents;
    protected FilterSupport filterSupport;
    protected ScreenBuilders screenBuilders;
    protected Fragments fragments;
    protected Messages messages;

    public FilterEditAction() {
        this(ID);
    }

    public FilterEditAction(String id) {
        super(id);
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.caption = messages.getMessage("actions.Filter.Edit");
        this.messages = messages;
    }

    @Autowired
    protected void setIcons(Icons icons) {
        this.icon = icons.get(JmixIcon.PENCIL);
    }

    @Autowired
    public void setScreenBuilders(ScreenBuilders screenBuilders) {
        this.screenBuilders = screenBuilders;
    }

    @Autowired
    public void setFilterComponents(FilterComponents filterComponents) {
        this.filterComponents = filterComponents;
    }

    @Autowired
    public void setFilterSupport(FilterSupport filterSupport) {
        this.filterSupport = filterSupport;
    }

    @Autowired
    public void setFragments(Fragments fragments) {
        this.fragments = fragments;
    }

    @Override
    protected boolean isApplicable() {
        return super.isApplicable()
                && !(filter.getCurrentConfiguration() instanceof DesignTimeConfiguration);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void execute() {
        if (filter.getFrame() == null) {
            throw new IllegalStateException("Filter component is not attached to the Frame");
        }

        Filter.Configuration currentConfiguration = filter.getCurrentConfiguration();
        boolean isNewConfiguration = Objects.equals(currentConfiguration.getId(),
                filter.getEmptyConfiguration().getId());

        LogicalFilterComponent rootComponent = currentConfiguration.getRootLogicalFilterComponent();
        Class modelClass = filterComponents.getModelClass(rootComponent.getClass());
        FilterConverter converter = filterComponents.getConverterByComponentClass(rootComponent.getClass(), filter);
        Map<String, Object> valuesMap = filterSupport.initConfigurationValuesMap(currentConfiguration);
        LogicalFilterCondition model = (LogicalFilterCondition) converter.convertToModel(rootComponent);

        Screen editScreen = createEditScreen(modelClass, model);
        applyScreenConfigurer(editScreen);

        ScreenFragment screenFragment = filterSupport.createFilterConfigurationFragment(
                editScreen.getWindow().getFrameOwner(), isNewConfiguration, currentConfiguration);
        Fragment fragment = screenFragment.getFragment();
        fragment.setWidthFull();
        editScreen.getWindow().add(fragment, 0);
        editScreen.getWindow().setCaption(messages.getMessage(FilterEditAction.class, "configurationEdit.caption"));
        if (editScreen instanceof GroupFilterConditionEdit) {
            Component groupConditionBox = editScreen.getWindow().getComponent("groupConditionBox");
            if (groupConditionBox instanceof Component.HasCaption) {
                ((Component.HasCaption) groupConditionBox).setCaption(
                        messages.getMessage(FilterEditAction.class, "configurationEdit.rootGroupCondition"));
            }
        }

        editScreen.addAfterCloseListener(afterCloseEvent -> {
            if (afterCloseEvent.closedWith(StandardOutcome.COMMIT)) {
                LogicalFilterCondition filterCondition =
                        (LogicalFilterCondition) ((FilterConditionEdit) afterCloseEvent.getSource())
                                .getInstanceContainer()
                                .getItem();

                onEditScreenAfterCommit(screenFragment, filterCondition, converter, isNewConfiguration,
                        currentConfiguration, valuesMap);
            } else {
                filterSupport.resetConfigurationValuesMap(currentConfiguration, valuesMap);
            }
        });

        editScreen.show();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Screen createEditScreen(Class modelClass, LogicalFilterCondition model) {
        return screenBuilders.editor(modelClass, filter.getFrame().getFrameOwner())
                .withScreenId(filterComponents.getEditScreenId(modelClass))
                .withOpenMode(OpenMode.DIALOG)
                .editEntity(model)
                .build();
    }

    protected void applyScreenConfigurer(Screen editScreen) {
        if (editScreen instanceof FilterConditionEdit) {
            ((FilterConditionEdit<?>) editScreen).setCurrentConfiguration(filter.getCurrentConfiguration());
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void onEditScreenAfterCommit(ScreenFragment configurationFormFragment,
                                           LogicalFilterCondition filterCondition,
                                           FilterConverter converter,
                                           boolean isNewConfiguration,
                                           Filter.Configuration currentConfiguration,
                                           Map<String, Object> valuesMap) {
        LogicalFilterComponent rootFilterComponent =
                (LogicalFilterComponent) converter.convertToComponent(filterCondition);
        Filter.Configuration resultConfiguration = filterSupport.saveCurrentFilterConfiguration(
                currentConfiguration, isNewConfiguration, rootFilterComponent, configurationFormFragment);

        filterSupport.refreshConfigurationDefaultValues(resultConfiguration);
        resultConfiguration.setModified(false);
        filterSupport.refreshConfigurationValuesMap(resultConfiguration, valuesMap);

        if (isNewConfiguration || !currentConfiguration.getId().equals(resultConfiguration.getId())) {
            filter.addConfiguration(resultConfiguration);
        }

        filter.setCurrentConfiguration(resultConfiguration);

        if (isNewConfiguration) {
            filter.getEmptyConfiguration().getRootLogicalFilterComponent().removeAll();
        }

        filter.apply();
    }
}
