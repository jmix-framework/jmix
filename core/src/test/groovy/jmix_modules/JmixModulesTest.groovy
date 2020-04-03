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

package jmix_modules

import test_support.addon1.TestAddon1Configuration
import test_support.AppContextTestExecutionListener
import test_support.app.TestAppConfiguration
import test_support.app.TestBean
import io.jmix.core.JmixCoreConfiguration
import io.jmix.core.JmixModules
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners
import spock.lang.Specification

@ContextConfiguration(classes = [TestAppConfiguration, TestAddon1Configuration, JmixCoreConfiguration])
@TestExecutionListeners(value = AppContextTestExecutionListener,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class JmixModulesTest extends Specification {

    @Autowired
    private JmixModules modules

    @Autowired
    private TestBean testBean

    @Autowired
    private Environment environment

    def "test dependencies"() {
        expect:

        modules != null
        modules.all.size() == 4
        modules.all[0].id == 'io.jmix.core'

        def jmixCore = modules.get('io.jmix.core')
        def addon1 = modules.get('test_support.addon1')
        def app = modules.get('test_support.app')

        addon1.dependsOn(jmixCore)
        app.dependsOn(addon1)
        app.dependsOn(jmixCore)
    }

    def "configuration properties of components"() {
        expect:

        def addon1 = modules.get('test_support.addon1')
        def app = modules.get('test_support.app')

        addon1.getProperty('jmix.core.fetchPlansConfig') == 'test_support/addon1/fetch-plans.xml'
        app.getProperty('jmix.core.fetchPlansConfig') == 'test_support/app/fetch-plans.xml'

    }

    def "resulting configuration properties"() {
        expect:

        modules.getProperty('jmix.core.fetchPlansConfig') == 'test_support/addon1/fetch-plans.xml test_support/app/fetch-plans.xml'
        modules.getProperty('prop1') == 'addon1_prop1 app_prop1'
        modules.getProperty('prop2') == 'app_prop2'
        modules.getProperty('prop3') == 'app_prop3'
    }

    def "using configuration properties"() {
        expect:

        testBean.prop1 == 'addon1_prop1 app_prop1'
    }

    def "app property file overrides JmixProperty"() {
        expect:

        environment.getProperty('prop_to_override') == 'app_properties_file_prop3'
    }
}
