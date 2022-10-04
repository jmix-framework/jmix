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

import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.function.SerializableBiPredicate;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.component.EntityPickerComponent;
import io.jmix.flowui.component.HasRequired;
import io.jmix.flowui.component.LookupComponent;
import io.jmix.flowui.component.SupportsValidation;
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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

public class EntityComboBox<V> extends ComboBoxPicker<V>
        implements EntityPickerComponent<V>, LookupComponent<V>,
        SupportsValidation<V>, SupportsDataProvider<V>, SupportsItemsContainer<V>,
        SupportsFilterableItemsContainer<V>, HasRequired,
        ApplicationContextAware, InitializingBean {

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
    public <C> void setDataProvider(DataProvider<V, C> dataProvider, SerializableFunction<String, C> filterConverter) {
        // Method is called from a constructor so bean can be null
        if (dataViewDelegate != null) {
            dataViewDelegate.bind(dataProvider);
        }
        super.setDataProvider(dataProvider, filterConverter);
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
        setDataProvider(dataProvider, filterText ->
                item -> itemFilter.test(item, filterText));
    }

    protected String generateLabel(@Nullable V item) {
        // WARNING: copied from base class.
        if (item == null) {
            return "";
        }
        String label = getItemLabelGenerator().apply(item);
        if (label == null) {
            throw new IllegalStateException(String.format(
                    "Got 'null' as a label value for the item '%s'. "
                            + "'%s' instance may not return 'null' values",
                    item, ItemLabelGenerator.class.getSimpleName()));
        }
        return label;
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
