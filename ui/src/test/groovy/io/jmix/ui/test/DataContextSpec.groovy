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

package io.jmix.ui.test

import io.jmix.core.AppBeans
import io.jmix.core.EntityStates
import io.jmix.core.JmixCoreConfiguration
import io.jmix.core.TimeSource
import io.jmix.core.commons.db.QueryRunner
import io.jmix.core.entity.*
import io.jmix.data.JmixDataConfiguration
import io.jmix.data.Persistence
import io.jmix.ui.JmixUiConfiguration
import io.jmix.ui.test.entity.TestNullableIdEntity
import io.jmix.ui.test.entity.TestNullableIdItemEntity
import org.eclipse.persistence.internal.queries.EntityFetchGroup
import org.eclipse.persistence.queries.FetchGroupTracker
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import javax.inject.Inject

import static io.jmix.core.impl.StandardSerialization.deserialize
import static io.jmix.core.impl.StandardSerialization.serialize

@ContextConfiguration(classes = [JmixCoreConfiguration, JmixUiConfiguration, JmixDataConfiguration, DataContextTestConfiguration])
class DataContextSpec extends Specification {

    @Inject EntityStates entityStates
    @Inject Persistence persistence

    void setup() {
        persistence.createTransaction().commit()
    }

    void cleanup() {
        TestNullableIdEntity.sequence.set(0L)
        TestNullableIdItemEntity.sequence.set(0L)

        def runner = new QueryRunner(persistence.getDataSource())
        runner.update('delete from TEST_NULLABLE_ID_ITEM_ENTITY')
        runner.update('delete from TEST_NULLABLE_ID_ENTITY')
        runner.update('delete from TEST_JPA_LIFECYCLE_CALLBACKS_ENTITY')
        runner.update('delete from TEST_IDENTITY_ID_ENTITY')
        runner.update('delete from TEST_STRING_ID_ENTITY')
        runner.update('delete from TEST_ORDER_LINE_PARAM')
        runner.update('delete from TEST_ORDER_LINE')
        runner.update('delete from TEST_PRODUCT_TAG_LINK')
        runner.update('delete from TEST_PRODUCT')
        runner.update('delete from TEST_PRODUCT_TAG')
        runner.update('delete from TEST_ORDER')
        runner.update('delete from TEST_CUSTOMER')
        runner.update('delete from SEC_USER_ROLE')
        runner.update('delete from SEC_USER')
        runner.update('delete from SEC_ROLE')
        runner.update('delete from SEC_GROUP')
    }

    void makeDetached(def entity) {
        entityStates.makeDetached(entity)
    }

    void makeDetached(Entity... entities) {
        entities.each { makeDetached(it) }
    }

    void makeDetached(def entity, List<String> attributes) {
        entityStates.makeDetached(entity)
        ((FetchGroupTracker) entity)._persistence_setFetchGroup(
                new EntityFetchGroup(['id', 'version', 'deleteTs'] + attributes))

    }

    @SuppressWarnings("unchecked")
    static <T> T reserialize(Serializable object) {
        if (object == null) {
            return null
        }

        return (T) deserialize(serialize(object))
    }

    static <T extends Serializable> T makeSaved(T entity) {
        EntityStates entityStates = AppBeans.get(EntityStates)
        TimeSource timeSource = AppBeans.get(TimeSource)

        T e = reserialize(entity)
        entityStates.makeDetached((BaseGenericIdEntity) e)

        if (e instanceof Versioned) {
            Versioned versioned = (Versioned) e
            versioned.version = versioned.version ?: 0
            versioned.version++
        }

        if (e instanceof Creatable) {
            Creatable creatable = (Creatable) e
            creatable.setCreateTs(timeSource.currentTimestamp())
            creatable.setCreatedBy("test_user")
        }

        if (e instanceof Updatable) {
            Updatable updatable = (Updatable) e
            updatable.setUpdateTs(timeSource.currentTimestamp())
            if (!entityStates.isNew(entity)) {
                updatable.setUpdatedBy("test_user")
            }
        }

        return e
    }

}
