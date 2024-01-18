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
import io.jmix.data.PersistenceHints
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.lazyloading.OneToOneFieldEntity
import test_support.entity.lazyloading.OneToOneNoFieldEntity
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

        then: "BUG: Soft deleted entity is not loaded, unlike in other cases, see https://github.com/jmix-framework/jmix/issues/2466"
        fieldEntity.getName() == "Field name"
        fieldEntity.getOneToOneNoFieldEntity() == null
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

        then: "BUG: reference is not null unlike in other cases."
        noFieldEntity.getName() == "No field name"
        noFieldEntity.getOneToOneFieldEntity() == fieldEntity
    }
}
