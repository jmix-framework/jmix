/*
 * Copyright 2020 Haulmont.
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

package io.jmix.uidata.app.filter.configuration;

import io.jmix.core.AccessManager;
import io.jmix.core.MessageTools;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.CheckBox;
import io.jmix.ui.component.Form;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.TextField;
import io.jmix.ui.component.data.value.ContainerValueSource;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.UiComponentProperties;
import io.jmix.ui.screen.*;
import io.jmix.uidata.accesscontext.UiFilterModifyGlobalConfigurationContext;
import io.jmix.uidata.entity.FilterConfiguration;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import static io.jmix.ui.component.filter.FilterUtils.generateConfigurationId;

@UiController("ui_UiDataFilterConfigurationModel.fragment")
@UiDescriptor("ui-data-filter-configuration-model-fragment.xml")
public class UiDataFilterConfigurationModelFragment extends ScreenFragment {

    @Autowired
    protected CurrentAuthentication currentAuthentication;
    @Autowired
    protected UiComponentProperties componentProperties;
    @Autowired
    protected AccessManager accessManager;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected MessageTools messageTools;

    @Autowired
    protected InstanceContainer<FilterConfiguration> configurationDc;

    @Autowired
    protected Form configurationForm;
    @Autowired
    protected TextField<String> configurationIdField;
    @Autowired
    protected CheckBox availableForAllField;
    @Autowired
    protected CheckBox defaultForAllField;
    @Autowired
    protected CheckBox generatedIdField;

    protected boolean defaultForMeFieldVisible = true;

    public void setDefaultForMeFieldVisible(boolean visible) {
        defaultForMeFieldVisible = visible;
    }

    public boolean getDefaultForMeFieldVisible() {
        return defaultForMeFieldVisible;
    }

    @Subscribe
    protected void onAfterInit(AfterInitEvent event) {
        initFirstConfigurationFormRow();
        initSecondConfigurationFormRow();
        initThirdConfigurationFormRow();
    }

    protected void initFirstConfigurationFormRow() {
        TextField<String> nameField = createNameField();
        if (defaultForMeFieldVisible) {
            configurationForm.add(nameField, 0, 0);
            CheckBox defaultForMeField = createDefaultForMeField();
            configurationForm.add(defaultForMeField, 1, 0);
        } else {
            configurationForm.add(nameField, 0, 0, 2, 1);
        }
    }

    protected TextField<String> createNameField() {
        TextField<String> nameField = uiComponents.create(TextField.TYPE_STRING);
        nameField.setValueSource(new ContainerValueSource<>(configurationDc, "name"));
        nameField.setRequired(true);
        String caption = messageTools.getPropertyCaption(configurationDc.getEntityMetaClass(), "name");
        nameField.setCaption(caption);
        nameField.addValueChangeListener(event -> {
            if (generatedIdField.isChecked()) {
                configurationIdField.setValue(generateConfigurationId(event.getValue()));
            }
        });
        nameField.setWidthFull();
        return nameField;
    }

    protected CheckBox createDefaultForMeField() {
        CheckBox defaultForMeField = uiComponents.create(CheckBox.NAME);
        defaultForMeField.setValueSource(new ContainerValueSource<>(configurationDc, "defaultForMe"));
        String caption = messageTools.getPropertyCaption(configurationDc.getEntityMetaClass(), "defaultForMe");
        defaultForMeField.setCaption(caption);
        defaultForMeField.setWidthFull();
        return defaultForMeField;
    }

    protected void initSecondConfigurationFormRow() {
        UiFilterModifyGlobalConfigurationContext globalFilterContext = new UiFilterModifyGlobalConfigurationContext();
        accessManager.applyRegisteredConstraints(globalFilterContext);
        boolean allowGlobalFilters = globalFilterContext.isPermitted();
        availableForAllField.setVisible(allowGlobalFilters);
        defaultForAllField.setVisible(allowGlobalFilters);

        boolean isAvailableForAll = StringUtils.isEmpty(configurationDc.getItem().getUsername());
        availableForAllField.setValue(isAvailableForAll);
        defaultForAllField.setEnabled(isAvailableForAll);

        updateHostScreenReadOnlyState(allowGlobalFilters, isAvailableForAll);
    }

    protected void updateHostScreenReadOnlyState(boolean allowGlobalFilters, boolean isAvailableForAll) {
        if (isAvailableForAll && !allowGlobalFilters
                && getHostScreen() instanceof ReadOnlyAwareScreen) {
            ((ReadOnlyAwareScreen) getHostScreen()).setReadOnly(true);
        }
    }

    protected void initThirdConfigurationFormRow() {
        generatedIdField.setValue(true);
        configurationIdField.setEnabled(StringUtils.isEmpty(configurationDc.getItem().getConfigurationId()));
        configurationIdField.setVisible(componentProperties.isFilterShowConfigurationIdField());
        generatedIdField.setVisible(componentProperties.isFilterShowConfigurationIdField());
    }

    @Subscribe("availableForAllField")
    protected void onAvailableForAllFieldValueChange(HasValue.ValueChangeEvent<Boolean> event) {
        boolean isAvailableForAll = BooleanUtils.isTrue(event.getValue());
        if (!isAvailableForAll) {
            defaultForAllField.setValue(false);
        }
        defaultForAllField.setEnabled(isAvailableForAll);
    }

    @Subscribe("generatedIdField")
    protected void onGeneratedIdFieldValueChange(HasValue.ValueChangeEvent<Boolean> event) {
        boolean checked = BooleanUtils.isTrue(event.getValue());
        configurationIdField.setEnabled(!checked);
    }

    @Subscribe(target = Target.PARENT_CONTROLLER)
    protected void onBeforeClose(Screen.BeforeCloseEvent event) {
        if (event.closedWith(StandardOutcome.COMMIT)) {
            FilterConfiguration editedConfigurationModel = configurationDc.getItem();

            if (availableForAllField.isChecked()) {
                editedConfigurationModel.setUsername(null);
            } else {
                editedConfigurationModel.setUsername(currentAuthentication.getUser().getUsername());
            }
        }
    }
}
