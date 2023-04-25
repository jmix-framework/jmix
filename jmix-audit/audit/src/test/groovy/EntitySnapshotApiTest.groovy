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


import io.jmix.audit.entity.EntitySnapshot
import io.jmix.audit.snapshot.EntitySnapshotManager
import io.jmix.audit.snapshot.model.EntitySnapshotModel
import io.jmix.core.Entity
import io.jmix.core.FetchPlan
import io.jmix.core.FetchPlanRepository
import io.jmix.core.security.InMemoryUserRepository
import io.jmix.core.security.SystemAuthenticator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.userdetails.User
import test_support.testmodel.IdentityEntity

import jakarta.persistence.TypedQuery

class EntitySnapshotApiTest extends AbstractEntityLogTest {


    @Autowired
    private EntitySnapshotManager snapshotApi

    @Autowired
    private FetchPlanRepository fetchPlanRepository

    @Autowired
    private AuthenticationManager authenticationManager

    @Autowired
    private InMemoryUserRepository inMemoryUserRepository

    @Autowired
    protected SystemAuthenticator authenticator;


    void setup() {
        authenticator.begin()
        def admin = User.builder()
                .username('admin')
                .password('{noop}admin')
                .authorities(Collections.emptyList())
                .build()
        inMemoryUserRepository.addUser(admin)
    }

    void cleanup() {
        clearTable("AUDIT_ENTITY_SNAPSHOT")
        authenticator.end()
    }

    def "Get last EntitySnapshot for the Entity"() {

        given:
        def entity = new IdentityEntity()
        Date date = new Date(100)
        entity.setName('testRole')
        def identityEntityMetaClass = metadata.findClass(entity.getClass())
        saveEntity(entity)

        when:
        // create first snapshot
        FetchPlan fetchPlan = fetchPlanRepository.getFetchPlan(IdentityEntity.class, FetchPlan.LOCAL)
        EntitySnapshotModel snapshot = null;
        authenticator.withSystem {
            snapshot = snapshotApi.createSnapshot(entity, fetchPlan) // used current date
        }
        // change entity and create last snapshot
        entity.setName('lastRole')
        authenticator.withSystem {
            snapshotApi.createSnapshot(entity, fetchPlan, date)
        }
        then:
        // it should return snapshot by the last date
        def lastSnapshot = snapshotApi.getLastEntitySnapshot(entity)
        snapshot.getCustomProperties().get("databaseId") == lastSnapshot.getCustomProperties().get("databaseId")

        def lastSnapshot1 = snapshotApi.getLastEntitySnapshot(identityEntityMetaClass, entity.getId())
        snapshot.getCustomProperties().get("databaseId") == lastSnapshot1.getCustomProperties().get("databaseId")

        // cases for non persistence entity
        when:
        def nonPersistRole = metadata.create(IdentityEntity)
        nonPersistRole.setName("nonPersistenceRole")


        authenticator.withSystem {
            snapshotApi.createSnapshot(nonPersistRole, fetchPlan) // used current  date
        }
        nonPersistRole.setName("changedNonPersistenceRole")
        authenticator.withSystem {
            snapshotApi.createSnapshot(nonPersistRole, fetchPlan, date)
        }
        then:
        def snapshot1 = snapshotApi.getLastEntitySnapshot(nonPersistRole)
        snapshot1.getSnapshotXml().contains("nonPersistenceRole") == true

        def snapshot2 = snapshotApi.getLastEntitySnapshot(identityEntityMetaClass, nonPersistRole.getId())
        snapshot2.getSnapshotXml().contains("nonPersistenceRole") == true
    }

    def "Create non-persistent snapshot"() {
        given:
        FetchPlan fetchPlan = fetchPlanRepository.getFetchPlan(IdentityEntity.class, FetchPlan.LOCAL)
        Date snapshotDate = new Date(100)
        def entity = metadata.create(IdentityEntity.class)
        entity.setName('testRole')
        def entityMetaClass = metadata.findClass(entity.getClass())
        saveEntity(entity)

        when:
        EntitySnapshotModel snapshot
        authenticator.withSystem{
            snapshot = snapshotApi.createTempSnapshot(entity, fetchPlan)
        }
        snapshot.getSnapshotXml().contains('testRole') == true
        then:
        def items = getSnapshotsList()
        items.size() == 0

        when:
        EntitySnapshotModel snapshot2
        authenticator.withSystem {
            snapshot2 = snapshotApi.createTempSnapshot(entity, fetchPlan, snapshotDate)
        }
        snapshot2.getSnapshotXml().contains('testRole') == true
        then:
        def snapshots2 = snapshotApi.getSnapshots(entityMetaClass, entity.getId())

        snapshots2.size() == 0
        snapshot2.getSnapshotDate() == snapshotDate

        when:
        EntitySnapshotModel snapshot3
        authenticator.withSystem {
            snapshot3 = snapshotApi.createTempSnapshot(entity, fetchPlan, snapshotDate, "admin")
        }
        snapshot3.getSnapshotXml().contains('testRole') == true
        then:
        def snapshots3 = snapshotApi.getSnapshots(entityMetaClass, entity.getId())

        snapshots3.size() == 0
        snapshot3.getAuthorUsername() == "admin"
    }

    private List<EntitySnapshot> getSnapshotsList() {
        List<EntitySnapshot> entitySnapshotList
        transaction.executeWithoutResult {
            TypedQuery<EntitySnapshot> query = em.createQuery(
                    'select e from audit_EntitySnapshot e', EntitySnapshot.class)
            entitySnapshotList = query.getResultList()
        }
        return entitySnapshotList
    }

    private saveEntity(Entity entity) {
        withTransaction {
            em.persist(entity)
        }
    }
}