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

package user_session_log

import spock.lang.Ignore
import spock.lang.Specification

@Ignore
class RestUserSessionLogTest extends Specification {
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
