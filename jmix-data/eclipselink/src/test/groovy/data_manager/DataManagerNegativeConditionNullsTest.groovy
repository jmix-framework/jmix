/*
 * Copyright 2026 Haulmont.
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
import io.jmix.core.querycondition.PropertyCondition
import io.jmix.data.impl.jpql.generator.ConditionGenerationContext
import io.jmix.data.impl.jpql.generator.PropertyConditionGenerator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.test.context.TestPropertySource
import test_support.DataSpec
import test_support.entity.TestAppEntity

@TestPropertySource(properties = ["jmix.data.negative-property-condition-includes-nulls=true"])
class DataManagerNegativeConditionNullsTest extends DataSpec {

    @Autowired
    DataManager dataManager

    @Autowired
    @Qualifier("data_PropertyConditionGenerator")
    PropertyConditionGenerator propertyConditionGenerator

    def "NOT_EQUAL includes records with null"() {

        TestAppEntity matching = dataManager.create(TestAppEntity)
        matching.name = 'target'

        TestAppEntity nonMatching = dataManager.create(TestAppEntity)
        nonMatching.name = 'other'

        TestAppEntity withNull = dataManager.create(TestAppEntity)
        withNull.name = null

        dataManager.save(matching, nonMatching, withNull)

        when:

        def list = dataManager.load(TestAppEntity)
                .condition(PropertyCondition.notEqual("name", "target"))
                .list()

        then:

        list.size() == 2
        list.contains(nonMatching)
        list.contains(withNull)
    }

    def "NOT_CONTAINS includes records with null"() {

        TestAppEntity matching = dataManager.create(TestAppEntity)
        matching.name = 'has target inside'

        TestAppEntity nonMatching = dataManager.create(TestAppEntity)
        nonMatching.name = 'other'

        TestAppEntity withNull = dataManager.create(TestAppEntity)
        withNull.name = null

        dataManager.save(matching, nonMatching, withNull)

        when:

        def list = dataManager.load(TestAppEntity)
                .condition(PropertyCondition.createWithValue("name", PropertyCondition.Operation.NOT_CONTAINS, "target"))
                .list()

        then:

        list.size() == 2
        list.contains(nonMatching)
        list.contains(withNull)
    }

    def "NOT_IN_LIST includes records with null"() {

        TestAppEntity matching = dataManager.create(TestAppEntity)
        matching.name = 'one'

        TestAppEntity nonMatching = dataManager.create(TestAppEntity)
        nonMatching.name = 'three'

        TestAppEntity withNull = dataManager.create(TestAppEntity)
        withNull.name = null

        dataManager.save(matching, nonMatching, withNull)

        when:

        def list = dataManager.load(TestAppEntity)
                .condition(PropertyCondition.notInList("name", ["one", "two"]))
                .list()

        then:

        list.size() == 2
        list.contains(nonMatching)
        list.contains(withNull)
    }

    def "generated JPQL wraps NOT_EQUAL with 'is null' check"() {
        when:

        def property = PropertyCondition.notEqual("name", "target")
        def context = new ConditionGenerationContext(property)
        context.entityName = "test_TestAppEntity"
        context.entityAlias = "e"
        def where = propertyConditionGenerator.generateWhere(context)

        then:

        where.contains("e.name <>")
        where.contains("e.name is null")
        where.startsWith("(")
        where.endsWith(")")
    }
}
