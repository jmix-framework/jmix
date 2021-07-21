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

package io.jmix.graphql.datafetcher.filter


import io.jmix.graphql.AbstractGraphQLTest
import test_support.entity.Car
import test_support.entity.CarType

class EnumFilterTest extends AbstractGraphQLTest {

    def "_eq for car.carType"() {
        when:
        List<Car> cars = queryCars('{"filter": {"carType": {"_eq": "SEDAN"}}}')
        then:
        cars.size() > 0
        cars.stream().allMatch(car -> car.carType == CarType.SEDAN)
    }

    def "_neq for car.carType"() {
        when:
        List<Car> cars = queryCars('{"filter": {"carType": {"_neq": "SEDAN"}}}')
        then:
        cars.size() > 0
        cars.stream().allMatch(car -> car.carType == CarType.HATCHBACK)
    }

    def "_in for car.carType"() {
        when:
        List<Car> carsAll = queryCars('{"filter": {"carType": {"_in": ["SEDAN", "HATCHBACK"]}}}')
        List<Car> carsInSedan = queryCars('{"filter": {"carType": {"_in": ["SEDAN"]}}}')
        List<Car> noCars = queryCars('{"filter": {"carType": {"_in": []}}}')
        then:
        carsAll.stream().anyMatch(car -> car.carType == CarType.HATCHBACK)
        carsAll.stream().anyMatch(car -> car.carType == CarType.SEDAN)

        carsInSedan.size() > 0
        carsInSedan.stream().allMatch(car -> car.carType == CarType.SEDAN)

        // todo '_in' should not returns cars with SEDAN and HATCHBACK car type when condition array is empty
        // https://github.com/Haulmont/jmix-core/issues/136
        // noCars.size() == 0
        noCars.size() == 22
    }

    def "_notIn for car.carType"() {
        when:
        List<Car> carsAll = queryCars('{"filter": {"carType": {"_notIn": []}}}')
        List<Car> carsSedan = queryCars('{"filter": {"carType": {"_notIn": ["HATCHBACK"]}}}')
        List<Car> noCars = queryCars('{"filter": {"carType": {"_notIn": ["SEDAN", "HATCHBACK"]}}}')
        then:
        carsAll.stream().anyMatch(car -> car.carType == CarType.HATCHBACK)
        carsAll.stream().anyMatch(car -> car.carType == CarType.SEDAN)

        carsSedan.size() > 0
        carsSedan.stream().allMatch(car -> car.carType == CarType.SEDAN)

         noCars.size() == 0
    }

    def "_isNull for car.ecoRank"() {
        when:
        List<Car> carsNullRank = queryCars('{"filter": {"ecoRank": {"_isNull": "true"}}}')
        List<Car> carsNotNullRank = queryCars('{"filter": {"ecoRank": {"_isNull": "false"}}}')
        then:
        carsNotNullRank.size() == 0
        carsNullRank.stream().allMatch(car -> car.ecoRank == null)
    }

    def "_isNull for car.carType"() {
        when:
        List<Car> carsNullCarType = queryCars('{"filter": {"carType": {"_isNull": "true"}}}')
        List<Car> carsNotNullCarType = queryCars('{"filter": {"carType": {"_isNull": "false"}}}')
        then:
        carsNullCarType.size() == 0
        carsNotNullCarType.stream().allMatch(car -> car.carType != null)
    }

    def "AND for car.carType"() {
        when:
        List<Car> noCars = queryCars('{"filter": {"carType": {"AND": [{"_eq": "SEDAN"}, {"_eq": "HATCHBACK"}]}}}')
        then:
        noCars.size() == 0
    }

    def "OR for car.carType"() {
        when:
        List<Car> allCars = queryCars('{"filter": {"carType": {"OR": [{"_eq": "SEDAN"}, {"_eq": "HATCHBACK"}]}}}')
        then:
        allCars.stream().anyMatch(car -> car.carType == CarType.HATCHBACK)
        allCars.stream().anyMatch(car -> car.carType == CarType.SEDAN)
    }

}
