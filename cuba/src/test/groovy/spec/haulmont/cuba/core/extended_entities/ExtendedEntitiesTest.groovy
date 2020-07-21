/*
 * Copyright (c) 2008-2018 Haulmont.
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

package spec.haulmont.cuba.core.extended_entities

import io.jmix.core.ExtendedEntities
import io.jmix.core.impl.keyvalue.KeyValueMetaClass
import io.jmix.core.impl.keyvalue.KeyValueMetaPropertyBuilder
import org.springframework.beans.factory.annotation.Autowired
import spec.haulmont.cuba.core.CoreTestSpecification

class ExtendedEntitiesTest extends CoreTestSpecification {
    @Autowired
    private ExtendedEntities extendedEntities

    @Autowired
    private KeyValueMetaPropertyBuilder keyValueMetaPropertyBuilder;

    def "KeyValueEntity cannot be extended so always return the same meta-class"() {

        def metaClass = new KeyValueMetaClass()
        metaClass.addProperty(keyValueMetaPropertyBuilder.build(metaClass, 'foo', String.class))
        metaClass.addProperty(keyValueMetaPropertyBuilder.build(metaClass, 'bar', String.class))

        expect:

        extendedEntities.getEffectiveMetaClass(metaClass) == metaClass
        extendedEntities.getOriginalOrThisMetaClass(metaClass) == metaClass
        extendedEntities.getOriginalMetaClass(metaClass) == null
    }
}
