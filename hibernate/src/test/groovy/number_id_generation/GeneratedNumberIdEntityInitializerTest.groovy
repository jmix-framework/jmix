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

package number_id_generation

import io.jmix.core.Metadata
import io.jmix.core.entity.EntityValues
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.number_id_generation.TestIntegerIdEntity
import test_support.entity.number_id_generation.TestLongIdEntity
import test_support.entity.number_id_generation.TestLongIdWithUuidEntity

class GeneratedNumberIdEntityInitializerTest extends DataSpec {

    @Autowired
    Metadata metadata

    def "generate long id"() {
        when:
        def entity = metadata.create(TestLongIdEntity)

        then:
        entity.getId() != null
        EntityValues.getGeneratedId(entity) == entity.getId()
    }

    def "generate integer id"() {
        when:
        def entity = metadata.create(TestIntegerIdEntity)

        then:
        entity.getId() != null
        EntityValues.getGeneratedId(entity) == entity.getId()
    }

    def "generate long id and uuid"() {
        when:
        def entity = metadata.create(TestLongIdWithUuidEntity)

        then:
        entity.getId() != null
        EntityValues.getGeneratedId(entity) == entity.getId()
        entity.getUuid() != null
    }
}
