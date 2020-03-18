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

package entity_scanning

import test_support.addon1.TestAddon1Configuration
import test_support.AppContextTestExecutionListener
import io.jmix.core.JmixCoreConfiguration
import io.jmix.core.impl.scanning.EntitiesScanner
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners
import spock.lang.Specification

@ContextConfiguration(classes = [JmixCoreConfiguration, TestAddon1Configuration])
@TestExecutionListeners(value = AppContextTestExecutionListener,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class EntitiesScannerMultiConfigTest extends Specification {

    @Autowired
    ApplicationContext context

    def "entities of the core and addon1 modules"() {

        when:

        def scanner = context.getBean(EntitiesScanner)

        then:

        scanner != null
        scanner.applicationContext != null
        scanner.metadataReaderFactory != null
        scanner.basePackages == ['io.jmix.core', 'test_support.base', 'test_support.addon1']

        when:

        def entityDefList = scanner.getEntityClassNames()

        then:

        entityDefList.find { it == 'test_support.base.entity.BaseUuidEntity' } != null
        entityDefList.find { it == 'test_support.addon1.entity.TestAddon1Entity' } != null
    }
}
