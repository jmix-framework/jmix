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

import io.jmix.core.Metadata
import io.jmix.security.user.PasswordChangeRequiredSupport
import org.springframework.beans.factory.annotation.Autowired
import test_support.SecuritySpecification
import test_support.entity.TestUserWithFlag
import test_support.entity.TestUserWithTwoFlags
import test_support.entity.TestUserWithoutFlag

class PasswordChangeRequiredSupportTest extends SecuritySpecification {

    @Autowired
    PasswordChangeRequiredSupport support

    @Autowired
    Metadata metadata

    def "flag property is found on entity annotated with @PasswordChangeRequired"() {
        when:
        def property = support.findFlagProperty(TestUserWithFlag)

        then:
        property != null
        property.name == 'changePasswordAtNextLogon'
    }

    def "flag property is not found on entity without @PasswordChangeRequired"() {
        expect:
        support.findFlagProperty(TestUserWithoutFlag) == null
    }

    def "isPasswordChangeRequired returns false for entity without the flag field"() {
        given:
        def user = metadata.create(TestUserWithoutFlag)

        expect:
        !support.isPasswordChangeRequired(user)
    }

    def "isPasswordChangeRequired reflects the value of the annotated field"() {
        given:
        def user = metadata.create(TestUserWithFlag)

        expect: 'default value is false'
        !support.isPasswordChangeRequired(user)

        when:
        user.setChangePasswordAtNextLogon(true)

        then:
        support.isPasswordChangeRequired(user)
    }

    def "isPasswordChangeRequired returns false for null user"() {
        expect:
        !support.isPasswordChangeRequired(null)
    }

    def "isPasswordChangeRequired returns false for non-entity object"() {
        expect:
        !support.isPasswordChangeRequired('not an entity')
    }

    def "setPasswordChangeRequired updates the field"() {
        given:
        def user = metadata.create(TestUserWithFlag)
        user.setChangePasswordAtNextLogon(true)

        when:
        support.setPasswordChangeRequired(user, false)

        then:
        user.changePasswordAtNextLogon == false

        when:
        support.setPasswordChangeRequired(user, true)

        then:
        user.changePasswordAtNextLogon == true
    }

    def "setPasswordChangeRequired is a no-op for entity without the flag field"() {
        given:
        def user = metadata.create(TestUserWithoutFlag)

        when:
        support.setPasswordChangeRequired(user, true)

        then:
        noExceptionThrown()
    }

    def "findFlagProperty fails with a clear error when more than one field is annotated"() {
        when:
        support.findFlagProperty(TestUserWithTwoFlags)

        then:
        def ex = thrown(IllegalStateException)
        ex.message.contains(TestUserWithTwoFlags.name)
        ex.message.contains('flagOne')
        ex.message.contains('flagTwo')
    }
}
