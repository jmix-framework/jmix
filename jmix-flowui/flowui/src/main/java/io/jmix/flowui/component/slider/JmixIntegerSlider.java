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

package io.jmix.flowui.component.slider;

import com.vaadin.flow.component.slider.IntegerSlider;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.component.HasRequired;
import io.jmix.flowui.component.SupportsStatusChangeHandler;
import io.jmix.flowui.component.SupportsValidation;
import io.jmix.flowui.component.delegate.IntegerSliderDelegate;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.data.SupportsValueSource;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.exception.ValidationException;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.function.Consumer;

public class JmixIntegerSlider extends IntegerSlider implements SupportsValueSource<Integer>,
        SupportsValidation<Integer>, SupportsStatusChangeHandler<JmixIntegerSlider>, HasRequired,
        ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;

    protected IntegerSliderDelegate fieldDelegate;

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

        // The component always has min and max values, so validators for the initial
        // ones are registered here because the delegate doesn't exist in constructor.
        fieldDelegate.setMin(getMin());
        fieldDelegate.setMax(getMax());

        addValueChangeListener(__ -> validate());
    }

    protected IntegerSliderDelegate createFieldDelegate() {
        return applicationContext.getBean(IntegerSliderDelegate.class, this);
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
    public Registration addValidator(Validator<? super Integer> validator) {
        return fieldDelegate.addValidator(validator);
    }

    @Override
    public void executeValidators() throws ValidationException {
        fieldDelegate.executeValidators();
    }

    protected void validate() {
        fieldDelegate.updateInvalidState();
    }

    @Nullable
    @Override
    public ValueSource<Integer> getValueSource() {
        return fieldDelegate.getValueSource();
    }

    @Override
    public void setValueSource(@Nullable ValueSource<Integer> valueSource) {
        fieldDelegate.setValueSource(valueSource);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The component cannot represent an absent value, so {@code null} clears
     * the component, i.e. sets the value to the minimum value.
     *
     * @see #clear()
     */
    @Override
    public void setValue(@Nullable Integer value) {
        if (value == null) {
            clear();
        } else {
            super.setValue(value);
        }
    }

    @Override
    public void setRequired(boolean required) {
        HasRequired.super.setRequired(required);

        fieldDelegate.updateRequiredState();
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        super.setRequiredIndicatorVisible(requiredIndicatorVisible);

        fieldDelegate.updateRequiredState();
    }

    @Override
    public boolean isRequired() {
        return HasRequired.super.isRequired();
    }

    @Override
    public boolean isInvalid() {
        return fieldDelegate.isInvalid();
    }

    @Override
    public void setInvalid(boolean invalid) {
        // Method is called from constructor so bean can be null
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
    public void setStatusChangeHandler(@Nullable Consumer<StatusContext<JmixIntegerSlider>> handler) {
        fieldDelegate.setStatusChangeHandler(handler);
    }

    @Override
    public void setMax(Integer max) {
        super.setMax(max);

        // Method is called from constructor so bean can be null
        if (fieldDelegate != null) {
            fieldDelegate.setMax(max);
        }
    }

    @Override
    public void setMin(Integer min) {
        super.setMin(min);

        // Method is called from constructor so bean can be null
        if (fieldDelegate != null) {
            fieldDelegate.setMin(min);
        }
    }
}
