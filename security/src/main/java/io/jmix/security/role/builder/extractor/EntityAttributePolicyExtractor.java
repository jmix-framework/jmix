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

package io.jmix.security.role.builder.extractor;

import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.ResourcePolicy;
import io.jmix.security.model.ResourcePolicyType;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Component(EntityAttributePolicyExtractor.NAME)
public class EntityAttributePolicyExtractor implements ResourcePolicyExtractor {

    public static final String NAME = "sec_EntityAttributePolicyExtractor";

    protected Metadata metadata;

    @Autowired
    public EntityAttributePolicyExtractor(Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public Collection<ResourcePolicy> extractResourcePolicies(Method method) {
        Set<ResourcePolicy> resourcePolicies = new HashSet<>();
        EntityAttributePolicy[] policyAnnotations = method.getAnnotationsByType(EntityAttributePolicy.class);
        for (EntityAttributePolicy policyAnnotation : policyAnnotations) {
            MetaClass metaClass = metadata.getClass(policyAnnotation.entityClass());
            for (String attribute : policyAnnotation.attributes()) {
                for (EntityAttributePolicyAction action : policyAnnotation.actions()) {
                    String resource = metaClass.getName() + "." + attribute;
                    ResourcePolicy resourcePolicy = new ResourcePolicy(ResourcePolicyType.ENTITY_ATTRIBUTE,
                            resource,
                            action.getId());
                    resourcePolicies.add(resourcePolicy);
                }
            }
        }
        return resourcePolicies;
    }
}
