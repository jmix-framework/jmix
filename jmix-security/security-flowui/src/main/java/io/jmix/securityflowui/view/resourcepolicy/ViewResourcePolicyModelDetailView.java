/*
 * Copyright 2026 Haulmont.
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

import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.model.InstanceContainer.ItemPropertyChangeEvent;
import io.jmix.flowui.view.*;
import io.jmix.securityflowui.model.DefaultResourcePolicyGroupResolver;
import io.jmix.security.model.ResourcePolicyModel;
import org.springframework.beans.factory.annotation.Autowired;

@ViewController("sec_ViewResourcePolicyModel.detail")
@ViewDescriptor("view-resource-policy-model-detail-view.xml")
@EditedEntityContainer("resourcePolicyModelDc")
@DialogMode(width = "32em")
public class ViewResourcePolicyModelDetailView extends StandardDetailView<ResourcePolicyModel> {

    @ViewComponent
    private JmixComboBox<String> resourceField;

    @Autowired
    private ResourcePolicyViewUtils resourcePolicyEditorUtils;
    @Autowired
    private DefaultResourcePolicyGroupResolver resourcePolicyGroupResolver;

    @Subscribe
    public void onInit(InitEvent event) {
        setReloadEdited(false);
        ComponentUtils.setItemsMap(resourceField, resourcePolicyEditorUtils.getViewsOptionsMap());
    }

    @Subscribe(id = "resourcePolicyModelDc", target = Target.DATA_CONTAINER)
    public void onResourcePolicyModelDcItemPropertyChange(ItemPropertyChangeEvent<ResourcePolicyModel> event) {
        if ("resource".equals(event.getProperty())) {
            String policyGroup = resourcePolicyGroupResolver.resolvePolicyGroup(getEditedEntity().getType(),
                    getEditedEntity().getResource());
            getEditedEntity().setPolicyGroup(policyGroup);
        }
    }
}
