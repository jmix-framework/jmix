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

package test_support.view;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import io.jmix.securityflowui.view.resourcerole.ResourceRoleModelListView;

/**
 * An application view that replaces the initialization of the resource role list without calling it.
 */
@Route("test-replaced-init-resource-role-models")
@ViewController("test_ReplacedInitResourceRoleModel.list")
@ViewDescriptor(path = "/io/jmix/securityflowui/view/resourcerole/resource-role-model-list-view.xml")
public class TestReplacedInitResourceRoleModelListView extends ResourceRoleModelListView {

    @Override
    @Subscribe
    public void onInit(InitEvent event) {
    }
}
