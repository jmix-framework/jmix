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

package io.jmix.flowui.util;

import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.Query;
import io.jmix.flowui.component.SupportsItemsFetchCallback;

import java.util.stream.Stream;

/**
 * Allows to pass {@link SupportsItemsFetchCallback.FetchCallback} to methods that
 * suppose {@link CallbackDataProvider.FetchCallback}.
 *
 * @param <T> data provider data type
 * @param <F> data provider filter type
 */
public class FetchCallbackAdapter<T, F> implements CallbackDataProvider.FetchCallback<T, F> {

    protected final SupportsItemsFetchCallback.FetchCallback<T, F> fetchCallback;

    public FetchCallbackAdapter(SupportsItemsFetchCallback.FetchCallback<T, F> fetchCallback) {
        this.fetchCallback = fetchCallback;
    }

    @Override
    public Stream<T> fetch(Query<T, F> query) {
        return fetchCallback.fetch(query);
    }
}
