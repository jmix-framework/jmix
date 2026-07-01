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

package password_change_required

import io.jmix.core.CoreConfiguration
import io.jmix.core.UnconstrainedDataManager
import io.jmix.core.security.PasswordNotMatchException
import io.jmix.core.security.UserManager
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.security.SecurityConfiguration
import io.jmix.securitydata.SecurityDataConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ContextConfiguration
import password_change_required.test_support.PasswordChangeRequiredTestConfiguration
import spock.lang.Specification
import test_support.TestContextInititalizer
import test_support.entity.TestUser

@ContextConfiguration(
        classes = [CoreConfiguration, DataConfiguration, EclipselinkConfiguration,
                SecurityConfiguration, SecurityDataConfiguration, PasswordChangeRequiredTestConfiguration],
        initializers = [TestContextInititalizer]
)
class PasswordChangeRequiredTest extends Specification {

    @Autowired
    UserManager userManager
    @Autowired
    UnconstrainedDataManager dataManager
    @Autowired
    PasswordEncoder passwordEncoder
    @Autowired
    JdbcTemplate jdbcTemplate

    def cleanup() {
        jdbcTemplate.update('delete from TEST_USER')
    }

    private TestUser createUser(String username, String rawPassword, boolean changePasswordFlag) {
        def user = dataManager.create(TestUser)
        user.username = username
        user.password = passwordEncoder.encode(rawPassword)
        user.changePasswordAtNextLogon = changePasswordFlag
        return dataManager.save(user)
    }

    private TestUser reload(String username) {
        return dataManager.load(TestUser).query('e.username = :u').parameter('u', username).one()
    }

    def "changePassword resets the changePasswordAtNextLogon flag"() {
        given:
        createUser('john', 'oldPwd', true)

        when:
        userManager.changePassword('john', 'oldPwd', 'newPwd')

        then:
        def reloaded = reload('john')
        reloaded.changePasswordAtNextLogon == false
        passwordEncoder.matches('newPwd', reloaded.password)
    }

    def "changePassword in-memory updates the flag when saveChanges is false"() {
        given:
        createUser('alice', 'oldPwd', true)

        when:
        def result = userManager.changePassword('alice', 'oldPwd', 'newPwd', false) as TestUser

        then:
        result.changePasswordAtNextLogon == false

        and: 'database still has the original flag value because changes were not saved'
        reload('alice').changePasswordAtNextLogon == true
    }

    def "changePassword keeps the flag when PasswordNotMatchException is thrown"() {
        given:
        createUser('mary', 'samePwd', true)

        when:
        userManager.changePassword('mary', 'samePwd', 'samePwd')

        then:
        thrown(PasswordNotMatchException)

        and:
        reload('mary').changePasswordAtNextLogon == true
    }

    def "resetPasswords with requireChangeAtNextLogon=true sets the flag to true"() {
        given:
        def user = createUser('bob', 'oldPwd', false)

        when:
        def passwords = userManager.resetPasswords([user] as Set, true, true)

        then:
        passwords.size() == 1
        def reloaded = reload('bob')
        reloaded.changePasswordAtNextLogon == true
        !passwordEncoder.matches('oldPwd', reloaded.password)
    }

    def "resetPasswords with requireChangeAtNextLogon=false keeps the flag false"() {
        given:
        def user = createUser('eve', 'oldPwd', false)

        when:
        userManager.resetPasswords([user] as Set, true, false)

        then:
        reload('eve').changePasswordAtNextLogon == false
    }

    def "default resetPasswords overload defaults to requireChangeAtNextLogon=true"() {
        given:
        def user = createUser('carol', 'oldPwd', false)

        when:
        userManager.resetPasswords([user] as Set)

        then:
        reload('carol').changePasswordAtNextLogon == true
    }
}
