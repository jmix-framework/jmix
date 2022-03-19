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

import io.jmix.security.role.assignment.RoleAssignment;
import io.jmix.security.role.assignment.RoleAssignmentProvider;
import io.jmix.security.role.assignment.RoleAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component("sec_RoleAssignmentRepository")
public class RoleAssignmentRepositoryImpl implements RoleAssignmentRepository {

    protected Collection<RoleAssignmentProvider> assignmentProviders = new ArrayList<>();

    @Autowired(required = false)
    public void setAssignmentProviders(@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") List<RoleAssignmentProvider> assignmentProviders) {
        this.assignmentProviders = assignmentProviders;
    }

    @Override
    public Collection<RoleAssignment> getAllAssignments() {
        return assignmentProviders.stream()
                .flatMap(roleAssignmentProvider -> roleAssignmentProvider.getAllAssignments().stream())
                .collect(Collectors.toList());
    }

    @Override
    public Collection<RoleAssignment> getAssignmentsByUsername(String username) {
        return assignmentProviders.stream()
                .flatMap(roleAssignmentProvider -> roleAssignmentProvider.getAssignmentsByUsername(username).stream())
                .collect(Collectors.toList());
    }
}
