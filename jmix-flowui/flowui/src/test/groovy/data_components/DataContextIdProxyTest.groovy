/*
 * Copyright (c) 2020 Haulmont.
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

package data_components

import io.jmix.core.Metadata
import io.jmix.core.impl.StandardSerialization
import io.jmix.flowui.model.DataComponents
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.sales.Order
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class DataContextIdProxyTest extends FlowuiTestSpecification {

    @Autowired
    DataComponents dataComponents
    @Autowired
    Metadata metadata

    @Autowired
    protected StandardSerialization standardSerialization;

    @SuppressWarnings("GroovyAccessibility")
    def "identity-id entity merged after assigning id generated by database"() {
        def entity = metadata.create(Order)
        entity.number = 'num'

        def dataContext = dataComponents.createDataContext()

        def merged = dataContext.merge(entity)

        when:

        Order serverSideEntity = reserialize(merged)
        serverSideEntity.setId(UUID.fromString('60885987-1b61-4247-94c7-dff348347f93'))
        Order returnedEntity = reserialize(serverSideEntity)

        Order mergedReturnedEntity = dataContext.merge(returnedEntity)

        then:

        mergedReturnedEntity.getId() == UUID.fromString('60885987-1b61-4247-94c7-dff348347f93')
    }

    Order reserialize(Serializable object) {
        if (object == null) {
            return null;
        }
        return standardSerialization.deserialize(standardSerialization.serialize(object)) as Order;
    }
}
