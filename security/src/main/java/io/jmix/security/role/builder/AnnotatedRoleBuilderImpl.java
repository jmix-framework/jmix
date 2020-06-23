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

package io.jmix.security.role.builder;

import io.jmix.security.model.ResourcePolicy;
import io.jmix.security.model.Role;
import io.jmix.security.model.RoleSource;
import io.jmix.security.model.RowLevelPolicy;
import io.jmix.security.role.builder.extractor.ResourcePolicyExtractor;
import io.jmix.security.role.builder.extractor.RowLevelPolicyExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Component(AnnotatedRoleBuilder.NAME)
public class AnnotatedRoleBuilderImpl implements AnnotatedRoleBuilder {

    protected Collection<ResourcePolicyExtractor> resourcePolicyExtractors;
    private Collection<RowLevelPolicyExtractor> rowLevelPolicyExtractors;

    @Autowired
    public AnnotatedRoleBuilderImpl(Collection<ResourcePolicyExtractor> resourcePolicyExtractors,
                                    Collection<RowLevelPolicyExtractor> rowLevelPolicyExtractors) {
        this.resourcePolicyExtractors = resourcePolicyExtractors;
        this.rowLevelPolicyExtractors = rowLevelPolicyExtractors;
    }

    @Override
    public Role createRole(String className) {
        Class<?> roleClass;
        try {
            roleClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot find role class: " + className);
        }

        io.jmix.security.role.annotation.Role roleAnnotation = roleClass.getAnnotation(io.jmix.security.role.annotation.Role.class);
        Set<ResourcePolicy> resourcePolicies = new HashSet<>();
        Set<RowLevelPolicy> rowLevelPolicies = new HashSet<>();

        Method[] methods = roleClass.getMethods();
        for (Method method : methods) {
            for (ResourcePolicyExtractor policyExtractor : resourcePolicyExtractors) {
                resourcePolicies.addAll(policyExtractor.extractResourcePolicies(method));
            }

            for (RowLevelPolicyExtractor policyExtractor : rowLevelPolicyExtractors) {
                rowLevelPolicies.addAll(policyExtractor.extractRowLevelPolicies(method));
            }
        }

        Role role = new Role();
        role.setName(roleAnnotation.name());
        role.setCode(roleAnnotation.code());
        role.setResourcePolicies(resourcePolicies);
        role.setRowLevelPolicies(rowLevelPolicies);
        role.setSource(RoleSource.ANNOTATED_CLASS);
        return role;
    }
}
