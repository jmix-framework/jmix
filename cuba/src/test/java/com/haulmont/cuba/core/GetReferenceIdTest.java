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

import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.core.model.common.Group;
import com.haulmont.cuba.core.model.common.Role;
import com.haulmont.cuba.core.model.common.User;
import com.haulmont.cuba.core.model.common.UserRole;
import com.haulmont.cuba.core.testsupport.CoreTest;
import com.haulmont.cuba.core.testsupport.TestSupport;
import io.jmix.core.Metadata;
import io.jmix.data.ReferenceIdProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@CoreTest
public class GetReferenceIdTest {
    @Autowired
    private Persistence persistence;
    @Autowired
    private Metadata metadata;
    @Autowired
    private TestSupport testSupport;

    private User user;
    private Group group;

    @BeforeEach
    public void setUp() throws Exception {
        try (Transaction tx = persistence.createTransaction()) {
            group = new Group();
            group.setName("test group");
            persistence.getEntityManager().persist(group);

            user = metadata.create(User.class);
            user.setName("test user");
            user.setLogin("test login");
            user.setGroup(group);
            persistence.getEntityManager().persist(user);

            tx.commit();
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        testSupport.deleteRecord(user, group);
    }

    @Test
    public void testWithFetchGroup() throws Exception {
        User user = null;

        // not in a view
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();

            Query q = em.createQuery("select u from test$User u where u.id = ?1");
            q.setView(
                    new View(User.class, false)
                            .addProperty("login")
                            .addProperty("userRoles", new View(UserRole.class)
                                    .addProperty("role", new View(Role.class)
                                            .addProperty("name")))
                            .setLoadPartialEntities(true)
            );
            q.setParameter(1, this.user.getId());
            List<User> list = q.getResultList();
            if (!list.isEmpty()) {
                user = list.get(0);

                ReferenceIdProvider.RefId refId = persistence.getTools().getReferenceId(user, "group");
                assertFalse(refId.isLoaded());
                try {
                    refId.getValue();
                    fail();
                } catch (IllegalStateException e) {
                    // ok
                }
            }
            tx.commit();
        }
        user = testSupport.reserialize(user);
        assertNotNull(user);
        assertNotNull(user.getUserRoles());
        user.getUserRoles().size();

        // in a view
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();

            Query q = em.createQuery("select u from test$User u where u.id = ?1");
            q.setView(
                    new View(User.class, false)
                            .addProperty("login")
                            .addProperty("group", new View(Group.class)
                                    .addProperty("name"))
                            .addProperty("userRoles", new View(UserRole.class)
                                    .addProperty("role", new View(Role.class)
                                            .addProperty("name")))
                            .setLoadPartialEntities(true)
            );
            q.setParameter(1, this.user.getId());
            List<User> list = q.getResultList();
            if (!list.isEmpty()) {
                user = list.get(0);

                ReferenceIdProvider.RefId refId = persistence.getTools().getReferenceId(user, "group");
                assertTrue(refId.isLoaded());
                assertEquals(group.getId(), refId.getValue());
            }
            tx.commit();
        }
        user = testSupport.reserialize(user);
        assertNotNull(user);
        assertNotNull(user.getUserRoles());
        user.getUserRoles().size();

    }

    @Test
    public void testWithoutFetchGroup() throws Exception {
        User user = null;

        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();

            TypedQuery<User> q = em.createQuery("select u from test$User u where u.id = ?1", User.class);
            q.setParameter(1, this.user.getId());
            List<User> list = q.getResultList();
            if (!list.isEmpty()) {
                user = list.get(0);

                ReferenceIdProvider.RefId refId = persistence.getTools().getReferenceId(user, "group");
                assertTrue(refId.isLoaded());
                assertEquals(group.getId(), refId.getValue());
            }
            tx.commit();
        }
        try {
            persistence.getTools().getReferenceId(user, "group");
            fail();
        } catch (IllegalStateException e) {
            // ok
        }
        user = testSupport.reserialize(user);
        assertNotNull(user);
        try {
            persistence.getTools().getReferenceId(user, "group");
            fail();
        } catch (IllegalStateException e) {
            // ok
        }
    }
}
