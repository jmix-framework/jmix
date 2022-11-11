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

import io.jmix.security.model.ResourcePolicyEffect;
import io.jmix.securityflowui.constraint.FlowuiPolicyStore;
import io.jmix.securityflowui.constraint.FlowuiSecureOperations;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("sec_FlowuiSecureOperations")
public class FlowuiSecureOperationsImpl implements FlowuiSecureOperations {

    @Override
    public boolean isViewPermitted(String viewId, FlowuiPolicyStore policyStore) {
        boolean result = policyStore.getViewResourcePolicies(viewId)
                .anyMatch(policy -> Objects.equals(policy.getEffect(), ResourcePolicyEffect.ALLOW));

        if (!result) {
            result = policyStore.getViewResourcePolicies("*")
                    .anyMatch(policy -> Objects.equals(policy.getEffect(), ResourcePolicyEffect.ALLOW));
        }

        return result;
    }

    @Override
    public boolean isMenuItemPermitted(String menuId, FlowuiPolicyStore policyStore) {
        boolean result = policyStore.getMenuResourcePolicies(menuId)
                .anyMatch(policy -> Objects.equals(policy.getEffect(), ResourcePolicyEffect.ALLOW));

        if (!result) {
            result = policyStore.getMenuResourcePolicies("*")
                    .anyMatch(policy -> Objects.equals(policy.getEffect(), ResourcePolicyEffect.ALLOW));
        }

        return result;
    }
}
