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
import test_support.entity.TestStringIdEntity
import test_support.spec.DataContextSpec

class DataContextNullStringIdTest extends DataContextSpec {

    @Autowired
    private DataComponents factory

    def "can contain any number of instances with null id"() {
        TestStringIdEntity entity1 = new TestStringIdEntity(name: "111")
        TestStringIdEntity entity2 = new TestStringIdEntity(name: "222")

        def dataContext = factory.createDataContext()

        when:
        def merged1 = dataContext.merge(entity1)
        def merged2 = dataContext.merge(entity2)

        then:
        dataContext.contains(merged1)
        dataContext.contains(merged2)
        dataContext.content.get(TestStringIdEntity).size() == 2
    }

    def "merged instance with null id"() {
        TestStringIdEntity entity = new TestStringIdEntity(name: "111")

        def dataContext = factory.createDataContext()

        when:

        def merged = dataContext.merge(entity)

        then:

        !merged.is(entity)
        dataContext.contains(entity)
        dataContext.contains(merged)
    }

    def "merged instance changes id"() {
        TestStringIdEntity entity = new TestStringIdEntity(name: "111")

        def dataContext = factory.createDataContext()

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
        dataContext.content.get(TestStringIdEntity).size() == 1
    }
}
