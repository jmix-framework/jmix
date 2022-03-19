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

package io.jmix.security.impl.role.assignment;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.jmix.core.security.event.UserRemovedEvent;
import io.jmix.security.role.assignment.RoleAssignment;
import io.jmix.security.role.assignment.RoleAssignmentProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Collection;

/**
 * {@link RoleAssignmentProvider} that stores role assignments in memory. Use the {@link #addAssignment(RoleAssignment)}
 * method to put the {@code RoleAssignment} into in-memory storage.
 */
@Component("sec_InMemoryRoleAssignmentProvider")
public class InMemoryRoleAssignmentProvider implements RoleAssignmentProvider {

    //the key of the map is User.username
    protected Multimap<String, RoleAssignment> assignments = HashMultimap.create();

    @Override
    public Collection<RoleAssignment> getAllAssignments() {
        return assignments.values();
    }

    @Override
    public Collection<RoleAssignment> getAssignmentsByUsername(String username) {
        return assignments.get(username);
    }

    public void addAssignment(RoleAssignment roleAssignment) {
        assignments.put(roleAssignment.getUsername(), roleAssignment);
    }

    public void removeAssignments(String username) {
        assignments.removeAll(username);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, fallbackExecution = true)
    private void onUserInvalidation(UserRemovedEvent event) {
        removeAssignments(event.getUsername());
    }
}
