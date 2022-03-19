/*
 * Copyright 2019 Haulmont.
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

package security

import io.jmix.core.CoreConfiguration
import io.jmix.core.security.InMemoryUserRepository
import io.jmix.core.security.SystemAuthenticationToken
import io.jmix.core.security.SystemAuthenticator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.base.TestBaseConfiguration

@ContextConfiguration(classes = [CoreConfiguration, TestBaseConfiguration])
class SystemAuthenticatorTest extends Specification {

    @Autowired
    SystemAuthenticator authenticator

    @Autowired
    InMemoryUserRepository userRepository

    UserDetails admin

    def setup() {
        admin = User.builder()
                .username('admin')
                .password('{noop}admin123')
                .authorities(Collections.emptyList())
                .build()
        userRepository.addUser(admin)
    }

    def cleanup() {
        userRepository.removeUser(admin)
    }


    def "authenticate as system"() {
        when:

        authenticator.begin()

        then:

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication()
        authentication instanceof SystemAuthenticationToken
        authentication.principal instanceof UserDetails
        ((UserDetails) authentication.principal).username == 'system'

        when:

        authenticator.end()

        then:

        SecurityContextHolder.getContext().getAuthentication() == null
    }

    def "authenticate as admin"() {
        when:

        authenticator.begin('admin')

        then:

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication()
        authentication instanceof SystemAuthenticationToken
        authentication.principal instanceof UserDetails
        ((UserDetails) authentication.principal).username == 'admin'

        when:

        authenticator.end()

        then:

        SecurityContextHolder.getContext().getAuthentication() == null
    }

    def "nested authentication"() {

        when: "outer auth"

        authenticator.begin()

        then:

        Authentication outerAuth = SecurityContextHolder.getContext().getAuthentication()
        outerAuth instanceof SystemAuthenticationToken
        outerAuth.principal instanceof UserDetails
        ((UserDetails) outerAuth.principal).username == 'system'

        when: "inner auth"

        authenticator.begin('admin')

        then:

        Authentication innerAuth = SecurityContextHolder.getContext().getAuthentication()
        innerAuth instanceof SystemAuthenticationToken
        innerAuth.principal instanceof UserDetails
        ((UserDetails) innerAuth.principal).username == 'admin'

        when: "end inner"

        authenticator.end()

        then:

        Authentication outerAuth1 = SecurityContextHolder.getContext().getAuthentication()
        outerAuth1 instanceof SystemAuthenticationToken
        outerAuth1.principal instanceof UserDetails
        ((UserDetails) outerAuth1.principal).username == 'system'

        when: "end outer"

        authenticator.end()

        then:

        SecurityContextHolder.getContext().getAuthentication() == null
    }
}
