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

package io.jmix.ui.app.filter.condition;

import com.google.common.collect.Lists;
import io.jmix.core.AccessManager;
import io.jmix.core.LoadContext;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.accesscontext.UiFilterModifyJpqlConditionContext;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.component.Filter;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.PopupButton;
import io.jmix.ui.component.TextField;
import io.jmix.ui.component.Tree;
import io.jmix.ui.component.filter.builder.PropertyConditionBuilder;
import io.jmix.ui.component.filter.registration.FilterComponents;
import io.jmix.ui.component.groupfilter.LogicalFilterSupport;
import io.jmix.ui.component.propertyfilter.PropertyFilterSupport;
import io.jmix.ui.entity.AbstractSingleFilterCondition;
import io.jmix.ui.entity.FilterCondition;
import io.jmix.ui.entity.FilterValueComponent;
import io.jmix.ui.entity.HeaderFilterCondition;
import io.jmix.ui.entity.JpqlFilterCondition;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.Install;
import io.jmix.ui.screen.LookupComponent;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.StandardLookup;
import io.jmix.ui.screen.StandardOutcome;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.Target;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@SuppressWarnings({"rawtypes"})
@UiController("ui_AddConditionScreen")
@UiDescriptor("add-condition-screen.xml")
@LookupComponent("filterConditionsTree")
public class AddConditionScreen extends StandardLookup<FilterCondition> {

    @Autowired
    protected Messages messages;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected ScreenBuilders screenBuilders;
    @Autowired
    protected FilterComponents filterComponents;
    @Autowired
    protected AccessManager accessManager;
    @Autowired
    protected PropertyFilterSupport propertyFilterSupport;
    @Autowired
    protected LogicalFilterSupport logicalFilterSupport;

    @Autowired
    protected CollectionLoader<FilterCondition> filterConditionsDl;

    @Autowired
    protected Tree<FilterCondition> filterConditionsTree;
    @Autowired
    protected PopupButton createPopupButton;
    @Autowired
    protected TextField<String> conditionCaptionFilterField;

    protected List<FilterCondition> conditions = new ArrayList<>();
    protected List<FilterCondition> rootConditions = new ArrayList<>();
    protected List<FilterCondition> foundConditions = new ArrayList<>();
    protected MetaClass filterMetaClass;
    protected HeaderFilterCondition propertiesHeaderCondition;

    protected Filter.Configuration currentFilterConfiguration;

