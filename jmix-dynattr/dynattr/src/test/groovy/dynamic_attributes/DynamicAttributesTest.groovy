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

import io.jmix.core.*
import io.jmix.core.entity.EntityValues
import io.jmix.data.DataConfiguration
import io.jmix.data.entity.ReferenceToEntity
import io.jmix.dynattr.AttributeType
import io.jmix.dynattr.DynAttrConfiguration
import io.jmix.dynattr.DynAttrMetadata
import io.jmix.dynattr.DynAttrQueryHints
import io.jmix.dynattr.model.CategoryAttribute
import io.jmix.eclipselink.EclipselinkConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.JmixDynAttrTestConfiguration
import test_support.TestContextInititalizer
import test_support.UserRepository
import test_support.entity.Group
import test_support.entity.Role
import test_support.entity.User
import test_support.entity.UserRole
import test_support.entity.event_clearing.Project
import test_support.entity.event_clearing.Task

import javax.sql.DataSource

@ContextConfiguration(
        classes = [CoreConfiguration, DataConfiguration, EclipselinkConfiguration,
                DynAttrConfiguration, JmixDynAttrTestConfiguration],
        initializers = [TestContextInititalizer]
)
class DynamicAttributesTest extends Specification {
    @Autowired
    protected DataManager dataManager
    @Autowired
    protected DynAttrMetadata dynamicModelConfiguration
    @Autowired
    protected Metadata metadata
    @Autowired
    protected DataSource dataSource;
    @Autowired
    protected UserRepository repository;

    protected io.jmix.dynattr.model.Category userCategory, userRoleCategory, roleCategory, taskCategory

    protected CategoryAttribute userAttribute, userRoleAttribute, roleAttribute,
                                userGroupAttribute, userGroupCollectionAttribute, userIntCollectionAttribute,
                                userEnumAttribute, userEnumCollectionAttribute, taskAttribute

    protected User user1, user2

    protected UserRole userRole

    protected Role role

    protected Group group1, group2

    void setup() {
        userCategory = metadata.create(io.jmix.dynattr.model.Category)
        userCategory.name = 'user'
        userCategory.entityType = 'dynattr$User'

        userRoleCategory = metadata.create(io.jmix.dynattr.model.Category)
        userRoleCategory.name = 'userRole'
        userRoleCategory.entityType = 'dynattr$UserRole'

        roleCategory = metadata.create(io.jmix.dynattr.model.Category)
        roleCategory.name = 'role'
        roleCategory.entityType = 'dynattr$Role'

        createTaskAndProjectAttributes()

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
                roleAttribute,
                taskCategory,
                taskAttribute)

        dataManager.save(user1, role, userRole, group1, group2)

