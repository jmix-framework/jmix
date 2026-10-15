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

package annotated_role_inheritance

import io.jmix.security.model.EntityPolicyAction
import io.jmix.security.role.ResourceRoleRepository
import io.jmix.security.role.RowLevelRoleRepository
import org.springframework.beans.factory.annotation.Autowired
import test_support.SecuritySpecification

class AnnotatedRoleInheritanceTest extends SecuritySpecification {

    @Autowired
    ResourceRoleRepository resourceRoleRepository

    @Autowired
    RowLevelRoleRepository rowLevelRoleRepository

    def "extended resource role keeps its child role and has the parent policies once"() {
        when:

        resourceRoleRepository.invalidateCache()
        resourceRoleRepository.findRoleByCode('TestChildRole')
        resourceRoleRepository.invalidateCache()
        def role = resourceRoleRepository.findRoleByCode('TestChildRole')

        then:

        role.childRoles == ['TestParentRole'] as Set
        role.allResourcePolicies.size() == 2
        role.allResourcePolicies.count { it.action == EntityPolicyAction.CREATE.id } == 1
    }

    def "extended row-level role keeps its child role and has the parent policies once"() {
        when:

        rowLevelRoleRepository.invalidateCache()
        rowLevelRoleRepository.findRoleByCode('TestChildRowLevelRole')
        rowLevelRoleRepository.invalidateCache()
        def role = rowLevelRoleRepository.findRoleByCode('TestChildRowLevelRole')

        then:

        role.childRoles == ['TestParentRowLevelRole'] as Set
        role.allRowLevelPolicies.size() == 2
        role.allRowLevelPolicies.count { it.joinClause == 'join {E}.customer c' } == 1
    }
}
