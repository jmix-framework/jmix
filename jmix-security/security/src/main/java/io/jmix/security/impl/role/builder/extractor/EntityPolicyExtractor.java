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

package io.jmix.security.impl.role.builder.extractor;

import com.google.common.base.Strings;
import io.jmix.core.Metadata;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.ResourcePolicy;
import io.jmix.security.model.ResourcePolicyType;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.EntityPolicyContainer;
import io.jmix.security.role.annotation.NullEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Component("sec_EntityPolicyExtractor")
public class EntityPolicyExtractor implements ResourcePolicyExtractor {

    private static final EntityPolicyAction[] ALL_CRUD_ACTIONS = {
            EntityPolicyAction.CREATE,
            EntityPolicyAction.READ,
            EntityPolicyAction.UPDATE,
            EntityPolicyAction.DELETE
    };

    private static final Logger log = LoggerFactory.getLogger(EntityPolicyExtractor.class);

    protected Metadata metadata;

    protected PolicyExtractorUtils policyExtractorUtils;

    @Autowired
    public EntityPolicyExtractor(Metadata metadata, PolicyExtractorUtils policyExtractorUtils) {
        this.metadata = metadata;
        this.policyExtractorUtils = policyExtractorUtils;
    }

    @Override
    public Collection<ResourcePolicy> extractResourcePolicies(Method method) {
        Set<ResourcePolicy> resourcePolicies = new HashSet<>();
        Set<EntityPolicy> annotations = AnnotatedElementUtils.findMergedRepeatableAnnotations(method,
                EntityPolicy.class, EntityPolicyContainer.class);
        for (EntityPolicy annotation : annotations) {
            Class<?> entityClass = annotation.entityClass();
            String entityName = annotation.entityName();
            if (entityClass != NullEntity.class) {
                entityName = policyExtractorUtils.getEntityNameByEntityClass(entityClass);
            } else if (Strings.isNullOrEmpty(entityName)) {
                log.error("Neither entityClass, nor entityName is defined for the EntityPolicy annotation. " +
                        "Class: {}, method: {}", method.getClass().getName(), method.getName());
                continue;
            }
            EntityPolicyAction[] actions = annotation.actions();
            if (Arrays.asList(actions).contains(EntityPolicyAction.ALL)) {
                actions = ALL_CRUD_ACTIONS;
            }
            for (EntityPolicyAction action : actions) {
                ResourcePolicy resourcePolicy = ResourcePolicy.builder(ResourcePolicyType.ENTITY, entityName)
                        .withAction(action.getId())
                        .withPolicyGroup(method.getName())
                        .build();
                resourcePolicies.add(resourcePolicy);
            }
        }
        return resourcePolicies;
    }
}
