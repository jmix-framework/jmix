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

import io.jmix.core.FetchPlan
import io.jmix.core.FetchPlanBuilder
import io.jmix.core.FetchPlanRepository
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Unroll
import test_support.DataSpec
import test_support.entity.entity_extension.Doc
import test_support.entity.entity_extension.Driver
import test_support.entity.entity_extension.ExtDoc
import test_support.entity.entity_extension.ExtDriver

class EntityExtensionFetchPlanTest extends DataSpec {

    @Autowired
    FetchPlanRepository fetchPlanRepository

    def "fetch plan builder"() {
        when:
        def fetchPlan = FetchPlanBuilder.of(Driver).addAll('name', 'info').build()

        then:
        fetchPlan.entityClass == ExtDriver
        fetchPlan.getProperty('name')
        fetchPlan.getProperty('info')
    }

    @Unroll
    def "predefined fetch plans"(Class entityClass) {
        when:
        def local = fetchPlanRepository.getFetchPlan(entityClass, FetchPlan.LOCAL)
        def minimal = fetchPlanRepository.getFetchPlan(entityClass, FetchPlan.MINIMAL)

        then:
        local.entityClass == ExtDriver
        minimal.entityClass == ExtDriver
        local.getProperty('info')
        minimal.getProperty('info')

        where:
        entityClass << [ExtDriver, Driver]
    }

    def "shared fetch plan"() {
        when:
        def docEditFP = fetchPlanRepository.getFetchPlan(Doc, 'edit')

        then:
        docEditFP.entityClass == ExtDoc

        when:
        def driverEditFP = fetchPlanRepository.getFetchPlan(Driver, 'driverEdit')

        then:
        driverEditFP.entityClass == ExtDriver
        driverEditFP.getProperty('info')

        when:
        def driverTestEditFP = fetchPlanRepository.getFetchPlan(Driver, 'testEdit')

        then:
        driverTestEditFP.entityClass == ExtDriver
        driverTestEditFP.getProperty('info')
    }
}
