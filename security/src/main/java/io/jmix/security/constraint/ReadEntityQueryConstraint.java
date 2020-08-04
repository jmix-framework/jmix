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

import com.google.common.base.Strings;
import io.jmix.core.constraint.RowLevelConstraint;
import io.jmix.data.impl.context.ReadEntityQueryContext;
import io.jmix.security.model.RowLevelPolicy;
import io.jmix.security.model.RowLevelPolicyAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component(ReadEntityQueryConstraint.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ReadEntityQueryConstraint implements RowLevelConstraint<ReadEntityQueryContext> {
    public static final String NAME = "sec_ReadEntityQueryConstraint";

    protected ResourcePolicyStore policyStore;
    protected PredefinedQueryParameters predefinedQueryParameters;

    @Autowired
    public void setPolicyStore(ResourcePolicyStore policyStore) {
        this.policyStore = policyStore;
    }

    @Autowired
    public void setPredefinedQueryParameters(PredefinedQueryParameters predefinedQueryParameters) {
        this.predefinedQueryParameters = predefinedQueryParameters;
    }

    @Override
    public Class<ReadEntityQueryContext> getContextType() {
        return ReadEntityQueryContext.class;
    }

    @Override
    public void applyTo(ReadEntityQueryContext context) {
        for (RowLevelPolicy policy : policyStore.getRowLevelPolicies(context.getEntityClass())) {
            if (policy.getAction() == RowLevelPolicyAction.READ) {
                if (!Strings.isNullOrEmpty(policy.getWhereClause()) || !Strings.isNullOrEmpty(policy.getJoinClause())) {
                    context.addJoinAndWhere(policy.getJoinClause(), policy.getWhereClause());
                }
            }
        }
        if (predefinedQueryParameters != null) {
            context.setQueryParamsProvider(param -> predefinedQueryParameters.getParameterValue(param));
        }
    }
}
