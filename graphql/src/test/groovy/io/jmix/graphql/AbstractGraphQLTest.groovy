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

package io.jmix.graphql

import com.graphql.spring.boot.test.GraphQLTestAutoConfiguration
import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionTemplate
import spock.lang.Specification
import test_support.GraphQLTestConfiguration
import test_support.TestContextInitializer

@SuppressWarnings('SpringJavaInjectionPointsAutowiringInspection')
@ContextConfiguration(
        classes = [CoreConfiguration, DataConfiguration, EclipselinkConfiguration,
                GraphQLTestConfiguration, GraphqlConfiguration],
        initializers = [TestContextInitializer]
)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@OverrideAutoConfiguration(enabled = false)
@ImportAutoConfiguration(
        classes = [
                ServletWebServerFactoryAutoConfiguration.class,
                GraphQLTestAutoConfiguration.class,
                JacksonAutoConfiguration.class
        ]
)@TestPropertySource("classpath:/test_support/test-app.properties")
class AbstractGraphQLTest extends Specification {

    protected TransactionTemplate transaction

    @Autowired
    protected void setTransactionManager(PlatformTransactionManager transactionManager) {
        transaction = new TransactionTemplate(transactionManager)
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW)
    }
}
