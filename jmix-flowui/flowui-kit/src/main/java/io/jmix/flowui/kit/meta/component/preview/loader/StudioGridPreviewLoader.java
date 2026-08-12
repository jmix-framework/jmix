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

import java.util.List;
import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import io.jmix.flowui.kit.component.grid.JmixGrid;
import io.jmix.flowui.kit.component.grid.JmixTreeGrid;
import io.jmix.flowui.kit.meta.StudioXmlElements;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewComponentLoader;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewEnvironment;
import org.jspecify.annotations.Nullable;
import org.dom4j.Element;

/**
 * Studio preview loader for {@code dataGrid} and {@code treeDataGrid}
 */
public class StudioGridPreviewLoader implements StudioPreviewComponentLoader {

    @Override
    public boolean isSupported(Element element) {
        return hasViewOrFragmentSchema(element)
                && (StudioXmlElements.DATA_GRID.equals(element.getName())
                || StudioXmlElements.TREE_DATA_GRID.equals(element.getName()));
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
        if (StudioXmlElements.TREE_DATA_GRID.equals(componentElement.getName())) {
            grid = new JmixTreeGrid<>();
        } else {
            grid = new JmixGrid<>();
        }

        loadComponentBaseAttributes(grid, componentElement);
        loadGridAttributes(grid, componentElement);

        Element columnsElement = componentElement.element(StudioXmlElements.COLUMNS);
        if (columnsElement != null && environment != StudioPreviewEnvironment.NOOP) {
            loadColumns(grid, columnsElement, componentElement, environment);
        }

        // Placeholder rows so columns are visible in preview; tree grids reject setItems(Collection).
        if (environment != StudioPreviewEnvironment.NOOP && !(grid instanceof JmixTreeGrid)) {
            grid.setItems(List.of("Item 1", "Item 2", "Item 3"));
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
                case StudioXmlElements.COLUMN ->
                        loadColumn(grid, childElement, gridElement, environment, columnsSortable, columnsResizable);
                case StudioXmlElements.EDITOR_ACTIONS_COLUMN ->
                        loadEditorActionsColumn(grid, childElement, environment);
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
                .ifPresent(footer -> column.setFooter(PreviewActionSupport.resolveText(environment, footer)));
    }

    protected void loadColumnHeader(Element columnElement, Element gridElement, StudioPreviewEnvironment environment,
                                    @Nullable String property, Grid.Column<Object> column) {
        Optional<String> header = loadString(columnElement, "header");
        if (header.isPresent()) {
            column.setHeader(PreviewActionSupport.resolveText(environment, header.get()));
        } else if (property != null) {
            String dataContainerId = loadString(gridElement, "dataContainer").orElse(null);
            String metaClass = loadString(gridElement, "metaClass").orElse(null);
            String caption = environment.propertyCaption(dataContainerId, metaClass, property);
            column.setHeader(caption != null ? caption : property);
        }
    }

    protected void loadEditorActionsColumn(Grid<Object> grid, Element columnElement,
                                           StudioPreviewEnvironment environment) {
        String key = loadString(columnElement, "key").orElse(StudioXmlElements.EDITOR_ACTIONS_COLUMN);
        Grid.Column<Object> column = grid.addColumn(item -> "").setKey(key);

        loadString(columnElement, "width", column::setWidth);
        loadBoolean(columnElement, "autoWidth", column::setAutoWidth);
        loadBoolean(columnElement, "resizable", column::setResizable);
        loadInteger(columnElement, "flexGrow", column::setFlexGrow);
        loadString(columnElement, "header")
                .ifPresent(header -> column.setHeader(PreviewActionSupport.resolveText(environment, header)));
        loadString(columnElement, "footer")
                .ifPresent(footer -> column.setFooter(PreviewActionSupport.resolveText(environment, footer)));
        loadBoolean(columnElement, "visible", column::setVisible);
    }
}
