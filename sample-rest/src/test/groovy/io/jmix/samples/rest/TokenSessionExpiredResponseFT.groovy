/*
 * Copyright (c) 2008-2018 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package io.jmix.samples.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.session.SessionRegistry
import org.springframework.security.core.userdetails.User

import static io.jmix.samples.rest.RestSpecsUtils.createRequest

class TokenSessionExpiredResponseFT extends RestSpec {
    @Autowired
    protected SessionRegistry sessionRegistry

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
}
