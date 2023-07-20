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

package io.jmix.flowui.component;

import com.vaadin.flow.data.provider.Query;

import java.io.Serializable;
import java.util.stream.Stream;

/**
 * A component that supports a callback for fetching items from a back end.
 *
 * @param <T> item type
 * @param <F> filter type
 */
public interface SupportsItemsFetchCallback<T, F> {

    /**
     * Supply items lazily with a callback from a backend.
     *
     * @param fetchCallback function that returns a stream of items from the backend based on the
     *                      offset, limit and an optional filter provided by the query object
     */
    void setItemsFetchCallback(FetchCallback<T, String> fetchCallback);

    /**
     * Callback interface for fetching a stream of items from a backend based on a query.
     *
     * @param <T> the type of the items to fetch
     * @param <F> the type of the optional filter in the query,
     *            {@code Void} if filtering is not supported
     */
    @FunctionalInterface
    interface FetchCallback<T, F> extends Serializable {

        /**
         * Fetches a stream of items based on a query. The query defines the
         * paging of the items to fetch through {@link Query#getOffset()} and
         * {@link Query#getLimit()}, the sorting through
         * {@link Query#getSortOrders()} and optionally also any filtering to
         * use through {@link Query#getFilter()}.
         *
         * @param query the query that defines which items to fetch
         * @return a stream of items
         */
        Stream<T> fetch(Query<T, F> query);
    }
}
