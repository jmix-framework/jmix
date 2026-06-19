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

import io.jmix.flowui.view.*;
import io.jmix.security.model.ResourcePolicyModel;

@ViewController("sec_ResourcePolicyModel.detail")
@ViewDescriptor("resource-policy-model-detail-view.xml")
@EditedEntityContainer("resourcePolicyModelDc")
@DialogMode(width = "32em")
public class ResourcePolicyModelDetailView extends StandardDetailView<ResourcePolicyModel> {

    @Subscribe
    public void onInit(InitEvent event) {
        setReloadEdited(false);
    }

}
