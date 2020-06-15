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
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.ResourcePolicy;
import io.jmix.security.model.ResourcePolicyType;
import io.jmix.security.role.annotation.EntityPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Component(EntityPolicyExtractor.NAME)
public class EntityPolicyExtractor implements ResourcePolicyExtractor {

    public static final String NAME = "sec_EntityPolicyExtractor";

    private static final EntityPolicyAction[] ALL_CRUD_ACTIONS = {
            EntityPolicyAction.CREATE,
            EntityPolicyAction.READ,
            EntityPolicyAction.UPDATE,
            EntityPolicyAction.DELETE
    };

    protected Metadata metadata;

    @Autowired
    public EntityPolicyExtractor(Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public Collection<ResourcePolicy> extractResourcePolicies(Method method) {
        Set<ResourcePolicy> resourcePolicies = new HashSet<>();
        EntityPolicy[] entityPolicyAnnotations = method.getAnnotationsByType(EntityPolicy.class);
        for (EntityPolicy entityPolicyAnnotation : entityPolicyAnnotations) {
            MetaClass metaClass = metadata.getClass(entityPolicyAnnotation.entityClass());
            EntityPolicyAction[] actions = entityPolicyAnnotation.actions();
            if (Arrays.asList(actions).contains(EntityPolicyAction.ALL)) {
                actions = ALL_CRUD_ACTIONS;
            }
            for (EntityPolicyAction action : actions) {
                ResourcePolicy resourcePolicy = new ResourcePolicy(ResourcePolicyType.ENTITY,
                        metaClass.getName(),
                        action.getId());
                resourcePolicies.add(resourcePolicy);
            }
        }
        return resourcePolicies;
    }
}
