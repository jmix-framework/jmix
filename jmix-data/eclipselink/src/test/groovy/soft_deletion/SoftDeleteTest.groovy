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

package soft_deletion


import io.jmix.core.Id
import io.jmix.core.LoadContext
import io.jmix.core.Metadata
import io.jmix.core.SaveContext
import io.jmix.core.Stores
import io.jmix.core.ValueLoadContext
import io.jmix.data.PersistenceHints
import io.jmix.data.StoreAwareLocator
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.DefaultTransactionDefinition
import test_support.entity.TestAppEntity
import io.jmix.core.DataManager
import io.jmix.core.EntityStates
import test_support.DataSpec

import org.springframework.beans.factory.annotation.Autowired
import test_support.entity.soft_delete.sd_restore.Group
import test_support.entity.soft_delete.sd_restore.Student

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext

import static org.assertj.core.api.Assertions.assertThat

class SoftDeleteTest extends DataSpec {

    @Autowired
    DataManager dataManager

    @Autowired
    Metadata metadata

    @Autowired
    EntityStates entityStates

    @Autowired
    StoreAwareLocator storeAwareLocator

    @PersistenceContext
    EntityManager entityManager

    def "load deleted entity with filter by soft delete"() {
        def entity
        setup:

        entity = dataManager.create(TestAppEntity)
        entity.name = 'e1'
        entity = dataManager.save(entity)
        dataManager.remove(entity)

        when:

        def entity2 = dataManager.load(TestAppEntity).id(entity.id).optional().orElse(null)

        then:

        entity2 == null
    }

    def "load deleted entity with disabled filter by soft delete"() {
        def entity
        setup:

        entity = dataManager.create(TestAppEntity)
        entity.name = 'e1'
        entity = dataManager.save(entity)
        dataManager.remove(entity)

        when:

        def entity2 = dataManager.load(TestAppEntity).id(entity.id).hint(PersistenceHints.SOFT_DELETION, false)
                .optional().orElse(null)

        then:

        entity2 != null
    }

    def "disabling soft deletion for query should affect current query only"() {
        setup:
        var commonGroup = dataManager.create(Group)
        commonGroup.name = "group one"
        var dummyGroup = dataManager.create(Group)
        dummyGroup.name = "dummy group"
        var studentOne = dataManager.create(Student)
        studentOne.name = "First"
        studentOne.group = commonGroup
        var studentTwo = dataManager.create(Student)
        studentTwo.name = "Second"
        studentTwo.group = commonGroup

        dataManager.save(dummyGroup, commonGroup, studentOne, studentTwo)
        dataManager.remove(studentTwo)

        when:

        PlatformTransactionManager transactionManager = storeAwareLocator.getTransactionManager(Stores.MAIN)
        TransactionStatus tx = transactionManager.getTransaction(new DefaultTransactionDefinition())
        try {
            var studentsBefore = dataManager.load(Student.class).all().list()

            assertThat(studentsBefore.size()).isEqualTo(1)

            dataManager.load(Student).all().hint(PersistenceHints.SOFT_DELETION, false).list()
            checkSoftDeletionState()

            dataManager.load(Id.of(studentTwo)).hint(PersistenceHints.SOFT_DELETION, false).one()
            checkSoftDeletionState()

            dataManager.getCount(new LoadContext<>(metadata.getClass(Student)).setHint(PersistenceHints.SOFT_DELETION, false))
            checkSoftDeletionState()

            var names = dataManager.loadValues("select e.name from testsd_Student e")
                    .hint(PersistenceHints.SOFT_DELETION, false)
                    .list()
            assertThat(names.size()).isEqualTo(2)
            checkSoftDeletionState()

            var vlc = new ValueLoadContext().setHint(PersistenceHints.SOFT_DELETION, false)
            vlc.setQueryString("select e.name from testsd_Student e")
            assertThat(dataManager.getCount(vlc)).isEqualTo(2L)
            checkSoftDeletionState()

            dataManager.save(new SaveContext().removing(dummyGroup).setHint(PersistenceHints.SOFT_DELETION, false))
            checkSoftDeletionState()

            transactionManager.commit(tx);
        } catch (Exception e) {
            transactionManager.rollback(tx);
        }

        then:
        noExceptionThrown()

        cleanup:
        jdbc.update("delete from TESTSD_STUDENT")
        jdbc.update("delete from TESTSD_GROUP")
    }

    private void checkSoftDeletionState() {
        assertThat(entityManager.getProperties().get(PersistenceHints.SOFT_DELETION)).isNotEqualTo(false)

        var studentsAfter = entityManager.createQuery("select e from testsd_Student e", Student.class)
                .getResultList();
        assertThat(studentsAfter.size()).isEqualTo(1)
    }
}
