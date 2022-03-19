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

package io.jmix.securitydata.constraint;

import io.jmix.core.accesscontext.InMemoryCrudEntityContext;
import io.jmix.core.constraint.InMemoryConstraint;
import io.jmix.core.constraint.RowLevelConstraint;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.model.RowLevelPolicyAction;
import io.jmix.security.model.RowLevelPolicyType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("sec_InMemoryCrudEntityConstraint")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class InMemoryCrudEntityConstraint
        implements InMemoryConstraint<InMemoryCrudEntityContext>, RowLevelConstraint<InMemoryCrudEntityContext> {

    protected PolicyStore policyStore;

    @Autowired
    public void setPolicyStore(PolicyStore policyStore) {
        this.policyStore = policyStore;
    }

    @Override
    public Class<InMemoryCrudEntityContext> getContextType() {
        return InMemoryCrudEntityContext.class;
    }

    @Override
    public void applyTo(InMemoryCrudEntityContext context) {
        policyStore.getRowLevelPolicies(context.getEntityClass()).forEach(policy -> {
            if (policy.getType() == RowLevelPolicyType.PREDICATE) {
                if (policy.getAction() == RowLevelPolicyAction.CREATE) {
                    context.addCreatePredicate(policy.getBiPredicate());
                } else if (policy.getAction() == RowLevelPolicyAction.READ) {
                    context.addReadPredicate(policy.getBiPredicate());
                } else if (policy.getAction() == RowLevelPolicyAction.UPDATE) {
                    context.addUpdatePredicate(policy.getBiPredicate());
                } else if (policy.getAction() == RowLevelPolicyAction.DELETE) {
                    context.addDeletePredicate(policy.getBiPredicate());
                }
            }
        });
    }
}
