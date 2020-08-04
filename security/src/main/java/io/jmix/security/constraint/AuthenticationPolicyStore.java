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

import io.jmix.core.ExtendedEntities;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.security.authentication.ResourcePolicyIndex;
import io.jmix.security.authentication.RowLevelPolicyIndex;
import io.jmix.security.authentication.SecuredAuthentication;
import io.jmix.security.model.ResourcePolicy;
import io.jmix.security.model.ResourcePolicyType;
import io.jmix.security.model.RowLevelPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component(AuthenticationPolicyStore.NAME)
public class AuthenticationPolicyStore implements ResourcePolicyStore {
    public static final String NAME = "sec_AuthenticationPolicyStore";

    @Autowired
    protected CurrentAuthentication currentAuthentication;
    @Autowired
    protected ExtendedEntities extendedEntities;

    @Override
    public Collection<RowLevelPolicy> getRowLevelPolicies(MetaClass metaClass) {
        MetaClass originalMetaClass = extendedEntities.getOriginalOrThisMetaClass(metaClass);

        return extractFromAuthentication(auth -> {
            Collection<RowLevelPolicy> result = new ArrayList<>(auth.getRowLevelPoliciesByIndex(RowLevelPolicyByEntityIndex.class,
                    index -> index.getPolicies(metaClass.getName())));

            for (MetaClass parent : originalMetaClass.getAncestors()) {
                result.addAll(auth.getRowLevelPoliciesByIndex(RowLevelPolicyByEntityIndex.class,
                        index -> index.getPolicies(parent.getName())));
            }

            return result;
        });
    }

    @Override
    public Collection<ResourcePolicy> getEntityResourcePolicies(MetaClass metaClass) {
        MetaClass originalMetaClass = extendedEntities.getOriginalOrThisMetaClass(metaClass);

        return extractFromAuthentication(auth -> auth.getResourcePoliciesByIndex(EntityResourcePolicyByEntityIndex.class,
                index -> index.getPolicies(originalMetaClass.getName())));
    }

    @Override
    public Collection<ResourcePolicy> getEntityAttributesResourcePolicies(MetaClass metaClass, String attribute) {
        return extractFromAuthentication(auth -> auth.getResourcePoliciesByIndex(EntityResourcePolicyByAttributesIndex.class,
                index -> index.getPolicies(metaClass.getName(), attribute)));
    }

    @Override
    public Collection<ResourcePolicy> getSpecificResourcePolicies(String resourceName) {
        return extractFromAuthentication(auth ->
                auth.getResourcePoliciesByIndex(SpecificResourcePolicyByNameIndex.class, index -> index.getPolicies(resourceName)));
    }

    protected <T> Collection<T> extractFromAuthentication(
            Function<SecuredAuthentication, Collection<T>> extractor) {
        if (currentAuthentication.getAuthentication() instanceof SecuredAuthentication) {
            Collection<T> result = extractor.apply((SecuredAuthentication) currentAuthentication.getAuthentication());
            return result == null ? Collections.emptyList() : result;
        }
        return Collections.emptyList();
    }

    public static class RowLevelPolicyByEntityIndex implements RowLevelPolicyIndex {
        protected Map<String, List<RowLevelPolicy>> policyByEntity;

        @Override
        public void indexAll(Collection<RowLevelPolicy> rowLevelPolicies) {
            policyByEntity = rowLevelPolicies.stream()
                    .collect(Collectors.groupingBy(RowLevelPolicy::getEntityName));
        }

        public Collection<RowLevelPolicy> getPolicies(String entityName) {
            return policyByEntity.get(entityName);
        }
    }

    public static class EntityResourcePolicyByEntityIndex implements ResourcePolicyIndex {
        protected Map<String, List<ResourcePolicy>> policyByEntity;

        @Override
        public void indexAll(Collection<ResourcePolicy> resourcePolicies) {
            policyByEntity = resourcePolicies.stream()
                    .filter(p -> Objects.equals(p.getType(), ResourcePolicyType.ENTITY))
                    .collect(Collectors.groupingBy(ResourcePolicy::getResource));
        }

        public Collection<ResourcePolicy> getPolicies(String entityName) {
            return policyByEntity.get(entityName);
        }
    }

    public static class EntityResourcePolicyByAttributesIndex implements ResourcePolicyIndex {
        protected Map<String, List<ResourcePolicy>> policyByAttributes;

        @Override
        public void indexAll(Collection<ResourcePolicy> resourcePolicies) {
            policyByAttributes = resourcePolicies.stream()
                    .filter(p -> Objects.equals(p.getType(), ResourcePolicyType.ENTITY_ATTRIBUTE))
                    .collect(Collectors.groupingBy(ResourcePolicy::getResource));
        }

        public Collection<ResourcePolicy> getPolicies(String entityName, String attribute) {
            return policyByAttributes.get(entityName + "." + attribute);
        }
    }

    public static class SpecificResourcePolicyByNameIndex implements ResourcePolicyIndex {
        protected Map<String, List<ResourcePolicy>> policyByName;

        @Override
        public void indexAll(Collection<ResourcePolicy> resourcePolicies) {
            policyByName = resourcePolicies.stream()
                    .filter(p -> Objects.equals(p.getType(), ResourcePolicyType.SPECIFIC))
                    .collect(Collectors.groupingBy(ResourcePolicy::getResource));
        }

        public Collection<ResourcePolicy> getPolicies(String name) {
            return policyByName.get(name);
        }
    }
}
