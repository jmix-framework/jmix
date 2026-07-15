/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.component.factory;

import com.google.common.base.Strings;
import com.vaadin.flow.data.provider.Query;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.FluentLoader;
import io.jmix.core.QueryUtils;
import io.jmix.core.Sort;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.querycondition.Condition;
import io.jmix.flowui.component.SupportsItemsFetchCallback;
import io.jmix.flowui.sys.substitutor.StringSubstitutor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.function.Function;

/**
 * Creates items fetch callbacks that load entities or scalar values through {@link DataManager}
 * with offset/limit paging, either from a JPQL query with a {@code :searchString} parameter or
 * from a {@link Condition} built for each search string.
 */
@Component("flowui_ItemsFetchCallbackSupport")
public class ItemsFetchCallbackSupport {

    protected static final String VALUE_PARAMETER = "value";
    protected static final String SEARCH_STRING_PARAMETER = "searchString";

    protected final DataManager dataManager;
    protected final StringSubstitutor stringSubstitutor;

    public ItemsFetchCallbackSupport(DataManager dataManager, StringSubstitutor stringSubstitutor) {
        this.dataManager = dataManager;
        this.stringSubstitutor = stringSubstitutor;
    }

    public <E> SupportsItemsFetchCallback.FetchCallback<E, String> createEntityFetchCallback(
            Class<E> entityClass, String queryString, @Nullable String searchStringFormat,
            boolean escapeValueForLike, @Nullable FetchPlan fetchPlan) {
        return query -> {
            String searchString = getSearchString(query, searchStringFormat, escapeValueForLike);

            FluentLoader.ByQuery<E> loader = dataManager.load(entityClass)
                    .query(queryString)
                    .parameter(SEARCH_STRING_PARAMETER, searchString)
                    .firstResult(query.getOffset())
                    .maxResults(query.getLimit());
            if (fetchPlan != null) {
                loader.fetchPlan(fetchPlan);
            }

            return loader.list().stream();
        };
    }

    public <E> SupportsItemsFetchCallback.FetchCallback<E, String> createEntityFetchCallback(
            Class<E> entityClass, Function<String, Condition> conditionBuilder,
            @Nullable Sort sort, @Nullable FetchPlan fetchPlan) {
        return query -> {
            Condition condition = conditionBuilder.apply(query.getFilter().orElse(""));

            FluentLoader.ByCondition<E> loader = dataManager.load(entityClass)
                    .condition(condition)
                    .firstResult(query.getOffset())
                    .maxResults(query.getLimit());
            if (sort != null) {
                loader.sort(sort);
            }
            if (fetchPlan != null) {
                loader.fetchPlan(fetchPlan);
            }

            return loader.list().stream();
        };
    }

    public SupportsItemsFetchCallback.FetchCallback<Object, String> createValuesFetchCallback(
            String queryString, @Nullable String searchStringFormat, boolean escapeValueForLike) {
        return query -> {
            String searchString = getSearchString(query, searchStringFormat, escapeValueForLike);

            return dataManager.loadValues(queryString)
                    .properties(VALUE_PARAMETER)
                    .parameter(SEARCH_STRING_PARAMETER, searchString)
                    .firstResult(query.getOffset())
                    .maxResults(query.getLimit())
                    .list().stream()
                    .map(entity -> entity.getValue(VALUE_PARAMETER));
        };
    }

    protected String getSearchString(Query<?, String> query,
                                     @Nullable String searchStringFormat, boolean escapeValue) {
        String searchString = query.getFilter().orElse("");
        if (escapeValue) {
            searchString = QueryUtils.escapeForLike(searchString);
        }

        if (!Strings.isNullOrEmpty(searchStringFormat)) {
            searchString = stringSubstitutor.substitute(searchStringFormat,
                    ParamsMap.of("inputString", searchString));
        }

        return searchString;
    }
}
