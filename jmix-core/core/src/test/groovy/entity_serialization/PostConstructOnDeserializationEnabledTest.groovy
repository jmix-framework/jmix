/*
 * Copyright 2026 Haulmont.
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

package entity_serialization

import io.jmix.core.CoreConfiguration
import io.jmix.core.EntitySerialization
import io.jmix.core.Metadata
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification
import test_support.base.TestBaseConfiguration
import test_support.base.entity.TestPostConstructEntity

@ContextConfiguration(classes = [CoreConfiguration, TestBaseConfiguration])
@TestPropertySource(properties = ['jmix.core.invoke-post-construct-on-entity-deserialization = true'])
class PostConstructOnDeserializationEnabledTest extends Specification {

    @Autowired
    EntitySerialization entitySerialization

    @Autowired
    Metadata metadata

    def "@PostConstruct is invoked when an entity is deserialized if the property is set"() {

        def json = entitySerialization.toJson(metadata.create(TestPostConstructEntity))
        TestPostConstructEntity.initCount.set(0)

        when:
        entitySerialization.entityFromJson(json, metadata.getClass(TestPostConstructEntity))

        then:
        TestPostConstructEntity.initCount.get() == 1
    }
}
