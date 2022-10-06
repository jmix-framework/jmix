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

package navigation

import io.jmix.flowui.view.navigation.UrlParamSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class UrlParamSerializerTest extends FlowuiTestSpecification {

    @Autowired
    UrlParamSerializer urlParamSerializer

    def "Positive serialization cases"() {
        def stringId = 'someStringId'
        def intId = 42
        def longId = 12041961l
        def uuidId = UUID.fromString('79c08841-8063-4f85-86d0-25b3410a857c')

        when: "suitable object are passed as id"

        def serializedStringId = urlParamSerializer.serialize(stringId)
        def serializedIntId = urlParamSerializer.serialize(intId)
        def serializedLongId = urlParamSerializer.serialize(longId)
        def serializedUuidId = urlParamSerializer.serialize(uuidId)

        then: "ok"

        serializedStringId == stringId
        serializedIntId == String.valueOf(intId)
        serializedLongId == String.valueOf(longId)
//        serializedUuidId == '3sr24430339y2rdm15pd0gn1bw'
        serializedUuidId == '79c08841-8063-4f85-86d0-25b3410a857c'
    }

    def "Negative serialization cases"() {
        def nullId = null
        def bigDecimal = new BigDecimal(new Random().nextInt())
        def array = [1, 2, 3]

        when: "null passed as id"
        urlParamSerializer.serialize(nullId)

        then: "fail"
        thrown IllegalArgumentException

        when: "unsupported object is passed as id"
        urlParamSerializer.serialize(bigDecimal)

        then: "fail"
        thrown IllegalArgumentException

        when: "array is passed as id"
        urlParamSerializer.serialize(array)

        then: "fail"
        thrown IllegalArgumentException
    }

    def "Negative deserialization cases"() {
        def badIntId = "4b2"
        def badLongId = "120a41q96s1"
        def bigDecimalId = "1234567890"

        when: "null type and null id are passed for deserialization"
        urlParamSerializer.deserialize(null, null)

        then: "fail"
        thrown IllegalArgumentException

        when: "null id with type are passed for deserialization"
        urlParamSerializer.deserialize(Integer.class, null)

        then: "fail"
        thrown IllegalArgumentException

        when: "id without type is passed for deserialization"
        urlParamSerializer.deserialize(null, "randomString")

        then: "fail"
        thrown IllegalArgumentException

        when: "not int value is passed with int type"
        urlParamSerializer.deserialize(Integer.class, badIntId)

        then: "fail"
        thrown RuntimeException

        when: "not long value is passed with long type"
        urlParamSerializer.deserialize(Long.class, badLongId)

        then: "fail"
        thrown RuntimeException

        when: "unsupported type is passed for deserialization"
        urlParamSerializer.deserialize(BigDecimal.class, bigDecimalId)

        then: "fail"
        thrown IllegalArgumentException
    }

    def "Serialized and deserialized comparison"() {
        def stringId = 'someStringId'
        def intId = 42
        def longId = 12041961l
        def uuidId = UUID.fromString('79c08841-8063-4f85-86d0-25b3410a857c')

        when: "sequence of serialization and deserialization"

        def serializedStringId = urlParamSerializer.serialize(stringId)
        def deserializedStringId = urlParamSerializer.deserialize(String.class, serializedStringId)

        def serializedIntId = urlParamSerializer.serialize(intId)
        def deserializedIntId = urlParamSerializer.deserialize(Integer.class, serializedIntId)

        def serializedLongId = urlParamSerializer.serialize(longId)
        def deserializedLongId = urlParamSerializer.deserialize(Long.class, serializedLongId)

        def serializedUuidId = urlParamSerializer.serialize(uuidId)
        def deserializedUuidId = urlParamSerializer.deserialize(UUID.class, serializedUuidId)

        then: "deserialized ids should be equal to initial values"

        stringId == deserializedStringId
        intId == deserializedIntId
        longId == deserializedLongId
        uuidId == deserializedUuidId
    }
}
