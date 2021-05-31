/*
 * Copyright 2021 Haulmont.
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

package io.jmix.searchui.component;

import com.google.common.base.Strings;
import io.jmix.search.searching.SearchStrategy;
import io.jmix.search.searching.SearchStrategyManager;
import io.jmix.ui.xml.layout.loader.AbstractFieldLoader;
import org.dom4j.Element;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SearchFieldLoader extends AbstractFieldLoader<SearchField> {

    @Override
    public void createComponent() {
        resultComponent = factory.create(SearchField.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        loadStrategy(resultComponent, element);
        loadEntities(resultComponent, element);
    }

    protected void loadStrategy(SearchField component, Element element) {
        String strategyName = element.attributeValue("strategy");
        SearchStrategyManager strategyManager = applicationContext.getBean(SearchStrategyManager.class);
        SearchStrategy strategy;
        if (Strings.isNullOrEmpty(strategyName)) {
            strategy = strategyManager.getDefaultSearchStrategy();
        } else {
            strategy = strategyManager.getSearchStrategyByName(strategyName);
        }
        component.setSearchStrategy(strategy);
    }

    protected void loadEntities(SearchField component, Element element) {
        String entitiesString = element.attributeValue("entities");
        List<String> entities;
        if (Strings.isNullOrEmpty(entitiesString)) {
            entities = Collections.emptyList();
        } else {
            String[] split = entitiesString.split(",");
            entities = Stream.of(split)
                    .map(String::trim)
                    .collect(Collectors.toList());
        }
        component.setEntities(entities);
    }
}
