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

package app_properties

import test_support.addon1.TestAddon1Configuration
import test_support.AppContextTestExecutionListener
import test_support.app.TestAppConfiguration
import io.jmix.core.JmixCoreConfiguration
import io.jmix.core.compatibility.AppProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners
import spock.lang.Specification

@ContextConfiguration(classes = [TestAppConfiguration, TestAddon1Configuration, JmixCoreConfiguration])
@TestExecutionListeners(value = AppContextTestExecutionListener,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class AppPropertiesTest extends Specification {

    @Autowired
    AppProperties appProperties

    def "properties can be changed at runtime"() {
        when:

        appProperties.setProperty('prop3', 'changed_prop3')
        appProperties.setProperty('prop4', 'changed_prop4')

        then:

        appProperties.getProperty('prop3') == 'changed_prop3'
        appProperties.getProperty('prop4') == 'changed_prop4'

        when:

        appProperties.setProperty('prop3', null)
        appProperties.setProperty('prop4', null)

        then:

        appProperties.getProperty('prop3') == 'app_prop3'
        appProperties.getProperty('prop4') == null
    }
}
