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
import org.atmosphere.cpr.ApplicationConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import test_support.spec.FlowuiTestSpecification

@SpringBootTest(classes = PushCorsTestConfiguration)
class PushCorsServletRegistrationWiringTest extends FlowuiTestSpecification {

    @Autowired
    ServletRegistrationBean<SpringServlet> vaadinServletRegistration

    def "test FlowUI drops push CORS headers on the Vaadin servlet registration"() {
        expect:
        vaadinServletRegistration.initParameters[ApplicationConfig.DROP_ACCESS_CONTROL_ALLOW_ORIGIN_HEADER] == 'true'
    }

    @Configuration
    static class PushCorsTestConfiguration {

        @Bean
        ServletRegistrationBean<SpringServlet> vaadinServletRegistration(ApplicationContext applicationContext) {
            return new ServletRegistrationBean<>(new SpringServlet(applicationContext, true), '/*')
        }
    }
}
