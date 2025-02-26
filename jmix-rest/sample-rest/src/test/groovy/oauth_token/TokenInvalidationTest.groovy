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
import io.restassured.filter.session.SessionFilter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.core.session.SessionRegistry
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import spock.lang.Ignore
import test_support.RestSpec

import static test_support.RestSpecsUtils.createRequest


class TokenInvalidationTest extends RestSpec {
    private static final Logger log = LoggerFactory.getLogger(TokenInvalidationTest);

    @Autowired
    protected SessionRegistry sessionRegistry
    @Autowired
    protected ApplicationEventPublisher eventPublisher

    @Ignore //todo [jmix-framework/jmix#4160]
    def "session associated with access token is expired"() {
        setup:
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
        UserDetails principal = sessionRegistry.getAllPrincipals().stream()
                .filter({ it instanceof UserDetails })
                .map({ (UserDetails) it })
                .filter({ it.getUsername() == username })
                .findFirst()
                .orElseThrow({ new RuntimeException("Unable to find principal") })

        sessionRegistry.getAllSessions(principal, false)
                .stream()
                .forEach({
                    log.info("Found session ${it.getSessionId()}[expired:${it.isExpired()}] for user '$username'")
                    it.expireNow()
                    log.info("Session ${it.sessionId} 'expired' set to true")
                })
    }

    protected void disableUser(String username) {
        eventPublisher.publishEvent(new UserDisabledEvent(username))
    }
}
