/*
 * Copyright (c) 2008-2018 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package io.jmix.samples.rest


import org.junit.Ignore
import spock.lang.Specification

@Ignore
class RestUserSessionLogFT extends Specification {
    static final PORTAL_REST_API_BASE_URL = 'http://localhost:8080/app-portal/rest/v2/'

    def "UserSessionLog registers login / logout in REST-API"() {

        //def userSessionLogSupportJmx = jmx(UserSessionLogService.class)
        def userSessionLogSupportJmx

        userSessionLogSupportJmx.changeUserSessionLogEnabled(true)

        def host = null //new RestApiHost("admin", "admin", PORTAL_REST_API_BASE_URL)


        when:
        //def token = oAuthTokenService.token(host.user, host.password, host.grantType).execute().body()
        def sessionId = userSessionLogSupportJmx.findLastLoggedSessionId()

        then:
        userSessionLogSupportJmx.getLastLoggedSessionAction(sessionId) == "LOGIN"

        when:
        def revokeResult = null //revoke token
                //oAuthTokenService.revoke(token.accessToken, null).execute()

        then:
        revokeResult.code() == 200
        userSessionLogSupportJmx.getLastLoggedSessionAction(sessionId) == "LOGOUT"

        cleanup:
        userSessionLogSupportJmx.changeUserSessionLogEnabled(false)
        userSessionLogSupportJmx.cleanupLogEntries()
    }
}
