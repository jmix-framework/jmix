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

package io.jmix.core.security

import com.sample.addon1.TestAddon1Configuration
import io.jmix.core.JmixCoreConfiguration
import io.jmix.core.security.impl.CoreSecurityImpl
import org.springframework.context.ApplicationContext
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import spock.lang.Ignore
import spock.lang.Specification

import javax.inject.Inject

@ContextConfiguration(classes = [JmixCoreConfiguration, TestAddon1Configuration])
@TestPropertySource(properties = ["jmix.securityImplementation = core"])
class CoreSecurityImplTest extends Specification {

    @Inject
    Security security

    @Inject
    ApplicationContext context

    @Inject
    AuthenticationManager authenticationManager

    def "Security impl is default"() {
        expect:

        security instanceof CoreSecurityImpl
    }

    def "authentication as admin"() {
        when:

        def authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken('admin', 'admin123'))

        then:

        authentication != null

        when:

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken('admin', 'no'))

        then:

        def e = thrown(AuthenticationException)
        e instanceof BadCredentialsException
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
