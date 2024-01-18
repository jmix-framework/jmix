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

package io.jmix.searchflowui.view.filter;

import io.jmix.flowui.app.filter.condition.FilterConditionDetailView;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import io.jmix.search.searching.SearchStrategy;
import io.jmix.search.searching.SearchStrategyManager;
import io.jmix.searchflowui.entity.FullTextFilterCondition;
import io.jmix.searchflowui.utils.FullTextFilterUtils;
import org.elasticsearch.common.Strings;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@ViewController("search_FullTextFilterCondition.detail")
@ViewDescriptor("full-text-filter-condition-detail-view.xml")
@EditedEntityContainer("filterConditionDc")
@DialogMode(width = "40em", resizable = true)
public class FullTextFilterConditionDetailView extends FilterConditionDetailView<FullTextFilterCondition> {

    @ViewComponent
    protected InstanceContainer<FullTextFilterCondition> filterConditionDc;
    @ViewComponent
    protected JmixComboBox<String> searchStrategyNameField;

    @Autowired
    protected MessageBundle messageBundle;
    @Autowired
    protected SearchStrategyManager searchStrategyManager;

    @Override
    public InstanceContainer<FullTextFilterCondition> getInstanceContainer() {
        return filterConditionDc;
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        initSearchStrategyNameItems();
        initLabel();

        generateRandomParameterName();
    }

    @Subscribe("parameterNameValuePicker.generateRandomParameterName")
    protected void onParameterNameValuePickerGenerateActionPerformed(ActionPerformedEvent event) {
        generateRandomParameterName();
    }

    @Subscribe
    protected void onBeforeSave(BeforeSaveEvent event) {
        getEditedEntity().setLocalizedLabel(getEditedEntity().getLabel());
    }

    private void initLabel() {
        if (Strings.isNullOrEmpty(getEditedEntity().getLabel())) {
            getEditedEntity().setLabel(messageBundle.getMessage("defaultLabel"));
        }
    }

    private void initSearchStrategyNameItems() {
        Collection<SearchStrategy> searchStrategies = searchStrategyManager.getAllSearchStrategies();
        List<String> searchStrategyNames = searchStrategies.stream()
                .map(SearchStrategy::getName)
                .collect(Collectors.toList());
        searchStrategyNameField.setItems(searchStrategyNames);
    }

    private void generateRandomParameterName() {
        getEditedEntity().setParameterName(FullTextFilterUtils.generateParameterName());
    }
}
