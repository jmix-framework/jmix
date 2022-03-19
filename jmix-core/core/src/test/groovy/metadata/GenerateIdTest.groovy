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

package metadata

import io.jmix.core.CoreConfiguration
import io.jmix.core.Metadata
import io.jmix.core.entity.EntityValues
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import test_support.addon1.TestAddon1Configuration
import test_support.app.TestAppConfiguration
import test_support.app.entity.generated_id.GBar
import test_support.app.entity.nullable_and_generated_id.NGBar

@ContextConfiguration(classes = [CoreConfiguration, TestAddon1Configuration, TestAppConfiguration])
class GenerateIdTest extends Specification {

    @Autowired
    Metadata metadata

    def "generate uuid"() {
        when:
        def bar1 = metadata.create(GBar)

        then:
        EntityValues.getGeneratedId(bar1) != null
        EntityValues.getGeneratedId(bar1) == bar1.uuid

        when:
        def bar2 = metadata.create(NGBar)

        then:
        EntityValues.getGeneratedId(bar2) != null
        EntityValues.getGeneratedId(bar2) == bar2.uuid
    }
}
