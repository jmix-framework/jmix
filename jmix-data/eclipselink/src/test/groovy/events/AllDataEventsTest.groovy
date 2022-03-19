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

package events

import io.jmix.core.DataManager
import io.jmix.core.Metadata
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.DefaultTransactionDefinition
import test_support.DataSpec
import test_support.entity.events.Foo
import test_support.listeners.TestAllDataEventsListener

/**
 * @see TestAllDataEventsListener
 */
class AllDataEventsTest extends DataSpec {

    @Autowired
    private TestAllDataEventsListener listener
    @Autowired
    private DataManager dataManager
    @Autowired
    private Metadata metadata
    @Autowired
    private PlatformTransactionManager txManager

    void setup() {
        TestAllDataEventsListener.clear()

        // warm up EntityLog, etc.
        Foo toDiscard = metadata.create(Foo)
        dataManager.save(toDiscard)

        TestAllDataEventsListener.clear()
    }

    void cleanup() {
        TestAllDataEventsListener.clear()
    }

    def "create sequence"() {

        when:

        Foo foo = metadata.create(Foo)
        foo.name = 'abc'
        foo.amount = 10
        dataManager.save(foo)

        then:

        int i = 0
        listener.allEvents[i++].message == 'EntitySavingEvent: isNew=true'
        listener.allEvents[i++].message == 'JPA PrePersist'
        listener.allEvents[i++].message == 'BeforeInsertEntityListener'
        listener.allEvents[i++].message == 'AfterInsertEntityListener'
        listener.allEvents[i++].message == 'JPA PostPersist'
        listener.allEvents[i++].message == 'BeforeDetachEntityListener'
        listener.allEvents[i++].message == 'EntityChangedEvent: beforeCommit, CREATED'
        listener.allEvents[i++].message == 'BeforeCommitTransactionListener'
        listener.allEvents[i++].message == 'AfterCompleteTransactionListener'
        listener.allEvents[i++].message == 'EntityChangedEvent: afterCommit, CREATED'
        listener.allEvents[i++].message == 'JPA PostLoad'
        listener.allEvents[i++].message == 'BeforeDetachEntityListener'
        listener.allEvents[i++].message == 'EntityLoadingEvent'
    }

    def "update sequence"() {

        Foo foo = metadata.create(Foo)
        foo.name = 'abc'
        foo.amount = 10
        Foo foo1 = dataManager.save(foo)
        listener.clear()

        when:

        foo1.setAmount(100)
        dataManager.save(foo1)

        then:

        int i = 0
        listener.allEvents[i++].message == 'EntitySavingEvent: isNew=false'
        listener.allEvents[i++].message == 'BeforeAttachEntityListener'
        listener.allEvents[i++].message == 'JPA PostLoad'
        listener.allEvents[i++].message == 'BeforeUpdateEntityListener'
        listener.allEvents[i++].message == 'JPA PreUpdate'
        listener.allEvents[i++].message == 'AfterUpdateEntityListener'
        listener.allEvents[i++].message == 'JPA PostUpdate'
        listener.allEvents[i++].message == 'BeforeDetachEntityListener'
        listener.allEvents[i++].message == 'EntityChangedEvent: beforeCommit, UPDATED'
        listener.allEvents[i++].message == 'BeforeCommitTransactionListener'
        listener.allEvents[i++].message == 'AfterCompleteTransactionListener'
        listener.allEvents[i++].message == 'EntityChangedEvent: afterCommit, UPDATED'
        listener.allEvents[i++].message == 'JPA PostLoad'
        listener.allEvents[i++].message == 'BeforeDetachEntityListener'
        listener.allEvents[i++].message == 'EntityLoadingEvent'
    }

    def "delete sequence"() {

        Foo foo = metadata.create(Foo)
        foo.name = 'abc'
        foo.amount = 10
        Foo foo1 = dataManager.save(foo)
        listener.clear()

        when:

        dataManager.remove(foo1)

        then:

        int i = 0
        listener.allEvents[i++].message == 'BeforeAttachEntityListener'
        listener.allEvents[i++].message == 'JPA PostLoad'
        listener.allEvents[i++].message == 'JPA PreRemove'
        listener.allEvents[i++].message == 'BeforeDeleteEntityListener'
        listener.allEvents[i++].message == 'AfterDeleteEntityListener'
        listener.allEvents[i++].message == 'JPA PostRemove'
        listener.allEvents[i++].message == 'BeforeDetachEntityListener'
        listener.allEvents[i++].message == 'EntityChangedEvent: beforeCommit, DELETED'
        listener.allEvents[i++].message == 'BeforeCommitTransactionListener'
        listener.allEvents[i++].message == 'AfterCompleteTransactionListener'
        listener.allEvents[i++].message == 'EntityChangedEvent: afterCommit, DELETED'
    }

