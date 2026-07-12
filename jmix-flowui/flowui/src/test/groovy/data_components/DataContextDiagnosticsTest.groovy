/*
 * Copyright 2026 Haulmont.
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
import io.jmix.flowui.model.impl.DataContextDiagnostics
import org.springframework.beans.factory.annotation.Autowired
import test_support.entity.sales.Customer
import test_support.spec.DataContextSpec

class DataContextDiagnosticsTest extends DataContextSpec {

    @Autowired
    Metadata metadata

    def "messages carry entity, attribute, values"() {
        given:
        Customer customer = metadata.create(Customer)

        expect:
        DataContextDiagnostics.attributeDirtied(customer, 'name', 'a', 'b')
                .contains("Customer-${customer.id}")
        DataContextDiagnostics.attributeDirtied(customer, 'name', 'a', 'b').contains("'a' -> 'b'")
        DataContextDiagnostics.attributeReverted(customer, 'name').contains('reverted')
        DataContextDiagnostics.mergeSkippedDirty(customer, 'name').contains('kept user value')
        DataContextDiagnostics.formatEntity(null) == 'null'
        DataContextDiagnostics.formatEntity('x') == 'x'
        DataContextDiagnostics.formatEntity(customer) == "Customer-${customer.id}"
    }
}
