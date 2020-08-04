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
import io.jmix.security.authentication.SecuredAuthentication;
import io.jmix.security.model.ResourcePolicy;
import io.jmix.security.model.ResourcePolicyType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component(AuthenticationUiPolicyStore.NAME)
public class AuthenticationUiPolicyStore implements UiPolicyStore {
    public static final String NAME = "sec_AuthenticationUiPolicyStore";

    @Autowired
    protected CurrentAuthentication currentAuthentication;

    @Override
    public Collection<ResourcePolicy> getScreenResourcePolicies(String windowId) {
        return extractFromAuthentication(auth ->
                auth.getResourcePoliciesByIndex(ScreenResourcePolicyByIdIndex.class, index -> index.getPolicies(windowId)));
    }

    @Override
    public Collection<ResourcePolicy> getMenuResourcePolicies(String menuId) {
        return extractFromAuthentication(auth ->
                auth.getResourcePoliciesByIndex(MenuResourcePolicyByIdIndex.class, index -> index.getPolicies(menuId)));
    }

    protected Collection<ResourcePolicy> extractFromAuthentication(
            Function<SecuredAuthentication, Collection<ResourcePolicy>> extractor) {
        if (currentAuthentication.getAuthentication() instanceof SecuredAuthentication) {
            return extractor.apply((SecuredAuthentication) currentAuthentication.getAuthentication());
        }
        return Collections.emptyList();
    }

    public static class ScreenResourcePolicyByIdIndex implements ResourcePolicyIndex {
        protected Map<String, List<ResourcePolicy>> policyById;

        @Override
        public void indexAll(Collection<ResourcePolicy> resourcePolicies) {
            policyById = resourcePolicies.stream()
                    .filter(p -> Objects.equals(p.getResource(), ResourcePolicyType.SCREEN))
                    .collect(Collectors.groupingBy(ResourcePolicy::getResource));
        }

        public Collection<ResourcePolicy> getPolicies(String name) {
            return policyById.get(name);
        }
    }

    public static class MenuResourcePolicyByIdIndex implements ResourcePolicyIndex {
        protected Map<String, List<ResourcePolicy>> policyById;

        @Override
        public void indexAll(Collection<ResourcePolicy> resourcePolicies) {
            policyById = resourcePolicies.stream()
                    .filter(p -> Objects.equals(p.getResource(), ResourcePolicyType.MENU))
                    .collect(Collectors.groupingBy(ResourcePolicy::getResource));
        }

        public Collection<ResourcePolicy> getPolicies(String name) {
            return policyById.get(name);
        }
    }
}
