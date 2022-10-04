/*
 * Copyright 2022 Haulmont.
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

package data_components

import io.jmix.flowui.model.DataComponents
import org.springframework.beans.factory.annotation.Autowired
import test_support.entity.TestNullableIdEntity
import test_support.entity.TestNullableIdItemEntity
import test_support.entity.TestStringIdEntity
import test_support.spec.DataContextSpec

class DataContextNullIdTest extends DataContextSpec {

    @Autowired DataComponents factory

    def "can contain any number of instances with null id"() {
        TestNullableIdEntity entity1 = new TestNullableIdEntity(name: "111")
        TestNullableIdEntity entity2 = new TestNullableIdEntity(name: "222")

        def dataContext = factory.createDataContext()

        when:
        def merged1 = dataContext.merge(entity1)

        then:
        dataContext.contains(merged1)
        dataContext.find(merged1).is(merged1)

        and: 'if entity has no id, its identity is determined by generated id'
        dataContext.contains(entity1)
        dataContext.find(entity1).is(merged1)
        dataContext.find(TestNullableIdEntity, null) == null

        when:
        def merged2 = dataContext.merge(entity2)

        then:
        dataContext.contains(merged2)
        dataContext.find(merged2).is(merged2)

        and:
        dataContext.contains(entity2)

        when:
        dataContext.evict(merged1)

        then:
        !dataContext.contains(merged1)
        dataContext.contains(merged2)

        when:
        dataContext.evict(merged2)

        then:
        !dataContext.contains(merged1)
        !dataContext.contains(merged2)
    }

    def "merged instance changes id"() {
        def dataContext = factory.createDataContext()
        TestStringIdEntity entity = new TestStringIdEntity(name: "111")

        when:
        def merged = dataContext.merge(entity)
        merged.setCode('1')

        then:
        dataContext.find(TestStringIdEntity, '1').is(merged)

        when:
        merged.setCode('2')

        then:
        dataContext.find(TestStringIdEntity, '1') == null
        dataContext.find(TestStringIdEntity, '2').is(merged)
    }

    def "save"() {
        def dataContext = factory.createDataContext()

        TestNullableIdEntity entity = new TestNullableIdEntity(name: "111")

        when:
        def merged = dataContext.merge(entity)

        then:
        dataContext.hasChanges()
        dataContext.isModified(merged)
        dataContext.getModified().contains(merged)

        when:
        dataContext.save()

        then:
        !dataContext.hasChanges()
        merged.id != null
        dataContext.contains(merged)

        and:
        Map<Object, Object> entityMap = dataContext.content.get(TestNullableIdEntity)
        entityMap.size() == 1
    }

    def "save two instances"() {
        def dataContext = factory.createDataContext()

        TestNullableIdEntity entity1 = new TestNullableIdEntity(name: "111")
        TestNullableIdEntity entity2 = new TestNullableIdEntity(name: "222")

        when:
        def merged1 = dataContext.merge(entity1)
        def merged2 = dataContext.merge(entity2)

        then:
        dataContext.hasChanges()
        dataContext.isModified(merged1)
        dataContext.getModified().contains(merged1)
        dataContext.isModified(merged2)
        dataContext.getModified().contains(merged2)

        when:
        dataContext.save()

        then:
        !dataContext.hasChanges()
        merged1.id != null
        dataContext.contains(merged1)
        merged2.id != null
        dataContext.contains(merged2)
    }

    def "save graph"() {
        def dataContext = factory.createDataContext()

        TestNullableIdEntity master = new TestNullableIdEntity(name: "master1")
        TestNullableIdItemEntity item1 = new TestNullableIdItemEntity(name: "item1", master: master)
        TestNullableIdItemEntity item2 = new TestNullableIdItemEntity(name: "item2", master: master)
        master.items = [item1, item2]

        when:
        def mergedMaster = dataContext.merge(master)

        then:
        dataContext.hasChanges()
        !mergedMaster.is(master)
        !mergedMaster.items[0].is(item1)
        mergedMaster.items[0].master.is(mergedMaster)
        mergedMaster.items[1].master.is(mergedMaster)
        dataContext.find(mergedMaster).is(mergedMaster)
        dataContext.find(mergedMaster.items[0]).is(mergedMaster.items[0])
        dataContext.find(mergedMaster.items[1]).is(mergedMaster.items[1])

        when:
        dataContext.save()

        then:
        !dataContext.hasChanges()
        mergedMaster.id != null
        mergedMaster.items[0].id != null
        mergedMaster.items[1].id != null
        dataContext.find(TestNullableIdEntity, mergedMaster.id).is(mergedMaster)
        dataContext.find(TestNullableIdItemEntity, mergedMaster.items[0].id).is(mergedMaster.items[0])
        dataContext.find(TestNullableIdItemEntity, mergedMaster.items[1].id).is(mergedMaster.items[1])
    }
}
