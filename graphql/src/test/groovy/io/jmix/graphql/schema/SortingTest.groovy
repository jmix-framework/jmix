/*
 * Copyright 2021 Haulmont.
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

package io.jmix.graphql.schema

import com.graphql.spring.boot.test.GraphQLTestTemplate
import io.jmix.graphql.AbstractGraphQLTest
import org.springframework.beans.factory.annotation.Autowired

class SortingTest extends AbstractGraphQLTest {

    @Autowired
    GraphQLTestTemplate graphQLTestTemplate

    def "default sorting by lastModifiedDate is enabled without any sorting"() {
        when:
        def response = graphQLTestTemplate.postForResource("graphql/sorting_test/cars-without-sorting.graphql")
        println "response = $response.rawResponse"

        then:
        response.rawResponse.body == '{"data":{"scr_CarList":[' +
                '{"id":"c5a0c22e-a8ce-4c5a-9068-8fb142af26ae","lastModifiedDate":null},' +
                '{"id":"aa595879-484f-4e7d-b19a-429cb2d84f79","lastModifiedDate":"2021-04-06T20:00:00Z"},' +
                '{"id":"7db61cfc-1e50-4898-a76d-42347ffb763f","lastModifiedDate":"2021-03-30T20:00:00Z"},' +
                '{"id":"8561ba7a-49c5-4683-9251-59f376018a89","lastModifiedDate":"2021-03-04T20:00:00Z"},' +
                '{"id":"c7052489-3697-48f6-a0f3-8e874d732865","lastModifiedDate":"2021-02-28T20:00:00Z"},' +
                '{"id":"b94eede4-c1da-43df-830d-36ef1414385b","lastModifiedDate":"2021-02-27T20:00:00Z"},' +
                '{"id":"bc5b3371-7418-4c79-90e8-81b09c59d9a1","lastModifiedDate":"2021-02-20T20:00:00Z"},' +
                '{"id":"6b853033-db8c-4d51-ab4c-4b3146796348","lastModifiedDate":"2021-02-13T20:00:00Z"},' +
                '{"id":"2325c7af-9569-4f66-bcf7-bb52cba5388b","lastModifiedDate":"2021-02-10T20:00:00Z"},' +
                '{"id":"bf6791e6-0e0a-8ca1-6a98-75b0a8971676","lastModifiedDate":"2021-01-30T20:00:00Z"},' +
                '{"id":"5f14d58d-6f24-4590-eef9-4b5885ed3e34","lastModifiedDate":"2021-01-20T20:00:00Z"},' +
                '{"id":"c4ef4c14-5be9-406a-8457-db0bc760913a","lastModifiedDate":"2021-01-17T20:00:00Z"},' +
                '{"id":"c2a14bec-cd7d-a3e4-1581-db243cf704aa","lastModifiedDate":"2021-01-10T20:00:00Z"},' +
                '{"id":"a64e6ef7-49d6-4ce5-8973-8c95ac1576e0","lastModifiedDate":"2021-01-08T20:00:00Z"},' +
                '{"id":"50277e41-97d1-4af2-a122-1e87ae3011d9","lastModifiedDate":"2021-01-05T20:00:00Z"},' +
                '{"id":"3da61043-aaad-7e30-c7f5-c1f1328d3980","lastModifiedDate":"2021-01-02T20:00:00Z"},' +
                '{"id":"63e88502-3cf0-382c-8f5f-07a0c8a4d9b2","lastModifiedDate":"2020-12-31T20:00:00Z"},' +
                '{"id":"fc63ccfc-e8e9-5486-5c38-98ae42f729da","lastModifiedDate":"2020-12-30T20:00:00Z"},' +
                '{"id":"5db1dce7-ceee-42f8-a14b-ddb93c4ad999","lastModifiedDate":"2020-11-30T20:00:00Z"},' +
                '{"id":"f44d486f-2fa3-4789-d02a-c1d2b2c67fc6","lastModifiedDate":"2020-10-31T20:00:00Z"},' +
                '{"id":"73c05bf0-ef67-4291-48a2-1481fc7f17e6","lastModifiedDate":"2020-09-30T20:00:00Z"},' +
                '{"id":"94505084-e12c-44c0-9e55-0ee9ef5f3a90","lastModifiedDate":"2020-06-11T20:00:00Z"}' +
                ']}}'
    }

    def "default sorting by lastModifiedDate is disabled when it's already sorted"() {
        when:
        def response = graphQLTestTemplate.postForResource(
                "graphql/sorting_test/cars-with-sorting-by-manufacturer.graphql"
        )
        println "response = $response.rawResponse"

        then:
        response.rawResponse.body == '{"data":{"scr_CarList":[' +
                '{"id":"73c05bf0-ef67-4291-48a2-1481fc7f17e6","lastModifiedDate":"2020-09-30T20:00:00Z"},' +
                '{"id":"bf6791e6-0e0a-8ca1-6a98-75b0a8971676","lastModifiedDate":"2021-01-30T20:00:00Z"},' +
                '{"id":"63e88502-3cf0-382c-8f5f-07a0c8a4d9b2","lastModifiedDate":"2020-12-31T20:00:00Z"},' +
                '{"id":"2325c7af-9569-4f66-bcf7-bb52cba5388b","lastModifiedDate":"2021-02-10T20:00:00Z"},' +
                '{"id":"50277e41-97d1-4af2-a122-1e87ae3011d9","lastModifiedDate":"2021-01-05T20:00:00Z"},' +
                '{"id":"5db1dce7-ceee-42f8-a14b-ddb93c4ad999","lastModifiedDate":"2020-11-30T20:00:00Z"},' +
                '{"id":"6b853033-db8c-4d51-ab4c-4b3146796348","lastModifiedDate":"2021-02-13T20:00:00Z"},' +
                '{"id":"7db61cfc-1e50-4898-a76d-42347ffb763f","lastModifiedDate":"2021-03-30T20:00:00Z"},' +
                '{"id":"8561ba7a-49c5-4683-9251-59f376018a89","lastModifiedDate":"2021-03-04T20:00:00Z"},' +
                '{"id":"94505084-e12c-44c0-9e55-0ee9ef5f3a90","lastModifiedDate":"2020-06-11T20:00:00Z"},' +
                '{"id":"a64e6ef7-49d6-4ce5-8973-8c95ac1576e0","lastModifiedDate":"2021-01-08T20:00:00Z"},' +
                '{"id":"aa595879-484f-4e7d-b19a-429cb2d84f79","lastModifiedDate":"2021-04-06T20:00:00Z"},' +
                '{"id":"b94eede4-c1da-43df-830d-36ef1414385b","lastModifiedDate":"2021-02-27T20:00:00Z"},' +
                '{"id":"bc5b3371-7418-4c79-90e8-81b09c59d9a1","lastModifiedDate":"2021-02-20T20:00:00Z"},' +
                '{"id":"c4ef4c14-5be9-406a-8457-db0bc760913a","lastModifiedDate":"2021-01-17T20:00:00Z"},' +
                '{"id":"c5a0c22e-a8ce-4c5a-9068-8fb142af26ae","lastModifiedDate":null},' +
                '{"id":"c7052489-3697-48f6-a0f3-8e874d732865","lastModifiedDate":"2021-02-28T20:00:00Z"},' +
                '{"id":"fc63ccfc-e8e9-5486-5c38-98ae42f729da","lastModifiedDate":"2020-12-30T20:00:00Z"},' +
                '{"id":"c2a14bec-cd7d-a3e4-1581-db243cf704aa","lastModifiedDate":"2021-01-10T20:00:00Z"},' +
                '{"id":"f44d486f-2fa3-4789-d02a-c1d2b2c67fc6","lastModifiedDate":"2020-10-31T20:00:00Z"},' +
                '{"id":"3da61043-aaad-7e30-c7f5-c1f1328d3980","lastModifiedDate":"2021-01-02T20:00:00Z"},' +
                '{"id":"5f14d58d-6f24-4590-eef9-4b5885ed3e34","lastModifiedDate":"2021-01-20T20:00:00Z"}' +
                ']}}'
    }

}
