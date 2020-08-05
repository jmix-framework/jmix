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

import io.jmix.core.DataManager
import io.jmix.core.FetchPlanRepository
import io.jmix.core.LoadContext
import io.jmix.core.Metadata
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.lazyloading.*

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
        fieldEntity = dataManager.loadList(loadContext).iterator().next()

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
        noFieldEntity = dataManager.loadList(loadContext).iterator().next()

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
        manyToOneEntity = dataManager.loadList(loadContext).iterator().next()

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
        oneToManyEntity = dataManager.loadList(loadContext).iterator().next()
        then:

        oneToManyEntity.getName() == "Name"
        oneToManyEntity.getManyToOneEntities().size() == 2
    }

    def "ManyToMany test"() {
        setup:

        UUID twoId = prepareManyToMany()

        LoadContext<ManyToManySecondEntity> loadContext = new LoadContext<>(metadata.getClass(ManyToManySecondEntity.class))
        loadContext.setId(twoId)

        loadContext.setFetchPlan(fetchPlanRepository.getFetchPlan(ManyToManySecondEntity.class, "ManyToManySecondEntity"))

        when:

        ManyToManySecondEntity result = dataManager.loadList(loadContext).iterator().next()

        then:

        result.getName() == "Name 1"
        result.getManyToManyFirstEntities().size() == 5
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
