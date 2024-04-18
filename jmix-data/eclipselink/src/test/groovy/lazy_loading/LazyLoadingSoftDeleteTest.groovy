/*
 * Copyright 2023 Haulmont.
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
import io.jmix.core.accesscontext.InMemoryCrudEntityContext
import io.jmix.core.constraint.InMemoryConstraint
import io.jmix.core.constraint.RowLevelConstraint
import io.jmix.data.PersistenceHints
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.lazyloading.*
import test_support.entity.lazyloading.soft_deletion_vh_propagation.Activity
import test_support.entity.lazyloading.soft_deletion_vh_propagation.Customer
import test_support.entity.lazyloading.soft_deletion_vh_propagation.Details

class LazyLoadingSoftDeleteTest extends DataSpec {

    @Autowired
    DataManager dataManager
    @Autowired
    Metadata metadata
    @Autowired
    FetchPlanRepository fetchPlanRepository

    @PersistenceContext
    EntityManager entityManager


    def "soft deleted collection items not loaded for nested collection"() {
        setup:
        Customer customer = dataManager.create(Customer)
        Details details = dataManager.create(Details)
        Activity firstActivity = dataManager.create(Activity)
        Activity secondActivity = dataManager.create(Activity)

        customer.name = "Customer one"
        details.customer = customer
        firstActivity.name = "First"
        firstActivity.details = details
        secondActivity.name = "Second"
        secondActivity.details = details

        dataManager.save(customer, details, firstActivity, secondActivity)
        dataManager.remove(secondActivity)

        when:
        Customer loadedCompany = dataManager.load(Id.of(customer))
                .fetchPlan(b -> b.add("name"))
                .one()

        then:
        loadedCompany.getDetails().getActivities().size() == 1


        when:
        loadedCompany = dataManager.load(Id.of(customer))
                .hint(PersistenceHints.SOFT_DELETION, true)
                .fetchPlan(b -> b.add("name"))
                .one()

        then:
        loadedCompany.getDetails().getActivities().size() == 1


        when:
        loadedCompany = dataManager.load(Id.of(customer))
                .hint(PersistenceHints.SOFT_DELETION, false)
                .fetchPlan(b -> b.add("name"))
                .one()

        then:
        loadedCompany.getDetails().getActivities().size() == 2


        cleanup:
        jdbc.update("delete from TESTVH_ACTIVITY")
        jdbc.update("delete from TESTVH_DETAILS")
        jdbc.update("delete from TESTVH_CUSTOMER")
    }

    def "soft deleted collection items not loaded for nested collection with constraints"() {
        setup:
        Customer customer = dataManager.create(Customer)
        Details details = dataManager.create(Details)
        Activity firstActivity = dataManager.create(Activity)
        Activity secondActivity = dataManager.create(Activity)

        customer.name = "Customer one"
        details.customer = customer
        firstActivity.name = "First"
        firstActivity.details = details
        secondActivity.name = "Second"
        secondActivity.details = details

        dataManager.save(customer, details, firstActivity, secondActivity)
        dataManager.remove(secondActivity)

        when:
        Customer loadedCompany = dataManager.load(Id.of(customer))
                .fetchPlan(b -> b.add("name"))
                .accessConstraints(Collections.singleton(new TestDummyConstraint()))
                .one()

        then:
        loadedCompany.getDetails().getActivities().size() == 1


        when:
        loadedCompany = dataManager.load(Id.of(customer))
                .hint(PersistenceHints.SOFT_DELETION, true)
                .fetchPlan(b -> b.add("name"))
                .accessConstraints(Collections.singleton(new TestDummyConstraint()))
                .one()

        then:
        loadedCompany.getDetails().getActivities().size() == 1


        when:
        loadedCompany = dataManager.load(Id.of(customer))
                .hint(PersistenceHints.SOFT_DELETION, false)
                .fetchPlan(b -> b.add("name"))
                .accessConstraints(Collections.singleton(new TestDummyConstraint()))
                .one()

        then:
        loadedCompany.getDetails().getActivities().size() == 2


        cleanup:
        jdbc.update("delete from TESTVH_ACTIVITY")
        jdbc.update("delete from TESTVH_DETAILS")
        jdbc.update("delete from TESTVH_CUSTOMER")
    }


    def "OneToOne with field test (referenced entity soft deleted)"() {
        setup:

        OneToOneNoFieldEntity noFieldEntity = metadata.create(OneToOneNoFieldEntity.class)
        noFieldEntity.setName("No field name")
        dataManager.save(noFieldEntity)

        OneToOneFieldEntity fieldEntity = metadata.create(OneToOneFieldEntity.class)
        fieldEntity.setName("Field name")
        fieldEntity.setOneToOneNoFieldEntity(noFieldEntity)
        dataManager.save(fieldEntity)
        UUID id = fieldEntity.getId()

        dataManager.remove(noFieldEntity)


        when: "Load through EntityManager by query"
        fieldEntity = transaction.execute {
            OneToOneFieldEntity entity = entityManager.createQuery("select e from test_OneToOneFieldEntity e where e.id=:id", OneToOneFieldEntity)
                    .setParameter("id", id).getSingleResult()
            entity.getOneToOneNoFieldEntity()
            return entity
        }

        then: "OK, see PL-7152"
        fieldEntity.getName() == "Field name"
        fieldEntity.getOneToOneNoFieldEntity() == noFieldEntity


        when: "Load through EntityManager by find"
        fieldEntity = transaction.execute {
            OneToOneFieldEntity entity = entityManager.find(OneToOneFieldEntity, id);
            entity.getOneToOneNoFieldEntity()
            return entity
        }

        then: "OK, see PL-7152"
        fieldEntity.getName() == "Field name"
        fieldEntity.getOneToOneNoFieldEntity() == noFieldEntity

        when: "Loading through DataManager by full fetchPlan"
        LoadContext<OneToOneFieldEntity> fullLoadContext = new LoadContext<>(metadata.getClass(OneToOneFieldEntity.class))
        fullLoadContext.setId(id)

        fullLoadContext.setFetchPlan(fetchPlanRepository.getFetchPlan(OneToOneFieldEntity.class, "FullOneToOneFieldEntity"))
        fieldEntity = dataManager.load(fullLoadContext)

        then: "OK"
        fieldEntity.getName() == "Field name"
        fieldEntity.getOneToOneNoFieldEntity() == noFieldEntity


        when: "Lazy loading occurs"
        LoadContext<OneToOneFieldEntity> loadContext = new LoadContext<>(metadata.getClass(OneToOneFieldEntity.class))
        loadContext.setId(id)

        loadContext.setFetchPlan(fetchPlanRepository.getFetchPlan(OneToOneFieldEntity.class, "OneToOneFieldEntity"))
        fieldEntity = dataManager.load(loadContext)

        then: "OK, soft deleted entity must be loaded like in other cases, see https://github.com/jmix-framework/jmix/issues/2466"
        fieldEntity.getName() == "Field name"
        fieldEntity.getOneToOneNoFieldEntity() == noFieldEntity
    }

    def "OneToOne without field test (owning side entity soft deleted)"() {
        setup:

        OneToOneNoFieldEntity noFieldEntity = metadata.create(OneToOneNoFieldEntity.class)
        noFieldEntity.setName("No field name")
        dataManager.save(noFieldEntity)

        OneToOneFieldEntity fieldEntity = metadata.create(OneToOneFieldEntity.class)
        fieldEntity.setName("Field name")
        fieldEntity.setOneToOneNoFieldEntity(noFieldEntity)
        dataManager.save(fieldEntity)
        UUID id = noFieldEntity.getId()

        dataManager.remove(fieldEntity)


        when: "Load through EntityManager by query"
        noFieldEntity = transaction.execute {
            OneToOneNoFieldEntity entity = entityManager.createQuery("select e from test_OneToOneNoFieldEntity e where e.id=:id", OneToOneNoFieldEntity)
                    .setParameter("id", id).getSingleResult()
            entity.getOneToOneFieldEntity()
            return entity
        }

        then: "OK"
        noFieldEntity.getName() == "No field name"
        noFieldEntity.getOneToOneFieldEntity() == null


        when: "Load through EntityManager by find"
        noFieldEntity = transaction.execute {
            OneToOneNoFieldEntity entity = entityManager.find(OneToOneNoFieldEntity, id);
            entity.getOneToOneFieldEntity()
            return entity
        }

        then: "OK"
        noFieldEntity.getName() == "No field name"
        noFieldEntity.getOneToOneFieldEntity() == null


        when: "Loading through DataManager by full fetchPlan"
        LoadContext<OneToOneNoFieldEntity> loadContextWithFetchPlan = new LoadContext<>(metadata.getClass(OneToOneNoFieldEntity.class))
        loadContextWithFetchPlan.setId(id)

        loadContextWithFetchPlan.setFetchPlan(fetchPlanRepository.getFetchPlan(OneToOneNoFieldEntity.class, "FullOneToOneNoFieldEntity"))
        noFieldEntity = dataManager.load(loadContextWithFetchPlan)

        then: "OK"
        noFieldEntity.getName() == "No field name"
        noFieldEntity.getOneToOneFieldEntity() == null


        when: "lazy loading"
        LoadContext<OneToOneNoFieldEntity> loadContext = new LoadContext<>(metadata.getClass(OneToOneNoFieldEntity.class))
        loadContext.setId(id)

        loadContext.setFetchPlan(fetchPlanRepository.getFetchPlan(OneToOneNoFieldEntity.class, "OneToOneNoFieldEntity"))
        noFieldEntity = dataManager.load(loadContext)

        then: "OK, reference must be null like in other cases."
        noFieldEntity.getName() == "No field name"
        noFieldEntity.getOneToOneFieldEntity() == null
    }


    def "ManyToOne field test"() {
        setup:

        OneToManyEntity oneToManyEntity = dataManager.create(OneToManyEntity)
        oneToManyEntity.name = "Test OneToManyEntity"

        ManyToOneEntity manyToOneEntity = dataManager.create(ManyToOneEntity)

        manyToOneEntity.oneToManyEntity = oneToManyEntity
        manyToOneEntity.name = "Test ManyToOneEntity"

        dataManager.save(oneToManyEntity, manyToOneEntity)
        dataManager.remove(oneToManyEntity)
        UUID id = manyToOneEntity.id


        when:
        manyToOneEntity = transaction.execute {
            ManyToOneEntity entity = entityManager.find(ManyToOneEntity, id);
            entity.getOneToManyEntity()
            return entity
        }

        then:
        manyToOneEntity.oneToManyEntity == oneToManyEntity


        when:
        manyToOneEntity = dataManager.load(Id.of(manyToOneEntity))
                .fetchPlan(builder -> builder.add("oneToManyEntity"))
                .one()

        then:
        manyToOneEntity.oneToManyEntity == oneToManyEntity


        when:
        manyToOneEntity = dataManager.load(Id.of(manyToOneEntity)).one()

        then: "OneToManyEntity must be the same as for eager fetching"
        manyToOneEntity.oneToManyEntity == oneToManyEntity
    }

    def "OneToMany field test"() {
        setup:

        OneToManyEntity oneToManyEntity = dataManager.create(OneToManyEntity)
        oneToManyEntity.name = "Test OneToManyEntity"

        ManyToOneEntity manyToOneEntity = dataManager.create(ManyToOneEntity)

        manyToOneEntity.oneToManyEntity = oneToManyEntity
        manyToOneEntity.name = "Test ManyToOneEntity"

        dataManager.save(oneToManyEntity, manyToOneEntity)

        dataManager.remove(manyToOneEntity)
        UUID id = oneToManyEntity.id


        when:
        oneToManyEntity = transaction.execute {
            OneToManyEntity entity = entityManager.find(OneToManyEntity, id);
            List<ManyToOneEntity> manyToOneEntities = entity.getManyToOneEntities()
            println manyToOneEntities.size();
            return entity
        }

        then:
        oneToManyEntity.manyToOneEntities.size() == 0


        when:
        oneToManyEntity = dataManager.load(Id.of(oneToManyEntity))
                .fetchPlan(builder -> builder.add("manyToOneEntities"))
                .one()

        then:
        oneToManyEntity.manyToOneEntities.size() == 0


        when:
        oneToManyEntity = dataManager.load(Id.of(oneToManyEntity)).one()

        then:
        oneToManyEntity.manyToOneEntities.size() == 0
    }

    def "ManyToMany field test"() {
        setup:

        ManyToManyFirstEntity manyToManyFirstEntity = dataManager.create(ManyToManyFirstEntity)
        manyToManyFirstEntity.name = "Test ManyToManyFirstEntity"

        ManyToManySecondEntity manyToManySecondEntity = dataManager.create(ManyToManySecondEntity)

        manyToManySecondEntity.name = "Test ManyToManySecondEntity"
        manyToManySecondEntity.manyToManyFirstEntities = new ArrayList<>()
        manyToManySecondEntity.manyToManyFirstEntities.add(manyToManyFirstEntity)

        dataManager.save(manyToManyFirstEntity, manyToManySecondEntity)

        dataManager.remove(manyToManyFirstEntity)
        UUID id = manyToManySecondEntity.id


        when:
        manyToManySecondEntity = transaction.execute {
            ManyToManySecondEntity entity = entityManager.find(ManyToManySecondEntity, id);
            List<ManyToManyFirstEntity> manyToOneEntities = entity.getManyToManyFirstEntities()
            println manyToOneEntities.size();
            return entity
        }

        then:
        manyToManySecondEntity.manyToManyFirstEntities.size() == 0


        when:
        manyToManySecondEntity = dataManager.load(Id.of(manyToManySecondEntity))
                .fetchPlan(builder -> builder.add("manyToManyFirstEntities"))
                .one()

        then:
        manyToManySecondEntity.manyToManyFirstEntities.size() == 0

        when:
        manyToManySecondEntity = dataManager.load(Id.of(manyToManySecondEntity))
                .one()

        then:
        manyToManySecondEntity.manyToManyFirstEntities.size() == 0
    }

    /**
     * Does nothing but triggering loading through SingleValueMappedByPropertyHolder#createLoadContextByOwner(..)
     */
    static class TestDummyConstraint implements InMemoryConstraint<InMemoryCrudEntityContext>, RowLevelConstraint<InMemoryCrudEntityContext> {
        @Override
        Class<InMemoryCrudEntityContext> getContextType() {
            return InMemoryCrudEntityContext.class;
        }

        @Override
        void applyTo(InMemoryCrudEntityContext context) {

        }
    }
}
