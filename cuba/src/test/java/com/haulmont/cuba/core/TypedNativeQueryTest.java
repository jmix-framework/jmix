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
public class TypedNativeQueryTest {
    @Autowired
    private Persistence persistence;
    @Autowired
    private TestSupport testSupport;

    private UUID groupId, userId;

    @AfterEach
    public void tearDown() throws Exception {
        if (userId != null) {
            testSupport.deleteRecord("TEST_USER", userId);
        }
        if (groupId != null) {
            testSupport.deleteRecord("TEST_GROUP", groupId);
        }
    }

    /*
     * Test that entity which is loaded by native typed query, is MANAGED,
     * by changing loaded entity attribute.
     */
    @Test
    public void testTypedNativeQueryByChangingAttribute() {
        Group group = new Group();
        groupId = group.getId();
        group.setName("Old Name");
        Transaction tx = persistence.createTransaction();
        try {
            persistence.getEntityManager().persist(group);
            tx.commit();
        } finally {
            tx.end();
        }

        // load with native query, change attribute
        String nativeQuery = "select ID, VERSION, CREATE_TS, CREATED_BY, UPDATE_TS, UPDATED_BY, NAME from TEST_GROUP where ID = ?";
        tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            TypedQuery<Group> q = em.createNativeQuery(nativeQuery, Group.class);
            q.setParameter(1, group.getId().toString());

            Group g = q.getResultList().get(0);

            g.setName("New Name");
            tx.commit();
        } finally {
            tx.end();
        }

        // load again, check
        Group g2;
        tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            g2 = em.find(Group.class, group.getId());

            assertNotNull(g2);
            assertEquals("New Name", g2.getName());

            tx.commit();
        } finally {
            tx.end();
        }
    }

    /*
     * Test that entity which is loaded by native typed query,
     * is MANAGED, by persisting another entity linked to it.
     */
    @Test
    public void testTypedNativeQueryByPersistingAnotherEntity() {
        Group group = new Group();
        groupId = group.getId();
        group.setName("Old Name");
        Transaction tx = persistence.createTransaction();
        try {
            persistence.getEntityManager().persist(group);
            tx.commit();
        } finally {
            tx.end();
        }

        String nativeQuery = "select ID, CREATE_TS, CREATED_BY, UPDATE_TS, UPDATED_BY, VERSION, NAME from TEST_GROUP where ID = ?";
        tx = persistence.createTransaction();
        Group g;
        try {
            EntityManager em = persistence.getEntityManager();

            TypedQuery<Group> q = em.createNativeQuery(nativeQuery, Group.class);
            q.setParameter(1, group.getId().toString());
            g = q.getResultList().get(0);
            tx.commit();
        } finally {
            tx.end();
        }

        User user = new User();
        userId = user.getId();
        user.setLogin("typednativesqlquery");
        user.setGroup(g);
        user.setName("Test");

        tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            em.persist(user);
            tx.commit();
        } finally {
            tx.end();
        }
        // gets persisted without error
    }

    @Test
    public void testAssigningView() throws Exception {
        String nativeQuery = "select ID, CREATE_TS, CREATED_BY, UPDATE_TS, UPDATED_BY, VERSION, NAME from TEST_GROUP where ID = ?";

        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            TypedQuery<Group> q = em.createNativeQuery(nativeQuery, Group.class);
            q.setParameter(1, UUID.randomUUID());
            try {
                q.setView(Group.class, "group.browse");
                fail();
            } catch (UnsupportedOperationException e) {
                // ok
            }
        }
    }

    @Test
    public void testEclipseLinkInabilityToMapToString() {
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            javax.persistence.Query query = em.getDelegate().createNativeQuery("select LOGIN from TEST_USER where ID = ?", String.class);
            query.setParameter(1, UUID.randomUUID());
            query.getResultList();
            fail();
        } catch (Exception e) {
            // ok
        }
    }
}
