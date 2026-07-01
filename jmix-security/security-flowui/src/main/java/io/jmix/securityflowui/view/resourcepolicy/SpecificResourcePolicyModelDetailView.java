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

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.ComboBoxBase.CustomValueSetEvent;
import io.jmix.core.security.SpecificPolicyInfoRegistry;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.view.*;
import io.jmix.security.model.ResourcePolicyModel;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@ViewController("sec_SpecificResourcePolicyModel.detail")
@ViewDescriptor("specific-resource-policy-model-detail-view.xml")
@EditedEntityContainer("resourcePolicyModelDc")
@DialogMode(width = "32em")
public class SpecificResourcePolicyModelDetailView extends StandardDetailView<ResourcePolicyModel> {

    @ViewComponent
    private JmixComboBox<String> resourceField;

    @Autowired
    private SpecificPolicyInfoRegistry specificPolicyInfoRegistry;

    @Subscribe
    public void onInit(InitEvent event) {
        setReloadEdited(false);
        List<String> specificPolicyNames = specificPolicyInfoRegistry.getSpecificPolicyInfos().stream()
                .map(SpecificPolicyInfoRegistry.SpecificPolicyInfo::getName)
                .sorted()
                .collect(Collectors.toList());

        resourceField.setItems(specificPolicyNames);
    }

    @Subscribe("resourceField")
    private void onCustomValueSet(CustomValueSetEvent<ComboBox<String>> event) {
        resourceField.setValue(event.getDetail());
    }
}
