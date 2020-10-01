/*
 * Copyright (c) 2008-2018 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package io.jmix.samples.rest

import com.haulmont.masquerade.restapi.ServiceGenerator
import io.jmix.core.security.UserRepository
import io.jmix.core.security.impl.CoreUser
import io.jmix.samples.rest.service.NotFoundUrlService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import spock.lang.Specification

import static com.haulmont.masquerade.Connectors.RestApiHost
import static com.haulmont.masquerade.Connectors.restApi

@SpringBootTest(classes = SampleRestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NotFoundUrlFT extends Specification {

    @LocalServerPort
    private int port

    @Autowired
    UserRepository userRepository

    CoreUser admin

    private String baseUrl

    void setup() {
        admin = new CoreUser('admin', '{noop}admin123', 'Admin')
        userRepository.createUser(admin)
        baseUrl = "http://localhost:" + port + "/rest/"
    }

    void cleanup() {
        userRepository.removeUser(admin)
    }

    def "REST-API returns 404 for incorrect URL"() {
        def host = new RestApiHost("admin", "admin123", baseUrl)
        def notFoundUrlService = restApi(NotFoundUrlService.class, host)

        when:
        def notFoundResponse = notFoundUrlService.notFound().execute()

        then:
        notFoundResponse.code() == 404
    }

    def "REST-API returns 404 for incorrect URL if unauthorized"() {
        def unauthorizedService = ServiceGenerator.createService(baseUrl, NotFoundUrlService.class)

        when:
        def unauthorizedResponse = unauthorizedService.notFound().execute()

        then:
        unauthorizedResponse.code() == 404
    }
}
