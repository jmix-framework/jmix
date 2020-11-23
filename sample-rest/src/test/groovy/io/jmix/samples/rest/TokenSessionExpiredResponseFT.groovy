///*
// * Copyright (c) 2008-2018 Haulmont. All rights reserved.
// * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
// */
//
//package io.jmix.samples.rest
//
//import com.haulmont.masquerade.restapi.ServiceGenerator
//import io.jmix.samples.rest.jmx.UserSessionsSupportJmxService
//import io.jmix.samples.rest.service.LdapTokenService
//import io.jmix.samples.rest.service.UserInfoService
//import org.junit.Ignore
//import spock.lang.Specification
//
//import static com.haulmont.masquerade.Connectors.*
//
//@Ignore
//class TokenSessionExpiredResponseFT extends Specification {
//
//    static final PORTAL_REST_API_BASE_URL = 'http://localhost:8080/app-portal/rest/v2/'
//
//    def "PL-10535 Exception when session associated with access token is expired"() {
//        def userSessionsSupportJmx = jmx(UserSessionsSupportJmxService.class)
//
//        def host = new RestApiHost("admin", "admin", PORTAL_REST_API_BASE_URL)
//        def oAuthTokenService = restApiOAuthService(host)
//
//        def token = oAuthTokenService.token(host.user, host.password, host.grantType).execute().body()
//        def userInfoService = restApi(UserInfoService.class, host, token)
//
//        when:
//        def infoBeforeResponse = userInfoService.getUserInfo().execute()
//
//        then:
//        infoBeforeResponse.code() == 200
//        infoBeforeResponse.body().name == "Administrator"
//        infoBeforeResponse.body().login == "admin"
//
//        when:
//        userSessionsSupportJmx.killSessions(['admin'] as String[])
//
//        def infoAfterResponse = userInfoService.getUserInfo().execute()
//
//        then:
//        infoAfterResponse.code() == 200
//        infoAfterResponse.body().name == "Administrator"
//        infoAfterResponse.body().login == "admin"
//
//        cleanup:
//        userSessionsSupportJmx.killSessions(['admin'] as String[])
//    }
//
//    @Ignore
//    def "PL-10540 User session is not re-created after expiration when the user is authenticated with LDAP"() {
//        def userSessionsSupportJmx = jmx(UserSessionsSupportJmxService.class)
//
//        def host = new RestApiHost("admin", "admin", PORTAL_REST_API_BASE_URL)
//        def ldapTokenService = restApiLdapService(host)
//
//        def token = ldapTokenService.token(host.user, host.password, host.grantType).execute().body()
//        def userInfoService = restApi(UserInfoService.class, host, token)
//
//        when:
//        def infoBeforeResponse = userInfoService.getUserInfo().execute()
//
//        then:
//        infoBeforeResponse.code() == 200
//        infoBeforeResponse.body().name == "Administrator"
//        infoBeforeResponse.body().login == "admin"
//
//        when:
//        userSessionsSupportJmx.killSessions(['admin'] as String[])
//
//        def infoAfterResponse = userInfoService.getUserInfo().execute()
//
//        then:
//        infoAfterResponse.code() == 200
//        infoAfterResponse.body().name == "Administrator"
//        infoAfterResponse.body().login == "admin"
//    }
//
//    static LdapTokenService restApiLdapService(RestApiHost hostInfo) {
//        return ServiceGenerator.createService(hostInfo.getBaseUrl(),
//                LdapTokenService.class, hostInfo.getClientId(), hostInfo.getClientSecret())
//    }
//}
