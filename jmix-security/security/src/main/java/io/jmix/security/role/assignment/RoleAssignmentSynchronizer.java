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

package io.jmix.security.role.assignment;

import io.jmix.core.SaveContext;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Synchronizes the stored role assignments of a user with the roles asserted by an identity provider. Implemented
 * by the {@code jmix-security-data} module, which stores role assignments in the database.
 * <p>
 * A stored assignment is matched with an asserted one by role code and role type. What happens to it depends on its
 * {@link RoleAssignmentSource source}:
 * <ul>
 *     <li>the source of the synchronization: kept if the role is asserted, removed otherwise;</li>
 *     <li>no source (written before role assignments had one): taken over by the synchronization if the role is
 *     asserted, removed otherwise;</li>
 *     <li>{@link RoleAssignmentSource#MANUAL} or another source: never changed.</li>
 * </ul>
 * An asserted role that no stored assignment holds is added with the source of the synchronization.
 */
@NullMarked
public interface RoleAssignmentSynchronizer {

    /**
     * Adds to the given save context the changes that synchronize the stored role assignments of the user with
     * the asserted ones, without saving it. The caller saves the context, together with the user if needed, and
     * chooses whether removed assignments are soft-deleted.
     *
     * @param username            user whose role assignments are synchronized
     * @param assertedAssignments role assignments asserted by the identity provider
     * @param source              source of the synchronization, such as {@link RoleAssignmentSource#OIDC}; neither
     *                            {@code null} nor {@link RoleAssignmentSource#MANUAL}
     * @param saveContext         save context to add the changes to
     * @return authorities of the user's {@link RoleAssignmentSource#MANUAL manual} role assignments
     */
    Collection<GrantedAuthority> synchronize(String username, Collection<RoleAssignment> assertedAssignments,
                                             String source, SaveContext saveContext);

    /**
     * Returns the authorities of the user's {@link RoleAssignmentSource#MANUAL manual} role assignments without
     * changing anything.
     *
     * @param username user whose role assignments are read
     * @return authorities of the manual role assignments, or an empty collection
     */
    Collection<GrantedAuthority> getManualAuthorities(String username);
}
