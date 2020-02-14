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

package spec.haulmont.cuba.core.tx_listeners

import com.haulmont.cuba.core.model.common.Group
import com.haulmont.cuba.core.model.common.Role
import com.haulmont.cuba.core.model.common.User
import com.haulmont.cuba.core.model.common.UserRole
import com.haulmont.cuba.core.tx_listener.TestAfterCompleteTxListener
import io.jmix.core.AppBeans
import io.jmix.core.DataManager
import io.jmix.core.EntityStates
import io.jmix.core.FetchPlan
import io.jmix.data.Persistence
import spec.haulmont.cuba.core.CoreTestSpecification

import javax.inject.Inject

import static com.haulmont.cuba.core.testsupport.TestSupport.deleteRecord

class AfterCompleteTransactionListenerTest extends CoreTestSpecification {
    @Inject
    DataManager dataManager
    @Inject
    Persistence persistence

    User user
    Group group
    Role role
    UserRole userRole

    void setup() {
        group = new Group(name: 'Company')
        user = new User(login: 'admin', group: group)
        role = new Role(name: 'role')
        userRole = new UserRole(user: user, role: role)

        dataManager.commit(user, group, role, userRole)
    }

    void cleanup() {
        deleteRecord(userRole, role, user, group)
    }

    def "reference CAN be fetched in afterComplete if entity is not partial"() {

        def entityStates = AppBeans.get(EntityStates)
        TestAfterCompleteTxListener.test = 'accessGroup'

        when:

        def user = persistence.callInTransaction { em ->
            em.find(User, user.id)
        }

        then:

        entityStates.isLoaded(user, 'login')
        entityStates.isLoaded(user, 'name')
        entityStates.isLoaded(user, 'group')
        user.group != null

        cleanup:

        TestAfterCompleteTxListener.test = null
    }

    def "collection CAN be fetched in afterComplete if entity is not partial"() {

        def entityStates = AppBeans.get(EntityStates)
        TestAfterCompleteTxListener.test = 'accessUserRoles'

        when:

        def user = persistence.callInTransaction { em ->
            em.find(User, user.id)
        }

        then:

        entityStates.isLoaded(user, 'login')
        entityStates.isLoaded(user, 'name')
        entityStates.isLoaded(user, 'userRoles')
        user.userRoles.size() == 1

        cleanup:

        TestAfterCompleteTxListener.test = null
    }

    def "local attribute CANNOT be fetched in afterComplete if entity is partial"() {

        def entityStates = AppBeans.get(EntityStates)
        TestAfterCompleteTxListener.test = 'accessName'

        def view = new FetchPlan(User)
                .addProperty('login')
                .addProperty('group', new FetchPlan(Group).addProperty('name'))
        view.setLoadPartialEntities(true)

        when:

        def user = persistence.callInTransaction { em ->
            em.find(User, user.id, view)
        }

        then:

        entityStates.isLoaded(user, 'login')
        entityStates.isLoaded(user, 'group')
        entityStates.isLoaded(user.group, 'name')
        !entityStates.isLoaded(user, 'name')

        cleanup:

        TestAfterCompleteTxListener.test = null
    }

    def "reference CANNOT be fetched in afterComplete if entity is partial"() {

        def entityStates = AppBeans.get(EntityStates)
        TestAfterCompleteTxListener.test = 'accessGroup'

        def view = new FetchPlan(User)
                .addProperty('login')
                .addProperty('userRoles', new FetchPlan(UserRole)
                        .addProperty('role', new FetchPlan(Role)
                                .addProperty('name')))
        view.setLoadPartialEntities(true)

        when:

        def user = persistence.callInTransaction { em ->
            em.find(User, user.id, view)
        }

        then:

        entityStates.isLoaded(user, 'login')
        entityStates.isLoaded(user, 'userRoles')
        !entityStates.isLoaded(user, 'group')

        cleanup:

        TestAfterCompleteTxListener.test = null
    }

    def "collection CANNOT be fetched in afterComplete if entity is partial"() {

        def entityStates = AppBeans.get(EntityStates)
        TestAfterCompleteTxListener.test = 'accessUserRoles'

        def view = new FetchPlan(User)
                .addProperty('login')
        view.setLoadPartialEntities(true)

        when:

        def user = persistence.callInTransaction { em ->
            em.find(User, user.id, view)
        }

        then:

        entityStates.isLoaded(user, 'login')
        !entityStates.isLoaded(user, 'userRoles')

        cleanup:

        TestAfterCompleteTxListener.test = null
    }
}
