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

package io.jmix.dynattrui.role;

import io.jmix.dynattr.model.Category;
import io.jmix.dynattr.model.CategoryAttribute;
import io.jmix.dynattr.model.CategoryAttributeConfiguration;
import io.jmix.dynattr.model.CategoryAttributeValue;
import io.jmix.dynattrui.impl.model.AttributeLocalizedEnumValue;
import io.jmix.dynattrui.impl.model.AttributeLocalizedValue;
import io.jmix.dynattrui.impl.model.TargetScreenComponent;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityui.role.annotation.MenuPolicy;
import io.jmix.securityui.role.annotation.ScreenPolicy;

@ResourceRole(name = "Dynamic Attributes: read-only access", code = DynamicAttributesReadOnlyRole.CODE, scope = SecurityScope.UI)
public interface DynamicAttributesReadOnlyRole {
    String CODE = "dynamic-attributes-read-only";

    @EntityPolicy(entityClass = Category.class, actions = {EntityPolicyAction.READ})
    @EntityPolicy(entityClass = CategoryAttribute.class, actions = {EntityPolicyAction.READ})
    @EntityPolicy(entityClass = CategoryAttributeValue.class, actions = {EntityPolicyAction.READ})
    @EntityPolicy(entityClass = CategoryAttributeConfiguration.class, actions = {EntityPolicyAction.READ})
    @EntityPolicy(entityClass = TargetScreenComponent.class, actions = {EntityPolicyAction.READ})
    @EntityPolicy(entityClass = AttributeLocalizedEnumValue.class, actions = {EntityPolicyAction.READ})
    @EntityPolicy(entityClass = AttributeLocalizedValue.class, actions = {EntityPolicyAction.READ})

    @EntityAttributePolicy(entityClass = Category.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityAttributePolicy(entityClass = CategoryAttribute.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityAttributePolicy(entityClass = CategoryAttributeValue.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityAttributePolicy(entityClass = CategoryAttributeConfiguration.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityAttributePolicy(entityClass = TargetScreenComponent.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityAttributePolicy(entityClass = AttributeLocalizedEnumValue.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityAttributePolicy(entityClass = AttributeLocalizedValue.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)

    @ScreenPolicy(screenIds = {"dynat_Category.browse", "dynat_Category.edit", "dynat_CategoryAttribute.edit",
            "dynat_CategoryAttribute.fragment", "dynat_AttributeEnumerationScreen", "dynat_AttributeLocalizationFragment"})
    @MenuPolicy(menuIds = {"dynat_Category.browse"})
    void dynamicAttributes();
}