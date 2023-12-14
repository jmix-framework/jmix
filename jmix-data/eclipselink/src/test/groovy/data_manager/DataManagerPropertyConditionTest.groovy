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
import io.jmix.core.querycondition.LogicalCondition
import io.jmix.core.querycondition.PropertyCondition
import io.jmix.data.impl.jpql.generator.ConditionGenerationContext
import io.jmix.data.impl.jpql.generator.PropertyConditionGenerator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import test_support.DataSpec
import test_support.entity.TestAppEntity
import test_support.entity.TestAppEntityItem

class DataManagerPropertyConditionTest extends DataSpec {

    @Autowired
    DataManager dataManager

    @Autowired
    @Qualifier("data_PropertyConditionGenerator")
    PropertyConditionGenerator propertyConditionGenerator

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

    def "load using PropertyCondition in list"() {

        TestAppEntity testEntity1 = dataManager.create(TestAppEntity)
        testEntity1.name = 'test one'

        TestAppEntity testEntity2 = dataManager.create(TestAppEntity)
        testEntity2.name = 'test two'

        TestAppEntity testEntity3 = dataManager.create(TestAppEntity)
        testEntity3.name = 'test three'

        dataManager.save(testEntity1, testEntity2, testEntity3)

        when:

        def list = dataManager.load(TestAppEntity)
                .condition(PropertyCondition.inList("name", ["test one", "test two"]))
                .list()

        then:

        list.contains(testEntity1)
        list.contains(testEntity2)
    }

    def "load using PropertyCondition not in list"() {

        TestAppEntity testEntity1 = dataManager.create(TestAppEntity)
        testEntity1.name = 'test one'

        TestAppEntity testEntity2 = dataManager.create(TestAppEntity)
        testEntity2.name = 'test two'

        TestAppEntity testEntity3 = dataManager.create(TestAppEntity)
        testEntity3.name = 'test three'

        dataManager.save(testEntity1, testEntity2, testEntity3)

        when:

        def list = dataManager.load(TestAppEntity)
                .condition(PropertyCondition.notInList("name", ["test one", "test two"]))
                .list()

        then:

        list.contains(testEntity3)
    }

    def "load using PropertyCondition for non-empty collections"() {

        TestAppEntity testAppEntity1 = dataManager.create(TestAppEntity)
        testAppEntity1.name = 'test one'

        TestAppEntity testAppEntity2 = dataManager.create(TestAppEntity)
        testAppEntity2.name = 'test two'

        TestAppEntityItem appEntityItem1 = dataManager.create(TestAppEntityItem)
        appEntityItem1.name = 'one one'
        appEntityItem1.appEntity = testAppEntity1

        TestAppEntityItem appEntityItem2 = dataManager.create(TestAppEntityItem)
        appEntityItem2.name = 'one two'
        appEntityItem2.appEntity = testAppEntity1

        dataManager.save(testAppEntity1, testAppEntity2, appEntityItem1, appEntityItem2)

        when:

        def list = dataManager.load(TestAppEntity)
                .condition(
                        LogicalCondition.and()
                                .add(PropertyCondition.isCollectionEmpty("items", false))
                )
                .list()

        then:

        list == [testAppEntity1]
    }

    def "load using PropertyCondition for empty collections"() {

        TestAppEntity testAppEntity1 = dataManager.create(TestAppEntity)
        testAppEntity1.name = 'test one'

        TestAppEntity testAppEntity2 = dataManager.create(TestAppEntity)
        testAppEntity2.name = 'test two'

        TestAppEntityItem appEntityItem1 = dataManager.create(TestAppEntityItem)
        appEntityItem1.name = 'one one'
        appEntityItem1.appEntity = testAppEntity1

        TestAppEntityItem appEntityItem2 = dataManager.create(TestAppEntityItem)
        appEntityItem2.name = 'one two'
        appEntityItem2.appEntity = testAppEntity1

        dataManager.save(testAppEntity1, testAppEntity2, appEntityItem1, appEntityItem2)

        when:

        def list = dataManager.load(TestAppEntity)
                .condition(
                        LogicalCondition.and()
                                .add(PropertyCondition.isCollectionEmpty("items", true))
                )
                .list()

        then:

        list == [testAppEntity2]
    }

    def "load using PropertyCondition 'member of'"() {

        TestAppEntity testAppEntity1 = dataManager.create(TestAppEntity)
        testAppEntity1.name = 'test one'

        TestAppEntity testAppEntity2 = dataManager.create(TestAppEntity)
        testAppEntity2.name = 'test two'

        TestAppEntityItem appEntityItem1 = dataManager.create(TestAppEntityItem)
        appEntityItem1.name = 'one one'
        appEntityItem1.appEntity = testAppEntity1

        TestAppEntityItem appEntityItem2 = dataManager.create(TestAppEntityItem)
        appEntityItem2.name = 'two one'
        appEntityItem2.appEntity = testAppEntity2

        dataManager.save(testAppEntity1, testAppEntity2, appEntityItem1, appEntityItem2)

        when:

        def list = dataManager.load(TestAppEntity)
                .condition(PropertyCondition.memberOfCollection("items", appEntityItem1))
                .list()

        then:

        list == [testAppEntity1]
    }

