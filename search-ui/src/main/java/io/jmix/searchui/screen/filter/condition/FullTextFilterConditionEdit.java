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

package io.jmix.searchui.screen.filter.condition;

import io.jmix.search.searching.SearchStrategy;
import io.jmix.search.searching.SearchStrategyManager;
import io.jmix.searchui.component.fulltextfilter.FullTextFilterUtils;
import io.jmix.searchui.entity.FullTextFilterCondition;
import io.jmix.ui.app.filter.condition.FilterConditionEdit;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.Filter;
import io.jmix.ui.component.data.options.ListOptions;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An editor that is displayed when a FullTextCondition is added to the {@link Filter} component.
 */
@UiController("search_FullTextFilterCondition.edit")
@UiDescriptor("full-text-filter-condition-edit.xml.xml")
@EditedEntityContainer("filterConditionDc")
public class FullTextFilterConditionEdit extends FilterConditionEdit<FullTextFilterCondition> {

    @Autowired
    protected InstanceContainer<FullTextFilterCondition> filterConditionDc;

    @Autowired
    private MessageBundle messageBundle;

    @Autowired
    private ComboBox<String> searchStrategyNameField;

    @Autowired
    private SearchStrategyManager searchStrategyManager;

    @Override
    public InstanceContainer<FullTextFilterCondition> getInstanceContainer() {
        return filterConditionDc;
    }

    @Subscribe
    public void onInitEntity(InitEntityEvent<FullTextFilterCondition> event) {
        FullTextFilterCondition condition = event.getEntity();
        condition.setParameterName(FullTextFilterUtils.generateParameterName());
        condition.setCaption(messageBundle.getMessage("defaultCaption"));
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        Collection<SearchStrategy> searchStrategies = searchStrategyManager.getAllSearchStrategies();
        List<String> searchStrategyNames = searchStrategies.stream()
                .map(SearchStrategy::getName)
                .collect(Collectors.toList());
        searchStrategyNameField.setOptions(new ListOptions<>(searchStrategyNames));
    }
}