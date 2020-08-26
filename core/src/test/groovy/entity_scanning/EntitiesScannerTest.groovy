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

import io.jmix.core.CoreConfiguration
import io.jmix.core.impl.scanning.JmixModulesClasspathScanner
import io.jmix.core.impl.scanning.EntityDetector
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import test_support.base.TestBaseConfiguration

@ContextConfiguration(classes = [CoreConfiguration, TestBaseConfiguration])
class EntitiesScannerTest extends Specification {

    @Autowired
    ApplicationContext context

    def "entities of the core module"() {

        when:

        def scanner = context.getBean(JmixModulesClasspathScanner)

        then:

        scanner != null

        with (scanner) {
            applicationContext != null
            metadataReaderFactory != null
            basePackages == ['io.jmix.core', 'test_support.base']
        }

        when:

        def entityDefList = scanner.getClassNames(EntityDetector)

        then:

        entityDefList.any { entity ->
            entity == 'test_support.base.entity.BaseUuidEntity'
        }
    }
}
