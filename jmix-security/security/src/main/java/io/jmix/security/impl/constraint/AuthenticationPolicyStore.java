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
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.model.*;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.security.role.RoleGrantedAuthorityUtils;
import io.jmix.security.role.RowLevelRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

@Component("sec_AuthenticationPolicyStore")
public class AuthenticationPolicyStore implements PolicyStore {

    @Autowired
    protected CurrentAuthentication currentAuthentication;
    @Autowired
    protected ExtendedEntities extendedEntities;

    @Autowired
    protected ResourceRoleRepository resourceRoleRepository;

    @Autowired
    protected RowLevelRoleRepository rowLevelRoleRepository;

    @Autowired
    protected RoleGrantedAuthorityUtils roleGrantedAuthorityUtils;

    @Override
    public Stream<RowLevelPolicy> getRowLevelPolicies(MetaClass metaClass) {
        MetaClass originalMetaClass = extendedEntities.getOriginalMetaClass(metaClass);
        Set<String> suitableMetaClassNames = new HashSet<>();
        suitableMetaClassNames.add(metaClass.getName());
        if (originalMetaClass != null) {
            suitableMetaClassNames.add(originalMetaClass.getName());
            for (MetaClass ancestor : originalMetaClass.getAncestors()) {
                suitableMetaClassNames.add(ancestor.getName());
            }
        }
        for (MetaClass ancestor : metaClass.getAncestors()) {
            suitableMetaClassNames.add(ancestor.getName());
        }

        return extractRowLevelPoliciesFromAuthentication(rowLevelRole ->
                suitableMetaClassNames.stream()
                                .flatMap(metaClassName ->
                                        rowLevelRole.getAllRowLevelPoliciesIndex().getRowLevelPoliciesByEntityName(metaClassName).stream())
        );
    }

    @Override
    public Stream<ResourcePolicy> getEntityResourcePolicies(MetaClass metaClass) {
        MetaClass originalMetaClass = extendedEntities.getOriginalMetaClass(metaClass);
        return extractResourcePoliciesFromAuthenticationByScope(resourceRole -> {
            Set<String> resources = new HashSet<>();
            resources.add(metaClass.getName());
            if (originalMetaClass != null) {
                resources.add(originalMetaClass.getName());
            }
            return getPoliciesStreamByTypeAndResources(resourceRole, ResourcePolicyType.ENTITY, resources);
        });
    }

    @Override
    public Stream<ResourcePolicy> getEntityResourcePoliciesByWildcard(String wildcard) {
        return extractResourcePoliciesFromAuthenticationByScope(resourceRole ->
            getPoliciesStreamByTypeAndResources(resourceRole,
                    ResourcePolicyType.ENTITY,
                    Set.of(wildcard))
        );
    }

    @Override
    public Stream<ResourcePolicy> getEntityAttributesResourcePolicies(MetaClass metaClass, String attribute) {
        MetaClass originalMetaClass = extendedEntities.getOriginalMetaClass(metaClass);
        return extractResourcePoliciesFromAuthenticationByScope(resourceRole -> {
            Set<String> resources = new HashSet<>();
            resources.add(metaClass.getName() + "." + attribute);
            if (originalMetaClass != null) {
                resources.add(originalMetaClass.getName() + "." + attribute);
            }
            return getPoliciesStreamByTypeAndResources(resourceRole, ResourcePolicyType.ENTITY_ATTRIBUTE, resources);
        });
    }

    @Override
    public Stream<ResourcePolicy> getEntityAttributesResourcePoliciesByWildcard(String entityWildcard, String attributeWildcard) {
        return extractResourcePoliciesFromAuthenticationByScope(resourceRole ->
                getPoliciesStreamByTypeAndResources(resourceRole,
                        ResourcePolicyType.ENTITY_ATTRIBUTE,
                        Set.of(entityWildcard + "." + attributeWildcard)));
    }

    @Override
    public Stream<ResourcePolicy> getSpecificResourcePolicies(String resourceName) {
        return extractResourcePoliciesFromAuthenticationByScope(resourceRole ->
                getPoliciesStreamByTypeAndResources(resourceRole,
                        ResourcePolicyType.SPECIFIC,
                        Set.of(resourceName)));
    }

    @Override
    public Stream<ResourcePolicy> getGraphQLResourcePolicies(String resourceName) {
        return extractResourcePoliciesFromAuthenticationByScope(resourceRole ->
                getPoliciesStreamByTypeAndResources(resourceRole,
                        ResourcePolicyType.GRAPHQL,
                        Set.of(resourceName)));
    }

    protected Stream<ResourcePolicy> extractResourcePoliciesFromAuthenticationByScope(Function<ResourceRole, Stream<ResourcePolicy>> extractor) {
        Stream<ResourcePolicy> stream = Stream.empty();

        Authentication authentication = currentAuthentication.getAuthentication();
        String scope = getScope(authentication);
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (authority.getAuthority() != null) {
                String roleCode = authority.getAuthority();
                String defaultRolePrefix = roleGrantedAuthorityUtils.getDefaultRolePrefix();
                if (roleCode.startsWith(defaultRolePrefix)) {
                    roleCode = roleCode.substring(defaultRolePrefix.length());
                    ResourceRole resourceRole = resourceRoleRepository.getRoleByCode(roleCode);
                    if (isAppliedForScope(resourceRole, scope)) {
                        Stream<ResourcePolicy> extractedStream = extractor.apply(resourceRole);
                        if (extractedStream != null) {
                            stream = Stream.concat(stream, extractedStream);
                        }
                    }
                }
            }
        }

        return stream;
    }

    protected Stream<RowLevelPolicy> extractRowLevelPoliciesFromAuthentication(Function<RowLevelRole, Stream<RowLevelPolicy>> extractor) {
        Stream<RowLevelPolicy> stream = Stream.empty();

        Authentication authentication = currentAuthentication.getAuthentication();
        for (GrantedAuthority grantedAuthority : authentication.getAuthorities()) {
            if (grantedAuthority.getAuthority() != null) {
                String roleName = grantedAuthority.getAuthority();
                String defaultRowLevelRolePrefix = roleGrantedAuthorityUtils.getDefaultRowLevelRolePrefix();
                if (roleName.startsWith(defaultRowLevelRolePrefix)) {
                    roleName = roleName.substring(defaultRowLevelRolePrefix.length());
                    RowLevelRole rowLevelRole = rowLevelRoleRepository.getRoleByCode(roleName);
                    Stream<RowLevelPolicy> extractedStream = extractor.apply(rowLevelRole);
                    if (extractedStream != null) {
                        stream = Stream.concat(stream, extractedStream);
                    }
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

    protected boolean isAppliedForScope(ResourceRole resourceRole, @Nullable String scope) {
        return scope == null || resourceRole.getScopes().contains(scope);
    }

    protected Stream<ResourcePolicy> getPoliciesStreamByTypeAndResources(ResourceRole resourceRole,
                                                                         String policyType,
                                                                         Collection<String> resources) {
        return resources.stream()
                .flatMap(r -> resourceRole.getAllResourcePoliciesIndex().getPoliciesByTypeAndResource(policyType, r).stream());
    }
}
