/*
 * Copyright 2020 Haulmont.
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

package entity_extension


import io.jmix.core.DataManager
import io.jmix.core.ExtendedEntities
import io.jmix.core.FetchPlan
import io.jmix.core.Metadata
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.entity_extension.*

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.TypedQuery

class EntityExtensionTest extends DataSpec {

    @Autowired
    ExtendedEntities extendedEntities
    @Autowired
    Metadata metadata
    @Autowired
    DataManager dataManager
    @PersistenceContext
    EntityManager entityManager

    def "original class"() {
        expect:
        extendedEntities.getOriginalMetaClass(metadata.getClass(ExtDriver)).javaClass == Driver
        extendedEntities.getOriginalMetaClass('exttest_ExtDriver').javaClass == Driver
        extendedEntities.getOriginalOrThisMetaClass(metadata.getClass(ExtDriver)).javaClass == Driver

        extendedEntities.getOriginalMetaClass('exttest_DriverAllocation') == null
        extendedEntities.getOriginalOrThisMetaClass(metadata.getClass(DriverAllocation)).javaClass == DriverAllocation
    }

    def "effective class"() {
        expect:
        extendedEntities.getEffectiveClass(Driver) == ExtDriver
        extendedEntities.getEffectiveClass(DriverAllocation) == DriverAllocation
    }

    def "create entity"() {
        when:
        def driver = metadata.create(Driver)

        then:
        driver instanceof ExtDriver
    }

    def "load entity"() {
        def driver = dataManager.create(Driver)
        driver.name = 'Joe'
        dataManager.save(driver)

        when:
        def driver1 = dataManager.load(Driver).id(driver.id).one()

        then:
        driver1 instanceof ExtDriver

        when:
        def list = dataManager.load(Driver)
                .query('select d from exttest_Driver d where d.id = :id')
                .parameter('id', driver.id)
                .list()

        then:
        list[0] instanceof ExtDriver
    }

    def "load entity with inheritance"() {
        def doc = dataManager.create(Doc)
        doc.number = '1'
        doc.extName = 'ext name'
        dataManager.save(doc)

        when:
        def doc1 = dataManager.load(Doc).query('select e from exttest_Doc e where e.id = :id').parameter('id', doc.id).one()

        then:
        doc1 instanceof ExtDoc
        doc1.extName == 'ext name'
    }

    def "load one-to-one association"() {
        def callsign = dataManager.create(DriverCallsign)
        callsign.callsign = '111'

        def driver = dataManager.create(Driver)
        driver.name = 'Joe'
        driver.info = 'the driver'
        driver.callsign = callsign
        dataManager.save(driver, callsign)

        when:
        def callsign1 = dataManager.load(DriverCallsign)
                .id(callsign.id)
                .fetchPlan { fp -> fp.add('driver', FetchPlan.INSTANCE_NAME) }
                .one()

        then:
        callsign1.driver instanceof ExtDriver
        callsign1.driver.getInstanceName() == 'Joe:the driver'
    }

    def "load many-to-one association"() {
        def driver = dataManager.create(Driver)
        driver.name = 'Joe'
        driver.info = 'the driver'

        def driverAllocation = dataManager.create(DriverAllocation)
        driverAllocation.car = 'abc'
        driverAllocation.driver = driver
        dataManager.save(driver, driverAllocation)

        when:
        def driverAllocation1 = dataManager.load(DriverAllocation)
                .id(driverAllocation.id)
                .fetchPlan { fp -> fp.add('driver', FetchPlan.INSTANCE_NAME) }
                .one()

        then:
        driverAllocation1.driver instanceof ExtDriver
        driverAllocation1.driver.getInstanceName() == 'Joe:the driver'
    }

    def "load many-to-one association with inheritance"() {
        def doc = dataManager.create(Doc)
        doc.description = 'doc 1'
        doc.number = '1'
        doc.extName = 'ext 1'

        def plant = dataManager.create(Plant)
        plant.name = 'the plant'
        plant.doc = doc

        dataManager.save(doc, plant)

        when:
        def plant1 = dataManager.load(Plant)
                .id(plant.id)
                .fetchPlan { fp -> fp.add('doc', FetchPlan.BASE) }
                .one()

        then:
        plant1.doc instanceof ExtDoc
        plant1.doc.extName == 'ext 1'
    }

    def "load many-to-one association in embedded"() {
        def place = dataManager.create(Place)
        place.name = 'somewhere'
        place.description = 'good place'

        def driver = dataManager.create(Driver)
        driver.name = 'Joe'
        driver.address.place = place

        dataManager.save(place, driver)

        when:
        def driver1 = dataManager.load(Driver)
                .id(driver.id)
                .fetchPlan { fp -> fp.addAll('name', 'address.place.name', 'address.place.description') }
                .one()

        then:
        driver1.address.place instanceof ExtPlace
        driver1.address.place.description == 'good place'
    }

    def "load many-to-many association"() {
        def model1 = metadata.create(Model)
        model1.name = 'm1'
        model1.launchYear = 2001

        def model2 = metadata.create(Model)
        model2.name = 'm2'
        model2.launchYear = 2002

        def plant = dataManager.create(Plant)
        plant.name = 'the plant'
        plant.models = [model1, model2]

        dataManager.save(plant, model1, model2)

        when:
        def plant1 = dataManager.load(Plant)
                .id(plant.id)
                .fetchPlan { fp -> fp.addAll('name', 'models.name', 'models.launchYear') }
                .one()

        then:
        plant1.models.find { it.name == 'm1'}.launchYear == 2001
        plant1.models.find { it.name == 'm2'}.launchYear == 2002

        when:
        def model = dataManager.load(Model)
                .id(model1.id)
                .fetchPlan { fp -> fp.addAll('name', 'plants.name') }
                .one()

        then:
        model.plants[0].name == 'the plant'
    }

    def "load by native query"() {
        def model = metadata.create(Model)
        model.name = 'm1'
        model.launchYear = 2001
        dataManager.save(model)

        TypedQuery<Model> query = entityManager.createNativeQuery('select ID, NAME from EXTTEST_MODEL where ID = ?', Model)
        query.setParameter(1, model.id)

        when:
        def model1 = query.getSingleResult()

        then:
        model1 instanceof ExtModel
    }

    def "load entity from additional data store"() {
        def foo = dataManager.create(Db1Foo)
        foo.name = 'foo'
        foo.info = 'abc'

        def bar = dataManager.create(Db1Bar)
        bar.name = 'bar'
        bar.foo = foo

        dataManager.save(foo, bar)

        when:
        def bar1 = dataManager.load(Db1Bar)
                .id(bar.id)
                .fetchPlan { fp -> fp.addAll('name', 'foo.name', 'foo.info') }
                .one()

        then:
        bar1.foo instanceof Db1ExtFoo
        bar1.foo.info == 'abc'
    }
}
