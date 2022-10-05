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

import io.jmix.core.ExtendedEntities;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.ClientDetails;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.security.authentication.PolicyAwareGrantedAuthority;
import io.jmix.security.authentication.ResourcePolicyIndex;
import io.jmix.security.authentication.RowLevelPolicyIndex;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.model.ResourcePolicy;
import io.jmix.security.model.ResourcePolicyType;
import io.jmix.security.model.RowLevelPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component("sec_AuthenticationPolicyStore")
public class AuthenticationPolicyStore implements PolicyStore {

    @Autowired
    protected CurrentAuthentication currentAuthentication;
    @Autowired
    protected ExtendedEntities extendedEntities;

    @Override
    public Stream<RowLevelPolicy> getRowLevelPolicies(MetaClass metaClass) {
        MetaClass originalMetaClass = extendedEntities.getOriginalOrThisMetaClass(metaClass);

        return extractFromAuthentication(authority -> {
            Stream<RowLevelPolicy> result = authority.getRowLevelPoliciesByIndex(RowLevelPolicyByEntityIndex.class,
                    index -> index.getPolicies(metaClass.getName()));

            for (MetaClass parent : originalMetaClass.getAncestors()) {
                result = Stream.concat(result, authority.getRowLevelPoliciesByIndex(RowLevelPolicyByEntityIndex.class,
                        index -> index.getPolicies(parent.getName())));
            }

            return result;
        });
    }

    @Override
    public Stream<ResourcePolicy> getEntityResourcePolicies(MetaClass metaClass) {
        MetaClass originalMetaClass = extendedEntities.getOriginalMetaClass(metaClass);
        return extractFromAuthenticationByScope(authority -> authority.getResourcePoliciesByIndex(EntityResourcePolicyByEntityIndex.class,
                index -> {
                    Stream<ResourcePolicy> metaClassPolicies = index.getPolicies(metaClass.getName());
                    //if entity replaces some other entity then we return policies for both of them
                    if (originalMetaClass != null) {
                        Stream<ResourcePolicy> originalMetaClassPolicies = index.getPolicies(originalMetaClass.getName());
                        return Stream.concat(metaClassPolicies, originalMetaClassPolicies);
                    }
                    return metaClassPolicies;
                }));
    }

    @Override
    public Stream<ResourcePolicy> getEntityResourcePoliciesByWildcard(String wildcard) {
        return extractFromAuthenticationByScope(authority -> authority.getResourcePoliciesByIndex(EntityResourcePolicyByEntityIndex.class,
                index -> index.getPolicies(wildcard)));
    }

    @Override
    public Stream<ResourcePolicy> getEntityAttributesResourcePolicies(MetaClass metaClass, String attribute) {
        MetaClass originalMetaClass = extendedEntities.getOriginalMetaClass(metaClass);
        return extractFromAuthenticationByScope(authority -> authority.getResourcePoliciesByIndex(EntityResourcePolicyByAttributesIndex.class,
                index -> {
                    Stream<ResourcePolicy> metaClassPolicies = index.getPolicies(metaClass.getName(), attribute);
                    //if entity replaces some other entity then we return policies for both of them
                    if (originalMetaClass != null) {
                        Stream<ResourcePolicy> originalMetaClassPolicies = index.getPolicies(originalMetaClass.getName(), attribute);
                        return Stream.concat(metaClassPolicies, originalMetaClassPolicies);
                    }
                    return metaClassPolicies;
                }));
    }

    @Override
    public Stream<ResourcePolicy> getEntityAttributesResourcePoliciesByWildcard(String entityWildcard, String attributeWildcard) {
        return extractFromAuthenticationByScope(authority -> authority.getResourcePoliciesByIndex(EntityResourcePolicyByAttributesIndex.class,
                index -> index.getPolicies(entityWildcard, attributeWildcard)));
    }

    @Override
    public Stream<ResourcePolicy> getSpecificResourcePolicies(String resourceName) {
        return extractFromAuthenticationByScope(authority ->
                authority.getResourcePoliciesByIndex(SpecificResourcePolicyByNameIndex.class, index -> index.getPolicies(resourceName)));
    }

    @Override
    public Stream<ResourcePolicy> getGraphQLResourcePolicies(String resourceName) {
        return extractFromAuthenticationByScope(authority -> authority
                .getResourcePoliciesByIndex(GraphQLResourcePolicyByNameIndex.class, index -> index.getPolicies(resourceName)));
    }

    protected <T> Stream<T> extractFromAuthenticationByScope(Function<PolicyAwareGrantedAuthority, Stream<T>> extractor) {
        Stream<T> stream = Stream.empty();

        Authentication authentication = currentAuthentication.getAuthentication();
        String scope = getScope(authentication);
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (authority instanceof PolicyAwareGrantedAuthority) {
                PolicyAwareGrantedAuthority policyAwareAuthority = (PolicyAwareGrantedAuthority) authority;
                if (isAppliedForScope(policyAwareAuthority, scope)) {
                    Stream<T> extractedStream = extractor.apply(policyAwareAuthority);
                    if (extractedStream != null) {
                        stream = Stream.concat(stream, extractedStream);
                    }
                }
            }
        }

