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

package com.haulmont.cuba.gui.model.impl;

import com.haulmont.cuba.gui.model.LoaderSupportsApplyToSelected;
import io.jmix.core.LoadContext;
import io.jmix.ui.model.impl.CollectionLoaderImpl;

import javax.annotation.Nullable;
import java.util.List;

public class CubaCollectionLoaderImpl<E> extends CollectionLoaderImpl<E>
        implements LoaderSupportsApplyToSelected {

    protected LoadContext.Query lastQuery;
    protected List<LoadContext.Query> prevQueries;
    protected Integer queryKey;

    @Override
    protected boolean _load() {
        if (super._load()) {
            lastQuery = createLoadContext().getQuery();
            return true;
        }
        return false;
    }

    @Override
    public LoadContext<E> createLoadContext() {
        LoadContext<E> loadContext = super.createLoadContext();

        if (prevQueries != null) {
            loadContext.getPreviousQueries().addAll(prevQueries);
        }
        if (queryKey != null) {
            loadContext.setQueryKey(queryKey);
        }

        return loadContext;
    }

    @Nullable
    @Override
    public LoadContext.Query getLastQuery() {
        return lastQuery;
    }

    @Nullable
    @Override
    public List<LoadContext.Query> getPrevQueries() {
        return prevQueries;
    }

    @Override
    public void setPrevQueries(@Nullable List<LoadContext.Query> prevQueries) {
        this.prevQueries = prevQueries;
    }

    @Override
    public Integer getQueryKey() {
        return queryKey;
    }

    @Override
    public void setQueryKey(@Nullable Integer queryKey) {
        this.queryKey = queryKey;
    }
}
