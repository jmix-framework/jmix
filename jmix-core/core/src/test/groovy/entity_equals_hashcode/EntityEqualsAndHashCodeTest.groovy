/*
 * Copyright (c) 2008-2020 Haulmont.
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

package entity_equals_hashcode

import io.jmix.core.CoreConfiguration
import io.jmix.core.entity.EntityValues
import io.jmix.core.impl.StandardSerialization
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import test_support.addon1.TestAddon1Configuration
import test_support.app.TestAppConfiguration
import test_support.app.entity.generated_id.GFoo
import test_support.app.entity.no_id.NoIdFoo
import test_support.app.entity.nullable_and_generated_id.NGFoo
import test_support.app.entity.nullable_id.NFoo
import test_support.base.TestBaseConfiguration

@ContextConfiguration(classes = [CoreConfiguration, TestBaseConfiguration, TestAddon1Configuration, TestAppConfiguration])
class EntityEqualsAndHashCodeTest extends Specification {

    @Autowired
    StandardSerialization standardSerialization

    // nullable id

    def "nullable id - reserialized object is equal and has the same hashCode"() {
        NFoo foo1 = new NFoo(name: 'foo1')
        NFoo foo11 = reserialize(foo1)

        expect:
        foo1 == foo11
        foo1.hashCode() == foo11.hashCode()
    }

    def "nullable id - different object is not equal and has the same hashCode"() {
        NFoo foo1 = new NFoo(name: 'foo1')
        NFoo foo2 = new NFoo(name: 'foo2')

        expect:
        foo1 != foo2
        foo1.hashCode() == foo2.hashCode() // !!!
    }

    def "nullable id - reserialized object with different id is equal and has the same hashCode"() {
        // case when an entity is persisted and the same instance gets an id
        NFoo foo1 = new NFoo(name: 'foo1')
        NFoo foo11 = reserialize(foo1)
        foo11.id = 10

        expect:
        foo1 == foo11
        foo1.hashCode() == foo11.hashCode()
    }

    def "nullable id - loaded object with same id is equal and has same hashCode"() {
        NFoo foo1 = new NFoo(name: 'foo1')
        foo1.id = 10

        NFoo foo11 = new NFoo(name: 'foo1')
        foo11.id = 10
        foo11.__copyEntityEntry()

        expect:
        foo1 == foo11
        foo1.hashCode() == foo11.hashCode()
    }

    // nullable id + generated id

    def "nullable+generated id - reserialized object is equal and has the same hashCode"() {
        NGFoo foo1 = new NGFoo(name: 'foo1')
        NGFoo foo11 = reserialize(foo1)

        expect:
        foo1 == foo11
        foo1.hashCode() == foo11.hashCode()
    }

    def "nullable+generated id - different object is not equal and has different hashCode"() {
        NGFoo foo1 = new NGFoo(name: 'foo1')
        NGFoo foo2 = new NGFoo(name: 'foo2')

        expect:
        foo1 != foo2
        foo1.hashCode() != foo2.hashCode()
    }

    def "nullable+generated id - reserialized object with different id is equal and has the same hashCode"() {
        // case when an entity is persisted and the same instance gets an id
        NGFoo foo1 = new NGFoo(name: 'foo1')
        NGFoo foo11 = reserialize(foo1)
        foo11.id = 10

        expect:
        foo1 == foo11
        foo1.hashCode() == foo11.hashCode()
    }

    def "nullable+generated id - loaded object with same id is equal and has same hashCode"() {
        NGFoo foo1 = new NGFoo(name: 'foo1')
        foo1.id = 10

        NGFoo foo11 = new NGFoo(name: 'foo1')
        foo11.id = foo1.id
        foo11.uuid = foo1.uuid
        foo11.__copyEntityEntry()

        expect:
        foo1 == foo11
        foo1.hashCode() == foo11.hashCode()
    }

    // generated id

    def "generated id - reserialized object is equal and has the same hashCode"() {
        GFoo foo1 = new GFoo(name: 'foo1')
        GFoo foo11 = reserialize(foo1)

        expect:
        foo1 == foo11
        foo1.hashCode() == foo11.hashCode()
    }

    def "generated id - different object is not equal and has different hashCode"() {
        GFoo foo1 = new GFoo(name: 'foo1')
        GFoo foo2 = new GFoo(name: 'foo2')

        expect:
        foo1 != foo2
        foo1.hashCode() != foo2.hashCode()
    }

    def "generated id - loaded object with same id is equal and has the same hashCode"() {
        GFoo foo1 = new GFoo(name: 'foo1')
        foo1.uuid = UUID.randomUUID()

        GFoo foo11 = new GFoo(name: 'foo1')
        foo11.uuid = foo1.uuid
        foo11.__copyEntityEntry()

        expect:
        foo1 == foo11
        foo1.hashCode() == foo11.hashCode()
    }

    // no id

    def "no id - reserialized object is equal and has the same hashCode"() {
        NoIdFoo foo1 = new NoIdFoo(name: 'foo1')
        NoIdFoo foo11 = reserialize(foo1)

        expect:
        foo1 == foo11
        foo1.hashCode() == foo11.hashCode()

        EntityValues.getId(foo1) != null
        EntityValues.getId(foo1) == EntityValues.getId(foo11)
    }

    def "no id - different object is not equal and has different hashCode"() {
        NoIdFoo foo1 = new NoIdFoo(name: 'foo1')
        NoIdFoo foo2 = new NoIdFoo(name: 'foo2')

        expect:
        foo1 != foo2
        foo1.hashCode() != foo2.hashCode()

        EntityValues.getId(foo1) != EntityValues.getId(foo2)
    }

    @SuppressWarnings("unchecked")
    def <T> T reserialize(Serializable object) {
        if (object == null) {
            return null
        }

        return (T) standardSerialization.deserialize(standardSerialization.serialize(object))
    }
}
