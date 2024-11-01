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

package io.jmix.flowui.kit.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.grid.GridSingleSelectionModel;
import com.vaadin.flow.data.selection.MultiSelectionListener;
import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.data.selection.SingleSelectionListener;
import com.vaadin.flow.shared.Registration;

/**
 * Interface to be implemented by UI components that support adding items selection listeners.
 *
 * @param <C> the component type
 * @param <T> the type of the items to select
 */
public interface SelectionChangeNotifier<C extends Component, T> {

    /**
     * Adds a selection listener to the component.
     *
     * @param listener the listener to add
     * @return a registration handle to remove the listener
     */
    Registration addSelectionListener(SelectionListener<C, T> listener);
}
