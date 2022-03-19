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

import io.jmix.core.*
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.entity_extension.Driver
import test_support.entity.entity_extension.SampleExtensionEntity
import test_support.entity.entity_extension.SamplePlatformEntity
import test_support.entity.entity_extension.SampleProductEntity

class EntityExtension2ndLevelTest extends DataSpec {

    @Autowired
    ExtendedEntities extendedEntities
    @Autowired
    Metadata metadata
    @Autowired
    FetchPlanRepository fetchPlanRepository
    @Autowired
    DataManager dataManager

    def "original class"() {
        expect:
        extendedEntities.getOriginalMetaClass(metadata.getClass(SampleExtensionEntity)).javaClass == SamplePlatformEntity
        extendedEntities.getOriginalMetaClass('exttest_SampleExtensionEntity').javaClass == SamplePlatformEntity
        extendedEntities.getOriginalMetaClass('exttest_SampleProductEntity').javaClass == SamplePlatformEntity
        extendedEntities.getOriginalOrThisMetaClass(metadata.getClass(SampleExtensionEntity)).javaClass == SamplePlatformEntity
    }

    def "effective class"() {
        expect:
        extendedEntities.getEffectiveClass(SamplePlatformEntity) == SampleExtensionEntity
        extendedEntities.getEffectiveClass(SampleProductEntity) == SampleExtensionEntity
    }

    def "create entity"() {
        def extensionEntity = metadata.create(SampleExtensionEntity)
        extensionEntity instanceof SampleExtensionEntity

        def platformEntity = metadata.create(SamplePlatformEntity)
        platformEntity instanceof SampleExtensionEntity

        def productEntity = metadata.create(SampleProductEntity)
        productEntity instanceof SampleExtensionEntity
    }

    def "load entity"() {
        def entity = dataManager.create(SamplePlatformEntity)
        entity.setName('an entity')
        entity.description = 'description1'
        entity.info = 'info1'

        dataManager.save(entity)

        when:
        def entity1 = dataManager.load(SamplePlatformEntity).id(entity.id).one()

        then:
        entity1 instanceof SampleExtensionEntity
        entity1.description == 'description1'
        entity1.info == 'info1'

        when:
        def entity2 = dataManager.load(SampleProductEntity).id(entity.id).one()

        then:
        entity2 instanceof SampleExtensionEntity
        entity2.description == 'description1'
        entity2.info == 'info1'
    }

    def "load entity by query"() {
        def entity = dataManager.create(SamplePlatformEntity)
        entity.setName('an entity')
        entity.description = 'description1'
        entity.info = 'info1'

        dataManager.save(entity)

        when:
        def entity1 = dataManager.load(SamplePlatformEntity)
                .query('select e from exttest_SamplePlatformEntity e where e.id = :id')
                .parameter('id', entity.id)
                .one()

        then:
        entity1 instanceof SampleExtensionEntity
        entity1.description == 'description1'
        entity1.info == 'info1'

        when:
        def entity2 = dataManager.load(SampleProductEntity)
                .query('select e from exttest_SampleProductEntity e where e.id = :id')
                .parameter('id', entity.id)
                .one()

        then:
        entity2 instanceof SampleExtensionEntity
        entity2.description == 'description1'
        entity2.info == 'info1'
    }

    def "load many-to-one association"() {
        def entity = dataManager.create(SamplePlatformEntity)
        entity.setName('an entity')
        entity.description = 'description1'
        entity.info = 'info1'

        def driver = dataManager.create(Driver)
        driver.name = 'driver1'
        driver.platformEntity = entity

        dataManager.save(entity, driver)

        when:
        def driver1 = dataManager.load(Driver)
                .id(driver.id)
                .fetchPlan { fp -> fp.addAll('name', 'platformEntity.name', 'platformEntity.info', 'platformEntity.description') }
                .one()

        then:
        driver1.platformEntity instanceof SampleExtensionEntity
        driver1.platformEntity.name == 'an entity'
        driver1.platformEntity.description == 'description1'
        driver1.platformEntity.info == 'info1'
    }

    def "shared fetch plans"() {
        when:
        FetchPlan fetchPlan = fetchPlanRepository.findFetchPlan(metadata.getClass(SampleExtensionEntity), "test1")
        then:
        fetchPlan.getProperty("name")

        when:
        fetchPlan = fetchPlanRepository.findFetchPlan(metadata.getClass(SampleProductEntity), 'test1')
        then:
        fetchPlan.getProperty("name")

        when:
        fetchPlan = fetchPlanRepository.findFetchPlan(metadata.getClass(SamplePlatformEntity), 'test1')
        then:
        fetchPlan.getProperty("name")

        when:
        fetchPlan = fetchPlanRepository.findFetchPlan(metadata.getClass(SampleExtensionEntity), 'test2')
        then:
        fetchPlan.getProperty("name")
        fetchPlan.getProperty("description")

        when:
        fetchPlan = fetchPlanRepository.findFetchPlan(metadata.getClass(SampleProductEntity), 'test2')
        then:
        fetchPlan.getProperty("name")
        fetchPlan.getProperty("description")

        when:
        fetchPlan = fetchPlanRepository.findFetchPlan(metadata.getClass(SamplePlatformEntity), 'test2')
        then:
        fetchPlan.getProperty("name")
        fetchPlan.getProperty("description")

        when:
        fetchPlan = fetchPlanRepository.findFetchPlan(metadata.getClass(SampleExtensionEntity), 'test3')
        then:
        fetchPlan.getProperty("name")
        fetchPlan.getProperty("description")
        fetchPlan.getProperty("info")

        when:
        fetchPlan = fetchPlanRepository.findFetchPlan(metadata.getClass(SampleProductEntity), 'test3')
        then:
        fetchPlan.getProperty("name")
        fetchPlan.getProperty("description")
        fetchPlan.getProperty("info")

        when:
        fetchPlan = fetchPlanRepository.findFetchPlan(metadata.getClass(SamplePlatformEntity), 'test3')
        then:
        fetchPlan.getProperty("name")
        fetchPlan.getProperty("description")
        fetchPlan.getProperty("info")
    }
}