    def "create/update/delete in one transaction sequence"() {

        int i = 0
        def txStatus = txManager.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW))

        when:

        Foo foo = metadata.create(Foo)
        foo.name = 'abc'
        foo.amount = 10
        Foo foo1 = dataManager.save(foo)
        i = 0

        then:

        listener.allEvents[i++].message == 'EntitySavingEvent: isNew=true'
        listener.allEvents[i++].message == 'JPA PrePersist'
        listener.allEvents[i++].message == 'BeforeInsertEntityListener'
        listener.allEvents[i++].message == 'AfterInsertEntityListener'
        listener.allEvents[i++].message == 'JPA PostPersist'
        listener.allEvents[i++].message == 'BeforeDetachEntityListener'
        listener.allEvents[i++].message == 'EntityChangedEvent: beforeCommit, CREATED'
        listener.allEvents[i++].message == 'JPA PostLoad'
        listener.allEvents[i++].message == 'BeforeDetachEntityListener'
        listener.allEvents[i++].message == 'EntityLoadingEvent'

        when:

        listener.clear()
        foo1.setAmount(100)
        Foo foo2 = dataManager.save(foo1)
        i = 0

        then:

        listener.allEvents[i++].message == 'EntitySavingEvent: isNew=false'
        listener.allEvents[i++].message == 'BeforeAttachEntityListener'
        listener.allEvents[i++].message == 'JPA PostLoad'
        listener.allEvents[i++].message == 'BeforeUpdateEntityListener'
        listener.allEvents[i++].message == 'JPA PreUpdate'
        listener.allEvents[i++].message == 'AfterUpdateEntityListener'
        listener.allEvents[i++].message == 'JPA PostUpdate'
        listener.allEvents[i++].message == 'BeforeDetachEntityListener'
        listener.allEvents[i++].message == 'EntityChangedEvent: beforeCommit, UPDATED'
        listener.allEvents[i++].message == 'JPA PostLoad'
        listener.allEvents[i++].message == 'BeforeDetachEntityListener'
        listener.allEvents[i++].message == 'EntityLoadingEvent'

        when:

        listener.clear()
        dataManager.remove(foo2)
        i = 0

        then:

        listener.allEvents[i++].message == 'BeforeAttachEntityListener'
        listener.allEvents[i++].message == 'JPA PostLoad'
        listener.allEvents[i++].message == 'JPA PreRemove'
        listener.allEvents[i++].message == 'BeforeDeleteEntityListener'
        listener.allEvents[i++].message == 'AfterDeleteEntityListener'
        listener.allEvents[i++].message == 'JPA PostRemove'
        listener.allEvents[i++].message == 'BeforeDetachEntityListener'
        listener.allEvents[i++].message == 'EntityChangedEvent: beforeCommit, DELETED'

        when:

        listener.clear()
        txManager.commit(txStatus)
        i = 0

        then:

        listener.allEvents[i++].message == 'BeforeCommitTransactionListener'
        listener.allEvents[i++].message == 'AfterCompleteTransactionListener'
        listener.allEvents[i++].message == 'EntityChangedEvent: afterCommit, CREATED'
        listener.allEvents[i++].message == 'EntityChangedEvent: afterCommit, UPDATED'
        listener.allEvents[i++].message == 'EntityChangedEvent: afterCommit, DELETED'

        cleanup:

        if (!txStatus.completed) {
            txManager.rollback(txStatus)
        }
    }

    def "load/update in one transaction sequence"() {

        Foo foo = metadata.create(Foo)
        foo.name = 'abc'
        foo.amount = 10
        dataManager.save(foo)
        listener.clear()

        int i = 0
        def txStatus = txManager.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW))

        when:

        Foo foo1 = dataManager.load(Foo).id(foo.id).one()
        i = 0

        then:

        listener.allEvents[i++].message == 'JPA PostLoad'
        listener.allEvents[i++].message == 'BeforeDetachEntityListener'
        listener.allEvents[i++].message == 'EntityLoadingEvent'

        when:

        listener.clear()
        foo1.setAmount(100)
        dataManager.save(foo1)
        i = 0

        then:

        listener.allEvents[i++].message == 'EntitySavingEvent: isNew=false'
        listener.allEvents[i++].message == 'BeforeAttachEntityListener'
        listener.allEvents[i++].message == 'JPA PostLoad'
        listener.allEvents[i++].message == 'BeforeUpdateEntityListener'
        listener.allEvents[i++].message == 'JPA PreUpdate'
        listener.allEvents[i++].message == 'AfterUpdateEntityListener'
        listener.allEvents[i++].message == 'JPA PostUpdate'
        listener.allEvents[i++].message == 'BeforeDetachEntityListener'
        listener.allEvents[i++].message == 'EntityChangedEvent: beforeCommit, UPDATED'
        listener.allEvents[i++].message == 'JPA PostLoad'
        listener.allEvents[i++].message == 'BeforeDetachEntityListener'
        listener.allEvents[i++].message == 'EntityLoadingEvent'

        when:

        listener.clear()
        txManager.commit(txStatus)
        i = 0

        then:

        listener.allEvents[i++].message == 'BeforeCommitTransactionListener'
        listener.allEvents[i++].message == 'AfterCompleteTransactionListener'
        listener.allEvents[i++].message == 'EntityChangedEvent: afterCommit, UPDATED'

        cleanup:

        if (!txStatus.completed) {
            txManager.rollback(txStatus)
        }
    }

}
