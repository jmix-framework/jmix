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

import io.jmix.securityui.model.DefaultResourcePolicyGroupResolver;
import io.jmix.securityui.model.ResourcePolicyModel;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("sec_MenuResourcePolicyModel.edit")
@UiDescriptor("menu-resource-policy-model-edit.xml")
@EditedEntityContainer("resourcePolicyModelDc")
public class MenuResourcePolicyModelEdit extends StandardEditor<ResourcePolicyModel> {

    @Autowired
    private ComboBox<String> menuField;

    @Autowired
    private ResourcePolicyEditorUtils resourcePolicyEditorUtils;

    @Autowired
    private DefaultResourcePolicyGroupResolver resourcePolicyGroupResolver;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        menuField.setOptionsMap(resourcePolicyEditorUtils.getMenuItemOptionsMap());
    }

    @Subscribe(id = "resourcePolicyModelDc", target = Target.DATA_CONTAINER)
    public void onResourcePolicyModelDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<ResourcePolicyModel> event) {
        if ("resource".equals(event.getProperty())) {
            String policyGroup = resourcePolicyGroupResolver.resolvePolicyGroup(getEditedEntity().getType(), getEditedEntity().getResource());
            getEditedEntity().setPolicyGroup(policyGroup);
        }
    }
}