/*
 * Copyright 2021 Haulmont.
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

package user_substitution_provider


import io.jmix.core.UnconstrainedDataManager
import io.jmix.securitydata.entity.UserSubstitutionEntity
import io.jmix.securitydata.impl.DatabaseUserSubstitutionProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import test_support.SecurityDataSpecification

import javax.sql.DataSource
import java.text.SimpleDateFormat

class DatabaseUserSubstitutionProviderTest extends SecurityDataSpecification {

    @Autowired
    DatabaseUserSubstitutionProvider userSubstitutionProvider

    @Autowired
    UnconstrainedDataManager dataManager

    @Autowired
    DataSource dataSource

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd")

    def setup() {
    }

    def cleanup() {
        new JdbcTemplate(dataSource).execute('delete from SEC_USER_SUBSTITUTION')
    }

    def "provider should return substitutions for the current user only"() {

        UserSubstitutionEntity substitution1 = dataManager.create(UserSubstitutionEntity)
        substitution1.username = 'user1'
        substitution1.substitutedUsername = 'user3'
        dataManager.save(substitution1)

        UserSubstitutionEntity substitution2 = dataManager.create(UserSubstitutionEntity)
        substitution2.username = 'user2'
        substitution2.substitutedUsername = 'user3'
        dataManager.save(substitution2)

        when:

        def substitutions = userSubstitutionProvider.getUserSubstitutions("user1", sdf.parse("2021-01-15 09:00"))

        then:

        substitutions.size() == 1
        substitutions[0].username == 'user1'
    }

    def "provider should return substitutions active at the given time"() {

        UserSubstitutionEntity substitution1 = dataManager.create(UserSubstitutionEntity)
        substitution1.username = 'orig1'
        substitution1.substitutedUsername = 'subst'
        substitution1.startDate = sdf.parse("2021-01-01")
        substitution1.endDate = sdf.parse("2021-02-01")
        dataManager.save(substitution1)

        UserSubstitutionEntity substitution2 = dataManager.create(UserSubstitutionEntity)
        substitution2.username = 'orig1'
        substitution2.substitutedUsername = 'subst'
        substitution2.startDate = sdf.parse("2021-01-01")
        substitution2.endDate = sdf.parse("2021-01-10")

        dataManager.save(substitution2)


        when:

        def substitutions = userSubstitutionProvider.getUserSubstitutions("orig1", sdf.parse("2021-01-15 09:00"))

        then:

        substitutions.size() == 1
        substitutions[0].username == 'orig1'
        substitutions[0].endDate == sdf.parse("2021-02-01")
    }

    def "provider should return substitutions without start and end dates"() {

        UserSubstitutionEntity substitution1 = dataManager.create(UserSubstitutionEntity)
        substitution1.username = 'orig1'
        substitution1.substitutedUsername = 'subst'
        dataManager.save(substitution1)

        when:

        def substitutions = userSubstitutionProvider.getUserSubstitutions("orig1", sdf.parse("2021-01-15"))

        then:

        substitutions.size() == 1
        substitutions[0].username == 'orig1'
    }
}