        return stream;
    }

    protected <T> Stream<T> extractFromAuthentication(Function<PolicyAwareGrantedAuthority, Stream<T>> extractor) {
        Stream<T> stream = Stream.empty();

        Authentication authentication = currentAuthentication.getAuthentication();
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (authority instanceof PolicyAwareGrantedAuthority) {
                PolicyAwareGrantedAuthority policyAwareAuthority = (PolicyAwareGrantedAuthority) authority;
                Stream<T> extractedStream = extractor.apply(policyAwareAuthority);
                if (extractedStream != null) {
                    stream = Stream.concat(stream, extractedStream);
                }
            }
        }

        return stream;
    }

    @Nullable
    protected String getScope(Authentication authentication) {
        Object details = authentication.getDetails();
        if (details instanceof ClientDetails) {
            return ((ClientDetails) details).getScope();
        }
        return null;
    }

    protected boolean isAppliedForScope(PolicyAwareGrantedAuthority policyAwareAuthority, @Nullable String scope) {
        return scope == null || policyAwareAuthority.getScopes().contains(scope);
    }

    public static class RowLevelPolicyByEntityIndex implements RowLevelPolicyIndex {
        private static final long serialVersionUID = -783139963235923669L;

        protected Map<String, List<RowLevelPolicy>> policyByEntity;

        @Override
        public void indexAll(Collection<RowLevelPolicy> rowLevelPolicies) {
            policyByEntity = rowLevelPolicies.stream()
                    .collect(Collectors.groupingBy(RowLevelPolicy::getEntityName));
        }

        public Stream<RowLevelPolicy> getPolicies(String entityName) {
            return policyByEntity.getOrDefault(entityName, Collections.emptyList()).stream();
        }
    }

    public static class EntityResourcePolicyByEntityIndex implements ResourcePolicyIndex {
        private static final long serialVersionUID = -8714429649726170927L;

        protected Map<String, List<ResourcePolicy>> policyByEntity;

        @Override
        public void indexAll(Collection<ResourcePolicy> resourcePolicies) {
            policyByEntity = resourcePolicies.stream()
                    .filter(p -> Objects.equals(p.getType(), ResourcePolicyType.ENTITY))
                    .collect(Collectors.groupingBy(ResourcePolicy::getResource));
        }

        public Stream<ResourcePolicy> getPolicies(String entityName) {
            return policyByEntity.getOrDefault(entityName, Collections.emptyList()).stream();
        }
    }

    public static class EntityResourcePolicyByAttributesIndex implements ResourcePolicyIndex {
        private static final long serialVersionUID = 3834313189763360074L;

        protected Map<String, List<ResourcePolicy>> policyByAttributes;

        @Override
        public void indexAll(Collection<ResourcePolicy> resourcePolicies) {
            policyByAttributes = resourcePolicies.stream()
                    .filter(p -> Objects.equals(p.getType(), ResourcePolicyType.ENTITY_ATTRIBUTE))
                    .collect(Collectors.groupingBy(ResourcePolicy::getResource));
        }

        public Stream<ResourcePolicy> getPolicies(String entityName, String attribute) {
            return policyByAttributes.getOrDefault(entityName + "." + attribute, Collections.emptyList()).stream();
        }
    }

    public static class SpecificResourcePolicyByNameIndex implements ResourcePolicyIndex {
        private static final long serialVersionUID = 4454925194590197149L;

        protected Map<String, List<ResourcePolicy>> policyByName;

        @Override
        public void indexAll(Collection<ResourcePolicy> resourcePolicies) {
            policyByName = resourcePolicies.stream()
                    .filter(p -> Objects.equals(p.getType(), ResourcePolicyType.SPECIFIC))
                    .collect(Collectors.groupingBy(ResourcePolicy::getResource));
        }

        public Stream<ResourcePolicy> getPolicies(String name) {
            return policyByName.getOrDefault(name, Collections.emptyList()).stream();
        }
    }

    public static class GraphQLResourcePolicyByNameIndex implements ResourcePolicyIndex {

        protected Map<String, List<ResourcePolicy>> policyByName;

        @Override
        public void indexAll(Collection<ResourcePolicy> resourcePolicies) {
            policyByName = resourcePolicies.stream()
                    .filter(p -> Objects.equals(p.getType(), ResourcePolicyType.GRAPHQL))
                    .collect(Collectors.groupingBy(ResourcePolicy::getResource));
        }

        public Stream<ResourcePolicy> getPolicies(String name) {
            return policyByName.getOrDefault(name, Collections.emptyList()).stream();
        }
    }
}
