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

package data_components

import io.jmix.core.EntityStates
import io.jmix.flowui.model.DataComponents
import io.jmix.flowui.model.DataContext
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Unroll
import test_support.entity.sales.Order
import test_support.entity.sales.OrderLine
import test_support.entity.sales.OrderLineParam
import test_support.entity.sec.Role
import test_support.entity.sec.User
import test_support.entity.sec.UserRole
import test_support.spec.DataContextSpec

@SuppressWarnings("GroovyAssignabilityCheck")
class DataContextParentTest extends DataContextSpec {

    @Autowired
    DataComponents factory
    @Autowired
    EntityStates entityStates

    def "child context creation and merging instance from parent"() throws Exception {

        DataContext context = factory.createDataContext()

        when: "merge instance into parent context"

        User user1 = makeSaved(new User(login: 'u1', name: 'User 1', userRoles: []))
        Role role1 = makeSaved(new Role(name: 'Role 1'))
        UserRole user1Role1 = makeSaved(new UserRole(user: user1, role: role1))
        user1.userRoles.add(user1Role1)

        def parentUser = context.merge(user1)
        def parentRole = context.find(Role, role1.id)
        def parentUserRole = context.find(UserRole, user1Role1.id)

        then:

        !context.hasChanges()

        when: "create child context"

        DataContext childContext = factory.createDataContext()
        childContext.setParent(context)

        then: "child context is clean and empty"

        !context.hasChanges()
        !childContext.hasChanges()
        childContext.getAll().isEmpty()

        when: "merge instance from parent to child context"

        childContext.merge(parentUser)

        def childUser = childContext.find(User, user1.id)
        def childRole = childContext.find(Role, role1.id)
        def childUserRole = childContext.find(UserRole, user1Role1.id)

        then: "child context has correct graph of different instances"

        childUser == parentUser
        !childUser.is(parentUser)
        childUser.userRoles[0] == childUserRole

        childRole == parentRole
        !childRole.is(parentRole)

        childUserRole == parentUserRole
        !childUserRole.is(parentUserRole)

        childUserRole.user?.is(childUser)
        childUserRole.role?.is(childRole)
    }

    def "modification of collection in child context"() throws Exception {

        DataContext ctx1 = factory.createDataContext()

        when: "merge instance into parent and child contexts"

        User user1_ctx1 = ctx1.merge(new User(login: 'u1', name: 'User1'))

        DataContext ctx2 = factory.createDataContext()

        ctx2.setParent(ctx1)

        ctx2.merge(user1_ctx1)

        then: "it exists in child context too, but as a different instance"

        User user1_ctx2 = ctx2.find(User, user1_ctx1.id)
        user1_ctx2 != null
        !user1_ctx2.is(user1_ctx1)

        when: "add detail instance to collection of the master object in child context and save it"

        UserRole ur1_ctx2 = ctx2.merge(new UserRole(user: user1_ctx2))

        user1_ctx2.userRoles = []
        user1_ctx2.userRoles.add(ur1_ctx2)

        def modified = []
        ctx2.addPreSaveListener({ e ->
            modified.addAll(e.modifiedInstances)
        })

        ctx2.save()

        then: "child context saves both detail and master instances to parent context"

        modified.size() == 2
        modified.contains(user1_ctx2)
        modified.contains(ur1_ctx2)

        user1_ctx1.userRoles != null
        user1_ctx1.userRoles.size() == 1

        UserRole ur1_ctx1 = ctx1.find(UserRole, ur1_ctx2.id)
        user1_ctx1.userRoles[0].is(ur1_ctx1)

        when: "saving parent context"

        modified.clear()

        ctx1.addPreSaveListener({ e ->
            modified.addAll(e.modifiedInstances)
        })

        ctx1.save()

        then: "parent context saves both detail and master instances"

        modified.size() == 2
        modified.contains(user1_ctx1)
        modified.contains(ur1_ctx1)
    }

    def "creating new instances in child context"() throws Exception {

        DataContext ctx1 = factory.createDataContext()

        when: "merge instance into parent context"

        User user1_ctx1 = ctx1.merge(new User(login: 'u1', name: 'User 1'))

        DataContext ctx2 = factory.createDataContext()
        ctx2.setParent(ctx1)

        ctx2.merge(user1_ctx1)

        then:

        User user1_ctx2 = ctx2.find(User, user1_ctx1.id)
        user1_ctx2 != null
        !user1_ctx2.is(user1_ctx1)
        isNew(user1_ctx2)
        ctx2.hasChanges()

        when: "create new instances in child context and save"

        UserRole ur1_ctx2 = ctx2.merge(new UserRole(user: user1_ctx2))

        Role r1_ctx2 = ctx2.merge(new Role(name: 'r1'))
        ur1_ctx2.role = r1_ctx2

        user1_ctx2.userRoles = [ur1_ctx2]

        ctx2.save()

        then: "new instances are in parent context"

        User user1 = ctx1.find(User, user1_ctx1.id)
        user1.is(user1_ctx1)

        UserRole ur1 = ctx1.find(UserRole, ur1_ctx2.id)
        ur1.user.is(user1_ctx1)

        Role r1 = ctx1.find(Role, r1_ctx2.id)
        ur1.role.is(r1)

        user1.userRoles[0].is(ur1)

        ctx1.hasChanges()
    }

