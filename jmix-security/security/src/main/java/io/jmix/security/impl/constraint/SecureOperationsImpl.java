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

package io.jmix.security.impl.constraint;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.ResourcePolicy;
import io.jmix.security.model.ResourcePolicyEffect;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("sec_SecureOperations")
public class SecureOperationsImpl implements SecureOperations {

    public boolean isEntityCreatePermitted(MetaClass metaClass, PolicyStore policyStore) {
        return isEntityOperationPermitted(metaClass, EntityPolicyAction.CREATE, policyStore);
    }

    @Override
    public boolean isEntityReadPermitted(MetaClass metaClass, PolicyStore policyStore) {
        return isEntityOperationPermitted(metaClass, EntityPolicyAction.READ, policyStore);
    }

    @Override
    public boolean isEntityUpdatePermitted(MetaClass metaClass, PolicyStore policyContainer) {
        return isEntityOperationPermitted(metaClass, EntityPolicyAction.UPDATE, policyContainer);
    }

    @Override
    public boolean isEntityDeletePermitted(MetaClass metaClass, PolicyStore policyContainer) {
        return isEntityOperationPermitted(metaClass, EntityPolicyAction.DELETE, policyContainer);
    }

    protected boolean isEntityOperationPermitted(MetaClass metaClass, EntityPolicyAction entityPolicyAction,
                                                 PolicyStore policyStore) {

        boolean result = policyStore.getEntityResourcePolicies(metaClass)
                .anyMatch(policy -> isEntityOperationPermitted(policy, entityPolicyAction));

        if (!result) {
            result = policyStore.getEntityResourcePoliciesByWildcard("*")
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
    public boolean isEntityAttrReadPermitted(MetaPropertyPath metaPropertyPath, PolicyStore policyStore) {
        for (MetaProperty metaProperty : metaPropertyPath.getMetaProperties()) {
            if (!isEntityAttrPermitted(metaProperty.getDomain(), metaProperty.getName(),
                    new EntityAttributePolicyAction[]{EntityAttributePolicyAction.VIEW, EntityAttributePolicyAction.MODIFY},
                    policyStore)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isEntityAttrUpdatePermitted(MetaPropertyPath metaPropertyPath, PolicyStore policyStore) {
        for (MetaProperty metaProperty : metaPropertyPath.getMetaProperties()) {
            if (!isEntityAttrPermitted(metaProperty.getDomain(), metaProperty.getName(),
                    new EntityAttributePolicyAction[]{EntityAttributePolicyAction.MODIFY},
                    policyStore)) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean isEntityAttrPermitted(MetaClass metaClass, String name,
                                            EntityAttributePolicyAction[] policyActions,
                                            PolicyStore policyStore) {

        boolean result = policyStore.getEntityAttributesResourcePolicies(metaClass, name)
                .anyMatch(policy -> isEntityAttrPermitted(policy, policyActions));

        if (!result) {
            result = policyStore.getEntityAttributesResourcePolicies(metaClass, "*")
                    .anyMatch(policy -> isEntityAttrPermitted(policy, policyActions));
        }

        if (!result) {
            result = policyStore.getEntityAttributesResourcePoliciesByWildcard("*", "*")
                    .anyMatch(policy -> isEntityAttrPermitted(policy, policyActions));
        }

        return result;
    }

    protected boolean isEntityAttrPermitted(ResourcePolicy policy, EntityAttributePolicyAction[] policyActions) {
        for (EntityAttributePolicyAction policyAction : policyActions) {
            if (Objects.equals(policy.getAction(), policyAction.getId())
                    && Objects.equals(policy.getEffect(), ResourcePolicyEffect.ALLOW)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isSpecificPermitted(String resourceName, PolicyStore policyStore) {
        boolean result = policyStore.getSpecificResourcePolicies(resourceName)
                .anyMatch(policy -> Objects.equals(policy.getEffect(), ResourcePolicyEffect.ALLOW));

        if (!result) {
            result = policyStore.getSpecificResourcePolicies("*")
                    .anyMatch(policy -> Objects.equals(policy.getEffect(), ResourcePolicyEffect.ALLOW));
        }

        return result;
    }

    @Override
    public boolean isGraphQLPermitted(String resourceName, PolicyStore policyStore) {
        boolean result = policyStore.getGraphQLResourcePolicies(resourceName)
                .anyMatch(policy -> Objects.equals(policy.getEffect(), ResourcePolicyEffect.ALLOW));

        if (!result) {
            result = policyStore.getGraphQLResourcePolicies("*")
                    .anyMatch(policy -> Objects.equals(policy.getEffect(), ResourcePolicyEffect.ALLOW));
        }

        return result;
    }
}
