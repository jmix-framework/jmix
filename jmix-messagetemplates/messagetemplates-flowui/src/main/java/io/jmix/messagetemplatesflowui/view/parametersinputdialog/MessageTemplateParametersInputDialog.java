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

package io.jmix.messagetemplatesflowui.view.parametersinputdialog;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasValue;
import io.jmix.flowui.component.HasRequired;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.UiComponentsGenerator;
import io.jmix.flowui.component.formlayout.JmixFormLayout;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import io.jmix.messagetemplates.entity.MessageTemplateParameter;
import io.jmix.messagetemplatesflowui.MessageParameterLocalizationSupport;
import io.jmix.messagetemplatesflowui.MessageParameterResolver;
import io.jmix.messagetemplatesflowui.ObjectToStringConverter;
import io.jmix.messagetemplatesflowui.component.factory.MessageTemplateParameterGenerationContext;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ViewController("msgtmp_MessageTemplateParametersInputDialogView")
@ViewDescriptor("message-template-parameters-input-dialog.xml")
public class MessageTemplateParametersInputDialog extends StandardView {

    @ViewComponent
    protected JmixFormLayout form;

    @Autowired
    protected UiComponentsGenerator uiComponentsGenerator;
    @Autowired
    protected ObjectToStringConverter objectToStringConverter;
    @Autowired
    protected MessageParameterResolver messageParameterResolver;
    @Autowired
    protected MessageParameterLocalizationSupport messageParameterLocalizationSupport;
    @Autowired
    protected ViewValidation viewValidation;

    protected List<MessageTemplateParameter> templateParameters;

    protected Map<String, Object> parameters;
    protected Map<String, Component> aliasToComponentMap = new HashMap<>();

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        generateForm();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void generateForm() {
        form.removeAll();

        for (MessageTemplateParameter parameter : templateParameters) {
            MessageTemplateParameterGenerationContext context =
                    new MessageTemplateParameterGenerationContext(parameter);
            Component defaultValueComponent = uiComponentsGenerator.generate(context);

            Class<?> parameterClass = messageParameterResolver.resolveClass(parameter);
            if (parameterClass != null && parameter.getDefaultValue() != null
                    && defaultValueComponent instanceof HasValue hasValueComponent
                    && !Boolean.TRUE.equals(parameter.getDefaultDateIsCurrent())) {
                Object defaultValue = objectToStringConverter.convertFromString(
                        parameterClass, parameter.getDefaultValue()
                );

                UiComponentUtils.setValue(hasValueComponent, defaultValue);
            }

            if (Boolean.TRUE.equals(parameter.getRequired())
                    && defaultValueComponent instanceof HasRequired hasRequiredComponent) {
                hasRequiredComponent.setRequired(true);
            }

            defaultValueComponent.setVisible(!Boolean.TRUE.equals(parameter.getHidden()));

            if (defaultValueComponent instanceof HasLabel hasLabelComponent) {
                String localizedName = messageParameterLocalizationSupport.getLocalizedName(parameter);
                hasLabelComponent.setLabel(localizedName);
            }

            if (MapUtils.isNotEmpty(parameters)
                    && defaultValueComponent instanceof HasValue hasValueComponent) {
                UiComponentUtils.setValue(hasValueComponent, parameters.get(parameter.getAlias()));
            }

            aliasToComponentMap.put(parameter.getAlias(), defaultValueComponent);

            form.add(defaultValueComponent);
        }
    }

    @Subscribe("saveAndCloseButton")
    public void onSaveAndCloseButtonClick(ClickEvent<JmixButton> event) {
        ValidationErrors validationErrors = viewValidation.validateUiComponents(form);
        if (validationErrors.isEmpty()) {
            parameters = collectParameters();
            close(StandardOutcome.SAVE);
        } else {
            viewValidation.showValidationErrors(validationErrors);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Map<String, Object> collectParameters() {
        HashMap<String, Object> result = new HashMap<>();

        for (Map.Entry<String, Component> entry : aliasToComponentMap.entrySet()) {
            result.put(entry.getKey(), UiComponentUtils.getValue(((HasValue) entry.getValue())));
        }

        return result;
    }

    public void setTemplateParameters(List<MessageTemplateParameter> templateParameters) {
        this.templateParameters = templateParameters;
    }

    public Map<String, Object> getParameters() {
        return parameters == null
                ? Collections.emptyMap()
                : parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}