    @Unroll
    def "save to parent"(boolean detached) {

        DataContext ctx1 = factory.createDataContext()

        def order = new Order(number: 1, orderLines: [], version: 1)
        makeDetached(order, ['number', 'orderLines'])

        def line = new OrderLine(quantity: 1, order: order)
        if (detached) {
            makeDetached(line, ['quantity', 'order'])
        }
        order.orderLines.add(line)

        when:

        def order1 = ctx1.merge(order)

        DataContext ctx2 = factory.createDataContext()
        ctx2.setParent(ctx1)

        def line1 = ctx1.find(OrderLine, line.id)

        ctx2.merge(line1)

        then:

        def order2 = ctx2.find(Order, order.id)
        def line2 = ctx2.find(OrderLine, line.id)
        order2.orderLines[0].is(line2)
        line2.order.is(order2)

        when:

        line2.quantity = 2
        ctx2.save()

        then:

        order1.is(ctx1.find(Order, order.id))
        line1.is(ctx1.find(OrderLine, line.id))
        line1.quantity == 2
        order1.orderLines[0].is(line1)

        and:

        order2.is(ctx2.find(Order, order.id))
        line2.is(ctx2.find(OrderLine, line.id))
        order2.orderLines[0].is(line2)
        line2.order.is(order2)

        and:

        ctx1.hasChanges()
        !ctx2.hasChanges()

        where:

        detached << [false, true]
    }

    def "removing just created composition item"() {
        DataContext ctx1 = factory.createDataContext()
        DataContext ctx2 = factory.createDataContext()
        ctx2.setParent(ctx1)

        def order = new Order(number: 1, orderLines: [])
        makeDetached(order, ['number', 'orderLines'])

        def order1 = ctx1.merge(order)

        def line = new OrderLine(order: order1, quantity: 1)
        order1.orderLines.add(line)

        ctx2.merge(line)

        when:

        ctx2.save()

        then:

        ctx1.getModified().contains(line)

        when:

        ctx2.merge(ctx1.find(line))
        ctx2.remove(line)
        ctx2.save()

        then:

        !ctx1.getModified().contains(line)
    }

    def "removing just created composition item having new child"() {
        DataContext ctx1 = factory.createDataContext()
        DataContext ctx2 = factory.createDataContext()
        DataContext ctx3 = factory.createDataContext()

        ctx2.setParent(ctx1)
        ctx3.setParent(ctx2)

        def order = new Order(number: 1, orderLines: [])
        makeDetached(order, ['number', 'orderLines'])

        def order1 = ctx1.merge(order)

        def line = new OrderLine(order: order1, quantity: 1, params: [])
        order1.orderLines.add(line)

        def line1 = ctx2.merge(line)

        def param = new OrderLineParam(orderLine: line1, name: 'p1')
        line1.params.add(param)

        ctx3.merge(param)

        when:

        ctx3.save()
        ctx2.save()

        then:

        ctx1.getModified().containsAll(line, param)

        when:

        ctx2.merge(ctx1.find(line))
        ctx2.remove(line)
        ctx2.save()

        then:

        !ctx1.getModified().contains(line)
        !ctx1.getModified().contains(param)
    }

    def "removing in root context just created composition item having new child"() {
        DataContext ctx1 = factory.createDataContext()
        DataContext ctx2 = factory.createDataContext()
        DataContext ctx3 = factory.createDataContext()

        ctx2.setParent(ctx1)
        ctx3.setParent(ctx2)

        def order = new Order(number: 1, orderLines: [])
        makeDetached(order, ['number', 'orderLines'])

        def order1 = ctx1.merge(order)

        def line = new OrderLine(order: order1, quantity: 1, params: [])
        order1.orderLines.add(line)

        def line1 = ctx2.merge(line)

        def param = new OrderLineParam(orderLine: line1, name: 'p1')
        line1.params.add(param)

        ctx3.merge(param)

        when:

        ctx3.save()
        ctx2.save()

        then:

        ctx1.getModified().containsAll(line, param)

        when:

        ctx2.merge(ctx1.find(line))
        ctx2.save() // save to root context
        ctx1.remove(line) // then remove

        then:

        !ctx1.getModified().contains(line)
        !ctx1.getModified().contains(param)
    }

    boolean isNew(def entity) {
        entity.__getEntityEntry().isNew()
    }
}
