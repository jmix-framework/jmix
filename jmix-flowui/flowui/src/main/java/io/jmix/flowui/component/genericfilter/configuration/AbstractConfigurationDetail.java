/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.component.genericfilter.configuration;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.FlowuiComponentProperties;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.SupportsTypedValue.TypedValueChangeEvent;
import io.jmix.flowui.component.SupportsValidation;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.view.MessageBundle;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import static io.jmix.flowui.component.genericfilter.FilterUtils.generateConfigurationId;

public abstract class AbstractConfigurationDetail extends Composite<FormLayout>
        implements ApplicationContextAware, InitializingBean, SupportsValidation {

    protected static final String NAME_FIELD_ID = "nameField";
    protected static final String GENERATED_ID_FIELD_ID = "generatedIdField";
    protected static final String CONFIGURATION_ID_FIELD_ID = "configurationIdField";

    protected ApplicationContext applicationContext;
    protected UiComponents uiComponents;
    protected MessageBundle messageBundle;
    protected FlowuiComponentProperties flowuiComponentProperties;

    protected TypedTextField<String> nameField;
    protected TypedTextField<String> configurationIdField;
    protected JmixCheckbox generatedIdField;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    protected FormLayout initContent() {
        FormLayout formLayout = super.initContent();

        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("40em", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP)
        );
        formLayout.addClassName("p-s");

        return formLayout;
    }

    @Override
    public void afterPropertiesSet() {
        autowireDependencies();
    }

    protected void autowireDependencies() {
        uiComponents = applicationContext.getBean(UiComponents.class);
        messageBundle = applicationContext.getBean(MessageBundle.class);
        flowuiComponentProperties = applicationContext.getBean(FlowuiComponentProperties.class);
    }

    //hook to be implemented
    protected abstract void initFields();

    protected void createNameField() {
        nameField = createTextField();

        nameField.setId(NAME_FIELD_ID);
        nameField.setRequired(true);
        nameField.setWidthFull();

        nameField.addTypedValueChangeListener(this::nameFieldValueChangeListener);
    }

    protected void createGeneratedIdField() {
        generatedIdField = createCheckbox();

        generatedIdField.setId(GENERATED_ID_FIELD_ID);
        generatedIdField.setValue(true);
        generatedIdField.setWidthFull();

        generatedIdField.addValueChangeListener(this::generatedIdFieldValueChangeListener);
    }

    protected void createConfigurationIdField() {
        configurationIdField = createTextField();

        configurationIdField.setId(CONFIGURATION_ID_FIELD_ID);
        configurationIdField.setRequired(true);
        configurationIdField.setWidthFull();
    }

    protected void nameFieldValueChangeListener(TypedValueChangeEvent<TypedTextField<String>, String> event) {
        if (generatedIdField.getValue()) {
            configurationIdField.setTypedValue(generateConfigurationId(event.getValue()));
        }
    }

    protected void generatedIdFieldValueChangeListener(ComponentValueChangeEvent<Checkbox, Boolean> event) {
        configurationIdField.setEnabled(!event.getValue());

        if (event.getValue()) {
            configurationIdField.setTypedValue(generateConfigurationId(nameField.getTypedValue()));
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Registration addValidator(Validator validator) {
        throw new UnsupportedOperationException(
                String.format("Validator can't be added to %s", getClass().getSimpleName()));
    }

    @Override
    public void executeValidators() throws ValidationException {
        nameField.executeValidators();
        configurationIdField.executeValidators();
    }

    @Override
    public boolean isInvalid() {
        return nameField.isInvalid() || configurationIdField.isInvalid();
    }

    @Override
    public void setInvalid(boolean invalid) {
        nameField.setInvalid(invalid);
        configurationIdField.setInvalid(invalid);
    }

    protected JmixCheckbox createCheckbox() {
        return uiComponents.create(JmixCheckbox.class);
    }

    protected TypedTextField<String> createTextField() {
        //noinspection unchecked
        return uiComponents.create(TypedTextField.class);
    }
}
