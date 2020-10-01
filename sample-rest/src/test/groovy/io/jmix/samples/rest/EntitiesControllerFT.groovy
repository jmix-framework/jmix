package io.jmix.samples.rest

import static io.jmix.samples.rest.DataUtils.createGroup
import static io.jmix.samples.rest.DataUtils.createUser
import static io.jmix.samples.rest.RestSpecsUtils.createRequest

class EntitiesControllerFT extends DataSpec {

    void prepareDb() {
        UUID groupId = createGroup(dirtyData, sql, "Company")

        createUser(dirtyData, sql, "admin", groupId)
        createUser(dirtyData, sql, "anonymous", groupId)
    }

    def "GET-request with filter for obtaining the count of entities"() {
        def param = [
                'conditions': [
                        [
                                'property': 'login',
                                'operator': 'notEmpty'
                        ]
                ]
        ]

        when:
        def request = createRequest(userToken).param("filter", param)
        def response = request.with().get(baseUrl + "/entities/sec\$User/search/count")

        then:
        response.statusCode() == 200
        response.body.as(Integer) == 2
    }

    def "POST-request with filter for obtaining the count of entities"() {
        def body = [
                'filter': [
                        'conditions': [
                                [
                                        'property': 'login',
                                        'operator': '=',
                                        'value'   : "admin"
                                ]
                        ]
                ]
        ]

        when:
        def request = createRequest(userToken).body(body)
        def response = request.with().post(baseUrl + "/entities/sec\$User/search/count")

        then:
        response.statusCode() == 200
        response.body.as(Integer) == 1
    }
}
