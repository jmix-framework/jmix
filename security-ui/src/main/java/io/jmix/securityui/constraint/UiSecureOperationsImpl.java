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

import io.jmix.security.model.ResourcePolicyEffect;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("sec_UiSecureOperations")
public class UiSecureOperationsImpl implements UiSecureOperations {

    @Override
    public boolean isScreenPermitted(String windowId, UiPolicyStore policyStore) {
        boolean result = policyStore.getScreenResourcePolicies(windowId).stream()
                .anyMatch(policy -> Objects.equals(policy.getEffect(), ResourcePolicyEffect.ALLOW));

        if (!result) {
            result = policyStore.getScreenResourcePolicies("*").stream()
                    .anyMatch(policy -> Objects.equals(policy.getEffect(), ResourcePolicyEffect.ALLOW));
        }

        return result;
    }

    @Override
    public boolean isMenuItemPermitted(String menuId, UiPolicyStore policyStore) {
        boolean result = policyStore.getMenuResourcePolicies(menuId).stream()
                .anyMatch(policy -> Objects.equals(policy.getEffect(), ResourcePolicyEffect.ALLOW));

        if (!result) {
            result = policyStore.getMenuResourcePolicies("*").stream()
                    .anyMatch(policy -> Objects.equals(policy.getEffect(), ResourcePolicyEffect.ALLOW));
        }

        return result;
    }
}
