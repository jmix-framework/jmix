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
import com.haulmont.cuba.core.model.common.Server;
import com.haulmont.cuba.core.model.common.User;
import com.haulmont.cuba.core.testsupport.CoreTest;
import com.haulmont.cuba.core.testsupport.TestSupport;
import io.jmix.core.FetchPlan;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@CoreTest
public class PersistenceTest {
    @Autowired
    private Persistence persistence;
    @Autowired
    private TestSupport testSupport;

    private UUID userId, groupId;

    @BeforeEach
    public void setUp() {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            Group group = new Group();
            groupId = group.getId();
            group.setName("group");
            em.persist(group);

            User user = new User();
            userId = user.getId();
            user.setName("testUser");
            user.setLogin("testLogin");
            user.setGroup(group);
            em.persist(user);

            tx.commit();
        } finally {
            tx.end();
        }
    }

    @AfterEach
    public void tearDown() {
        testSupport.deleteRecord("TEST_USER", userId);
        testSupport.deleteRecord("TEST_GROUP", groupId);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(persistence.getDataSource());
        jdbcTemplate.update("DELETE FROM TEST_SERVER");
    }

    private void raiseException() {
        throw new RuntimeException("test_ex");
    }

    @Test
    public void testLoadByCombinedView() {
        User user;
        Transaction tx = persistence.createTransaction();
        try {
            // load by single view

            EntityManager em = persistence.getEntityManager();

            user = em.find(User.class, userId,
                    new View(User.class, false).addProperty("login").setLoadPartialEntities(true));

            assertTrue(persistence.getTools().isLoaded(user, "login"));
            assertFalse(persistence.getTools().isLoaded(user, "name"));

            tx.commitRetaining();

            // load by combined view

            em = persistence.getEntityManager();

            user = em.find(User.class, userId,
                    new View(User.class, false).addProperty("login").setLoadPartialEntities(true),
                    new View(User.class, false).addProperty("name").setLoadPartialEntities(true)
            );

            assertTrue(persistence.getTools().isLoaded(user, "login"));
            assertTrue(persistence.getTools().isLoaded(user, "name"));

            tx.commitRetaining();

            // load by complex combined view

            em = persistence.getEntityManager();

            user = em.find(User.class, userId,
                    new View(User.class, false).addProperty("login").setLoadPartialEntities(true),
                    new View(User.class, false).addProperty("group", new View(Group.class).addProperty("name")).setLoadPartialEntities(true)
            );

            assertTrue(persistence.getTools().isLoaded(user, "login"));
            assertFalse(persistence.getTools().isLoaded(user, "name"));
            assertTrue(persistence.getTools().isLoaded(user, "group"));
            assertTrue(persistence.getTools().isLoaded(user.getGroup(), "name"));

            tx.commit();
        } finally {
            tx.end();
        }
    }

    @Test
    public void testMergeNotLoaded() throws Exception {
        User user;
        Group group;

        Transaction tx = persistence.createTransaction();
        try {
            User transientUser = new User();
            transientUser.setId(userId);
            transientUser.setName("testUser1");

            EntityManager em = persistence.getEntityManager();
            em.merge(transientUser);

            tx.commitRetaining();

            em = persistence.getEntityManager();
            user = em.find(User.class, userId);
            assertNotNull(user);
            group = user.getGroup();
        } finally {
            tx.end();
        }

        assertEquals(userId, user.getId());
        assertEquals("testUser1", user.getName());
        assertEquals("testLogin", user.getLogin());
        assertNotNull(group);
    }

    @Test
    public void testDirtyFields() throws Exception {
        PersistenceTools persistenceTools = persistence.getTools();

        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            assertNotNull(em);
            Server server = new Server();
            UUID id = server.getId();
            server.setName("localhost");
            server.setRunning(true);
            em.persist(server);

            assertTrue(persistenceTools.isDirty(server, "name", "running"));
            assertNull(persistenceTools.getOldValue(server, "data"));

            tx.commitRetaining();

            em = persistence.getEntityManager();
            server = em.find(Server.class, id);
            assertNotNull(server);
            server.setData("testData");

            assertTrue(persistenceTools.isDirty(server));

            Set<String> dirtyFields = persistenceTools.getDirtyFields(server);
            assertTrue(dirtyFields.contains("data"));
            assertTrue(persistenceTools.isDirty(server, "data"));
            assertNull(persistenceTools.getOldValue(server, "data"));

            tx.commitRetaining();

            em = persistence.getEntityManager();
            server = em.find(Server.class, id);
            assertNotNull(server);

            server.setData("testData1");
            assertEquals("testData", persistenceTools.getOldValue(server, "data"));

            tx.commit();
        } finally {
            tx.end();
        }
    }

    /**
     * OpenJPA silently ignores setting null in nullable=false attribute.
     */
    @Test
    public void testNonNullAttribute() throws Exception {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            User user = em.find(User.class, userId);
            assertNotNull(user);
            user.setLogin(null);
            user.setName(null);
            tx.commitRetaining();
            fail();

// Old OpenJPA behaviour
//            em = persistence.getEntityManager();
//            user = em.find(User.class, userId);
//            assertNotNull(user);
//            assertNotNull(user.getLogin()); // null was not saved
//            assertNull(user.getName());     // null was saved

            tx.commit();
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("NOT NULL check constraint"));
        } finally {
            tx.end();
        }

    }

    @Test
    public void testFind() throws Exception {
        try (Transaction tx = persistence.createTransaction()) {
            User user = persistence.getEntityManager().find(User.class, userId);
            assertNotNull(user);

            tx.commit();
        }
    }

    @Test
    public void testRepeatingReloadNoView() throws Exception {
        persistence.runInTransaction((em) -> {
            User u = loadUserByName(em, null);

            u.setLanguage("ru");

            u = loadUserByName(em, null);

            assertEquals("ru", u.getLanguage());
        });

        User changedUser = persistence.callInTransaction((em) -> em.find(User.class, userId));
        assertEquals("ru", changedUser.getLanguage());
    }

    @Test
    public void testLostChangeOnReloadWithView1() throws Exception {
        persistence.runInTransaction((em) -> {
            User u = loadUserByName(em, FetchPlan.LOCAL);

            u.setLanguage("en");

            u = loadUserByName(em, FetchPlan.LOCAL);

            assertEquals("en", u.getLanguage());
        });
    }

    @Test
    public void testLostChangeOnReloadWithView2() throws Exception {
        persistence.runInTransaction((em) -> {
            User u = loadUserByName(em, FetchPlan.LOCAL);

            u.setLanguage("fr");

            u = loadUserByName(em, FetchPlan.LOCAL);
        });

        User changedUser = persistence.callInTransaction((em) -> em.find(User.class, userId));
        assertEquals("fr", changedUser.getLanguage());
    }

    @Test
    public void testLostChangesOnEmReload() throws Exception {
        User user = persistence.callInTransaction((em) -> em.find(User.class, userId));

        persistence.runInTransaction((em) -> {
            User u = em.merge(user);
            u.setEmail("abc@example.com");

            u = em.reload(u, FetchPlan.LOCAL);
        });

        User changedUser = persistence.callInTransaction((em) -> em.find(User.class, userId));
        assertEquals("abc@example.com", changedUser.getEmail());
    }

    private User loadUserByName(EntityManager em, @Nullable String viewName) {
        TypedQuery<User> q = em.createQuery("select u from test$User u where u.name = :name", User.class)
                .setParameter("name", "testUser");

        if (viewName != null) {
            q.setViewName(viewName);
        }
        return q.getFirstResult();
    }
}
