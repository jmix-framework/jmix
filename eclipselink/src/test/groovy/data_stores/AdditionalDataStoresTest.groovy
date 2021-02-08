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

package data_stores

import io.jmix.core.DataManager
import io.jmix.core.Id
import io.jmix.core.Metadata
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.JdbcTemplate
import test_support.DataSpec
import test_support.entity.multidb.Db1Customer
import test_support.entity.multidb.Mem1Customer

class AdditionalDataStoresTest extends DataSpec {

    @Autowired
    Metadata metadata

    @Autowired
    DataManager dataManager

    @Autowired
    JdbcTemplate jdbc

    @Autowired @Qualifier("db1JdbcTemplate")
    JdbcTemplate db1Jdbc

    def "meta-classes have correct store descriptors"() {
        when:
        def dbCustMetaClass = metadata.getClass(Db1Customer)

        then:
        dbCustMetaClass.getStore().name == 'db1'
        dbCustMetaClass.getStore().descriptor.beanName == 'eclipselink_JpaDataStore'
        dbCustMetaClass.getStore().descriptor.jpa

        when:
        def memCustMetaClass = metadata.getClass(Mem1Customer)

        then:
        memCustMetaClass.getStore().name == 'mem1'
        memCustMetaClass.getStore().descriptor.beanName == 'test_InMemoryDataStore'
        !memCustMetaClass.getStore().descriptor.jpa
    }

    def "save and load in additional store"() {
        def customer = new Db1Customer(name: 'cust1')

        when:
        dataManager.save(customer)

        then:
        db1Jdbc.queryForList('select * from CUSTOMER where ID = ?', customer.getId()).size() == 1

        when:
        def loadedCustomer = dataManager.load(Id.of(customer)).one()

        then:
        loadedCustomer == customer

        cleanup:
        db1Jdbc.update('delete from CUSTOMER where ID = ?', customer.getId())
    }
}
