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

import com.vaadin.flow.component.slider.DecimalSlider;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.component.HasRequired;
import io.jmix.flowui.component.SupportsStatusChangeHandler;
import io.jmix.flowui.component.SupportsValidation;
import io.jmix.flowui.component.delegate.DecimalSliderDelegate;
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

public class JmixDecimalSlider extends DecimalSlider implements SupportsValueSource<Double>,
        SupportsValidation<Double>, SupportsStatusChangeHandler<JmixDecimalSlider>, HasRequired,
        ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;

    protected DecimalSliderDelegate fieldDelegate;

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

    protected DecimalSliderDelegate createFieldDelegate() {
        return applicationContext.getBean(DecimalSliderDelegate.class, this);
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
    public Registration addValidator(Validator<? super Double> validator) {
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
    public ValueSource<Double> getValueSource() {
        return fieldDelegate.getValueSource();
    }

    @Override
    public void setValueSource(@Nullable ValueSource<Double> valueSource) {
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
    public void setValue(@Nullable Double value) {
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
    public void setStatusChangeHandler(@Nullable Consumer<StatusContext<JmixDecimalSlider>> handler) {
        fieldDelegate.setStatusChangeHandler(handler);
    }

    @Override
    public void setMax(Double max) {
        super.setMax(max);

        // Method is called from constructor so bean can be null
        if (fieldDelegate != null) {
            fieldDelegate.setMax(max);
        }
    }

    @Override
    public void setMin(Double min) {
        super.setMin(min);

        // Method is called from constructor so bean can be null
        if (fieldDelegate != null) {
            fieldDelegate.setMin(min);
        }
    }
}
