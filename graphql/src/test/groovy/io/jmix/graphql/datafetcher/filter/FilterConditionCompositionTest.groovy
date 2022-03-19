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

class FilterConditionCompositionTest extends AbstractGraphQLTest {

    def "filter conditions union - default (AND)"() {
        when:
        List<Car> cars = queryCars('{"filter": [' +
                '  {"price": {"_lte": "30"}},' +
                '  {"price": {"_isNull": false}}' +
                ']}')
        then:
        cars.size() > 0
        cars.stream().allMatch(car -> car.price <= 30)
    }

    def "filter conditions union by AND"() {
        when:
        List<Car> cars = queryCars('{"filter": {"AND": [' +
                '  {"price": {"_lte": "30"}},' +
                '  {"price": {"_isNull": false}}' +
                ']}}')
        then:
        cars.size() > 0
        cars.stream().allMatch(car -> car.price <= 30)
    }

    def "filter conditions union by OR"() {
        when:
        List<Car> cars = queryCars('{"filter": {"OR": [' +
                '  {"price": {"_lte": "10"}},' +
                '  {"price": {"_gt": "40"}}' +
                ']}}')
        then:
        cars.size() > 0
        cars.stream().allMatch(car -> car.price <= 10 || car.price > 40)
    }

}


