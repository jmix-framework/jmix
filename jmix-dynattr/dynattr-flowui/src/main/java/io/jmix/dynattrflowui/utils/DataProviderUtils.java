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

package io.jmix.dynattrflowui.utils;

import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;

import java.util.List;
import java.util.stream.Stream;

public final class DataProviderUtils {
    private DataProviderUtils() {}


    /**
     * Creates a new DataProvider from list for ui component that represents his own data from data provider interface
     * @param items source collection that contains items to show
     * @return lazy data provider with pagination support for ui component
     * @see com.vaadin.flow.data.provider.HasDataView
     * @see com.vaadin.flow.data.provider.DataProvider
     */
    @SuppressWarnings({"rawtypes"})
    public static DataProvider createCallbackDataProvider(List<?> items) {
        return new CallbackDataProvider<>(e -> {
            return (Stream) items.stream()
                    .limit(e.getLimit())
                    .skip(e.getOffset());

        }, e -> {
            return Math.toIntExact(items.stream()
                    .limit(e.getLimit())
                    .skip(e.getOffset())
                    .count());
        });
    }
}
