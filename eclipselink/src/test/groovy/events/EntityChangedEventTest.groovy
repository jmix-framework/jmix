/*
 * Copyright (c) 2008-2018 Haulmont.
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
package events

import io.jmix.core.DataManager
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.TestCustomerListener
import test_support.entity.sales.Customer
import test_support.entity.sales.Status

class EntityChangedEventTest extends DataSpec {

    @Autowired
    private DataManager dataManager
    @Autowired
    TestCustomerListener listener

    def "EntityChangedEvent changes contains enum instead of enum's id"() {
        def customer = dataManager.create(Customer)
        customer.name = 'Gomer'
        customer.status = Status.OK
        def customer1 = dataManager.save(customer)

        listener.changedEventConsumer = { event ->
            def oldValue = event.changes.getOldValue('status')
            assert oldValue == null || oldValue instanceof Status
        }

        when:
        customer1.status = Status.NOT_OK
        dataManager.save(customer1)

        then:
        noExceptionThrown()

        cleanup:
        listener.changedEventConsumer = null
    }
}
