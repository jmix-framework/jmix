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
import io.jmix.core.Metadata
import io.jmix.core.MetadataTools
import io.jmix.core.event.EntityChangedEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import test_support.DataSpec
import test_support.entity.petclinic.Address
import test_support.entity.petclinic.Owner
import test_support.entity.sales.Customer
import test_support.entity.sales.Status
import test_support.listeners.TestComplexListener
import test_support.listeners.TestCustomerListener

class EntityChangedEventTest extends DataSpec {

    @Autowired
    private DataManager dataManager
    @Autowired
    TestCustomerListener listener
    @Autowired
    private TestComplexListener complexListener;
    @Autowired
    JdbcTemplate jdbc
    @Autowired
    MetadataTools metadataTools
    @Autowired
    Metadata metadata

    void cleanup() {
        complexListener.setConsumer(null)
        jdbc.update("delete from SALES_CUSTOMER")
    }

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

    def "EntityChangeEvent changes contains embedded attribute changes"() {
        setup:
        def owner = dataManager.create(Owner)
        owner.name = "name"
        owner.address = new Address()
        owner.address.city = "Khorinis"
        owner.address.zip = "123"
        owner = dataManager.save(owner)

        println(metadataTools.getDatabaseTable(metadata.getClass(Owner)))

        List<EntityChangedEvent<Owner>> events = []
        complexListener.setConsumer(event -> {
            events.add(event)
        })

        when:
        owner.address.city = "Jarkendar"
        owner = dataManager.save(owner)

        then:
        events.size() == 1
        events[0].entityId.value == owner.id
        events[0].changes.getAttributes().size() == 1
        events[0].changes.getAttributes()[0] == "address.city"

        when:
        events.clear()
        owner.name = "Mark"
        owner.address.zip = "321"
        owner = dataManager.save(owner)
        then:
        events.size() == 1
        events[0].entityId.value == owner.id
        events[0].changes.getAttributes().size() == 2
        events[0].changes.getAttributes().contains("address.zip")
        events[0].changes.getAttributes().contains("name")
    }
}
