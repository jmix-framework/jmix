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

package io.jmix.graphql.datafetcher.sort

import io.jmix.graphql.AbstractGraphQLTest
import test_support.entity.Car
import test_support.entity.CarType
import test_support.entity.Garage

import java.text.SimpleDateFormat
import java.util.stream.Collectors

class SortingTest extends AbstractGraphQLTest {

    def "cars are sorted by enum field - carType"() {
        when:
        def response = query("datafetcher/sort/cars-with-sort.gql",
                asObjectNode('{"orderBy": {"carType": "ASC" }}'))
        then:
        List<Car> cars = response.getList('$.data.scr_CarList', Car)
        for (int i = 0; i < cars.size(); i++) {
            CarType expected = i < 5 ? CarType.HATCHBACK : CarType.SEDAN
            assert cars[i].carType == expected
        }
    }

    def "default sorting by lastModifiedDate is enabled without any sorting"() {
        when:
        def response = query("datafetcher/sort/cars-with-sort.gql",
                asObjectNode('{"orderBy": null}'))
        then:
        List<Car> cars = response.getList('$.data.scr_CarList', Car)
        def dates = [null,
                     "2021-04-07",
                     "2021-03-31",
                     "2021-03-05",
                     "2021-03-01",
                     "2021-02-28",
                     "2021-02-21",
                     "2021-02-14",
                     "2021-02-11",
                     "2021-01-31",
                     "2021-01-21",
                     "2021-01-18",
                     "2021-01-11",
                     "2021-01-09",
                     "2021-01-06",
                     "2021-01-03",
                     "2021-01-01",
                     "2020-12-31",
                     "2020-12-01",
                     "2020-11-01",
                     "2020-10-01",
                     "2020-06-12"]

        cars.get(0).lastModifiedDate == null;
        for (int i = 1; i < cars.size(); i++) {
            assert new SimpleDateFormat("yyyy-MM-dd").format(cars.get(i).lastModifiedDate) == dates[i];
        }
    }

    def "cars are sorted by nested object property - garage name"() {
        when:
        def responseAsc = query("datafetcher/sort/cars-with-sort.gql",
                asObjectNode('{"orderBy": {"garage": {"name": "ASC"}}}'))
        def responseDesc = query("datafetcher/sort/cars-with-sort.gql",
                asObjectNode('{"orderBy": {"garage": {"name": "DESC"}}}'))

        List<String> garNameAsc = responseAsc.getList('$.data.scr_CarList', Car).stream()
                .filter(car -> car.garage != null)
                .map(car -> car.garage.name)
                .collect(Collectors.toList())
        List<String> garNameDesc = responseDesc.getList('$.data.scr_CarList', Car).stream()
                .filter(car -> car.garage != null)
                .map(car -> car.garage.name)
                .collect(Collectors.toList())

        then:
        garNameAsc == ["Big Bob's Beeper Emporium", "P.S. 118", "The Fudge Place", "Watch Repair"]
        garNameDesc == ["Watch Repair", "The Fudge Place", "P.S. 118", "Big Bob's Beeper Emporium"]
    }

    def "garages are sorted by capacity"() {
        when:
        def response = query("datafetcher/sort/garages-with-sort.gql",
                asObjectNode('{"orderBy": {"capacity": "ASC"}}'))
        then:
        List<Garage> garages = response.getList('$.data.scr_GarageList', Garage)
        def capacities = garages.stream().map(gar -> gar.getCapacity()).collect(Collectors.toList());
        capacities == [7, 9, 20, 20, 21, 50, 50, 56, 63, 71]
    }

    def "cars are sorted by garage"() {
        when:
        def response = query("datafetcher/sort/cars-with-sort.gql",
                asObjectNode('{"orderBy": {"garage": {"_instanceName": "ASC"}}}'))

        List<Car> cars = response.getList('$.data.scr_CarList', Car)

        List<String> garNames = cars.stream()
                .filter(car -> car.garage != null)
                .map(car -> car.garage.name)
                .collect(Collectors.toList())

        then:
        garNames == ["Big Bob's Beeper Emporium", "P.S. 118", "The Fudge Place", "Watch Repair"]
    }

    def "cars are sorted by instance name"() {
        when:
        def response = query("datafetcher/sort/cars-with-sort.gql",
                asObjectNode('{"orderBy": {"_instanceName": "ASC"}}'))

        List<Car> cars = response.getList('$.data.scr_CarList', Car)

        List<String> manufacturers = cars.stream()
                .map(car -> car.manufacturer)
                .distinct()
                .collect(Collectors.toList())

        then:
        manufacturers == ["Acura", "Audi", "BMW", "GAZ", "Mercedes", "Porsche", "Tesla", "VAZ", "ZAZ"]
    }

}
