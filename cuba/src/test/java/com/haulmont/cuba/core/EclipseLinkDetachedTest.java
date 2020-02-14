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
import com.haulmont.cuba.core.model.common.Role;
import com.haulmont.cuba.core.model.common.User;
import com.haulmont.cuba.core.model.common.UserRole;
import com.haulmont.cuba.core.testsupport.CoreTest;
import com.haulmont.cuba.core.testsupport.TestSupport;
import io.jmix.core.Metadata;
import io.jmix.core.FetchPlan;
import io.jmix.data.EntityManager;
import io.jmix.data.Persistence;
import io.jmix.data.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.UUID;

import static com.haulmont.cuba.core.testsupport.TestSupport.reserialize;
import static org.junit.jupiter.api.Assertions.*;

@CoreTest
public class EclipseLinkDetachedTest {
    @Inject
    private Persistence persistence;

    private UUID userId;
    private UUID userRoleId;
    private Group group;
    private Role role;

    @BeforeEach
    public void setUp() throws Exception {
        Transaction tx = persistence.createTransaction();

        try {
            EntityManager em = persistence.getEntityManager();

            group = new Group();
            group.setName("Group");
            em.persist(group);

            User user = new User();
            userId = user.getId();
            user.setName("testUser");
            user.setLogin("testLogin");
            user.setPosition("manager");
            user.setGroup(group);
            em.persist(user);

            role = new Role();
            role.setName("role1");
            em.persist(role);

            UserRole userRole = new UserRole();
            userRoleId = userRole.getId();
            userRole.setUser(user);
            userRole.setRole(role);
            em.persist(userRole);

            tx.commit();
        } finally {
            tx.end();
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        TestSupport.deleteRecord("TEST_USER_ROLE", userRoleId);
        TestSupport.deleteRecord("TEST_USER", userId);
        TestSupport.deleteRecord(group);
        TestSupport.deleteRecord(role);
    }

    @Test
    public void testNotSerialized() throws Exception {
        Transaction tx;
        EntityManager em;
        User user;
        tx = persistence.createTransaction();
        try {
            em = persistence.getEntityManager();
            user = em.find(User.class, userId);
            assertNotNull(user);
            tx.commit();
        } finally {
            tx.end();
        }

        assertEquals("testUser", user.getName());

        try {
            user.getGroup();
            fail();
        } catch (Exception ignored) {
        }

        try {
            user.getUserRoles().size();
            fail();
        } catch (Exception ignored) {
        }
    }

    @Test
    public void testSerialized() throws Exception {
        Transaction tx;
        EntityManager em;
        User user;
        tx = persistence.createTransaction();
        try {
            em = persistence.getEntityManager();
            user = em.find(User.class, userId);
            assertNotNull(user);
            tx.commit();
        } finally {
            tx.end();
        }

        user = reserialize(user);

        assertEquals("testUser", user.getName());

        // exception on getting not loaded references
        try {
            assertNotNull(user.getGroup());
            fail();
            assertEquals(1, user.getUserRoles().size());
            fail();
        } catch (Exception ignored) {
        }
    }

    @Test
    public void testNotSerializedFetchGroup() throws Exception {
        Transaction tx;
        EntityManager em;
        User user;
        tx = persistence.createTransaction();
        try {
            em = persistence.getEntityManager();
            FetchPlan view = new FetchPlan(User.class).addProperty("login")
                    .setLoadPartialEntities(true);
            user = em.find(User.class, userId, view);
            assertNotNull(user);
            tx.commit();
        } finally {
            tx.end();
        }

        assertEquals("testLogin", user.getLogin());

        // unfetched
        try {
            user.getName();
            fail();
        } catch (IllegalStateException ignored) {
        }
        try {
            user.getGroup();
            fail();
        } catch (IllegalStateException ignored) {
        }
    }

    @Test
    public void testSerializedFetchGroup() throws Exception {
        Transaction tx;
        EntityManager em;
        User user;
        tx = persistence.createTransaction();
        try {
            em = persistence.getEntityManager();
            FetchPlan view = new FetchPlan(User.class).addProperty("login")
                    .setLoadPartialEntities(true);
            user = em.find(User.class, userId, view);
            assertNotNull(user);
            tx.commit();
        } finally {
            tx.end();
        }

        user = reserialize(user);

        assertEquals("testLogin", user.getLogin());
        // exception on getting not loaded references
        try {
            user.getName();
            fail();
        } catch (Exception ignored) {
        }
        try {
            user.getGroup();
            fail();
        } catch (Exception ignored) {
        }
        try {
            user.getUserRoles().size();
            fail();
        } catch (Exception ignored) {
        }
    }

    @Test
    public void testSerializedFetchGroupMerge() throws Exception {
        Transaction tx;
        EntityManager em;
        User user;
        tx = persistence.createTransaction();
        try {
            em = persistence.getEntityManager();
            FetchPlan view = new FetchPlan(User.class).addProperty("login")
                    .setLoadPartialEntities(true);
            user = em.find(User.class, userId, view);
            assertNotNull(user);

            tx.commit();
        } finally {
            tx.end();
        }
        user = reserialize(user);

        assertEquals("testLogin", user.getLogin());
        // exception on getting not loaded references
        try {
            user.getName();
            fail();
        } catch (Exception ignored) {
        }
        try {
            user.getGroup();
            fail();
        } catch (Exception ignored) {
        }
        try {
            user.getUserRoles().size();
            fail();
        } catch (Exception ignored) {
        }

        user.setLogin("testLogin-1");

        // merge
        tx = persistence.createTransaction();
        try {
            em = persistence.getEntityManager();
            user = em.merge(user);

            tx.commit();
        } finally {
            tx.end();
        }
        user = reserialize(user);

        assertEquals("testLogin-1", user.getLogin());
        // loaded by mapping rules
        assertEquals("testUser", user.getName());
        // exception on getting not loaded references
        try {
            user.getGroup();
            fail();
        } catch (Exception ignored) {
        }
        try {
            user.getUserRoles().size();
            fail();
        } catch (Exception ignored) {
        }

        // find without view
        tx = persistence.createTransaction();
        try {
            em = persistence.getEntityManager();
            user = em.find(User.class, userId);
            assertNotNull(user);
            tx.commit();
        } finally {
            tx.end();
        }
        user = reserialize(user);

        assertEquals("testLogin-1", user.getLogin());
        assertEquals("testUser", user.getName());
    }
}
