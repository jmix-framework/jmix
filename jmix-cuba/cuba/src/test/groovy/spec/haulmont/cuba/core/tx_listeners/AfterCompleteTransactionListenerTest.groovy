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

import com.haulmont.cuba.core.Persistence
import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.cuba.core.global.DataManager
import com.haulmont.cuba.core.global.View
import com.haulmont.cuba.core.model.common.Group
import com.haulmont.cuba.core.model.common.Role
import com.haulmont.cuba.core.model.common.User
import com.haulmont.cuba.core.model.common.UserRole
import com.haulmont.cuba.core.testsupport.TestSupport
import com.haulmont.cuba.core.tx_listener.TestAfterCompleteTxListener
import io.jmix.core.EntityStates
import org.springframework.beans.factory.annotation.Autowired
import spec.haulmont.cuba.core.CoreTestSpecification
import spock.lang.Ignore

class AfterCompleteTransactionListenerTest extends CoreTestSpecification {
    @Autowired
    DataManager dataManager
    @Autowired
    Persistence persistence
    @Autowired
    TestSupport testSupport

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
        testSupport.deleteRecord(userRole, role, user, group)
    }

    def "reference CAN be fetched in afterComplete if entity is not partial"() {

        def entityStates = AppBeans.get(EntityStates)
        TestAfterCompleteTxListener.test = 'accessGroup'

        when:

        def reloadedUser = persistence.callInTransaction { em ->
            em.find(User, user.id)
        }

        then:

        entityStates.isLoaded(reloadedUser, 'login')
        entityStates.isLoaded(reloadedUser, 'name')
        entityStates.isLoaded(reloadedUser, 'group')
        reloadedUser.group != null

        cleanup:

        TestAfterCompleteTxListener.test = null
    }

    def "collection CAN be fetched in afterComplete if entity is not partial"() {

        def entityStates = AppBeans.get(EntityStates)
        TestAfterCompleteTxListener.test = 'accessUserRoles'

        when:

        def reloadedUser = persistence.callInTransaction { em ->
            em.find(User, user.id)
        }

        then:

        entityStates.isLoaded(reloadedUser, 'login')
        entityStates.isLoaded(reloadedUser, 'name')
        entityStates.isLoaded(reloadedUser, 'userRoles')
        reloadedUser.userRoles.size() == 1

        cleanup:

        TestAfterCompleteTxListener.test = null
    }

    def "local attribute CANNOT be fetched in afterComplete if entity is partial"() {

        def entityStates = AppBeans.get(EntityStates)
        TestAfterCompleteTxListener.test = 'accessName'

        def view = new View(User)
                .addProperty('login')
                .addProperty('group', new View(Group).addProperty('name'))
        view.setLoadPartialEntities(true)

        when:

        def reloadedUser = persistence.callInTransaction { em ->
            em.find(User, user.id, view)
        }

        then:

        entityStates.isLoaded(reloadedUser, 'login')
        entityStates.isLoaded(reloadedUser, 'group')
        entityStates.isLoaded(reloadedUser.group, 'name')
        !entityStates.isLoaded(reloadedUser, 'name')

        cleanup:

        TestAfterCompleteTxListener.test = null
    }

    // ToDo: LazyLoading
    @Ignore
    def "reference CANNOT be fetched in afterComplete if entity is partial"() {

        def entityStates = AppBeans.get(EntityStates)
        TestAfterCompleteTxListener.test = 'accessGroup'

        def view = new View(User)
                .addProperty('login')
                .addProperty('userRoles', new View(UserRole)
                        .addProperty('role', new View(Role)
                                .addProperty('name')))
        view.setLoadPartialEntities(true)

        when:

        def reloadedUser = persistence.callInTransaction { em ->
            em.find(User, user.id, view)
        }

        then:

        entityStates.isLoaded(reloadedUser, 'login')
        entityStates.isLoaded(reloadedUser, 'userRoles')
        !entityStates.isLoaded(reloadedUser, 'group')

        cleanup:

        TestAfterCompleteTxListener.test = null
    }

    // ToDo: LazyLoading
    @Ignore
    def "collection CANNOT be fetched in afterComplete if entity is partial"() {

        def entityStates = AppBeans.get(EntityStates)
        TestAfterCompleteTxListener.test = 'accessUserRoles'

        def view = new View(User)
                .addProperty('login')
        view.setLoadPartialEntities(true)

        when:

        def reloadedUser = persistence.callInTransaction { em ->
            em.find(User, user.id, view)
        }

        then:

        entityStates.isLoaded(reloadedUser, 'login')
        !entityStates.isLoaded(reloadedUser, 'userRoles')

        cleanup:

        TestAfterCompleteTxListener.test = null
    }
}
