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

package io.jmix.flowui.component.grid.editor;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import io.jmix.flowui.component.SupportsStatusChangeHandler;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.data.ValueSource;

import jakarta.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An editor in a Grid.
 * <p>
 * This class contains methods for editor functionality: configure an editor,
 * open the editor, save and cancel a row editing, utility methods for
 * defining column edit components.
 *
 * @param <T> the type of the row/item being edited
 */
public interface DataGridEditor<T> extends Editor<T> {

    @Nullable
    @Override
    T getItem();

    /**
     * @return {@code true} if this editor is buffered and is in a
     * process of writing data to an item, {@code false} otherwise
     */
    boolean isSaving();

    /**
     * Inits the default function that returns the column editor component
     * that is bound to the passed entity's property. To find the column
     * it's assumed that the column key is equal to the property.
     *
     * @param property an entity attribute for which the edit component is created
     * @see #setColumnEditorComponent(String, Function)
     * @see #initColumnDefaultEditorComponent(Grid.Column, String)
     * @see #setColumnEditorComponent(Grid.Column, String, Function)
     */
    void initColumnDefaultEditorComponent(String property);

    /**
     * Sets a function that returns the column editor component. To bound an editor component
     * to the passed entity's property, the function implementation should use
     * {@link EditComponentGenerationContext#getValueSourceProvider} to obtain an instance
     * of {@link ValueSource}. To find the column it's assumed that the column key is equal
     * to the property.
     *
     * @param property  an entity attribute for which the edit component is created
     * @param generator a callback function that is used to create an edit component
     * @see #initColumnDefaultEditorComponent(String)
     * @see #initColumnDefaultEditorComponent(Grid.Column, String)
     * @see #setColumnEditorComponent(Grid.Column, String, Function)
     */
    void setColumnEditorComponent(String property,
                                  Function<EditComponentGenerationContext<T>, Component> generator);

    /**
     * Inits the default function that returns the column editor component
     * that is bound to the passed entity's property.
     *
     * @param column   a grid column for which to set editor component
     * @param property an entity attribute for which an edit component is created
     * @see #initColumnDefaultEditorComponent(String)
     * @see #setColumnEditorComponent(String, Function)
     * @see #setColumnEditorComponent(Grid.Column, String, Function)
     */
    void initColumnDefaultEditorComponent(Grid.Column<T> column, String property);

    /**
     * Sets a function that returns the column editor component. To bound an editor component
     * to the passed entity's property, the function implementation should use
     * {@link EditComponentGenerationContext#getValueSourceProvider} to obtain an instance
     * of {@link ValueSource}.
     *
     * @param column    a grid column for which to set editor component
     * @param property  an entity attribute for which the edit component is created
     * @param generator a callback function that is used to create an edit component
     * @see #initColumnDefaultEditorComponent(String)
     * @see #setColumnEditorComponent(String, Function)
     * @see #initColumnDefaultEditorComponent(Grid.Column, String)
     */
    void setColumnEditorComponent(Grid.Column<T> column, String property,
                                  Function<EditComponentGenerationContext<T>, Component> generator);

    /**
     * Sets a callback that is set to editor components which implement
     * {@link SupportsStatusChangeHandler}. If a custom component generator is used,
     * this status handler can be obtained from {@link EditComponentGenerationContext#getStatusHandler()}.
     *
     * @param handler a callback that is set to editor components which
     *                implement {@link SupportsStatusChangeHandler}
     */
    void setDefaultComponentStatusHandler(@Nullable Consumer<SupportsStatusChangeHandler.StatusContext<?>> handler);

    /**
     * Sets a callback that is used to handle validation errors when Editor
     * attempts to save the data, i.e. when {@link #save()} is invoked.
     * <p>
     * Note! This handler can be called only if editor is in buffered mode.
     *
     * @param validationErrorsHandler a callback that is used to handle validation errors
     * @see #setBuffered(boolean)
     */
    void setValidationErrorsHandler(@Nullable Consumer<ValidationErrors> validationErrorsHandler);
}
