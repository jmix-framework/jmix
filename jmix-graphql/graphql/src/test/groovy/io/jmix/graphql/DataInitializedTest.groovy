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

package io.jmix.graphql

import io.jmix.core.UnconstrainedDataManager
import org.springframework.beans.factory.annotation.Autowired
import test_support.entity.Car
import test_support.entity.CarType
import test_support.entity.Garage

class DataInitializedTest extends AbstractGraphQLTest {

    @Autowired
    private UnconstrainedDataManager dataManager

    def "data is initialized"() {
        given:
        def car
        def garage

        when:
        car = dataManager.load(Car)
                .id(UUID.fromString("3da61043-aaad-7e30-c7f5-c1f1328d3980"))
                .fetchPlanProperties("manufacturer", "model", "regNumber", "carType", "garage")
                .one()
        garage = dataManager.load(Garage)
                .id(UUID.fromString("d99d468e-3cc0-01da-295e-595e48fec620"))
                .one()

        then:
        car.manufacturer == "VAZ"
        car.model == "2121"
        car.regNumber == "ab345"
        car.carType == CarType.SEDAN
        car.garage == garage
    }
}
