/*
 * Copyright 2025 Haulmont.
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

package rest_enabled


import io.jmix.rest.security.impl.RestAsResourceServerBeforeInvocationEventListener
import io.jmix.rest.security.impl.RestOidcResourceServerBeforeInvocationEventListener
import org.springframework.mock.web.MockHttpServletRequest
import spock.lang.Specification

class ListenerTest extends Specification {

    def "test RestAsResourceServerBeforeInvocationEventListener without servlet context"() {
        def listener = new RestAsResourceServerBeforeInvocationEventListener()

        def request = new MockHttpServletRequest()

        when:
        request.setRequestURI('/rest/entities/User')
        request.setServletPath('/rest/entities/User')

        then:
        listener.shouldCheckRequest(request)

        when:
        request.setRequestURI('/something/entities/User')
        request.setServletPath('/something/entities/User')

        then:
        !listener.shouldCheckRequest(request)
    }

    def "test RestAsResourceServerBeforeInvocationEventListener with servlet context"() {
        def listener = new RestAsResourceServerBeforeInvocationEventListener()

        def request = new MockHttpServletRequest()
        request.setContextPath('/app')

        when:
        request.setRequestURI('/app/rest/entities/User')
        request.setServletPath('/rest/entities/User')

        then:
        listener.shouldCheckRequest(request)

        when:
        request.setRequestURI('/app/something/entities/User')
        request.setServletPath('/something/entities/User')

        then:
        !listener.shouldCheckRequest(request)
    }

    def "test RestOidcResourceServerBeforeInvocationEventListener without servlet context"() {
        def listener = new RestOidcResourceServerBeforeInvocationEventListener()

        def request = new MockHttpServletRequest()

        when:
        request.setRequestURI('/rest/entities/User')
        request.setServletPath('/rest/entities/User')

        then:
        listener.shouldCheckRequest(request)

        when:
        request.setRequestURI('/something/entities/User')
        request.setServletPath('/something/entities/User')

        then:
        !listener.shouldCheckRequest(request)
    }

    def "test RestOidcResourceServerBeforeInvocationEventListener with servlet context"() {
        def listener = new RestOidcResourceServerBeforeInvocationEventListener()

        def request = new MockHttpServletRequest()
        request.setContextPath('/app')

        when:
        request.setRequestURI('/app/rest/entities/User')
        request.setServletPath('/rest/entities/User')

        then:
        listener.shouldCheckRequest(request)

        when:
        request.setRequestURI('/app/something/entities/User')
        request.setServletPath('/something/entities/User')

        then:
        !listener.shouldCheckRequest(request)
    }
}
