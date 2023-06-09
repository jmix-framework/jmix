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

package io.jmix.flowui.data.items;

import com.vaadin.flow.data.provider.DataProviderWrapper;
import com.vaadin.flow.data.provider.InMemoryDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.function.SerializablePredicate;

import java.util.Optional;

public class InMemoryDataProviderWrapper<T> extends DataProviderWrapper<T, Void, SerializablePredicate<T>> {

    public InMemoryDataProviderWrapper(InMemoryDataProvider<T> dataProvider) {
        super(dataProvider);
    }

    @Override
    protected SerializablePredicate<T> getFilter(Query<T, Void> query) {
        // Just ignore the query filter (Void) and apply the
        // predicate only
        return Optional.ofNullable(getDataProvider().getFilter())
                .orElse(item -> true);
    }

    public InMemoryDataProvider<T> getDataProvider() {
        return ((InMemoryDataProvider<T>) dataProvider);
    }
}
