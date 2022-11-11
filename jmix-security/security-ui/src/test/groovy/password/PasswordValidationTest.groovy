/*
 * Copyright 2022 Haulmont.
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

package password

import io.jmix.securityui.password.PasswordValidation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.User
import test_support.SecurityUiSpecification

class PasswordValidationTest extends SecurityUiSpecification {

    @Autowired
    PasswordValidation passwordValidation

    def "test"() {
        def list

        when:
        list = passwordValidation.validate(new User("bob", "1", []), "1")

        then:
        list == ['Password is too short']

        when:
        list = passwordValidation.validate(new User("bob", "bob", []), "bob")

        then:
        list.size() == 2
        list.contains('Password is too short')
        list.contains('Password must be different from username')

        when:
        list = passwordValidation.validate(new User("bob", "1234", []), "1234")

        then:
        list.isEmpty()
    }
}
