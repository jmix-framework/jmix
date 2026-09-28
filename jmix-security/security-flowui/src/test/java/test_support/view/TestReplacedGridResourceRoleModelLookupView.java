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
import io.jmix.flowui.view.LookupComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import io.jmix.securityflowui.view.resourcerole.ResourceRoleModelLookupView;

/**
 * An application view that gives the lookup of resource roles its own descriptor, whose grid has another id.
 */
@Route("test-replaced-grid-resource-role-models-lookup")
@ViewController("test_ReplacedGridResourceRoleModel.lookup")
@ViewDescriptor("test-replaced-grid-resource-role-model-lookup-view.xml")
@LookupComponent("rolesGrid")
public class TestReplacedGridResourceRoleModelLookupView extends ResourceRoleModelLookupView {
}
