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

package not_found_url

import io.jmix.samples.rest.SampleRestApplication
import org.springframework.boot.test.context.SpringBootTest
import test_support.RestSpec

import static test_support.RestSpecsUtils.createRequest

@SpringBootTest(classes = SampleRestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NotFoundUrlFT extends RestSpec {

    def "REST-API returns 404 for incorrect URL"() {
        when:
        def request = createRequest(userToken)
        def response = request.with().get("/not-found-error-url")

        then:
        response.statusCode() == 404
    }

    def "REST-API returns 401 for incorrect URL if unauthorized"() {
        when:
        def request = createRequest()
        def response = request.with().get("/not-found-error-url")

        then:
        response.statusCode() == 401
    }
}
