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

package io.jmix.securityui.constraint;

import io.jmix.core.security.CurrentAuthentication;
import io.jmix.security.authentication.ResourcePolicyIndex;
import io.jmix.security.authentication.SecuredGrantedAuthority;
import io.jmix.security.model.ResourcePolicy;
import io.jmix.security.model.ResourcePolicyType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component("sec_AuthenticationUiPolicyStore")
public class AuthenticationUiPolicyStore implements UiPolicyStore {

    @Autowired
    protected CurrentAuthentication currentAuthentication;

    @Override
    public Stream<ResourcePolicy> getScreenResourcePolicies(String windowId) {
        return extractFromAuthentication(authority ->
                authority.getResourcePoliciesByIndex(ScreenResourcePolicyByIdIndex.class, index -> index.getPolicies(windowId)));
    }

    @Override
    public Stream<ResourcePolicy> getMenuResourcePolicies(String menuId) {
        return extractFromAuthentication(authority ->
                authority.getResourcePoliciesByIndex(MenuResourcePolicyByIdIndex.class, index -> index.getPolicies(menuId)));
    }

    protected Stream<ResourcePolicy> extractFromAuthentication(
            Function<SecuredGrantedAuthority, Stream<ResourcePolicy>> extractor) {
        Stream<ResourcePolicy> stream = Stream.empty();

        Authentication authentication = currentAuthentication.getAuthentication();
        if (authentication != null) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                if (authority instanceof SecuredGrantedAuthority) {
                    Stream<ResourcePolicy> extractedStream = extractor.apply((SecuredGrantedAuthority) authority);
                    if (extractedStream != null) {
                        stream = Stream.concat(stream, extractedStream);
                    }
                }
            }
        }

        return stream;
    }

    public static class ScreenResourcePolicyByIdIndex implements ResourcePolicyIndex {
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
