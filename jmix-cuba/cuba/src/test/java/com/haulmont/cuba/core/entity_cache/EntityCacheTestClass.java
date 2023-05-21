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
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.core.model.common.*;
import com.haulmont.cuba.core.model.entity_cache.ChildCachedEntity;
import com.haulmont.cuba.core.model.entity_cache.ParentCachedEntity;
import com.haulmont.cuba.core.model.entitycache_unfetched.CompositeOne;
import com.haulmont.cuba.core.model.entitycache_unfetched.CompositePropertyOne;
import com.haulmont.cuba.core.model.entitycache_unfetched.CompositePropertyTwo;
import com.haulmont.cuba.core.model.entitycache_unfetched.CompositeTwo;
import com.haulmont.cuba.core.sys.QueryImpl;
import com.haulmont.cuba.core.testsupport.*;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlanRepository;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.exceptions.QueryException;
import org.eclipse.persistence.internal.jpa.EntityManagerFactoryDelegate;
import org.eclipse.persistence.jpa.JpaCache;
import org.eclipse.persistence.sessions.server.ServerSession;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.rules.TestRule;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.function.Predicate;

import static com.haulmont.cuba.core.testsupport.TestAssertions.assertFail;
import static org.junit.Assert.*;

/**
 * Tests of EclipseLink shared cache.
 */
