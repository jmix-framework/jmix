/*
 * Copyright 2023 Haulmont.
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
import com.vaadin.flow.data.renderer.Renderer;

/**
 * For Studio use only.
 */
final class EditorActionsColumn<T> extends Grid.Column<T> {
    private EditorActionsColumn(Grid<T> grid, String columnId, Renderer<T> renderer) {
        super(grid, columnId, renderer);
    }
}
