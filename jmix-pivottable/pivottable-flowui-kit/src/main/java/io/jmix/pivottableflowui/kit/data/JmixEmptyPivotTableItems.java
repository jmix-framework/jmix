/*
 * Copyright 2024 Haulmont.
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

package io.jmix.pivottableflowui.kit.data;

import com.vaadin.flow.shared.Registration;
import io.jmix.pivottableflowui.kit.component.JmixPivotTable;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;

/**
 * Empty data provider for a {@link JmixPivotTable} component.
 *
 * @param <T> type of items contained
 */
public class JmixEmptyPivotTableItems<T> implements JmixPivotTableItems<T> {

    @SuppressWarnings("rawtypes")
    public static JmixEmptyPivotTableItems INSTANCE = new JmixEmptyPivotTableItems();

    /**
     * @param <T> type of items contained
     * @return instance of the {@link JmixEmptyPivotTableItems}
     */
    @SuppressWarnings("unchecked")
    public static <T> JmixEmptyPivotTableItems<T> getInstance() {
        return INSTANCE;
    }

    @Override
    public Collection<T> getItems() {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public T getItem(Object itemId) {
        return null;
    }

    @Nullable
    @Override
    public T getItem(String stringId) {
        return null;
    }

    @Nullable
    @Override
    public <V> V getItemValue(T itemId, String propertyId) {
        return null;
    }

    @Nullable
    @Override
    public Object getItemId(T item) {
        return null;
    }

    @Override
    public void setItemValue(T item, String propertyPath, @Nullable Object value) {
        // do nothing
    }

    @Override
    public void updateItem(T item) {
        // do nothing
    }

    @Override
    public boolean containsItem(T item) {
        return false;
    }

    @Override
    public Registration addItemsChangeListener(Consumer<ItemsChangeEvent<T>> listener) {
        throw new UnsupportedOperationException();
    }
}

