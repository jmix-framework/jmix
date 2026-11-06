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

package io.jmix.search.index

import spock.lang.Specification

class IndexOperationResultTest extends Specification {

    def "the row carries everything needed to report it on its own"() {
        when:
        def r = new IndexOperationResult<>("demo_Order", "order_index", "tenant_1", IndexManipulationResult.SUCCESS)

        then:
        r.entityName() == "demo_Order"
        r.indexName() == "order_index"
        r.tenantId() == "tenant_1"
        r.result() == IndexManipulationResult.SUCCESS
    }

    def "tenantId is null for an index that is not tenant-specific"() {
        when:
        def r = new IndexOperationResult<>("demo_Order", "order_index", null, IndexManipulationResult.SUCCESS)

        then:
        r.tenantId() == null
    }

    def "isSuccess delegates to the atomic result"() {
        expect:
        new IndexOperationResult<>("demo_Order", "idx", tenantId, result).isSuccess() == expected

        where:
        result                          | tenantId   || expected
        IndexManipulationResult.SUCCESS | null       || true
        IndexManipulationResult.FAILURE | null       || false
        IndexManipulationResult.SUCCESS | "tenant_1" || true
        IndexManipulationResult.FAILURE | "tenant_1" || false
    }

    def "isSuccess follows the meaning of the status enum"() {
        expect:
        new IndexOperationResult<>("demo_Order", "idx", null, status).isSuccess() == expected

        where:
        status                              || expected
        IndexSynchronizationStatus.ACTUAL   || true
        IndexSynchronizationStatus.CREATED  || true
        IndexSynchronizationStatus.MISSING  || false
        IndexValidationStatus.ACTUAL        || true
        IndexValidationStatus.IRRELEVANT    || false
        IndexRecreationStatus.SUCCESS       || true
    }

}
