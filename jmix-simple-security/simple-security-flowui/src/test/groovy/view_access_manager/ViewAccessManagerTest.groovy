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

package view_access_manager


import io.jmix.core.security.CurrentAuthentication
import io.jmix.simplesecurity.SimpleSecurityProperties
import io.jmix.simplesecurity.role.GrantedAuthorityUtils
import io.jmix.simplesecurityflowui.access.ViewAccessManager
import io.jmix.simplesecurityflowui.access.impl.ViewAccessManagerImpl
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import spock.lang.Specification

class ViewAccessManagerTest extends Specification {

    ViewAccessManager viewAccessManager

    CurrentAuthentication currentAuthentication = Stub {
        getAuthentication() >> new UsernamePasswordAuthenticationToken('user',
                'pass',
                [new SimpleGrantedAuthority('ROLE_USER')])
    }

    SimpleSecurityProperties simpleSecurityProperties = Stub {
        getAdminRole() >> "ADMIN"
    }

    GrantedAuthorityUtils roleUtils = new GrantedAuthorityUtils(simpleSecurityProperties)

    def setup() {
        viewAccessManager = new ViewAccessManagerImpl(currentAuthentication,
                simpleSecurityProperties, roleUtils)
    }

    def "grant access to the specified viewId for the specified roleName"() {
        when:
        viewAccessManager.grantAccess("ROLE_USER", "view1")
        viewAccessManager.grantAccess("ROLE_ANOTHER_USER", "view2")

        then:
        viewAccessManager.roleHasAccess("ROLE_USER", "view1")
        !viewAccessManager.roleHasAccess("ROLE_USER", "view2")
        !viewAccessManager.roleHasAccess("ROLE_ANOTHER_USER", "view1")
        viewAccessManager.roleHasAccess("ROLE_ANOTHER_USER", "view2")

        viewAccessManager.currentUserHasAccess("view1")
        !viewAccessManager.currentUserHasAccess("view2")
    }

    def "grantAccess should grant access to anonymous views"() {
        when:
        viewAccessManager.grantAnonymousAccess("anonView1")

        then:
        viewAccessManager.currentUserHasAccess("anonView1")
        viewAccessManager.roleHasAccess("ROLE_USER", "anonView1")
        viewAccessManager.roleHasAccess("ROLE_ANOTHER_USER", "anonView1")
    }

    def "grant access to the specified viewId for the collection of role names"() {
        when:
        viewAccessManager.grantAccess("ROLE_USER", ["view1", "view2"])

        then:
        viewAccessManager.roleHasAccess("ROLE_USER", "view1")
        viewAccessManager.roleHasAccess("ROLE_USER", "view2")
        !viewAccessManager.roleHasAccess("ROLE_USER", "view3")
    }

    def "grantAccess should work for role names both with and without the ROLE_ prefix"() {
        when:
        viewAccessManager.grantAccess("USER", "view1")
        viewAccessManager.grantAccess("ROLE_ANOTHER_USER", "view2")

        then:
        viewAccessManager.roleHasAccess("USER", "view1")
        viewAccessManager.roleHasAccess("ROLE_USER", "view1")

        viewAccessManager.roleHasAccess("ANOTHER_USER", "view2")
        viewAccessManager.roleHasAccess("ROLE_ANOTHER_USER", "view2")
    }
}
