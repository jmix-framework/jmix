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
import test_support.entity.sec.Role
import test_support.entity.sec.User
import test_support.entity.sec.UserRole

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
                .condition(PropertyCondition.startsWith("name", "one").skipNullOrEmpty())
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
                .condition(PropertyCondition.endsWith("name", "one").skipNullOrEmpty())
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
                .condition(PropertyCondition.contains("name", "test").skipNullOrEmpty())
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
                                .add(PropertyCondition.contains("name", "one").skipNullOrEmpty())
                                .add(PropertyCondition.contains("name", "two").skipNullOrEmpty())
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
                .condition(PropertyCondition.createWithValue("name", PropertyCondition.Operation.CONTAINS, "two").skipNullOrEmpty())
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
                .condition(PropertyCondition.inList("name", ["test one", "test two"]).skipNullOrEmpty())
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
                .condition(PropertyCondition.notInList("name", ["test one", "test two"]).skipNullOrEmpty())
                .list()

        then:

        list.contains(testEntity3)
    }

    def "NOT_EQUAL excludes records with null by default"() {

        TestAppEntity testEntity1 = dataManager.create(TestAppEntity)
        testEntity1.name = 'target'

        TestAppEntity testEntity2 = dataManager.create(TestAppEntity)
        testEntity2.name = 'other'

        TestAppEntity testEntity3 = dataManager.create(TestAppEntity)
        testEntity3.name = null

        dataManager.save(testEntity1, testEntity2, testEntity3)

        when:

        def list = dataManager.load(TestAppEntity)
                .condition(PropertyCondition.notEqual("name", "target"))
                .list()

        then:

        list == [testEntity2]
    }

    def "generated JPQL for NOT_EQUAL is not wrapped by default"() {
        when:

        def property = PropertyCondition.notEqual("name", "target")
        def context = new ConditionGenerationContext(property)
        context.entityName = "test_TestAppEntity"
        context.entityAlias = "e"
        def where = propertyConditionGenerator.generateWhere(context)

        then:

        where.contains("e.name <>")
        !where.contains("is null")
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
                                .add(PropertyCondition.isCollectionEmpty("items", false).skipNullOrEmpty())
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
                                .add(PropertyCondition.isCollectionEmpty("items", true).skipNullOrEmpty())
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
                .condition(PropertyCondition.memberOfCollection("items", appEntityItem1).skipNullOrEmpty())
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
                .condition(PropertyCondition.notMemberOfCollection("items", appEntityItem1).skipNullOrEmpty())
                .list()

        then:

        list == [testAppEntity2]
    }

    def "load using PropertyCondition 'member of' combined through OR"() {

        TestAppEntity entityWithTwoItems = dataManager.create(TestAppEntity)
        entityWithTwoItems.name = 'test one'
        entityWithTwoItems.number = '42'

        TestAppEntity entityWithoutItems = dataManager.create(TestAppEntity)
        entityWithoutItems.name = 'test two'
        entityWithoutItems.number = '42'

        TestAppEntity entityWithTargetItem = dataManager.create(TestAppEntity)
        entityWithTargetItem.name = 'test three'

        TestAppEntity entityNotMatching = dataManager.create(TestAppEntity)
        entityNotMatching.name = 'test four'

        TestAppEntityItem item1 = dataManager.create(TestAppEntityItem)
        item1.name = 'one one'
        item1.appEntity = entityWithTwoItems

        TestAppEntityItem item2 = dataManager.create(TestAppEntityItem)
        item2.name = 'one two'
        item2.appEntity = entityWithTwoItems

        TestAppEntityItem targetItem = dataManager.create(TestAppEntityItem)
        targetItem.name = 'three one'
        targetItem.appEntity = entityWithTargetItem

        dataManager.save(entityWithTwoItems, entityWithoutItems, entityWithTargetItem, entityNotMatching,
                item1, item2, targetItem)

        when: "a 'member of' condition is combined with another condition through OR"

        def list = dataManager.load(TestAppEntity)
                .condition(LogicalCondition.or(
                        PropertyCondition.memberOfCollection("items", targetItem),
                        PropertyCondition.equal("number", '42')
                ))
                .list()

        then: "entities matching only the other condition are not lost and no entity is duplicated"

        list.size() == 3
        list.toSet() == [entityWithTwoItems, entityWithoutItems, entityWithTargetItem] as Set
    }

    def "load using PropertyCondition 'member of' on a nested collection combined through OR"() {

        TestAppEntity appEntity = dataManager.create(TestAppEntity)
        appEntity.name = 'test one'

        TestAppEntityItem item1 = dataManager.create(TestAppEntityItem)
        item1.name = 'one one'
        item1.appEntity = appEntity

        TestAppEntityItem item2 = dataManager.create(TestAppEntityItem)
        item2.name = 'one two'
        item2.appEntity = appEntity

        TestAppEntityItem standaloneItem = dataManager.create(TestAppEntityItem)
        standaloneItem.name = 'standalone'

        dataManager.save(appEntity, item1, item2, standaloneItem)

        when: "a 'member of' condition on a collection behind a nullable to-one reference is combined through OR"

        def list = dataManager.load(TestAppEntityItem)
                .condition(LogicalCondition.or(
                        PropertyCondition.memberOfCollection("appEntity.items", item1),
                        PropertyCondition.equal("name", 'standalone')
                ))
                .list()

        then: "the item without the reference matches by name, items whose collection contains the value match once"

        list.size() == 3
        list.toSet() == [item1, item2, standaloneItem] as Set
    }

    def "load using PropertyCondition 'not member of' combined through OR"() {

        TestAppEntity entityWithTargetItem = dataManager.create(TestAppEntity)
        entityWithTargetItem.name = 'test one'
        entityWithTargetItem.number = '1'

        TestAppEntity entityMatchingNumber = dataManager.create(TestAppEntity)
        entityMatchingNumber.name = 'test two'
        entityMatchingNumber.number = '42'

        TestAppEntity entityWithOtherItems = dataManager.create(TestAppEntity)
        entityWithOtherItems.name = 'test three'
        entityWithOtherItems.number = '1'

        TestAppEntityItem targetItem = dataManager.create(TestAppEntityItem)
        targetItem.name = 'one one'
        targetItem.appEntity = entityWithTargetItem

        TestAppEntityItem item1 = dataManager.create(TestAppEntityItem)
        item1.name = 'three one'
        item1.appEntity = entityWithOtherItems

        TestAppEntityItem item2 = dataManager.create(TestAppEntityItem)
        item2.name = 'three two'
        item2.appEntity = entityWithOtherItems

        dataManager.save(entityWithTargetItem, entityMatchingNumber, entityWithOtherItems,
                targetItem, item1, item2)

        when: "a 'not member of' condition is combined with another condition through OR"

        def list = dataManager.load(TestAppEntity)
                .condition(LogicalCondition.or(
                        PropertyCondition.notMemberOfCollection("items", targetItem),
                        PropertyCondition.equal("number", '42')
                ))
                .list()

        then: "the entity containing the value is excluded, the rest match exactly once"

        list.size() == 2
        list.toSet() == [entityMatchingNumber, entityWithOtherItems] as Set
    }

    def "load using PropertyCondition 'not member of' on a nested collection combined through OR"() {

        TestAppEntity appEntityWithTarget = dataManager.create(TestAppEntity)
        appEntityWithTarget.name = 'test one'

        TestAppEntity appEntityWithoutTarget = dataManager.create(TestAppEntity)
        appEntityWithoutTarget.name = 'test two'

        TestAppEntityItem targetItem = dataManager.create(TestAppEntityItem)
        targetItem.name = 'one one'
        targetItem.appEntity = appEntityWithTarget

        TestAppEntityItem siblingItem = dataManager.create(TestAppEntityItem)
        siblingItem.name = 'one two'
        siblingItem.appEntity = appEntityWithTarget

        TestAppEntityItem otherItem = dataManager.create(TestAppEntityItem)
        otherItem.name = 'two one'
        otherItem.appEntity = appEntityWithoutTarget

        TestAppEntityItem standaloneItem = dataManager.create(TestAppEntityItem)
        standaloneItem.name = 'standalone'

        dataManager.save(appEntityWithTarget, appEntityWithoutTarget,
                targetItem, siblingItem, otherItem, standaloneItem)

        when: "a 'not member of' condition on a collection behind a nullable to-one reference is combined through OR"

        def list = dataManager.load(TestAppEntityItem)
                .condition(LogicalCondition.or(
                        PropertyCondition.notMemberOfCollection("appEntity.items", targetItem),
                        PropertyCondition.equal("name", 'one one')
                ))
                .list()

        then: "items whose collection contains the value match only by name, the rest match exactly once"

        list.size() == 3
        list.toSet() == [targetItem, otherItem, standaloneItem] as Set
    }

    def "load using PropertyCondition 'is collection empty' on a nested collection"() {

        User userWithRoles = dataManager.create(User)
        userWithRoles.login = 'u1'
        userWithRoles.name = 'user one'

        Role role = dataManager.create(Role)
        role.name = 'role one'

        UserRole userRole = dataManager.create(UserRole)
        userRole.user = userWithRoles
        userRole.role = role

        User userNoRoles = dataManager.create(User)
        userNoRoles.login = 'u2'
        userNoRoles.name = 'user two'

        TestAppEntity entityWithRoles = dataManager.create(TestAppEntity)
        entityWithRoles.name = 'test one'
        entityWithRoles.author = userWithRoles

        TestAppEntity entityNoRoles = dataManager.create(TestAppEntity)
        entityNoRoles.name = 'test two'
        entityNoRoles.author = userNoRoles

        TestAppEntity entityNoAuthor = dataManager.create(TestAppEntity)
        entityNoAuthor.name = 'test three'

        dataManager.save(userWithRoles, role, userRole, userNoRoles,
                entityWithRoles, entityNoRoles, entityNoAuthor)

        when: "'is empty' condition on a collection behind a to-one reference"

        def list = dataManager.load(TestAppEntity)
                .condition(PropertyCondition.isCollectionEmpty("author.userRoles", true))
                .list()

        then: "only entities with an empty collection or without the reference match"

        list.size() == 2
        list.toSet() == [entityNoRoles, entityNoAuthor] as Set

        when: "'is not empty' condition on a collection behind a to-one reference"

        list = dataManager.load(TestAppEntity)
                .condition(PropertyCondition.isCollectionEmpty("author.userRoles", false))
                .list()

        then: "only entities with a non-empty collection match"

        list == [entityWithRoles]

        cleanup:
        jdbc.update("DELETE FROM TEST_APP_ENTITY")
        jdbc.update("DELETE FROM SEC_USER_ROLE")
        jdbc.update("DELETE FROM SEC_ROLE")
        jdbc.update("DELETE FROM SEC_USER")
    }

    def "load using PropertyCondition 'is collection empty' on a nested collection combined through OR"() {

        User userWithRoles = dataManager.create(User)
        userWithRoles.login = 'u1'
        userWithRoles.name = 'user one'

        Role role = dataManager.create(Role)
        role.name = 'role one'

        UserRole userRole = dataManager.create(UserRole)
        userRole.user = userWithRoles
        userRole.role = role

        User userNoRoles = dataManager.create(User)
        userNoRoles.login = 'u2'
        userNoRoles.name = 'user two'

        TestAppEntity entityWithRoles = dataManager.create(TestAppEntity)
        entityWithRoles.name = 'test one'
        entityWithRoles.author = userWithRoles

        TestAppEntity entityNoRoles = dataManager.create(TestAppEntity)
        entityNoRoles.name = 'test two'
        entityNoRoles.author = userNoRoles

        TestAppEntity entityNotMatching = dataManager.create(TestAppEntity)
        entityNotMatching.name = 'test three'
        entityNotMatching.author = userWithRoles

        dataManager.save(userWithRoles, role, userRole, userNoRoles,
                entityWithRoles, entityNoRoles, entityNotMatching)

        when: "an 'is empty' condition is combined with another condition through OR"

        def list = dataManager.load(TestAppEntity)
                .condition(LogicalCondition.or(
                        PropertyCondition.isCollectionEmpty("author.userRoles", true),
                        PropertyCondition.equal("name", 'test one')
                ))
                .list()

        then: "both branches of the OR contribute to the result"

        list.size() == 2
        list.toSet() == [entityWithRoles, entityNoRoles] as Set

        cleanup:
        jdbc.update("DELETE FROM TEST_APP_ENTITY")
        jdbc.update("DELETE FROM SEC_USER_ROLE")
        jdbc.update("DELETE FROM SEC_ROLE")
        jdbc.update("DELETE FROM SEC_USER")
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
                                .add(PropertyCondition.contains("items.name", "one").skipNullOrEmpty())
                                .add(PropertyCondition.contains("items.name", "two").skipNullOrEmpty())
                )
                .list()

        then:

        list == [testAppEntity1]

        when:
        list = dataManager.load(TestAppEntity)
                .condition(
                        LogicalCondition.and()
                                .add(PropertyCondition.contains("items.appEntity.name", "two").skipNullOrEmpty())
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

        def property = PropertyCondition.equal("appEntity.name", "Test").skipNullOrEmpty()
        def context = new ConditionGenerationContext(property)
        context.entityName = "test_TestAppEntityItem"
        context.entityAlias = "e"

        then:

        " left join e.appEntity cje_0" == propertyConditionGenerator.generateJoin(context)
        propertyConditionGenerator.generateWhere(context).contains("cje_0.name =")
    }

    def "PropertyCondition generator join to many test"() {
        when:

        def property = PropertyCondition.equal("items.name", "Test").skipNullOrEmpty()
        def context = new ConditionGenerationContext(property)
        context.entityName = "test_TestAppEntity"
        context.entityAlias = "e"

        then:

        propertyConditionGenerator.generateJoin(context).contains("join e.items ")
        propertyConditionGenerator.generateWhere(context).contains(context.joinAlias + ".name =")
    }

    def "PropertyCondition generator join to one and many test"() {
        when:

        def property = PropertyCondition.equal("appEntity.items.name", "Test").skipNullOrEmpty()
        def context = new ConditionGenerationContext(property)
        context.entityName = "test_TestAppEntityItem"
        context.entityAlias = "e"

        then:

        propertyConditionGenerator.generateJoin(context).contains(" left join e.appEntity cje_0 left join cje_0.items cje_1")
        propertyConditionGenerator.generateWhere(context).contains(context.joinAlias + ".name =")
    }


    def "PropertyCondition generator multiple join to many test"() {
        when:

        def property = PropertyCondition.equal("items.appEntity.items.name", "Test").skipNullOrEmpty()
        def context = new ConditionGenerationContext(property)
        context.entityName = "test_TestAppEntity"
        context.entityAlias = "e"

        then:

        propertyConditionGenerator.generateJoin(context).count("join ") == 3
        propertyConditionGenerator.generateWhere(context).contains(context.joinAlias + ".name =")
    }

    def "PropertyCondition generator multiple join to one and many test"() {
        when:

        def property = PropertyCondition.equal("appEntity.items.appEntity.items.name", "Test").skipNullOrEmpty()
        def context = new ConditionGenerationContext(property)
        context.entityName = "test_TestAppEntityItem"
        context.entityAlias = "e"
        def joinClause = propertyConditionGenerator.generateJoin(context)
        def whereClause = propertyConditionGenerator.generateWhere(context)

        then:

        joinClause.count("join ") == 4
        joinClause.count("left join cje_") == 3
        whereClause.contains(context.joinAlias + ".name =")
    }

    def "basic outer join generation test"() {
        setup:
        TestAppEntity entity1 = dataManager.create(TestAppEntity)
        entity1.name = "e1"
        entity1.number = 1

        User user = dataManager.create(User)
        user.name = "u1"
        entity1.author = user

        TestAppEntity entity2 = dataManager.create(TestAppEntity)
        entity2.name = "e2"
        entity2.number = 2

        dataManager.save(user, entity1, entity2)

        when:
        List<TestAppEntity> orConditionResult
                = dataManager.load(TestAppEntity)
                .condition(LogicalCondition.or(
                        PropertyCondition.createWithValue("author.name", PropertyCondition.Operation.EQUAL, "u1"),
                        PropertyCondition.createWithValue("number", PropertyCondition.Operation.EQUAL, "2")
                ))
                .list()


        then:
        orConditionResult.size() == 2


        cleanup:
        jdbc.update("DELETE FROM TEST_APP_ENTITY")
        jdbc.update("DELETE FROM SEC_USER")

    }

    def "outer join generation test with param and jpql"() {
        setup:
        TestAppEntity entity1 = dataManager.create(TestAppEntity)
        entity1.name = "e1"
        entity1.number = 1

        User user = dataManager.create(User)
        user.name = "u1"
        user.firstName = "fn1"
        entity1.author = user

        TestAppEntityItem mainItem = dataManager.create(TestAppEntityItem)
        mainItem.name = "ma1"
        entity1.mainItem = mainItem

        TestAppEntity entity2 = dataManager.create(TestAppEntity)
        entity2.name = "e2"
        entity2.number = 2
        entity2.mainItem = mainItem

        TestAppEntity entity3 = dataManager.create(TestAppEntity)
        entity3.name = "e2"
        entity3.number = 2

        dataManager.save(user, mainItem, entity1, entity2, entity3)


        when:
        List<TestAppEntity> orConditionResult
                = dataManager.load(TestAppEntity)
                .query("select e from test_TestAppEntity e left join e.mainItem a_0 where a_0.name = 'ma1'")
                .condition(LogicalCondition.or(
                        PropertyCondition.createWithParameterName("author.name", PropertyCondition.Operation.EQUAL, "authorName"),
                        PropertyCondition.createWithParameterName("number", PropertyCondition.Operation.EQUAL, "number")
                ))
                .parameter("number", "2")
                .parameter("authorName", "u1")
                .list()


        then:
        orConditionResult.size() == 2 //entity3 filtered out by query condition


        cleanup:
        jdbc.update("DELETE FROM TEST_APP_ENTITY")
        jdbc.update("DELETE FROM SEC_USER")
        jdbc.update("DELETE FROM TEST_APP_ENTITY_ITEM")
    }

    // The five tests below pin down the ESCAPE-clause contract emitted by
    // PropertyConditionGenerator for LIKE-based operations: a backslash in the parameter
    // value escapes the following character so '_' and '%' can be matched literally.
    // Direct PropertyCondition users who want SQL wildcards keep passing '_'/'%' as-is.

    def "PropertyCondition 'contains' with backslash-escaped underscore matches it literally"() {

        TestAppEntity literal = dataManager.create(TestAppEntity)
        literal.name = '789_321'

        TestAppEntity wildcardMatch = dataManager.create(TestAppEntity)
        // matches LIKE '%_32%' when '_' is a wildcard, but lacks the literal '_32'
        wildcardMatch.name = '7894321'

        dataManager.save(literal, wildcardMatch)

        when:

        def list = dataManager.load(TestAppEntity)
                .condition(PropertyCondition.contains("name", "\\_32").skipNullOrEmpty())
                .list()

        then:

        list == [literal]
    }

    def "PropertyCondition 'contains' with backslash-escaped percent matches it literally"() {

        TestAppEntity literal = dataManager.create(TestAppEntity)
        literal.name = 'yuj%321'

        TestAppEntity wildcardMatch = dataManager.create(TestAppEntity)
        wildcardMatch.name = 'yuj32 something'

        dataManager.save(literal, wildcardMatch)

        when:

        def list = dataManager.load(TestAppEntity)
                .condition(PropertyCondition.contains("name", "\\%32").skipNullOrEmpty())
                .list()

        then:

        list == [literal]
    }

    def "PropertyCondition 'startsWith' with backslash-escaped underscore matches it literally"() {

        TestAppEntity literal = dataManager.create(TestAppEntity)
        literal.name = '_321 abc'

        TestAppEntity wildcardMatch = dataManager.create(TestAppEntity)
        wildcardMatch.name = 'X321 abc'

        dataManager.save(literal, wildcardMatch)

        when:

        def list = dataManager.load(TestAppEntity)
                .condition(PropertyCondition.startsWith("name", "\\_321").skipNullOrEmpty())
                .list()

        then:

        list == [literal]
    }

    def "PropertyCondition 'endsWith' with backslash-escaped percent matches it literally"() {

        TestAppEntity literal = dataManager.create(TestAppEntity)
        literal.name = 'abc 100%'

        TestAppEntity wildcardMatch = dataManager.create(TestAppEntity)
        wildcardMatch.name = 'abc 1003'

        dataManager.save(literal, wildcardMatch)

        when:

        def list = dataManager.load(TestAppEntity)
                .condition(PropertyCondition.endsWith("name", "100\\%").skipNullOrEmpty())
                .list()

        then:

        list == [literal]
    }

    def "PropertyCondition 'notContains' with backslash-escaped underscore matches it literally"() {

        TestAppEntity literal = dataManager.create(TestAppEntity)
        literal.name = '789_321'

        TestAppEntity other = dataManager.create(TestAppEntity)
        other.name = '7894321'

        dataManager.save(literal, other)

        when:

        def list = dataManager.load(TestAppEntity)
                .condition(PropertyCondition.createWithValue("name", PropertyCondition.Operation.NOT_CONTAINS, "\\_32").skipNullOrEmpty())
                .list()

        then:

        list == [other]
    }

    def "PropertyCondition 'contains' without escape still treats underscore as a SQL wildcard"() {

        TestAppEntity literal = dataManager.create(TestAppEntity)
        literal.name = '789_321'

        TestAppEntity wildcardMatch = dataManager.create(TestAppEntity)
        // matched as the wildcard expansion of '_32' = 'X32'
        wildcardMatch.name = '7894321'

        dataManager.save(literal, wildcardMatch)

        when:

        def list = dataManager.load(TestAppEntity)
                .condition(PropertyCondition.contains("name", "_32").skipNullOrEmpty())
                .list()

        then: "both entities match because '_' is still a SQL wildcard at the API level"

        list.size() == 2
        list.containsAll([literal, wildcardMatch])
    }
}
