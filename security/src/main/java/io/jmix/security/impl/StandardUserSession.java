/*
 * Copyright 2019 Haulmont.
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

package io.jmix.security.impl;

import io.jmix.core.security.PermissionType;
import io.jmix.core.security.UserSession;
import io.jmix.security.entity.Constraint;
import io.jmix.security.entity.Permission;
import io.jmix.security.entity.Role;
import io.jmix.security.entity.RoleType;
import org.springframework.security.core.Authentication;

import javax.annotation.Nullable;
import java.util.*;

public class StandardUserSession extends UserSession {

    private static final long serialVersionUID = 3464453157472449019L;

    protected Map<String, Integer>[] permissions;
    protected Map<String, List<ConstraintData>> constraints;
    protected EnumSet<RoleType> roleTypes;

    public StandardUserSession(Authentication authentication) {
        super(authentication);

        //noinspection unchecked
        permissions = new Map[PermissionType.values().length];
        for (int i = 0; i < permissions.length; i++) {
            permissions[i] = new HashMap<>();
        }
        constraints = new HashMap<>();
        roleTypes = EnumSet.noneOf(RoleType.class);
    }

    @Override
    public io.jmix.security.entity.User getUser() {
        return (io.jmix.security.entity.User) super.getUser();
    }

    public void addRole(Role role) {
        roles.add(role.getName());
        roleTypes.add(role.getType());
    }

    public void addPermission(PermissionType type, String target, @Nullable String extTarget, Integer value) {
        Integer currentValue = permissions[type.ordinal()].get(target);
        if (currentValue == null || currentValue < value) {
            permissions[type.ordinal()].put(target, value);
            if (extTarget != null)
                permissions[type.ordinal()].put(extTarget, value);
        }
    }

    public void addConstraint(Constraint constraint) {
        String entityName = constraint.getEntityName();
        List<ConstraintData> list = constraints.computeIfAbsent(entityName, k -> new ArrayList<>());
        list.add(new ConstraintData(constraint));
    }

    public Integer getPermissionValue(PermissionType type, String target) {
        return permissions[type.ordinal()].get(target);
    }

    /**
     * Check user permission for the specified value.
     *
     * @param type   permission type
     * @param target permission target:<ul>
     *               <li>screen
     *               <li>entity operation (view, create, update, delete)
     *               <li>entity attribute name
     *               <li>specific permission name
     *               </ul>
     * @param value  method returns true if the corresponding {@link Permission}
     *               record contains value equal or greater than specified
     * @return true if permitted, false otherwise
     */
    public boolean isPermitted(PermissionType type, String target, int value) {
        // If we have super-role no need to check anything
        if (roleTypes.contains(RoleType.SUPER))
            return true;
        // Get permission value assigned by the set of permissions
        Integer v = permissions[type.ordinal()].get(target);
        // Get permission value assigned by non-standard roles
        for (RoleType roleType : roleTypes) {
            Integer v1 = roleType.permissionValue(type, target);
            if (v1 != null && (v == null || v < v1)) {
                v = v1;
            }
        }
        // Return true if no value set for this target, or if the value is more than requested
        return v == null || v >= value;
    }

    public List<ConstraintData> getConstraints(String entityName) {
        return Collections.unmodifiableList(constraints.getOrDefault(entityName, Collections.emptyList()));
    }

    public boolean hasConstraints() {
        return !constraints.isEmpty();
    }
}