    public List<FilterCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<FilterCondition> conditions) {
        this.conditions = conditions;
        this.foundConditions = new ArrayList<>(conditions);
        this.rootConditions = searchRootConditions(conditions);
    }

    public Filter.Configuration getCurrentFilterConfiguration() {
        return currentFilterConfiguration;
    }

    public void setCurrentFilterConfiguration(Filter.Configuration currentFilterConfiguration) {
        this.currentFilterConfiguration = currentFilterConfiguration;
        this.filterMetaClass = currentFilterConfiguration.getOwner()
                .getDataLoader()
                .getContainer()
                .getEntityMetaClass();
    }

    @Install(to = "filterConditionsDl", target = Target.DATA_LOADER)
    protected List<FilterCondition> filterConditionsDlLoadDelegate(LoadContext<FilterCondition> loadContext) {
        return foundConditions;
    }

    @Subscribe
    protected void onAfterShow(AfterShowEvent event) {
        initFilterConditionsTree();
        initCreatePopupButton();
    }

    protected void initFilterConditionsTree() {
        filterConditionsTree.collapseTree();
        String propertiesCaption =
                messages.getMessage(PropertyConditionBuilder.class, "propertyConditionBuilder.headerCaption");

        propertiesHeaderCondition = getHeaderFilterConditionByCaption(propertiesCaption);
        if (propertiesHeaderCondition != null) {
            filterConditionsTree.expand(propertiesHeaderCondition);
        }
    }

    protected void initCreatePopupButton() {
        for (Class<? extends FilterCondition> modelClass : filterComponents.getRegisteredModelClasses()) {
            try {
                if (JpqlFilterCondition.class.isAssignableFrom(modelClass)) {
                    UiFilterModifyJpqlConditionContext jpqlConditionsContext = new UiFilterModifyJpqlConditionContext();
                    accessManager.applyRegisteredConstraints(jpqlConditionsContext);
                    if (!jpqlConditionsContext.isPermitted()) {
                        continue;
                    }
                }

                String editScreenId = filterComponents.getEditScreenId(modelClass);
                Action popupAction = createPopupAction(editScreenId, modelClass);
                createPopupButton.addAction(popupAction);
            } catch (IllegalArgumentException e) {
                return;
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected Action createPopupAction(String editScreenId, Class modelClass) {
        MetaClass metaClass = metadata.getClass(modelClass);

        return new BaseAction("filter_create_" + editScreenId)
                .withCaption(messages.formatMessage(AddConditionScreen.class,
                        "addConditionScreen.createPopupAction",
                        messageTools.getEntityCaption(metaClass)))
                .withHandler(actionPerformedEvent -> {
                    Screen editScreen = screenBuilders.editor(modelClass, getWindow().getFrameOwner())
                            .withScreenId(editScreenId)
                            .newEntity(createFilterCondition(modelClass))
                            .build();

                    applyScreenConfigurer(editScreen);
                    editScreen.addAfterCloseListener(this::onEditScreenAfterCommit);

                    editScreen.show();
                });
    }

    protected void applyScreenConfigurer(Screen editScreen) {
        if (editScreen instanceof FilterConditionEdit) {
            ((FilterConditionEdit<?>) editScreen).setCurrentConfiguration(getCurrentFilterConfiguration());
        }
    }

    protected void onEditScreenAfterCommit(AfterCloseEvent afterCloseEvent) {
        if (afterCloseEvent.closedWith(StandardOutcome.COMMIT)) {
            FilterCondition filterCondition =
                    (FilterCondition) ((FilterConditionEdit) afterCloseEvent.getSource())
                            .getInstanceContainer()
                            .getItem();
            select(Lists.newArrayList(filterCondition));
        }
    }

    protected FilterCondition createFilterCondition(Class<? extends FilterCondition> modelClass) {
        FilterCondition newCondition = metadata.create(modelClass);
        if (newCondition instanceof AbstractSingleFilterCondition) {
            FilterValueComponent filterValueComponent = metadata.create(FilterValueComponent.class);
            ((AbstractSingleFilterCondition) newCondition).setValueComponent(filterValueComponent);
        }
        return newCondition;
    }

    @Nullable
    protected HeaderFilterCondition getHeaderFilterConditionByCaption(String caption) {
        return conditions.stream()
                .filter(condition -> condition instanceof HeaderFilterCondition
                        && Objects.equals(condition.getLocalizedCaption(), caption))
                .map(condition -> (HeaderFilterCondition) condition)
                .findFirst()
                .orElse(null);
    }

    @Subscribe("conditionCaptionFilterField")
    protected void onConditionCaptionFilterFieldValueChange(HasValue.ValueChangeEvent<String> event) {
        search(event.getValue());
    }

    @Nullable
    protected List<FilterCondition> searchRootConditions(List<FilterCondition> conditions) {
        return conditions.stream()
                .filter(condition -> condition.getParent() == null)
                .collect(Collectors.toList());
    }

    protected void search(@Nullable String searchValue) {
        foundConditions.clear();

        boolean loadAllConditions = StringUtils.isEmpty(searchValue) || rootConditions == null;
        if (!loadAllConditions) {
            findConditionsRecursively(rootConditions, searchValue, false);

            List<FilterCondition> exactlyFoundConditions = new ArrayList<>(foundConditions);
            for (FilterCondition condition : exactlyFoundConditions) {
                addParentToExpand(condition);
            }
            filterConditionsTree.expandTree();
        } else {
            foundConditions = new ArrayList<>(conditions);
        }

        filterConditionsDl.load();

        if (loadAllConditions) {
            initFilterConditionsTree();
        }
    }

    protected void findConditionsRecursively(List<FilterCondition> conditions,
                                             String searchValue,
                                             boolean addChildrenAutomatically) {
        for (FilterCondition condition : conditions) {
            boolean conditionFound = addChildrenAutomatically ||
                    StringUtils.containsIgnoreCase(condition.getLocalizedCaption(), searchValue);
            if (conditionFound) {
                foundConditions.add(condition);
            }

            List<FilterCondition> children = searchChildren(condition);
            if (!children.isEmpty()) {
                findConditionsRecursively(children, searchValue, conditionFound);
            }
        }
    }

    protected List<FilterCondition> searchChildren(FilterCondition condition) {
        return conditions.stream()
                .filter(child -> Objects.equals(child.getParent(), condition))
                .collect(Collectors.toList());
    }

    protected void addParentToExpand(FilterCondition child) {
        FilterCondition parent = child.getParent();
        if (parent != null) {
            if (!foundConditions.contains(parent)) {
                foundConditions.add(parent);
            }
            addParentToExpand(parent);
        }
    }
}
