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

import io.jmix.core.security.SpecificPolicyInfoRegistry;
import io.jmix.securityui.model.ResourcePolicyModel;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.HasEnterPressHandler;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@UiController("sec_SpecificResourcePolicyModel.edit")
@UiDescriptor("specific-resource-policy-model-edit.xml")
@EditedEntityContainer("resourcePolicyModelDc")
public class SpecificResourcePolicyModelEdit extends StandardEditor<ResourcePolicyModel> {

    @Autowired
    private ComboBox<String> resourceField;

    @Autowired
    private SpecificPolicyInfoRegistry specificPolicyInfoRegistry;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        List<String> specificPolicyNames = specificPolicyInfoRegistry.getSpecificPolicyInfos().stream()
                .map(SpecificPolicyInfoRegistry.SpecificPolicyInfo::getName)
                .sorted()
                .collect(Collectors.toList());
        resourceField.setOptionsList(specificPolicyNames);
    }

    @Install(to = "resourceField", subject = "enterPressHandler")
    private void resourceFieldEnterPressHandler(HasEnterPressHandler.EnterPressEvent enterPressEvent) {
        resourceField.setValue(enterPressEvent.getText());
    }
}