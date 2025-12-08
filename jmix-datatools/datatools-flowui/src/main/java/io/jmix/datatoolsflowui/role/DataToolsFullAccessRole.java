/*
 * Copyright 2025 Haulmont.
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

package io.jmix.datatoolsflowui.role;

import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.SpecificPolicy;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

/**
 * System role that grants all permissions for DataTools functionality.
 */
@ResourceRole(name = "Data Tools: full access", code = DataToolsFullAccessRole.CODE, scope = SecurityScope.UI)
public interface DataToolsFullAccessRole extends ShowEntityInfoRole {

    String CODE = "datatools-full-access";

    @MenuPolicy(menuIds = "datatl_entityInspectorListView")
    @ViewPolicy(viewIds = {"datatl_entityInspectorListView", "datatl_entityInspectorDetailView"})
    void views();

    @SpecificPolicy(resources = "datatools.importExportEntity")
    void specific();
}
