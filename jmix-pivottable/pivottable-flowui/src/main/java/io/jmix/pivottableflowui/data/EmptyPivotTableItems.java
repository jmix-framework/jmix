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

package io.jmix.pivottableflowui.data;

import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.data.BindingState;
import io.jmix.flowui.data.DataUnit;
import io.jmix.flowui.data.EmptyDataUnit;
import io.jmix.pivottableflowui.component.PivotTable;
import io.jmix.pivottableflowui.kit.data.JmixEmptyPivotTableItems;

import java.util.function.Consumer;

/**
 * Empty data provider for a {@link PivotTable} component.
 *
 * @param <T> type of items contained
 */
public class EmptyPivotTableItems<T> extends JmixEmptyPivotTableItems<T>
        implements PivotTableItems<T>, EmptyDataUnit {

    @SuppressWarnings("rawtypes")
    public static EmptyPivotTableItems INSTANCE = new EmptyPivotTableItems();

    /**
     * @param <T> type of items contained
     * @return instance of the {@link EmptyPivotTableItems}
     */
    @SuppressWarnings("unchecked")
    public static <T> EmptyPivotTableItems<T> getInstance() {
        return INSTANCE;
    }

    @Override
    public BindingState getState() {
        return BindingState.ACTIVE;
    }

    @Override
    public Registration addStateChangeListener(Consumer<DataUnit.StateChangeEvent> listener) {
        return () -> {/* do nothing */};
    }

    @Override
    public Class<T> getType() {
        throw new UnsupportedOperationException();
    }
}

