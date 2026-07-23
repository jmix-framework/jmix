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

package role_model_converter

import io.jmix.security.model.ResourcePolicy
import io.jmix.security.model.ResourcePolicyType
import io.jmix.security.model.ResourceRole
import io.jmix.security.model.RoleModelConverter
import io.jmix.security.model.RowLevelPolicy
import io.jmix.security.model.RowLevelRole
import org.springframework.beans.factory.annotation.Autowired
import test_support.SecuritySpecification

class RoleModelConverterTest extends SecuritySpecification {

    @Autowired
    RoleModelConverter roleModelConverter

    def "resource role model includes policies by default"() {
        when:
        def model = roleModelConverter.createResourceRoleModel(resourceRoleWithOnePolicy())

        then:
        model.code == 'role1'
        model.resourcePolicies.size() == 1
    }

    def "resource role model excludes policies when withPolicies is false"() {
        when:
        def model = roleModelConverter.createResourceRoleModel(resourceRoleWithOnePolicy(), false)

        then:
        model.code == 'role1'
        model.resourcePolicies == null
    }

    def "row level role model excludes policies when withPolicies is false"() {
        when:
        def model = roleModelConverter.createRowLevelRoleModel(rowLevelRoleWithOnePolicy(), false)

        then:
        model.code == 'role2'
        model.rowLevelPolicies == null
    }

    private static ResourceRole resourceRoleWithOnePolicy() {
        def role = new ResourceRole()
        role.name = 'Role1'
        role.code = 'role1'
        role.setResourcePolicies([
                ResourcePolicy.builder(ResourcePolicyType.SCREEN, 'screen1').build()
        ])
        return role
    }

    private static RowLevelRole rowLevelRoleWithOnePolicy() {
        def role = new RowLevelRole()
        role.name = 'Role2'
        role.code = 'role2'
        role.setRowLevelPolicies([
                new RowLevelPolicy('test_Order', 'where1', 'join1', [:])
        ])
        return role
    }
}
