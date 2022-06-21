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

package io.jmix.flowui.component.select;

import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.provider.DataProvider;
import io.jmix.flowui.component.HasRequired;
import io.jmix.flowui.component.delegate.DataViewDelegate;
import io.jmix.flowui.component.delegate.FieldDelegate;
import io.jmix.flowui.data.SupportsDataProvider;
import io.jmix.flowui.data.SupportsItemsContainer;
import io.jmix.flowui.data.SupportsValueSource;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.data.items.ContainerDataProvider;
import io.jmix.flowui.model.CollectionContainer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nullable;

public class JmixSelect<V> extends Select<V> implements SupportsValueSource<V>, HasRequired, SupportsDataProvider<V>,
        SupportsItemsContainer<V>, ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;

    protected FieldDelegate<JmixSelect<V>, V, V> fieldDelegate;
    protected DataViewDelegate<JmixSelect<V>, V> dataViewDelegate;

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

        setItemLabelGenerator(fieldDelegate::applyDefaultValueFormat);
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
    public void validate() {
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
    public void setDataProvider(DataProvider<V, ?> dataProvider) {
        // Method is called from a constructor so bean can be null
        if (dataViewDelegate != null) {
            dataViewDelegate.bind(dataProvider);
        }
        super.setDataProvider(dataProvider);
    }

    @Override
    public void setItems(CollectionContainer<V> container) {
        setItems(new ContainerDataProvider<>(container));
    }

    // TODO: gg, enum items

    @Override
    public void setRequired(boolean required) {
        HasRequired.super.setRequired(required);
    }

    @SuppressWarnings("unchecked")
    protected FieldDelegate<JmixSelect<V>, V, V> createFieldDelegate() {
        return applicationContext.getBean(FieldDelegate.class, this);
    }

    @SuppressWarnings("unchecked")
    protected DataViewDelegate<JmixSelect<V>, V> createDataViewDelegate() {
        return applicationContext.getBean(DataViewDelegate.class, this);
    }
}
