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

package io.jmix.core.impl.scanning

import com.sample.app.AppContextTestExecutionListener
import io.jmix.core.JmixCoreConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners
import spock.lang.Specification

@ContextConfiguration(classes = [JmixCoreConfiguration])
@TestExecutionListeners(value = AppContextTestExecutionListener,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class EntitiesScannerTest extends Specification {

    @Autowired
    ApplicationContext context

    def "entities of the core module"() {

        when:

        def scanner = context.getBean(EntitiesScanner)

        then:

        scanner != null
        scanner.applicationContext != null
        scanner.metadataReaderFactory != null
        scanner.basePackages == ['io.jmix.core']

        when:

        def entityDefList = scanner.getEntityClassNames()

        then:

        entityDefList.find { it == 'io.jmix.core.entity.BaseUuidEntity' } != null
    }
}
