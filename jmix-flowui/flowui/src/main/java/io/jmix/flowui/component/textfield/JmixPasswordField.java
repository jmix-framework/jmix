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

package io.jmix.flowui.component.textfield;

import com.google.common.base.Strings;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.component.HasRequired;
import io.jmix.flowui.component.SupportsValidation;
import io.jmix.flowui.component.SupportsStatusChangeHandler;
import io.jmix.flowui.component.delegate.FieldDelegate;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.data.SupportsValueSource;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.kit.component.HasTitle;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import org.springframework.lang.Nullable;
import java.util.function.Consumer;

public class JmixPasswordField extends PasswordField implements SupportsValueSource<String>,
        SupportsValidation<String>, SupportsStatusChangeHandler<JmixPasswordField>, HasRequired,
        HasTitle, ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;

    protected FieldDelegate<JmixPasswordField, String, String> fieldDelegate;

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
    }

    @SuppressWarnings("unchecked")
    protected FieldDelegate<JmixPasswordField, String, String> createFieldDelegate() {
        return applicationContext.getBean(FieldDelegate.class, this);
    }

    @Override
    public void setRequired(boolean required) {
        super.setRequired(required);

        fieldDelegate.updateRequiredState();
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        super.setRequiredIndicatorVisible(requiredIndicatorVisible);

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
    public Registration addValidator(Validator<? super String> validator) {
        return fieldDelegate.addValidator(validator);
    }

    @Override
    public void executeValidators() throws ValidationException {
        fieldDelegate.executeValidators();
    }

    @Nullable
    @Override
    public ValueSource<String> getValueSource() {
        return fieldDelegate.getValueSource();
    }

    @Override
    public void setValueSource(@Nullable ValueSource<String> valueSource) {
        fieldDelegate.setValueSource(valueSource);
    }

    @Override
    public boolean isInvalid() {
        return fieldDelegate.isInvalid();
    }

    @Override
    public void setInvalid(boolean invalid) {
        // Method is called from constructor so delegate can be null
        if (fieldDelegate != null) {
            fieldDelegate.setInvalid(invalid);
        } else {
            super.setInvalid(invalid);
        }
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
    public void setStatusChangeHandler(@Nullable Consumer<StatusContext<JmixPasswordField>> handler) {
        fieldDelegate.setStatusChangeHandler(handler);
    }

    @Override
    public void setValue(String value) {
        super.setValue(Strings.nullToEmpty(value));
    }
}
