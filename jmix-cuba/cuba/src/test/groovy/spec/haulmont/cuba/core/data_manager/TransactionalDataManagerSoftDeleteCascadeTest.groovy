/*
 * Copyright (c) 2008-2019 Haulmont.
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

package spec.haulmont.cuba.core.data_manager

import com.haulmont.cuba.core.model.entitychangedevent.EceTestLogEntry
import com.haulmont.cuba.core.model.entitychangedevent.EceTestProduct
import com.haulmont.cuba.core.model.entitychangedevent.EceTestStock
import com.haulmont.cuba.core.global.DataManager
import com.haulmont.cuba.core.entity.contracts.Id
import com.haulmont.cuba.core.Persistence
import com.haulmont.cuba.core.TransactionalDataManager
import org.springframework.jdbc.core.JdbcTemplate
import spec.haulmont.cuba.core.CoreTestSpecification

import org.springframework.beans.factory.annotation.Autowired

class TransactionalDataManagerSoftDeleteCascadeTest extends CoreTestSpecification {
    @Autowired
    DataManager dm
    @Autowired
    TransactionalDataManager tdm
    @Autowired
    Persistence persistence

    void cleanup() {
        def queryRunner = new JdbcTemplate(persistence.dataSource)
        queryRunner.update('delete from TEST_ECE_STOCK')
        queryRunner.update('delete from TEST_ECE_PRODUCT')
        queryRunner.update('delete from TEST_ECE_LOG')
    }

    def "cascade delete works when there are intermediate saves"() {

        when:

        def product = dm.create(EceTestProduct)
        def product1 = dm.commit(product)

        then:

        def stock = dm.load(EceTestStock)
                .query('select e from test_EceTestStock e where e.product = :product')
                .parameter('product', product)
                .one()
        stock != null

        when:

//        dm.remove(product1)

        def tx = tdm.transactions().create()
        try {
            tdm.remove(product1)

            def logEntry = tdm.create(EceTestLogEntry)
            logEntry.message = "Removed $product1"
            tdm.save(logEntry)

            tx.commit()
        } finally {
            tx.end()
        }

        then:

        !dm.load(Id.of(stock)).optional().isPresent()

    }
}
