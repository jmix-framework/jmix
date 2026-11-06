/*
 * Copyright 2026 Haulmont.
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

package push_cors

import com.vaadin.flow.spring.SpringServlet
import io.jmix.flowui.sys.PushCorsServletRegistrationPostProcessor
import jakarta.servlet.http.HttpServlet
import org.atmosphere.cpr.ApplicationConfig
import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.context.ApplicationContext
import spock.lang.Specification

class PushCorsServletRegistrationPostProcessorTest extends Specification {

    static final String DROP_HEADER = ApplicationConfig.DROP_ACCESS_CONTROL_ALLOW_ORIGIN_HEADER

    PushCorsServletRegistrationPostProcessor postProcessor = new PushCorsServletRegistrationPostProcessor()

    def "test Vaadin servlet registration gets the parameter that drops CORS headers"() {
        given:
        def registration = new ServletRegistrationBean<>(servlet, '/*')

        when:
        def result = postProcessor.postProcessBeforeInitialization(registration, 'registration')

        then:
        result.is(registration)
        registration.initParameters[DROP_HEADER] == 'true'

        where:
        servlet << [
                new SpringServlet(Mock(ApplicationContext), true),
                new CustomSpringServlet(Mock(ApplicationContext), true)
        ]
    }

    def "test parameter set by the application is kept"() {
        given:
        def registration = new ServletRegistrationBean<>(new SpringServlet(Mock(ApplicationContext), true), '/*')
        registration.addInitParameter(DROP_HEADER, 'false')

        when:
        postProcessor.postProcessBeforeInitialization(registration, 'registration')

        then:
        registration.initParameters[DROP_HEADER] == 'false'
    }

    def "test registration without a Vaadin servlet is not changed"() {
        when:
        postProcessor.postProcessBeforeInitialization(registration, 'registration')

        then:
        registration.initParameters.isEmpty()

        where:
        registration << [
                new ServletRegistrationBean<>(new HttpServlet() {}, '/other/*'),
                new ServletRegistrationBean<>()
        ]
    }

    def "test other beans are returned as is"() {
        given:
        def bean = new Object()

        expect:
        postProcessor.postProcessBeforeInitialization(bean, 'bean').is(bean)
    }

    static class CustomSpringServlet extends SpringServlet {

        CustomSpringServlet(ApplicationContext context, boolean rootMapping) {
            super(context, rootMapping)
        }
    }
}
