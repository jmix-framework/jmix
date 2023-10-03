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

package io.jmix.bulkeditor.view.builder;

import io.jmix.bulkeditor.view.BulkEditView;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.View;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A builder that creates a new {@link BulkEditView} with defined parameters.
 *
 * @param <E> item type
 */
public class BulkEditorBuilder<E> {

    protected final MetaClass metaClass;
    protected final Collection<E> entities;
    protected final Function<BulkEditorBuilder<E>, DialogWindow<BulkEditView<E>>> handler;

    protected ListDataComponent<E> listDataComponent;

    protected String exclude;
    protected List<String> includeProperties = Collections.emptyList();
    protected Map<String, Validator<?>> fieldValidators;
    protected List<Validator<E>> modelValidators;
    protected boolean useConfirmDialog = true;
    protected Function<List<MetaProperty>, Map<MetaProperty, Integer>> fieldSorter;
    protected View<?> origin;

    public BulkEditorBuilder(BulkEditorBuilder<E> builder) {
        this.metaClass = builder.metaClass;
        this.handler = builder.handler;
        this.entities = builder.entities;

        this.listDataComponent = builder.listDataComponent;

        this.exclude = builder.exclude;
        this.includeProperties = builder.includeProperties;
        this.fieldValidators = builder.fieldValidators;
        this.modelValidators = builder.modelValidators;
        this.useConfirmDialog = builder.useConfirmDialog;
        this.fieldSorter = builder.fieldSorter;
        this.origin = builder.origin;
    }

    public BulkEditorBuilder(MetaClass metaClass,
                             Collection<E> entities,
                             View<?> origin,
                             Function<BulkEditorBuilder<E>, DialogWindow<BulkEditView<E>>> handler) {
        this.metaClass = metaClass;
        this.entities = entities;
        this.handler = handler;
        this.origin = origin;
    }

    /**
     * Sets the list data component that displays the items to be edited.
     *
     * @param listDataComponent the list data component to be used
     * @return this builder
     */
    public BulkEditorBuilder<E> withListDataComponent(ListDataComponent<E> listDataComponent) {
        this.listDataComponent = listDataComponent;
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
     * Sets the entity attributes to be included to bulk editor view.
     * If set, other attributes will be ignored.
     *
     * @param includeProperties the entity attributes to be included to bulk editor view
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
    public BulkEditorBuilder<E> withModelValidators(List<Validator<E>> modelValidators) {
        this.modelValidators = modelValidators;
        return this;
    }

    /**
     * Sets whether the confirmation dialog should be displayed to
     * the user before saving the changes. The default value is true.
     *
     * @param useConfirmDialog whether the confirmation dialog should be displayed
     * @return this builder
     */
    public BulkEditorBuilder<E> withUseConfirmDialog(boolean useConfirmDialog) {
        this.useConfirmDialog = useConfirmDialog;
        return this;
    }

    /**
     * Sets field sorter function that allows you to sort fields by custom logic.
     *
     * @param fieldSorter field sorter function
     * @return this builder
     */
    public BulkEditorBuilder<E> withFieldSorter(Function<List<MetaProperty>, Map<MetaProperty, Integer>> fieldSorter) {
        this.fieldSorter = fieldSorter;
        return this;
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
     * @return the list data component that displays the items to be edited
     */
    @Nullable
    public ListDataComponent<E> getListDataComponent() {
        return listDataComponent;
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
     * @return the entity attributes to be included to bulk editor view
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
     * @return a list with validators for the result of bulk editing
     */
    @Nullable
    public List<Validator<E>> getModelValidators() {
        return modelValidators;
    }

    /**
     * @return whether the confirmation dialog should be displayed to the user before saving the changes
     */
    public boolean isUseConfirmDialog() {
        return useConfirmDialog;
    }

    /**
     * @return field sorter function
     */
    @Nullable
    public Function<List<MetaProperty>, Map<MetaProperty, Integer>> getFieldSorter() {
        return fieldSorter;
    }

    /**
     * @return origin view
     */
    public View<?> getOrigin() {
        return origin;
    }

    /**
     * Builds an instance of DialogWindow for {@link BulkEditView}
     *
     * @return a new instance of DialogWindow for {@link BulkEditView}
     */
    public DialogWindow<BulkEditView<E>> build() {
        return handler.apply(this);
    }

    /**
     * Builds and opens an instance of DialogWindow for {@link BulkEditView}
     *
     * @return a new instance of DialogWindow for {@link BulkEditView}
     */
    public DialogWindow<BulkEditView<E>> open() {
        DialogWindow<BulkEditView<E>> dialogWindow = build();
        dialogWindow.open();
        return dialogWindow;
    }
}
