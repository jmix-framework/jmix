import io.jmix.audit.AuditConfiguration
import io.jmix.audit.EntityLog
import io.jmix.audit.entity.EntityLogItem
import io.jmix.audit.entity.LoggedAttribute
import io.jmix.audit.entity.LoggedEntity
import io.jmix.core.CoreConfiguration
import io.jmix.core.JmixEntity
import io.jmix.core.MetadataTools
import io.jmix.core.entity.EntityValues
import io.jmix.data.DataConfiguration
import io.jmix.data.PersistenceTools
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionTemplate
import spock.lang.Specification
import test_support.AuditTestConfiguration

import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.persistence.TypedQuery

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

@ContextConfiguration(classes = [CoreConfiguration, DataConfiguration, AuditConfiguration, AuditTestConfiguration])
class AbstractEntityLogTest extends Specification {
    @Autowired
    protected EntityLog entityLog
    @Autowired
    protected PersistenceTools persistenceTools
    @Autowired
    protected MetadataTools metadataTools
    @Autowired
    protected JdbcTemplate jdbc
    @PersistenceContext
    protected EntityManager em

    protected TransactionTemplate transaction

    @Autowired
    protected void setTransactionManager(PlatformTransactionManager transactionManager) {
        transaction = new TransactionTemplate(transactionManager);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    protected void saveEntityLogAutoConfFor(String entityName, String... attributes) {

        LoggedEntity le = new LoggedEntity(name: entityName, auto: true)
        em.persist(le)

        attributes.each {
            LoggedAttribute la = new LoggedAttribute(entity: le, name: it)
            em.persist(la)
        }

    }

    protected void saveManualEntityLogAutoConfFor(String entityName, String... attributes) {

        LoggedEntity le = new LoggedEntity(name: entityName, auto: false, manual: true)
        em.persist(le)

        attributes.each {
            LoggedAttribute la = new LoggedAttribute(entity: le, name: it)
            em.persist(la)
        }

    }

    protected List<EntityLogItem> getEntityLogItems(String entityName, def entityId) {
        List<EntityLogItem> items
        transaction.executeWithoutResult {
            String entityIdField
            if (entityId instanceof Integer) entityIdField = 'intEntityId'
            else if (entityId instanceof Long) entityIdField = 'longEntityId'
            else if (entityId instanceof String) entityIdField = 'stringEntityId'
            else entityIdField = 'entityId'

            TypedQuery<EntityLogItem> query = em.createQuery(
                    "select i from audit_EntityLog i where i.entity = ?1 and i.entityRef.$entityIdField = ?2 order by i.eventTs desc", EntityLogItem.class)
            query.setParameter(1, entityName)
            query.setParameter(2, entityId)
            items = query.getResultList()
        }
        return items
    }

    protected boolean loggedValueMatches(EntityLogItem entityLogItem, String attributeName, String value) {
        entityLogItem.attributes.find { it.name == attributeName }.value == value
    }

    protected boolean loggedOldValueMatches(EntityLogItem entityLogItem, String attributeName, String oldValue) {
        entityLogItem.attributes.find { it.name == attributeName }.oldValue == oldValue
    }

    protected EntityLogItem getLatestEntityLogItem(String entityName, def entityId) {
        getEntityLogItems(entityName, entityId).first()
    }

    protected EntityLogItem getLatestEntityLogItem(String entityName, JmixEntity entity) {
        Object id = EntityValues.getId(entity);
        getEntityLogItems(entityName, id).first()
    }

    protected void clearTable(EntityManager em, String tableName) {
        em.createNativeQuery("delete from " + tableName).executeUpdate()
    }


    protected withTransaction(Closure run) {

        transaction.executeWithoutResult({ status ->
            run.call(em)
        })
    }

    protected void runSqlUpdate(String sqlUpdateString) {
        transaction.executeWithoutResult({ status ->
            jdbc.update(sqlUpdateString)
        })
    }

    void clearTables(String... tableNames) {
        tableNames.each {
            clearTable(it)
        }
    }

    protected void clearTable(String tableName) {
        //runSqlUpdate("CREATE TABLE IF NOT EXISTS $tableName")
        runSqlUpdate("delete from $tableName")
    }

    protected void initEntityLogAPI() {
        entityLog.invalidateCache()
    }
}
