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

package io.jmix.flowui.action.genericfilter;

import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.Messages;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.app.filter.condition.FilterConditionDetailView;
import io.jmix.flowui.app.filter.condition.GroupFilterConditionDetailView;
import io.jmix.flowui.component.genericfilter.Configuration;
import io.jmix.flowui.component.genericfilter.GenericFilterSupport;
import io.jmix.flowui.component.genericfilter.configuration.AbstractConfigurationDetail;
import io.jmix.flowui.component.genericfilter.configuration.DesignTimeConfiguration;
import io.jmix.flowui.component.genericfilter.converter.FilterConverter;
import io.jmix.flowui.component.genericfilter.registration.FilterComponents;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent;
import io.jmix.flowui.entity.filter.LogicalFilterCondition;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.StandardOutcome;
import io.jmix.flowui.view.View;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Objects;

@ActionType(GenericFilterEditAction.ID)
public class GenericFilterEditAction extends GenericFilterAction<GenericFilterEditAction> {

    public static final String ID = "genericFilter_edit";

    protected Messages messages;
    protected FilterComponents filterComponents;
    protected GenericFilterSupport genericFilterSupport;
    protected DialogWindows dialogWindows;

    public GenericFilterEditAction() {
        this(ID);
    }

    public GenericFilterEditAction(String id) {
        super(id);
    }

    @Override
    protected void initAction() {
        super.initAction();

        this.icon = ComponentUtils.convertToIcon(VaadinIcon.PENCIL);
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.genericFilter.Edit");
        this.messages = messages;
    }

    @Autowired
    public void setFilterComponents(FilterComponents filterComponents) {
        this.filterComponents = filterComponents;
    }

    @Autowired
    public void setGenericFilterSupport(GenericFilterSupport genericFilterSupport) {
        this.genericFilterSupport = genericFilterSupport;
    }

    @Autowired
    public void setDialogWindows(DialogWindows dialogWindows) {
        this.dialogWindows = dialogWindows;
    }

    @Override
    protected boolean isApplicable() {
        return super.isApplicable()
                && !(target.getCurrentConfiguration() instanceof DesignTimeConfiguration);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void execute() {
        checkTarget();

        Configuration currentConfiguration = target.getCurrentConfiguration();

        LogicalFilterComponent<?> rootComponent = currentConfiguration.getRootLogicalFilterComponent();
        Class modelClass = filterComponents.getModelClass(rootComponent.getClass());
        FilterConverter converter = filterComponents.getConverterByComponentClass(rootComponent.getClass(), target);
        Map<String, Object> valuesMap = genericFilterSupport.initConfigurationValuesMap(currentConfiguration);
        LogicalFilterCondition model = (LogicalFilterCondition) converter.convertToModel(rootComponent);

        DialogWindow<? extends FilterConditionDetailView<?>> detailDialog = createDetailDialog(modelClass, model);

        boolean isNewConfiguration = isNewConfiguration(currentConfiguration);
        AbstractConfigurationDetail configurationDetail = genericFilterSupport.createFilterConfigurationDetail(
                detailDialog, isNewConfiguration, currentConfiguration);

        detailDialog.getView().getContent().addComponentAsFirst(configurationDetail);

        if (detailDialog.getView() instanceof GroupFilterConditionDetailView) {
            GroupFilterConditionDetailView detailView = (GroupFilterConditionDetailView) detailDialog.getView();

            String title = messages.getMessage(getClass(),
                    "genericFilterEditAction.filterConfigurationDetail.title");
            detailView.setTitle(title);

            String groupConditionTitle = messages.getMessage(getClass(),
                    "genericFilterEditAction.filterConfigurationDetail.groupConditionTitle");
            detailView.setGroupConditionTitle(groupConditionTitle);
        }

        detailDialog.addAfterCloseListener(afterCloseEvent -> {
            if (afterCloseEvent.closedWith(StandardOutcome.SAVE)) {
                LogicalFilterCondition filterCondition = (LogicalFilterCondition) afterCloseEvent.getSource()
                        .getView()
                        .getInstanceContainer()
                        .getItem();

                onDetailViewAfterSave(configurationDetail, filterCondition, converter, isNewConfiguration,
                        currentConfiguration, valuesMap);
            } else {
                genericFilterSupport.resetConfigurationValuesMap(currentConfiguration, valuesMap);
            }
        });

        detailDialog.open();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected DialogWindow createDetailDialog(Class modelClass, LogicalFilterCondition model) {
        View<?> parent = getParentView();
        DialogWindow dialog = dialogWindows.detail(parent, modelClass)
                .withViewId(filterComponents.getDetailViewId(modelClass))
                .editEntity(model)
                .build();

        applyViewConfigurer(dialog.getView());

        return dialog;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void onDetailViewAfterSave(AbstractConfigurationDetail configurationDetail,
                                         LogicalFilterCondition filterCondition,
                                         FilterConverter converter,
                                         boolean isNewConfiguration,
                                         Configuration currentConfiguration,
                                         Map<String, Object> valuesMap) {
        LogicalFilterComponent rootFilterComponent =
                (LogicalFilterComponent) converter.convertToComponent(filterCondition);
        Configuration resultConfiguration = genericFilterSupport.saveCurrentFilterConfiguration(
                currentConfiguration, isNewConfiguration, rootFilterComponent, configurationDetail);

        genericFilterSupport.refreshConfigurationDefaultValues(resultConfiguration);
        resultConfiguration.setModified(false);
        genericFilterSupport.refreshConfigurationValuesMap(resultConfiguration, valuesMap);

        if (isNewConfiguration || !currentConfiguration.getId().equals(resultConfiguration.getId())) {
            target.addConfiguration(resultConfiguration);
        }

        target.setCurrentConfiguration(resultConfiguration);

        if (isNewConfiguration) {
            target.getEmptyConfiguration().getRootLogicalFilterComponent().removeAll();
        }

        target.apply();
    }

    protected void applyViewConfigurer(View<?> detailView) {
        if (detailView instanceof FilterConditionDetailView) {
            ((FilterConditionDetailView<?>) detailView).setCurrentConfiguration(target.getCurrentConfiguration());
        }
    }

    protected boolean isNewConfiguration(Configuration currentConfiguration) {
        return Objects.equals(currentConfiguration.getId(), target.getEmptyConfiguration().getId());
    }
}
