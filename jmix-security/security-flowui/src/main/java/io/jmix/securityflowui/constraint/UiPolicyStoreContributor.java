/*
 * Copyright 2026 Haulmont.
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

package io.jmix.securityflowui.constraint;

import io.jmix.security.model.ResourcePolicy;

import java.util.stream.Stream;

/**
 * Contributes additional UI policies to {@link UiPolicyStore} implementations.
 */
public interface UiPolicyStoreContributor {

    /**
     * Returns extra view policies for the specified view.
     *
     * @param viewId view identifier
     * @return view policies
     */
    default Stream<ResourcePolicy> getViewResourcePolicies(String viewId) {
        return Stream.empty();
    }

    /**
     * Returns extra menu policies for the specified menu item.
     *
     * @param menuId menu identifier
     * @return menu policies
     */
    default Stream<ResourcePolicy> getMenuResourcePolicies(String menuId) {
        return Stream.empty();
    }
}
