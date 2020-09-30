package io.jmix.samples.rest


import static io.jmix.samples.rest.RestSpecsUtils.createRequest

class ServicesExceptionFT extends DataSpec {

    def "GET-request for the call service with a custom exception"() {
        when:
        def response = createRequest(userToken)
                .when()
                .pathParam('serviceName', 'jmix_RestTestService')
                .pathParam('methodName', 'methodWithCustomException')
                .get(baseUrl + '/services/{serviceName}/{methodName}')

        then:
        response.statusCode() == 418
        response.path('error') == "I'm a teapot"
        response.path('details') == 'Server is not a coffee machine'
    }

    def "GET-request for the call service with a exception"() {
        when:
        def response = createRequest(userToken)
                .when()
                .pathParam('serviceName', 'jmix_RestTestService')
                .pathParam('methodName', 'methodWithException')
                .get(baseUrl + '/services/{serviceName}/{methodName}')

        then:
        response.statusCode() == 500
        response.path('error') == "Server error"
        response.path('details') == ''
    }
}
