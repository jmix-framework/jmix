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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.User
import org.springframework.test.context.ContextConfiguration
import spock.lang.Ignore
import spock.lang.Specification
import test_support.addon1.TestAddon1Configuration
import test_support.base.TestBaseConfiguration

@ContextConfiguration(classes = [CoreConfiguration, TestBaseConfiguration, TestAddon1Configuration])
class CoreSecurityImplTest extends Specification {

    @Autowired
    ApplicationContext context

    @Autowired
    AuthenticationManager authenticationManager

    @Autowired
    InMemoryUserRepository userRepository

    def "authentication as admin"() {
        def admin = User.builder()
                .username('admin')
                .password('{noop}admin123')
                .authorities(Collections.emptyList())
                .build()
        userRepository.addUser(admin)

        when:

        def authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken('admin', 'admin123'))

        then:

        authentication != null
        authentication instanceof UsernamePasswordAuthenticationToken

        when:

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken('admin', 'no'))

        then:

        def e = thrown(AuthenticationException)
        e instanceof BadCredentialsException

        cleanup:

        userRepository.removeUser(admin)
    }

    // todo forbid using UsernamePasswordAuthenticationToken for system user
    @Ignore
    def "authentication as system with UsernamePasswordAuthenticationToken is impossible"() {
        when:

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken('system', ''))

        then:

        def e = thrown(AuthenticationException)
        e instanceof BadCredentialsException
    }
}
