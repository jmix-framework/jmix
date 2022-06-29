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

package io.jmix.flowui.component.radiobuttongroup;

import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.component.HasRequired;
import io.jmix.flowui.component.SupportsValidation;
import io.jmix.flowui.component.delegate.DataViewDelegate;
import io.jmix.flowui.component.delegate.FieldDelegate;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.data.*;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.model.CollectionContainer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nullable;

public class JmixRadioButtonGroup<V> extends RadioButtonGroup<V> implements SupportsValueSource<V>,
        SupportsDataProvider<V>, SupportsItemsContainer<V>, SupportsItemsEnum<V>, SupportsValidation<V>,
        HasRequired, ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;

    protected FieldDelegate<JmixRadioButtonGroup<V>, V, V> fieldDelegate;
    protected DataViewDelegate<JmixRadioButtonGroup<V>, V> dataViewDelegate;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initComponent();
    }

    protected void initComponent() {
        fieldDelegate = createFieldDelegate();
        dataViewDelegate = createDataViewDelegate();

        fieldDelegate.addValueBindingChangeListener(event ->
                dataViewDelegate.valueBindingChanged(event));

        setItemLabelGenerator(fieldDelegate::applyDefaultValueFormat);

        attachValueChangeListener();
    }

    protected void attachValueChangeListener() {
        addValueChangeListener(e -> validate());
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
    public void setItems(CollectionContainer<V> container) {
        dataViewDelegate.setItems(container);
    }

    @Override
    public void setItems(Class<V> itemsEnum) {
        dataViewDelegate.setItems(itemsEnum);
    }

    @Override
    public void setDataProvider(DataProvider<V, ?> dataProvider) {
        // Method is called from a constructor so bean can be null
        if (dataViewDelegate != null) {
            dataViewDelegate.bind(dataProvider);
        }
        super.setDataProvider(dataProvider);
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
    protected void validate() {
        isInvalid();
    }

    @Override
    public boolean isInvalid() {
        return fieldDelegate.isInvalid();
    }

    @Override
    public void setInvalid(boolean invalid) {
        fieldDelegate.setInvalid(invalid);
    }

    @Override
    public Registration addValidator(Validator<? super V> validator) {
        return fieldDelegate.addValidator(validator);
    }

    @Override
    public void executeValidators() throws ValidationException {
        fieldDelegate.executeValidators();
    }

    protected FieldDelegate<JmixRadioButtonGroup<V>, V, V> createFieldDelegate() {
        return applicationContext.getBean(FieldDelegate.class, this);
    }

    protected DataViewDelegate<JmixRadioButtonGroup<V>, V> createDataViewDelegate() {
        return applicationContext.getBean(DataViewDelegate.class, this);
    }
}
