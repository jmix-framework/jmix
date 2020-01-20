package spec.haulmont.cuba.core.data_events

import com.haulmont.cuba.core.model.entitychangedevent.EceTestProduct
import com.haulmont.cuba.core.model.entitychangedevent.TestProductChangeListener
import io.jmix.core.*

/*
 * Copyright 2019 Haulmont.
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

import io.jmix.data.Persistence
import org.springframework.jdbc.core.JdbcTemplate
import spec.haulmont.cuba.core.CoreTestSpecification

import javax.inject.Inject

class EntityChangedEventVersionTest extends CoreTestSpecification {

    @Inject
    DataManager dataManager
    @Inject
    Persistence persistence

    void cleanup() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(persistence.dataSource)
        jdbcTemplate.update('delete from TEST_ECE_STOCK')
        jdbcTemplate.update('delete from TEST_ECE_PRODUCT')
        jdbcTemplate.update('delete from TEST_ECE_LOG')
    }

    def "returned instance with latest version"() {

        def product = dataManager.create(EceTestProduct)

        when:

        def product1 = dataManager.commit(product)

        then:

        product1.version == dataManager.load(Id.of(product)).one().version
    }

    def "returned only given instance"() {

        def product = dataManager.create(EceTestProduct)

        when:

        EntitySet committed = dataManager.commit(new CommitContext(product))

        then:

        committed.size() == 1
        committed[0] == product
        committed[0].version == dataManager.load(Id.of(product)).one().version
    }

    def "suspended transaction"() {

        AppBeans.get(TestProductChangeListener).doLog = true

        def product = dataManager.create(EceTestProduct)

        when:

        def product1 = dataManager.commit(product)

        then:

        product1.version == dataManager.load(Id.of(product)).one().version

        cleanup:

        AppBeans.get(TestProductChangeListener).doLog = false
    }
}
