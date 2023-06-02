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

package lazy_loading

import io.jmix.core.*
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.entity_extension.Address
import test_support.entity.lazyloading.*
import test_support.entity.lazyloading.self_ref_in_param.A
import test_support.entity.lazyloading.self_ref_in_param.B

class LazyLoadingTest extends DataSpec {

    @Autowired
    DataManager dataManager
    @Autowired
    Metadata metadata
    @Autowired
    FetchPlanRepository fetchPlanRepository

    def "OneToOne with field test"() {
        setup:

        OneToOneNoFieldEntity noFieldEntity = metadata.create(OneToOneNoFieldEntity.class)
        noFieldEntity.setName("No field name")
        dataManager.save(noFieldEntity)

        OneToOneFieldEntity fieldEntity = metadata.create(OneToOneFieldEntity.class)
        fieldEntity.setName("Field name")
        fieldEntity.setOneToOneNoFieldEntity(noFieldEntity)
        dataManager.save(fieldEntity)
        UUID id = fieldEntity.getId()

        when:

        LoadContext<OneToOneFieldEntity> loadContext = new LoadContext<>(metadata.getClass(OneToOneFieldEntity.class))
        loadContext.setId(id)

        loadContext.setFetchPlan(fetchPlanRepository.getFetchPlan(OneToOneFieldEntity.class, "OneToOneFieldEntity"))
        fieldEntity = dataManager.load(loadContext)

        then:

        fieldEntity.getName() == "Field name"
        fieldEntity.getOneToOneNoFieldEntity() == noFieldEntity
    }

    def "OneToOne without field test"() {
        setup:

        OneToOneNoFieldEntity noFieldEntity = metadata.create(OneToOneNoFieldEntity.class)
        noFieldEntity.setName("No field name")
        dataManager.save(noFieldEntity)

        OneToOneFieldEntity fieldEntity = metadata.create(OneToOneFieldEntity.class)
        fieldEntity.setName("Field name")
        fieldEntity.setOneToOneNoFieldEntity(noFieldEntity)
        dataManager.save(fieldEntity)
        UUID id = noFieldEntity.getId()

        when:

        LoadContext<OneToOneNoFieldEntity> loadContext = new LoadContext<>(metadata.getClass(OneToOneNoFieldEntity.class))
        loadContext.setId(id)

        loadContext.setFetchPlan(fetchPlanRepository.getFetchPlan(OneToOneNoFieldEntity.class, "OneToOneNoFieldEntity"))
        noFieldEntity = dataManager.load(loadContext)

        then:

        noFieldEntity.getName() == "No field name"
        noFieldEntity.getOneToOneFieldEntity() == fieldEntity
    }

    def "ManyToOne test"() {
        setup:

        OneToManyEntity oneToManyEntity = metadata.create(OneToManyEntity.class)
        oneToManyEntity.setName("Name")
        dataManager.save(oneToManyEntity)

        ManyToOneEntity manyToOneEntity = metadata.create(ManyToOneEntity.class)
        manyToOneEntity.setName("Name many")
        manyToOneEntity.setOneToManyEntity(oneToManyEntity)
        dataManager.save(manyToOneEntity)
        UUID id = manyToOneEntity.getId()

        when:

        LoadContext<ManyToOneEntity> loadContext = new LoadContext<>(metadata.getClass(ManyToOneEntity.class))
        loadContext.setId(id)

        loadContext.setFetchPlan(fetchPlanRepository.getFetchPlan(ManyToOneEntity.class, "ManyToOneEntity"))
        manyToOneEntity = dataManager.load(loadContext)

        then:

        manyToOneEntity.getName() == "Name many"
        manyToOneEntity.getOneToManyEntity() == oneToManyEntity
    }

    def "OneToMany test"() {
        setup:

        OneToManyEntity oneToManyEntity = metadata.create(OneToManyEntity.class)
        oneToManyEntity.setName("Name")
        dataManager.save(oneToManyEntity)
        UUID id = oneToManyEntity.getId()

        ManyToOneEntity manyToOneEntity = metadata.create(ManyToOneEntity.class)
        manyToOneEntity.setName("Name many")
        manyToOneEntity.setOneToManyEntity(oneToManyEntity)
        dataManager.save(manyToOneEntity)

        manyToOneEntity = metadata.create(ManyToOneEntity.class)
        manyToOneEntity.setName("Name many 2")
        manyToOneEntity.setOneToManyEntity(oneToManyEntity)
        dataManager.save(manyToOneEntity)

        when:

        LoadContext<OneToManyEntity> loadContext = new LoadContext<>(metadata.getClass(OneToManyEntity.class))
        loadContext.setId(id)

        loadContext.setFetchPlan(fetchPlanRepository.getFetchPlan(OneToManyEntity.class, "OneToManyEntity"))
        oneToManyEntity = dataManager.load(loadContext)
        then:

        oneToManyEntity.getName() == "Name"
        oneToManyEntity.getManyToOneEntities().size() == 2
    }

