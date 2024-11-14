/*
 * Copyright 2024 Haulmont.
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

package io.jmix.searchflowui.view.settings;


import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.Route;
import io.jmix.core.Messages;
import io.jmix.flowui.component.multiselectcombobox.JmixMultiSelectComboBox;
import io.jmix.flowui.component.textfield.JmixIntegerField;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.view.*;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.searching.SearchStrategy;
import io.jmix.search.searching.SearchStrategyProvider;
import io.jmix.searchflowui.utils.SearchStrategyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "search-field-settings-view", layout = DefaultMainViewParent.class)
@ViewController("SearchFieldSettingsView")
@ViewDescriptor("search-field-settings-view.xml")
@DialogMode(width = "30em")
public class SearchFieldSettingsView extends StandardView {

    @ViewComponent
    protected JmixMultiSelectComboBox<String> searchEntitiesComboBox;
    @ViewComponent
    protected Select<String> searchStrategySelector;
    @ViewComponent
    protected JmixIntegerField searchSizeField;

    @Autowired
    protected SearchStrategyProvider<? extends SearchStrategy> searchStrategyProvider;
    @Autowired
    protected IndexConfigurationManager indexConfigurationManager;
    @Autowired
    protected Messages messages;
    @Autowired
    protected SearchStrategyUtils searchStrategyUtils;

    protected List<String> availableSearchStrategyNames = new ArrayList<>();

    @Subscribe
    public void onInit(final InitEvent event) {
        Collection<? extends SearchStrategy> searchStrategies = searchStrategyProvider.getAllSearchStrategies();
        availableSearchStrategyNames = searchStrategies.stream()
                .filter(this::isSearchStrategyVisible)
                .map(SearchStrategy::getName)
                .collect(Collectors.toList());
        searchStrategySelector.setItems(availableSearchStrategyNames);

        List<String> allIndexedEntities = indexConfigurationManager.getAllIndexedEntities().stream().toList();
        searchEntitiesComboBox.setItems(allIndexedEntities);
        searchEntitiesComboBox.setPlaceholder(messages.getMessage(getClass(), "searchFieldSettingsView.entitiesPlaceholder"));
    }

    public void initSettingsValues(String initialSearchStrategy,
                                   int initialSearchSize,
                                   List<String> initialSearchEntities) {
        this.searchStrategySelector.setValue(initialSearchStrategy);
        this.searchSizeField.setValue(initialSearchSize);

        List<String> effectiveInitialSearchEntities = initialSearchEntities.stream()
                .filter(entityName -> indexConfigurationManager.getIndexConfigurationByEntityNameOpt(entityName).isPresent())
                .toList();
        this.searchEntitiesComboBox.setValue(effectiveInitialSearchEntities);
    }

    public String getSelectedSearchStrategy() {
        return searchStrategySelector.getValue();
    }

    public int getSelectedSearchSize() {
        return searchSizeField.getValue();
    }

    public List<String> getSelectedSearchEntities() {
        return new ArrayList<>(searchEntitiesComboBox.getValue());
    }

    @Install(to = "searchStrategySelector", subject = "itemLabelGenerator")
    private String searchStrategySelectorItemLabelGenerator(final String item) {
        return getLocalizedStrategyName(item);
    }

    @Subscribe("saveAction")
    public void onSaveAction(final ActionPerformedEvent event) {
        close(StandardOutcome.SAVE);
    }

    @Subscribe("closeAction")
    public void onCloseAction(final ActionPerformedEvent event) {
        close(StandardOutcome.DISCARD);
    }

    protected String getLocalizedStrategyName(String strategyName) {
        return searchStrategyUtils.getLocalizedStrategyName(strategyName);
    }

    protected boolean isSearchStrategyVisible(SearchStrategy searchStrategy) {
        return searchStrategyUtils.isSearchStrategyVisible(searchStrategy);
    }
}