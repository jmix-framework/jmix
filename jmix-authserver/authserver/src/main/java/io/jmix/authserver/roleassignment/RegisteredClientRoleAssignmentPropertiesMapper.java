/*
 * Copyright 2022 Haulmont.
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

package io.jmix.authserver.roleassignment;

import io.jmix.authserver.AuthServerProperties;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Maps {@link AuthServerProperties} to {@link RegisteredClientRoleAssignment}
 */
public class RegisteredClientRoleAssignmentPropertiesMapper {

    private final AuthServerProperties properties;

    public RegisteredClientRoleAssignmentPropertiesMapper(AuthServerProperties properties) {
        this.properties = properties;
    }

    public Collection<RegisteredClientRoleAssignment> asRegisteredClientRoleAssignments() {
        Collection<RegisteredClientRoleAssignment> roleAssignments = new ArrayList<>();
        properties.getClient().forEach(
                (registrationId, roleAssignment) -> {
                    roleAssignments.add(createRoleAssignment(registrationId, roleAssignment));
                }
        );
        return roleAssignments;
    }

    private RegisteredClientRoleAssignment createRoleAssignment(String registrationId,
                                                                AuthServerProperties.JmixClient roleAssignment) {
        return RegisteredClientRoleAssignment.builder()
                .registrationId(registrationId)
                .clientId(roleAssignment.getClientId())
                .resourceRoles(roleAssignment.getResourceRoles())
                .rowLevelRoles(roleAssignment.getRowLevelRoles())
                .build();
    }
}
