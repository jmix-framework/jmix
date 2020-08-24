/*
 * Copyright (c) 2008-2017 Haulmont.
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
package spec.haulmont.cuba.core.entity_listeners

import com.haulmont.cuba.core.Persistence
import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.cuba.core.global.DataManager
import com.haulmont.cuba.core.global.LoadContext
import com.haulmont.cuba.core.global.View
import com.haulmont.cuba.core.listener.TestUserDetachListener
import com.haulmont.cuba.core.listener.TestUserEntityListener
import com.haulmont.cuba.core.model.common.Group
import com.haulmont.cuba.core.model.common.User
import com.haulmont.cuba.core.testsupport.TestSupport
import io.jmix.core.EntityStates
import io.jmix.core.FetchPlan
import io.jmix.core.FetchPlanRepository
import io.jmix.core.Metadata
import io.jmix.data.impl.EntityListenerManager
import org.springframework.beans.factory.annotation.Autowired
import spec.haulmont.cuba.core.CoreTestSpecification

import java.util.function.Consumer

class EntityListenerTest extends CoreTestSpecification {
    @Autowired
    private DataManager dataManager
    @Autowired
    private EntityStates entityStates
    @Autowired
    private Persistence persistence
    @Autowired
    private Metadata metadata
    @Autowired
    private TestSupport testSupport

    private Group companyGroup

    void setup() {
        companyGroup = new Group(name: 'Company')

        dataManager.commit(companyGroup)
    }

    void cleanup() {
        testSupport.deleteRecord(companyGroup)
    }

    def "PL-9350 onBeforeInsert listener fires twice if em.flush() is used"() {

        EntityListenerManager entityListenerManager = AppBeans.get(EntityListenerManager);
        entityListenerManager.addListener(User, TestUserEntityListener)
        def events = TestUserEntityListener.events
        events.clear()

        def user = metadata.create(User)
        user.setLogin("user-" + user.id)
        user.setGroup(persistence.callInTransaction { em -> em.find(Group, companyGroup.id) })

        when:

        persistence.runInTransaction() { em ->
            em.persist(user)
            em.flush()
            user.setName(user.login)
        }

        then:

        events.size() == 4
        events[0].startsWith("onBeforeInsert")
        events[1].startsWith("onAfterInsert")
        events[2].startsWith("onBeforeUpdate")
        events[3].startsWith("onAfterUpdate")

        cleanup:

        events.clear()
        entityListenerManager.removeListener(User, TestUserEntityListener)
        testSupport.deleteRecord(user)
    }

    def "accessing properties that are not loaded"() {

        def user = metadata.create(User)
        user.setLogin("User-$user.id")
        user.setName('test user')
        user.setGroup(persistence.callInTransaction { em -> em.find(Group, companyGroup.id) })
        persistence.runInTransaction() { em ->
            em.persist(user)
        }

        def view = new View(User).addProperty('name')
        def loadContext = LoadContext.create(User).setId(user.id).setView(view)

        when:

        def loadedUser = dataManager.load(loadContext)
        loadedUser.setName('changed name')
        dataManager.commit(loadedUser)

        loadedUser = persistence.callInTransaction() { em ->
            em.find(User, user.id)
        }

        then:

        noExceptionThrown()
        loadedUser.name == 'changed name'
        loadedUser.login == user.login
        loadedUser.loginLowerCase == user.login.toLowerCase()

        cleanup:

        testSupport.deleteRecord(user)
    }

    def "accessing not loaded attributes in BeforeDetach"() {

        def user = metadata.create(User)
        user.setLogin("User-$user.id")
        user.setName('test user')

        def group = persistence.callInTransaction { em -> em.find(Group, companyGroup.id) }
        user.setGroup(group)

        persistence.runInTransaction() { em ->
            em.persist(user)
        }

        EntityListenerManager entityListenerManager = AppBeans.get(EntityListenerManager);
        entityListenerManager.addListener(User, TestUserDetachListener)

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        when: "loading entity with view"

        def view = new View(User)
                .addProperty('name')
                .addProperty('group', new View(Group)
                        .addProperty('name'))
        dataManager.load(LoadContext.create(User).setId(user.id).setView(view))

        then: "throw exception on access unfetched attribute in DetachListener"

        def exception = thrown(IllegalStateException)
        println(exception)

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        when: "loading entity with local view"

        view = AppBeans.get(FetchPlanRepository).getFetchPlan(User, FetchPlan.LOCAL)

        def loadedUser = dataManager.load(LoadContext.create(User).setId(user.id).setView(view))

        then: "can fetch reference attributes in DetachListener"

        loadedUser.group == group

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        when: "loading entity without view"

        loadedUser = persistence.callInTransaction { em -> em.find(User, user.id) }

        then: "can fetch reference attributes in DetachListener"

        loadedUser.group == group

        cleanup:

        entityListenerManager.removeListener(User, TestUserDetachListener)
        testSupport.deleteRecord(user)
    }

    def "in BeforeInsert reference can be detached"() {
        def user = metadata.create(User)
        user.login = "User-$user.id"
        user.name = 'test user'
        user.group = dataManager.load(Group).id(companyGroup.id).one()

        EntityListenerManager entityListenerManager = AppBeans.get(EntityListenerManager);
        entityListenerManager.addListener(User, TestUserEntityListener)

        TestUserEntityListener.consumers.put("BeforeInsert",
                { User it ->
                    assert entityStates.isDetached(it.group)
                } as Consumer<User>
        )

        when:

        dataManager.commit(user)

        then:

        noExceptionThrown()

        cleanup:

        TestUserEntityListener.consumers.clear()
        entityListenerManager.removeListener(User, TestUserEntityListener)
        testSupport.deleteRecord(user)
    }

    def "in BeforeInsert reference can be new+managed"() {
        def group = metadata.create(Group)
        group.name = "test group"

        def user = metadata.create(User)
        user.login = "User-$user.id"
        user.name = 'test user'
        user.group = group

        EntityListenerManager entityListenerManager = AppBeans.get(EntityListenerManager);
        entityListenerManager.addListener(User, TestUserEntityListener)

        TestUserEntityListener.consumers.put("BeforeInsert",
                { User it ->
                    assert entityStates.isNew(it.group)
                    assert entityStates.isManaged(it.group)
                } as Consumer<User>
        )

        when:

        dataManager.commit(group, user)

        then:

        noExceptionThrown()

        cleanup:

        TestUserEntityListener.consumers.clear()
        entityListenerManager.removeListener(User, TestUserEntityListener)
        testSupport.deleteRecord(user, group)
    }

    def "in BeforeUpdate reference is managed"() {
        def user = metadata.create(User)
        user.login = "User-$user.id"
        user.name = 'test user'
        user.group = dataManager.load(Group).id(companyGroup.id).one()
        user = dataManager.commit(user)

        def group = metadata.create(Group)
        group.name = "test group"
        group = dataManager.commit(group)

        EntityListenerManager entityListenerManager = AppBeans.get(EntityListenerManager);
        entityListenerManager.addListener(User, TestUserEntityListener)

        TestUserEntityListener.consumers.put("BeforeUpdate",
                { User it ->
                    assert entityStates.isManaged(it.group)
                } as Consumer<User>
        )

        when:

        user.group = group
        dataManager.commit(user)

        then:

        noExceptionThrown()

        cleanup:

        TestUserEntityListener.consumers.clear()
        entityListenerManager.removeListener(User, TestUserEntityListener)
        testSupport.deleteRecord(user, group)
    }

    def "in BeforeUpdate reference is managed even if merged object loaded with local view"() {
        def user = metadata.create(User)
        user.login = "User-$user.id"
        user.name = 'test user'
        user.group = dataManager.load(Group).id(companyGroup.id).one()
        dataManager.commit(user)

        user = dataManager.load(User).id(user.id).view(FetchPlan.LOCAL).one()

        EntityListenerManager entityListenerManager = AppBeans.get(EntityListenerManager);
        entityListenerManager.addListener(User, TestUserEntityListener)

        TestUserEntityListener.consumers.put("BeforeUpdate",
                { User it ->
                    assert entityStates.isManaged(it.group)
                } as Consumer<User>
        )

        when:

        user.position = "test position"
        dataManager.commit(user)

        then:

        noExceptionThrown()

        cleanup:

        TestUserEntityListener.consumers.clear()
        entityListenerManager.removeListener(User, TestUserEntityListener)
        testSupport.deleteRecord(user)
    }

}
