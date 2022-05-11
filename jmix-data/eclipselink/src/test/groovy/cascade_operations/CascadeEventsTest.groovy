/*
 * Copyright 2021 Haulmont.
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

package cascade_operations


import io.jmix.core.DataManager
import io.jmix.core.FetchPlan
import io.jmix.core.FetchPlans
import io.jmix.core.SaveContext
import io.jmix.data.PersistenceHints
import io.jmix.data.impl.EntityListenerManager
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.cascade_operations.JpaCascadeBar
import test_support.entity.cascade_operations.JpaCascadeEmbeddable
import test_support.entity.cascade_operations.JpaCascadeFoo
import test_support.entity.cascade_operations.JpaCascadeItem
import test_support.listeners.cascade_operations.TestCascadeBarEventListener
import test_support.listeners.cascade_operations.TestCascadeFooEventListener
import test_support.listeners.cascade_operations.TestCascadeItemEventListener

import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

class CascadeEventsTest extends DataSpec {

    @Autowired
    private EntityListenerManager entityListenerManager

    @Autowired
    private DataManager dataManager

    @PersistenceContext
    private EntityManager entityManager

    @Autowired
    private FetchPlans fetchPlans;

    def setup() {
        entityListenerManager.addListener(JpaCascadeBar, TestCascadeBarEventListener.class)
        entityListenerManager.addListener(JpaCascadeItem, TestCascadeItemEventListener.class)
        entityListenerManager.addListener(JpaCascadeFoo, TestCascadeFooEventListener.class)
    }

    def "check OneToOne cascade operations events"() {
        when: "cascade persist occurs"
        def foo = dataManager.create(JpaCascadeFoo)
        foo.name = "testFoo"

        def bar = dataManager.create(JpaCascadeBar)
        bar.name = "testBar"

        foo.setBar(bar)

        dataManager.save(foo)


        def barChangedEvents = TestCascadeBarEventListener.allEvents
        def fooChangedEvents = TestCascadeFooEventListener.allEvents

        then: "All events present for cascade-persisted entity"
        barChangedEvents.size() == fooChangedEvents.size()

        barChangedEvents.stream().anyMatch(info -> info.message == "AfterInsertEntityListener")
        barChangedEvents.stream().anyMatch(info -> info.message == "BeforeInsertEntityListener")
        barChangedEvents.stream().anyMatch(info -> info.message == "BeforeDetachEntityListener")

        barChangedEvents.stream().anyMatch(info -> info.message == "EntitySavingEvent: isNew=true")
        barChangedEvents.stream().anyMatch(info -> info.message == "EntityChangedEvent: beforeCommit, CREATED")
        barChangedEvents.stream().anyMatch(info -> info.message == "EntityChangedEvent: afterCommit, CREATED")

        barChangedEvents.stream().anyMatch(info -> info.message == "EntityLoadingEvent")


        when: "cascade update occurs"
        def savedFoo = dataManager.load(JpaCascadeFoo.class)
                .id(foo.id)
                .fetchPlan(fetchPlans.builder(JpaCascadeFoo.class)
                        .add("name")
                        .add("bar", FetchPlan.LOCAL)
                        .build())
                .one()


        TestCascadeFooEventListener.clear()
        TestCascadeBarEventListener.clear()

        savedFoo.bar.name = savedFoo.bar.name + "_UPD1"
        savedFoo.name = savedFoo.name + "_UPD1"

        dataManager.save(savedFoo)

        barChangedEvents = TestCascadeBarEventListener.allEvents
        fooChangedEvents = TestCascadeFooEventListener.allEvents

        then: "All events present for cascade-persisted entity"
        barChangedEvents.size() == fooChangedEvents.size()

        barChangedEvents.stream().anyMatch(info -> info.message == "EntitySavingEvent: isNew=false")
        barChangedEvents.stream().anyMatch(info -> info.message == "BeforeAttachEntityListener")
        barChangedEvents.stream().anyMatch(info -> info.message == "BeforeUpdateEntityListener")
        barChangedEvents.stream().anyMatch(info -> info.message == "AfterUpdateEntityListener")
        barChangedEvents.stream().anyMatch(info -> info.message == "BeforeDetachEntityListener")

        barChangedEvents.stream().anyMatch(info -> info.message == "EntityChangedEvent: beforeCommit, UPDATED")
        barChangedEvents.stream().anyMatch(info -> info.message == "EntityChangedEvent: afterCommit, UPDATED")

        barChangedEvents.stream().anyMatch(info -> info.message == "EntityLoadingEvent")


        when: "cascade delete occurs"

        def updatedFoo = dataManager.load(JpaCascadeFoo.class)
                .id(foo.id)
                .fetchPlan(fetchPlans.builder(JpaCascadeFoo.class)
                        .add("name")
                        //.add("bar", FetchPlan.LOCAL)//do not load bar to check that entities will be removed correctly even if not fetched
                        .build())
                .one()

        TestCascadeFooEventListener.clear()
        TestCascadeBarEventListener.clear()

        dataManager.save(new SaveContext().removing(updatedFoo).setHint(PersistenceHints.SOFT_DELETION, false))

        barChangedEvents = TestCascadeBarEventListener.allEvents
        fooChangedEvents = TestCascadeFooEventListener.allEvents

        then: "All events present for cascade-deleted entity"

        barChangedEvents.size() == 6
        //entity should be loaded to be deleted with all events, entity log records e.t.c.
        barChangedEvents.stream().anyMatch(info -> info.message == "EntityLoadingEvent")

        barChangedEvents.stream().anyMatch(info -> info.message == "BeforeDeleteEntityListener")
        barChangedEvents.stream().anyMatch(info -> info.message == "AfterDeleteEntityListener")

        barChangedEvents.stream().anyMatch(info -> info.message == "EntityChangedEvent: beforeCommit, DELETED")
        barChangedEvents.stream().anyMatch(info -> info.message == "EntityChangedEvent: afterCommit, DELETED")

    }

    def "check OneToMany cascade operations events"() {
        when: "cascade persist occurs"
        def foo = dataManager.create(JpaCascadeFoo)
        foo.name = "testFoo"

        def item1 = dataManager.create(JpaCascadeItem)
        item1.name = "testItem1"
        item1.foo = foo

        def item2 = dataManager.create(JpaCascadeItem)
        item2.name = "testItem2"
        item2.foo = foo

        foo.setItems(Arrays.asList(item1, item2))

        dataManager.save(foo)


        def itemChangedEvents = TestCascadeItemEventListener.allEvents
        def fooChangedEvents = TestCascadeFooEventListener.allEvents

        then: "All events present for each cascade-persisted entity"
        itemChangedEvents.size() == fooChangedEvents.size() * 2 //the same events for each cascade-saved item
        itemChangedEvents.stream().filter(info -> info.message == "AfterInsertEntityListener").count() == 2
        itemChangedEvents.stream().filter(info -> info.message == "BeforeInsertEntityListener").count() == 2
        itemChangedEvents.stream().filter(info -> info.message == "BeforeDetachEntityListener").count() == 4


        itemChangedEvents.stream().filter(info -> info.message == "EntitySavingEvent: isNew=true").count() == 2
        itemChangedEvents.stream().filter(info -> info.message == "EntityChangedEvent: beforeCommit, CREATED").count() == 2
        itemChangedEvents.stream().filter(info -> info.message == "EntityChangedEvent: afterCommit, CREATED").count() == 2
        itemChangedEvents.stream().filter(info -> info.message == "EntityLoadingEvent").count() == 2


        when: "cascade update occurs"
        def savedFoo = dataManager.load(JpaCascadeFoo.class)
                .id(foo.id)
                .fetchPlan(fetchPlans.builder(JpaCascadeFoo.class)
                        .add("name")
                        .add("items", FetchPlan.LOCAL)
                        .build())
                .one()


        TestCascadeFooEventListener.clear()
        TestCascadeItemEventListener.clear()

        savedFoo.items[0].name = savedFoo.items[0].name + "_UPD1_0"
        savedFoo.items[1].name = savedFoo.items[1].name + "_UPD1_1"
        savedFoo.name = savedFoo.name + "_UPD1"

        dataManager.save(savedFoo)

        itemChangedEvents = TestCascadeItemEventListener.allEvents
        fooChangedEvents = TestCascadeFooEventListener.allEvents

        then: "All events present for each cascade-updated entity"
        itemChangedEvents.size() == fooChangedEvents.size() * 2
        itemChangedEvents.stream().filter(info -> info.message == "AfterUpdateEntityListener").count() == 2
        itemChangedEvents.stream().filter(info -> info.message == "BeforeUpdateEntityListener").count() == 2
        itemChangedEvents.stream().filter(info -> info.message == "BeforeDetachEntityListener").count() == 4

        itemChangedEvents.stream().filter(info -> info.message == "EntitySavingEvent: isNew=false").count() == 2
        itemChangedEvents.stream().filter(info -> info.message == "EntityChangedEvent: beforeCommit, UPDATED").count() == 2
        itemChangedEvents.stream().filter(info -> info.message == "EntityChangedEvent: afterCommit, UPDATED").count() == 2
        itemChangedEvents.stream().filter(info -> info.message == "EntityLoadingEvent").count() == 2


        when: "cascade delete occurs"
        def updatedFoo = dataManager.load(JpaCascadeFoo.class)
                .id(foo.id)
                .fetchPlan(fetchPlans.builder(JpaCascadeFoo.class)
                        .add("name")
                        .add("items", FetchPlan.LOCAL)
                        .build())
                .one()

        TestCascadeFooEventListener.clear()
        TestCascadeItemEventListener.clear()

        dataManager.save(new SaveContext().removing(updatedFoo).setHint(PersistenceHints.SOFT_DELETION, false))

        itemChangedEvents = TestCascadeItemEventListener.allEvents
        fooChangedEvents = TestCascadeFooEventListener.allEvents

        then: "All events present for each cascade-deleted entity"

        itemChangedEvents.size() == 8
        itemChangedEvents.stream().filter(info -> info.message == "AfterDeleteEntityListener").count() == 2
        itemChangedEvents.stream().filter(info -> info.message == "BeforeDeleteEntityListener").count() == 2

        itemChangedEvents.stream().filter(info -> info.message == "EntityChangedEvent: beforeCommit, DELETED").count() == 2
        itemChangedEvents.stream().filter(info -> info.message == "EntityChangedEvent: afterCommit, DELETED").count() == 2
    }

    def "check cascade events for embedded entities"() {
        when: "cascade persist occurs"
        def foo = dataManager.create(JpaCascadeFoo)
        foo.name = "testFoo"

        def bar = dataManager.create(JpaCascadeBar)
        bar.name = "testBar"

        foo.setEmbeddable(dataManager.create(JpaCascadeEmbeddable))
        foo.getEmbeddable().setBarInside(bar)

        dataManager.save(foo)


        def barChangedEvents = TestCascadeBarEventListener.allEvents
        def fooChangedEvents = TestCascadeFooEventListener.allEvents

        then: "All events present for cascade-persisted entity"
        barChangedEvents.size() == fooChangedEvents.size()

        barChangedEvents.stream().anyMatch(info -> info.message == "AfterInsertEntityListener")
        barChangedEvents.stream().anyMatch(info -> info.message == "BeforeInsertEntityListener")
        barChangedEvents.stream().anyMatch(info -> info.message == "BeforeDetachEntityListener")

        barChangedEvents.stream().anyMatch(info -> info.message == "EntitySavingEvent: isNew=true")
        barChangedEvents.stream().anyMatch(info -> info.message == "EntityChangedEvent: beforeCommit, CREATED")
        barChangedEvents.stream().anyMatch(info -> info.message == "EntityChangedEvent: afterCommit, CREATED")

        barChangedEvents.stream().anyMatch(info -> info.message == "EntityLoadingEvent")
        cleanup:
        dataManager.remove(bar, foo)
    }


    def cleanup() {
        TestCascadeFooEventListener.clear()
        TestCascadeBarEventListener.clear()
    }
}
