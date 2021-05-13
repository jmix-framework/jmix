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

package io.jmix.graphql.datafetcher

import io.jmix.core.DataManager
import io.jmix.core.Metadata
import io.jmix.graphql.AbstractGraphQLTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.DirtiesContext

@DirtiesContext
class MutationTest extends AbstractGraphQLTest {

    @Autowired
    DataManager dataManager;
    @Autowired
    Metadata metadata;

    def id1 = "4c34985b-67be-4788-891a-839d479bf9e6"
    def id2 = "0263a715-5fd4-4622-9b01-27daeeb9c357"

    def "should create new Car instance and return additional fetched attributes and _instanceName"() {
        when:
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/upsert-car-return-instance-name.gql", asObjectNode("{\"id\":\"$id1\"}"))
        then:
        response.get('$.data.upsert_scr_Car._instanceName') == "TESLA - Z"
        response.get('$.data.upsert_scr_Car.garage') == null
        response.get('$.data.upsert_scr_Car.maxPassengers') == null
    }

    def "should create new Car instance and return additional fetched attributes without _instanceName"() {
        when:
        def response = graphQLTestTemplate.perform(
                "graphql/io/jmix/graphql/datafetcher/upsert-car.gql", asObjectNode("{\"id\":\"$id2\"}"))
        then:
        response.get('$.data.upsert_scr_Car.garage') == null
        response.get('$.data.upsert_scr_Car.maxPassengers') == null
        response.get('$.data.upsert_scr_Car.purchaseDate') == null
    }

}
