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
 *
 */

package com.haulmont.cuba.core;

import com.haulmont.cuba.core.model.common.Group;
import com.haulmont.cuba.core.model.common.User;
import com.haulmont.cuba.core.testsupport.CoreTest;
import com.haulmont.cuba.core.testsupport.TestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@CoreTest
public class EntityStateTest {
    @Autowired
    private Persistence persistence;
    @Autowired
    private TestSupport testSupport;

    private UUID userId;
    private Group group;

    @AfterEach
    public void tearDown() throws Exception {
        if (userId != null)
            testSupport.deleteRecord("TEST_USER", userId);
        if (group != null) {
            testSupport.deleteRecord(group);
        }
    }

    @Test
    public void testTransactions() throws Exception {
        User user;
        Group localGroup;

        // create and persist

        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            user = new User();
            assertTrue(user.__getEntityEntry().isNew());

            assertFalse(user.__getEntityEntry().isManaged());
            assertFalse(user.__getEntityEntry().isDetached());

            group = new Group();
            group.setName("group");
            em.persist(group);

            userId = user.getId();
            user.setName("testUser");
            user.setLogin("testLogin");
            user.setGroup(group);
            em.persist(user);

            assertTrue(user.__getEntityEntry().isNew());

            assertTrue(user.__getEntityEntry().isManaged());
            assertFalse(user.__getEntityEntry().isDetached());

            tx.commit();
        } finally {
            tx.end();
        }

        assertFalse(user.__getEntityEntry().isNew());

        assertFalse(user.__getEntityEntry().isManaged());
        assertTrue(user.__getEntityEntry().isDetached());

        // load from DB

        tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            // find
            user = em.find(User.class, userId);
            assertNotNull(user);

            assertFalse(user.__getEntityEntry().isNew());

            assertTrue(user.__getEntityEntry().isManaged());
            assertFalse(user.__getEntityEntry().isDetached());

            localGroup = user.getGroup();
            assertNotNull(localGroup);

            assertFalse(user.__getEntityEntry().isNew());

            assertTrue(user.__getEntityEntry().isManaged());
            assertFalse(user.__getEntityEntry().isDetached());

