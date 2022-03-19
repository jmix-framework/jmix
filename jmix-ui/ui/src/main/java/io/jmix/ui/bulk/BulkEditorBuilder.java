/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.bulk;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.app.bulk.BulkEditorWindow;
import io.jmix.ui.app.bulk.ColumnsMode;
import io.jmix.ui.app.bulk.FieldSorter;
import io.jmix.ui.component.ListComponent;
import io.jmix.ui.component.validation.Validator;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.OpenMode;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * A builder that creates a new {@link BulkEditorWindow} with defined parameters.
 *
 * @param <E> item type
 */
public class BulkEditorBuilder<E> {

    protected final MetaClass metaClass;
    protected final FrameOwner origin;
    protected final Collection<E> entities;
    protected final Function<BulkEditorBuilder<E>, BulkEditorWindow<E>> handler;

    protected OpenMode openMode = OpenMode.DIALOG;
    protected ListComponent<E> listComponent;

    protected String exclude;
    protected List<String> includeProperties = Collections.emptyList();
    protected Map<String, Validator<?>> fieldValidators;
    protected List<Validator<?>> modelValidators;
    protected Boolean useConfirmDialog;
    protected FieldSorter fieldSorter;
    protected ColumnsMode columnsMode;

    public BulkEditorBuilder(BulkEditorBuilder<E> builder) {
        this.metaClass = builder.metaClass;
        this.origin = builder.origin;
        this.handler = builder.handler;
        this.entities = builder.entities;

        this.openMode = builder.openMode;
        this.listComponent = builder.listComponent;

        this.exclude = builder.exclude;
        this.includeProperties = builder.includeProperties;
        this.fieldValidators = builder.fieldValidators;
        this.modelValidators = builder.modelValidators;
        this.useConfirmDialog = builder.useConfirmDialog;
        this.fieldSorter = builder.fieldSorter;
        this.columnsMode = builder.columnsMode;
    }

    public BulkEditorBuilder(MetaClass metaClass, Collection<E> entities, FrameOwner origin,
                             Function<BulkEditorBuilder<E>, BulkEditorWindow<E>> handler) {
        this.metaClass = metaClass;
        this.entities = entities;
        this.origin = origin;
        this.handler = handler;
    }

    /**
     * Sets screen open mode.
     *
     * @param openMode the open mode to set
     * @return this builder
     */
    public BulkEditorBuilder<E> withOpenMode(OpenMode openMode) {
        checkNotNullArgument(openMode);

        this.openMode = openMode;
        return this;
    }

    /**
     * Sets the list component that displays the items to be edited.
     *
     * @param listComponent the list component to be used
     * @return this builder
     */
    public BulkEditorBuilder<E> withListComponent(ListComponent<E> listComponent) {
        this.listComponent = listComponent;
        return this;
    }

    /**
     * Sets a regular expression to exclude some fields explicitly
     * from the list of attributes available for editing.
     *
     * @param exclude a regular expression
     * @return this builder
     */
    public BulkEditorBuilder<E> withExclude(String exclude) {
        this.exclude = exclude;
        return this;
    }

    /**
     * Sets the entity attributes to be included to bulk editor window.
     * If set, other attributes will be ignored.
     *
     * @param includeProperties the entity attributes to be included to bulk editor window
     * @return this builder
     */
    public BulkEditorBuilder<E> withIncludeProperties(List<String> includeProperties) {
        this.includeProperties = includeProperties;
        return this;
    }

    /**
     * Sets a map with validators for fields that will be used for editing certain properties.
     *
     * @param fieldValidators a map with validators for fields that will be used for editing certain properties
     * @return this builder
     */
    public BulkEditorBuilder<E> withFieldValidators(Map<String, Validator<?>> fieldValidators) {
        this.fieldValidators = fieldValidators;
        return this;
    }

    /**
     * Sets a map with validators for the result of bulk editing.
     *
     * @param modelValidators a map with validators for the result of bulk editing
     * @return this builder
     */
    public BulkEditorBuilder<E> withModelValidators(List<Validator<?>> modelValidators) {
        this.modelValidators = modelValidators;
        return this;
    }

    /**
     * Sets whether or not the confirmation dialog should be displayed to
     * the user before saving the changes. The default value is true.
     *
     * @param useConfirmDialog whether or not the confirmation dialog should be displayed
     * @return this builder
     */
    public BulkEditorBuilder<E> withUseConfirmDialog(Boolean useConfirmDialog) {
        this.useConfirmDialog = useConfirmDialog;
        return this;
    }

    /**
     * Sets field sorter that allows you to sort fields by custom logic.
     *
     * @param fieldSorter field sorter
     * @return this builder
     */
    public BulkEditorBuilder<E> withFieldSorter(FieldSorter fieldSorter) {
        this.fieldSorter = fieldSorter;
        return this;
    }

    /**
     * Sets the columns mode for editor which defines number of columns.
     *
     * @param columnsMode columns mode
     * @return this builder
     * @see ColumnsMode#ONE_COLUMN
     * @see ColumnsMode#TWO_COLUMNS
     */
    public BulkEditorBuilder<E> withColumnsMode(ColumnsMode columnsMode) {
        this.columnsMode = columnsMode;
        return this;
    }

    /**
     * @return a {@link FrameOwner} of bulk editor
     */
    public FrameOwner getOrigin() {
        return origin;
    }

    /**
     * @return a {@link MetaClass} of items
     */
    public MetaClass getMetaClass() {
        return metaClass;
    }

    /**
     * @return a collection of items to be edited
     */
    public Collection<E> getEntities() {
        return entities;
    }

    /**
     * @return screen launch mode
     */
    public OpenMode getOpenMode() {
        return openMode;
    }

    /**
     * @return the list component that displays the items to be edited
     */
    @Nullable
    public ListComponent<E> getListComponent() {
        return listComponent;
    }

    /**
     * @return a regular expression to exclude some fields
     * explicitly from the list of attributes available for editing
     */
    @Nullable
    public String getExclude() {
        return exclude;
    }

    /**
     * @return the entity attributes to be included to bulk editor window
     */
    @Nullable
    public List<String> getIncludeProperties() {
        return includeProperties;
    }

    /**
     * @return a map with validators for fields that will be used for editing certain properties
     */
    @Nullable
    public Map<String, Validator<?>> getFieldValidators() {
        return fieldValidators;
    }

    /**
     * @return a map with validators for the result of bulk editing
     */
    @Nullable
    public List<Validator<?>> getModelValidators() {
        return modelValidators;
    }

    /**
     * @return whether or not the confirmation dialog should be displayed to the user before saving the changes
     */
    @Nullable
    public Boolean isUseConfirmDialog() {
        return useConfirmDialog;
    }

    /**
     * @return field sorter
     */
    @Nullable
    public FieldSorter getFieldSorter() {
        return fieldSorter;
    }

    /**
     * @return columns mode
     * @see ColumnsMode#ONE_COLUMN
     * @see ColumnsMode#TWO_COLUMNS
     */
    @Nullable
    public ColumnsMode getColumnsMode() {
        return columnsMode;
    }

    /**
     * @return a new instance of {@link BulkEditorWindow}
     */
    public BulkEditorWindow<E> create() {
        return handler.apply(this);
    }
}
