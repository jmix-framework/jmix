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
import org.springframework.beans.factory.annotation.Autowired
import test_support.entity.Car
import test_support.entity.CarType
import test_support.entity.Garage
import test_support.entity.test.CompositionO2OTestEntity
import test_support.entity.test.DatatypesTestEntity
import test_support.entity.test.DeeplyNestedTestEntity

class PopulateAndCheckCompositionTest extends AbstractGraphQLTest {
    @Autowired
    EntityMutationDataFetcher entityMutationDataFetcher = new EntityMutationDataFetcher()

    def "method doesn't work for associations"() {
        when:
        def car1 = new Car()
        car1.id = UUID.fromString("af86f938-1f91-4525-81bd-edeb8e11f6e9")
        car1.regNumber = 'a123aq'
        car1.carType = CarType.SEDAN
        car1.manufacturer = 'VAZ'

        def car2 = new Car()
        car2.id = UUID.fromString("74520185-4803-48db-86f6-a9e6df11c38c")
        car2.regNumber = 'a321aq'
        car2.carType = CarType.HATCHBACK
        car2.manufacturer = 'GAZ'

        List<Car> cars = new ArrayList<>()
        cars.add(car1)
        cars.add(car2)

        def garage = new Garage()
        garage.id = UUID.fromString("e7eb15d8-e34c-4cda-b15e-e9121b0dea01")
        garage.name = 'test'
        garage.cars = cars

        entityMutationDataFetcher.populateAndCheckComposition(garage, new HashSet<Object>())

        then:
        garage.cars.get(0).garage == null
        garage.cars.get(1).garage == null
    }

    def "method sets parent for deeply nested compositions"() {
        when:
        def datatypesTestEntity = new DatatypesTestEntity()
        datatypesTestEntity.id = UUID.fromString("af86f938-1f91-4525-81bd-edeb8e11f6e9")
        def o2OTestEntity = new CompositionO2OTestEntity()
        o2OTestEntity.id = UUID.fromString("74520185-4803-48db-86f6-a9e6df11c38c")
        def nestedTestEntity = new DeeplyNestedTestEntity()
        nestedTestEntity.id = UUID.fromString("e7eb15d8-e34c-4cda-b15e-e9121b0dea01")

        o2OTestEntity.nestedComposition = nestedTestEntity
        datatypesTestEntity.compositionO2Oattr = o2OTestEntity

        then:
        nestedTestEntity.parentO2Ocomposition == null

        when:
        entityMutationDataFetcher.populateAndCheckComposition(datatypesTestEntity, new HashSet<Object>())

        then:
        nestedTestEntity.parentO2Ocomposition == o2OTestEntity
    }

    def "method sets parent for one dept compositions"() {
        when:
        def o2OTestEntity = new CompositionO2OTestEntity()
        o2OTestEntity.id = UUID.fromString("af86f938-1f91-4525-81bd-edeb8e11f6e9")
        def nestedTestEntity = new DeeplyNestedTestEntity()
        nestedTestEntity.id = UUID.fromString("74520185-4803-48db-86f6-a9e6df11c38c")

        o2OTestEntity.nestedComposition = nestedTestEntity

        then:
        nestedTestEntity.parentO2Ocomposition == null

        when:
        entityMutationDataFetcher.populateAndCheckComposition(o2OTestEntity, new HashSet<Object>())

        then:
        nestedTestEntity.parentO2Ocomposition == o2OTestEntity
    }

    def "throws the exception when parent has wrong ID"() {
        when:
        def o2OTestEntity = new CompositionO2OTestEntity()
        o2OTestEntity.id = UUID.fromString("af86f938-1f91-4525-81bd-edeb8e11f6e9")
        def anotherO2OTestEntity = new CompositionO2OTestEntity()
        anotherO2OTestEntity.id = UUID.fromString("74520185-4803-48db-86f6-a9e6df11c38c")
        def nestedTestEntity = new DeeplyNestedTestEntity()
        nestedTestEntity.id = UUID.fromString("e7eb15d8-e34c-4cda-b15e-e9121b0dea01")

        o2OTestEntity.nestedComposition = nestedTestEntity
        nestedTestEntity.parentO2Ocomposition = anotherO2OTestEntity

        entityMutationDataFetcher.populateAndCheckComposition(o2OTestEntity, new HashSet<Object>())

        then:
        def e = thrown(GqlEntityValidationException)
        e.message == "Composition attribute 'nestedComposition' in class 'scr_CompositionO2OTestEntity' " +
                "doesn't contain the correct link to parent entity. " +
                "Please set correct parent ID 'af86f938-1f91-4525-81bd-edeb8e11f6e9' in composition relation."
    }

    def "works for entity without composition"() {
        when:
        def datatypesTestEntity = new DatatypesTestEntity()
        datatypesTestEntity.id =  UUID.fromString("af86f938-1f91-4525-81bd-edeb8e11f6e9")

        entityMutationDataFetcher.populateAndCheckComposition(datatypesTestEntity, new HashSet<Object>())

        then:
        noExceptionThrown()
    }

}
