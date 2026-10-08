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

package io.jmix.securitydata.impl.role;

import io.jmix.security.model.BaseRole;
import io.jmix.security.model.RoleSource;
import io.jmix.security.role.DuplicateRoleCodeException;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.security.role.RowLevelRoleRepository;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fails application startup if a design-time (annotated) role shares its {@code code}
 * with a runtime (database) role of the same type. Resource and row-level roles are
 * checked independently, since they are separate code namespaces.
 */
@Component("sec_RoleCodeUniquenessValidator")
public class RoleCodeUniquenessValidator implements SmartInitializingSingleton {

    protected final ResourceRoleRepository resourceRoleRepository;
    protected final RowLevelRoleRepository rowLevelRoleRepository;

    public RoleCodeUniquenessValidator(ResourceRoleRepository resourceRoleRepository,
                                       RowLevelRoleRepository rowLevelRoleRepository) {
        this.resourceRoleRepository = resourceRoleRepository;
        this.rowLevelRoleRepository = rowLevelRoleRepository;
    }

    @Override
    public void afterSingletonsInstantiated() {
        validate();
    }

    public void validate() {
        checkTypeUniqueness(resourceRoleRepository.getAllRoles(false), "resource");
        checkTypeUniqueness(rowLevelRoleRepository.getAllRoles(false), "row-level");
    }

    protected void checkTypeUniqueness(Collection<? extends BaseRole> roles, String roleType) {
        Map<String, List<BaseRole>> rolesByCode = new HashMap<>();
        for (BaseRole role : roles) {
            rolesByCode.computeIfAbsent(role.getCode(), key -> new ArrayList<>()).add(role);
        }
        for (Map.Entry<String, List<BaseRole>> entry : rolesByCode.entrySet()) {
            List<BaseRole> sameCodeRoles = entry.getValue();
            boolean hasAnnotated = sameCodeRoles.stream()
                    .anyMatch(role -> RoleSource.ANNOTATED_CLASS.equals(role.getSource()));
            if (sameCodeRoles.size() > 1 && hasAnnotated) {
                throw new DuplicateRoleCodeException(
                        String.format("Duplicate %s role code '%s': a design-time role conflicts " +
                                "with a runtime role with the same code", roleType, entry.getKey()),
                        entry.getKey());
            }
        }
    }
}
