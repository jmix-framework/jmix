/*
 * Copyright 2022 Haulmont.
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

package io.jmix.securityflowui.view.resourcepolicy;

import com.google.common.base.Strings;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.combobox.ComboBox;
import io.jmix.core.Metadata;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.kit.component.FlowuiComponentUtils;
import io.jmix.flowui.menu.MenuItem;
import io.jmix.flowui.view.*;
import io.jmix.security.model.ResourcePolicy;
import io.jmix.security.model.ResourcePolicyEffect;
import io.jmix.securityflowui.model.DefaultResourcePolicyGroupResolver;
import io.jmix.securityflowui.model.ResourcePolicyModel;
import io.jmix.securityflowui.model.ResourcePolicyType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@ViewController("sec_MenuResourcePolicyModel.create")
@ViewDescriptor("menu-resource-policy-model-create-view.xml")
@DialogMode(width = "32em")
public class MenuResourcePolicyModelCreateView extends MultipleResourcePolicyModelCreateView {

    @ViewComponent
    private JmixComboBox<String> menuItemField;
    @ViewComponent
    private TypedTextField<String> policyGroupField;
    @ViewComponent
    private TypedTextField<String> viewField;
    @ViewComponent
    private JmixCheckbox viewAccessField;

    @Autowired
    private ResourcePolicyViewUtils resourcePolicyEditorUtils;
    @Autowired
    private DefaultResourcePolicyGroupResolver resourcePolicyGroupResolver;

    @Autowired
    private Metadata metadata;
    @Autowired
    private MessageBundle messageBundle;

    private String viewId;

    @Subscribe
    public void onInit(InitEvent event) {
        FlowuiComponentUtils.setItemsMap(menuItemField, resourcePolicyEditorUtils.getMenuItemOptionsMap());
        menuItemField.addValueChangeListener(this::onMenuItemFieldValueChange);
    }

    private void onMenuItemFieldValueChange(ComponentValueChangeEvent<ComboBox<String>, String> event) {
        String menuItemId = event.getValue();
        String policyGroup = resourcePolicyGroupResolver
                .resolvePolicyGroup(ResourcePolicyType.MENU.getId(), menuItemId);
        if (policyGroup != null) {
            policyGroupField.setValue(policyGroup);
        } else {
            policyGroupField.clear();
        }

        viewId = null;
        if (menuItemId != null) {
            MenuItem menuItem = resourcePolicyEditorUtils.findMenuItemById(menuItemId);
            if (menuItem != null) {
                viewId = menuItem.getView();
            }
        }

        if (viewId == null) {
            viewField.clear();
            viewAccessField.setValue(Boolean.FALSE);
            viewAccessField.setReadOnly(true);
        } else {
            viewField.setValue(resourcePolicyEditorUtils.getViewTitle(viewId));
            viewAccessField.setReadOnly(false);
        }
    }

    @Override
    protected ValidationErrors validateView() {
        ValidationErrors validationErrors = new ValidationErrors();
        if (Strings.isNullOrEmpty(menuItemField.getValue())) {
            validationErrors.add(menuItemField,
                    messageBundle.getMessage("menuResourcePolicyModelCreateView.error.selectMenuItem"));
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

        if (Boolean.TRUE.equals(viewAccessField.getValue()) && viewId != null) {
            ResourcePolicyModel viewPolicy = metadata.create(ResourcePolicyModel.class);
            viewPolicy.setType(ResourcePolicyType.VIEW);
            viewPolicy.setResource(viewId);
            viewPolicy.setPolicyGroup(policyGroupField.getValue());
            viewPolicy.setAction(ResourcePolicy.DEFAULT_ACTION);
            viewPolicy.setEffect(ResourcePolicyEffect.ALLOW);
            policies.add(viewPolicy);
        }

        return policies;
    }
}
