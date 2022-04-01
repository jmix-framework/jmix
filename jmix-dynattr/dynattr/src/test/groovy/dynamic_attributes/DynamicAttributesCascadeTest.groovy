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


import io.jmix.core.CoreConfiguration
import io.jmix.core.DataManager
import io.jmix.core.FetchPlan
import io.jmix.core.Metadata
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
import test_support.entity.User
import test_support.entity.UserTechInfo

import javax.sql.DataSource

@ContextConfiguration(
        classes = [CoreConfiguration, DataConfiguration, EclipselinkConfiguration,
                DynAttrConfiguration, JmixDynAttrTestConfiguration],
        initializers = [TestContextInititalizer]
)
class DynamicAttributesCascadeTest extends Specification {
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

    protected io.jmix.dynattr.model.Category userCategory

    protected CategoryAttribute userAttribute

    protected User user1

    void setup() {
        userCategory = metadata.create(io.jmix.dynattr.model.Category)
        userCategory.name = 'user'
        userCategory.entityType = 'dynattr$User'

        createUserAttributes()

        user1 = new User(
                name: 'user1',
                login: 'user1'
        )

        dataManager.save(userCategory, userAttribute, user1)

        dynamicModelConfiguration.reload()
    }

    void cleanup() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource)
        jdbcTemplate.update('delete from DYNAT_ATTR_VALUE')
        jdbcTemplate.update('delete from DYNAT_CATEGORY_ATTR')
        jdbcTemplate.update('delete from DYNAT_CATEGORY')
        jdbcTemplate.update('delete from DYNATTR_GROUP')
        jdbcTemplate.update('delete from DYNATTR_USER_TECH_INFO')
        jdbcTemplate.update('delete from DYNATTR_USER')
    }

    protected void createUserAttributes() {
        userAttribute = metadata.create(CategoryAttribute)
        userAttribute.name = 'userAttribute'
        userAttribute.code = 'userAttribute'
        userAttribute.dataType = AttributeType.STRING
        userAttribute.categoryEntityType = 'dynattr$User'
        userAttribute.category = userCategory
        userAttribute.defaultEntity = new ReferenceToEntity()
    }

    def "dynamic attributes for cascade operations"() {
        when:
        String login = 'cascadeDynTestUser'
        def user = metadata.create(User)
        user.login = login

        EntityValues.setValue(user, '+userAttribute', 'userName')

        def info = metadata.create(UserTechInfo)
        info.info = "some additional info"
        info.user = user

        dataManager.save(info)


        def reloaded = dataManager.load(UserTechInfo)
                .id(info.getId())
                .fetchPlan({ builder ->
                    builder.addFetchPlan(FetchPlan.LOCAL)
                            .add("user", FetchPlan.LOCAL)
                })
                .hint(DynAttrQueryHints.LOAD_DYN_ATTR, true)
                .one()

        then:

        reloaded.user.login == login
        EntityValues.getValue(reloaded.user, '+userAttribute') == 'userName'

        when:

        EntityValues.setValue(reloaded.user, '+userAttribute', 'userSecondName')
        reloaded.info = 'newInfo'
        dataManager.save(reloaded)

        reloaded = dataManager.load(UserTechInfo)
                .id(info.getId())
                .fetchPlan({ builder ->
                    builder.addFetchPlan(FetchPlan.LOCAL)
                            .add("user", FetchPlan.LOCAL)
                })
                .hint(DynAttrQueryHints.LOAD_DYN_ATTR, true)
                .one()

        then:
        reloaded.info == 'newInfo'
        reloaded.user.login == login
        EntityValues.getValue(reloaded.user, '+userAttribute') == 'userSecondName'
    }

}
