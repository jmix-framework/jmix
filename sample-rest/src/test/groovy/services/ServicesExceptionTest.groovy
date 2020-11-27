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

package services

import test_support.RestSpec

import static test_support.RestSpecsUtils.createRequest

class ServicesExceptionTest extends RestSpec {

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
