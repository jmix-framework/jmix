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

import com.vaadin.flow.component.shared.HasPrefix;
import com.vaadin.flow.component.shared.HasSuffix;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.component.HasRequired;
import io.jmix.flowui.component.PickerComponent;
import io.jmix.flowui.component.SupportsValidation;
import io.jmix.flowui.component.SupportsStatusChangeHandler;
import io.jmix.flowui.component.delegate.FieldDelegate;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.valuepicker.ValuePickerActionSupport;
import io.jmix.flowui.kit.component.valuepicker.MultiValuePicker;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.function.Consumer;

public class JmixMultiValuePicker<V> extends MultiValuePicker<V>
        implements PickerComponent<Collection<V>>, SupportsValidation<Collection<V>>,
        SupportsStatusChangeHandler<JmixMultiValuePicker<V>>, HasRequired, HasPrefix, HasSuffix,
        ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;

    protected FieldDelegate<JmixMultiValuePicker<V>, Collection<V>, Collection<V>> fieldDelegate;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        fieldDelegate = createFieldDelegate();
    }

    @Override
    protected String applyDefaultValueFormat(@Nullable Collection<V> value) {
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

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        super.setRequiredIndicatorVisible(requiredIndicatorVisible);

        fieldDelegate.updateRequiredState();
    }

    @Override
    public void setRequired(boolean required) {
        HasRequired.super.setRequired(required);

        fieldDelegate.updateRequiredState();
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
    public Registration addValidator(Validator<? super Collection<V>> validator) {
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
    public String getErrorMessage() {
        return fieldDelegate.getErrorMessage();
    }

    @Override
    public void setErrorMessage(@Nullable String errorMessage) {
        fieldDelegate.setErrorMessage(errorMessage);
    }

    @Override
    public void setStatusChangeHandler(@Nullable Consumer<StatusContext<JmixMultiValuePicker<V>>> handler) {
        fieldDelegate.setStatusChangeHandler(handler);
    }

    @Nullable
    @Override
    public ValueSource<Collection<V>> getValueSource() {
        return fieldDelegate.getValueSource();
    }

    @Override
    public void setValueSource(@Nullable ValueSource<Collection<V>> valueSource) {
        fieldDelegate.setValueSource(valueSource);
    }

    @Override
    public void addAction(Action action, int index) {
        super.addAction(action, index);
    }

    @SuppressWarnings("unchecked")
    protected FieldDelegate<JmixMultiValuePicker<V>, Collection<V>, Collection<V>> createFieldDelegate() {
        return applicationContext.getBean(FieldDelegate.class, this);
    }

    @Override
    protected ValuePickerActionSupport createActionsSupport() {
        return applicationContext.getBean(JmixValuePickerActionSupport.class, this);
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() || CollectionUtils.isEmpty(getValue());
    }
}