        dynamicModelConfiguration.reload()
    }

    void cleanup() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource)
        jdbcTemplate.update('delete from DYNAT_ATTR_VALUE')
        jdbcTemplate.update('delete from DYNAT_CATEGORY_ATTR')
        jdbcTemplate.update('delete from DYNAT_CATEGORY')
        jdbcTemplate.update('delete from DYNATTR_GROUP')
        jdbcTemplate.update('delete from DYNATTR_USER_ROLE')
        jdbcTemplate.update('delete from DYNATTR_ROLE')
        jdbcTemplate.update('delete from DYNATTR_USER')
        jdbcTemplate.update('delete from DYNAT_TASK')
        jdbcTemplate.update('delete from DYNAT_PROJECT')
    }

    protected void createUserAttributes() {
        userAttribute = metadata.create(CategoryAttribute)
        userAttribute.name = 'userAttribute'
        userAttribute.code = 'userAttribute'
        userAttribute.dataType = AttributeType.STRING
        userAttribute.categoryEntityType = 'dynattr$User'
        userAttribute.category = userCategory
        userAttribute.defaultEntity = new ReferenceToEntity()

        userGroupAttribute = metadata.create(CategoryAttribute)
        userGroupAttribute.name = 'userGroupAttribute'
        userGroupAttribute.code = 'userGroupAttribute'
        userGroupAttribute.dataType = AttributeType.ENTITY
        userGroupAttribute.categoryEntityType = 'dynattr$User'
        userGroupAttribute.category = userCategory
        userGroupAttribute.entityClass = 'test_support.entity.Group'
        userGroupAttribute.defaultEntity = new ReferenceToEntity()

        userGroupCollectionAttribute = metadata.create(CategoryAttribute)
        userGroupCollectionAttribute.name = 'userGroupCollectionAttribute'
        userGroupCollectionAttribute.code = 'userGroupCollectionAttribute'
        userGroupCollectionAttribute.dataType = AttributeType.ENTITY
        userGroupCollectionAttribute.categoryEntityType = 'dynattr$User'
        userGroupCollectionAttribute.category = userCategory
        userGroupCollectionAttribute.entityClass = 'test_support.entity.Group'
        userGroupCollectionAttribute.isCollection = true
        userGroupCollectionAttribute.defaultEntity = new ReferenceToEntity()

        userIntCollectionAttribute = metadata.create(CategoryAttribute)
        userIntCollectionAttribute.name = 'userIntCollectionAttribute'
        userIntCollectionAttribute.code = 'userIntCollectionAttribute'
        userIntCollectionAttribute.dataType = AttributeType.INTEGER
        userIntCollectionAttribute.categoryEntityType = 'dynattr$User'
        userIntCollectionAttribute.category = userCategory
        userIntCollectionAttribute.isCollection = true
        userIntCollectionAttribute.defaultEntity = new ReferenceToEntity()

        userEnumAttribute = metadata.create(CategoryAttribute)
        userEnumAttribute.name = 'userEnumAttribute'
        userEnumAttribute.code = 'userEnumAttribute'
        userEnumAttribute.dataType = AttributeType.ENUMERATION
        userEnumAttribute.categoryEntityType = 'dynattr$User'
        userEnumAttribute.category = userCategory
        userEnumAttribute.enumeration = 'option1,option2,option3'
        userEnumAttribute.defaultEntity = new ReferenceToEntity()

        userEnumCollectionAttribute = metadata.create(CategoryAttribute)
        userEnumCollectionAttribute.name = 'userEnumCollectionAttribute'
        userEnumCollectionAttribute.code = 'userEnumCollectionAttribute'
        userEnumCollectionAttribute.dataType = AttributeType.ENUMERATION
        userEnumCollectionAttribute.categoryEntityType = 'dynattr$User'
        userEnumCollectionAttribute.category = userCategory
        userEnumCollectionAttribute.enumeration = 'option1,option2,option3'
        userEnumCollectionAttribute.isCollection = true
        userEnumCollectionAttribute.defaultEntity = new ReferenceToEntity()
    }

    protected void createUserRoleAttributes() {
        userRoleAttribute = metadata.create(CategoryAttribute)
        userRoleAttribute.name = 'userRoleAttribute'
        userRoleAttribute.code = 'userRoleAttribute'
        userRoleAttribute.dataType = AttributeType.STRING
        userRoleAttribute.categoryEntityType = 'dynattr$UserRole'
        userRoleAttribute.category = userRoleCategory
        userRoleAttribute.defaultEntity = new ReferenceToEntity()
    }

    protected void createRoleAttributes() {
        roleAttribute = metadata.create(CategoryAttribute)
        roleAttribute.name = 'roleAttribute'
        roleAttribute.code = 'roleAttribute'
        roleAttribute.dataType = AttributeType.STRING
        roleAttribute.categoryEntityType = 'dynattr$Role'
        roleAttribute.category = roleCategory
        roleAttribute.defaultEntity = new ReferenceToEntity()
    }

    protected void createTaskAndProjectAttributes() {
        taskCategory = metadata.create(io.jmix.dynattr.model.Category)
        taskCategory.name = 'task'
        taskCategory.entityType = 'dynat_Task'

        taskAttribute = metadata.create(CategoryAttribute)
        taskAttribute.name = 'taskAttribute'
        taskAttribute.code = 'taskAttribute'
        taskAttribute.dataType = AttributeType.STRING
        taskAttribute.categoryEntityType = 'dynat_Task'
        taskAttribute.category = taskCategory
        taskAttribute.defaultEntity = new ReferenceToEntity()
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
                .hint(DynAttrQueryHints.LOAD_DYN_ATTR, true)
                .one()

        then:
        EntityValues.getValue(user, '+userAttribute') == 'userName'
    }

    def "load user and save with dynamic attributes"() {
        setup:

        def user = dataManager.load(User)
                .id(user1.id)
                .hint(DynAttrQueryHints.LOAD_DYN_ATTR, true)
                .one()

        when:

        EntityValues.setValue(user, '+userAttribute', 'userName')
        dataManager.save(user)
        user = dataManager.load(User)
                .id(user1.id)
                .hint(DynAttrQueryHints.LOAD_DYN_ATTR, true)
                .one()

        then:
        EntityValues.getValue(user, '+userAttribute') == 'userName'
    }

    def "load nested dynamic attributes"() {
        setup:

        def user = dataManager.load(User)
                .id(user1.id)
                .hint(DynAttrQueryHints.LOAD_DYN_ATTR, true)
                .one()

        def role = dataManager.load(Role)
                .id(role.id)
                .hint(DynAttrQueryHints.LOAD_DYN_ATTR, true)
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
                .hint(DynAttrQueryHints.LOAD_DYN_ATTR, true)
                .one()

        then:
        EntityValues.getValue(user, '+userAttribute') == 'userName'
        EntityValues.getValue(user.userRoles[0].role, '+roleAttribute') == 'roleName'
    }

    def "load/save entity with entity dynamic attribute"() {
        setup:

        def user = dataManager.load(User)
                .id(user1.id)
                .hint(DynAttrQueryHints.LOAD_DYN_ATTR, true)
                .one()

        def group = dataManager.load(Group)
                .id(group1.id)
                .one()

        when:

        EntityValues.setValue(user, '+userGroupAttribute', group)
        dataManager.save(user)
        user = dataManager.load(User)
                .id(user1.id)
                .hint(DynAttrQueryHints.LOAD_DYN_ATTR, true)
                .one()

        then:
        EntityValues.getValue(user, '+userGroupAttribute') == group
    }

    def "load/save entity with collection of entities dynamic attribute"() {
        setup:

        def user = dataManager.load(User)
                .id(user1.id)
                .hint(DynAttrQueryHints.LOAD_DYN_ATTR, true)
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
                .hint(DynAttrQueryHints.LOAD_DYN_ATTR, true)
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
                .hint(DynAttrQueryHints.LOAD_DYN_ATTR, true)
                .one()

        when:

        EntityValues.setValue(user, '+userIntCollectionAttribute', [1, 5])
        dataManager.save(user)
        user = dataManager.load(User)
                .id(user1.id)
                .hint(DynAttrQueryHints.LOAD_DYN_ATTR, true)
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
                .hint(DynAttrQueryHints.LOAD_DYN_ATTR, true)
                .one()

        when:

        EntityValues.setValue(user, '+userEnumCollectionAttribute', ['option1', 'option3'])
        dataManager.save(user)
        user = dataManager.load(User)
                .id(user1.id)
                .hint(DynAttrQueryHints.LOAD_DYN_ATTR, true)
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
                .hint(DynAttrQueryHints.LOAD_DYN_ATTR, true)
                .one()

        when:

        EntityValues.setValue(user, '+userEnumAttribute', 'option2')
        dataManager.save(user)
        user = dataManager.load(User)
                .id(user1.id)
                .hint(DynAttrQueryHints.LOAD_DYN_ATTR, true)
                .one()

        then:
        EntityValues.getValue(user, '+userEnumAttribute') == 'option2'
    }

    def "load dynamic attributes through data repository"() {
        setup:
        String login = 'dynTestUser'
        def user = metadata.create(User)
        user.login = login

        EntityValues.setValue(user, '+userAttribute', 'userName')
        dataManager.save(user)
        when:
        user = repository.findById(user.id).get()
        then:
        EntityValues.getValue(user, '+userAttribute') == 'userName'

        when:
        user = repository.findByLogin(login)[0]
        EntityValues.getValue(user, '+userAttribute')
        then:
        thrown(EntityValueAccessException.class)

        when:
        user = repository.loadByLoginWithExplicitlyDisabledDynamicAttributes(login)[0]
        EntityValues.getValue(user, '+userAttribute')
        then:
        thrown(EntityValueAccessException.class)
    }

    def "check dynattr not caused stackOverflow"() {
        when:
        Project project = metadata.create(Project)
        project.name = 'test'

        project = dataManager.save(project)
        Task task = metadata.create(Task)

        task.name = 'testTask'
        task.project = project
        project.tasks.add(task)

        EntityValues.setValue(task, '+taskAttribute', 'theValue')

        dataManager.save(task, project)

        then:
        noExceptionThrown()

        Task loaded = dataManager.load(Task).id(task.id).hint(DynAttrQueryHints.LOAD_DYN_ATTR, true).one()
        loaded != null
        EntityValues.getValue(loaded, '+taskAttribute') == 'theValue'
    }
}
