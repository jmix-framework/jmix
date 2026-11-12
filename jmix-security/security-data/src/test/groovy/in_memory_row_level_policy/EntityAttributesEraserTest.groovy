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

package in_memory_row_level_policy

import io.jmix.core.Metadata
import io.jmix.data.impl.JpaEntityAttributesEraser
import org.springframework.beans.factory.annotation.Autowired
import test_support.SecurityDataSpecification
import test_support.entity.OrderInfo
import test_support.entity.TestOrder

class EntityAttributesEraserTest extends SecurityDataSpecification {

    @Autowired
    Metadata metadata

    @Autowired
    JpaEntityAttributesEraser eraser

    def "references are erased in each instance when the same row is loaded as two instances"() {

        given: "two instances of the same OrderInfo row, each one referencing its own order"

        def info1 = metadata.create(OrderInfo)
        info1.order = metadata.create(TestOrder)

        def info2 = metadata.create(OrderInfo)
        info2.id = info1.id
        info2.order = metadata.create(TestOrder)

        when: "reading orders is not permitted"

        def references = eraser.collectErasingReferences([info1, info2], { !(it instanceof TestOrder) })
        eraser.eraseReferences(references)

        then: "the order reference is erased in both instances"

        info1.order == null
        info2.order == null
    }
}
