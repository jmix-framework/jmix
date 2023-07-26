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

package io.jmix.dynattrflowui.role;

import io.jmix.dynattr.model.Category;
import io.jmix.dynattr.model.CategoryAttribute;
import io.jmix.dynattr.model.CategoryAttributeConfiguration;
import io.jmix.dynattr.model.CategoryAttributeValue;
import io.jmix.dynattrflowui.impl.model.AttributeLocalizedEnumValue;
import io.jmix.dynattrflowui.impl.model.AttributeLocalizedValue;
import io.jmix.dynattrflowui.impl.model.TargetViewComponent;
import io.jmix.dynattrflowui.view.category.CategoryBrowse;
import io.jmix.dynattrflowui.view.category.CategoryEdit;
import io.jmix.dynattrflowui.view.categoryattr.AttributeEnumerationScreen;
import io.jmix.dynattrflowui.view.categoryattr.CategoryAttrsEdit;
import io.jmix.dynattrflowui.view.categoryattr.CategoryAttrsFragment;
import io.jmix.dynattrflowui.view.localization.AttributeLocalizationFragment;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "Dynamic Attributes: administration", code = DynamicAttributesRole.CODE, scope = SecurityScope.UI)
public interface DynamicAttributesRole {
    String CODE = "dynamic-attributes";

    @EntityPolicy(entityClass = Category.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = CategoryAttribute.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = CategoryAttributeValue.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = CategoryAttributeConfiguration.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = TargetViewComponent.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = AttributeLocalizedEnumValue.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = AttributeLocalizedValue.class, actions = {EntityPolicyAction.ALL})

    @EntityAttributePolicy(entityClass = Category.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityAttributePolicy(entityClass = CategoryAttribute.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityAttributePolicy(entityClass = CategoryAttributeValue.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityAttributePolicy(entityClass = CategoryAttributeConfiguration.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityAttributePolicy(entityClass = TargetViewComponent.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityAttributePolicy(entityClass = AttributeLocalizedEnumValue.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityAttributePolicy(entityClass = AttributeLocalizedValue.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)

    @ViewPolicy(viewClasses = {CategoryBrowse.class, CategoryEdit.class, CategoryAttrsEdit.class,
            CategoryAttrsFragment.class, AttributeEnumerationScreen.class, AttributeLocalizationFragment.class})
    @MenuPolicy(menuIds = {"dynat_Category.browse"})
    void dynamicAttributes();
}