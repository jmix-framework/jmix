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
import io.jmix.core.security.Security
import io.jmix.core.security.authentication.CoreAuthentication
import io.jmix.core.security.impl.CoreSecurityImpl
import io.jmix.core.security.impl.CoreUser
import io.jmix.core.security.impl.InMemoryUserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.TestPropertySource
import spock.lang.Ignore
import spock.lang.Specification
import test_support.AppContextTestExecutionListener
import test_support.addon1.TestAddon1Configuration

@ContextConfiguration(classes = [CoreConfiguration, TestAddon1Configuration])
@TestPropertySource(properties = ["jmix.securityImplementation = core"])
@TestExecutionListeners(value = AppContextTestExecutionListener,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class CoreSecurityImplTest extends Specification {

    @Autowired
    Security security

    @Autowired
    ApplicationContext context

    @Autowired
    AuthenticationManager authenticationManager

    @Autowired
    InMemoryUserRepository userRepository

    def "Security impl is default"() {
        expect:

        security instanceof CoreSecurityImpl
    }

    def "authentication as admin"() {
        when:

        def admin = new CoreUser('admin', '{noop}admin123', 'Admin')
        userRepository.createUser(admin)

        def authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken('admin', 'admin123'))

        then:

        authentication != null
        authentication instanceof CoreAuthentication

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
