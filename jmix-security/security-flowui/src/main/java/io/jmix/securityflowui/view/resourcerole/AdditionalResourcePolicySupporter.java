/*
 * Copyright 2024 Haulmont.
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

package io.jmix.securityflowui.view.resourcerole;

import io.jmix.flowui.view.StandardDetailView;
import io.jmix.securityflowui.model.ResourcePolicyModel;
import io.jmix.securityflowui.view.resourcepolicy.MultipleResourcePolicyModelCreateView;

/**
 * Interface for supporting additional resource policy types (e.g. that come from add-ons) in the resource role details
 * view. Implementations of this interface provide necessary information for creating and editing resource policies of
 * specific types.
 */
public interface AdditionalResourcePolicySupporter {

    /**
     * Returns the class of the view used for creating new resource policies of the specific type.
     *
     * @return the class of the view for creating new resource policies
     */
    Class<? extends MultipleResourcePolicyModelCreateView> getCreatePolicyViewClass();

    /**
     * Returns the class of the detail view used for editing resource policies of the specific type.
     *
     * @return the class of the detail view for editing resource policies
     */
    Class<? extends StandardDetailView<ResourcePolicyModel>> getEditPolicyViewClass();

    /**
     * Checks if the "effect" column should be visible in the policies grid of the resource role editor.
     *
     * @return true if the effect column should be visible, false otherwise
     */
    boolean isEffectColumnVisible();

    /**
     * Returns the localized name of the policy supported by this provider.
     *
     * @return the name of the policy
     */
    String getPolicyName();

    /**
     * Checks if the provider supports the given resource policy type.
     *
     * @param policyType the type of the resource policy
     * @return true if the provider supports the given type, false otherwise
     */
    boolean supports(String policyType);
}
