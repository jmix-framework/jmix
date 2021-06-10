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

package io.jmix.searchui.component.loader;

import io.jmix.search.searching.SearchStrategy;
import io.jmix.search.searching.SearchStrategyManager;
import io.jmix.searchui.component.FullTextFilter;
import io.jmix.searchui.component.fulltextfilter.FullTextFilterUtils;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.TextField;
import io.jmix.ui.xml.layout.loader.AbstractSingleFilterComponentLoader;
import org.dom4j.Element;

public class FullTextFilterLoader extends AbstractSingleFilterComponentLoader<FullTextFilter> {

    @Override
    public void createComponent() {
        resultComponent = factory.create(FullTextFilter.NAME);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();
        loadSearchStrategy(resultComponent, element);
    }

    protected void loadSearchStrategy(FullTextFilter resultComponent, Element element) {
        loadString(element, "searchStrategy")
                .ifPresent(strategyName -> {
                    SearchStrategy strategy = applicationContext.getBean(SearchStrategyManager.class).getSearchStrategyByName(strategyName);
                    resultComponent.setSearchStrategy(strategy);
                });
    }

    @Override
    protected HasValue<?> generateValueComponent() {
        return applicationContext.getBean(UiComponents.class).create(TextField.TYPE_STRING);
    }

    @Override
    protected void loadAttributesBeforeValueComponent() {
        super.loadAttributesBeforeValueComponent();
        resultComponent.setParameterName(loadString(element, "parameterName")
                .orElse(FullTextFilterUtils.generateParameterName()));
    }
}
