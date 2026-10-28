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

package io.jmix.multitenancy

import io.jmix.core.EntityStates
import io.jmix.core.Metadata
import io.jmix.core.MetadataTools
import io.jmix.core.metamodel.model.MetaClass
import io.jmix.core.metamodel.model.MetaProperty
import io.jmix.multitenancy.core.TenantEntityOperation
import io.jmix.multitenancy.core.TenantProvider
import io.jmix.multitenancy.entity.Tenant
import io.jmix.multitenancy.service.impl.TenantLoader
import spock.lang.Specification

/**
 * The questions the add-on answers to anyone who asks: which tenants exist, whose a user is, whose a record is.
 *
 * <p>Two rules are worth pinning here, and they point in opposite directions. Asked about a <em>user</em>, the
 * facade turns the {@code no_tenant} sentinel into {@code null}: the sentinel is how an SPI implementation says
 * "none", and it must not travel further. Asked about a <em>record</em>, it leaves the value alone - the
 * attribute never holds the sentinel by accident, so a record whose attribute does hold that string belongs to a
 * tenant that has been named so, and translating it would move the data into the shared scope.
 *
 * <p>The other rule is about not answering: an entity with no tenant attribute is a mistake in the calling code,
 * while an attribute that was not fetched is a state of the runtime that the caller can fix. Different types, so
 * that a {@code catch} written for the first does not swallow the second.
 */
class MultitenancyTest extends Specification {

    static final String TENANT_PROPERTY = "tenantId"

    def metadata = Mock(Metadata)
    def metadataTools = Mock(MetadataTools)
    def entityStates = Mock(EntityStates)
    def tenantLoader = Mock(TenantLoader)
    def tenantProvider = Mock(TenantProvider)
    def tenantEntityOperation = Mock(TenantEntityOperation)

    def multitenancy = new Multitenancy(
            metadata: metadata,
            metadataTools: metadataTools,
            entityStates: entityStates,
            tenantLoader: tenantLoader,
            tenantProvider: tenantProvider,
            tenantEntityOperation: tenantEntityOperation)

    def "the tenants of the application are the ids of the registered ones"() {
        given:
        tenantLoader.findAllWithTenantId() >> [tenant("acme"), tenant("globex")]

        expect:
        multitenancy.getAvailableTenants() == ["acme", "globex"] as Set
    }

    def "an application with no tenants registered has none"() {
        given:
        tenantLoader.findAllWithTenantId() >> []

        expect:
        multitenancy.getAvailableTenants().isEmpty()
    }

    def "asked about a user, the sentinel does not travel further"() {
        given:
        tenantProvider.getCurrentUserTenantId() >> providedValue

        expect: "'no tenant' is what the SPI says; the caller hears null"
        multitenancy.getCurrentUserTenantId() == expected

        where:
        providedValue           | expected
        TenantProvider.NO_TENANT | null
        "acme"                   | "acme"
    }

    def "the same holds for a named user"() {
        given:
        def user = Stub(org.springframework.security.core.userdetails.UserDetails)
        tenantProvider.getTenantIdForUser(user) >> TenantProvider.NO_TENANT

        expect:
        multitenancy.getTenantIdForUser(user) == null
    }

    def "asked about a record, the value is left alone"() {
        given: "a tenant that has been named 'no_tenant' is a tenant like any other"
        def record = recordWithLoadedTenant(TenantProvider.NO_TENANT)

        expect: "translating it here would move the data of that tenant into the shared scope"
        multitenancy.getTenantIdForEntity(record) == TenantProvider.NO_TENANT
    }

    def "an entity with no tenant attribute is a mistake in the calling code"() {
        given:
        def record = entity()
        metadataTools.findTenantIdProperty(_ as MetaClass) >> null

        when:
        multitenancy.getTenantIdForEntity(record)

        then: "fixed by changing the call, not by loading anything"
        thrown(IllegalArgumentException)
    }

    def "an attribute that was not fetched is a state the caller can fix"() {
        given:
        def record = entity()
        metadataTools.findTenantIdProperty(_ as MetaClass) >> tenantProperty()
        entityStates.isLoaded(record, TENANT_PROPERTY) >> false

        when:
        multitenancy.getTenantIdForEntity(record)

        then: "a separate type, so that a catch written for the mistake does not swallow this one"
        def e = thrown(TenantIdNotLoadedException)
        !(e instanceof IllegalArgumentException)
    }

    def "the guard tells both ways of not answering from an answer"() {
        given:
        def record = entity()
        metadataTools.findTenantIdProperty(_ as MetaClass) >> property
        entityStates.isLoaded(record, TENANT_PROPERTY) >> loaded

        expect:
        multitenancy.isTenantIdReadable(record) == available

        where:
        property          | loaded || available
        null              | true   || false
        tenantProperty()  | false  || false
        tenantProperty()  | true   || true
    }

    protected static Tenant tenant(String tenantId) {
        def tenant = new Tenant()
        tenant.setTenantId(tenantId)
        return tenant
    }

    /**
     * A where-block is evaluated before the fixture, so this cannot be a Spock mock. Groovy's map coercion is
     * enough: nothing but the name is ever asked of it.
     */
    protected static MetaProperty tenantProperty() {
        return [getName: { TENANT_PROPERTY }] as MetaProperty
    }

    protected Object entity() {
        def record = new Tenant()
        metadata.getClass(record) >> Stub(MetaClass)
        return record
    }

    protected Object recordWithLoadedTenant(String value) {
        def record = entity()
        metadataTools.findTenantIdProperty(_ as MetaClass) >> tenantProperty()
        entityStates.isLoaded(record, TENANT_PROPERTY) >> true
        tenantEntityOperation.getTenant(record) >> value
        return record
    }
}
