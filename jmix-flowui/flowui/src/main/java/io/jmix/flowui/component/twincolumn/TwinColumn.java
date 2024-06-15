/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowui.component.twincolumn;

import com.vaadin.flow.data.provider.DataChangeEvent;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.InMemoryDataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.Messages;
import io.jmix.core.MetadataTools;
import io.jmix.flowui.component.HasRequired;
import io.jmix.flowui.component.SupportsValidation;
import io.jmix.flowui.component.delegate.DataViewDelegate;
import io.jmix.flowui.component.delegate.TwinColumnDelegate;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.data.*;
import io.jmix.flowui.data.items.InMemoryDataProviderWrapper;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.kit.component.twincolumn.JmixTwinColumn;
import io.jmix.flowui.kit.component.twincolumn.TwinColumnDataView;
import io.jmix.flowui.kit.component.twincolumn.TwinColumnListDataView;
import io.jmix.flowui.model.CollectionContainer;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;

import java.util.Collection;

public class TwinColumn<V> extends JmixTwinColumn<V> implements
        SupportsItemsContainer<V>, SupportsValueSource<Collection<V>>, SupportsItemsEnum<V>,
        SupportsDataProvider<V>, SupportsValidation<Collection<V>>,
        HasRequired, ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;
    protected Messages messages;
    protected MetadataTools metadataTools;

    protected TwinColumnDelegate<TwinColumn<V>, Collection<V>, Collection<V>> fieldDelegate;
    protected DataViewDelegate<TwinColumn<V>, V> dataViewDelegate;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        autowireDependencies();
        createValueChangeListener();
        initComponentMessages();

        setItemLabelGenerator(fieldDelegate::applyDefaultValueFormat);
    }

    @Override
    public void setValueSource(@Nullable ValueSource<Collection<V>> valueSource) {
        fieldDelegate.setValueSource(valueSource);
    }

    @Override
    public ValueSource<Collection<V>> getValueSource() {
        return fieldDelegate.getValueSource();
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
    public TwinColumnDataView<V> setItems(DataProvider<V, Void> dataProvider) {
        bindDataProvider(dataProvider);
        dataProvider.addDataProviderListener(this::onDataChange);
        return getGenericDataView();
    }

    @Override
    public TwinColumnDataView<V> setItems(InMemoryDataProvider<V> dataProvider) {
        InMemoryDataProviderWrapper<V> wrapper = new InMemoryDataProviderWrapper<>(dataProvider);
        return setItems(wrapper);
    }

    @Override
    public TwinColumnListDataView<V> setItems(ListDataProvider<V> dataProvider) {
        bindDataProvider(dataProvider);

        TwinColumnListDataView<V> twinColumnListDataView = super.setItems(dataProvider);
        updateInvalidState();
        return twinColumnListDataView;
    }

    @Nullable
    @Override
    public DataProvider<V, ?> getDataProvider() {
        return dataViewDelegate.getDataProvider();
    }

    @Override
    public Registration addValidator(Validator<? super Collection<V>> validator) {
        return fieldDelegate.addValidator(validator);
    }

    @Override
    public void executeValidators() throws ValidationException {
        fieldDelegate.executeValidators();
    }

    @Override
    public void setInvalid(boolean invalid) {
        if (fieldDelegate != null) {
            fieldDelegate.setInvalid(invalid);
        }
    }

    @Override
    public boolean isInvalid() {
        return fieldDelegate.isInvalid();
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
    public void setRequired(boolean required) {
        HasRequired.super.setRequired(required);

        updateInvalidState();
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        super.setRequiredIndicatorVisible(requiredIndicatorVisible);

        updateInvalidState();
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() || CollectionUtils.isEmpty(getValue());
    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return super.isRequiredIndicatorVisible();
    }

    @Override
    public void setErrorMessage(@Nullable String errorMessage) {
        fieldDelegate.setErrorMessage(errorMessage);
    }

    @Override
    public String getErrorMessage() {
        return fieldDelegate.getErrorMessage();
    }

    @Override
    public void setValue(Collection<V> value) {
        super.setValue(value);

        updateValueInternal(value);
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);

        updateInvalidState();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        updateInvalidState();
    }

    @Override
    public void setSelectAllButtonsVisible(Boolean selectAllButtonsVisible) {
        super.setSelectAllButtonsVisible(selectAllButtonsVisible);

        if (selectAllItems != null) {
            selectAllItems.setTooltipText(messages.getMessage("twinColumn.selectAllItems.tooltip"));
            deselectAllItems.setTooltipText(messages.getMessage("twinColumn.deselectAllItems.tooltip"));
        }
    }

    protected void autowireDependencies() {
        messages = applicationContext.getBean(Messages.class);
        metadataTools = applicationContext.getBean(MetadataTools.class);

        fieldDelegate = createFieldDelegate();
        dataViewDelegate = createDataViewDelegate();
    }

    protected void updateInvalidState() {
        if (fieldDelegate != null) {
            fieldDelegate.updateInvalidState();
        }
    }

    protected void initComponentMessages() {
        selectItems.setTooltipText(messages.getMessage("twinColumn.selectItems.tooltip"));
        deselectItems.setTooltipText(messages.getMessage("twinColumn.deselectItems.tooltip"));
    }

    protected void createValueChangeListener() {
        addValueChangeListener((ValueChangeListener<ComponentValueChangeEvent<JmixTwinColumn<V>, Collection<V>>>)
                event -> updateInvalidState());
    }

    @Override
    protected void onDataChange(DataChangeEvent<V> event) {
        super.onDataChange(event);

        updateInvalidState();
    }

    protected void bindDataProvider(DataProvider<V, ?> dataProvider) {
        // One of binding methods is called from a constructor so bean can be null
        if (dataViewDelegate != null) {
            dataViewDelegate.bind(dataProvider);
        }
    }

    protected TwinColumnDelegate<TwinColumn<V>, Collection<V>, Collection<V>>  createFieldDelegate() {
        return applicationContext.getBean(TwinColumnDelegate.class, this);
    }

    protected DataViewDelegate<TwinColumn<V>, V> createDataViewDelegate() {
        return applicationContext.getBean(DataViewDelegate.class, this);
    }

    @Override
    protected String applyColumnItemLabelFormat(V value) {
        return metadataTools.format(value);
    }
}