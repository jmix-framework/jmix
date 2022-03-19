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

package jmix_modules

import io.jmix.core.CoreConfiguration
import io.jmix.core.JmixModulesAwareBeanSelector
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import test_support.TestBean
import test_support.addon1.TestAddon1Configuration
import test_support.app.TestAppBean
import test_support.app.TestAppConfiguration
import test_support.base.TestBaseConfiguration

@ContextConfiguration(classes = [TestAppConfiguration, TestAddon1Configuration, TestBaseConfiguration, CoreConfiguration])
class JmixModulesAwareSelectorTest extends Specification {

    @Autowired
    JmixModulesAwareBeanSelector selector

    @Autowired
    ApplicationContext applicationContext

    def "select bean from the last module"() {
        Map<String, TestBean> beansMap = applicationContext.getBeansOfType(TestBean)

        when:
        TestBean bean = selector.selectFrom(beansMap.values())

        then:
        bean instanceof TestAppBean
    }
}
