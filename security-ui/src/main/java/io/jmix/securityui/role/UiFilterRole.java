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

package io.jmix.securityui.role;

import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.SpecificPolicy;
import io.jmix.securityui.role.annotation.ScreenPolicy;
import io.jmix.ui.entity.FilterCondition;
import io.jmix.ui.entity.GroupFilterCondition;
import io.jmix.ui.entity.JpqlFilterCondition;
import io.jmix.ui.entity.PropertyFilterCondition;

@ResourceRole(name = "UI: edit filters", code = UiFilterRole.CODE, scope = SecurityScope.UI)
public interface UiFilterRole {

    String CODE = "ui-filter";

    @ScreenPolicy(screenIds = {
            "ui_FilterConfigurationModel.fragment",
            "ui_UiDataFilterConfigurationModel.fragment",
            "ui_AddConditionScreen"
    })
    @EntityPolicy(entityName = "ui_FilterConfiguration", actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityName = "ui_FilterConfiguration", attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @SpecificPolicy(resources = {
            "ui.filter.modifyConfiguration",
            "ui.filter.modifyGlobalConfiguration"
    })
    void configuration();

    @ScreenPolicy(screenIds = "ui_GroupFilterCondition.edit")
    @EntityPolicy(entityClass = GroupFilterCondition.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = GroupFilterCondition.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = FilterCondition.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = FilterCondition.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    void groupFilter();

    @ScreenPolicy(screenIds = "ui_PropertyFilterCondition.edit")
    @EntityPolicy(entityClass = PropertyFilterCondition.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = PropertyFilterCondition.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    void propertyFilter();

    @ScreenPolicy(screenIds = "ui_JpqlFilterCondition.edit")
    @EntityPolicy(entityClass = JpqlFilterCondition.class, actions = EntityPolicyAction.ALL)
    @EntityAttributePolicy(entityClass = JpqlFilterCondition.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @SpecificPolicy(resources = "ui.filter.modifyJpqlCondition")
    void jpqlFilter();
}
