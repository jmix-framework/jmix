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

import io.jmix.core.CoreConfiguration
import io.jmix.core.usersubstitution.InMemoryUserSubstitutionProvider
import io.jmix.core.usersubstitution.UserSubstitution
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.base.TestBaseConfiguration

import java.text.SimpleDateFormat

@ContextConfiguration(classes = [CoreConfiguration, TestBaseConfiguration])
class InMemoryUserSubstitutionProviderTest extends Specification {

    @Autowired
    InMemoryUserSubstitutionProvider userSubstitutionProvider

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm")

    def setup() {
        userSubstitutionProvider.clear()
    }

    def "provider should return substitutions for the current user only"() {

        userSubstitutionProvider.addUserSubstitution(new UserSubstitution("orig1",
                "subst",
                sdf.parse("2021-01-01 09:00"),
                sdf.parse("2021-02-01 10:00")
        ))

        userSubstitutionProvider.addUserSubstitution(new UserSubstitution("orig2",
                "subst",
                sdf.parse("2021-01-01 09:00"),
                sdf.parse("2021-02-01 10:00")
        ))

        when:

        def substitutions = userSubstitutionProvider.getUserSubstitutions("orig1", sdf.parse("2021-01-15 09:00"))

        then:

        substitutions.size() == 1
        substitutions[0].username == 'orig1'
    }

    def "provider should return substitutions active at the given time"() {

        userSubstitutionProvider.addUserSubstitution(new UserSubstitution("orig1",
                "subst",
                sdf.parse("2021-01-01 09:00"),
                sdf.parse("2021-02-01 10:00")
        ))

        userSubstitutionProvider.addUserSubstitution(new UserSubstitution("orig1",
                "subst",
                sdf.parse("2021-01-01 09:00"),
                sdf.parse("2021-01-10 10:00")
        ))

        when:

        def substitutions = userSubstitutionProvider.getUserSubstitutions("orig1", sdf.parse("2021-01-15 09:00"))

        then:

        substitutions.size() == 1
        substitutions[0].username == 'orig1'
        substitutions[0].endDate == sdf.parse("2021-02-01 10:00")
    }

    def "provider should return substitutions without start and end dates"() {

        userSubstitutionProvider.addUserSubstitution(new UserSubstitution("orig1",
                "subst"
        ))

        when:

        def substitutions = userSubstitutionProvider.getUserSubstitutions("orig1", sdf.parse("2021-01-15 09:00"))

        then:

        substitutions.size() == 1
        substitutions[0].username == 'orig1'
    }
}