    def "OneToMany traverse from ManyToOne"() {
        setup:

        OneToManyEntity oneToManyEntity = metadata.create(OneToManyEntity.class)
        oneToManyEntity.setName("Name")
        dataManager.save(oneToManyEntity)

        ManyToOneEntity manyToOneEntity1 = metadata.create(ManyToOneEntity.class)
        manyToOneEntity1.setName("Name many")
        manyToOneEntity1.setOneToManyEntity(oneToManyEntity)
        dataManager.save(manyToOneEntity1)

        ManyToOneEntity manyToOneEntity2 = metadata.create(ManyToOneEntity.class)
        manyToOneEntity2.setName("Name many 2")
        manyToOneEntity2.setOneToManyEntity(oneToManyEntity)
        dataManager.save(manyToOneEntity2)

        when:

        LoadContext<ManyToOneEntity> loadContext = new LoadContext<>(metadata.getClass(ManyToOneEntity.class))
        loadContext.setId(manyToOneEntity2)

        ManyToOneEntity manyToOneEntity = dataManager.load(loadContext)
        def manyToOneEntities = manyToOneEntity.getOneToManyEntity().manyToOneEntities

        then:

        manyToOneEntities.size() == 2
        manyToOneEntities.each {
            assert it.name.startsWith('Name')
        }
    }

    def "ManyToMany test"() {
        setup:

        UUID twoId = prepareManyToMany()

        LoadContext<ManyToManySecondEntity> loadContext = new LoadContext<>(metadata.getClass(ManyToManySecondEntity.class))
        loadContext.setId(twoId)

        loadContext.setFetchPlan(fetchPlanRepository.getFetchPlan(ManyToManySecondEntity.class, "ManyToManySecondEntity"))

        when:

        ManyToManySecondEntity result = dataManager.load(loadContext)

        then:

        result.getName() == "Name 1"
        result.getManyToManyFirstEntities().size() == 5
    }

    def "OneToOne duplicate test"() {
        setup:

        OneToOneNoFieldEntity noFieldEntity = metadata.create(OneToOneNoFieldEntity.class)
        noFieldEntity.setName("No field name")
        dataManager.save(noFieldEntity)
        UUID idNoField = noFieldEntity.getId()

        OneToOneFieldEntity fieldEntity = metadata.create(OneToOneFieldEntity.class)
        fieldEntity.setName("Field name")
        fieldEntity.setOneToOneNoFieldEntity(noFieldEntity)
        dataManager.save(fieldEntity)
        UUID idField = fieldEntity.getId()

        when:

        LoadContext loadContext = new LoadContext<>(metadata.getClass(OneToOneFieldEntity.class))
        loadContext.setId(idField)
        loadContext.setFetchPlan(fetchPlanRepository.getFetchPlan(OneToOneFieldEntity.class, "OneToOneFieldEntity"))
        fieldEntity = dataManager.load(loadContext)

        loadContext = new LoadContext<>(metadata.getClass(OneToOneNoFieldEntity.class))
        loadContext.setId(idNoField)
        loadContext.setFetchPlan(fetchPlanRepository.getFetchPlan(OneToOneNoFieldEntity.class, "OneToOneNoFieldEntity"))
        noFieldEntity = dataManager.load(loadContext)

        then:

        fieldEntity.is(fieldEntity.getOneToOneNoFieldEntity().getOneToOneFieldEntity())
        noFieldEntity.is(noFieldEntity.getOneToOneFieldEntity().getOneToOneNoFieldEntity())
    }

    def "ManyToMany duplicate test"() {
        setup:

        UUID twoId = prepareManyToMany()

        LoadContext<ManyToManySecondEntity> loadContext = new LoadContext<>(metadata.getClass(ManyToManySecondEntity.class))
        loadContext.setId(twoId)

        loadContext.setFetchPlan(fetchPlanRepository.getFetchPlan(ManyToManySecondEntity.class, "ManyToManySecondEntity"))

        when:

        ManyToManySecondEntity result = dataManager.load(loadContext)

        then:

        checkManyToManyDuplicate(result)
    }

