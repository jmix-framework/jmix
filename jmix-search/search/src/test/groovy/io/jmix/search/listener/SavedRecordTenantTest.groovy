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

package io.jmix.search.listener

import io.jmix.search.index.impl.MultitenancyAdapter
import spock.lang.Specification

/**
 * Where the tenant written into a queue item comes from.
 *
 * <p>The listener asks {@link MultitenancyAdapter} rather than reading the {@code @TenantId} attribute itself.
 * Two answers to the same question drift apart sooner or later, and the adapter is the one that knows whether the
 * application manages tenants at all: without the add-on it reports none, and a queue item must not then carry a
 * tenant the rest of the module does not believe in.
 *
 * <p>The guard matters as much as the read. An instance the application loaded narrowly may not carry the tenant
 * attribute at all, and the add-on throws when asked for what it cannot read.
 */
class SavedRecordTenantTest extends Specification {

    def multitenancyAdapter = Mock(MultitenancyAdapter)

    def listener = new EntityTrackingListener(multitenancyAdapter: multitenancyAdapter)

    def "the tenant of a saved record is the one the adapter reports"() {
        given:
        def record = new Object()
        multitenancyAdapter.isTenantIdReadable(record) >> true
        multitenancyAdapter.getTenantIdForInstance(record) >> "acme"

        expect:
        listener.tenantOfSavedRecord(record) == "acme"
    }

    def "a record that belongs to no tenant carries none"() {
        given:
        def record = new Object()
        multitenancyAdapter.isTenantIdReadable(record) >> true
        multitenancyAdapter.getTenantIdForInstance(record) >> null

        expect: "this is also what an application without the add-on gets for every record"
        listener.tenantOfSavedRecord(record) == null
    }

    def "a record whose tenant cannot be read is not asked for it"() {
        given:
        def record = new Object()
        multitenancyAdapter.isTenantIdReadable(record) >> false

        when:
        def tenantId = listener.tenantOfSavedRecord(record)

        then: "the guard is what keeps the add-on from throwing on an attribute that was never fetched"
        tenantId == null
        0 * multitenancyAdapter.getTenantIdForInstance(_)
    }
}
