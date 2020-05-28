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

package dynamic_attributes

import io.jmix.core.DataManager
import io.jmix.core.FetchPlan
import io.jmix.core.JmixCoreConfiguration
import io.jmix.core.Metadata
import io.jmix.core.entity.EntityValues
import io.jmix.data.JmixDataConfiguration
import io.jmix.data.entity.ReferenceToEntity
import io.jmix.dynattr.AttributeType
import io.jmix.dynattr.DynAttrMetadata
import io.jmix.dynattr.JmixDynAttrConfiguration
import io.jmix.dynattr.impl.model.CategoryAttribute
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.JmixDynAttrTestConfiguration
import test_support.entity.Group
import test_support.entity.Role
import test_support.entity.User
import test_support.entity.UserRole

import javax.sql.DataSource

@ContextConfiguration(classes = [JmixCoreConfiguration, JmixDataConfiguration, JmixDynAttrConfiguration, JmixDynAttrTestConfiguration])
class DynamicAttributesTest extends Specification {
    @Autowired
    protected DataManager dataManager
    @Autowired
    protected DynAttrMetadata dynamicModelConfiguration
    @Autowired
    protected Metadata metadata
    @Autowired
    protected DataSource dataSource;

    protected io.jmix.dynattr.impl.model.Category userCategory, userRoleCategory, roleCategory

    protected CategoryAttribute userAttribute, userRoleAttribute, roleAttribute,
                                userGroupAttribute, userGroupCollectionAttribute, userIntCollectionAttribute,
                                userEnumAttribute, userEnumCollectionAttribute

    protected User user1, user2

    protected UserRole userRole

    protected Role role

    protected Group group1, group2

    void setup() {
        userCategory = new io.jmix.dynattr.impl.model.Category(
                name: 'user',
                entityType: 'dynattr$User'
        )

        userRoleCategory = new io.jmix.dynattr.impl.model.Category(
                name: 'userRole',
                entityType: 'dynattr$UserRole'
        )

        roleCategory = new io.jmix.dynattr.impl.model.Category(
                name: 'role',
                entityType: 'dynattr$Role'
        )

        createUserAttributes()

        createUserRoleAttributes()

        createRoleAttributes()

        user1 = new User(
                name: 'user1',
                login: 'user1'
        )

        role = new Role(name: 'role')

        userRole = new UserRole(user: user1, role: role)

        group1 = new Group(name: 'Group1')

        group2 = new Group(name: 'Group1')

        dataManager.save(
                userCategory,
                userRoleCategory,
                roleCategory,
                userAttribute,
                userGroupAttribute,
                userGroupCollectionAttribute,
                userIntCollectionAttribute,
                userEnumAttribute,
                userEnumCollectionAttribute,
                userRoleAttribute,
                roleAttribute)

        dataManager.save(user1, role, userRole, group1, group2)

        dynamicModelConfiguration.reload()
    }

