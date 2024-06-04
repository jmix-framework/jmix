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

package io.jmix.securityflowui.impl.constraint;

import io.jmix.core.security.ClientDetails;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.security.model.ResourcePolicy;
import io.jmix.security.model.ResourcePolicyType;
import io.jmix.security.model.ResourceRole;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.security.role.RoleGrantedAuthorityUtils;
import io.jmix.securityflowui.constraint.UiPolicyStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

@Component("sec_AuthenticationUiPolicyStore")
public class AuthenticationUiPolicyStore implements UiPolicyStore {

    @Autowired
    protected CurrentAuthentication currentAuthentication;

    @Autowired
    protected ResourceRoleRepository resourceRoleRepository;

    @Autowired
    protected RoleGrantedAuthorityUtils roleGrantedAuthorityUtils;

    @Override
    public Stream<ResourcePolicy> getViewResourcePolicies(String viewId) {
        return extractFromAuthenticationByScope(resourceRole ->
                getPoliciesStreamByTypeAndResources(resourceRole, ResourcePolicyType.SCREEN, Set.of(viewId))
        );
    }

    @Override
    public Stream<ResourcePolicy> getMenuResourcePolicies(String menuId) {
        return extractFromAuthenticationByScope(resourceRole ->
                        getPoliciesStreamByTypeAndResources(resourceRole, ResourcePolicyType.MENU, Set.of(menuId))
        );
    }

    protected <T> Stream<T> extractFromAuthenticationByScope(Function<ResourceRole, Stream<T>> extractor) {
        Stream<T> stream = Stream.empty();

        Authentication authentication = currentAuthentication.getAuthentication();
        String scope = getScope(authentication);
        for (GrantedAuthority grantedAuthority : authentication.getAuthorities()) {
            if (grantedAuthority.getAuthority() != null) {
                String roleCode = grantedAuthority.getAuthority();
                String defaultRolePrefix = roleGrantedAuthorityUtils.getDefaultRolePrefix();
                if (roleCode.startsWith(defaultRolePrefix)) {
                    roleCode = roleCode.substring(defaultRolePrefix.length());
                    ResourceRole resourceRole = resourceRoleRepository.findRoleByCode(roleCode);
                    if (resourceRole != null) {
                        if (isAppliedForScope(resourceRole, scope)) {
                            Stream<T> extractedStream = extractor.apply(resourceRole);
                            if (extractedStream != null) {
                                stream = Stream.concat(stream, extractedStream);
                            }
                        }
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
