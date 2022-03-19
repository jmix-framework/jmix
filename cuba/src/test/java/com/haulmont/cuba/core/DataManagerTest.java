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

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.CommitContext;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.model.common.Group;
import com.haulmont.cuba.core.model.common.Server;
import com.haulmont.cuba.core.model.common.User;
import com.haulmont.cuba.core.testsupport.CoreTest;
import com.haulmont.cuba.core.testsupport.TestSupport;
import io.jmix.core.DevelopmentException;
import io.jmix.core.Entity;
import io.jmix.core.Metadata;
import io.jmix.core.TimeSource;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.persistence.TemporalType;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@CoreTest
public class DataManagerTest {
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected Persistence persistence;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected TestSupport testSupport;

    protected User user;
    protected Group group;

    @BeforeEach
    public void setUp() {
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();

            group = metadata.create(Group.class);
            group.setName("Group");
            em.persist(group);

            user = metadata.create(User.class);
            user.setName("testUser");
            user.setLogin("admin");
            user.setPassword("000");
            user.setGroup(group);
            em.persist(user);

            tx.commit();
        }
    }

    @AfterEach
    public void cleanup() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(persistence.getDataSource());
        jdbcTemplate.update("delete from TEST_SERVER");
        testSupport.deleteRecord(user);
        testSupport.deleteRecord(group);
    }

    @Test
    public void test() {
        Server server = new Server();
        UUID id = server.getId();
        server.setName("localhost");
        server.setRunning(true);

        dataManager.commit(new CommitContext(Collections.<Entity>singleton(server)));

        LoadContext<Server> loadContext = LoadContext.create(Server.class).setId(id);

        server = dataManager.load(loadContext);
        assertEquals("localhost", server.getName());

        server.setName("krivopustov");
        dataManager.commit(new CommitContext(Collections.<Entity>singleton(server)));
    }

    @Test
    public void testLoad() {
        Server server = new Server();
        UUID id = server.getId();
        server.setName("localhost");
        server.setRunning(true);

        dataManager.commit(new CommitContext(Collections.<Entity>singleton(server)));

        LoadContext<Server> loadContext = LoadContext.create(Server.class).setId(id);

        server = dataManager.load(loadContext);
        assertEquals("localhost", server.getName());
    }

    @Test
    public void testLoadList() {
        Server server = new Server();
        server.setName("localhost");
        server.setRunning(true);

        dataManager.commit(new CommitContext(Collections.<Entity>singleton(server)));

        LoadContext<Server> loadContext = LoadContext.create(Server.class);
        loadContext.setQueryString("select s from " + metadata.getClass(Server.class).getName() + " s");

        List<Server> list = dataManager.loadList(loadContext);
        assertTrue(list.size() > 0);
    }

    @Test
    public void testLoadListById() {
        Server server = new Server();
        UUID id = server.getId();
        server.setName("localhost");
        server.setRunning(true);

        dataManager.commit(new CommitContext(Collections.<Entity>singleton(server)));

        LoadContext<Server> loadContext = LoadContext.create(Server.class).setId(id);

        List<Server> list = dataManager.loadList(loadContext);
        assertTrue(list.size() == 1);
    }

    @Test
    public void testAssociatedResult() throws Exception {
        LoadContext<Group> loadContext = LoadContext.create(Group.class);
        loadContext.setQueryString("select u.group from test$User u where u.id = :userId")
                .setParameter("userId", user.getId());

        List<Group> groups = dataManager.loadList(loadContext);
        assertEquals(1, groups.size());
    }

    @Test
    public void testLoadListCaseInsensitive() {
        Server server = new Server();
        server.setName("LocalHost");
        server.setRunning(true);

        dataManager.commit(new CommitContext(Collections.<Entity>singleton(server)));

        LoadContext<Server> loadContext = LoadContext.create(Server.class);
        loadContext.setQueryString("select s from test$Server s where s.name like :name")
                .setParameter("name", "(?i)%loc%host%");

        List<Server> list = dataManager.loadList(loadContext);
        assertTrue(list.size() > 0);
    }

    @Test
    public void testLoadListCaseInsensitiveLower() {
        Server server = new Server();
        server.setName("LocalHost");
        server.setRunning(true);

        DataManager dataManager = AppBeans.get(DataManager.NAME);
        dataManager.commit(new CommitContext(Collections.<Entity>singleton(server)));

        LoadContext<Server> loadContext = LoadContext.create(Server.class);
        loadContext.setQueryString("select s from test$Server s where s.name like :name")
                .setParameter("name", "(?i)%localhost%");

        List<Server> list = dataManager.loadList(loadContext);
        assertTrue(list.size() > 0);
    }

    @Test
    public void testUnexistingQueryParameters() throws Exception {
        LoadContext<User> loadContext = LoadContext.create(User.class).setQuery(
                LoadContext.createQuery("select u from test$User u where u.login = :login").setParameter("name", "admin"));

        try {
            dataManager.loadList(loadContext);
            fail("DataService must throw exception for nonexistent parameters");
        } catch (Exception e) {
            // ok
        }

        loadContext = LoadContext.create(User.class).setQuery(
                LoadContext.createQuery("select u from test$User u where u.login = :login").setParameter("login", "admin"));
        List<User> list = dataManager.loadList(loadContext);
        assertEquals(1, list.size());
    }

    @Test
    public void testGetCount() throws Exception {
        LoadContext<User> loadContext = LoadContext.create(User.class).setQuery(
                LoadContext.createQuery("select u from test$User u where u.login = :login").setParameter("login", "admin"));

        long count = dataManager.getCount(loadContext);
        assertEquals(1, count);

        loadContext.getQuery().setParameter("login", "cc1aa09f-c5d5-4bd1-896c-cb774d2e2898");
        count = dataManager.getCount(loadContext);
        assertEquals(0, count);
    }

    @Test
    public void testTemporalType() throws Exception {
        Date nextYear = DateUtils.addYears(AppBeans.get(TimeSource.class).currentTimestamp(), 1);
        LoadContext<User> loadContext = LoadContext.create(User.class).setQuery(
                LoadContext.createQuery("select u from test$User u where u.createTs = :ts")
                        .setParameter("ts", nextYear, TemporalType.DATE));

        List<User> users = dataManager.loadList(loadContext);
        assertTrue(users.isEmpty());
    }

    @Test
    public void testExtendedLoadContext() throws Exception {
        LoadContext<User> loadContext = new MyLoadContext<>(User.class, "test").setQuery(
                LoadContext.createQuery("select u from test$User u where u.login = :login").setParameter("login", "admin"));

        long count = dataManager.getCount(loadContext);
        assertEquals(1, count);

    }

    @Test
    public void testDataManagerLoadOneRecord() {
        Server server1 = new Server();
        server1.setName("app1");
        server1.setRunning(false);

        Server server2 = new Server();
        server2.setName("app2");
        server2.setRunning(false);

        dataManager.commit(new CommitContext(server1, server2));

        LoadContext<Server> lc = new LoadContext<>(Server.class);
        lc.setQueryString("select s from test$Server s order by s.name")
                .setMaxResults(1);

        Server latest = dataManager.load(lc);
        assertEquals(server1, latest);
    }

    @Test
    public void testNonEntityResults() throws Exception {
        // fails on aggregates
        LoadContext context = LoadContext.create(Server.class).setQuery(LoadContext.createQuery("select count(s) from test$Server s"));
        try {
            dataManager.load(context);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof DevelopmentException);
        }

        // fails on single attributes
        context = LoadContext.create(Server.class).setQuery(LoadContext.createQuery("select s.name from test$Server s"));
        try {
            dataManager.load(context);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof DevelopmentException);
        }
    }

    @Test
    public void testDiscardCommitted() throws Exception {
        Server server = new Server();
        server.setName("localhost");
        CommitContext commitContext = new CommitContext(server);
        commitContext.setDiscardCommitted(true);

        Set<Entity> committed = dataManager.commit(commitContext);
        assertTrue(committed.isEmpty());

        Server saved = dataManager.load(LoadContext.create(Server.class).setId(server.getId()));
        assertNotNull(saved);
    }

    public static class MyLoadContext<E extends Entity> extends LoadContext<E> {

        private String info;

        public MyLoadContext() {
        }

        public MyLoadContext(Class<E> javaClass, String info) {
            super(javaClass);
            this.info = info;
        }

        @Override
        public MyLoadContext<?> copy() {
            MyLoadContext<?> copy = (MyLoadContext) super.copy();
            copy.info = info;
            return copy;
        }
    }
}
