/*
 * Copyright (c) 2008-2018 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package io.jmix.samples.rest


import org.springframework.boot.test.context.SpringBootTest

import static io.jmix.samples.rest.RestSpecsUtils.createRequest

@SpringBootTest(classes = SampleRestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NotFoundUrlFT extends RestSpec {

    def "REST-API returns 404 for incorrect URL"() {
        when:
        def request = createRequest(userToken)
        def response = request.with().get("/not-found-error-url")

        then:
        response.statusCode() == 404
    }

    def "REST-API returns 404 for incorrect URL if unauthorized"() {
        when:
        def request = createRequest()
        def response = request.with().get("/not-found-error-url")

        then:
        response.statusCode() == 404
    }
}
