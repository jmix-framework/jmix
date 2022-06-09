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

package io.jmix.flowui.component.grid;

import com.vaadin.flow.component.grid.Grid;
import io.jmix.flowui.action.list.ListDataComponentAction;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.grid.GridActionsSupport;

public class DataGridActionsSupport<C extends Grid<T> & ListDataComponent<T>, T> extends GridActionsSupport<C, T> {

    public DataGridActionsSupport(C grid) {
        super(grid);
    }

    @Override
    protected void addActionInternal(Action action, int index) {
        super.addActionInternal(action, index);

        attachAction(action);
    }

    @Override
    protected void removeActionInternal(Action action) {
        super.removeActionInternal(action);

        detachAction(action);
    }

    @SuppressWarnings("unchecked")
    protected void attachAction(Action action) {
        if (action instanceof ListDataComponentAction) {
            ((ListDataComponentAction<?, T>) action).setTarget(component);
        }
    }

    @SuppressWarnings("unchecked")
    protected void detachAction(Action action) {
        if (action instanceof ListDataComponentAction) {
            ((ListDataComponentAction<?, T>) action).setTarget(component);
        }
    }
}
