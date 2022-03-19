/*
 * Copyright 2021 Haulmont.
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

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.jmix.core.CoreConfiguration
import io.jmix.core.EntitySerialization
import io.jmix.core.EntitySerializationOption
import io.jmix.core.Metadata
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.base.TestBaseConfiguration
import test_support.base.entity.TestSecretFieldEntity

@ContextConfiguration(classes = [CoreConfiguration, TestBaseConfiguration])
class EntitySerializationTest extends Specification {

    @Autowired
    EntitySerialization entitySerialization

    @Autowired
    Metadata metadata

    def "should not serialize @Secret fields by default"() {

        TestSecretFieldEntity entity = metadata.create(TestSecretFieldEntity.class)
        entity.regularField = 'regular'
        entity.secretField = 'secret'

        when:

        def json = entitySerialization.toJson(entity)

        then:
        Map jsonFields = new Gson().fromJson(json, new TypeToken<Map<String, Object>>() {}.getType())
        jsonFields['regularField'] == 'regular'

        !jsonFields.containsKey('secretField')
    }

    def "should serialize @Secret fields if SERIALIZE_SECRET_FIELDS is set"() {

        TestSecretFieldEntity entity = metadata.create(TestSecretFieldEntity.class)
        entity.regularField = 'regular'
        entity.secretField = 'secret'

        when:

        def json = entitySerialization.toJson(entity, null, EntitySerializationOption.SERIALIZE_SECRET_FIELDS)

        then:
        Map jsonFields = new Gson().fromJson(json, new TypeToken<Map<String, Object>>() {}.getType())
        jsonFields['regularField'] == 'regular'
        jsonFields['secretField'] == 'secret'
    }
}
