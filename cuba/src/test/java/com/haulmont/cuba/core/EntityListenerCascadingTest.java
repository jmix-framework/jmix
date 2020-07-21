/*
 * Copyright (c) 2008-2016 Haulmont.
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
package com.haulmont.cuba.core;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.listener.TestCascadingEntityListener;
import com.haulmont.cuba.core.model.common.Group;
import com.haulmont.cuba.core.model.common.User;
import com.haulmont.cuba.core.testsupport.CoreTest;
import com.haulmont.cuba.core.testsupport.TestSupport;
import io.jmix.core.FetchPlan;
import io.jmix.core.Metadata;
import io.jmix.data.impl.EntityListenerManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

@CoreTest
public class EntityListenerCascadingTest {
    @Autowired
    private Persistence persistence;
    @Autowired
    private Metadata metadata;
    @Autowired
    private TestSupport testSupport;

    private EntityListenerManager entityListenerManager;

    private User user, admin;
    private Group group;

    @BeforeEach
    public void setUp() throws Exception {
        try (Transaction tx = persistence.createTransaction()) {
            group = metadata.create(Group.class);
            group.setName("Group");
            persistence.getEntityManager().persist(group);

            user = metadata.create(User.class);

            user.setGroup(group);
            user.setLogin("user-" + user.getId());
            persistence.getEntityManager().persist(user);

            admin = new User();
            admin.setGroup(group);
            admin.setLogin("admin");
            persistence.getEntityManager().persist(admin);
            tx.commit();
        }

        entityListenerManager = AppBeans.get(EntityListenerManager.class);
        entityListenerManager.addListener(User.class, TestCascadingEntityListener.class);

        TestCascadingEntityListener.events.clear();
    }

    @AfterEach
    public void tearDown() throws Exception {
        entityListenerManager.removeListener(User.class, TestCascadingEntityListener.class);
        testSupport.deleteRecord(user, admin, group);
    }

    @Test
    public void testQueryInListener() throws Exception {
        TestCascadingEntityListener.withNewTx = false;
        TestCascadingEntityListener.withView = false;
        doQueryInListener();
    }

    @Test
    public void testQueryWithViewInListener() throws Exception {
        TestCascadingEntityListener.withNewTx = false;
        TestCascadingEntityListener.withView = true;
        doQueryInListener();
    }

    @Test
    public void testQueryWithViewInNewTxInListener() throws Exception {
        TestCascadingEntityListener.withNewTx = true;
        TestCascadingEntityListener.withView = true;
        doQueryInListener();
    }

    private void doQueryInListener() {
        try (Transaction tx = persistence.createTransaction()) {
            User u = persistence.getEntityManager().find(User.class, user.getId());
            assertNotNull(u);
            u.setName(u.getLogin());
            tx.commit();
        }
        System.out.println("\n" + TestCascadingEntityListener.events + "\n");
        assertEquals(2, TestCascadingEntityListener.events.size());
        assertTrue(TestCascadingEntityListener.events.get(0).contains("onBeforeUpdate"));
        assertTrue(TestCascadingEntityListener.events.get(1).contains("onAfterUpdate"));
    }

    @Test
    public void testUpdateBySecondListener() throws Exception {
        TestCascadingEntityListener.withNewTx = false;
        TestCascadingEntityListener.withView = true;
        try (Transaction tx = persistence.createTransaction()) {
            User u = persistence.getEntityManager().find(User.class, user.getId());
            assertNotNull(u);
            u.setLogin("1-NEW-" + user.getId());

            tx.commit();
        }
        System.out.println("\n" + TestCascadingEntityListener.events + "\n");
        assertEquals(3, TestCascadingEntityListener.events.size());
        assertTrue(TestCascadingEntityListener.events.get(0).contains("onBeforeUpdate"));
        assertTrue(TestCascadingEntityListener.events.get(1).contains("onAfterUpdate"));
        // second onAfterUpdate because UserEntityListener changes loginLowerCase and the instance becomes dirty
        assertTrue(TestCascadingEntityListener.events.get(2).contains("onAfterUpdate"));

        try (Transaction tx = persistence.createTransaction()) {
            User u = persistence.getEntityManager().find(User.class, user.getId());
            assertNotNull(u);
            assertEquals("1-new-" + u.getId(), u.getLoginLowerCase());
        }
    }

    @Test
    public void testChangeThenQuery() throws Exception {
        TestCascadingEntityListener.withNewTx = false;
        TestCascadingEntityListener.withView = true;
        try (Transaction tx = persistence.createTransaction()) {
            User u = persistence.getEntityManager().find(User.class, user.getId());
            assertNotNull(u);
            u.setLogin("1-NEW-" + user.getId());

            TypedQuery<User> query = persistence.getEntityManager().createQuery("select u from test$User u where u.login = 'admin'", User.class);
            query.setViewName(FetchPlan.INSTANCE_NAME);
            User admin = query.getSingleResult();
            System.out.println(admin.getLogin());

            tx.commit();
        }
        System.out.println("\n" + TestCascadingEntityListener.events + "\n");
        assertEquals(3, TestCascadingEntityListener.events.size());
        // on flush by query
        assertTrue(TestCascadingEntityListener.events.get(0).contains("onBeforeUpdate"));
        assertTrue(TestCascadingEntityListener.events.get(1).contains("onAfterUpdate"));
        // second onAfterUpdate because UserEntityListener changes loginLowerCase and the instance becomes dirty
        assertTrue(TestCascadingEntityListener.events.get(2).contains("onAfterUpdate"));

        try (Transaction tx = persistence.createTransaction()) {
            User u = persistence.getEntityManager().find(User.class, user.getId());
            assertNotNull(u);
            assertEquals("1-new-" + u.getId(), u.getLoginLowerCase());
        }
    }

    @Test
    public void testChangeThenQueryThenChange() throws Exception {
        TestCascadingEntityListener.withNewTx = false;
        TestCascadingEntityListener.withView = true;
        try (Transaction tx = persistence.createTransaction()) {
            User u = persistence.getEntityManager().find(User.class, user.getId());
            assertNotNull(u);
            u.setLogin("1-NEW-" + user.getId());

            TypedQuery<User> query = persistence.getEntityManager().createQuery("select u from test$User u where u.login = 'admin'", User.class);
            query.setViewName(FetchPlan.INSTANCE_NAME);
            User admin = query.getSingleResult();
            System.out.println(admin.getLogin());

            u.setLogin("2-NEW-" + user.getId());

            tx.commit();
        }
        System.out.println("\n" + TestCascadingEntityListener.events + "\n");
        assertEquals(6, TestCascadingEntityListener.events.size());
        // on flush by query
        assertTrue(TestCascadingEntityListener.events.get(0).contains("onBeforeUpdate"));
        assertTrue(TestCascadingEntityListener.events.get(1).contains("onAfterUpdate"));
        // second onAfterUpdate because UserEntityListener changes loginLowerCase and the instance becomes dirty
        assertTrue(TestCascadingEntityListener.events.get(2).contains("onAfterUpdate"));
        // on commit because of change after flush
        assertTrue(TestCascadingEntityListener.events.get(0).contains("onBeforeUpdate"));
        assertTrue(TestCascadingEntityListener.events.get(1).contains("onAfterUpdate"));
        // second onAfterUpdate because UserEntityListener changes loginLowerCase and the instance becomes dirty
        assertTrue(TestCascadingEntityListener.events.get(2).contains("onAfterUpdate"));

        try (Transaction tx = persistence.createTransaction()) {
            User u = persistence.getEntityManager().find(User.class, user.getId());
            assertNotNull(u);
            assertEquals("2-new-" + u.getId(), u.getLoginLowerCase());
        }
    }
}
