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

package io.jmix.multitenancyflowui.view.tenant;

import com.vaadin.flow.router.Route;
import io.jmix.core.EntityStates;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.view.DefaultMainViewParent;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import io.jmix.multitenancy.entity.Tenant;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "mten/tenants/:id", layout = DefaultMainViewParent.class)
@ViewController("mten_Tenant.detail")
@ViewDescriptor("tenant-detail-view.xml")
@EditedEntityContainer("tenantDc")
@DialogMode(width = "50em", height = "37.5em")
public class TenantDetailView extends StandardDetailView<Tenant> {

    @ViewComponent
    protected TypedTextField<String> tenantIdField;

    @Autowired
    protected EntityStates entityStates;

    @Subscribe
    public void onReady(ReadyEvent event) {
        tenantIdField.setReadOnly(!entityStates.isNew(getEditedEntity()));
    }
}
