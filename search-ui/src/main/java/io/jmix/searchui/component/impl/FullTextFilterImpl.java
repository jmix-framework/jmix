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

package io.jmix.searchui.component.impl;

import io.jmix.core.Id;
import io.jmix.core.IdSerialization;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.JpqlCondition;
import io.jmix.search.searching.*;
import io.jmix.searchui.component.FullTextFilter;
import io.jmix.ui.component.impl.AbstractSingleFilterComponent;
import io.jmix.ui.component.jpqlfilter.JpqlFilterSupport;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.model.DataLoader;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.Strings;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;
import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

public class FullTextFilterImpl extends AbstractSingleFilterComponent<String> implements FullTextFilter {

    protected JpqlFilterSupport jpqlFilterSupport;

    protected EntitySearcher entitySearcher;

    protected IdSerialization idSerialization;

    protected String parameterName;

    protected SearchStrategy searchStrategy;

    protected SearchStrategyManager searchStrategyManager;

    protected String correctWhere;

    @Autowired
    public void setJpqlFilterSupport(JpqlFilterSupport jpqlFilterSupport) {
        this.jpqlFilterSupport = jpqlFilterSupport;
    }

    @Autowired
    public void setEntitySearcher(EntitySearcher entitySearcher) {
        this.entitySearcher = entitySearcher;
    }

    @Autowired
    public void setIdSerialization(IdSerialization idSerialization) {
        this.idSerialization = idSerialization;
    }

    @Autowired
    public void setSearchStrategyManager(SearchStrategyManager searchStrategyManager) {
        this.searchStrategyManager = searchStrategyManager;
    }

    @Override
    public void setDataLoader(DataLoader dataLoader) {
        if (!(dataLoader instanceof CollectionLoader)) {
            throw new RuntimeException(FullTextFilter.NAME + " component can only work with CollectionLoader");
        }
        super.setDataLoader(dataLoader);
        registerDataLoaderPreLoadListener((CollectionLoader) dataLoader);
    }

    private void registerDataLoaderPreLoadListener(CollectionLoader dataLoader) {
        ((CollectionLoader<?>) dataLoader).addPreLoadListener(preLoadEvent -> {
            String value = valueComponent.getValue();
            if (value != null && !"".equals(value)) {
                List<Id> ids = performFullTextSearch(value);
                List<Object> idValues = ids.stream()
                        .map(Id::getValue)
                        .collect(Collectors.toList());
                if (idValues.isEmpty()) {
                    //if no data is returned by full-test search then we must set the condition that is always false
                    enableAlwaysFalseWhereClause();
                    clearConditionParameterValuesMap();
                } else {
                    enableCorrectWhereClause();
                    setQueryConditionParameterValue(idValues);
                }
            }
        });
    }

    private void setQueryConditionParameterValue(List<Object> value) {
        getQueryCondition().setParameterValuesMap(Collections.singletonMap(parameterName, value));
    }

    private void clearConditionParameterValuesMap() {
        getQueryCondition().setParameterValuesMap(Collections.emptyMap());
    }

    /**
     * When no data is returned by full-text search we must make the condition return false. We set invalid where
     * clause for that purpose.
     */
    private void enableAlwaysFalseWhereClause() {
        getQueryCondition().setWhere("1 <> 1");
    }

    private void enableCorrectWhereClause() {
        getQueryCondition().setWhere(correctWhere);
    }

    @Override
    protected Condition createQueryCondition() {
        JpqlCondition fullTextCondition = new JpqlCondition();
        this.correctWhere = "{E}.id in ?";
        fullTextCondition.setWhere(correctWhere);
        fullTextCondition.setJoin("");
        return fullTextCondition;
    }

    @Override
    public String getInnerComponentPrefix() {
        return jpqlFilterSupport.getJpqlFilterPrefix(getId());
    }

    @Override
    protected void updateQueryCondition(@Nullable String newValue) {
        if (Strings.isNullOrEmpty(newValue)) {
            setQueryConditionParameterValue(Collections.emptyList());
        }
    }

    private List<Id> performFullTextSearch(String searchTerm) {
        SearchContext searchContext = new SearchContext(searchTerm);
        //todo limit size?
//        searchContext.setSize(getDataLoader().getMaxResults());
        searchContext.setEntities(getDataLoader().getContainer().getEntityMetaClass().getName());
        SearchResult searchResult = entitySearcher.search(searchContext,
                searchStrategy != null ? searchStrategy : searchStrategyManager.getDefaultSearchStrategy());
        List<Id> ids = searchResult.getAllEntries().stream()
                .map(searchResultEntry -> {
                    String docId = searchResultEntry.getDocId();
                    return idSerialization.stringToId(docId);
                })
                .collect(Collectors.toList());
        return ids;
    }

    @Override
    public JpqlCondition getQueryCondition() {
        return (JpqlCondition) queryCondition;
    }

    @Override
    public CollectionLoader getDataLoader() {
        return (CollectionLoader) super.getDataLoader();
    }

    @Override
    public String getParameterName() {
        return parameterName;
    }

    @Override
    public void setParameterName(String parameterName) {
        checkState(this.parameterName == null, "Parameter name has already been initialized");
        checkNotNullArgument(parameterName);
        String where = getQueryCondition().getWhere();
        if (StringUtils.isNotEmpty(where)) {
            correctWhere = where.replace("?", ":" + parameterName);
            getQueryCondition().setWhere(correctWhere);
        }
        this.parameterName = parameterName;
    }

    @Override
    public SearchStrategy getSearchStrategy() {
        return searchStrategy;
    }

    @Override
    public void setSearchStrategy(SearchStrategy searchStrategy) {
        this.searchStrategy = searchStrategy;
    }
}