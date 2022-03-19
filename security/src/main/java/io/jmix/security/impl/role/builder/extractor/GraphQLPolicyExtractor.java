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

package io.jmix.security.impl.role.builder.extractor;

import io.jmix.security.model.ResourcePolicy;
import io.jmix.security.model.ResourcePolicyType;
import io.jmix.security.role.annotation.GraphQLPolicy;
import io.jmix.security.role.annotation.GraphQLPolicyContainer;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Component("sec_graphQLPolicyExtractor")
public class GraphQLPolicyExtractor implements ResourcePolicyExtractor {

    @Override
    public Collection<ResourcePolicy> extractResourcePolicies(Method method) {
        Set<ResourcePolicy> resourcePolicies = new HashSet<>();
        Set<GraphQLPolicy> annotations = AnnotatedElementUtils.findMergedRepeatableAnnotations(method,
                GraphQLPolicy.class, GraphQLPolicyContainer.class);
        for (GraphQLPolicy annotation : annotations) {
            for (String endpointID : annotation.operations()) {
                ResourcePolicy resourcePolicy = ResourcePolicy.builder(ResourcePolicyType.GRAPHQL, endpointID)
                        .withPolicyGroup(method.getName())
                        .build();
                resourcePolicies.add(resourcePolicy);
            }
        }
        return resourcePolicies;
    }
}
