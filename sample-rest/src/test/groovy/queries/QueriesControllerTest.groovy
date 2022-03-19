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

package queries


import io.jmix.samples.rest.entity.driver.DriverStatus
import test_support.RestSpec

import static test_support.DataUtils.createDriver
import static test_support.RestSpecsUtils.createRequest

class QueriesControllerTest extends RestSpec {

    private UUID driver1Id
    private UUID driver2Id

    void prepareDb() {
        driver1Id = createDriver(dirtyData, sql, "Bob", DriverStatus.ACTIVE)
        driver2Id = createDriver(dirtyData, sql, "John", DriverStatus.RETIRED)
    }

    def "Execute a query with an enumeration parameter"() {

        when:
        def request = createRequest(userToken).param('status', 'ACTIVE')
        def response = request.with().get(baseUrl + "/queries/ref\$Driver/getDriversByStatus")

        then:
        response.statusCode() == 200
        response.jsonPath().getList("").size() == 1

        def driver = response.jsonPath().getList("").first()

        driver['id'] == driver1Id.toString()
        driver['name'] == 'Bob'
        driver['status'] == 'ACTIVE'
    }
}