@CoreTest
@TestPropertySource("classpath:/com/haulmont/cuba/core/test-entitycache-app.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class EntityCacheTestClass {
    @RegisterExtension
    public TestRule testNamePrinter = new TestNamePrinter();

    @Autowired
    private Persistence persistence;
    @Autowired
    private Metadata metadata;
    @Autowired
    private MetadataTools metadataTools;
    @Autowired
    private TestSupport testSupport;
    @Autowired
    private TestAdditionalCriteriaProvider provider;
    @Autowired
    private Environment env;

    private JpaCache cache;

    private final TestAppender appender;
    private Group group;
    private User user;
    private User user2;
    private Role role, role1;
    private UserRole userRole;
    private User user1;
    private UserSetting userSetting;
    private UserSubstitution userSubstitution;
    private CompositeOne compositeOne;
    private CompositeTwo compositeTwo;
    private CompositePropertyOne compositePropertyOne;
    private CompositePropertyTwo compositePropertyTwo;
    private ParentCachedEntity cachedParent;
    private ChildCachedEntity cachedChild;

    private Predicate<String> selectsOnly = s -> s.contains("> SELECT");

    public EntityCacheTestClass() {
        appender = new TestAppender();
        appender.start();
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = context.getLogger("eclipselink.logging.sql");
        logger.addAppender(appender);
    }

    @BeforeEach
    public void setUp() throws Exception {

        try (Transaction tx = persistence.createTransaction()) {
            EntityManagerFactory emf = persistence.getEntityManager().getDelegate().getEntityManagerFactory();

            assertTrue(metadataTools.isCacheable(metadata.getClass(User.class)));
            assertFalse(metadataTools.isCacheable(metadata.getClass(UserSubstitution.class)));

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
            user.setGroup(group);
            persistence.getEntityManager().persist(user);

            user2 = metadata.create(User.class);
            user2.setLogin("ECTest-" + user2.getId());
            user2.setPassword("111");
            user2.setGroup(group);
            persistence.getEntityManager().persist(user2);

            role = metadata.create(Role.class);
            role.setName("Test role");
            role.setDescription("Test role descr");
            persistence.getEntityManager().persist(role);

            userRole = metadata.create(UserRole.class);
            userRole.setRole(role);
            userRole.setUser(user);
            persistence.getEntityManager().persist(userRole);

            userSetting = metadata.create(UserSetting.class);
            userSetting.setUser(user);
            persistence.getEntityManager().persist(userSetting);

            userSubstitution = metadata.create(UserSubstitution.class);
            userSubstitution.setUser(user);
            userSubstitution.setSubstitutedUser(user2);
            persistence.getEntityManager().persist(userSubstitution);

            compositeOne = metadata.create(CompositeOne.class);
            compositeOne.setName("compositeOne");
            persistence.getEntityManager().persist(compositeOne);

            compositeTwo = metadata.create(CompositeTwo.class);
            compositeTwo.setName("compositeTwo");
            persistence.getEntityManager().persist(compositeTwo);

            compositePropertyOne = metadata.create(CompositePropertyOne.class);
            compositePropertyOne.setName("compositePropertyOne");
            compositePropertyOne.setCompositeOne(compositeOne);
            compositePropertyOne.setCompositeTwo(compositeTwo);
            persistence.getEntityManager().persist(compositePropertyOne);

            compositePropertyTwo = metadata.create(CompositePropertyTwo.class);
            compositePropertyTwo.setName("compositePropertyTwo");
            compositePropertyTwo.setCompositeTwo(compositeTwo);
            persistence.getEntityManager().persist(compositePropertyTwo);

            cachedParent = metadata.create(ParentCachedEntity.class);
            cachedParent.setTitle("parent one");
            cachedParent.setTestAdditional("ONE");
            persistence.getEntityManager().persist(cachedParent);

            cachedChild = metadata.create(ChildCachedEntity.class);
            cachedChild.setSimpleProperty("simple value");
            cachedChild.setTestAdditional("ONE");
            cachedChild.setParent(cachedParent);
            persistence.getEntityManager().persist(cachedChild);

            tx.commit();
        }
        cache.clear();
    }

    @AfterEach
    public void tearDown() throws Exception {
        testSupport.deleteRecord(userSetting, userRole, role, userSubstitution, user, user2);
        testSupport.deleteRecord(compositePropertyTwo, compositePropertyOne, compositeTwo, compositeOne);
        if (role1 != null)
            testSupport.deleteRecord(role1);
        if (user1 != null)
            testSupport.deleteRecord(user1);
        testSupport.deleteRecord(group);
        testSupport.deleteRecord(cachedChild, cachedParent);
    }

    @Test
    public void testFind() throws Exception {
        appender.clearMessages();

        loadUserAlone();

        assertEquals(1, appender.filterMessages(m -> m.contains("> SELECT")).count());
        appender.clearMessages();

        loadUserAlone();

        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count());
    }

    @Test
    public void testQueryById() throws Exception {
        appender.clearMessages();

        User u;

        try (Transaction tx = persistence.createTransaction()) {
            TypedQuery<User> query = persistence.getEntityManager().createQuery("select u from test$User u where u.id = ?1", User.class);
            query.setParameter(1, this.user.getId());
            query.setViewName("user.browse");
            u = query.getSingleResult();

            tx.commit();
        }
        assertEquals(this.user.getLogin(), u.getLogin());
        assertEquals(this.group, u.getGroup());

        assertEquals(2, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group
        appender.clearMessages();

        try (Transaction tx = persistence.createTransaction()) {
            TypedQuery<User> query = persistence.getEntityManager().createQuery("select u from test$User u where u.id = ?1", User.class);
            query.setParameter(1, this.user.getId());
            query.setViewName("user.browse");
            u = query.getSingleResult();

            tx.commit();
        }
        assertEquals(this.user.getLogin(), u.getLogin());
        assertEquals(this.group, u.getGroup());

        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count());
    }

    @Test
    public void testFindWithView1() throws Exception {
        appender.clearMessages();
        User user;

        try (Transaction tx = persistence.createTransaction()) {
            user = persistence.getEntityManager().find(User.class, this.user.getId(), "user.browse");
            assertNotNull(user);

            tx.commit();
        }
        user = testSupport.reserialize(user);
        assertNotNull(user.getGroup());

        assertEquals(2, appender.filterMessages(m -> m.contains("> SELECT")).count()); // user, group
        appender.clearMessages();

        try (Transaction tx = persistence.createTransaction()) {
            user = persistence.getEntityManager().find(User.class, this.user.getId(), "user.browse");
            assertNotNull(user);

            tx.commit();
        }
        user = testSupport.reserialize(user);
        Group g = user.getGroup();
        assertEquals(this.group, g);
        assertNotNull(g.getName());
        assertFail(g::getParent);

        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count());
    }

    @Test
    public void testFindWithView2() throws Exception {
        appender.clearMessages();

        User u;
        try (Transaction tx = persistence.createTransaction()) {
            u = persistence.getEntityManager().find(User.class, user.getId(), "user.edit");
            tx.commit();
        }
        checkUser(u);

        assertEquals(6, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group, UserRoles, Role, UserSubstitution, substituted User
        appender.clearMessages();

        try (Transaction tx = persistence.createTransaction()) {
            u = persistence.getEntityManager().find(User.class, user.getId(), "user.edit");
            tx.commit();
        }
        checkUser(u);

        assertEquals(1, appender.filterMessages(m -> m.contains("> SELECT")).count()); // 1 because UserSubstitution is not cached
    }

    private void checkUser(User u) throws Exception {
        u = testSupport.reserialize(u);
        assertNotNull(u);

        assertEquals(group, u.getGroup());
        assertNotNull(u.getGroup().getName());
        assertFail(u.getGroup()::getParent);

        assertFalse(u.getUserRoles().isEmpty());
        UserRole ur = u.getUserRoles().iterator().next();
        assertEquals(userRole, ur);

        Role r = ur.getRole();
        assertEquals(this.role, r);
        assertNotNull(r.getName());
        assertNotNull(r.getDescription());

        assertEquals(1, u.getSubstitutions().size());
    }

    @Test
    public void testFindWithView3() throws Exception {
        appender.clearMessages();

        User user;

        // no name in group
        FetchPlan groupView = new View(Group.class, false);
        FetchPlan userView = new View(User.class)
                .addProperty("login")
                .addProperty("group", groupView)
                .setLoadPartialEntities(true);

        try (Transaction tx = persistence.createTransaction()) {
            user = persistence.getEntityManager().find(User.class, this.user.getId(), userView);
            assertNotNull(user);

            tx.commit();
        }
        user = testSupport.reserialize(user);
        Group g = user.getGroup();
        assertNotNull(user.getGroup());
        assertEquals(this.group, g);
        assertNotNull(g.getName()); // due to caching, we load all attributes anyway

        assertEquals(2, appender.filterMessages(m -> m.contains("> SELECT")).count()); // user, group
        appender.clearMessages();

        // second time - from cache
        try (Transaction tx = persistence.createTransaction()) {
            user = persistence.getEntityManager().find(User.class, this.user.getId(), userView);
            assertNotNull(user);

            tx.commit();
        }
        user = testSupport.reserialize(user);
        g = user.getGroup();
        assertEquals(this.group, g);
        assertNotNull(g.getName());

        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count()); // user, group
        appender.clearMessages();

        // name in group - from cache again
        FetchPlan groupView1 = new View(Group.class, true)
                .addProperty("name");
        FetchPlan userView1 = new View(User.class)
                .addProperty("login")
                .addProperty("group", groupView1)
                .setLoadPartialEntities(true);

        try (Transaction tx = persistence.createTransaction()) {
            user = persistence.getEntityManager().find(User.class, this.user.getId(), userView1);
            assertNotNull(user);

            tx.commit();
        }
        user = testSupport.reserialize(user);
        g = user.getGroup();
        assertEquals(this.group, g);
        assertNotNull(g.getName());

        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count()); // user, group
        appender.clearMessages();
    }

    @Test
    public void testStaleData_insert() throws Exception {
        appender.clearMessages();

        loadUser();
        assertEquals(2, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group
        assertEquals(1, appender.filterMessages(m -> m.contains("FROM TEST_USER")).count());
        assertEquals(1, appender.filterMessages(m -> m.contains("FROM TEST_GROUP")).count());
        appender.clearMessages();

        loadUser();
        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count());
        appender.clearMessages();

        User newUser = metadata.create(User.class);
        newUser.setLogin("new user");
        newUser.setGroup(this.group);
        try (Transaction tx = persistence.createTransaction()) {
            persistence.getEntityManager().persist(newUser);
            tx.commit();
        }

        loadUser();
        assertEquals(0, appender.filterMessages(m -> m.contains("FROM TEST_USER")).count()); // inserting new entities does not affect existing in cache

        testSupport.deleteRecord(newUser);
    }

    @Test
    public void testStaleData_update() throws Exception {
        appender.clearMessages();

        loadUser();
        assertEquals(2, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group
        appender.clearMessages();

        loadUser();
        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count());
        appender.clearMessages();


        try (Transaction tx = persistence.createTransaction()) {
            User u = persistence.getEntityManager().find(User.class, this.user.getId());
            u.setName("new name");
            tx.commit();
        }

        User u = loadUser();
        assertEquals("new name", u.getName());
        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count()); // no DB requests - the User has been updated in cache
        appender.clearMessages();

        loadUser();
        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count());
    }

    @Test
    public void testStaleData_update_DM() throws Exception {
        appender.clearMessages();

        loadUser();
        assertEquals(2, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group
        appender.clearMessages();

        loadUser();
        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count());
        appender.clearMessages();

        DataManager dataManager = AppBeans.get(DataManager.class);
        User u = dataManager.load(LoadContext.create(User.class).setId(this.user.getId()).setFetchPlan("user.browse"));
        u.setName("new name");
        dataManager.commit(u);
        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT") && !m.contains("DYNAT_CATEGORY")).count()); // no DB requests - the User has been updated in cache
        appender.clearMessages();

        u = loadUser();
        assertEquals("new name", u.getName());
        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count()); // no DB requests - the User has been updated in cache
        appender.clearMessages();

        loadUser();
        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count());
    }

    @Test
    public void testStaleData_updateCollection_add() throws Exception {
        appender.clearMessages();

        loadUserWithRoles();
        assertEquals(4, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group, UserRoles, Role
        appender.clearMessages();

        loadUserWithRoles();
        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count());
        appender.clearMessages();

        Role newRole;
        UserRole newUserRole;

        try (Transaction tx = persistence.createTransaction()) {
            User u = persistence.getEntityManager().find(User.class, this.user.getId());

            newRole = metadata.create(Role.class);
            newRole.setName("new role");
            persistence.getEntityManager().persist(newRole);

            newUserRole = metadata.create(UserRole.class);
            newUserRole.setRole(newRole);
            newUserRole.setUser(u);
            persistence.getEntityManager().persist(newUserRole);

            tx.commit(); // User should be evicted from cache to update collection of UserRoles - see JpaCacheSupport.evictMasterEntity()
        }

        User u = loadUserWithRoles();
        assertEquals(2, u.getUserRoles().size());
        assertTrue(u.getUserRoles().stream()
                .map(UserRole::getRole)
                .anyMatch(r -> r.getName().equals("new role")));

        assertEquals(2, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, UserRoles

        testSupport.deleteRecord(newUserRole, newRole);
    }

    @Test
    public void testStaleData_updateCollection_remove() throws Exception {
        appender.clearMessages();

        loadUserWithRoles();
        assertEquals(4, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group, UserRoles, Role
        appender.clearMessages();

        loadUserWithRoles();
        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count());
        appender.clearMessages();

        try (Transaction tx = persistence.createTransaction()) {
            UserRole ur = persistence.getEntityManager().find(UserRole.class, this.userRole.getId());
            persistence.getEntityManager().remove(ur);

            tx.commit(); // User should be evicted from cache to update collection of UserRoles - see JpaCacheSupport.evictMasterEntity()
        }

        User u = loadUserWithRoles();
        assertEquals(0, u.getUserRoles().size());

        assertEquals(2, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, UserRoles
    }

    @Test
    public void testStaleData_updateCollection_changeMaster() throws Exception {
        appender.clearMessages();

        loadUserWithRoles();
        assertEquals(4, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group, UserRoles, Role
        appender.clearMessages();

        loadUserWithRoles();
        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count());
        appender.clearMessages();

        try (Transaction tx = persistence.createTransaction()) {
            UserRole ur = persistence.getEntityManager().find(UserRole.class, this.userRole.getId());
            user1 = metadata.create(User.class);
            user1.setLogin("user1-" + user1.getId());
            user1.setGroup(group);
            persistence.getEntityManager().persist(user1);
            ur.setUser(user1);

            tx.commit(); // User should be evicted from cache to update collection of UserRoles - see JpaCacheSupport.evictMasterEntity()
        }

        appender.clearMessages();

        User u = loadUserWithRoles();
        assertEquals(0, u.getUserRoles().size());

        assertEquals(2, appender.filterMessages(m -> m.contains("> SELECT")).count()); // Default Roles, User, UserRoles
    }

    @Test
    public void testStaleData_updateCollectionElement() throws Exception {
        appender.clearMessages();

        loadUserWithRoles();
        assertEquals(4, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group, UserRoles, Role
        appender.clearMessages();

        loadUserWithRoles();
        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count());
        appender.clearMessages();

        try (Transaction tx = persistence.createTransaction()) {
            User u = persistence.getEntityManager().find(User.class, this.user.getId());

            role1 = metadata.create(Role.class);
            role1.setName("new role");
            persistence.getEntityManager().persist(role1);

            UserRole userRole = u.getUserRoles().get(0);
            userRole.setRole(role1);

            tx.commit();
        }

        User u = loadUserWithRoles();
        assertTrue(u.getUserRoles().get(0).getRole().getName().equals("new role"));

        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count());
    }

    @Test
    public void testSoftDeleteToOne() throws Exception {
        appender.clearMessages();

        loadUserSetting();
        assertEquals(2, appender.filterMessages(m -> m.contains("> SELECT")).count()); // UserSetting, User
        appender.clearMessages();

        loadUserSetting();
        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count());
        appender.clearMessages();

        User u;
        try (Transaction tx = persistence.createTransaction()) {
            u = persistence.getEntityManager().find(User.class, this.user.getId());
            persistence.getEntityManager().remove(u);
            tx.commit();
        }
        appender.clearMessages();

        UserSetting us = loadUserSetting();
        assertTrue(us.getUser().isDeleted());
        appender.clearMessages();

        us = loadUserSetting();
        assertTrue(us.getUser().isDeleted());
        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count());
    }

    @Test
    public void testSoftDelete() throws Exception {
        User u;
        try (Transaction tx = persistence.createTransaction()) {
            u = persistence.getEntityManager().find(User.class, this.user.getId());
            persistence.getEntityManager().remove(u);
            tx.commit();
        }
        appender.clearMessages();

        appender.clearMessages();
        // loading first time - select is issued because the entity was evicted
        try (Transaction tx = persistence.createTransaction()) {
            u = persistence.getEntityManager().find(User.class, this.user.getId());
            tx.commit();
        }
        assertNull(u);
        assertEquals(1, appender.filterMessages(m -> m.contains("> SELECT")).count());

        appender.clearMessages();
        // loading second time - select again because the previous query returned nothing
        try (Transaction tx = persistence.createTransaction()) {
            u = persistence.getEntityManager().find(User.class, this.user.getId());
            tx.commit();
        }
        assertNull(u);
        assertEquals(1, appender.filterMessages(m -> m.contains("> SELECT")).count());
    }

    @Test
    public void testSoftDeleteOff() throws Exception {
        User u;
        try (Transaction tx = persistence.createTransaction()) {
            u = persistence.getEntityManager().find(User.class, this.user.getId());
            persistence.getEntityManager().remove(u);
            tx.commit();
        }
        appender.clearMessages();

        appender.clearMessages();
        try (Transaction tx = persistence.createTransaction()) {
            persistence.getEntityManager().setSoftDeletion(false);
            u = persistence.getEntityManager().find(User.class, this.user.getId());
            tx.commit();
        }
        assertNotNull(u);
        assertEquals(1, appender.filterMessages(m -> m.contains("> SELECT")).count());

        appender.clearMessages();
        try (Transaction tx = persistence.createTransaction()) {
            u = persistence.getEntityManager().find(User.class, this.user.getId());
            tx.commit();
        }
        assertNull(u);
    }

    @Test
    public void testQuery() throws Exception {
        appender.clearMessages();

        User u1, u2;
        List<User> list;

        try (Transaction tx = persistence.createTransaction()) {
            TypedQuery<User> query = persistence.getEntityManager().createQuery("select u from test$User u where u.login like ?1", User.class);
            query.setParameter(1, "ECTest%");
            query.setViewName("user.browse");
            list = query.getResultList();

            tx.commit();
        }
        u1 = list.stream().filter(u -> u.getId().equals(this.user.getId())).findFirst().get();
        assertEquals(this.user.getLogin(), u1.getLogin());
        assertEquals(this.group, u1.getGroup());
        u2 = list.stream().filter(user -> user.getId().equals(this.user2.getId())).findFirst().get();
        assertEquals(this.user2.getLogin(), u2.getLogin());
        assertEquals(this.group, u2.getGroup());

        assertEquals(2, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group
        appender.clearMessages();

        try (Transaction tx = persistence.createTransaction()) {
            TypedQuery<User> query = persistence.getEntityManager().createQuery("select u from test$User u where u.login like ?1", User.class);
            query.setParameter(1, "ECTest%");
            query.setViewName("user.browse");
            list = query.getResultList();

            tx.commit();
        }
        u1 = list.stream().filter(u -> u.getId().equals(this.user.getId())).findFirst().get();
        assertEquals(this.user.getLogin(), u1.getLogin());
        assertEquals(this.group, u1.getGroup());
        u2 = list.stream().filter(user -> user.getId().equals(this.user2.getId())).findFirst().get();
        assertEquals(this.user2.getLogin(), u2.getLogin());
        assertEquals(this.group, u2.getGroup());

        assertEquals(1, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User only
    }

    private User loadUser() throws Exception {
        User user;
        try (Transaction tx = persistence.createTransaction()) {
            user = persistence.getEntityManager().find(User.class, this.user.getId(), "user.browse");
            assertNotNull(user);

            tx.commit();
        }
        user = testSupport.reserialize(user);
        Group g = user.getGroup();
        assertEquals(this.group, g);
        assertNotNull(g.getName());
        assertFail(g::getParent);
        return user;
    }

    private User loadUserWithRoles() throws Exception {
        FetchPlan roleView = new View(Role.class)
                .addProperty("name");
        FetchPlan userRoleView = new View(UserRole.class)
                .addProperty("role", roleView);
        FetchPlan groupView = new View(Group.class)
                .addProperty("name");
        FetchPlan userView = new View(User.class)
                .addProperty("login")
                .addProperty("name")
                .addProperty("userRoles", userRoleView)
                .addProperty("group", groupView);

        User user;
        try (Transaction tx = persistence.createTransaction()) {
            user = persistence.getEntityManager().find(User.class, this.user.getId(), userView);
            assertNotNull(user);

            tx.commit();
        }
        user = testSupport.reserialize(user);
        Group g = user.getGroup();
        assertEquals(this.group, g);
        assertNotNull(g.getName());
        assertFail(g::getParent);
        user.getUserRoles().size();
        return user;
    }

    private UserSetting loadUserSetting() throws Exception {
        UserSetting us;
        FetchPlan usView = new View(UserSetting.class)
                .addProperty("name")
                .addProperty("user", new View(User.class)
                        .addProperty("login"));
        try (Transaction tx = persistence.createTransaction()) {
            us = persistence.getEntityManager().find(UserSetting.class, this.userSetting.getId(), usView);
            assertNotNull(us);
            tx.commit();
        }
        us = testSupport.reserialize(us);
        assertEquals(userSetting, us);
        assertEquals(user, us.getUser());
        return us;
    }


    @Test
    public void testUpdateQuery() throws Exception {
        appender.clearMessages();

        loadUser();

        assertEquals(2, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group
        appender.clearMessages();

        try (Transaction tx = persistence.createTransaction()) {
            Query query = persistence.getEntityManager().createQuery("update test$User u set u.position = ?1 where u.loginLowerCase = ?2");
            query.setParameter(1, "new position");
            query.setParameter(2, this.user.getLoginLowerCase());
            query.executeUpdate();
            tx.commit();
        }
        appender.clearMessages();

        User u = loadUser();
        assertEquals("new position", u.getPosition());

        assertEquals(1, appender.filterMessages(m -> m.contains("> SELECT")).count()); // Group
    }

    @Test
    public void testNativeQuery() throws Exception {
        appender.clearMessages();

        loadUser();

        assertEquals(2, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group
        appender.clearMessages();

        try (Transaction tx = persistence.createTransaction()) {
            Query query = persistence.getEntityManager().createNativeQuery("update test_user set position_ = ? where login_lc = ?");
            query.setParameter(1, "new position");
            query.setParameter(2, this.user.getLoginLowerCase());
            query.executeUpdate(); // all evicted here
            tx.commit();
        }
        appender.clearMessages();

        User u;
        try (Transaction tx = persistence.createTransaction()) {
            u = persistence.getEntityManager().find(User.class, this.user.getId(), "user.browse");
            assertNotNull(u);
            tx.commit();
        }
        u = testSupport.reserialize(u);
        Group g = u.getGroup();
        assertEquals(this.group, g);
        assertNotNull(g.getName());
        assertEquals("new position", u.getPosition());

        assertEquals(2, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User,Group
    }

    @Test
    @Disabled
    public void testAccessConnectionWithCacheInvalidation() {
        appender.clearMessages();

        try (Transaction tx = persistence.createTransaction()) {
            persistence.getEntityManager().getConnection();
            FetchPlanRepository viewRepository = AppBeans.get(FetchPlanRepository.class);
            FetchPlan view = viewRepository.getFetchPlan(metadata.getClass(User.class), "user.browse");
            persistence.getEntityManager().find(User.class, this.user.getId(), view);
            tx.commit();
        }

        assertEquals(2, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group
        appender.clearMessages();

        try (Transaction tx = persistence.createTransaction()) {
            persistence.getEntityManager().getConnection();
            FetchPlanRepository viewRepository = AppBeans.get(FetchPlanRepository.class);
            FetchPlan view = viewRepository.getFetchPlan(metadata.getClass(User.class), "user.browse");
            persistence.getEntityManager().find(User.class, this.user.getId(), view);
            tx.commit();
        }

        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group
        appender.clearMessages();


        try (Transaction tx = persistence.createTransaction(new TransactionParams().setReadOnly(true))) {

            try (Transaction tx1 = persistence.getTransaction()) {
                persistence.getEntityManager().getConnection();
                tx1.commit();
            }
            FetchPlanRepository fetchPlanRepository = AppBeans.get(FetchPlanRepository.class);
            FetchPlan fetchPlan = fetchPlanRepository.getFetchPlan(metadata.getClass(User.class), "user.browse");

            Query query = persistence.getEntityManager().createQuery("select u from test$User u where u.id = :id")
                    .setParameter("id", user.getId());
            query.setView(View.copy((View) fetchPlan).setLoadPartialEntities(true));
            ((QueryImpl) query).setSingleResultExpected(true);
            User userL = (User) query.getSingleResult();
            //User userL = persistence.getEntityManager().find(User.class, user.getId(), view);
            assertNotNull(userL);

            tx.commit();
        }
        assertEquals(0, appender.filterMessages(m -> m.contains("> SELECT")).count()); // User, Group
    }

    @Test
    public void testLoadingRelatedEntityFromCache() {
        appender.clearMessages();

        FetchPlanRepository viewRepository = AppBeans.get(FetchPlanRepository.class);
        FetchPlan view = viewRepository.getFetchPlan(metadata.getClass(UserSubstitution.class), "usersubst.edit");

        try (Transaction tx = persistence.createTransaction()) {
            persistence.getEntityManager().find(UserSubstitution.class, this.userSubstitution.getId(), view);
            tx.commit();
        }

        assertEquals(3, appender.filterMessages(selectsOnly).count()); // UserSubstitution, User, User
        assertTrue(appender.filterMessages(selectsOnly).noneMatch(s -> s.contains("JOIN TEST_USER"))); // User must not be joined because it is cached

        appender.clearMessages();

        try (Transaction tx = persistence.createTransaction()) {
            persistence.getEntityManager().find(UserSubstitution.class, this.userSubstitution.getId(), view);
            tx.commit();
        }

        assertEquals(1, appender.filterMessages(selectsOnly).count()); // UserSubstitution only, User is cached
        assertTrue(appender.filterMessages(selectsOnly).noneMatch(s -> s.contains("JOIN TEST_USER"))); // User must not be joined because it is cached
    }

    @Test
    public void testNonCachedOneToManyFromCache() {
        appender.clearMessages();

        DataManager dataManager = AppBeans.get(DataManager.class);

        LoadContext<CompositeOne> loadContextList = new LoadContext<>(CompositeOne.class).setFetchPlan("compositeOne-view");
        loadContextList.setQueryString("select e from test$CompositeOne e where e.name = 'compositeOne'").setMaxResults(1);

        List<CompositeOne> results = dataManager.loadList(loadContextList);
        Assertions.assertEquals(1, results.size());
        CompositeOne compositeOne = results.get(0);
        Assertions.assertEquals(1, compositeOne.getCompositePropertyOne().size());
        CompositePropertyOne compositePropertyOne = compositeOne.getCompositePropertyOne().get(0);
        CompositeTwo compositeTwo = compositePropertyOne.getCompositeTwo();
        Assertions.assertNotNull(compositeTwo);
        Assertions.assertEquals(1, compositeTwo.getCompositePropertyTwo().size());
        CompositePropertyTwo compositePropertyTwo = compositeTwo.getCompositePropertyTwo().get(0);
        Assertions.assertEquals("compositePropertyTwo", compositePropertyTwo.getName());

        assertEquals(4, appender.filterMessages(s -> s.contains("> SELECT") && !s.contains("DYNAT_CATEGORY")).count()); // UserSubstitution, User, User

        appender.clearMessages();

        LoadContext<CompositeOne> loadContextOne = new LoadContext<>(CompositeOne.class)
                .setId(compositeOne.getId()).setFetchPlan("compositeOne-view");
        CompositeOne result = dataManager.load(loadContextOne);

        assertEquals(3, appender.filterMessages(selectsOnly).count()); // UserSubstitution only, User is cached
    }

    private void loadUserAlone() {
        try (Transaction tx = persistence.createTransaction()) {
            User user = persistence.getEntityManager().find(User.class, this.user.getId());
            assertNotNull(user);

            tx.commit();
        }
    }

    @Test
    public void testAdditionalParametersUsedDuringLazyLoading() {
        //make sure that entity cache enabled for current entities
        assertTrue(Boolean.parseBoolean(env.getProperty("eclipselink.cache.shared.test$ChildCachedEntity")));
        assertTrue(Boolean.parseBoolean(env.getProperty("eclipselink.cache.shared.test$ParentCachedEntity")));


        provider.setParam(null);
        try (Transaction tx = persistence.createTransaction()) {
            persistence.getEntityManager().find(ParentCachedEntity.class, cachedParent.getId());
            fail("Query should throw exception without additional parameter");
        } catch (QueryException e) {
            assertTrue(e.getMessage().contains("No value was provided for the session property [testAdditional]"));
        }

        provider.setParam("ONE");
        try (Transaction tx = persistence.createTransaction()) {

            Query query = persistence
                    .getEntityManager()
                    .createQuery("select p from test$ParentCachedEntity p where p.id = :id");

            query.setParameter("id", cachedParent.getId());
            ParentCachedEntity parent = (ParentCachedEntity) query.getResultList().get(0);
            javax.persistence.EntityManager em = persistence.getEntityManager().getDelegate();
            assertNotNull(parent);
            List<ChildCachedEntity> children = parent.getChildren();
            assertNotNull(children);

            /* Next line checks that changes from eclipselink 2.7.9-6-jmix (Haulmont/jmix-data#107) works:
             * DatabaseValueHolder passes additional criteria parameters using JmixUtil.
             * Entity in shared cache has no access to ClientSession with additional criteria parameters
             * so they has to be passed using another way.
             *
             * otherwise it will be exception:
             *   org.eclipse.persistence.exceptions.QueryException:
             *   Exception Description: No value was provided for the session property [testAdditional]
             */
            assertEquals(1, children.size());

            tx.commit();
        } finally {
            provider.setParam(null);
        }
    }


}
