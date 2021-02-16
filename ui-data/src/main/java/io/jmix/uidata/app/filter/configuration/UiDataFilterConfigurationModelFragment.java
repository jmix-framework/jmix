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
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.component.CheckBox;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.TextField;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.property.UiFilterProperties;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.screen.StandardOutcome;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.Target;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
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
    protected UiFilterProperties uiFilterProperties;
    @Autowired
    protected AccessManager accessManager;

    @Autowired
    protected InstanceContainer<FilterConfiguration> configurationDc;
    @Autowired
    protected TextField<String> configurationIdField;
    @Autowired
    protected CheckBox availableForAllField;
    @Autowired
    protected CheckBox defaultForAllField;
    @Autowired
    protected CheckBox generatedIdField;
    @Autowired
    protected CheckBox defaultForMeField;

    protected boolean defaultForMeFieldVisible = true;

    public void setDefaultForMeFieldVisible(boolean visible) {
        defaultForMeFieldVisible = visible;
    }

    public boolean getDefaultForMeFieldVisible() {
        return defaultForMeFieldVisible;
    }

    @Subscribe
    protected void onAfterInit(AfterInitEvent event) {
        FilterConfiguration editedConfigurationModel = configurationDc.getItem();
        generatedIdField.setValue(true);

        defaultForMeField.setVisible(defaultForMeFieldVisible);

        configurationIdField.setEnabled(StringUtils.isEmpty(editedConfigurationModel.getConfigurationId()));
        configurationIdField.setVisible(uiFilterProperties.isShowConfigurationIdField());
        generatedIdField.setVisible(uiFilterProperties.isShowConfigurationIdField());

        UiFilterModifyGlobalConfigurationContext globalFilterContext = new UiFilterModifyGlobalConfigurationContext();
        accessManager.applyRegisteredConstraints(globalFilterContext);
        boolean allowGlobalFilters = globalFilterContext.isPermitted();
        availableForAllField.setVisible(allowGlobalFilters);
        defaultForAllField.setVisible(allowGlobalFilters);

        boolean isAvailableForAll = StringUtils.isEmpty(editedConfigurationModel.getUsername());
        availableForAllField.setValue(isAvailableForAll);
        defaultForAllField.setEnabled(isAvailableForAll);
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

    @Subscribe("nameField")
    protected void onNameFieldValueChange(HasValue.ValueChangeEvent<String> event) {
        if (generatedIdField.isChecked()) {
            configurationIdField.setValue(generateConfigurationId(event.getValue()));
        }
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
