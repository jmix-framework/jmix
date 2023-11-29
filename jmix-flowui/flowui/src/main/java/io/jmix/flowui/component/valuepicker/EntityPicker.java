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

package io.jmix.flowui.component.valuepicker;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.shared.HasPrefix;
import com.vaadin.flow.component.shared.HasSuffix;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.component.*;
import io.jmix.flowui.component.delegate.EntityFieldDelegate;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.kit.component.valuepicker.CustomValueSetEvent;
import io.jmix.flowui.kit.component.valuepicker.ValuePickerActionSupport;
import io.jmix.flowui.kit.component.valuepicker.ValuePickerBase;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import org.springframework.lang.Nullable;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;

public class EntityPicker<V> extends ValuePickerBase<EntityPicker<V>, V>
        implements EntityPickerComponent<V>, LookupComponent<V>, SupportsValidation<V>,
        SupportsStatusChangeHandler<EntityPicker<V>>, HasRequired, HasPrefix, HasSuffix,
        ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;

    protected EntityFieldDelegate<EntityPicker<V>, V, V> fieldDelegate;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        fieldDelegate = createFieldDelegate();
    }

    @Override
    public void setValue(@Nullable V value) {
        fieldDelegate.checkValueType(value);
        super.setValue(value);
    }

    @Override
    public void setValueFromClient(@Nullable V value) {
        fieldDelegate.checkValueType(value);
        super.setValueFromClient(value);
    }

    @Override
    protected boolean valueEquals(V value1, V value2) {
        return EntityValues.propertyValueEquals(value1, value2);
    }

    @Override
    protected String applyDefaultValueFormat(@Nullable V value) {
        return fieldDelegate.applyDefaultValueFormat(value);
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
    public void setStatusChangeHandler(@Nullable Consumer<StatusContext<EntityPicker<V>>> handler) {
        fieldDelegate.setStatusChangeHandler(handler);
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        super.setRequiredIndicatorVisible(requiredIndicatorVisible);

        fieldDelegate.updateInvalidState();
    }

    @Override
    public void setRequired(boolean required) {
        HasRequired.super.setRequired(required);

        fieldDelegate.updateInvalidState();
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

    @Override
    protected void validate() {
        fieldDelegate.updateInvalidState();
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

    @Nullable
    @Override
    public MetaClass getMetaClass() {
        return fieldDelegate.getMetaClass();
    }

    @Override
    public void setMetaClass(@Nullable MetaClass metaClass) {
        fieldDelegate.setMetaClass(metaClass);
    }

    public boolean isAllowCustomValue() {
        return super.isAllowCustomValueBoolean();
    }

    @Override
    public void setAllowCustomValue(boolean allowCustomValue) {
        super.setAllowCustomValue(allowCustomValue);
    }

    @Override
    public Registration addCustomValueSetListener(ComponentEventListener<CustomValueSetEvent<EntityPicker<V>, V>> listener) {
        return super.addCustomValueSetListener(listener);
    }

    @Override
    public Set<V> getSelectedItems() {
        return isEmpty() ? Collections.emptySet() : Collections.singleton(getValue());
    }

    @SuppressWarnings("unchecked")
    protected EntityFieldDelegate<EntityPicker<V>, V, V> createFieldDelegate() {
        return applicationContext.getBean(EntityFieldDelegate.class, this);
    }

    @Override
    protected ValuePickerActionSupport createActionsSupport() {
        return new JmixValuePickerActionSupport(this);
    }
}
