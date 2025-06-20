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
import com.vaadin.flow.function.SerializableFunction;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.HasActions;
import io.jmix.flowui.kit.component.HasSubParts;
import io.jmix.flowui.kit.component.SelectionChangeNotifier;

import io.jmix.flowui.kit.meta.StudioIgnore;
import jakarta.annotation.Nullable;
import java.util.Collection;

/**
 * A customizable grid component that extends the Vaadin {@link Grid} and provides
 * additional capabilities such as handling actions and sub-parts support.
 *
 * @param <T> the type of the items displayed in the grid
 */
public class JmixGrid<T> extends Grid<T> implements SelectionChangeNotifier<Grid<T>, T>, HasActions, HasSubParts {

    protected GridActionsSupport<JmixGrid<T>, T> actionsSupport;

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
    @StudioIgnore
    public void setClassNameGenerator(SerializableFunction<T, String> classNameGenerator) {
        super.setClassNameGenerator(classNameGenerator);
    }

    /**
     * @return a default header row
     */
    @Override
    public HeaderRow getDefaultHeaderRow() {
        return super.getDefaultHeaderRow();
    }

    /**
     * Provides access to the {@link GridActionsSupport} instance associated with this grid component.
     *
     * @return the {@link GridActionsSupport} instance associated with this grid
     */
    public GridActionsSupport<JmixGrid<T>, T> getActionsSupport() {
        if (actionsSupport == null) {
            actionsSupport = createActionsSupport();
        }

        return actionsSupport;
    }

    protected GridActionsSupport<JmixGrid<T>, T> createActionsSupport() {
        return new GridActionsSupport<>(this);
    }
}
