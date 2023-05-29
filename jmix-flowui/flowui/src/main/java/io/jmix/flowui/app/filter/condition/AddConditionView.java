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

package io.jmix.flowui.app.filter.condition;

import com.vaadin.flow.router.Route;
import io.jmix.core.LoadContext;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.component.SupportsTypedValue.TypedValueChangeEvent;
import io.jmix.flowui.component.genericfilter.Configuration;
import io.jmix.flowui.component.genericfilter.builder.PropertyConditionBuilder;
import io.jmix.flowui.component.grid.TreeDataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.entity.filter.FilterCondition;
import io.jmix.flowui.entity.filter.HeaderFilterCondition;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Route(value = "add-condition", layout = DefaultMainViewParent.class)
@ViewController("flowui_AddConditionView")
@ViewDescriptor("add-condition-view.xml")
@LookupComponent("filterConditionsTreeDataGrid")
@DialogMode(width = "25em", height = "37.5em")
public class AddConditionView extends StandardListView<FilterCondition> {

    @ViewComponent
    protected TreeDataGrid<FilterCondition> filterConditionsTreeDataGrid;

    @ViewComponent
    protected CollectionLoader<FilterCondition> filterConditionsDl;

    @Autowired
    protected Messages messages;

    protected List<FilterCondition> conditions = new ArrayList<>();
    protected List<FilterCondition> rootConditions = new ArrayList<>();
    protected List<FilterCondition> foundConditions = new ArrayList<>();
    protected HeaderFilterCondition propertiesHeaderCondition;
    protected MetaClass filterMetaClass;

    protected Configuration currentFilterConfiguration;

    public List<FilterCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<FilterCondition> conditions) {
        this.conditions = conditions;
        this.foundConditions = new ArrayList<>(conditions);
        this.rootConditions = searchRootConditions(conditions);
    }

    @Nullable
    protected List<FilterCondition> searchRootConditions(List<FilterCondition> conditions) {
        return conditions.stream()
                .filter(condition -> condition.getParent() == null)
                .collect(Collectors.toList());
    }

    public Configuration getCurrentFilterConfiguration() {
        return currentFilterConfiguration;
    }

    public void setCurrentFilterConfiguration(Configuration currentFilterConfiguration) {
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
    protected void onReady(ReadyEvent event) {
        initFilterConditionsTreeDataGrid();
    }

    protected void initFilterConditionsTreeDataGrid() {
//        filterConditionsTreeDataGrid.collapseAll(); // TODO: gg, implement
        String propertiesCaption =
                messages.getMessage(PropertyConditionBuilder.class, "propertyConditionBuilder.headerCaption");

        propertiesHeaderCondition = getHeaderFilterConditionByCaption(propertiesCaption);
        if (propertiesHeaderCondition != null) {
            filterConditionsTreeDataGrid.expand(propertiesHeaderCondition);
        }
    }

    @Nullable
    protected HeaderFilterCondition getHeaderFilterConditionByCaption(String caption) {
        return conditions.stream()
                .filter(condition -> condition instanceof HeaderFilterCondition
                        && Objects.equals(condition.getLocalizedLabel(), caption))
                .map(condition -> (HeaderFilterCondition) condition)
                .findFirst()
                .orElse(null);
    }

    @Subscribe("conditionFilterField")
    protected void onConditionFilterFieldValueChange(TypedValueChangeEvent<TypedTextField<String>, String> event) {
        search(event.getValue());
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

//            filterConditionsTreeDataGrid.expandTree(); // TODO: gg, replace
        } else {
            foundConditions = new ArrayList<>(conditions);
        }

        filterConditionsDl.load();

        if (loadAllConditions) {
            initFilterConditionsTreeDataGrid();
        }
    }

    protected void findConditionsRecursively(List<FilterCondition> conditions,
                                             String searchValue,
                                             boolean addChildrenAutomatically) {
        for (FilterCondition condition : conditions) {
            boolean conditionFound = addChildrenAutomatically ||
                    StringUtils.containsIgnoreCase(condition.getLocalizedLabel(), searchValue);
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