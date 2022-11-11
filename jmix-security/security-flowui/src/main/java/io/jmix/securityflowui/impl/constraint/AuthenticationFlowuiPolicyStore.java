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
import io.jmix.security.authentication.PolicyAwareGrantedAuthority;
import io.jmix.security.authentication.ResourcePolicyIndex;
import io.jmix.security.model.ResourcePolicy;
import io.jmix.security.model.ResourcePolicyType;
import io.jmix.securityflowui.constraint.FlowuiPolicyStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component("sec_AuthenticationFlowuiPolicyStore")
public class AuthenticationFlowuiPolicyStore implements FlowuiPolicyStore {

    @Autowired
    protected CurrentAuthentication currentAuthentication;

    @Override
    public Stream<ResourcePolicy> getViewResourcePolicies(String viewId) {
        return extractFromAuthenticationByScope(authority ->
                authority.getResourcePoliciesByIndex(ViewResourcePolicyByIdIndex.class,
                        index -> index.getPolicies(viewId)));
    }

    @Override
    public Stream<ResourcePolicy> getMenuResourcePolicies(String menuId) {
        return extractFromAuthenticationByScope(authority ->
                authority.getResourcePoliciesByIndex(MenuResourcePolicyByIdIndex.class,
                        index -> index.getPolicies(menuId)));
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

    public static class ViewResourcePolicyByIdIndex implements ResourcePolicyIndex {
        private static final long serialVersionUID = -2668694174861058682L;

        protected Map<String, List<ResourcePolicy>> policyById;

        @Override
        public void indexAll(Collection<ResourcePolicy> resourcePolicies) {
            policyById = resourcePolicies.stream()
                    .filter(p -> Objects.equals(p.getType(), ResourcePolicyType.SCREEN))
                    .collect(Collectors.groupingBy(ResourcePolicy::getResource));
        }

        public Stream<ResourcePolicy> getPolicies(String name) {
            return policyById.getOrDefault(name, Collections.emptyList()).stream();
        }
    }

    public static class MenuResourcePolicyByIdIndex implements ResourcePolicyIndex {
        private static final long serialVersionUID = 5018128694788321319L;

        protected Map<String, List<ResourcePolicy>> policyById;

        @Override
        public void indexAll(Collection<ResourcePolicy> resourcePolicies) {
            policyById = resourcePolicies.stream()
                    .filter(p -> Objects.equals(p.getType(), ResourcePolicyType.MENU))
                    .collect(Collectors.groupingBy(ResourcePolicy::getResource));
        }

        public Stream<ResourcePolicy> getPolicies(String name) {
            return policyById.getOrDefault(name, Collections.emptyList()).stream();
        }
    }
}
