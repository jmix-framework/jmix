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

package session_query_params

import io.jmix.core.CoreConfiguration
import io.jmix.core.session.SessionData
import io.jmix.data.DataConfiguration
import io.jmix.data.impl.SessionQueryParamValueProvider
import io.jmix.eclipselink.EclipselinkConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.TestDataConfiguration

@ContextConfiguration(classes = [CoreConfiguration, DataConfiguration, EclipselinkConfiguration, TestDataConfiguration])
class SessionQueryParamValueProviderTest extends Specification {

    @Autowired
    SessionQueryParamValueProvider provider

    @Autowired
    SessionData sessionData

    def "test supports"() {
        expect:
        provider.supports('session_param1')
        !provider.supports('param1')
        !provider.supports('sessionParam1')
    }

    def "test get value"() {
        sessionData.setAttribute('param1', 'val1')

        when:
        def value1 = provider.getValue('session_param1')

        then:
        value1 == 'val1'

        when:
        def value2 = provider.getValue('session_param2')

        then:
        value2 == null

        when:
        def value3 = provider.getValue('some_param1') // unsupported prefix

        then:
        noExceptionThrown()
        value3 == null

        cleanup:
        sessionData.setAttribute('param1', null)
    }
}
