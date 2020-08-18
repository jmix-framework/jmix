/*
 * Copyright 2020 Haulmont.
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

package io.jmix.security.constraint;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.ResourcePolicy;
import io.jmix.security.model.ResourcePolicyEffect;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component(SecureOperations.NAME)
public class SecureOperationsImpl implements SecureOperations {

    public boolean isEntityCreatePermitted(MetaClass metaClass, ResourcePolicyStore policyStore) {
        return isEntityOperationPermitted(metaClass, EntityPolicyAction.CREATE, policyStore);
    }

    @Override
    public boolean isEntityReadPermitted(MetaClass metaClass, ResourcePolicyStore policyStore) {
        return isEntityOperationPermitted(metaClass, EntityPolicyAction.READ, policyStore);
    }

    @Override
    public boolean isEntityUpdatePermitted(MetaClass metaClass, ResourcePolicyStore policyContainer) {
        return isEntityOperationPermitted(metaClass, EntityPolicyAction.UPDATE, policyContainer);
    }

    @Override
    public boolean isEntityDeletePermitted(MetaClass metaClass, ResourcePolicyStore policyContainer) {
        return false;
    }

    protected boolean isEntityOperationPermitted(MetaClass metaClass, EntityPolicyAction entityPolicyAction,
                                                 ResourcePolicyStore policyStore) {

        boolean result = policyStore.getEntityResourcePolicies(metaClass).stream()
                .anyMatch(policy -> isEntityOperationPermitted(policy, entityPolicyAction));

        if (!result) {
            result = policyStore.getEntityResourcePoliciesByWildcard("*").stream()
                    .anyMatch(policy -> isEntityOperationPermitted(policy, entityPolicyAction));
        }

        return result;
    }

    protected boolean isEntityOperationPermitted(ResourcePolicy policy, EntityPolicyAction entityPolicyAction) {
        return Objects.equals(policy.getEffect(), ResourcePolicyEffect.ALLOW) &&
                (Objects.equals(policy.getAction(), entityPolicyAction.getId()) ||
                        Objects.equals(policy.getAction(), EntityPolicyAction.ALL.getId()));
    }

    @Override
    public boolean isEntityAttrReadPermitted(MetaPropertyPath metaPropertyPath, ResourcePolicyStore policyStore) {
        for (MetaProperty metaProperty : metaPropertyPath.getMetaProperties()) {
            if (!isEntityAttrPermitted(metaProperty.getDomain(), metaProperty.getName(),
                    EntityAttributePolicyAction.READ, policyStore)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isEntityAttrUpdatePermitted(MetaPropertyPath metaPropertyPath, ResourcePolicyStore policyStore) {
        for (MetaProperty metaProperty : metaPropertyPath.getMetaProperties()) {
            if (!isEntityAttrPermitted(metaProperty.getDomain(), metaProperty.getName(),
                    EntityAttributePolicyAction.UPDATE, policyStore)) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean isEntityAttrPermitted(MetaClass metaClass, String name,
                                            EntityAttributePolicyAction policyAction,
                                            ResourcePolicyStore policyStore) {

        boolean result = policyStore.getEntityAttributesResourcePolicies(metaClass, name).stream()
                .anyMatch(policy -> isEntityAttrPermitted(policy, policyAction));

        if (!result) {
            result = policyStore.getEntityAttributesResourcePolicies(metaClass, "*").stream()
                    .anyMatch(policy -> isEntityAttrPermitted(policy, policyAction));
        }

        if (!result) {
            result = policyStore.getEntityAttributesResourcePoliciesByWildcard("*", "*").stream()
                    .anyMatch(policy -> isEntityAttrPermitted(policy, policyAction));
        }

        return result;
    }

    protected boolean isEntityAttrPermitted(ResourcePolicy policy, EntityAttributePolicyAction policyAction) {
        return Objects.equals(policy.getAction(), policyAction.getId()) && Objects.equals(policy.getEffect(), ResourcePolicyEffect.ALLOW);
    }

    @Override
    public boolean isSpecificPermitted(String resourceName, ResourcePolicyStore policyStore) {
        boolean result = policyStore.getSpecificResourcePolicies(resourceName).stream()
                .anyMatch(policy -> Objects.equals(policy.getEffect(), ResourcePolicyEffect.ALLOW));

        if (!result) {
            result = policyStore.getSpecificResourcePolicies("*").stream()
                    .anyMatch(policy -> Objects.equals(policy.getEffect(), ResourcePolicyEffect.ALLOW));
        }

        return result;
    }
}
