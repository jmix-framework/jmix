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


import io.jmix.core.CoreConfiguration
import io.jmix.core.JmixModules
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import test_support.addon1.TestAddon1Configuration
import test_support.app.TestAppConfiguration
import test_support.base.TestBaseConfiguration

@ContextConfiguration(classes = [TestAppConfiguration, TestAddon1Configuration, TestBaseConfiguration, CoreConfiguration])
class JmixModulesTest extends Specification {

    @Autowired
    private JmixModules modules

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

    def "properties of components"() {
        expect:

        def base = modules.get('test_support.base')
        def addon1 = modules.get('test_support.addon1')
        def app = modules.get('test_support.app')

        base.getProperty('jmix.core.fetch-plans-config') == 'test_support/base/fetch-plans.xml'

        addon1.getProperty('jmix.core.fetch-plans-config') == 'test_support/addon1/fetch-plans.xml'
        addon1.getProperty('prop1') == 'addon1_prop1'
        addon1.getProperty('prop2') == 'addon1_prop2'

        // because @PropertySource has no name in TestAppConfiguration
        app.getProperty('jmix.core.fetch-plans-config') == null
        app.getProperty('prop2') == null
        // app values
        environment.getProperty('jmix.core.fetch-plans-config') == 'test_support/app/fetch-plans.xml'
        environment.getProperty('prop2') == 'app_prop2'
    }

    def "resulting properties"() {
        expect:

        environment.getProperty('jmix.core.fetch-plans-config') == 'test_support/app/fetch-plans.xml'
        environment.getProperty('prop1') == 'addon1_prop1'
        environment.getProperty('prop2') == 'app_prop2'

        modules.getPropertyValues('jmix.core.fetch-plans-config') == [
                'test_support/base/fetch-plans.xml', 'test_support/addon1/fetch-plans.xml', 'test_support/app/fetch-plans.xml'
        ]
    }
}
