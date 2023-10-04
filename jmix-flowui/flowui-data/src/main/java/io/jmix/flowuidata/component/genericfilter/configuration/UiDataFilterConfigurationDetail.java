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
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.genericfilter.configuration.AbstractConfigurationDetail;
import io.jmix.flowui.data.value.ContainerValueSource;
import io.jmix.flowui.exception.ComponentValidationException;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowuidata.accesscontext.UiGenericFilterModifyGlobalConfigurationContext;
import io.jmix.flowuidata.entity.FilterConfiguration;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class UiDataFilterConfigurationDetail extends AbstractConfigurationDetail {

    protected static final String DEFAULT_FOR_ME_FIELD_ID = "defaultForMeField";
    protected static final String AVAILABLE_FOR_ALL_USERS_FIELD_ID = "availableForAllUsersField";
    protected static final String DEFAULT_FOR_ALL_USERS_FIELD_ID = "defaultForAllUsersField";

    protected CurrentUserSubstitution currentUserSubstitution;
    protected AccessManager accessManager;
    protected MessageTools messageTools;

    protected JmixCheckbox defaultForAllUsersField;
    protected JmixCheckbox availableForAllUsersField;

    protected boolean defaultForMeFieldVisible = true;
    protected boolean viewReadOnly = false;

    protected GenericFilter filter;
    protected String originalConfigurationId;

    protected InstanceContainer<FilterConfiguration> configurationDc;

    @Override
    protected void autowireDependencies() {
        super.autowireDependencies();
        currentUserSubstitution = applicationContext.getBean(CurrentUserSubstitution.class);
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
            editedConfigurationModel.setUsername(currentUserSubstitution.getEffectiveUser().getUsername());
        }
    }

    public void init() {
        initFields();
        initOriginalConfigurationId();
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
        UiGenericFilterModifyGlobalConfigurationContext globalFilterContext =
                new UiGenericFilterModifyGlobalConfigurationContext();
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
        configurationIdField.setVisible(uiComponentProperties.isFilterShowConfigurationIdField());

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
        generatedIdField.setVisible(uiComponentProperties.isFilterShowConfigurationIdField());

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

    protected void initOriginalConfigurationId() {
        originalConfigurationId = configurationDc.getItem().getConfigurationId();
    }

    public void setFilter(GenericFilter filter) {
        this.filter = filter;
    }

    @Override
    public void executeValidators() throws ValidationException {
        super.executeValidators();
        if (uiComponentProperties.isFilterConfigurationUniqueNames()) {
            validateConfigurationName();
        }
    }

    protected void validateConfigurationName() {
        boolean configurationWithSameNameExists = configurationWithSameNameExists();
        if (configurationWithSameNameExists) {
            boolean availableForAllUsers = availableForAllUsersField.getValue();
            String messageKey;
            if (availableForAllUsers) {
                messageKey = "nameField.nonUniqueGlobalName";
            } else {
                messageKey = "nameField.nonUniqueUserName";
            }
            throw new ComponentValidationException(messages.getMessage(getClass(), messageKey), nameField);
        }
    }

    protected boolean configurationWithSameNameExists() {
        if (filter == null) {
            return false;
        }

        String editedConfigurationName = configurationDc.getItem().getName();
        boolean availableForAllUsers = availableForAllUsersField.getValue();
        return filter.getConfigurations().stream()
                .anyMatch(conf ->
                        Objects.equals(editedConfigurationName, conf.getName())
                                && (availableForAllUsers == conf.isAvailableForAllUsers())
                                //skip itself the configuration being checked
                                && !Objects.equals(originalConfigurationId, conf.getId())
                );
    }

    @Override
    public boolean isInvalid() {
        return super.isInvalid() || configurationWithSameNameExists();
    }
}
