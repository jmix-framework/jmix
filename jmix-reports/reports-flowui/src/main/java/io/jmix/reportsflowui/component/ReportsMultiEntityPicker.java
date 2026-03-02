/*
 * Copyright 2026 Haulmont.
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

package io.jmix.reportsflowui.component;

import com.vaadin.flow.component.shared.HasPrefix;
import com.vaadin.flow.component.shared.HasSuffix;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.component.*;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.component.valuepicker.JmixValuePickerActionSupport;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.kit.component.valuepicker.ValuePickerActionSupport;
import io.jmix.flowui.kit.component.valuepicker.ValuePickerBase;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * INTERNAL! Will be removed in next releases.
 *
 * @param <E> entity type
 */
public class ReportsMultiEntityPicker<E> extends ValuePickerBase<ReportsMultiEntityPicker<E>, Collection<E>>
        implements EntityMultiPickerComponent<E>, SupportsValidation<Collection<E>>,
        SupportsStatusChangeHandler<ReportsMultiEntityPicker<E>>, HasRequired, HasPrefix, HasSuffix,
        ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;

    protected ReportsEntityPickerDelegate<ReportsMultiEntityPicker<E>, E> fieldDelegate;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        fieldDelegate = createFieldDelegate();
    }

    @Nullable
    @Override
    public ValueSource<Collection<E>> getValueSource() {
        return fieldDelegate.getValueSource();
    }

    @Override
    public void setValueSource(@Nullable ValueSource<Collection<E>> valueSource) {
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

    @Override
    public Registration addValidator(Validator<? super Collection<E>> validator) {
        return fieldDelegate.addValidator(validator);
    }

    @Override
    public void executeValidators() throws ValidationException {
        fieldDelegate.executeValidators();
    }

    @Override
    public void setInvalid(boolean invalid) {
        fieldDelegate.setInvalid(invalid);
    }

    @Override
    public void setStatusChangeHandler(@Nullable Consumer<StatusContext<ReportsMultiEntityPicker<E>>> handler) {
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
    protected String applyDefaultValueFormat(@Nullable Collection<E> value) {
        return fieldDelegate.applyDefaultValueFormat(value);
    }

    @Override
    protected boolean valueEquals(@Nullable Collection<E> value1, @Nullable Collection<E> value2) {
        return fieldDelegate.equalCollections(value1, value2);
    }

    @Override
    protected ValuePickerActionSupport createActionsSupport() {
        return applicationContext.getBean(JmixValuePickerActionSupport.class, this);
    }

    @SuppressWarnings("unchecked")
    protected ReportsEntityPickerDelegate<ReportsMultiEntityPicker<E>, E> createFieldDelegate() {
        return applicationContext.getBean(ReportsEntityPickerDelegate.class, this);
    }
}
