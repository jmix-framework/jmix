/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.kit.meta.component.preview.loader;

import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import io.jmix.flowui.kit.component.grid.JmixGrid;
import io.jmix.flowui.kit.component.grid.JmixTreeGrid;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewComponentLoader;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewEnvironment;
import org.jspecify.annotations.Nullable;
import org.dom4j.Element;

/**
 * Studio preview loader for {@code dataGrid} and {@code treeDataGrid}: instantiates the grid
 * and builds its column skeleton (keys, widths, resolved headers/footers) so the designer
 * preview reflects the declared layout without requiring live data.
 * <p>
 * Column building is gated on the {@link StudioPreviewEnvironment} handshake: released Studio
 * versions (&ge; 2.3.0) call the old 2-arg {@link #load(Element, Element) load} entry point and
 * have no bind-by-key guard, so they add their own columns on top of whatever the loader
 * returns — without an environment there is no way for the caller to bind to loader-built
 * columns, and every column would be duplicated in the designer. So when {@code environment} is
 * {@link StudioPreviewEnvironment#NOOP NOOP} (the 2-arg entry point routes here), the loader
 * returns a bare grid with only the base/grid-level attributes applied (phase-1 behavior) and
 * lets old Studio build columns itself. Only callers that pass a real environment through the
 * 3-arg entry point get the populated column skeleton.
 * <p>
 * Limitations inherent to a data-less preview:
 * <ul>
 *     <li>{@code includeAll="true"} cannot be expanded from a fetch plan (there is no entity
 *     metadata here); only explicitly declared {@code column} children are loaded. The loader
 *     still builds the column skeleton in that case, since Studio itself only models the
 *     explicitly declared tags too.</li>
 *     <li>{@code editorActionsColumn} cell buttons (save/cancel/close/edit) are not built,
 *     since cells never render without data — only the header/width is visible in preview.</li>
 *     <li>{@code treeDataGrid} columns are added as plain columns; there is no hierarchy
 *     column to build without rows to indent.</li>
 * </ul>
 */
public class StudioGridPreviewLoader implements StudioPreviewComponentLoader {

    protected static final String DATA_GRID_ELEMENT = "dataGrid";
    protected static final String TREE_DATA_GRID_ELEMENT = "treeDataGrid";

    protected static final String COLUMNS_ELEMENT = "columns";
    protected static final String COLUMN_ELEMENT = "column";
    protected static final String EDITOR_ACTIONS_COLUMN_ELEMENT = "editorActionsColumn";
    protected static final String EDITOR_ACTIONS_COLUMN_DEFAULT_KEY = "editorActionsColumn";

    protected static final String MESSAGE_REF_PREFIX = "msg://";

    @Override
    public boolean isSupported(Element element) {
        return hasViewOrFragmentSchema(element)
                && (DATA_GRID_ELEMENT.equals(element.getName()) || TREE_DATA_GRID_ELEMENT.equals(element.getName()));
    }

    @Nullable
    @Override
    public Component load(Element componentElement, Element viewElement) {
        return load(componentElement, viewElement, StudioPreviewEnvironment.NOOP);
    }

    @Nullable
    @Override
    public Component load(Element componentElement, Element viewElement, StudioPreviewEnvironment environment) {
        Grid<Object> grid;
        if (TREE_DATA_GRID_ELEMENT.equals(componentElement.getName())) {
            grid = new JmixTreeGrid<>();
        } else {
            grid = new JmixGrid<>();
        }

        loadComponentBaseAttributes(grid, componentElement);
        loadGridAttributes(grid, componentElement);

        // Old-Studio compatibility (no environment handshake): without an environment the caller
        // cannot bind to loader-built columns, so it adds its own on top and every column gets
        // duplicated. Only build columns when a real environment was passed.
        Element columnsElement = componentElement.element(COLUMNS_ELEMENT);
        if (columnsElement != null && environment != StudioPreviewEnvironment.NOOP) {
            loadColumns(grid, columnsElement, componentElement, environment);
        }

        return grid;
    }

    protected void loadGridAttributes(Grid<Object> grid, Element gridElement) {
        loadEnum(gridElement, Grid.SelectionMode.class, "selectionMode", grid::setSelectionMode);
        loadBoolean(gridElement, "columnReorderingAllowed", grid::setColumnReorderingAllowed);
        loadBoolean(gridElement, "allRowsVisible", grid::setAllRowsVisible);
        loadInteger(gridElement, "pageSize", grid::setPageSize);
        loadBoolean(gridElement, "rowsDraggable", grid::setRowsDraggable);
    }

