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

package io.jmix.security.role;

import io.jmix.security.model.ResourceRole;
import io.jmix.security.model.RowLevelRole;

import java.util.List;


/**
 * Interface for obtaining the current user's roles.
 */
public interface CurrentUserRoles {

    /**
     * Retrieves the list of resource roles assigned to the current user.
     *
     * @return a list of {@link ResourceRole}
     */
    List<ResourceRole> getResourceRoles();

    /**
     * Retrieves the list of row-level roles assigned to the current user.
     *
     * @return a list of {@link RowLevelRole}
     */
    List<RowLevelRole> getRowLevelRoles();
}