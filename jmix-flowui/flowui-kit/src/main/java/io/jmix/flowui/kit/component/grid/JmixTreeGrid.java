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

package io.jmix.flowui.kit.component.grid;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.treegrid.TreeGrid;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.HasActions;
import io.jmix.flowui.kit.component.HasSubParts;
import io.jmix.flowui.kit.component.SelectionChangeNotifier;
import jakarta.annotation.Nullable;

import java.util.Collection;

/**
 * A specialized TreeGrid component that introduces additional capabilities, including
 * support for actions and sub-parts functionality. This class allows enhanced
 * interaction with grid items, enabling action handling and sub-part retrieval.
 */
public class JmixTreeGrid<T> extends TreeGrid<T> implements SelectionChangeNotifier<Grid<T>, T>, HasActions, HasSubParts {

    protected GridActionsSupport<JmixTreeGrid<T>, T> actionsSupport;

    @Override
    public void addAction(Action action) {
        getActionsSupport().addAction(action);
    }

    @Override
    public void addAction(Action action, int index) {
        getActionsSupport().addAction(action, index);
    }

    @Override
    public void removeAction(Action action) {
        getActionsSupport().removeAction(action);
    }

    @Override
    public Collection<Action> getActions() {
        return getActionsSupport().getActions();
    }

    @Nullable
    @Override
    public Action getAction(String id) {
        return getActionsSupport().getAction(id).orElse(null);
    }

    @Nullable
    @Override
    public Object getSubPart(String name) {
        return getColumnByKey(name);
    }

    @Override
    public HeaderRow getDefaultHeaderRow() {
        return super.getDefaultHeaderRow();
    }

    /**
     * Provides access to the {@link GridActionsSupport} instance associated with this component.
     *
     * @return the {@link GridActionsSupport} instance responsible for handling actions in the grid
     */
    public GridActionsSupport<JmixTreeGrid<T>, T> getActionsSupport() {
        if (actionsSupport == null) {
            actionsSupport = createActionsSupport();
        }

        return actionsSupport;
    }

    protected GridActionsSupport<JmixTreeGrid<T>, T> createActionsSupport() {
        return new GridActionsSupport<>(this);
    }
}
