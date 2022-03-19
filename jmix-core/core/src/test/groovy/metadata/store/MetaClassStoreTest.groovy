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

package metadata.store

import io.jmix.core.CoreConfiguration
import io.jmix.core.Metadata
import io.jmix.core.Stores
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import test_support.addon1.TestAddon1Configuration
import test_support.addon1.entity.TestAddon1Entity
import test_support.app.TestAppConfiguration
import test_support.app.entity.Address
import test_support.app.entity.Owner
import test_support.app.entity.Pet
import test_support.base.entity.BaseEntity

@ContextConfiguration(classes = [CoreConfiguration, TestAddon1Configuration, TestAppConfiguration])
class MetaClassStoreTest extends Specification {

    @Autowired
    Metadata metadata

    def "store of entity is NOOP"() {

        def metaClass = metadata.getClass(TestAddon1Entity)

        expect:

        metaClass.store != null
        metaClass.store.name == Stores.NOOP
    }

    def "store of mapped superclass is UNDEFINED"() {

        def metaClass = metadata.getClass(BaseEntity)

        expect:

        metaClass.store != null
        metaClass.store.name == Stores.UNDEFINED
    }

    def "store of embeddable and its properties is MAIN"() {

        def metaClass = metadata.getClass(Address)

        expect:

        metaClass.store != null
        metaClass.store.name == Stores.MAIN
        metaClass.getProperty('city').store.name == Stores.MAIN
    }

    def "store of embedded property is MAIN"() {

        def metaClass = metadata.getClass(Owner)
        def property = metaClass.getProperty('address')

        expect:

        property.store.name == Stores.MAIN
    }

    def "store of entity property is NOOP"() {

        def metaProperty = metadata.getClass(TestAddon1Entity).getProperty('name')

        expect:

        metaProperty.store != null
        metaProperty.store.name == Stores.NOOP
    }

    def "store of mapped superclass property is UNDEFINED"() {

        def metaProperty = metadata.getClass(BaseEntity).getProperty('createTs')

        expect:

        metaProperty.store != null
        metaProperty.store.name == Stores.UNDEFINED
    }

    def "store of entity property inherited from mapped superclass is NOOP"() {

        def idProp = metadata.getClass(TestAddon1Entity).getProperty('id')
        def createTsProp = metadata.getClass(TestAddon1Entity).getProperty('createTs')

        expect:

        idProp.store.name == Stores.NOOP
        createTsProp.store.name == Stores.NOOP
    }

    def "store of entity annotated with @Entity is MAIN"() {

        def metaClass = metadata.getClass(Pet)
        def idProp = metaClass.getProperty('id')
        def nameProp = metaClass.getProperty('name')

        expect:

        idProp.store.name == Stores.MAIN
        nameProp.store.name == Stores.MAIN
    }

    def "store of non-mapped property of entity annotated with @Entity is UNDEFINED"() {

        def metaClass = metadata.getClass(Pet)
        def nickProp = metaClass.getProperty('nick')

        expect:

        nickProp.store.name == Stores.UNDEFINED
    }

    def "store of method-based property of entity annotated with @Entity is UNDEFINED"() {

        def metaClass = metadata.getClass(Pet)
        def descriptionProp = metaClass.getProperty('description')

        expect:

        descriptionProp.store.name == Stores.UNDEFINED
    }
}
