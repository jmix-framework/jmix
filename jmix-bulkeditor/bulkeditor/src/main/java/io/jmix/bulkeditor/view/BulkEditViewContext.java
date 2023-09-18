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

package io.jmix.bulkeditor.view;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.flowui.component.validation.Validator;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

public class BulkEditViewContext<V> {

    protected final MetaClass metaClass;
    protected final Collection<V> selectedItems;

    protected String exclude;
    protected List<String> includeProperties = Collections.emptyList();
    protected Map<String, Validator<?>> fieldValidators;
    protected List<Validator<V>> modelValidators;
    protected boolean useConfirmDialog;
    protected Function<List<MetaProperty>, Map<MetaProperty, Integer>> fieldSorter;

    public BulkEditViewContext(MetaClass metaClass, Collection<V> selectedItems) {
        checkNotNullArgument(metaClass);
        checkNotNullArgument(selectedItems);

        this.metaClass = metaClass;
        this.selectedItems = selectedItems;
    }

    public MetaClass getMetaClass() {
        return metaClass;
    }

    public Collection<V> getSelectedItems() {
        return selectedItems;
    }

    @Nullable
    public String getExclude() {
        return exclude;
    }

    public void setExclude(@Nullable String exclude) {
        this.exclude = exclude;
    }

    public List<String> getIncludeProperties() {
        return includeProperties != null
                ? Collections.unmodifiableList(includeProperties)
                : Collections.emptyList();
    }

    public void setIncludeProperties(@Nullable List<String> includeProperties) {
        this.includeProperties = includeProperties;
    }

    public Map<String, Validator<?>> getFieldValidators() {
        return fieldValidators != null
                ? Collections.unmodifiableMap(fieldValidators)
                : Collections.emptyMap();
    }

    public void setFieldValidators(@Nullable Map<String, Validator<?>> fieldValidators) {
        this.fieldValidators = fieldValidators;
    }

    public List<Validator<V>> getModelValidators() {
        return modelValidators != null
                ? Collections.unmodifiableList(modelValidators)
                : Collections.emptyList();
    }

    public void setModelValidators(@Nullable List<Validator<V>> modelValidators) {
        this.modelValidators = modelValidators;
    }

    public boolean isUseConfirmDialog() {
        return useConfirmDialog;
    }

    public void setUseConfirmDialog(boolean useConfirmDialog) {
        this.useConfirmDialog = useConfirmDialog;
    }

    @Nullable
    public Function<List<MetaProperty>, Map<MetaProperty, Integer>> getFieldSorter() {
        return fieldSorter;
    }

    public void setFieldSorter(@Nullable Function<List<MetaProperty>, Map<MetaProperty, Integer>> fieldSorter) {
        this.fieldSorter = fieldSorter;
    }
}
