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
import com.haulmont.cuba.core.model.common.EntityDiff;
import com.haulmont.cuba.core.model.common.Server;
import com.haulmont.cuba.core.testsupport.CoreTest;
import io.jmix.core.EntityStates;
import io.jmix.core.FetchPlan;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@CoreTest
public class PersistenceHelperTest {
    @Autowired
    private Persistence persistence;

    @Autowired
    private EntityStates entityStates;

    @AfterEach
    public void tearDown() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(persistence.getDataSource());
        jdbcTemplate.update("delete from TEST_SERVER");
    }

    @Test
    public void testEntityStates() {
        try {
            entityStates.isNew(null);
            fail("isNew() should not accept null");
        } catch (Exception e) {
            //
        }
        try {
            entityStates.isManaged(null);
            fail("isManaged() should not accept null");
        } catch (Exception e) {
            //
        }
        try {
            entityStates.isDetached(null);
            fail("isDetached() should not accept null");
        } catch (Exception e) {
            //
        }

        assertTrue(entityStates.isNew(new EntityDiff(null)));

        UUID id;
        Server server;
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            assertNotNull(em);
            server = new Server();

            assertTrue(entityStates.isNew(server));
            assertFalse(entityStates.isManaged(server));
            assertFalse(entityStates.isDetached(server));

            id = server.getId();
            server.setName("localhost");
            server.setRunning(true);
            em.persist(server);
            assertTrue(entityStates.isNew(server));
            assertTrue(entityStates.isManaged(server));
            assertFalse(entityStates.isDetached(server));

            tx.commit();
        } finally {
            tx.end();
        }
        assertFalse(entityStates.isNew(server));
        assertFalse(entityStates.isManaged(server));
        assertTrue(entityStates.isDetached(server));


        tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            server = em.find(Server.class, id);
            assertNotNull(server);
            assertEquals(id, server.getId());

            assertFalse(entityStates.isNew(server));
            assertTrue(entityStates.isManaged(server));
            assertFalse(entityStates.isDetached(server));

            server.setRunning(false);

            tx.commit();
        } finally {
            tx.end();
        }
        assertFalse(entityStates.isNew(server));
        assertFalse(entityStates.isManaged(server));
        assertTrue(entityStates.isDetached(server));

        tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            server = em.merge(server);

            assertFalse(entityStates.isNew(server));
            assertTrue(entityStates.isManaged(server));
            assertFalse(entityStates.isDetached(server));

            tx.commit();
        } finally {
            tx.end();
        }


        tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            server = em.find(Server.class, id);
            assertNotNull(server);
            assertEquals(id, server.getId());

            em.remove(server);

            assertFalse(entityStates.isNew(server));
            assertTrue(entityStates.isManaged(server));  // is it correct?
            assertFalse(entityStates.isDetached(server));

            tx.commit();
        } finally {
            tx.end();
        }

        assertFalse(entityStates.isNew(server));
        assertFalse(entityStates.isManaged(server));
        assertTrue(entityStates.isDetached(server)); // is it correct?
    }

    @Test
    public void testCheckLoaded() {
        Server server = new Server();

        persistence.runInTransaction((em) -> {
            em.persist(server);
        });

        FetchPlan view = new View(Server.class).addProperty("name").addProperty("data")
                .setLoadPartialEntities(true);
        Server reloadedServer = persistence.callInTransaction((em) -> {
            return em.find(Server.class, server.getId(), view);
        });

        entityStates.checkLoaded(reloadedServer, "name"); // fine

        try {
            entityStates.checkLoaded(reloadedServer, "data", "running");
            Assertions.fail("Must throw exception");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Server.running"));
        }
    }
}
