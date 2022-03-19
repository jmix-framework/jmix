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

package io.jmix.security.impl.constraint;

import com.google.common.collect.ImmutableSet;
import io.jmix.core.accesscontext.GraphQLOperationAccessContext;
import io.jmix.core.constraint.GraphQLOperationConstraint;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component("sec_GraphQLOperationConstraintImpl")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GraphQLOperationConstraintImpl implements GraphQLOperationConstraint<GraphQLOperationAccessContext> {

    protected static final Set<String> PERMANENTLY_ALLOWED_OPERATIONS = ImmutableSet.of("userInfo");

    protected SecureOperations secureOperations;
    protected PolicyStore policyStore;

    @Autowired
    public void setSecureOperations(SecureOperations secureOperations) {
        this.secureOperations = secureOperations;
    }

    @Autowired
    public void setPolicyStore(PolicyStore policyStore) {
        this.policyStore = policyStore;
    }

    @Override
    public Class<GraphQLOperationAccessContext> getContextType() {
        return GraphQLOperationAccessContext.class;
    }

    @Override
    public void applyTo(GraphQLOperationAccessContext context) {
        if (!PERMANENTLY_ALLOWED_OPERATIONS.contains(context.getOperationName()) &&
                !secureOperations.isGraphQLPermitted(context.getOperationName(), policyStore)) {
            context.setDenied();
        }
    }
}
