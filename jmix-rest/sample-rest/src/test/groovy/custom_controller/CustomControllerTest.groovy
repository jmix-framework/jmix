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

package custom_controller


import org.springframework.test.context.TestPropertySource
import test_support.RestSpec

import static test_support.RestSpecsUtils.createRequest

@TestPropertySource(properties =
        "jmix.rest.anonymousUrlPatterns=/rest/sample/unprotectedMethod"
)
class CustomControllerTest extends RestSpec {

    def "Unprotected custom controller access"() {
        when:
        def request = createRequest()
        def response = request.with().get("http://localhost:" + port + "/rest/sample/unprotectedMethod")

        then:
        response.statusCode() == 200
        response.body.asString() == 'unprotectedMethod'
    }

    def "Unprotected custom controller access from public path"() {
        when:
        def request = createRequest()
        def response = request.with().get("http://localhost:" + port + "/rest/public/another_sample/unprotectedMethod")

        then:
        response.statusCode() == 200
        response.body.asString() == 'unprotectedMethod'
    }

    def "Protected custom controller access without token"() {
        when:
        def request = createRequest()
        def response = request.with().get("http://localhost:" + port + "/rest/sample/protectedMethod")

        then:
        response.statusCode() == 401
    }

    def "Protected custom controller access with token"() {
        when:
        def request = createRequest(userToken)
        def response = request.with().get("http://localhost:" + port + "/rest/sample/protectedMethod")

        then:
        response.statusCode() == 200
        response.body.asString() == 'protectedMethod'
    }
}
