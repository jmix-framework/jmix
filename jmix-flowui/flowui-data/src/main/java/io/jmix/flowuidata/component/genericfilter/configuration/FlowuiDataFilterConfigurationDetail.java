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

package io.jmix.flowuidata.component.genericfilter.configuration;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.checkbox.Checkbox;
import io.jmix.core.AccessManager;
import io.jmix.core.MessageTools;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.genericfilter.configuration.AbstractConfigurationDetail;
import io.jmix.flowui.data.value.ContainerValueSource;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowuidata.accesscontext.FlowuiGenericFilterModifyGlobalConfigurationContext;
import io.jmix.flowuidata.entity.FilterConfiguration;
import org.apache.commons.lang3.StringUtils;

public class FlowuiDataFilterConfigurationDetail extends AbstractConfigurationDetail {

    protected static final String DEFAULT_FOR_ME_FIELD_ID = "defaultForMeField";
    protected static final String AVAILABLE_FOR_ALL_USERS_FIELD_ID = "availableForAllUsersField";
    protected static final String DEFAULT_FOR_ALL_USERS_FIELD_ID = "defaultForAllUsersField";

    protected CurrentAuthentication currentAuthentication;
    protected AccessManager accessManager;
    protected MessageTools messageTools;

    protected JmixCheckbox defaultForAllUsersField;
    protected JmixCheckbox availableForAllUsersField;

    protected boolean defaultForMeFieldVisible = true;
    protected boolean viewReadOnly = false;

    protected InstanceContainer<FilterConfiguration> configurationDc;

    @Override
    protected void autowireDependencies() {
        super.autowireDependencies();
        currentAuthentication = applicationContext.getBean(CurrentAuthentication.class);
        accessManager = applicationContext.getBean(AccessManager.class);
        messageTools = applicationContext.getBean(MessageTools.class);
    }

    public InstanceContainer<FilterConfiguration> getConfigurationDc() {
        return configurationDc;
    }

    public void setConfigurationDc(InstanceContainer<FilterConfiguration> configurationDc) {
        this.configurationDc = configurationDc;
    }

    public void setDefaultForMeFieldVisible(boolean visible) {
        defaultForMeFieldVisible = visible;
    }

    public boolean isViewReadOnly() {
        return viewReadOnly;
    }

    public void initUsername() {
        FilterConfiguration editedConfigurationModel = configurationDc.getItem();

        if (availableForAllUsersField.getValue()) {
            editedConfigurationModel.setUsername(null);
        } else {
            editedConfigurationModel.setUsername(currentAuthentication.getUser().getUsername());
        }
    }

    @Override
    public void initFields() {
        createFields();
        initFirstConfigurationFormRow();
        initSecondConfigurationFormRow();
        initThirdConfigurationFormRow();
    }

    protected void createFields() {
        createNameField();

        availableForAllUsersField = createCheckbox();
        defaultForAllUsersField = createCheckbox();

        createGeneratedIdField();
        createConfigurationIdField();
    }

    protected void initFirstConfigurationFormRow() {
        initNameField();
        initConfigurationIdField();
    }

    protected void initSecondConfigurationFormRow() {
        FlowuiGenericFilterModifyGlobalConfigurationContext globalFilterContext =
                new FlowuiGenericFilterModifyGlobalConfigurationContext();
        accessManager.applyRegisteredConstraints(globalFilterContext);
        boolean allowGlobalFilters = globalFilterContext.isPermitted();

        boolean isAvailableForAll = StringUtils.isEmpty(configurationDc.getItem().getUsername());

        initAvailableForAllUsersField(allowGlobalFilters, isAvailableForAll);
        initDefaultForAllUsersField(allowGlobalFilters, isAvailableForAll);

        viewReadOnly = isAvailableForAll && !allowGlobalFilters;
    }

    protected void initThirdConfigurationFormRow() {
        initGeneratedIdField();
        if (defaultForMeFieldVisible) {
            initDefaultForMeField();
        }
    }

    protected void initNameField() {
        String label = getLabelByProperty("name");

        nameField.setLabel(label);
        nameField.setValueSource(new ContainerValueSource<>(configurationDc, "name"));

        getContent().add(nameField);
    }

    protected void initConfigurationIdField() {
        String label = getLabelByProperty("configurationId");

        configurationIdField.setLabel(label);
        configurationIdField.setValueSource(new ContainerValueSource<>(configurationDc, "configurationId"));

        configurationIdField.setEnabled(StringUtils.isEmpty(configurationDc.getItem().getConfigurationId())
                && !generatedIdField.getValue());
        configurationIdField.setVisible(flowuiComponentProperties.isFilterShowConfigurationIdField());

        getContent().add(configurationIdField);
    }

    protected void initDefaultForMeField() {
        JmixCheckbox defaultForMeField = createCheckbox();
        String label = getLabelByProperty("defaultForMe");

        defaultForMeField.setLabel(label);
        defaultForMeField.setId(DEFAULT_FOR_ME_FIELD_ID);
        defaultForMeField.setWidthFull();
        defaultForMeField.setValueSource(new ContainerValueSource<>(configurationDc, "defaultForMe"));

        getContent().add(defaultForMeField);
    }

    protected void initAvailableForAllUsersField(boolean allowGlobalFilters, boolean isAvailableForAll) {
        String label = messages.getMessage(getClass(), "availableForAllUsersField.label");

        availableForAllUsersField.setLabel(label);
        availableForAllUsersField.setId(AVAILABLE_FOR_ALL_USERS_FIELD_ID);
        availableForAllUsersField.setWidthFull();
        availableForAllUsersField.addValueChangeListener(this::availableForAllUsersFieldValueChangeListener);
        availableForAllUsersField.setVisible(allowGlobalFilters);
        availableForAllUsersField.setValue(isAvailableForAll);

        getContent().add(availableForAllUsersField);
    }

    protected void initDefaultForAllUsersField(boolean allowGlobalFilters, boolean isAvailableForAll) {
        String label = getLabelByProperty("defaultForAll");

        defaultForAllUsersField.setLabel(label);
        defaultForAllUsersField.setId(DEFAULT_FOR_ALL_USERS_FIELD_ID);
        defaultForAllUsersField.setWidthFull();
        defaultForAllUsersField.setValueSource(new ContainerValueSource<>(configurationDc, "defaultForAll"));

        defaultForAllUsersField.setVisible(allowGlobalFilters);
        defaultForAllUsersField.setEnabled(isAvailableForAll);

        getContent().add(defaultForAllUsersField);
    }

    protected void initGeneratedIdField() {
        String label = messages.getMessage(getClass(), "generatedIdField.label");

        generatedIdField.setLabel(label);
        generatedIdField.setVisible(flowuiComponentProperties.isFilterShowConfigurationIdField());

        getContent().add(generatedIdField);
    }

    protected void availableForAllUsersFieldValueChangeListener(ComponentValueChangeEvent<Checkbox, Boolean> event) {
        if (!event.getValue()) {
            defaultForAllUsersField.setValue(false);
        }

        defaultForAllUsersField.setEnabled(event.getValue());
    }

    protected String getLabelByProperty(String propertyName) {
        return messageTools.getPropertyCaption(configurationDc.getEntityMetaClass(), propertyName);
    }
}
