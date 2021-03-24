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

package io.jmix.ui.app.filter.configuration;

import io.jmix.ui.component.CheckBox;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.TextField;
import io.jmix.ui.UiComponentProperties;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import static io.jmix.ui.component.filter.FilterUtils.generateConfigurationId;

@UiController("ui_FilterConfigurationModel.fragment")
@UiDescriptor("filter-configuration-model-fragment.xml")
public class FilterConfigurationModelFragment extends ScreenFragment {

    @Autowired
    protected UiComponentProperties componentProperties;

    @Autowired
    protected TextField<String> idField;
    @Autowired
    protected TextField<String> nameField;
    @Autowired
    protected CheckBox generatedIdField;

    protected String name;
    protected String id;

    public String getConfigurationName() {
        return name;
    }

    public void setConfigurationName(String name) {
        this.name = name;
    }

    public String getConfigurationId() {
        return id;
    }

    public void setConfigurationId(String code) {
        this.id = code;
    }

    @Subscribe
    protected void onAfterInit(AfterInitEvent event) {
        if (id != null) {
            idField.setValue(id);
            idField.setEnabled(false);
        }

        if (name != null) {
            nameField.setValue(name);
        }

        idField.setVisible(componentProperties.isFilterShowConfigurationIdField());
        generatedIdField.setVisible(componentProperties.isFilterShowConfigurationIdField());
    }

    @Subscribe("idField")
    protected void onIdFieldValueChange(HasValue.ValueChangeEvent<String> event) {
        id = event.getValue();
    }

    @Subscribe("nameField")
    protected void onNameFieldValueChange(HasValue.ValueChangeEvent<String> event) {
        name = event.getValue();

        if (generatedIdField.isChecked()) {
            idField.setValue(generateConfigurationId(name));
        }
    }

    @Subscribe("generatedIdField")
    protected void onGeneratedIdFieldValueChange(HasValue.ValueChangeEvent<Boolean> event) {
        boolean checked = BooleanUtils.isTrue(event.getValue());
        idField.setEnabled(!checked);
    }
}
