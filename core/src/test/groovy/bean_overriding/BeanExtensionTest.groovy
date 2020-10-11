/*
 * Copyright 2020 Haulmont.
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

package bean_overriding

import io.jmix.core.CoreConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.TestContextInititalizer
import test_support.addon1.TestAddon1Configuration
import test_support.addon1.TestAddonFooBean
import test_support.app.TestAppBarBean
import test_support.app.TestAppConfiguration
import test_support.base.TestBaseBarBean
import test_support.base.TestBaseConfiguration
import test_support.base.TestBaseFooBean

@ContextConfiguration(
        classes = [TestAppConfiguration, TestAddon1Configuration, TestBaseConfiguration, CoreConfiguration],
        initializers = [TestContextInititalizer]
)
class BeanExtensionTest extends Specification {

    @Autowired
    ApplicationContext applicationContext

    def "one extension"() {
        expect:
        applicationContext.containsBean('baseFooBean')
        applicationContext.containsBean('addonFooBean')

        applicationContext.getBean('baseFooBean') instanceof TestBaseFooBean
        applicationContext.getBean('addonFooBean') instanceof TestAddonFooBean

        applicationContext.getBean(TestBaseFooBean) instanceof TestAddonFooBean
    }

    def "two extensions"() {
        expect:
        applicationContext.containsBean('baseBarBean')
        !applicationContext.containsBean('addonBarBean')
        applicationContext.containsBean('appBarBean')

        applicationContext.getBean('baseBarBean') instanceof TestBaseBarBean
        applicationContext.getBean('appBarBean') instanceof TestAppBarBean

        applicationContext.getBean(TestBaseBarBean) instanceof TestAppBarBean
    }
}
