/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.widget.grid;

import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.components.grid.Editor;

import java.util.EventObject;
import java.util.Map;

/**
 * An event that is fired before a Grid editor is saved.
 *
 * @param <T> the bean type
 * @see JmixEditorBeforeSaveListener
 * @see JmixEditorImpl#addBeforeSaveListener(JmixEditorBeforeSaveListener)
 */
public class JmixEditorBeforeSaveEvent<T> extends EventObject {

    protected T bean;
    protected Map<Grid.Column<T, ?>, Component> columnFieldMap;

    /**
     * Constructor for a editor save event.
     *
     * @param editor the source of the event
     * @param bean   the bean being edited
     */
    public JmixEditorBeforeSaveEvent(Editor<T> editor, T bean, Map<Grid.Column<T, ?>, Component> columnFieldMap) {
        super(editor);
        this.bean = bean;
        this.columnFieldMap = columnFieldMap;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Editor<T> getSource() {
        return (Editor<T>) super.getSource();
    }

    /**
     * Gets the editor grid.
     *
     * @return the editor grid
     */
    public Grid<T> getGrid() {
        return getSource().getGrid();
    }

    /**
     * Gets the bean being edited.
     *
     * @return the bean being edited
     */
    public T getBean() {
        return bean;
    }

    /**
     * @return a mapping of field to columns
     */
    public Map<Grid.Column<T, ?>, Component> getColumnFieldMap() {
        return columnFieldMap;
    }
}
