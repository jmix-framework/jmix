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

package io.jmix.graphql.loader;

import io.jmix.core.FetchPlan;
import io.jmix.core.LoadContext;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.graphql.schema.Types;
import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.lang3.tuple.Pair;

public class GraphQLEntityListDataFetcherContext<E> {
    private MetaClass metaClass;
    private LogicalCondition filter;
    private OrderedMap<String, Types.SortOrder> orderBy;
    private Integer limit;
    private Integer offset;
    private LoadContext<E> ctx;
    private FetchPlan fetchPlan;

    public GraphQLEntityListDataFetcherContext(MetaClass metaClass, LoadContext<E> ctx, LogicalCondition filter, OrderedMap<String, Types.SortOrder> orderBy,
                                               Integer limit, Integer offset, FetchPlan fetchPlan) {
        this.metaClass = metaClass;
        this.filter = filter;
        this.orderBy = orderBy;
        this.limit = limit;
        this.offset = offset;
        this.ctx = ctx;
        this.fetchPlan = fetchPlan;
    }

    public MetaClass getMetaClass() {
        return metaClass;
    }

    public void setMetaClass(MetaClass metaClass) {
        this.metaClass = metaClass;
    }

    public LogicalCondition getFilter() {
        return filter;
    }

    public void setFilter(LogicalCondition filter) {
        this.filter = filter;
    }

    public OrderedMap<String, Types.SortOrder> getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(OrderedMap<String, Types.SortOrder> orderBy) {
        this.orderBy = orderBy;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public LoadContext<E> getLoadContext() {
        return ctx;
    }

    public void setLoadContext(LoadContext<E> loadContext) {
        this.ctx = loadContext;
    }

    public FetchPlan getFetchPlan() {
        return fetchPlan;
    }

    public void setFetchPlan(FetchPlan fetchPlan) {
        this.fetchPlan = fetchPlan;
    }
}
