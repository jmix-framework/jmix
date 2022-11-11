/*
 * Copyright 2022 Haulmont.
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

package test_support.spec

import io.jmix.core.*
import io.jmix.core.entity.EntityEntryAuditable
import io.jmix.core.entity.EntityValues
import io.jmix.core.impl.StandardSerialization
import io.jmix.core.metamodel.model.MetaClass
import org.eclipse.persistence.internal.queries.EntityFetchGroup
import org.eclipse.persistence.queries.FetchGroupTracker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.support.TransactionTemplate
import spock.lang.Specification
import test_support.FlowuiTestConfiguration

@ContextConfiguration(classes = [FlowuiTestConfiguration])
class DataContextSpec extends Specification {

    @Autowired
    EntityStates entityStates
    @Autowired
    TransactionTemplate transaction
    @Autowired
    JdbcTemplate jdbc
    @Autowired
    TimeSource timeSource
    @Autowired
    StandardSerialization standardSerialization
    @Autowired
    Metadata metadata
    @Autowired
    MetadataTools metadataTools

    void setup() {
        transaction.executeWithoutResult {}
    }

    void cleanup() {
        jdbc.update('delete from TEST_NULLABLE_ID_ITEM_ENTITY')
        jdbc.update('delete from TEST_NULLABLE_ID_ENTITY')
        jdbc.update('delete from TEST_JPA_LIFECYCLE_CALLBACKS_ENTITY')
        jdbc.update('delete from TEST_IDENTITY_ID_ENTITY')
        jdbc.update('delete from TEST_STRING_ID_ENTITY')
        jdbc.update('delete from TEST_ORDER_LINE_PARAM')
        jdbc.update('delete from TEST_ORDER_LINE')
        jdbc.update('delete from TEST_PRODUCT_TAG_LINK')
        jdbc.update('delete from TEST_PRODUCT')
        jdbc.update('delete from TEST_PRODUCT_TAG')
        jdbc.update('delete from TEST_ORDER')
        jdbc.update('delete from TEST_CUSTOMER')
        jdbc.update('delete from SEC_USER_ROLE')
        jdbc.update('delete from SEC_USER')
        jdbc.update('delete from SEC_ROLE')
        jdbc.update('delete from SEC_GROUP')
    }

    void makeDetached(def entity) {
        entityStates.makeDetached(entity)
    }

    void makeDetached(Object... entities) {
        entities.each { makeDetached(it) }
    }

    void makeDetached(def entity, List<String> attributes) {
        entityStates.makeDetached(entity)
        ((FetchGroupTracker) entity)._persistence_setFetchGroup(
                new EntityFetchGroup(['id', 'version', 'deleteTs'] + attributes))

    }

    def <T> T reserialize(Serializable object) {
        if (object == null) {
            return null
        }

        return (T) standardSerialization.deserialize(standardSerialization.serialize(object))
    }

    void deleteRecord(String table, String primaryKeyCol, Object... ids) {
        for (Object id : ids) {
            String sql = "delete from " + table + " where " + primaryKeyCol + " = '" + id.toString() + "'";
            try {
                jdbc.update(sql);
            } catch (DataAccessException e) {
                throw new RuntimeException(
                        String.format("Unable to delete record %s from %s", id.toString(), table), e);
            }
        }
    }

    void deleteRecord(Object... entities) {
        if (entities == null) {
            return;
        }

        for (Object entity : entities) {
            if (entity == null) {
                continue;
            }

            MetaClass metaClass = metadata.getClass(entity.getClass());

            String table = metadataTools.getDatabaseTable(metaClass);
            String primaryKey = metadataTools.getPrimaryKeyName(metaClass);
            if (table == null || primaryKey == null) {
                throw new RuntimeException("Unable to determine table or primary key name for " + entity);
            }

            deleteRecord(table, primaryKey, EntityValues.getId(entity));
        }
    }

    def <T extends Serializable> T makeSaved(T entity) {

        T e = reserialize(entity)
        entityStates.makeDetached(e)

        if (EntityValues.isVersionSupported(e)) {
            def version = (Integer) EntityValues.getVersion(e) ?: 0;
            version++
            EntityValues.setVersion(e, version)
        }

        if (e instanceof Entity && e.__getEntityEntry() instanceof EntityEntryAuditable) {
            EntityEntryAuditable entityEntry = ((EntityEntryAuditable) e.__getEntityEntry());

            entityEntry.setCreatedDate(timeSource.currentTimestamp())
            entityEntry.setCreatedBy("test_user")

            entityEntry.setLastModifiedDate(timeSource.currentTimestamp())
            if (!entityStates.isNew(entity)) {
                entityEntry.setLastModifiedBy("test_user")
            }
        }
        return e
    }

}
