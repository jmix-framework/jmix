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

package com.haulmont.cuba.core.entity_cache;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.haulmont.cuba.core.*;
import com.haulmont.cuba.core.entity.AppFolder;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.core.model.common.*;
import com.haulmont.cuba.core.testsupport.*;
import io.jmix.core.FetchPlan;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.eclipselink.impl.entitycache.QueryCache;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.internal.jpa.EntityManagerFactoryDelegate;
import org.eclipse.persistence.jpa.JpaCache;
import org.eclipse.persistence.sessions.server.ServerSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.rules.TestRule;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import javax.persistence.EntityManagerFactory;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static com.haulmont.cuba.core.testsupport.TestAssertions.assertFail;
import static org.junit.Assert.*;

@CoreTest
@TestPropertySource("classpath:/com/haulmont/cuba/core/test-entitycache-app.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class QueryCacheTestClass {
    @RegisterExtension
    public TestRule testNamePrinter = new TestNamePrinter();

    @Autowired
    private Persistence persistence;
    @Autowired
    private Metadata metadata;
    @Autowired
    private TestSupport testSupport;
    @Autowired
    private io.jmix.core.DataManager dataManager;

    @Autowired
    private TestAdditionalCriteriaProvider testAdditionalCriteriaProvider;

    private JpaCache cache;
    private QueryCache queryCache;

    private final TestAppender appender;
    private Group group;
    private User user;
    private User user2;
    private User user3;
    private Role role, role1;
    private UserRole userRole;
    private User newUser;
    private AppFolder appFolder;
    private UserSetting userSetting;

    public QueryCacheTestClass() {
        appender = new TestAppender();
        appender.start();
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = context.getLogger("eclipselink.logging.sql");
        logger.addAppender(appender);
    }

    @BeforeEach
    public void setUp() throws Exception {
        queryCache = AppBeans.get(QueryCache.class);
        try (Transaction tx = persistence.createTransaction()) {
            EntityManagerFactory emf = persistence.getEntityManager().getDelegate().getEntityManagerFactory();

            assertTrue(metadata.getTools().isCacheable(metadata.getClass(User.class)));
            assertFalse(metadata.getTools().isCacheable(metadata.getClass(UserSubstitution.class)));

            ServerSession serverSession = ((EntityManagerFactoryDelegate) emf).getServerSession();
            ClassDescriptor descriptor = serverSession.getDescriptor(User.class);
            assertEquals(500, descriptor.getCachePolicy().getIdentityMapSize());

            this.cache = (JpaCache) emf.getCache();

            group = metadata.create(Group.class);
            group.setName("group-" + group.getId());
            persistence.getEntityManager().persist(group);

            user = metadata.create(User.class);
            user.setLogin("ECTest-" + user.getId());
            user.setPassword("111");
            user.setName("1");
            user.setGroup(group);
            persistence.getEntityManager().persist(user);

            user2 = metadata.create(User.class);
            user2.setLogin("ECTest-" + user2.getId());
            user2.setPassword("111");
            user2.setName("2");
            user2.setGroup(group);
            persistence.getEntityManager().persist(user2);

            user3 = metadata.create(User.class);
            user3.setLogin("ECTest-" + user3.getId());
            user3.setPassword("111");
            user3.setGroup(group);
            persistence.getEntityManager().persist(user3);

            role = metadata.create(Role.class);
            role.setName("TestRole");
            role.setDescription("Test role descr");
            persistence.getEntityManager().persist(role);

            userRole = metadata.create(UserRole.class);
            userRole.setRole(role);
            userRole.setUser(user);
            persistence.getEntityManager().persist(userRole);

            userSetting = metadata.create(UserSetting.class);
            userSetting.setUser(user);
            persistence.getEntityManager().persist(userSetting);

            tx.commit();
        }

        try (Transaction tx = persistence.createTransaction()) {
            persistence.getEntityManager().remove(user3);
            tx.commit();
        }
        cache.clear();
        queryCache.invalidateAll();
    }

    @AfterEach
    public void tearDown() throws Exception {
        testSupport.deleteRecord(userSetting, userRole, role, user, user2);
        if (role1 != null)
            testSupport.deleteRecord(role1);
        if (newUser != null)
            testSupport.deleteRecord(newUser);
        if (user3 != null)
            testSupport.deleteRecord(user3);
        if (appFolder != null) {
            testSupport.deleteRecord("SYS_APP_FOLDER", "FOLDER_ID", appFolder.getId());
            testSupport.deleteRecord("SYS_FOLDER", "ID", appFolder.getId());
        }
        testSupport.deleteRecord(group);
    }

    @Test
    public void testSingleResultQueryByLoginPositionParameter() throws Exception {
        appender.clearMessages();

        User u;

        assertEquals(0, queryCache.size());
        u = getSingleResultUserByLoginPositional(user, null);
        assertEquals(this.user.getLogin(), u.getLogin());
        assertEquals(this.group, u.getGroup());
        assertEquals(1, queryCache.size());

        assertEquals(2, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group
        appender.clearMessages();

        u = getSingleResultUserByLoginPositional(user, null);
        assertEquals(this.user.getLogin(), u.getLogin());
        assertEquals(this.group, u.getGroup());
        assertEquals(1, queryCache.size());

        assertEquals(0, appender.filterMessages(m -> m.startsWith("> SELECT")).count());
        appender.clearMessages();

        u = getSingleResultUserByLoginPositional(user, query -> query.setCacheable(false));
        assertEquals(this.user.getLogin(), u.getLogin());
        assertEquals(this.group, u.getGroup());
        assertEquals(1, queryCache.size());

        assertEquals(1, appender.filterMessages(m -> m.contains("> SELECT")).count());
        appender.clearMessages();

        u = getSingleResultUserByLoginPositional(user2, null);
        assertEquals(this.user2.getLogin(), u.getLogin());
        assertEquals(this.group, u.getGroup());
        assertEquals(2, queryCache.size());

        assertEquals(1, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group
        appender.clearMessages();

        u = getSingleResultUserByLoginPositional(user2, null);
        assertEquals(this.user2.getLogin(), u.getLogin());
        assertEquals(this.group, u.getGroup());
        assertEquals(2, queryCache.size());

        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group
        appender.clearMessages();
    }

    @Test
    public void testSingleResultQueryForNotExistingUser() throws Exception {
        appender.clearMessages();
        User user = metadata.create(User.class);
        assertEquals(0, queryCache.size());
        try {
            getSingleResultUserByLoginPositional(user, null);
            fail();
        } catch (NoResultException e) {
            //It's OK
        }
        assertEquals(1, queryCache.size());
        assertEquals(1, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group
        appender.clearMessages();

        try {
            getSingleResultUserByLoginPositional(user, null);
            fail();
        } catch (NoResultException e) {
            //It's OK
        }
        assertEquals(1, queryCache.size());
        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count());
    }


    @Test
    public void testSingleResultQueryByLoginNamedParameter() throws Exception {
        appender.clearMessages();

        User u;
        assertEquals(0, queryCache.size());
        u = getSingleResultUserByLoginNamed(user, null);
        assertEquals(this.user.getLogin(), u.getLogin());
        assertEquals(this.group, u.getGroup());
        assertEquals(1, queryCache.size());

        assertEquals(2, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group
        appender.clearMessages();

        u = getSingleResultUserByLoginNamed(user, null);
        assertEquals(this.user.getLogin(), u.getLogin());
        assertEquals(this.group, u.getGroup());
        assertEquals(1, queryCache.size());

        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count());
        appender.clearMessages();

        u = getSingleResultUserByLoginNamed(user, query -> query.setCacheable(false));
        assertEquals(this.user.getLogin(), u.getLogin());
        assertEquals(this.group, u.getGroup());
        assertEquals(1, queryCache.size());

        assertEquals(1, appender.filterMessages(m -> m.contains("> SELECT")).count());
        appender.clearMessages();

        u = getSingleResultUserByLoginNamed(user2, null);
        assertEquals(this.user2.getLogin(), u.getLogin());
        assertEquals(this.group, u.getGroup());
        assertEquals(2, queryCache.size());

        assertEquals(1, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group
        appender.clearMessages();

        u = getSingleResultUserByLoginNamed(user2, null);
        assertEquals(this.user2.getLogin(), u.getLogin());
        assertEquals(this.group, u.getGroup());
        assertEquals(2, queryCache.size());

        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group
        appender.clearMessages();
    }

    @Test
    public void testDataManager() throws Exception {
        appender.clearMessages();
        User u;
        assertEquals(0, queryCache.size());

        u = dataManager_getResultListUserByLoginNamed();
        assertNotNull(u);
        assertEquals(this.user.getLogin(), u.getLogin());
        assertEquals(this.group, u.getGroup());
        assertEquals(1, queryCache.size());

        assertEquals(2, appender.filterMessages(m -> m.contains("> SELECT") && !m.contains("DYNAT_CATEGORY")).count()); // User, Group
        appender.clearMessages();

        u = dataManager_getResultListUserByLoginNamed();
        assertNotNull(u);
        assertEquals(this.user.getLogin(), u.getLogin());
        assertEquals(this.group, u.getGroup());
        assertEquals(1, queryCache.size());

        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count());
    }

    @Test
    public void testDataManagerWithSingleCondition() throws Exception {
        appender.clearMessages();
        User u;
        assertEquals(0, queryCache.size());

        u = dataManager_getResultListUserByLoginCondition();
        assertNotNull(u);
        assertEquals(this.user.getLogin(), u.getLogin());
        assertEquals(this.group, u.getGroup());
        assertEquals(1, queryCache.size());

        assertEquals(2, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group
        appender.clearMessages();

        u = dataManager_getResultListUserByLoginCondition();
        assertNotNull(u);
        assertEquals(this.user.getLogin(), u.getLogin());
        assertEquals(this.group, u.getGroup());
        assertEquals(1, queryCache.size());

        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count());
    }

    @Test
    public void testDataManagerWithMultipleCondition() throws Exception {
        appender.clearMessages();
        User u;
        assertEquals(0, queryCache.size());

        u = dataManager_getResultListUserByLoginAndNameCondition();
        assertNotNull(u);
        assertEquals(this.user.getLogin(), u.getLogin());
        assertEquals(this.group, u.getGroup());
        assertEquals(1, queryCache.size());

        assertEquals(2, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group
        appender.clearMessages();

        u = dataManager_getResultListUserByLoginAndNameCondition();
        assertNotNull(u);
        assertEquals(this.user.getLogin(), u.getLogin());
        assertEquals(this.group, u.getGroup());
        assertEquals(1, queryCache.size());

        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count());
    }

    @Test
    public void testSoftDeletion() throws Exception {
        appender.clearMessages();

        assertEquals(0, queryCache.size());

        User u = this.getResultListUserByLoginNamed(user3, true, null, null);

        assertNull(u);
        assertEquals(1, queryCache.size());
        assertEquals(1, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group
        appender.clearMessages();

        u = this.getResultListUserByLoginNamed(user3, true, null, null);

        assertNull(u);
        assertEquals(1, queryCache.size());
        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group
        appender.clearMessages();

        u = this.getResultListUserByLoginNamed(user3, true, em -> em.setSoftDeletion(false), null);

        assertNotNull(u);
        assertEquals(this.user3.getLogin(), u.getLogin());
        assertEquals(this.group, u.getGroup());
        assertEquals(2, queryCache.size());

        assertEquals(2, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group
        appender.clearMessages();

        u = this.getResultListUserByLoginNamed(user3, true, em -> em.setSoftDeletion(false), null);

        assertNotNull(u);
        assertEquals(this.user3.getLogin(), u.getLogin());
        assertEquals(this.group, u.getGroup());
        assertEquals(2, queryCache.size());

        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group
        appender.clearMessages();
    }

    @Test
    public void testNativeQuery() {
        appender.clearMessages();
        assertEquals(0, queryCache.size());
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            TypedQuery<User> query = em.createNativeQuery("select * from TEST_USER", User.class);
            query.setCacheable(true);
            query.getResultList();
            tx.commit();
        }
        assertEquals(0, queryCache.size());
    }

    @Test
    public void testLockModeQuery() throws Exception {
        appender.clearMessages();
        assertEquals(0, queryCache.size());
        getResultListUserByLoginNamed(user, false, null, query -> query.setLockMode(LockModeType.PESSIMISTIC_READ));
        assertEquals(0, queryCache.size());
    }

    @Test
    public void testNotEntityQuery() throws Exception {
        appender.clearMessages();
        assertEquals(0, queryCache.size());
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            Query query = em.createQuery("select u, u.name from test$User u where u.login = :login");
            query.setCacheable(true);
            query.setParameter("login", "ECTest-" + user.getId());
            query.getResultList();
            tx.commit();
        }
        assertEquals(0, queryCache.size());
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            Query query = em.createQuery("select u.name from test$User u where u.login = :login");
            query.setCacheable(true);
            query.setParameter("login", "ECTest-" + user.getId());
            query.getResultList();
            tx.commit();
        }
        assertEquals(0, queryCache.size());
    }

    @Test
    public void testQueryWithoutParams() throws Exception {
        appender.clearMessages();
        assertEquals(0, queryCache.size());
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            TypedQuery<User> query = em.createQuery("select u from test$User u where u.login like 'ECTest-%' order by u.name asc", User.class);
            query.setCacheable(true);
            query.setViewName("user.browse");
            List<User> result = query.getResultList();
            assertEquals(result.size(), 2);
            assertUserBrowseView(result.get(0));
            assertEquals(this.user.getLogin(), result.get(0).getLogin());
            assertEquals(this.group, result.get(0).getGroup());
            assertUserBrowseView(result.get(1));
            assertEquals(this.user2.getLogin(), result.get(1).getLogin());
            assertEquals(this.group, result.get(1).getGroup());
            tx.commit();
        }
        assertEquals(2, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group
        appender.clearMessages();
        assertEquals(1, queryCache.size());
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            TypedQuery<User> query = em.createQuery("select u from test$User u where u.login like 'ECTest-%' order by u.name asc", User.class);
            query.setCacheable(true);
            query.setViewName("user.browse");
            List<User> result = query.getResultList();
            assertEquals(result.size(), 2);
            assertUserBrowseView(result.get(0));
            assertEquals(this.user.getLogin(), result.get(0).getLogin());
            assertEquals(this.group, result.get(0).getGroup());
            assertUserBrowseView(result.get(1));
            assertEquals(this.user2.getLogin(), result.get(1).getLogin());
            assertEquals(this.group, result.get(1).getGroup());
            tx.commit();
        }
        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group
        assertEquals(1, queryCache.size());
        appender.clearMessages();
    }

    @Test
    public void testQueryPaging() throws Exception {
        appender.clearMessages();
        assertEquals(0, queryCache.size());
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            TypedQuery<User> query = em.createQuery("select u from test$User u where u.login like 'ECTest-%' order by u.name asc", User.class);
            query.setCacheable(true);
            query.setViewName("user.browse");
            query.setFirstResult(0);
            query.setMaxResults(1);
            List<User> result = query.getResultList();
            assertEquals(result.size(), 1);
            assertUserBrowseView(result.get(0));
            assertEquals(this.user.getLogin(), result.get(0).getLogin());
            assertEquals(this.group, result.get(0).getGroup());
            tx.commit();
        }
        assertEquals(2, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group
        appender.clearMessages();
        assertEquals(1, queryCache.size());

        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            TypedQuery<User> query = em.createQuery("select u from test$User u where u.login like 'ECTest-%' order by u.name asc", User.class);
            query.setCacheable(true);
            query.setViewName("user.browse");
            query.setFirstResult(0);
            query.setMaxResults(1);
            List<User> result = query.getResultList();
            assertEquals(result.size(), 1);
            assertUserBrowseView(result.get(0));
            assertEquals(this.user.getLogin(), result.get(0).getLogin());
            assertEquals(this.group, result.get(0).getGroup());
            tx.commit();
        }
        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group
        appender.clearMessages();
        assertEquals(1, queryCache.size());

        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            TypedQuery<User> query = em.createQuery("select u from test$User u where u.login like 'ECTest-%' order by u.name asc", User.class);
            query.setCacheable(true);
            query.setViewName("user.browse");
            query.setFirstResult(1);
            query.setMaxResults(1);
            List<User> result = query.getResultList();
            assertEquals(result.size(), 1);
            assertUserBrowseView(result.get(0));
            assertEquals(this.user2.getLogin(), result.get(0).getLogin());
            assertEquals(this.group, result.get(0).getGroup());
            tx.commit();
        }
        assertEquals(1, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group
        assertEquals(2, queryCache.size());
        appender.clearMessages();

        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            TypedQuery<User> query = em.createQuery("select u from test$User u where u.login like 'ECTest-%' order by u.name asc", User.class);
            query.setCacheable(true);
            query.setViewName("user.browse");
            query.setFirstResult(1);
            query.setMaxResults(1);
            List<User> result = query.getResultList();
            assertEquals(result.size(), 1);
            assertUserBrowseView(result.get(0));
            assertEquals(this.user2.getLogin(), result.get(0).getLogin());
            assertEquals(this.group, result.get(0).getGroup());
            tx.commit();
        }
        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group
        assertEquals(2, queryCache.size());

        appender.clearMessages();
    }

    @Test
    public void testResultListEmptyResult() throws Exception {
        appender.clearMessages();
        User user = metadata.create(User.class);
        assertEquals(0, queryCache.size());
        assertNull(getResultListUserByLoginNamed(user, false, null, null));

        assertEquals(1, queryCache.size());
        assertEquals(1, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group
        appender.clearMessages();

        assertNull(getResultListUserByLoginNamed(user, false, null, null));
        assertEquals(1, queryCache.size());
        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count());
    }

    @Test
    public void testStaleData_insert() throws Exception {
        appender.clearMessages();

        User u = getResultListUserByLoginNamed(user, true, null, null);
        assertEquals(2, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group
        appender.clearMessages();
        assertEquals(1, queryCache.size());

        newUser = metadata.create(User.class);
        newUser.setLogin("new user");
        newUser.setGroup(this.group);
        try (Transaction tx = persistence.createTransaction()) {
            persistence.getEntityManager().persist(newUser);
            tx.commit();
        }
        appender.clearMessages();
        assertEquals(0, queryCache.size());
        u = getResultListUserByLoginNamed(user, true, null, null);
        assertEquals(1, appender.filterMessages(m -> m.contains("> SELECT")).count());
    }

    @Test
    public void testStaleData_DM_insert() throws Exception {
        appender.clearMessages();

        User u = getResultListUserByLoginNamed(user, true, null, null);
        assertEquals(2, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group
        appender.clearMessages();
        assertEquals(1, queryCache.size());

        newUser = metadata.create(User.class);
        newUser.setLogin("new user");
        newUser.setGroup(this.group);

        dataManager.save(newUser);

        appender.clearMessages();
        assertEquals(0, queryCache.size());
        u = getResultListUserByLoginNamed(user, true, null, null);
        assertEquals(1, appender.filterMessages(m -> m.contains("> SELECT")).count());
    }

    @Test
    public void testStaleData_update() throws Exception {
        appender.clearMessages();

        User u = getResultListUserByLoginNamed(user, true, null, null);
        assertEquals(2, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group
        appender.clearMessages();
        assertEquals(1, queryCache.size());

        try (Transaction tx = persistence.createTransaction()) {
            User updatedUser = persistence.getEntityManager().find(User.class, this.user2.getId());
            updatedUser.setName("new name");
            tx.commit();
        }
        appender.clearMessages();
        assertEquals(0, queryCache.size());
        u = getResultListUserByLoginNamed(user, true, null, null);
        assertEquals(1, appender.filterMessages(m -> m.contains("> SELECT")).count());
    }

    @Test
    public void testStaleData_UpdateDependentEntity() throws Exception {
        appender.clearMessages();

        getResultListUserByRole("TestRole");

        assertEquals(5, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group
        assertEquals(1, queryCache.size());
        appender.clearMessages();

        try (Transaction tx = persistence.createTransaction()) {
            Role updatedRole = persistence.getEntityManager().find(Role.class, role.getId());
            updatedRole.setDescription("New Role Description");
            tx.commit();
        }
        appender.clearMessages();
        assertEquals(0, queryCache.size());
        getResultListUserByRole("TestRole");
        assertEquals(1, queryCache.size());

        assertEquals(3, appender.filterMessages(m -> m.contains("> SELECT")).count());
    }

    @Test
    public void testStaleData_UpdateParentEntity() throws Exception {
        appender.clearMessages();

        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            Query query = em.createQuery("select f from sys$Folder f");
            query.setCacheable(true);
            query.getResultList();
            tx.commit();
        }
        assertEquals(1, queryCache.size());
        appender.clearMessages();

        appFolder = metadata.create(AppFolder.class);
        appFolder.setName("new app folder");
        try (Transaction tx = persistence.createTransaction()) {
            persistence.getEntityManager().persist(appFolder);
            tx.commit();
        }

        appender.clearMessages();
        assertEquals(0, queryCache.size());
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            Query query = em.createQuery("select f from sys$Folder f");
            query.setCacheable(true);
            query.getResultList();
            tx.commit();
        }
        assertEquals(1, queryCache.size());
    }

    @Test
    @Disabled
    public void testWarningInLog() throws Exception {
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            Query query = em.createQuery("select f from sys$Folder f", User.class);
            query.setCacheable(true);
            query.getResultList();
            tx.commit();
        }

        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            Query query = em.createQuery("select f from sys$Folder f", User.class);
            query.setCacheable(true);
            query.getResultList();
            tx.commit();
        }
    }


    @Test
    public void testUpdateQuery() throws Exception {
        appender.clearMessages();
        List<User> resultList;
        getResultListUserByRole("TestRole");

        assertEquals(5, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group
        assertEquals(1, queryCache.size());
        appender.clearMessages();

        try (Transaction tx = persistence.createTransaction()) {
            Query query = persistence.getEntityManager().createQuery("update test$User u set u.position = ?1 where u.loginLowerCase = ?2");
            query.setParameter(1, "new position");
            query.setParameter(2, this.user.getLoginLowerCase());
            query.executeUpdate();
            tx.commit();
        }
        appender.clearMessages();
        assertEquals(0, queryCache.size());
        resultList = getResultListUserByRole("TestRole");
        assertEquals("new position", resultList.get(0).getPosition());
        assertEquals(1, queryCache.size());

        assertEquals(3, appender.filterMessages(m -> m.contains("> SELECT")).count());

        try (Transaction tx = persistence.createTransaction()) {
            Query query = persistence.getEntityManager().createQuery("update test$Role r set r.description = ?1 where r.name = ?2");
            query.setParameter(1, "New Role Description");
            query.setParameter(2, "TestRole");
            query.executeUpdate();
            tx.commit();
        }
        appender.clearMessages();
        assertEquals(0, queryCache.size());
        getResultListUserByRole("TestRole");
        assertEquals(1, queryCache.size());

        assertEquals(4, appender.filterMessages(m -> m.contains("> SELECT")).count());
    }

//    @Test
//    public void testSerializeQueryKey() throws Exception {
//        appender.clearMessages();
//        getSingleResultUserByLoginPositional(user, null);
//        getResultListUserByLoginNamed(user, true, null, null);
//        assertEquals(2, queryCache.size());
//        QueryCacheSupportMBean mBean = AppBeans.get("cuba_QueryCacheSupportMBean");
//        queryCache.asMap().keySet().forEach(queryKey -> {
//            try {
//                reserialize(queryKey);
//                System.out.println(queryKey.printDescription());
//                System.out.println(mBean.printQueryResultsByQueryId(queryKey.getId().toString()));
//            } catch (Exception e) {
//                fail();
//            }
//        });
//        System.out.println(mBean.printCacheContent());
//    }

    @Test
    public void testUuidParameter() {
        appender.clearMessages();
        assertEquals(0, queryCache.size());
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            TypedQuery<User> query = em.createQuery("select u from test$User u where u.id = ?1", User.class);
            query.setCacheable(true);
            query.setParameter(1, user.getId());
            query.setViewName("user.browse");
            query.getResultList();
            tx.commit();
        }
        assertEquals(2, appender.filterMessages(m -> m.contains("> SELECT")).count());
        appender.clearMessages();
        assertEquals(1, queryCache.size());

        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            TypedQuery<User> query = em.createQuery("select u from test$User u where u.id = ?1", User.class);
            query.setCacheable(true);
            query.setParameter(1, user.getId());
            query.setViewName("user.browse");
            query.getResultList();
            tx.commit();
        }
        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count());
        appender.clearMessages();
        assertEquals(1, queryCache.size());
    }


    @Test
    public void testListNamedParameter() {
        appender.clearMessages();
        assertEquals(0, queryCache.size());
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            TypedQuery<User> query = em.createQuery("select u from test$User u where u.id in :users", User.class);
            query.setCacheable(true);
            query.setParameter("users", Arrays.asList(user.getId(), user2.getId()));
            query.setViewName("user.browse");
            query.getResultList();
            tx.commit();
        }
        assertEquals(2, appender.filterMessages(m -> m.contains("> SELECT")).count());
        appender.clearMessages();
        assertEquals(1, queryCache.size());

        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            TypedQuery<User> query = em.createQuery("select u from test$User u where u.id in :users", User.class);
            query.setCacheable(true);
            query.setParameter("users", Arrays.asList(user.getId(), user2.getId()));
            query.setViewName("user.browse");
            query.getResultList();
            tx.commit();
        }
        assertEquals(1, queryCache.size());
        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count());
    }

    @Test
    public void testListPositionalParameter() {
        appender.clearMessages();
        assertEquals(0, queryCache.size());
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            TypedQuery<User> query = em.createQuery("select u from test$User u where u.id in ?1", User.class);
            query.setCacheable(true);
            query.setParameter(1, Arrays.asList(user.getId(), user2.getId()));
            query.setViewName("user.browse");
            query.getResultList();
            tx.commit();
        }
        assertEquals(2, appender.filterMessages(m -> m.contains("> SELECT")).count());
        appender.clearMessages();
        assertEquals(1, queryCache.size());

        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            TypedQuery<User> query = em.createQuery("select u from test$User u where u.id in ?1", User.class);
            query.setCacheable(true);
            query.setParameter(1, Arrays.asList(user.getId(), user2.getId()));
            query.setViewName("user.browse");
            query.getResultList();
            tx.commit();
        }
        assertEquals(1, queryCache.size());
        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count());
    }

    @Test
    public void testLoadingWithNotPartialView() throws Exception {
        appender.clearMessages();

        User user;
        List<User> resultList;

        //load with view: one parameter login
        FetchPlan userLoginView = new View(User.class)
                .addProperty("login");

        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            TypedQuery<User> query = em.createQuery("select u from test$User u where u.login like 'ECTest-%' order by u.name asc", User.class);
            query.setCacheable(true);
            query.setView(userLoginView);
            resultList = query.getResultList();
            assertEquals(resultList.size(), 2);
            tx.commit();
        }

        user = testSupport.reserialize(resultList.get(0));
        assertNotNull(user.getLogin());
        assertNotNull(user.getName());

        user = testSupport.reserialize(resultList.get(1));
        assertNotNull(user.getLogin());
        assertNotNull(user.getName());
        assertEquals(1, appender.filterMessages(m -> m.contains("> SELECT")).count()); // user

        appender.clearMessages();

        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            TypedQuery<User> query = em.createQuery("select u from test$User u where u.login like 'ECTest-%' order by u.name asc", User.class);
            query.setCacheable(true);
            query.setView(userLoginView);
            resultList = query.getResultList();
            assertEquals(resultList.size(), 2);
            tx.commit();
        }

        user = testSupport.reserialize(resultList.get(0));
        assertNotNull(user.getLogin());
        assertNotNull(user.getName());

        user = testSupport.reserialize(resultList.get(1));
        assertNotNull(user.getLogin());
        assertNotNull(user.getName());
        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count()); // user

        appender.clearMessages();

        //load with view: group
        FetchPlan groupView = new View(Group.class, false)
                .addProperty("name");
        FetchPlan userGroupView = new View(User.class)
                .addProperty("group", groupView);

        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            TypedQuery<User> query = em.createQuery("select u from test$User u where u.login like 'ECTest-%' order by u.name asc", User.class);
            query.setCacheable(true);
            query.setView(userGroupView);
            resultList = query.getResultList();
            assertEquals(resultList.size(), 2);
            tx.commit();
        }

        user = testSupport.reserialize(resultList.get(0));
        assertNotNull(user.getLogin());
        assertNotNull(user.getName());
        assertEquals(user.getGroup(), group);
        assertEquals(user.getGroup().getName(), group.getName());

        user = testSupport.reserialize(resultList.get(1));
        assertNotNull(user.getLogin());
        assertNotNull(user.getName());
        assertEquals(user.getGroup(), group);
        assertEquals(user.getGroup().getName(), group.getName());
        assertEquals(1, appender.filterMessages(m -> m.contains("> SELECT") && m.contains("TEST_GROUP")).count());

        appender.clearMessages();

        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            TypedQuery<User> query = em.createQuery("select u from test$User u where u.login like 'ECTest-%' order by u.name asc", User.class);
            query.setCacheable(true);
            query.setView(userGroupView);
            resultList = query.getResultList();
            assertEquals(resultList.size(), 2);
            tx.commit();
        }

        user = testSupport.reserialize(resultList.get(0));
        assertNotNull(user.getLogin());
        assertNotNull(user.getName());
        assertEquals(user.getGroup(), group);
        assertEquals(user.getGroup().getName(), group.getName());

        user = testSupport.reserialize(resultList.get(1));
        assertNotNull(user.getLogin());
        assertNotNull(user.getName());
        assertEquals(user.getGroup(), group);
        assertEquals(user.getGroup().getName(), group.getName());
        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count());

        appender.clearMessages();

        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            TypedQuery<User> query = em.createQuery("select u from test$User u where u.login like 'ECTest-%' order by u.name asc", User.class);
            query.setCacheable(true);
            query.setView(userLoginView);
            resultList = query.getResultList();
            assertEquals(resultList.size(), 2);
            tx.commit();
        }

        user = testSupport.reserialize(resultList.get(0));
        assertNotNull(user.getLogin());
        assertNotNull(user.getName());
        assertFail(user::getGroup);

        user = testSupport.reserialize(resultList.get(1));
        assertNotNull(user.getLogin());
        assertNotNull(user.getName());
        assertFail(user::getGroup);
        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count());

        appender.clearMessages();

        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            TypedQuery<User> query = em.createQuery("select u from test$User u where u.login like 'ECTest-%' order by u.name asc", User.class);
            query.setCacheable(true);
            query.setView(userGroupView);
            resultList = query.getResultList();
            assertEquals(resultList.size(), 2);
            tx.commit();
        }

        user = testSupport.reserialize(resultList.get(0));
        assertNotNull(user.getLogin());
        assertNotNull(user.getName());
        assertEquals(user.getGroup(), group);
        assertEquals(user.getGroup().getName(), group.getName());

        user = testSupport.reserialize(resultList.get(1));
        assertNotNull(user.getLogin());
        assertNotNull(user.getName());
        assertEquals(user.getGroup(), group);
        assertEquals(user.getGroup().getName(), group.getName());
        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count());
    }

    @Test
    public void testLoadingWithPartialView() throws Exception {
        appender.clearMessages();

        User user;
        List<User> resultList;

        //load with view: one parameter login
        FetchPlan userLoginView = new View(User.class)
                .addProperty("login").setLoadPartialEntities(true);

        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            TypedQuery<User> query = em.createQuery("select u from test$User u where u.login like 'ECTest-%' order by u.name asc", User.class);
            query.setCacheable(true);
            query.setView(userLoginView);
            resultList = query.getResultList();
            assertEquals(resultList.size(), 2);
            tx.commit();
        }

        user = testSupport.reserialize(resultList.get(0));
        assertNotNull(user.getLogin());
        assertNotNull(user.getName());

        user = testSupport.reserialize(resultList.get(1));
        assertNotNull(user.getLogin());
        assertNotNull(user.getName());
        assertEquals(1, appender.filterMessages(m -> m.contains("> SELECT")).count()); // user

        appender.clearMessages();

        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            TypedQuery<User> query = em.createQuery("select u from test$User u where u.login like 'ECTest-%' order by u.name asc", User.class);
            query.setCacheable(true);
            query.setView(userLoginView);
            resultList = query.getResultList();
            assertEquals(resultList.size(), 2);
            tx.commit();
        }

        user = testSupport.reserialize(resultList.get(0));
        assertNotNull(user.getLogin());
        assertNotNull(user.getName());

        user = testSupport.reserialize(resultList.get(1));
        assertNotNull(user.getLogin());
        assertNotNull(user.getName());
        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count()); // user

        appender.clearMessages();

        //load with view: group
        FetchPlan groupView = new View(Group.class, false)
                .addProperty("name").setLoadPartialEntities(true);
        FetchPlan userGroupView = new View(User.class)
                .addProperty("group", groupView).setLoadPartialEntities(true);

        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            TypedQuery<User> query = em.createQuery("select u from test$User u where u.login like 'ECTest-%' order by u.name asc", User.class);
            query.setCacheable(true);
            query.setView(userGroupView);
            resultList = query.getResultList();
            assertEquals(resultList.size(), 2);
            tx.commit();
        }

        user = testSupport.reserialize(resultList.get(0));
        assertNotNull(user.getLogin());
        assertNotNull(user.getName());
        assertEquals(user.getGroup(), group);
        assertEquals(user.getGroup().getName(), group.getName());

        user = testSupport.reserialize(resultList.get(1));
        assertNotNull(user.getLogin());
        assertNotNull(user.getName());
        assertEquals(user.getGroup(), group);
        assertEquals(user.getGroup().getName(), group.getName());
        assertEquals(1, appender.filterMessages(m -> m.contains("> SELECT") && m.contains("TEST_GROUP")).count());

        appender.clearMessages();

        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            TypedQuery<User> query = em.createQuery("select u from test$User u where u.login like 'ECTest-%' order by u.name asc", User.class);
            query.setCacheable(true);
            query.setView(userGroupView);
            resultList = query.getResultList();
            assertEquals(resultList.size(), 2);
            tx.commit();
        }

        user = testSupport.reserialize(resultList.get(0));
        assertNotNull(user.getLogin());
        assertNotNull(user.getName());
        assertEquals(user.getGroup(), group);
        assertEquals(user.getGroup().getName(), group.getName());

        user = testSupport.reserialize(resultList.get(1));
        assertNotNull(user.getLogin());
        assertNotNull(user.getName());
        assertEquals(user.getGroup(), group);
        assertEquals(user.getGroup().getName(), group.getName());
        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count());

        appender.clearMessages();

        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            TypedQuery<User> query = em.createQuery("select u from test$User u where u.login like 'ECTest-%' order by u.name asc", User.class);
            query.setCacheable(true);
            query.setView(userLoginView);
            resultList = query.getResultList();
            assertEquals(resultList.size(), 2);
            tx.commit();
        }

        user = testSupport.reserialize(resultList.get(0));
        assertNotNull(user.getLogin());
        assertNotNull(user.getName());
        assertFail(user::getGroup);

        user = testSupport.reserialize(resultList.get(1));
        assertNotNull(user.getLogin());
        assertNotNull(user.getName());
        assertFail(user::getGroup);
        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count());

        appender.clearMessages();

        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            TypedQuery<User> query = em.createQuery("select u from test$User u where u.login like 'ECTest-%' order by u.name asc", User.class);
            query.setCacheable(true);
            query.setView(userGroupView);
            resultList = query.getResultList();
            assertEquals(resultList.size(), 2);
            tx.commit();
        }

        user = testSupport.reserialize(resultList.get(0));
        assertNotNull(user.getLogin());
        assertNotNull(user.getName());
        assertEquals(user.getGroup(), group);
        assertEquals(user.getGroup().getName(), group.getName());

        user = testSupport.reserialize(resultList.get(1));
        assertNotNull(user.getLogin());
        assertNotNull(user.getName());
        assertEquals(user.getGroup(), group);
        assertEquals(user.getGroup().getName(), group.getName());
        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count());
    }

    @Test
    public void testAdditionalCriteriaParametersInQueryKey() throws Exception {
        try {
            testAdditionalCriteriaProvider.setParam("ONE");

            assertEquals(0, queryCache.size());

            getSingleResultUserByLoginNamed(user, null);
            assertEquals(1, queryCache.size());

            getSingleResultUserByLoginNamed(user, null);
            assertEquals(1, queryCache.size());//the same key for the same param

            testAdditionalCriteriaProvider.setParam("TWO");

            getSingleResultUserByLoginNamed(user, null);
            assertEquals(2, queryCache.size());//another key for another param

            getSingleResultUserByLoginNamed(user, null);
            assertEquals(2, queryCache.size());//two keys for two different params

        } finally {
            testAdditionalCriteriaProvider.setParam(null);
        }
    }

    @Test
    public void testRepeatedParameterReplacedWithoutProblems() {
        assertEquals(0, queryCache.size());

        DataManager dataManager = AppBeans.get(DataManager.NAME);
        LoadContext<User> loadContext = new LoadContext<>(User.class).setFetchPlan("user.browse");
        loadContext.setQueryString("select u from test$User u where u.login like :str or u.name like :str")
                .setParameter("str", "%2%")
                .setCacheable(true);
        dataManager.loadList(loadContext);//no exception must occur

        assertEquals(1, queryCache.size());
    }


    protected User getResultListUserByLoginNamed(User loadedUser, boolean checkView, Consumer<EntityManager> emBuilder, Consumer<Query> queryBuilder) throws Exception {
        User user;
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            if (emBuilder != null) {
                emBuilder.accept(em);
            }
            TypedQuery<User> query = em.createQuery("select u from test$User u where u.login = :login", User.class);
            query.setCacheable(true);
            query.setParameter("login", "ECTest-" + loadedUser.getId());
            query.setViewName("user.browse");
            if (queryBuilder != null) {
                queryBuilder.accept(query);
            }
            List<User> resultList = query.getResultList();
            user = resultList.isEmpty() ? null : resultList.get(0);
            tx.commit();
        }

        if (user != null && checkView) {
            assertUserBrowseView(user);
        }
        return user;
    }

    protected List<User> getResultListUserByRole(String roleName) throws Exception {
        List<User> resultList;
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            TypedQuery<User> query = em.createQuery("select u from test$User u join u.userRoles ur where ur.role.name = :name", User.class);
            query.setCacheable(true);
            query.setParameter("name", roleName);
            query.setViewName("user.edit");
            resultList = query.getResultList();
            tx.commit();
        }
        return resultList;
    }


    protected User getSingleResultUserByLoginNamed(User loadedUser, Consumer<Query> queryBuilder) throws Exception {
        User user;
        try (Transaction tx = persistence.createTransaction()) {
            TypedQuery<User> query = persistence.getEntityManager().createQuery("select u from test$User u where u.login = ?1", User.class);
            query.setCacheable(true);
            query.setParameter(1, "ECTest-" + loadedUser.getId());
            query.setViewName("user.browse");
            if (queryBuilder != null) {
                queryBuilder.accept(query);
            }
            user = query.getSingleResult();

            tx.commit();
        }

        if (user != null) {
            assertUserBrowseView(user);
        }
        return user;
    }

    protected User getSingleResultUserByLoginPositional(User loadedUser, Consumer<Query> queryBuilder) throws Exception {
        User user;
        try (Transaction tx = persistence.createTransaction()) {
            TypedQuery<User> query = persistence.getEntityManager().createQuery("select u from test$User u where u.login = ?1", User.class);
            query.setCacheable(true);
            query.setParameter(1, "ECTest-" + loadedUser.getId());
            query.setViewName("user.browse");
            if (queryBuilder != null) {
                queryBuilder.accept(query);
            }
            user = query.getSingleResult();

            tx.commit();
        }

        if (user != null) {
            assertUserBrowseView(user);
        }
        return user;
    }

    protected void assertUserBrowseView(User user) throws Exception {
        user = testSupport.reserialize(user);
        Group g = user.getGroup();
        assertEquals(this.group, g);
        assertNotNull(g.getName());
        assertFail(g::getParent);
    }


    protected User dataManager_getResultListUserByLoginNamed() {
        DataManager dataManager = AppBeans.get(DataManager.NAME);
        LoadContext<User> loadContext = new LoadContext<>(User.class).setFetchPlan("user.browse");
        loadContext.setQueryString("select u from test$User u where u.login = :login")
                .setParameter("login", "ECTest-" + this.user.getId())
                .setCacheable(true);
        return dataManager.load(loadContext);
    }

    protected User dataManager_getResultListUserByLoginCondition() {
        DataManager dataManager = AppBeans.get(DataManager.NAME);
        LoadContext<User> loadContext = new LoadContext<>(User.class).setFetchPlan("user.browse");
        loadContext.setQueryString("select u from test$User u")
                .setCondition(PropertyCondition.equal("login", "ECTest-" + this.user.getId()))
                .setCacheable(true);
        return dataManager.load(loadContext);
    }

    protected User dataManager_getResultListUserByLoginAndNameCondition() {
        DataManager dataManager = AppBeans.get(DataManager.NAME);
        LoadContext<User> loadContext = new LoadContext<>(User.class).setFetchPlan("user.browse");
        loadContext.setQueryString("select u from test$User u")
                .setCondition(LogicalCondition.and(
                        PropertyCondition.equal("login", "ECTest-" + this.user.getId()),
                        PropertyCondition.equal("name", this.user.getName())))
                .setCacheable(true);
        return dataManager.load(loadContext);
    }
}
