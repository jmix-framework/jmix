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

public class GraphQLEntityDataFetcherContext<E> {
    private MetaClass metaClass;
    private String id;
    private LoadContext<E> loadContext;
    private FetchPlan fetchPlan;

    public GraphQLEntityDataFetcherContext(MetaClass metaClass, String id, LoadContext<E> loadContext, FetchPlan fetchPlan) {
        this.metaClass = metaClass;
        this.id = id;
        this.loadContext = loadContext;
        this.fetchPlan = fetchPlan;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LoadContext<E> getLoadContext() {
        return loadContext;
    }

    public void setLoadContext(LoadContext<E> loadContext) {
        this.loadContext = loadContext;
    }

    public FetchPlan getFetchPlan() {
        return fetchPlan;
    }

    public void setFetchPlan(FetchPlan fetchPlan) {
        this.fetchPlan = fetchPlan;
    }
}