    def "Value holder should not be searched for transient or embedded entities"() {
        setup: "This test checks embedded case. Transient case checked using ManyToOneEntity#transientField in other tests"
        SelfReferencedEmployee supervisor = metadata.create(SelfReferencedEmployee)
        supervisor.setHomeAddress(new Address())
        supervisor.homeAddress.city = "Moskow"
        supervisor.homeAddress.street = "Sharikopodshipnikovskaya"

        SelfReferencedEmployee worker = metadata.create(SelfReferencedEmployee)
        worker.setSupervisor(supervisor)

        dataManager.save(supervisor, worker)

        when: "Entity loaded by ValueHolder through reference on owning side"
        SelfReferencedEmployee loadedWorker = dataManager.load(SelfReferencedEmployee)
                .id(worker.id)
                .one()
        SelfReferencedEmployee loadedSupervisor = loadedWorker.getSupervisor()

        then: "ValueHolder should not be searched for Embedded property"
        noExceptionThrown()

        cleanup:
        if (worker != null) dataManager.remove(worker)
        if (supervisor != null) dataManager.remove(supervisor)
    }

    def "AbstractValueHolder should not throw exception when entity passed as self-reference parameter in query"() {
        setup:

        def a0 = dataManager.create(A)

        def a = dataManager.create(A)
        def b = dataManager.create(B)
        a.b = b

        dataManager.save(a0, a, b)

        when:
        def reloadedA = dataManager.load(Id.of(a)).one();

        //AbstractSingleValueHolder#getRow() invoked for reloadedA instance when it passed as parameter
        def reloadedAWithParam = dataManager.load(A)
                .query("select e from test_ll_A e where e.b.a = :param")
                .parameter("param", reloadedA)
                .one()

        then:
        reloadedA == reloadedAWithParam

        cleanup:
        jdbc.update("delete from TEST_LL_A")
        jdbc.update("delete from TEST_LL_B")
    }

    boolean checkManyToManyDuplicate(ManyToManySecondEntity entity) {
        boolean contains = false
        for (ManyToManySecondEntity lazyLoadedEntity : entity.manyToManyFirstEntities.iterator().next().manyToManySecondEntities) {
            if (entity.is(lazyLoadedEntity)) {
                contains = true
                break
            }
        }
        return contains
    }

    UUID prepareManyToMany() {
        ManyToManyFirstEntity manyToManyEntity1 = metadata.create(ManyToManyFirstEntity.class)
        manyToManyEntity1.setName("Name 1")
        dataManager.save(manyToManyEntity1)

        ManyToManyFirstEntity manyToManyEntity2 = metadata.create(ManyToManyFirstEntity.class)
        manyToManyEntity2.setName("Name 2")
        dataManager.save(manyToManyEntity2)

        ManyToManyFirstEntity manyToManyEntity3 = metadata.create(ManyToManyFirstEntity.class)
        manyToManyEntity3.setName("Name 3")
        dataManager.save(manyToManyEntity3)

        ManyToManyFirstEntity manyToManyEntity4 = metadata.create(ManyToManyFirstEntity.class)
        manyToManyEntity4.setName("Name 4")
        dataManager.save(manyToManyEntity4)

        ManyToManyFirstEntity manyToManyEntity5 = metadata.create(ManyToManyFirstEntity.class)
        manyToManyEntity5.setName("Name 5")
        dataManager.save(manyToManyEntity5)

        List<ManyToManyFirstEntity> manyToManyEntities = new ArrayList<>()
        manyToManyEntities.add(manyToManyEntity1)
        manyToManyEntities.add(manyToManyEntity2)
        manyToManyEntities.add(manyToManyEntity3)
        manyToManyEntities.add(manyToManyEntity4)
        manyToManyEntities.add(manyToManyEntity5)

        ManyToManySecondEntity manyToManyTwoEntity1 = metadata.create(ManyToManySecondEntity.class)
        manyToManyTwoEntity1.setName("Name 1")
        manyToManyTwoEntity1.setManyToManyFirstEntities(manyToManyEntities)
        dataManager.save(manyToManyTwoEntity1)
        UUID secondId = manyToManyTwoEntity1.getId()

        ManyToManySecondEntity manyToManyTwoEntity2 = metadata.create(ManyToManySecondEntity.class)
        manyToManyTwoEntity2.setName("Name 2")
        manyToManyTwoEntity2.setManyToManyFirstEntities(manyToManyEntities)
        dataManager.save(manyToManyTwoEntity2)

        return secondId
    }
}
