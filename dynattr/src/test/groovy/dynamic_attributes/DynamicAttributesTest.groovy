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
import io.jmix.core.JmixCoreConfiguration
import io.jmix.core.entity.EntityValues
import io.jmix.data.JmixDataConfiguration
import io.jmix.data.entity.ReferenceToEntity
import io.jmix.dynattr.AttributeType
import io.jmix.dynattr.DynAttrMetadata
import io.jmix.dynattr.JmixDynAttrConfiguration
import io.jmix.dynattr.impl.model.CategoryAttribute
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.JmixDynAttrTestConfiguration
import test_support.entity.Role
import test_support.entity.User
import test_support.entity.UserRole

import org.springframework.beans.factory.annotation.Autowired

@ContextConfiguration(classes = [JmixCoreConfiguration, JmixDataConfiguration, JmixDynAttrConfiguration, JmixDynAttrTestConfiguration])
class DynamicAttributesTest extends Specification {
    @Autowired
    protected DataManager dataManager
    @Autowired
    protected DynAttrMetadata dynamicModelConfiguration

    protected io.jmix.dynattr.impl.model.Category userCategory, userRoleCategory, roleCategory

    protected CategoryAttribute userAttribute, userRoleAttribute, roleAttribute

    protected User user1, user2

    protected UserRole userRole

    protected Role role

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

        userAttribute = new CategoryAttribute(
                name: 'userAttribute',
                code: 'userAttribute',
                dataType: AttributeType.STRING,
                categoryEntityType: 'dynattr$User',
                category: userCategory,
                defaultEntity: new ReferenceToEntity()
        )

        userRoleAttribute = new CategoryAttribute(
                name: 'userRoleAttribute',
                code: 'userRoleAttribute',
                dataType: AttributeType.STRING,
                categoryEntityType: 'dynattr$UserRole',
                category: userRoleCategory,
                defaultEntity: new ReferenceToEntity()
        )

        roleAttribute = new CategoryAttribute(
                name: 'roleAttribute',
                code: 'roleAttribute',
                dataType: AttributeType.STRING,
                categoryEntityType: 'dynattr$Role',
                category: roleCategory,
                defaultEntity: new ReferenceToEntity()
        )

        user1 = new User(
                name: 'user1',
                login: 'user1'
        )

        user2 = new User(
                name: 'user2',
                login: 'user2'
        )

        role = new Role(name: 'role')

        userRole = new UserRole()

        dataManager.save(
                userCategory,
                userRoleCategory,
                roleCategory,
                userAttribute,
                userRoleAttribute,
                roleAttribute)

        dataManager.save(user1, user2, role, userRole)

        dynamicModelConfiguration.reload()
    }

    def "load user and save with dynamic attributes"() {
        setup:

        def user = dataManager.load(User)
                .id(user1.id)
                .dynamicAttributes(true)
                .one()

        when:

        EntityValues.setValue(user, '+userAttribute', "userName")
        dataManager.save(user)
        user = dataManager.load(User)
                .id(user1.id)
                .dynamicAttributes(true)
                .one()

        then:
        EntityValues.getValue(user, '+userAttribute') == 'userName'
    }
}
