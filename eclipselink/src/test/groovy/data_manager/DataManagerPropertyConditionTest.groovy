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

package data_manager


import io.jmix.core.DataManager
import io.jmix.core.EntityStates
import io.jmix.core.querycondition.LogicalCondition
import io.jmix.core.querycondition.PropertyCondition
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.TestAppEntity

class DataManagerPropertyConditionTest extends DataSpec {

    @Autowired
    DataManager dataManager

    @Autowired
    EntityStates entityStates

    def "load using PropertyCondition starts with"() {

        TestAppEntity testEntity1 = dataManager.create(TestAppEntity)
        testEntity1.name = 'test one'

        TestAppEntity testEntity2 = dataManager.create(TestAppEntity)
        testEntity2.name = 'one test'

        dataManager.save(testEntity1, testEntity2)

        when:

        def list = dataManager.load(TestAppEntity)
                .condition(PropertyCondition.startsWith("name", "one"))
                .list()

        then:

        list == [testEntity2]
    }

    def "load using PropertyCondition ends with"() {

        TestAppEntity testEntity1 = dataManager.create(TestAppEntity)
        testEntity1.name = 'test one'

        TestAppEntity testEntity2 = dataManager.create(TestAppEntity)
        testEntity2.name = 'one test'

        dataManager.save(testEntity1, testEntity2)

        when:

        def list = dataManager.load(TestAppEntity)
                .condition(PropertyCondition.endsWith("name", "one"))
                .list()

        then:

        list == [testEntity1]
    }

    def "load using PropertyCondition contains"() {

        TestAppEntity testEntity1 = dataManager.create(TestAppEntity)
        testEntity1.name = 'test one'

        TestAppEntity testEntity2 = dataManager.create(TestAppEntity)
        testEntity2.name = 'one test'

        dataManager.save(testEntity1, testEntity2)

        when:

        def list = dataManager.load(TestAppEntity)
                .condition(PropertyCondition.contains("name", "test"))
                .list()

        then:

        list.contains(testEntity1)
        list.contains(testEntity2)
    }

    def "load using multiple PropertyCondition"() {

        TestAppEntity testAppEntity1 = dataManager.create(TestAppEntity)
        testAppEntity1.name = 'test one'

        TestAppEntity testAppEntity2 = dataManager.create(TestAppEntity)
        testAppEntity2.name = 'test two'

        TestAppEntity testAppEntity123 = dataManager.create(TestAppEntity)
        testAppEntity123.name = 'test one two three'

        dataManager.save(testAppEntity1, testAppEntity2, testAppEntity123)

        when:

        def list = dataManager.load(TestAppEntity)
                .condition(
                        LogicalCondition.and()
                                .add(PropertyCondition.contains("name", "one"))
                                .add(PropertyCondition.contains("name", "two"))
                )
                .list()

        then:

        list == [testAppEntity123]
    }

    def "load using query string and PropertyCondition"() {

        TestAppEntity testAppEntity1 = dataManager.create(TestAppEntity)
        testAppEntity1.name = 'test one'

        TestAppEntity testAppEntity2 = dataManager.create(TestAppEntity)
        testAppEntity2.name = 'test two'

        dataManager.save(testAppEntity1, testAppEntity2)

        when:

        def list = dataManager.load(TestAppEntity)
                .query("select e from test_TestAppEntity e")
                .condition(PropertyCondition.createWithValue("name", PropertyCondition.Operation.CONTAINS, "two"))
                .list()

        then:

        list == [testAppEntity2]
    }

}
