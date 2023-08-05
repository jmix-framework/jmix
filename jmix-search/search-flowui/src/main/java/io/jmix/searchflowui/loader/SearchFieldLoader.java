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

package io.jmix.searchflowui.loader;

import com.google.common.base.Strings;
import io.jmix.flowui.view.OpenMode;
import io.jmix.flowui.xml.layout.loader.component.TextFieldLoader;
import io.jmix.search.searching.SearchStrategy;
import io.jmix.search.searching.SearchStrategyManager;
import io.jmix.searchflowui.component.SearchField;
import org.dom4j.Element;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SearchFieldLoader extends TextFieldLoader {


    @Override
    public void loadComponent() {
        super.loadComponent();
        loadEntities((SearchField) resultComponent, element);
        loadStrategy((SearchField) resultComponent, element);
        loadOpenMode((SearchField) resultComponent, element);
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

    protected void loadOpenMode(SearchField component, Element element) {
        String openModeName = element.attributeValue("openMode");
        OpenMode openMode;
        if (Strings.isNullOrEmpty(openModeName)) {
            openMode = OpenMode.NAVIGATION;
        } else {
            openMode = OpenMode.valueOf(openModeName);
        }
        component.setOpenMode(openMode);
    }

    @Override
    protected SearchField createComponent() {
        resultComponent = factory.create(SearchField.class);
        loadId(resultComponent, element);
        return (SearchField) resultComponent;
    }
}
