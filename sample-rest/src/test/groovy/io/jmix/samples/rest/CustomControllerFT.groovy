package io.jmix.samples.rest


import org.springframework.test.context.TestPropertySource

import static io.jmix.samples.rest.RestSpecsUtils.createRequest

@TestPropertySource(properties =
        "jmix.rest.authenticatedUrlPatterns=/myapi/sample/protectedMethod"
)
class CustomControllerFT extends RestSpec {

    def "Unprotected custom controller access"() {
        when:
        def request = createRequest()
        def response = request.with().get("http://localhost:" + port + "/myapi/sample/unprotectedMethod")

        then:
        response.statusCode() == 200
        response.body.asString() == 'unprotectedMethod'
    }

    def "Protected custom controller access without token"() {
        when:
        def request = createRequest()
        def response = request.with().get("http://localhost:" + port + "/myapi/sample/protectedMethod")

        then:
        response.statusCode() == 401
    }

    def "Protected custom controller access with token"() {
        when:
        def request = createRequest(userToken)
        def response = request.with().get("http://localhost:" + port + "/myapi/sample/protectedMethod")

        then:
        response.statusCode() == 200
        response.body.asString() == 'protectedMethod'
    }
}
