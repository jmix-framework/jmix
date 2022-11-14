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
import io.jmix.ui.component.*;
import io.jmix.ui.menu.MenuItem;
import io.jmix.ui.screen.MessageBundle;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@UiController("sec_ScreenResourcePolicyModel.create")
@UiDescriptor("screen-resource-policy-model-create.xml")
public class ScreenResourcePolicyModelCreate extends MultipleResourcePolicyModelCreateScreen {
    @Autowired
    private TextField<String> policyGroupField;

    @Autowired
    private ComboBox<String> screenField;

    @Autowired
    private TextArea<String> menuItemField;

    @Autowired
    private CheckBox menuAccessField;

    @Autowired
    private ResourcePolicyEditorUtils resourcePolicyEditorUtils;

    @Autowired
    private DefaultResourcePolicyGroupResolver resourcePolicyGroupResolver;

    @Autowired
    private Metadata metadata;

    @Autowired
    private MessageBundle messageBundle;

    private String menuItemId;

    private boolean hasChanges = false;

    @Subscribe
    public void onInit(InitEvent event) {
        screenField.setOptionsMap(resourcePolicyEditorUtils.getScreenOptionsMap());
    }

    @Subscribe("policyGroupField")
    protected void onPolicyGroupFieldValueChange(HasValue.ValueChangeEvent<String> event) {
        hasChanges = true;
    }
    @Subscribe("menuItemField")
    protected void onMenuItemFieldValueChange(HasValue.ValueChangeEvent<String> event) {
        hasChanges = true;
    }
    @Subscribe("menuAccessField")
    protected void onMenuAccessFieldValueChange(HasValue.ValueChangeEvent<Boolean> booleanValueChangeEvent) {
        hasChanges = true;
    }

    @Subscribe("screenField")
    public void onScreenFieldValueChange(HasValue.ValueChangeEvent<String> event) {
        String screenId = event.getValue();
        if(screenId!=null) {
            policyGroupField.setValue(resourcePolicyGroupResolver.resolvePolicyGroup(ResourcePolicyType.SCREEN, screenId));

            menuItemId = null;
            MenuItem menuItem = null;
            if (screenId != null) {
                menuItem = resourcePolicyEditorUtils.findMenuItemByScreen(screenId);
                if (menuItem != null) {
                    menuItemId = menuItem.getId();
                }
            }

            if (this.menuItemId == null) {
                menuItemField.setValue(null);
                menuAccessField.setValue(Boolean.FALSE);
                menuAccessField.setEditable(false);
            } else {
                menuItemField.setValue(resourcePolicyEditorUtils.getMenuCaption(menuItem));
                menuAccessField.setEditable(true);
            }
            hasChanges = true;
        }
    }

    @Override
    protected ValidationErrors validateScreen() {
        ValidationErrors validationErrors = new ValidationErrors();
        if (Strings.isNullOrEmpty(screenField.getValue())) {
            validationErrors.add(screenField,
                    messageBundle.getMessage("ScreenResourcePolicyModelCreate.selectResource"));
        }
        return validationErrors;
    }

    @Override
    public List<ResourcePolicyModel> getResourcePolicies() {
        List<ResourcePolicyModel> policies = new ArrayList<>();
        String screenId = screenField.getValue();

        ResourcePolicyModel screenPolicy = metadata.create(ResourcePolicyModel.class);
        screenPolicy.setType(ResourcePolicyType.SCREEN);
        screenPolicy.setResource(screenId);
        screenPolicy.setPolicyGroup(policyGroupField.getValue());
        screenPolicy.setAction(ResourcePolicy.DEFAULT_ACTION);
        screenPolicy.setEffect(ResourcePolicyEffect.ALLOW);
        policies.add(screenPolicy);

        if (menuAccessField.isChecked() && menuItemId != null) {
            ResourcePolicyModel menuPolicy = metadata.create(ResourcePolicyModel.class);
            menuPolicy.setType(ResourcePolicyType.MENU);
            menuPolicy.setResource(menuItemId);
            menuPolicy.setPolicyGroup(policyGroupField.getValue());
            menuPolicy.setAction(ResourcePolicy.DEFAULT_ACTION);
            menuPolicy.setEffect(ResourcePolicyEffect.ALLOW);
            policies.add(menuPolicy);
        }

        return policies;
    }

    @Override
    public boolean hasUnsavedChanges() {
        return hasChanges;
    }
}