            tx.commit();
        } finally {
            tx.end();
        }

        tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            // query
            Query query = em.createQuery("select u from test$User u where u.id = ?1").setParameter(1, userId);
            user = (User) query.getFirstResult();
            assertNotNull(user);

            assertFalse(user.__getEntityEntry().isNew());

            assertTrue(user.__getEntityEntry().isManaged());
            assertFalse(user.__getEntityEntry().isDetached());

            localGroup = user.getGroup();
            assertNotNull(localGroup);

            assertFalse(localGroup.__getEntityEntry().isNew());

            assertTrue(localGroup.__getEntityEntry().isManaged());
            assertFalse(localGroup.__getEntityEntry().isDetached());

            tx.commit();
        } finally {
            tx.end();
        }

        assertFalse(user.__getEntityEntry().isNew());

        assertFalse(user.__getEntityEntry().isManaged());
        assertTrue(user.__getEntityEntry().isDetached());

        assertFalse(localGroup.__getEntityEntry().isNew());

        assertFalse(user.__getEntityEntry().isManaged());
        assertTrue(user.__getEntityEntry().isDetached());

        user.setName("changed name");

        // merge changed

        tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            user = em.merge(user);

            assertFalse(user.__getEntityEntry().isNew());

            assertTrue(user.__getEntityEntry().isManaged());
            assertFalse(user.__getEntityEntry().isDetached());

            tx.commit();
        } finally {
            tx.end();
        }

        assertFalse(user.__getEntityEntry().isNew());

        assertFalse(user.__getEntityEntry().isManaged());
        assertTrue(user.__getEntityEntry().isDetached());
    }

    @Test
    public void testSerialization() throws Exception {
        User user;
        Group localGroup;

        // serialize new
        user = new User();
        assertTrue(user.__getEntityEntry().isNew());

        assertFalse(user.__getEntityEntry().isManaged());
        assertFalse(user.__getEntityEntry().isDetached());

        user = testSupport.reserialize(user);

        assertTrue(user.__getEntityEntry().isNew());

        assertFalse(user.__getEntityEntry().isManaged());
        assertFalse(user.__getEntityEntry().isDetached());

        // serialize managed

        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            group = new Group();
            group.setName("group");
            em.persist(group);

            user = new User();
            userId = user.getId();
            user.setName("testUser");
            user.setLogin("testLogin");

            user.setGroup(group);
            em.persist(user);

            tx.commit();
        } finally {
            tx.end();
        }

        tx = persistence.createTransaction();

        try {
            EntityManager em = persistence.getEntityManager();
            user = em.find(User.class, userId);
            assertNotNull(user);

            assertFalse(user.__getEntityEntry().isNew());

            assertTrue(user.__getEntityEntry().isManaged());
            assertFalse(user.__getEntityEntry().isDetached());

            localGroup = user.getGroup();
            assertNotNull(localGroup);

            assertFalse(localGroup.__getEntityEntry().isNew());

            assertTrue(localGroup.__getEntityEntry().isManaged());
            assertFalse(localGroup.__getEntityEntry().isDetached());

            user = testSupport.reserialize(user);

            assertFalse(user.__getEntityEntry().isNew());

            assertFalse(user.__getEntityEntry().isManaged());
            assertTrue(user.__getEntityEntry().isDetached());

            tx.commit();
        } finally {
            tx.end();
        }

        user.setName("changed name");

        // merge changed and serialize

        tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            user = em.merge(user);

            assertFalse(user.__getEntityEntry().isNew());

            assertTrue(user.__getEntityEntry().isManaged());
            assertFalse(user.__getEntityEntry().isDetached());

            tx.commit();
        } finally {
            tx.end();
        }

        assertFalse(user.__getEntityEntry().isNew());

        assertFalse(user.__getEntityEntry().isManaged());
        assertTrue(user.__getEntityEntry().isDetached());

        user = testSupport.reserialize(user);

        assertFalse(user.__getEntityEntry().isNew());

        assertFalse(user.__getEntityEntry().isManaged());
        assertTrue(user.__getEntityEntry().isDetached());
    }

    @Test
    public void testTransactionRollback_new() throws Exception {
        User user = null;

        // create and persist

        Transaction tx = persistence.createTransaction();
        try {
            user = new User();
            assertTrue(user.__getEntityEntry().isNew());

            assertFalse(user.__getEntityEntry().isManaged());
            assertFalse(user.__getEntityEntry().isDetached());

            userId = user.getId();

            persistence.getEntityManager().persist(user);

            assertTrue(user.__getEntityEntry().isNew());

            assertTrue(user.__getEntityEntry().isManaged());
            assertFalse(user.__getEntityEntry().isDetached());

            tx.commit();

            fail(); // due to absence login
        } catch (Exception e) {
            // ok
        } finally {
            tx.end();
        }

        assertTrue(user.__getEntityEntry().isNew());

        assertFalse(user.__getEntityEntry().isManaged());
        assertFalse(user.__getEntityEntry().isDetached());
    }

    @Test
    public void testTransactionRollback_loaded() {
        User user;

        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            user = new User();
            assertTrue(user.__getEntityEntry().isNew());

            assertFalse(user.__getEntityEntry().isManaged());
            assertFalse(user.__getEntityEntry().isDetached());

            group = new Group();
            group.setName("group");
            em.persist(group);

            userId = user.getId();
            user.setName("testUser");
            user.setLogin("testLogin");
            user.setGroup(group);
            em.persist(user);

            tx.commit();
        } finally {
            tx.end();
        }


        tx = persistence.createTransaction();
        try {
            user = persistence.getEntityManager().find(User.class, userId);

            assertFalse(user.__getEntityEntry().isNew());

            assertTrue(user.__getEntityEntry().isManaged());
            assertFalse(user.__getEntityEntry().isDetached());
        } finally {
            tx.end();
        }

        assertFalse(user.__getEntityEntry().isNew());

        assertFalse(user.__getEntityEntry().isManaged());
        assertTrue(user.__getEntityEntry().isDetached());
    }
}
