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

package extended_entity_role

import io.jmix.core.AccessManager
import io.jmix.core.Metadata
import io.jmix.core.accesscontext.CrudEntityContext
import io.jmix.core.accesscontext.EntityAttributeContext
import io.jmix.core.security.InMemoryUserRepository
import io.jmix.core.security.SecurityContextHelper
import io.jmix.security.authentication.RoleGrantedAuthority
import io.jmix.security.role.ResourceRoleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import test_support.SecurityDataSpecification
import test_support.entity.Bar
import test_support.entity.ExtBar
import test_support.role.TestExtBarRole

class ExtendedEntityRoleTest extends SecurityDataSpecification {

    @Autowired
    AuthenticationManager authenticationManager

    @Autowired
    InMemoryUserRepository userRepository

    @Autowired
    ResourceRoleRepository roleRepository

    @Autowired
    Metadata metadata

    @Autowired
    AccessManager accessManager

    UserDetails user1

    String PASSWORD = '123'

    def setup() {
        user1 = User.builder()
                .username("user1")
                .password("{noop}$PASSWORD")
                .authorities(RoleGrantedAuthority.ofResourceRole(roleRepository.getRoleByCode(TestExtBarRole.CODE)))
                .build()

        userRepository.addUser(user1)
    }

    def cleanup() {
        userRepository.removeUser(user1)
    }

    def authenticate(String username) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, PASSWORD))
        SecurityContextHelper.setAuthentication(authentication)
    }

    def "role should contain policies both from original and extended entities (original class)"() {

        when:

        authenticate('user1')
        def origBarMetaClass = metadata.getClass(Bar)
        def context = new CrudEntityContext(origBarMetaClass)
        accessManager.applyRegisteredConstraints(context)

        then:

        context.isCreatePermitted()
        context.isReadPermitted()
        !context.isUpdatePermitted()
        !context.isDeletePermitted()
    }

    def "role should contain policies both from original and extended entities (extended class)"() {
        authenticate('user1')
        def extBarMetaClass = metadata.getClass(ExtBar)

        when:

        def crudContext = new CrudEntityContext(extBarMetaClass)
        accessManager.applyRegisteredConstraints(crudContext)

        then:

        crudContext.isCreatePermitted()
        crudContext.isReadPermitted()
        !crudContext.isUpdatePermitted()
        !crudContext.isDeletePermitted()

        when:

        def nameAttributeContext = new EntityAttributeContext(extBarMetaClass, "name")
        accessManager.applyRegisteredConstraints(nameAttributeContext)

        then:

        nameAttributeContext.canModify()

        when:

        def descriptionAttributeContext = new EntityAttributeContext(extBarMetaClass, "description")
        accessManager.applyRegisteredConstraints(descriptionAttributeContext)

        then:

        descriptionAttributeContext.canView()
        !descriptionAttributeContext.canModify()
    }
}
