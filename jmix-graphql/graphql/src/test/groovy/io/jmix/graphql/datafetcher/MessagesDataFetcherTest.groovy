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


import io.jmix.core.security.SystemAuthenticator
import io.jmix.graphql.AbstractGraphQLTest
import org.springframework.beans.factory.annotation.Autowired

class MessagesDataFetcherTest extends AbstractGraphQLTest {

    @Autowired
    SystemAuthenticator authenticator

    @SuppressWarnings('unused')
    void setup() {
        authenticator.begin()
    }

    @SuppressWarnings('unused')
    void cleanup() {
        authenticator.end()
    }

    def "all entities messages"() {
        when:
        def response = query(
                        "datafetcher/entities-messages.graphql"
                )

        then:
        def body = getBody(response)
        body.contains('{"data":{"entityMessages":[')
        body.contains('{"key":"scr$Car","value":"Car"}')
        body.contains('{"key":"scr$Car.purchaseDate","value":"Purchase Date"}')
        body.contains('{"key":"scr$Car.lastModifiedDate","value":"Car.lastModifiedDate"}')
        body.contains('{"key":"scr$Car.maxPassengers","value":"Max Passengers"}')
        body.contains('{"key":"scr$Car.lastModifiedBy","value":"Car.lastModifiedBy"}')
        body.contains('{"key":"scr$Car.garage","value":"Garage"}')
        body.contains('{"key":"scr$Car.wheelOnRight","value":"Wheel On Right"}')
        body.contains('{"key":"scr$Car.version","value":"Car.version"}')
        body.contains('{"key":"scr$Car.manufacturer","value":"Car manufacturer"}')
        body.contains('{"key":"scr$Car.technicalCertificate","value":"Technical Certificate"}')
        body.contains('{"key":"scr$Car.regNumber","value":"Reg Number"}')
        body.contains('{"key":"scr$Car.carType","value":"Type"}')
        body.contains('{"key":"scr$Car.createdDate","value":"Car.createdDate"}')
        body.contains('{"key":"scr$Car.ecoRank","value":"Eco Rank"}')
        body.contains('{"key":"scr$Car.createdBy","value":"Car.createdBy"}')
        body.contains('{"key":"scr$Car.price","value":"Price"}')
        body.contains('{"key":"scr$Car.manufactureDate","value":"Car.manufactureDate"}')
        body.contains('{"key":"scr$Car.model","value":"Model"}')
        body.contains('{"key":"scr$Car.id","value":"Car.id"}')
        body.contains('{"key":"scr$Car.mileage","value":"Mileage"}')
    }

    def 'scr$Car messages'() {
        when:
        def response = query(
                        "datafetcher/entities-messages.graphql",
                        asObjectNode('{"className": "scr$Car"}')
                )

        then:
        def body = getBody(response)
        body.contains('{"data":{"entityMessages":[')
        body.contains('{"key":"scr$Car","value":"Car"}')
        body.contains('{"key":"scr$Car.purchaseDate","value":"Purchase Date"}')
        body.contains('{"key":"scr$Car.lastModifiedDate","value":"Car.lastModifiedDate"}')
        body.contains('{"key":"scr$Car.maxPassengers","value":"Max Passengers"}')
        body.contains('{"key":"scr$Car.lastModifiedBy","value":"Car.lastModifiedBy"}')
        body.contains('{"key":"scr$Car.garage","value":"Garage"}')
        body.contains('{"key":"scr$Car.wheelOnRight","value":"Wheel On Right"}')
        body.contains('{"key":"scr$Car.version","value":"Car.version"}')
        body.contains('{"key":"scr$Car.manufacturer","value":"Car manufacturer"}')
        body.contains('{"key":"scr$Car.technicalCertificate","value":"Technical Certificate"}')
        body.contains('{"key":"scr$Car.regNumber","value":"Reg Number"}')
        body.contains('{"key":"scr$Car.carType","value":"Type"}')
        body.contains('{"key":"scr$Car.createdDate","value":"Car.createdDate"}')
        body.contains('{"key":"scr$Car.ecoRank","value":"Eco Rank"}')
        body.contains('{"key":"scr$Car.createdBy","value":"Car.createdBy"}')
        body.contains('{"key":"scr$Car.price","value":"Price"}')
        body.contains('{"key":"scr$Car.manufactureDate","value":"Car.manufactureDate"}')
        body.contains('{"key":"scr$Car.model","value":"Model"}')
        body.contains('{"key":"scr$Car.id","value":"Car.id"}')
        body.contains('{"key":"scr$Car.mileage","value":"Mileage"}')
    }

