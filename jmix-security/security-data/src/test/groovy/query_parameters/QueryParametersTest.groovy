/*
 * Copyright 2021 Haulmont.
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

package query_parameters


import io.jmix.core.security.ClientDetails
import io.jmix.core.security.SystemAuthenticationToken
import io.jmix.core.security.SystemAuthenticator
import io.jmix.securitydata.impl.CurrentLocaleQueryParamValueProvider
import io.jmix.securitydata.impl.CurrentUserQueryParamValueProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.context.SecurityContextHolder
import test_support.SecurityDataSpecification

class QueryParametersTest extends SecurityDataSpecification {

    @Autowired
    SystemAuthenticator authenticator

    @Autowired
    AuthenticationManager authenticationManager

    @Autowired
    CurrentUserQueryParamValueProvider currentUserQueryParamValueProvider

    @Autowired
    CurrentLocaleQueryParamValueProvider currentLocaleQueryParamValueProvider

    def "test UserDetails attributes"() {
        when:
        def value = authenticator.withSystem {
            return currentUserQueryParamValueProvider.getValue('current_user_username')
        }

        then:
        value == 'system'

        when:
        value = authenticator.withSystem {
            return currentUserQueryParamValueProvider.getValue('current_user_enabled')
        }

        then:
        value == true
    }

    def "test locale attribute"() {
        when:
        def token = new SystemAuthenticationToken(null)
        def authentication = authenticationManager.authenticate(token) as SystemAuthenticationToken
        authentication.details = ClientDetails.builder().locale(Locale.of("ru")).build()

        SecurityContextHolder.getContext().setAuthentication(authentication)

        def value = currentLocaleQueryParamValueProvider.getValue('current_locale')

        SecurityContextHolder.getContext().setAuthentication(null)

        then:
        value == 'ru'
    }
}
