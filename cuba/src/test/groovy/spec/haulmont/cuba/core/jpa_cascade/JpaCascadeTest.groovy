/*
 * Copyright (c) 2008-2018 Haulmont.
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

package spec.haulmont.cuba.core.jpa_cascade

import com.haulmont.cuba.core.Persistence
import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.cuba.core.model.jpa_cascade.JpaCascadeBar
import com.haulmont.cuba.core.model.jpa_cascade.JpaCascadeFoo
import com.haulmont.cuba.core.model.jpa_cascade.JpaCascadeItem
import io.jmix.core.EntityStates
import io.jmix.core.Metadata
import io.jmix.core.security.SecurityContextHelper
import io.jmix.core.security.SystemAuthenticationToken
import io.jmix.data.impl.EntityListenerManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import spec.haulmont.cuba.core.CoreTestSpecification

class JpaCascadeTest extends CoreTestSpecification {
    @Autowired
    private Metadata metadata
    @Autowired
    private Persistence persistence
    @Autowired
    private EntityStates entityStates
    @Autowired
    private AuthenticationManager authenticationManager

    void setup() {
        AppBeans.get(EntityListenerManager).addListener(JpaCascadeBar, TestJpaCascadeBarListener)
        AppBeans.get(EntityListenerManager).addListener(JpaCascadeFoo, TestJpaCascadeFooListener)
        AppBeans.get(EntityListenerManager).addListener(JpaCascadeItem, TestJpaCascadeItemListener)
        TestJpaCascadeFooListener.messages.clear()
        TestJpaCascadeBarListener.messages.clear()
        TestJpaCascadeItemListener.messages.clear()

        Authentication authentication = authenticationManager.authenticate(new SystemAuthenticationToken(null))
        SecurityContextHelper.setAuthentication(authentication)
    }

    void cleanup() {
        AppBeans.get(EntityListenerManager).removeListener(JpaCascadeFoo, TestJpaCascadeFooListener)
        AppBeans.get(EntityListenerManager).removeListener(JpaCascadeBar, TestJpaCascadeBarListener)
        AppBeans.get(EntityListenerManager).removeListener(JpaCascadeItem, TestJpaCascadeItemListener)

        def jdbcTemplate = new JdbcTemplate(persistence.getDataSource())
        jdbcTemplate.update('delete from TEST_JPA_CASCADE_ITEM')
        jdbcTemplate.update('delete from TEST_JPA_CASCADE_FOO')
        jdbcTemplate.update('delete from TEST_JPA_CASCADE_BAR')
    }

    def "many-to-one reference with cascade"() {

        def isManaged, isNew

        when: "foo.bar is persisted implicitly"

        def foo = metadata.create(JpaCascadeFoo)
        def bar = metadata.create(JpaCascadeBar)
        foo.setBar(bar)

        persistence.runInTransaction { em ->
            em.persist(foo)
            isManaged = entityStates.isManaged(foo.bar)
            isNew = entityStates.isNew(foo.bar)
        }

        then: "EntityStates contracts are broken and entity listeners are not invoked"

        !isManaged // EntityStates.isManaged contract is broken
        isNew

        TestJpaCascadeFooListener.messages.size() == 1
        TestJpaCascadeFooListener.messages[0].startsWith('onBeforeInsert')

        TestJpaCascadeBarListener.messages.size() == 0 // listeners on implicitly persisted entities are not invoked

        // createTs/createdBy are correct
        def foo1
        persistence.runInTransaction { em ->
            foo1 = em.find(JpaCascadeFoo, foo.id)
            assert foo1.bar == bar
            assert foo1.bar.createTs != null
            assert foo1.bar.createdBy != null
        }

        when: "foo.bar is merged implicitly"

        foo1.name = 'foo_changed'
        foo1.bar.name = 'bar_changed'
        persistence.runInTransaction { em ->
            em.merge(foo1)
        }

        then: "value is saved and updateTs/updatedBy are correct"

        def foo2
        persistence.runInTransaction { em ->
            foo2 = em.find(JpaCascadeFoo, foo.id)
            assert foo2.bar.name == 'bar_changed'
            assert foo2.bar.updateTs != null
            assert foo2.bar.updatedBy != null
        }
    }

    def "one-to-many collection with cascade and orphanRemoval"() {

        def isManaged, isNew

        when: "foo.items collection is persisted implicitly"

        def foo = metadata.create(JpaCascadeFoo)
        def item1 = metadata.create(JpaCascadeItem)
        def item2 = metadata.create(JpaCascadeItem)
        item1.foo = foo
        item2.foo = foo
        foo.setItems([item1, item2])

        persistence.runInTransaction { em ->
            em.persist(foo)
            isManaged = entityStates.isManaged(foo.items[0])
            isNew = entityStates.isNew(foo.items[0])
        }

        then: "EntityStates contracts are broken and entity listeners are not invoked"

        !isManaged // EntityStates.isManaged contract is broken
        isNew

        TestJpaCascadeFooListener.messages.size() == 1
        TestJpaCascadeFooListener.messages[0].startsWith('onBeforeInsert')

        TestJpaCascadeBarListener.messages.size() == 0 // listeners on implicitly persisted entities are not invoked

        // createTs/createdBy are correct
        persistence.runInTransaction { em ->
            def foo1 = em.find(JpaCascadeFoo, foo.id)
            assert foo1.items[0].createTs != null
            assert foo1.items[0].createdBy != null
        }

        when: "removing item from collection"

        def removedItem
        persistence.runInTransaction { em ->
            def foo1 = em.find(JpaCascadeFoo, foo.id)
            removedItem = foo1.items[0]
            foo1.items.remove(removedItem)
        }

        then: "it is hard-deleted from the database although it is a soft-deleted entity"

        persistence.runInTransaction { em ->
            def foo1 = em.find(JpaCascadeFoo, foo.id)
            assert foo1.items.size() == 1

            em.softDeletion = false
            assert em.find(JpaCascadeItem, removedItem.id) == null
        }
    }
}
