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

import io.jmix.graphql.AbstractGraphQLTest
import org.springframework.test.annotation.DirtiesContext
import test_support.entity.CarType

@DirtiesContext
class SecurityTest extends AbstractGraphQLTest {

    def 'Modifying attributes is forbidden for scr$Garage with mechanic role'() {
        def id = "d99d468e-3cc0-01da-295e-595e48fec620"
        when:
        def response = query(
                "datafetcher/upsert-garage.gql",
                asObjectNode('{"garage": {' +
                        '"id":"' + id +'",' +
                        '"name": "created garage"}}'),
                mechanicToken
        )
        def error = getErrors(response)[0].getAsJsonObject()

        then:
        getMessage(error) == "Exception while fetching data (/upsert_scr_Garage) : Modifying attributes is forbidden [name]"
    }

    def 'Creating scr$Garage entity is forbidden for scr$Garage with mechanic role'() {
        def id = "bfe41616-f03d-f287-1397-8619f5dde390"
        when:
        def response = query(
                "datafetcher/upsert-garage.gql",
                asObjectNode('{"garage": {' +
                        '"id":"' + id +'"}}'),
                mechanicToken
        )

        def error = getErrors(response)[0].getAsJsonObject()

        then:
        getMessage(error) == "Exception while fetching data (/upsert_scr_Garage) : Can't save entity to database. Access denied"
    }

    def 'Deletion scr$Garage entity is forbidden for scr$Garage with mechanic role'() {
        def id = "d99d468e-3cc0-01da-295e-595e48fec620"
        when:
        def response = query(
                "datafetcher/delete-garage.gql",
                asObjectNode('{"id":"' + id +'"}}'),
                mechanicToken
        )

        def error = getErrors(response)[0].getAsJsonObject()

        then:
        getMessage(error) == "Exception while fetching data (/delete_scr_Garage) : Deletion of the scr\$Garage is forbidden"
    }

    def 'Modifying attributes is available for scr$Car with mechanic role'() {
        def id = "3da61043-aaad-7e30-c7f5-c1f1328d3980"
        when:
        def response = query(
                "datafetcher/upsert-car.gql",
                asObjectNode('{"car": {' +
                        '"id": "' + id + '",' +
                        '"manufacturer":"TESLA",' +
                        '"model": "Z",' +
                        '"carType":"' + CarType.SEDAN + '"' +
                        '}}'),
                mechanicToken
        )

        then:
        getBody(response) == '{"data":{"upsert_scr_Car":{' +
                '"id":"3da61043-aaad-7e30-c7f5-c1f1328d3980",' +
                '"purchaseDate":null,' +
                '"maxPassengers":null,' +
                '"garage":null' +
                '}}}'
    }

    def 'Creating is available for scr$Car with mechanic role'() {
        def id = "265f1282-b36b-48f2-80ab-cb22e0b75bbc"
        when:
        def response = query(
                "datafetcher/upsert-car.gql",
                asObjectNode('{"car": {' +
                        '"id": "' + id + '",' +
                        '"manufacturer":"TESLA",' +
                        '"model": "Z",' +
                        '"carType":"' + CarType.SEDAN + '"' +
                        '}}'),
                mechanicToken
        )

        then:
        getBody(response) == '{"data":{"upsert_scr_Car":{' +
                '"id":"265f1282-b36b-48f2-80ab-cb22e0b75bbc",' +
                '"purchaseDate":null,' +
                '"maxPassengers":null,' +
                '"garage":null' +
                '}}}'
    }

    def 'Deletion is available for scr$Car with mechanic role'() {
        def id = "265f1282-b36b-48f2-80ab-cb22e0b75bbc"
        when:
        def response = query(
                "datafetcher/delete-car.gql",
                asObjectNode('{"id": "' + id + '"}}'),
                mechanicToken
        )

        then:
        getBody(response) == '{"data":{"delete_scr_Car":null}}'
    }

}
