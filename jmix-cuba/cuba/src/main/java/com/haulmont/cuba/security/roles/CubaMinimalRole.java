/*
 * Copyright 2021 Haulmont.
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

package com.haulmont.cuba.security.roles;

import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.security.entity.FilterEntity;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityui.role.annotation.ScreenPolicy;

@ResourceRole(name = "CUBA compatibility: minimal access",
        code = "cuba-minimal",
        scope = SecurityScope.UI)
public interface CubaMinimalRole {

    @ScreenPolicy(screenIds = {
            "addCondition",
            "backgroundWorkProgressWindow",
            "backgroundWorkWindow",
            "customConditionEditor",
            "customConditionFrame",
            "editWindowActions",
            "extendedEditWindowActions",
            "fileUploadDialog",
            "filterEditor",
            "filterSelect",
            "groupConditionFrame",
            "multiuploadDialog",
            "propertyConditionFrame",
            "runtimePropertiesFrame",
            "saveFilter",
            "saveSetInFolder",
            "inputDialog",
            "list-editor-popup",
            "date-interval-editor"
    })
    void screenPermissions();

    @EntityPolicy(entityClass = FilterEntity.class, actions = {EntityPolicyAction.READ})
    @EntityPolicy(entityClass = FileDescriptor.class, actions = {EntityPolicyAction.ALL})
    @EntityAttributePolicy(entityClass = FileDescriptor.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    void entityPermissions();
}
