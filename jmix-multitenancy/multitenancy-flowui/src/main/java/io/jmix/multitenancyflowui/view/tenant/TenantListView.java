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
import io.jmix.flowui.view.*;
import io.jmix.multitenancy.core.TenantProvider;
import io.jmix.multitenancy.entity.Tenant;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "mten/tenants", layout = DefaultMainViewParent.class)
@ViewController("mten_Tenant.list")
@ViewDescriptor("tenant-list-view.xml")
@LookupComponent("tenantsTable")
@DialogMode(width = "50em")
public class TenantListView extends StandardListView<Tenant> {

    @Autowired
    private TenantProvider tenantProvider;

    @Install(to = "tenantsTable.create", subject = "enabledRule")
    private boolean tenantsTableCreateEnabledRule() {
        return !isTenantUser();
    }

    @Install(to = "tenantsTable.edit", subject = "enabledRule")
    private boolean tenantsTableEditEnabledRule() {
        return !isTenantUser();
    }

    @Install(to = "tenantsTable.remove", subject = "enabledRule")
    private boolean tenantsTableRemoveEnabledRule() {
        return !isTenantUser();
    }

    protected boolean isTenantUser() {
        String currentUserTenantId = tenantProvider.getCurrentUserTenantId();
        return !TenantProvider.NO_TENANT.equals(currentUserTenantId);
    }
}
