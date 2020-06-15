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

package annotated_role_provider


import io.jmix.security.model.Role
import io.jmix.security.role.provider.AnnotatedRoleProvider
import org.springframework.beans.factory.annotation.Autowired
import test_support.SecuritySpecification

class AnnotatedRoleProviderTest extends SecuritySpecification {

    @Autowired
    AnnotatedRoleProvider annotatedRoleProvider

    def "scan classpath for roles with annotation"() {

        when:
        Role[] roles = annotatedRoleProvider.getAllRoles()

        then: //find multiple roles including the ones from test_support/annotated_role_provider package
        roles.size() > 0

        roles.find {it.name = 'TestAnnotatedRoleProviderRole'} != null
    }
}
