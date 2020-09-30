package io.jmix.samples.rest


import io.jmix.samples.rest.entity.driver.DriverStatus

import static io.jmix.samples.rest.DataUtils.createDriver
import static io.jmix.samples.rest.RestSpecsUtils.createRequest

class QueriesControllerFT extends DataSpec {

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
