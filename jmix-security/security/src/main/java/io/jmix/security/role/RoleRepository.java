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

package io.jmix.security.role;

import io.jmix.security.model.BaseRole;

import org.springframework.lang.Nullable;

import java.util.Collection;

/**
 * Interface for roles repository. Implementations of the interface are responsible for finding roles of different
 * types: resource, row-level, etc.
 */
public interface RoleRepository<T extends BaseRole> {

    T getRoleByCode(String code);

    @Nullable
    T findRoleByCode(String code);

    boolean deleteRole(String code);

    Collection<T> getAllRoles();

    /**
     * Invalidates role cache. The method must be invoked after each role modification, for example, when database role
     * is changed with the UI or annotated role is hot deployed.
     */
    void invalidateCache();
}