    def 'scr$Car messages with locale'() {
        when:
        def response = query(
                        "datafetcher/entities-messages.graphql",
                        asObjectNode('{"className": "scr$Car", "locale": "ru"}')
                )

        then:
        def body = getBody(response)
        body.contains('{"data":{"entityMessages":[')
        body.contains('{"key":"scr$Car","value":"Автомобиль"}')
        body.contains('{"key":"scr$Car.purchaseDate","value":"Дата покупки"}')
        body.contains('{"key":"scr$Car.lastModifiedDate","value":"Car.lastModifiedDate"}')
        body.contains('{"key":"scr$Car.maxPassengers","value":"Пассажировместимость"}')
        body.contains('{"key":"scr$Car.lastModifiedBy","value":"Car.lastModifiedBy"}')
        body.contains('{"key":"scr$Car.garage","value":"Гараж"}')
        body.contains('{"key":"scr$Car.wheelOnRight","value":"Правый руль"}')
        body.contains('{"key":"scr$Car.version","value":"Car.version"}')
        body.contains('{"key":"scr$Car.manufacturer","value":"Производитель"}')
        body.contains('{"key":"scr$Car.technicalCertificate","value":"Technical Certificate"}')
        body.contains('{"key":"scr$Car.regNumber","value":"Рег. номер"}')
        body.contains('{"key":"scr$Car.carType","value":"Тип"}')
        body.contains('{"key":"scr$Car.createdDate","value":"Car.createdDate"}')
        body.contains('{"key":"scr$Car.ecoRank","value":"Эко класс"}')
        body.contains('{"key":"scr$Car.createdBy","value":"Car.createdBy"}')
        body.contains('{"key":"scr$Car.price","value":"Цена"}')
        body.contains('{"key":"scr$Car.manufactureDate","value":"Дата изготовления"}')
        body.contains('{"key":"scr$Car.model","value":"Модель"}')
        body.contains('{"key":"scr$Car.id","value":"Car.id"}')
        body.contains('{"key":"scr$Car.mileage","value":"Пробег"}')
    }

    def "all enum messages"() {
        when:
        def response = query(
                        "datafetcher/enum-messages.graphql"
                )

        then:
        def body = getBody(response)
        body.contains('{"data":{"enumMessages":[')
        body.contains('{"key":"test_support.entity.CarType","value":"CarType"}')
        body.contains('{"key":"test_support.entity.CarType.SEDAN","value":"Sedan"}')
        body.contains('{"key":"test_support.entity.CarType.HATCHBACK","value":"Hatchback"}')
    }

    def "CarType enum messages"() {
        when:
        def response = query(
                        "datafetcher/enum-messages.graphql",
                        asObjectNode('{"className": "test_support.entity.CarType"}')
                )

        then:
        def body = getBody(response)
        body.contains('{"data":{"enumMessages":[')
        body.contains('{"key":"test_support.entity.CarType","value":"CarType"}')
        body.contains('{"key":"test_support.entity.CarType.SEDAN","value":"Sedan"}')
        body.contains('{"key":"test_support.entity.CarType.HATCHBACK","value":"Hatchback"}')
    }

    def "CarType enum messages with locale"() {
        when:
        def response = query(
                        "datafetcher/enum-messages.graphql",
                        asObjectNode('{"className": "test_support.entity.CarType", "locale": "ru"}')
                )

        then:
        def body = getBody(response)
        body.contains('{"data":{"enumMessages":[')
        body.contains('{"key":"test_support.entity.CarType","value":"CarType"}')
        body.contains('{"key":"test_support.entity.CarType.SEDAN","value":"Седан"}')
        body.contains('{"key":"test_support.entity.CarType.HATCHBACK","value":"Хетчбек"}')
    }
}
