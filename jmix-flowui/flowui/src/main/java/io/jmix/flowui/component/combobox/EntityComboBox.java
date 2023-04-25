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

package io.jmix.flowui.component.combobox;

import com.vaadin.flow.component.combobox.dataview.ComboBoxDataView;
import com.vaadin.flow.component.combobox.dataview.ComboBoxLazyDataView;
import com.vaadin.flow.component.combobox.dataview.ComboBoxListDataView;
import com.vaadin.flow.data.provider.BackEndDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.InMemoryDataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.SerializableBiPredicate;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.component.*;
import io.jmix.flowui.component.delegate.DataViewDelegate;
import io.jmix.flowui.component.delegate.EntityFieldDelegate;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.component.valuepicker.JmixValuePickerActionSupport;
import io.jmix.flowui.data.SupportsDataProvider;
import io.jmix.flowui.data.SupportsFilterableItemsContainer;
import io.jmix.flowui.data.SupportsItemsContainer;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.data.items.ContainerDataProvider;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.kit.component.combobox.ComboBoxPicker;
import io.jmix.flowui.kit.component.valuepicker.ValuePickerActionSupport;
import io.jmix.flowui.model.CollectionContainer;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;

public class EntityComboBox<V> extends ComboBoxPicker<V>
        implements EntityPickerComponent<V>, LookupComponent<V>, SupportsValidation<V>, SupportsDataProvider<V>,
        SupportsItemsContainer<V>, SupportsFilterableItemsContainer<V>, SupportsStatusChangeHandler<EntityComboBox<V>>,
        HasRequired, ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;

    protected EntityFieldDelegate<EntityComboBox<V>, V, V> fieldDelegate;
    protected DataViewDelegate<EntityComboBox<V>, V> dataViewDelegate;

    protected MetaClass metaClass;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        initComponent();
    }

    private void initComponent() {
        fieldDelegate = createFieldDelegate();
        dataViewDelegate = createDataViewDelegate();

        fieldDelegate.addValueBindingChangeListener(event ->
                dataViewDelegate.valueBindingChanged(event));

        setItemLabelGenerator(fieldDelegate::applyDefaultValueFormat);
    }

    @Override
    public void setValue(@Nullable V value) {
        checkValueType(value);
        super.setValue(value);
    }

    @Override
    public void setValueFromClient(@Nullable V value) {
        checkValueType(value);
        setModelValue(value, true);
    }

    protected void checkValueType(@Nullable V value) {
        if (value != null) {
            fieldDelegate.checkValueType(value);
        }
    }

    @Override
    public boolean isInvalid() {
        return fieldDelegate.isInvalid();
    }

    @Override
    public void setInvalid(boolean invalid) {
        fieldDelegate.setInvalid(invalid);
    }

    @Nullable
    @Override
    public String getErrorMessage() {
        return fieldDelegate.getErrorMessage();
    }

    @Override
    public void setErrorMessage(@Nullable String errorMessage) {
        fieldDelegate.setErrorMessage(errorMessage);
    }

    @Override
    public void setStatusChangeHandler(@Nullable Consumer<StatusContext<EntityComboBox<V>>> handler) {
        fieldDelegate.setStatusChangeHandler(handler);
    }

    @Nullable
    @Override
    public String getRequiredMessage() {
        return fieldDelegate.getRequiredMessage();
    }

    @Override
    public void setRequiredMessage(@Nullable String requiredMessage) {
        fieldDelegate.setRequiredMessage(requiredMessage);
    }

    @Override
    public Registration addValidator(Validator<? super V> validator) {
        return fieldDelegate.addValidator(validator);
    }

    @Override
    public void executeValidators() throws ValidationException {
        fieldDelegate.executeValidators();
    }

    @Nullable
    @Override
    public ValueSource<V> getValueSource() {
        return fieldDelegate.getValueSource();
    }

    @Override
    public void setValueSource(@Nullable ValueSource<V> valueSource) {
        fieldDelegate.setValueSource(valueSource);
    }

    @Override
    public void setItems(CollectionContainer<V> container) {
        ItemFilter<V> itemFilter = (item, filterText) ->
                generateLabel(item).toLowerCase(getLocale())
                        .contains(filterText.toLowerCase(getLocale()));

        setItems(container, itemFilter);
    }

    @Override
    public void setItems(CollectionContainer<V> container,
                         SerializableBiPredicate<V, String> itemFilter) {
        ContainerDataProvider<V> dataProvider = new ContainerDataProvider<>(container);
        setItems(dataProvider, filterText ->
                item -> itemFilter.test(item, filterText));
    }

    @Override
    public ComboBoxListDataView<V> setItems(ItemFilter<V> itemFilter,
                                            ListDataProvider<V> listDataProvider) {
        bindDataProvider(listDataProvider);
        return super.setItems(itemFilter, listDataProvider);
    }

    @Override
    public ComboBoxListDataView<V> setItems(ListDataProvider<V> dataProvider) {
        bindDataProvider(dataProvider);
        return super.setItems(dataProvider);
    }

    @Override
    public ComboBoxLazyDataView<V> setItems(BackEndDataProvider<V, String> dataProvider) {
        bindDataProvider(dataProvider);
        return super.setItems(dataProvider);
    }

    @Override
    public ComboBoxDataView<V> setItems(DataProvider<V, String> dataProvider) {
        bindDataProvider(dataProvider);
        return super.setItems(dataProvider);
    }

    @Override
    public ComboBoxDataView<V> setItems(InMemoryDataProvider<V> inMemoryDataProvider,
                                        SerializableFunction<String, SerializablePredicate<V>> filterConverter) {
        bindDataProvider(inMemoryDataProvider);
        return super.setItems(inMemoryDataProvider, filterConverter);
    }

    protected void bindDataProvider(DataProvider<V, ?> dataProvider) {
        // One of binding methods is called from a constructor so bean can be null
        if (dataViewDelegate != null) {
            dataViewDelegate.bind(dataProvider);
        }
    }

    @Nullable
    @Override
    public MetaClass getMetaClass() {
        return fieldDelegate.getMetaClass();
    }

    @Override
    public void setMetaClass(@Nullable MetaClass metaClass) {
        fieldDelegate.setMetaClass(metaClass);
    }

    @Override
    public Set<V> getSelectedItems() {
        return isEmpty() ? Collections.emptySet() : Collections.singleton(getValue());
    }

    @SuppressWarnings("unchecked")
    protected EntityFieldDelegate<EntityComboBox<V>, V, V> createFieldDelegate() {
        return applicationContext.getBean(EntityFieldDelegate.class, this);
    }

    @SuppressWarnings("unchecked")
    protected DataViewDelegate<EntityComboBox<V>, V> createDataViewDelegate() {
        return applicationContext.getBean(DataViewDelegate.class, this);
    }

    @Override
    protected ValuePickerActionSupport createActionsSupport() {
        return new JmixValuePickerActionSupport(this);
    }
}
