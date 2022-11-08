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

package io.jmix.securityui.screen.resourcepolicy;

import com.google.common.base.Strings;
import io.jmix.core.Metadata;
import io.jmix.security.model.ResourcePolicy;
import io.jmix.security.model.ResourcePolicyEffect;
import io.jmix.security.model.ResourcePolicyType;
import io.jmix.securityui.model.DefaultResourcePolicyGroupResolver;
import io.jmix.securityui.model.ResourcePolicyModel;
import io.jmix.ui.component.CheckBox;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.TextField;
import io.jmix.ui.component.ValidationErrors;
import io.jmix.ui.menu.MenuItem;
import io.jmix.ui.screen.MessageBundle;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@UiController("sec_MenuResourcePolicyModel.create")
@UiDescriptor("menu-resource-policy-model-create.xml")
public class MenuResourcePolicyModelCreate extends MultipleResourcePolicyModelCreateScreen {
    @Autowired
    private TextField<String> policyGroupField;

    @Autowired
    private ComboBox<String> menuItemField;

    @Autowired
    private TextField<String> screenField;

    @Autowired
    private CheckBox screenAccessField;

    @Autowired
    private ResourcePolicyEditorUtils resourcePolicyEditorUtils;

    @Autowired
    private DefaultResourcePolicyGroupResolver resourcePolicyGroupResolver;

    @Autowired
    private Metadata metadata;

    @Autowired
    private MessageBundle messageBundle;

    private String screenId;

    private boolean hasChanges = false;

    @Subscribe
    public void onInit(InitEvent event) {
        menuItemField.setOptionsMap(resourcePolicyEditorUtils.getMenuItemOptionsMap());
    }

    @Subscribe("menuItemField")
    public void onMenuFieldValueChange(HasValue.ValueChangeEvent<String> event) {
        String menuItemId = event.getValue();
        policyGroupField.setValue(resourcePolicyGroupResolver.resolvePolicyGroup(ResourcePolicyType.MENU, menuItemId));

        screenId = null;
        if (menuItemId != null) {
            MenuItem menuItem = resourcePolicyEditorUtils.findMenuItemById(menuItemId);
            if (menuItem != null) {
                screenId = menuItem.getScreen();
            }
        }

        if (screenId == null) {
            screenField.setValue(null);
            screenAccessField.setValue(Boolean.FALSE);
            screenAccessField.setEditable(false);
        } else {
            screenField.setValue(resourcePolicyEditorUtils.getScreenCaption(screenId));
            screenAccessField.setEditable(true);
        }
        hasChanges = true;
    }

    @Subscribe("policyGroupField")
    protected void onPolicyGroupFieldValueChange(HasValue.ValueChangeEvent<String> event){
        hasChanges = true;
    }

    @Subscribe("screenAccessField")
    protected void onScreenAccessFieldValueChange(HasValue.ValueChangeEvent<Boolean> event) {
        hasChanges = true;
    }

    @Subscribe("screenField")
    protected void onScreenFieldValueChange(HasValue.ValueChangeEvent<String> event){
        hasChanges = true;
    }

    @Override
    protected ValidationErrors validateScreen() {
        ValidationErrors validationErrors = new ValidationErrors();
        if (Strings.isNullOrEmpty(menuItemField.getValue())) {
            validationErrors.add(menuItemField,
                    messageBundle.getMessage("MenuResourcePolicyModelCreate.selectResource"));
        }
        return validationErrors;
    }

    @Override
    public List<ResourcePolicyModel> getResourcePolicies() {
        List<ResourcePolicyModel> policies = new ArrayList<>();
        String menuItemId = menuItemField.getValue();

        ResourcePolicyModel menuPolicy = metadata.create(ResourcePolicyModel.class);
        menuPolicy.setType(ResourcePolicyType.MENU);
        menuPolicy.setResource(menuItemId);
        menuPolicy.setPolicyGroup(policyGroupField.getValue());
        menuPolicy.setAction(ResourcePolicy.DEFAULT_ACTION);
        menuPolicy.setEffect(ResourcePolicyEffect.ALLOW);
        policies.add(menuPolicy);

        if (screenAccessField.isChecked() && screenId != null) {
            ResourcePolicyModel screenPolicy = metadata.create(ResourcePolicyModel.class);
            screenPolicy.setType(ResourcePolicyType.SCREEN);
            screenPolicy.setResource(screenId);
            screenPolicy.setPolicyGroup(policyGroupField.getValue());
            screenPolicy.setAction(ResourcePolicy.DEFAULT_ACTION);
            screenPolicy.setEffect(ResourcePolicyEffect.ALLOW);
            policies.add(screenPolicy);
        }

        return policies;
    }

    @Override
    public boolean hasUnsavedChanges() {
        return hasChanges;
    }
}