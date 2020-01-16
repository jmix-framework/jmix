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

package spec.haulmont.cuba.core.setget

import com.haulmont.cuba.core.model.Many2ManyA
import com.haulmont.cuba.core.model.Many2ManyB
import com.haulmont.cuba.core.model.SetGetEntity
import com.haulmont.cuba.core.testsupport.TestContainer
import io.jmix.core.Metadata
import io.jmix.core.entity.StandardEntity
import spec.haulmont.cuba.core.CoreTestSpecification

import javax.inject.Inject

class SetGetTest extends CoreTestSpecification {

    public TestContainer cont = TestContainer.Common.INSTANCE

    @Inject
    private Metadata metadata

    private SetGetEntity<String> setGetEntity

    void setup() {
        setGetEntity = metadata.create(SetGetEntity.class)
    }

    def "ID and UUID"() {
        when:
        UUID uuid = setGetEntity.getValue("uuid")
        setGetEntity.setValue("id", uuid)
        UUID afterId = setGetEntity.getValue("id")

        then:
        uuid == afterId && uuid != null
    }

    def "Map"() {
        when:
        Map<String, Integer> map = new HashMap<>()
        map.put("key", 12)
        setGetEntity.setValue("map", map)
        Map<String, Integer> afterMap = setGetEntity.getValue("map")
        then:
        map.identity { afterMap }
    }

    def "Array"() {
        when:
        int[] intArray = new int[1]
        intArray[0] = 12
        setGetEntity.setValue("intArray", intArray)
        int[] afterIntArray = setGetEntity.getValue("intArray")

        then:
        intArray == afterIntArray
    }

    def "StandardEntity"() {
        when:
        StandardEntity[] standardEntityArray = new StandardEntity[1]
        standardEntityArray[0] = new SetGetEntity()
        setGetEntity.setValue("standardEntityArray", standardEntityArray)
        StandardEntity[] afterStandardEntityArray = setGetEntity.getValue("standardEntityArray")

        then:
        standardEntityArray == afterStandardEntityArray
    }

    def "Generic field"() {
        when:
        String genericField = "12"
        setGetEntity.setValue("genericField", genericField)
        String afterGenericValue = setGetEntity.getValue("genericField")

        then:
        genericField == afterGenericValue
    }

    def "Generic map"() {
        when:
        Map<String, Integer> genericMap = new HashMap<>()
        genericMap.put("key", 12)
        setGetEntity.setValue("genericMap", genericMap)
        Map<String, Integer> afterGenericMap = setGetEntity.getValue("genericMap")

        then:
        genericMap.identity { afterGenericMap }
    }

    def "Generic array"() {
        when:
        String[] genericArray = new String[1]
        genericArray[0] = "12"
        setGetEntity.setValue("genericArray", genericArray)
        String[] afterGenericArray = setGetEntity.getValue("genericArray")

        then:
        genericArray == afterGenericArray
    }

    def "Many to many"() {
        when:
        Set<Many2ManyB> collectionOfB = new HashSet<>()
        collectionOfB.add(cont.metadata().create(Many2ManyB.class))
        Many2ManyA many2ManyA = cont.metadata().create(Many2ManyA.class)
        many2ManyA.setValue("collectionOfB", collectionOfB)
        Set<Many2ManyB> afterCollectionOfB = many2ManyA.getValue("collectionOfB")

        then:
        collectionOfB == afterCollectionOfB
    }

    def "byte field"() {
        when:
        byte byteValue = 1
        setGetEntity.setValue("byteField", byteValue)
        byte afterByteValue = setGetEntity.getValue("byteField")

        then:
        byteValue == afterByteValue
    }

    def "char field"() {
        when:
        char charValue = 'a'
        setGetEntity.setValue("charField", charValue)
        char afterCharValue = setGetEntity.getValue("charField")

        then:
        charValue == afterCharValue
    }

    def "short field"() {
        when:
        short shortValue = 12
        setGetEntity.setValue("shortField", shortValue)
        short afterShortValue = setGetEntity.getValue("shortField")

        then:
        shortValue == afterShortValue
    }

    def "int field"() {
        when:
        int intValue = 12
        setGetEntity.setValue("intField", intValue)
        int afterIntValue = setGetEntity.getValue("intField")

        then:
        intValue == afterIntValue
    }

    def "long field"() {
        when:
        long longValue = 12L
        setGetEntity.setValue("longField", longValue)
        long afterLongValue = setGetEntity.getValue("longField")

        then:
        longValue == afterLongValue
    }

    def "float field"() {
        when:
        float floatValue = 12F
        setGetEntity.setValue("floatField", floatValue)
        float afterFloatValue = setGetEntity.getValue("floatField")

        then:
        floatValue == afterFloatValue
    }

    def "double field"() {
        when:
        double doubleValue = 12D
        setGetEntity.setValue("doubleField", doubleValue)
        double afterDoubleValue = setGetEntity.getValue("doubleField")

        then:
        doubleValue == afterDoubleValue
    }

    def "boolean field"() {
        when:
        boolean booleanValue = true
        setGetEntity.setValue("booleanField", booleanValue)
        boolean afterBooleanValue = setGetEntity.getValue("booleanField")

        then:
        booleanValue == afterBooleanValue
    }
}
