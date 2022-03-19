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

package oauth_token

import io.jmix.core.security.event.UserDisabledEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.core.session.SessionRegistry
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import test_support.RestSpec

import static test_support.RestSpecsUtils.createRequest

class TokenInvalidationTest extends RestSpec {
    @Autowired
    protected SessionRegistry sessionRegistry
    @Autowired
    protected ApplicationEventPublisher eventPublisher

    def "session associated with access token is expired"() {
        when:
        def response = createRequest(userToken)
                .when()
                .get('/userInfo')

        then:
        response.then().statusCode(200)
        response.thenReturn().path('username') == "admin"

        when:
        killSession('admin')

        response = createRequest(userToken)
                .when()
                .get('/userInfo')

        then:
        response.then().statusCode(200)
        response.asString().contains("session has been expired")

        when:

        response = createRequest(userToken)
                .when()
                .get('/userInfo')

        then:
        response.then().statusCode(401)
    }

    def "invalidate token if user is deactivated"() {
        when:
        def response = createRequest(userToken)
                .when()
                .get('/userInfo')

        then:
        response.then().statusCode(200)
        response.thenReturn().path('username') == "admin"

        when:
        disableUser('admin')

        response = createRequest(userToken)
                .when()
                .get('/userInfo')

        then:
        response.then().statusCode(401)
    }

    protected void killSession(String username) {
        User principal = sessionRegistry.getAllPrincipals().stream()
                .filter({ it instanceof User })
                .map({ (User) it })
                .filter({ it.getUsername() == username })
                .findFirst()
                .orElseThrow({ new RuntimeException("Unable to find principal") })

        sessionRegistry.getAllSessions(principal, false)
                .stream()
                .forEach({ it.expireNow() })
    }

    protected void disableUser(String username) {
        UserDetails userDetails = userRepository.loadUserByUsername(username)
        eventPublisher.publishEvent(new UserDisabledEvent(username))
    }
}
