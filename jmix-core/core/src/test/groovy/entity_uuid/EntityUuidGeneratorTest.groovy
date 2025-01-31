/*
 * Copyright 2025 Haulmont.
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

package entity_uuid

import io.jmix.core.CoreConfiguration
import io.jmix.core.EntityUuidGenerator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.base.TestBaseConfiguration

@ContextConfiguration(classes = [CoreConfiguration, TestBaseConfiguration])
class EntityUuidGeneratorTest extends Specification {

    @Autowired
    EntityUuidGenerator entityUuidGenerator

    def "test UUID is v7"() {
        when: "generate UUIDs"
        List<String> list = []
        100.times {
            def uuid = entityUuidGenerator.generate()
            list << uuid.toString()
        }

        then: "UUIDs are v7 and increasing"
        (0..<100).each { i ->
            list[i][14] == '7'
            if (i > 0) {
                list[i] > list[i - 1]
            }
        }
    }
}
