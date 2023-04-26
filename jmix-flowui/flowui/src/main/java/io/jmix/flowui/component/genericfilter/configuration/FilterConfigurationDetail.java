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

import com.vaadin.flow.component.formlayout.FormLayout;

import javax.annotation.Nullable;
import java.util.Optional;

public class FilterConfigurationDetail extends AbstractConfigurationDetail {

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        initFields();
    }

    @Override
    protected void initFields() {
        initNameField();
        initConfigurationIdField();
        initGeneratedIdField();
    }

    protected void initNameField() {
        createNameField();

        getContent().addFormItem(nameField,
                messageBundle.getMessage("filterConfigurationDetail.nameField.label"));
    }

    protected void initConfigurationIdField() {
        createConfigurationIdField();

        configurationIdField.setEnabled(false);

        String label = messageBundle.getMessage("filterConfigurationDetail.configurationIdField.label");
        FormLayout.FormItem formItem = getContent().addFormItem(configurationIdField, label);

        formItem.setVisible(flowuiComponentProperties.isFilterShowConfigurationIdField());
    }

    protected void initGeneratedIdField() {
        createGeneratedIdField();

        FormLayout.FormItem formItem = getContent().addFormItem(generatedIdField,
                messageBundle.getMessage("filterConfigurationDetail.generatedIdField.label"));

        formItem.setVisible(flowuiComponentProperties.isFilterShowConfigurationIdField());
    }

    public String getConfigurationId() {
        return Optional.ofNullable(configurationIdField.getTypedValue())
                .orElse("");
    }

    public void setConfigurationId(@Nullable String configurationId) {
        if (configurationId != null) {
            configurationIdField.setTypedValue(configurationId);
            configurationIdField.setEnabled(false);
        }
    }

    public String getConfigurationName() {
        return Optional.ofNullable(nameField.getTypedValue())
                .orElse("");
    }

    public void setConfigurationName(@Nullable String configurationName) {
        if (configurationName != null) {
            nameField.setTypedValue(configurationName);
        }
    }
}