    def "load using PropertyCondition 'not member of'"() {

        TestAppEntity testAppEntity1 = dataManager.create(TestAppEntity)
        testAppEntity1.name = 'test one'

        TestAppEntity testAppEntity2 = dataManager.create(TestAppEntity)
        testAppEntity2.name = 'test two'

        TestAppEntityItem appEntityItem1 = dataManager.create(TestAppEntityItem)
        appEntityItem1.name = 'one one'
        appEntityItem1.appEntity = testAppEntity1

        TestAppEntityItem appEntityItem2 = dataManager.create(TestAppEntityItem)
        appEntityItem2.name = 'two one'
        appEntityItem2.appEntity = testAppEntity2

        dataManager.save(testAppEntity1, testAppEntity2, appEntityItem1, appEntityItem2)

        when:

        def list = dataManager.load(TestAppEntity)
                .condition(PropertyCondition.notMemberOfCollection("items", appEntityItem1))
                .list()

        then:

        list == [testAppEntity2]
    }

    def "load using PropertyCondition for collection properties"() {

        TestAppEntity testAppEntity1 = dataManager.create(TestAppEntity)
        testAppEntity1.name = 'test one'

        TestAppEntity testAppEntity2 = dataManager.create(TestAppEntity)
        testAppEntity2.name = 'test two'

        TestAppEntityItem appEntityItem1 = dataManager.create(TestAppEntityItem)
        appEntityItem1.name = 'one two'
        appEntityItem1.appEntity = testAppEntity1

        TestAppEntityItem appEntityItem2 = dataManager.create(TestAppEntityItem)
        appEntityItem2.name = 'three'
        appEntityItem2.appEntity = testAppEntity2

        dataManager.save(testAppEntity1, testAppEntity2, appEntityItem1, appEntityItem2)

        when:

        def list = dataManager.load(TestAppEntity)
                .condition(
                        LogicalCondition.and()
                                .add(PropertyCondition.contains("items.name", "one"))
                                .add(PropertyCondition.contains("items.name", "two"))
                )
                .list()

        then:

        list == [testAppEntity1]

        when:
        list = dataManager.load(TestAppEntity)
                .condition(
                        LogicalCondition.and()
                                .add(PropertyCondition.contains("items.appEntity.name", "two"))
                )
                .list()

        then:

        list == [testAppEntity2]

        when:
        list = dataManager.load(TestAppEntity)
                .condition(
                        LogicalCondition.and()
                                .add(PropertyCondition.contains("items.appEntity.items.name", "two"))
                )
                .list()

        then:

        list == [testAppEntity1]
    }

    def "PropertyCondition generator join to one test"() {
        when:

        def property = PropertyCondition.equal("appEntity.name","Test")
        def context = new ConditionGenerationContext(property)
        context.entityName = "test_TestAppEntityItem"
        context.entityAlias = "e"

        then:

        ""==propertyConditionGenerator.generateJoin(context)
        propertyConditionGenerator.generateWhere(context).contains("e.appEntity.name =")
    }

    def "PropertyCondition generator join to many test"() {
        when:

        def property = PropertyCondition.equal("items.name","Test")
        def context = new ConditionGenerationContext(property)
        context.entityName = "test_TestAppEntity"
        context.entityAlias = "e"

        then:

        propertyConditionGenerator.generateJoin(context).contains("join e.items ")
        propertyConditionGenerator.generateWhere(context).contains(context.joinAlias+".name =")
    }

    def "PropertyCondition generator join to one and many test"() {
        when:

        def property = PropertyCondition.equal("appEntity.items.name","Test")
        def context = new ConditionGenerationContext(property)
        context.entityName = "test_TestAppEntityItem"
        context.entityAlias = "e"

        then:

        propertyConditionGenerator.generateJoin(context).contains("join e.appEntity.items ")
        propertyConditionGenerator.generateWhere(context).contains(context.joinAlias+".name =")
    }

    def "PropertyCondition generator multiple join to many test"() {
        when:

        def property = PropertyCondition.equal("items.appEntity.items.name","Test")
        def context = new ConditionGenerationContext(property)
        context.entityName = "test_TestAppEntity"
        context.entityAlias = "e"

        then:

        propertyConditionGenerator.generateJoin(context).count("join ")==2
        propertyConditionGenerator.generateWhere(context).contains(context.joinAlias+".name =")
    }

    def "PropertyCondition generator multiple join to one and many test"() {
        when:

        def property = PropertyCondition.equal("appEntity.items.appEntity.items.name","Test")
        def context = new ConditionGenerationContext(property)
        context.entityName = "test_TestAppEntityItem"
        context.entityAlias = "e"

        then:

        propertyConditionGenerator.generateJoin(context).count("join ")==2
        propertyConditionGenerator.generateJoin(context).count("appEntity.items")==2
        propertyConditionGenerator.generateWhere(context).contains(context.joinAlias+".name =")
    }
}