    void cleanup() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource)
        jdbcTemplate.update('delete from SYS_ATTR_VALUE')
        jdbcTemplate.update('delete from SYS_CATEGORY_ATTR')
        jdbcTemplate.update('delete from SYS_CATEGORY')
        jdbcTemplate.update('delete from DYNATTR_GROUP')
        jdbcTemplate.update('delete from DYNATTR_USER_ROLE')
        jdbcTemplate.update('delete from DYNATTR_ROLE')
        jdbcTemplate.update('delete from DYNATTR_USER')
    }

    protected void createUserAttributes() {
        userAttribute = new CategoryAttribute(
                name: 'userAttribute',
                code: 'userAttribute',
                dataType: AttributeType.STRING,
                categoryEntityType: 'dynattr$User',
                category: userCategory,
                defaultEntity: new ReferenceToEntity()
        )

        userGroupAttribute = new CategoryAttribute(
                name: 'userGroupAttribute',
                code: 'userGroupAttribute',
                dataType: AttributeType.ENTITY,
                categoryEntityType: 'dynattr$User',
                category: userCategory,
                entityClass: 'test_support.entity.Group',
                defaultEntity: new ReferenceToEntity()
        )

        userGroupCollectionAttribute = new CategoryAttribute(
                name: 'userGroupCollectionAttribute',
                code: 'userGroupCollectionAttribute',
                dataType: AttributeType.ENTITY,
                categoryEntityType: 'dynattr$User',
                category: userCategory,
                entityClass: 'test_support.entity.Group',
                isCollection: true,
                defaultEntity: new ReferenceToEntity()
        )

        userIntCollectionAttribute = new CategoryAttribute(
                name: 'userIntCollectionAttribute',
                code: 'userIntCollectionAttribute',
                dataType: AttributeType.INTEGER,
                categoryEntityType: 'dynattr$User',
                category: userCategory,
                isCollection: true,
                defaultEntity: new ReferenceToEntity()
        )

        userEnumAttribute = new CategoryAttribute(
                name: 'userEnumAttribute',
                code: 'userEnumAttribute',
                dataType: AttributeType.ENUMERATION,
                categoryEntityType: 'dynattr$User',
                category: userCategory,
                enumeration: 'option1,option2,option3',
                defaultEntity: new ReferenceToEntity()
        )

        userEnumCollectionAttribute = new CategoryAttribute(
                name: 'userEnumCollectionAttribute',
                code: 'userEnumCollectionAttribute',
                dataType: AttributeType.ENUMERATION,
                categoryEntityType: 'dynattr$User',
                category: userCategory,
                enumeration: 'option1,option2,option3',
                isCollection: true,
                defaultEntity: new ReferenceToEntity()
        )
    }

    protected void createUserRoleAttributes() {
        userRoleAttribute = new CategoryAttribute(
                name: 'userRoleAttribute',
                code: 'userRoleAttribute',
                dataType: AttributeType.STRING,
                categoryEntityType: 'dynattr$UserRole',
                category: userRoleCategory,
                defaultEntity: new ReferenceToEntity()
        )
    }

    protected void createRoleAttributes() {
        roleAttribute = new CategoryAttribute(
                name: 'roleAttribute',
                code: 'roleAttribute',
                dataType: AttributeType.STRING,
                categoryEntityType: 'dynattr$Role',
                category: roleCategory,
                defaultEntity: new ReferenceToEntity()
        )
    }

    def "create user and save with dynamic attributes"() {
        setup:

        user2 = metadata.create(User)
        user2.login = 'user2'

        when:

        EntityValues.setValue(user2, '+userAttribute', 'userName')
        dataManager.save(user2)
        def user = dataManager.load(User)
                .id(user2.id)
                .dynamicAttributes(true)
                .one()

        then:
        EntityValues.getValue(user, '+userAttribute') == 'userName'
    }

    def "load user and save with dynamic attributes"() {
        setup:

        def user = dataManager.load(User)
                .id(user1.id)
                .dynamicAttributes(true)
                .one()

        when:

        EntityValues.setValue(user, '+userAttribute', 'userName')
        dataManager.save(user)
        user = dataManager.load(User)
                .id(user1.id)
                .dynamicAttributes(true)
                .one()

        then:
        EntityValues.getValue(user, '+userAttribute') == 'userName'
    }

    def "load nested dynamic attributes"() {
        setup:

        def user = dataManager.load(User)
                .id(user1.id)
                .dynamicAttributes(true)
                .one()

        def role = dataManager.load(Role)
                .id(role.id)
                .dynamicAttributes(true)
                .one()

        EntityValues.setValue(user, '+userAttribute', 'userName')
        EntityValues.setValue(role, '+roleAttribute', 'roleName')

        dataManager.save(user, role)

        when:

        user = dataManager.load(User)
                .id(user1.id)
                .fetchPlan({ builder ->
                    builder.addFetchPlan(FetchPlan.LOCAL)
                            .add("userRoles.role.name")
                })
                .dynamicAttributes(true)
                .one()

        then:
        EntityValues.getValue(user, '+userAttribute') == 'userName'
        EntityValues.getValue(user.userRoles[0].role, '+roleAttribute') == 'roleName'
    }

    def "load/save entity with entity dynamic attribute"() {
        setup:

        def user = dataManager.load(User)
                .id(user1.id)
                .dynamicAttributes(true)
                .one()

        def group = dataManager.load(Group)
                .id(group1.id)
                .one()

        when:

        EntityValues.setValue(user, '+userGroupAttribute', group)
        dataManager.save(user)
        user = dataManager.load(User)
                .id(user1.id)
                .dynamicAttributes(true)
                .one()

        then:
        EntityValues.getValue(user, '+userGroupAttribute') == group
    }

    def "load/save entity with collection of entities dynamic attribute"() {
        setup:

        def user = dataManager.load(User)
                .id(user1.id)
                .dynamicAttributes(true)
                .one()

        def group1 = dataManager.load(Group)
                .id(group1.id)
                .one()

        def group2 = dataManager.load(Group)
                .id(group2.id)
                .one()

        when:

        EntityValues.setValue(user, '+userGroupCollectionAttribute', [group1, group2])
        dataManager.save(user)
        user = dataManager.load(User)
                .id(user1.id)
                .dynamicAttributes(true)
                .one()

        then:
        Collection collection = EntityValues.getValue(user, '+userGroupCollectionAttribute')
        collection != null
        collection.size() == 2
        collection.contains(group1)
        collection.contains(group2)
    }

    def "load/save entity with collection of integers dynamic attribute"() {
        setup:

        def user = dataManager.load(User)
                .id(user1.id)
                .dynamicAttributes(true)
                .one()

        when:

        EntityValues.setValue(user, '+userIntCollectionAttribute', [1, 5])
        dataManager.save(user)
        user = dataManager.load(User)
                .id(user1.id)
                .dynamicAttributes(true)
                .one()

        then:
        Collection collection = EntityValues.getValue(user, '+userIntCollectionAttribute')
        collection != null
        collection.size() == 2
        collection.contains(1)
        collection.contains(5)
    }

    def "load/save entity with collection of enum dynamic attribute"() {
        setup:

        def user = dataManager.load(User)
                .id(user1.id)
                .dynamicAttributes(true)
                .one()

        when:

        EntityValues.setValue(user, '+userEnumCollectionAttribute', ['option1', 'option3'])
        dataManager.save(user)
        user = dataManager.load(User)
                .id(user1.id)
                .dynamicAttributes(true)
                .one()

        then:
        Collection collection = EntityValues.getValue(user, '+userEnumCollectionAttribute')
        collection != null
        collection.size() == 2
        collection.contains('option1')
        collection.contains('option3')
    }

    def "load/save entity with enum dynamic attribute"() {
        setup:

        def user = dataManager.load(User)
                .id(user1.id)
                .dynamicAttributes(true)
                .one()

        when:

        EntityValues.setValue(user, '+userEnumAttribute', 'option2')
        dataManager.save(user)
        user = dataManager.load(User)
                .id(user1.id)
                .dynamicAttributes(true)
                .one()

        then:
        EntityValues.getValue(user, '+userEnumAttribute') == 'option2'
    }
}
