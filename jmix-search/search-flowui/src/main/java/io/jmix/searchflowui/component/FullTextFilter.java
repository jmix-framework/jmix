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

package io.jmix.searchflowui.component;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.jmix.core.Id;
import io.jmix.core.IdSerialization;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.JpqlCondition;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.flowui.component.filter.SingleFilterComponentBase;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.DataLoader;
import io.jmix.search.SearchProperties;
import io.jmix.search.searching.EntitySearcher;
import io.jmix.search.searching.SearchContext;
import io.jmix.search.searching.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;
import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

public class FullTextFilter extends SingleFilterComponentBase<String> {
    public static final String NAME = "fullTextFilter";
    protected static final String FULL_TEXT_FILTER_STYLENAME = "jmix-full-text-filter";
    protected IdSerialization idSerialization;
    protected EntitySearcher entitySearcher;
    protected String parameterName;
    protected String searchStrategy;
    protected SearchProperties searchProperties;
    protected MetadataTools metadataTools;
    protected String correctWhere;

    @Override
    protected void autowireDependencies() {
        super.autowireDependencies();
        idSerialization = applicationContext.getBean(IdSerialization.class);
        entitySearcher = applicationContext.getBean(EntitySearcher.class);
        searchProperties = applicationContext.getBean(SearchProperties.class);
        metadataTools = applicationContext.getBean(MetadataTools.class);
    }

    @Override
    protected void initRootComponent(HorizontalLayout root) {
        super.initRootComponent(root);

        root.addClassName(FULL_TEXT_FILTER_STYLENAME);
    }

    @Override
    public String getParameterName() {
        return parameterName;
    }

    @Override
    public void setParameterName(String parameterName) {
        checkState(this.parameterName == null, "Parameter name has already been initialized");
        checkNotNullArgument(parameterName);
        if (queryCondition instanceof JpqlCondition jpqlCondition) {
            String where = jpqlCondition.getWhere();
            if (StringUtils.isNotEmpty(where)) {
                correctWhere = where.replace("?", ":" + parameterName);
                jpqlCondition.setWhere(correctWhere);
            }
        }
        this.parameterName = parameterName;
    }

    @Override
    protected Condition createQueryCondition() {
        JpqlCondition fullTextCondition = new JpqlCondition().skipNullOrEmpty();
        this.correctWhere = "{E}.id in ?";
        fullTextCondition.setWhere(correctWhere);
        fullTextCondition.setJoin("");
        return fullTextCondition;
    }

    /**
     * Creates the condition for an entity whose store cannot run the JPQL of {@link #createQueryCondition()}. Such a
     * store supports an {@code IN} condition on the primary key.
     *
     * @param metaClass meta class of the entity the data loader selects
     * @return condition restricting the primary key to the ids returned by the full text search
     */
    protected Condition createNonJpaQueryCondition(MetaClass metaClass) {
        PropertyCondition condition = PropertyCondition.inList(
                metadataTools.getPrimaryKeyName(metaClass), Collections.emptyList());
        // Skippable while empty, so an unfilled filter does not restrict the loader.
        condition.setSkipNullOrEmpty(true);
        return condition;
    }

    @Override
    protected void updateQueryCondition(@Nullable String newValue) {
        if (StringUtils.isEmpty(newValue)) {
            // An earlier search that found nothing left the condition always false.
            enableCorrectWhereClause();
            setQueryConditionParameterValue(Collections.emptyList());
        }
    }

    @Override
    public void setDataLoader(DataLoader dataLoader) {
        if (!(dataLoader instanceof CollectionLoader)) {
            throw new RuntimeException(FullTextFilter.NAME + " component can only work with CollectionLoader");
        }
        // The condition is handed to the loader below, so its shape must suit the entity's store.
        MetaClass metaClass = ((CollectionLoader<?>) dataLoader).getContainer().getEntityMetaClass();
        if (!metadataTools.isJpaEntity(metaClass)) {
            queryCondition = createNonJpaQueryCondition(metaClass);
        }
        super.setDataLoader(dataLoader);
        registerDataLoaderPreLoadListener((CollectionLoader<?>) dataLoader);
    }

    private void registerDataLoaderPreLoadListener(CollectionLoader<?> dataLoader) {
        dataLoader.addPreLoadListener(preLoadEvent -> {
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

    private List<Id> performFullTextSearch(String searchTerm) {
        SearchContext searchContext = new SearchContext(searchTerm);
        searchContext.setEntities(getDataLoader().getContainer().getEntityMetaClass().getName());
        searchContext.setSize(searchProperties.getSearchResultPageSize());

        SearchResult searchResult = searchStrategy == null
                ? entitySearcher.search(searchContext)
                : entitySearcher.search(searchContext, searchStrategy);

        return searchResult.getAllEntries().stream()
                .map(searchResultEntry -> {
                    String docId = searchResultEntry.getDocId();
                    return idSerialization.stringToId(docId);
                })
                .collect(Collectors.toList());
    }

    /**
     * When no data is returned by full-text search we must make the condition return false. We set invalid where
     * clause for that purpose. A property condition instead stops being skippable with an empty list of ids.
     */
    private void enableAlwaysFalseWhereClause() {
        if (queryCondition instanceof JpqlCondition jpqlCondition) {
            jpqlCondition.setWhere("1 <> 1");
        } else {
            ((PropertyCondition) queryCondition).setSkipNullOrEmpty(false);
        }
    }

    private void enableCorrectWhereClause() {
        if (queryCondition instanceof JpqlCondition jpqlCondition) {
            jpqlCondition.setWhere(correctWhere);
        } else {
            ((PropertyCondition) queryCondition).setSkipNullOrEmpty(true);
        }
    }

    private void setQueryConditionParameterValue(List<Object> value) {
        if (queryCondition instanceof JpqlCondition jpqlCondition) {
            jpqlCondition.setParameterValuesMap(Collections.singletonMap(parameterName, value));
        } else {
            ((PropertyCondition) queryCondition).setParameterValue(value);
        }
    }

    private void clearConditionParameterValuesMap() {
        if (queryCondition instanceof JpqlCondition jpqlCondition) {
            jpqlCondition.setParameterValuesMap(Collections.emptyMap());
        } else {
            ((PropertyCondition) queryCondition).setParameterValue(Collections.emptyList());
        }
    }

    @Override
    public String getInnerComponentPrefix() {
        return getId().orElse("fullTextFilter") + "_";
    }

    @Nullable
    public String getSearchStrategy() {
        return searchStrategy;
    }

    public void setSearchStrategy(@Nullable String searchStrategy) {
        this.searchStrategy = searchStrategy;
    }
}