    /**
     * Loads the {@code columns} child of a grid element, one column per declared child.
     * {@code includeAll} is intentionally ignored: expanding it needs a fetch plan/metaClass
     * that a data-less preview does not have, so only explicitly declared children are built.
     */
    protected void loadColumns(Grid<Object> grid, Element columnsElement, Element gridElement,
                               StudioPreviewEnvironment environment) {
        boolean columnsSortable = loadBoolean(columnsElement, "sortable").orElse(true);
        boolean columnsResizable = loadBoolean(columnsElement, "resizable").orElse(false);

        for (Element childElement : columnsElement.elements()) {
            switch (childElement.getName()) {
                case COLUMN_ELEMENT ->
                        loadColumn(grid, childElement, gridElement, environment, columnsSortable, columnsResizable);
                case EDITOR_ACTIONS_COLUMN_ELEMENT -> loadEditorActionsColumn(grid, childElement, environment);
                default -> {
                    // unknown columns' child (e.g. groupColumn): skipped silently in preview
                }
            }
        }
    }

    protected void loadColumn(Grid<Object> grid, Element columnElement, Element gridElement,
                              StudioPreviewEnvironment environment, boolean columnsSortable,
                              boolean columnsResizable) {
        String property = loadString(columnElement, "property").orElse(null);
        String key = loadString(columnElement, "key").orElse(property);
        if (key == null) {
            // Neither key nor property defined: runtime throws, preview skips silently.
            return;
        }

        Grid.Column<Object> column = grid.addColumn(item -> "").setKey(key);

        loadString(columnElement, "width", column::setWidth);
        loadBoolean(columnElement, "autoWidth", column::setAutoWidth);
        loadInteger(columnElement, "flexGrow", column::setFlexGrow);
        loadBoolean(columnElement, "frozen", column::setFrozen);
        loadEnum(columnElement, ColumnTextAlign.class, "textAlign", column::setTextAlign);
        column.setSortable(loadBoolean(columnElement, "sortable").orElse(columnsSortable));
        column.setResizable(loadBoolean(columnElement, "resizable").orElse(columnsResizable));
        loadBoolean(columnElement, "visible", column::setVisible);

        loadColumnHeader(columnElement, gridElement, environment, property, column);
        loadString(columnElement, "footer")
                .ifPresent(footer -> column.setFooter(resolveText(environment, footer)));
    }

    protected void loadColumnHeader(Element columnElement, Element gridElement, StudioPreviewEnvironment environment,
                                    @Nullable String property, Grid.Column<Object> column) {
        Optional<String> header = loadString(columnElement, "header");
        if (header.isPresent()) {
            column.setHeader(resolveText(environment, header.get()));
        } else if (property != null) {
            String dataContainerId = loadString(gridElement, "dataContainer").orElse(null);
            String metaClass = loadString(gridElement, "metaClass").orElse(null);
            String caption = environment.propertyCaption(dataContainerId, metaClass, property);
            column.setHeader(caption != null ? caption : property);
        }
    }

    protected void loadEditorActionsColumn(Grid<Object> grid, Element columnElement,
                                           StudioPreviewEnvironment environment) {
        String key = loadString(columnElement, "key").orElse(EDITOR_ACTIONS_COLUMN_DEFAULT_KEY);
        Grid.Column<Object> column = grid.addColumn(item -> "").setKey(key);

        loadString(columnElement, "width", column::setWidth);
        loadBoolean(columnElement, "autoWidth", column::setAutoWidth);
        loadBoolean(columnElement, "resizable", column::setResizable);
        loadInteger(columnElement, "flexGrow", column::setFlexGrow);
        loadString(columnElement, "header").ifPresent(header -> column.setHeader(resolveText(environment, header)));
        loadString(columnElement, "footer").ifPresent(footer -> column.setFooter(resolveText(environment, footer)));
        loadBoolean(columnElement, "visible", column::setVisible);
    }

    /**
     * Resolves a {@code msg://} message reference through the environment, falling back to
     * the raw value when the reference isn't a message key or the environment can't resolve it
     * (e.g. {@link StudioPreviewEnvironment#NOOP}).
     */
    protected String resolveText(StudioPreviewEnvironment environment, String value) {
        if (!value.startsWith(MESSAGE_REF_PREFIX)) {
            return value;
        }
        String resolved = environment.resolveMessage(value);
        return resolved != null ? resolved : value;
    }
}
