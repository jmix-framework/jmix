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

package io.jmix.searchui.component.fulltextfilter;

import io.jmix.core.Metadata;
import io.jmix.core.annotation.Internal;
import io.jmix.search.searching.SearchStrategy;
import io.jmix.search.searching.SearchStrategyManager;
import io.jmix.searchui.component.FullTextFilter;
import io.jmix.searchui.entity.FullTextFilterCondition;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.Filter;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.TextField;
import io.jmix.ui.component.filter.converter.AbstractFilterComponentConverter;
import org.elasticsearch.common.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@Internal
@Component("search_FullTextFilterConverter")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FullTextFilterConverter extends AbstractFilterComponentConverter<FullTextFilter, FullTextFilterCondition> {

    @Autowired
    protected UiComponents uiComponents;

    @Autowired
    protected Metadata metadata;

    @Autowired
    private SearchStrategyManager searchStrategyManager;

    public FullTextFilterConverter(Filter filter) {
        super(filter);
    }

    @Override
    protected FullTextFilter createComponent() {
        return uiComponents.create(FullTextFilter.class);
    }

    @Override
    protected FullTextFilterCondition createModel() {
        return metadata.create(FullTextFilterCondition.class);
    }

    @Nullable
    @Override
    protected String getLocalizedModelCaption(FullTextFilter component) {
        return component.getCaption();
    }

    @Override
    public FullTextFilter convertToComponent(FullTextFilterCondition model) {
        FullTextFilter fullTextFilter = super.convertToComponent(model);
        fullTextFilter.setCaption(model.getCaption());
        fullTextFilter.setCaptionPosition(model.getCaptionPosition());
        fullTextFilter.setRequired(model.getRequired());
        fullTextFilter.setParameterName(model.getParameterName());
        String searchStrategyName = model.getSearchStrategyName();
        SearchStrategy searchStrategy = !Strings.isNullOrEmpty(searchStrategyName) ?
                searchStrategyManager.findSearchStrategyByName(searchStrategyName) :
                null;
        fullTextFilter.setSearchStrategy(searchStrategy);
        HasValue<String> valueComponent = uiComponents.create(TextField.TYPE_STRING);
        fullTextFilter.setValueComponent(valueComponent);
        return fullTextFilter;
    }

    @Override
    public FullTextFilterCondition convertToModel(FullTextFilter fullTextFilter) {
        FullTextFilterCondition condition = super.convertToModel(fullTextFilter);
        condition.setCaption(fullTextFilter.getCaption());
        condition.setLocalizedCaption(getLocalizedModelCaption(fullTextFilter));
        condition.setCaptionPosition(fullTextFilter.getCaptionPosition());
        condition.setRequired(fullTextFilter.isRequired());
        condition.setParameterName(fullTextFilter.getParameterName());
        if (fullTextFilter.getSearchStrategy() != null) {
            condition.setSearchStrategyName(fullTextFilter.getSearchStrategy().getName());
        }
        return condition;
    }
}
