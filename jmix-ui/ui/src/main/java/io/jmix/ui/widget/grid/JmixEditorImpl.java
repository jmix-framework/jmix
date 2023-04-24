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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.vaadin.data.HasValue;
import com.vaadin.data.PropertySet;
import com.vaadin.data.ValidationResult;
import com.vaadin.shared.Registration;
import com.vaadin.shared.ui.grid.editor.EditorClientRpc;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.components.grid.EditorImpl;
import com.vaadin.util.ReflectTools;
import io.jmix.ui.widget.JmixEnhancedGrid;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JmixEditorImpl<T> extends EditorImpl<T> {

    protected Function<Map<String, Object>, String> crossFieldValidationHandler;

    /**
     * Constructor for internal implementation of the Editor.
     *
     * @param propertySet the property set to use for configuring the default binder
     */
    public JmixEditorImpl(PropertySet<T> propertySet) {
        super(propertySet);
    }

    /**
     * Sets cross field validation handler. The first parameter in Function contains changed values from the editor's
     * fields: property - value, the second parameter is String which must contain error message or null if validation
     * was successful.
     *
     * @param handler handler function
     */
    public void setCrossFieldValidationHandler(@Nullable Function<Map<String, Object>, String> handler) {
        this.crossFieldValidationHandler = handler;
    }

    /**
     * @return cross field validation handler
     */
    @Nullable
    public Function<Map<String, Object>, String> getCrossFieldValidationHandler() {
        return crossFieldValidationHandler;
    }

    protected JmixEnhancedGrid<T> getEnhancedGrid() {
        //noinspection unchecked
        return (JmixEnhancedGrid<T>) super.getParent();
    }

    public T getBean() {
        return edited;
    }

    @Override
    protected void doConfirmBind(EditorClientRpc rpc, String key) {
        T data = getData(key);
        if (data != null) {
            rpc.confirmBind(true);
            doEdit(data);
        } else {
            rpc.confirmBind(false);
        }
    }

    @Override
    protected void doEdit(T bean) {
        Objects.requireNonNull(bean, "Editor can't edit null");
        if (!isEnabled()) {
            throw new IllegalStateException(
                    "Editing is not allowed when Editor is disabled.");
        }

        edited = bean;

        getParent().select(edited); // ItemPropertyChangedEvent is sent only if a row is selected

        getParent().getColumns().stream().filter(Grid.Column::isEditable)
                .forEach(c -> {
                    JmixEditorField<?> editorField = getEnhancedGrid().getColumnEditorField(bean, c);
                    configureField(editorField);
                    addComponentToGrid(editorField);
                    columnFields.put(c, editorField);
                    getState().columnFields.put(getInternalIdForColumn(c), editorField.getConnectorId());
                });

        eventRouter.fireEvent(new JmixEditorOpenEvent<>(this, edited, Collections.unmodifiableMap(columnFields)));
    }

    protected void configureField(JmixEditorField<?> field) {
        field.setBuffered(isBuffered());
        field.setEnabled(isEnabled());
        field.addValueChangeListener(this::onFieldValueChange);
    }

    protected void onFieldValueChange(HasValue.ValueChangeEvent<?> ignored) {
        isEditorFieldsValid();
    }

    @Override
    public boolean save() {
        if (isOpen() && isBuffered()) {
            eventRouter.fireEvent(
                    new JmixEditorBeforeSaveEvent<>(this, edited, Collections.unmodifiableMap(columnFields)));
            if (isEditorFieldsValid()) {
                commitFields();
                refresh(edited);
                eventRouter.fireEvent(
                        new JmixEditorSaveEvent<>(this, edited, Collections.unmodifiableMap(columnFields)));
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doCancel(boolean afterBeingSaved) {
        // CAUTION copied from parent with changes
        T editedBean = edited;
        // As columnFields is cleared in doClose, we need to make a copy of it
        Map<Grid.Column<T, ?>, Component> usedColumnFields = ImmutableMap.copyOf(columnFields);
        doClose();

        eventRouter.fireEvent(
                new JmixEditorCancelEvent<>(this, editedBean, usedColumnFields, !afterBeingSaved));
    }

    protected boolean isEditorFieldsValid() {
        Map<Component, ValidationResult> errors = getValidationErrors();
        return handleValidation(errors);
    }

    protected boolean handleValidation(Map<Component, ValidationResult> errors) {
        if (errors.isEmpty()) {
            // validate cross fields if there is no errors after validation and
            // user clicked save or it is not buffered mode
            if (!isBuffered() || saving) {
                if (crossFieldValidationHandler != null) {
                    String errorMessage = crossFieldValidationHandler.apply(generatePropertiesMap());
                    boolean ok = errorMessage == null;

                    confirmSave(ok);
                    rpc.setErrorMessage(errorMessage, Collections.emptyList());

                    return ok;
                } else {
                    confirmSave(true);
                }
            }
            // Hide error message
            rpc.setErrorMessage(null, Collections.emptyList());
        } else {
            confirmSave(false);

            List<Component> fields = errors.keySet().stream()
                    .filter(columnFields.values()::contains)
                    .collect(Collectors.toList());

            Map<Component, Grid.Column<T, ?>> fieldToColumn = new HashMap<>();
            columnFields.entrySet().stream()
                    .filter(entry -> fields.contains(entry.getValue()))
                    .forEach(entry -> fieldToColumn.put(entry.getValue(),
                            entry.getKey()));

            String message = generateErrorMessage(fieldToColumn, errors);

            List<String> columnIds = fieldToColumn.values().stream()
                    .map(this::getInternalIdForColumn)
                    .collect(Collectors.toList());

            rpc.setErrorMessage(message, columnIds);
            return false;
        }

        return true;
    }

    protected Map<String, Object> generatePropertiesMap() {
        Map<String, Object> properties = new HashMap<>();
        for (Map.Entry<Grid.Column<T, ?>, Component> entry : columnFields.entrySet()) {
            properties.put(entry.getKey().getId(), ((JmixEditorField<?>) entry.getValue()).getValue());
        }
        return properties;
    }

    protected void confirmSave(boolean ok) {
        if (saving) {
            rpc.confirmSave(ok);
            saving = false;
        }
    }

    protected String generateErrorMessage(Map<Component, Grid.Column<T, ?>> fieldToColumn,
                                          Map<Component, ValidationResult> errors) {
        return errors.entrySet().stream()
                .filter(entry ->
                        !Strings.isNullOrEmpty(entry.getValue().getErrorMessage())
                                && fieldToColumn.containsKey(entry.getKey()))
                .map(entry ->
                        fieldToColumn.get(entry.getKey()).getCaption() + ": " +
                                entry.getValue().getErrorMessage())
                .collect(Collectors.joining("; "));
    }

    protected Map<Component, ValidationResult> getValidationErrors() {
        Map<Component, ValidationResult> errors = new HashMap<>();
        columnFields.values().forEach(field -> {
            ValidationResult validationResult = ((JmixEditorField<?>) field).validate();
            if (validationResult.isError()) {
                errors.put(field, validationResult);
            }
        });
        return errors;
    }

    protected void commitFields() {
        columnFields.values().forEach(field -> {
            ((JmixEditorField<?>) field).commit();
        });
    }

    public Registration addBeforeSaveListener(JmixEditorBeforeSaveListener<T> listener) {
        return eventRouter.addListener(JmixEditorBeforeSaveEvent.class, listener,
                ReflectTools.getMethod(JmixEditorBeforeSaveListener.class));
    }
}
