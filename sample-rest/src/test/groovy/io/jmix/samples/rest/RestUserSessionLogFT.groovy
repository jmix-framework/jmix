/*
 * Copyright (c) 2008-2018 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package io.jmix.samples.rest

import io.jmix.samples.rest.jmx.UserSessionLogService
import org.junit.Ignore
import spock.lang.Specification

import static com.haulmont.masquerade.Connectors.*

@Ignore
class RestUserSessionLogFT extends Specification {
    static final PORTAL_REST_API_BASE_URL = 'http://localhost:8080/app-portal/rest/v2/'

    def "UserSessionLog registers login / logout in REST-API"() {
        def userSessionLogSupportJmx = jmx(UserSessionLogService.class)

        userSessionLogSupportJmx.changeUserSessionLogEnabled(true)

        def host = new RestApiHost("admin", "admin", PORTAL_REST_API_BASE_URL)
        def oAuthTokenService = restApiOAuthService(host)

        when:
        def token = oAuthTokenService.token(host.user, host.password, host.grantType).execute().body()
        def sessionId = userSessionLogSupportJmx.findLastLoggedSessionId()

        then:
        userSessionLogSupportJmx.getLastLoggedSessionAction(sessionId) == "LOGIN"

        when:
        def revokeResult = oAuthTokenService.revoke(token.accessToken, null).execute()

        then:
        revokeResult.code() == 200
        userSessionLogSupportJmx.getLastLoggedSessionAction(sessionId) == "LOGOUT"

        cleanup:
        userSessionLogSupportJmx.changeUserSessionLogEnabled(false)
        userSessionLogSupportJmx.cleanupLogEntries()
    }
}
