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

package authentication

import io.jmix.core.DataManager
import io.jmix.core.JmixCoreConfiguration
import io.jmix.core.security.SystemAuthenticationToken
import io.jmix.core.security.UserAuthentication
import io.jmix.core.security.UserRepository
import io.jmix.data.JmixDataConfiguration
import io.jmix.data.PersistenceTools
import io.jmix.security.JmixSecurityConfiguration
import io.jmix.security.entity.User
import io.jmix.security.impl.StandardUserRepository
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification
import test_support.JmixSecurityTestConfiguration

import org.springframework.beans.factory.annotation.Autowired

@ContextConfiguration(classes = [JmixCoreConfiguration, JmixDataConfiguration, JmixSecurityConfiguration, JmixSecurityTestConfiguration])
@TestPropertySource(properties = ["jmix.securityImplementation = standard"])
class AuthenticationTest extends Specification {

    @Autowired
    DataManager dataManager

    @Autowired
    PersistenceTools persistenceTools

    @Autowired
    AuthenticationManager authenticationManager

    @Autowired
    UserRepository userRepository

    def "standard implementations are in use"() {
        expect:

        userRepository instanceof StandardUserRepository
        userRepository.loadUserByUsername('admin') instanceof User
    }

    def "authenticate with UsernamePasswordAuthenticationToken"() {

        def user = new User(username: 'user1', password: '{noop}123')
        dataManager.save(user)

        when:

        def token = new UsernamePasswordAuthenticationToken('user1', '123')
        Authentication authentication = authenticationManager.authenticate(token)

        then:

        authentication instanceof UserAuthentication
        authentication.user instanceof User
        authentication.user.username == 'user1'

        cleanup:

        persistenceTools.deleteRecord(user)
    }

    def "authenticate with SystemAuthenticationToken"() {

        def user = new User(username: 'user1', password: '{noop}123')
        dataManager.save(user)

        when:

        def token = new SystemAuthenticationToken('user1')
        Authentication authentication = authenticationManager.authenticate(token)

        then:

        authentication instanceof SystemAuthenticationToken
        authentication.isAuthenticated()
        authentication.principal == user

        cleanup:

        persistenceTools.deleteRecord(user)
    }
}
