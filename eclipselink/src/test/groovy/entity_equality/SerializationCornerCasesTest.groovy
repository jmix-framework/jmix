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

package entity_equality

import io.jmix.core.Metadata
import io.jmix.core.impl.StandardSerialization
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.deserialization_bug.CyclicChild
import test_support.entity.deserialization_bug.CyclicParent
import test_support.entity.entity_extension.Address

class SerializationCornerCasesTest extends DataSpec {

    @Autowired
    Metadata metadata
    @Autowired
    StandardSerialization serialization


    def "Entity graph with cycle and hashset deserialized correctly"() {
//see https://bugs.openjdk.java.net/browse/JDK-8201131
        when:
        CyclicParent parent = metadata.create(CyclicParent)

        CyclicChild child = metadata.create(CyclicChild)

        parent.children = new LinkedHashSet<CyclicChild>()

        parent.children.add(child)
        child.parent = parent

        child.address = metadata.create(Address)
        child.address.city = "Ankh-Morpork"
        child.address.street = "Cockbill Street"

        List entities = Arrays.asList(child, parent)//IT IS IMPORTANT! child first, then parent. Bug is not reproduced otherwise.

        byte[] serialized = serialization.serialize(entities)

        List deserialized = serialization.deserialize(serialized)

        then:
        //make sure no exception thrown (java.lang.IllegalStateException: Generated ID is null in test_support.entity.deserialization_bug.CyclicChild-null [new])
        deserialized != null
        ((CyclicChild) deserialized.get(0)).address.city == "Ankh-Morpork"
    }

}
