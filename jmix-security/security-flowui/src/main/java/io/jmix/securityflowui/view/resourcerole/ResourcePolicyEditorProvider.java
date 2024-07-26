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

import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.securityflowui.model.ResourcePolicyModel;

/**
 * Interface for providing information required for displaying detail views of additional resource policies types.
 * Implementations of this interface are used in the {@link ResourceRoleModelDetailView}.
 */
public interface ResourcePolicyEditorProvider {

    /**
     * Returns the class of the detail view used for editing resource policies of specific type.
     *
     * @return the class of the detail view
     */
    Class<? extends StandardDetailView<ResourcePolicyModel>> getPolicyDetailViewClass();

    /**
     * Creates an action for creating a new resource policy of specific type.
     *
     * @param context the context for creating the policy action
     * @return the action for creating a new resource policy
     */
    Action getCreatePolicyAction(CreatePolicyActionContext context);

    /**
     * Checks if the provider supports the given resource policy type.
     *
     * @param resourcePolicyType the type of the resource policy
     * @return true if the provider supports the given type, false otherwise
     */
    boolean supports(String resourcePolicyType);

    /**
     * Checks if the effect column should be visible in the resource policy editor.
     */
    boolean isEffectColumnVisible();

    /**
     * Context for building an action that creates a new resource policy.
     */
    record CreatePolicyActionContext(DataGrid<ResourcePolicyModel> resourcePolicyDataGrid) {
    }
}
