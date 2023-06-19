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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.Arrays;
import java.util.Collection;

/**
 * In-memory implementation of {@link RegisteredClientRoleAssignmentRepository}.
 */
public class InMemoryRegisteredClientRoleAssignmentRepository implements RegisteredClientRoleAssignmentRepository {

    protected Multimap<String, RegisteredClientRoleAssignment> registrationToRoleAssignmentsMap = HashMultimap.create();

    protected Multimap<String, RegisteredClientRoleAssignment> clientToRoleAssignmentsMap = HashMultimap.create();

    public InMemoryRegisteredClientRoleAssignmentRepository(RegisteredClientRoleAssignment... roleAssignments) {
        this(Arrays.asList((roleAssignments)));
    }

    public InMemoryRegisteredClientRoleAssignmentRepository(Collection<RegisteredClientRoleAssignment> roleAssignments) {
        for (RegisteredClientRoleAssignment roleAssignment : roleAssignments) {
            registrationToRoleAssignmentsMap.put(roleAssignment.registrationId(), roleAssignment);
            clientToRoleAssignmentsMap.put(roleAssignment.clientId(), roleAssignment);
        }
    }

    @Override
    public void save(RegisteredClientRoleAssignment roleAssignment) {
        registrationToRoleAssignmentsMap.put(roleAssignment.registrationId(), roleAssignment);
        clientToRoleAssignmentsMap.put(roleAssignment.clientId(), roleAssignment);
    }

    @Override
    public Collection<RegisteredClientRoleAssignment> findByClientId(String clientId) {
        return clientToRoleAssignmentsMap.get(clientId);
    }

    @Override
    public Collection<RegisteredClientRoleAssignment> findByRegistrationId(String registrationId) {
        return registrationToRoleAssignmentsMap.get(registrationId);
    }
}
