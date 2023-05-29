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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.EntityStates;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.core.model.LinkEntity;
import com.haulmont.cuba.core.model.MultiLinkEntity;
import com.haulmont.cuba.core.model.common.*;
import com.haulmont.cuba.core.model.selfinherited.ChildEntity;
import com.haulmont.cuba.core.model.selfinherited.RootEntity;
import com.haulmont.cuba.core.model.selfinherited.RootEntityDetail;
import com.haulmont.cuba.core.testsupport.CoreTest;
import com.haulmont.cuba.core.testsupport.TestSupport;
import io.jmix.core.*;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@CoreTest
public class ViewTest {
    @Autowired
    private TimeSource timeSource;
    @Autowired
    private Persistence persistence;
    @Autowired
    private Metadata metadata;
    @Autowired
    private EntityStates entityStates;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private TestSupport testSupport;
    @Autowired
    private ViewRepository viewRepository;
    @Autowired
    private FetchPlans fetchPlans;

    private UUID userId;
    private UUID groupId;
    private RootEntity rootEntity;
    private ChildEntity childEntity;
    private MultiLinkEntity multiLinkEntity;
    private LinkEntity linkEntity1, linkEntity2, linkEntity3;


    @BeforeEach
    public void setUp() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.getLogger("com.haulmont.cuba.core.sys.FetchGroupManager").setLevel(Level.TRACE);

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
            user.setLogin("login" + userId);
            user.setPassword("000");
            user.setGroup(group);
            em.persist(user);

            childEntity = metadata.create(ChildEntity.class);
            childEntity.setName("childEntityName");
            childEntity.setDescription("childEntityDescription");
            em.persist(childEntity);

            rootEntity = metadata.create(RootEntity.class);
            rootEntity.setDescription("rootEntityDescription");
            rootEntity.setEntity(childEntity);
            em.persist(rootEntity);

            RootEntityDetail detail1 = metadata.create(RootEntityDetail.class);
            detail1.setInfo("detail1");
            detail1.setMaster(childEntity);
            em.persist(detail1);

            RootEntityDetail detail2 = metadata.create(RootEntityDetail.class);
            detail2.setInfo("detail2");
            detail2.setMaster(childEntity);
            em.persist(detail2);

            linkEntity1 = metadata.create(LinkEntity.class);
            linkEntity1.setId(1L);
            linkEntity1.setName("A");
            em.persist(linkEntity1);

            linkEntity2 = metadata.create(LinkEntity.class);
            linkEntity2.setId(2L);
            linkEntity2.setName("B");
            em.persist(linkEntity2);

            linkEntity3 = metadata.create(LinkEntity.class);
            linkEntity3.setId(3L);
            linkEntity3.setName("C");
            em.persist(linkEntity3);

            multiLinkEntity = metadata.create(MultiLinkEntity.class);
            multiLinkEntity.setId(1L);
            multiLinkEntity.setA(linkEntity1);
            multiLinkEntity.setB(linkEntity2);
            multiLinkEntity.setC(linkEntity3);
            em.persist(multiLinkEntity);

            tx.commit();
        } finally {
            tx.end();
        }
    }

    @AfterEach
    public void tearDown() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.getLogger("com.haulmont.cuba.core.sys.FetchGroupManager").setLevel(Level.DEBUG);

        testSupport.deleteRecord("TEST_USER", userId);
        testSupport.deleteRecord("TEST_GROUP", groupId);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(persistence.getDataSource());

        jdbcTemplate.update("delete from TEST_ROOT_ENTITY_DETAIL");
        jdbcTemplate.update("delete from TEST_CHILD_ENTITY");
        jdbcTemplate.update("delete from TEST_ROOT_ENTITY");
        jdbcTemplate.update("delete from TEST_MULTI_LINK_ENTITY");
        jdbcTemplate.update("delete from TEST_LINK_ENTITY");
    }

    @Test
    public void testQuery() throws Exception {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            Query q = em.createQuery("select u from test$User u where u.id = ?1");
            q.setParameter(1, userId);

            FetchPlan view = new View(User.class)
                    .addProperty("name")
                    .addProperty("login")
                    .addProperty("group",
                            new View(Group.class)
                                    .addProperty("name")
                    )
                    .setLoadPartialEntities(true);
            q.setView(view);

            User user = (User) q.getSingleResult();

            tx.commit();
            user = testSupport.reserialize(user);

            try {
                user.getPassword();
                fail();
            } catch (Exception ignored) {
            }
            assertNotNull(user.getGroup().getName());
        } finally {
            tx.end();
        }
    }

    @Test
    public void testMinimalView() throws Exception {
        FetchPlan minimalView = viewRepository.getView(User.class, View.MINIMAL);

        assertEquals(View.MINIMAL, minimalView.getName());

        User user = dataManager.load(LoadContext.create(User.class).setId(userId).setView(minimalView));
        assertFalse(entityStates.isLoaded(user, "updateTs"));
        assertTrue(entityStates.isLoaded(user, "login"));

        user = dataManager.load(LoadContext.create(User.class).setId(userId).setView(View.MINIMAL));
        assertFalse(entityStates.isLoaded(user, "updateTs"));
        assertTrue(entityStates.isLoaded(user, "login"));
    }

    @Test
    public void testInstanceNameView() throws Exception {
        FetchPlan instanceNameView = viewRepository.getView(User.class, View.INSTANCE_NAME);

        assertEquals(View.INSTANCE_NAME, instanceNameView.getName());

        User user = dataManager.load(LoadContext.create(User.class).setId(userId).setView(instanceNameView));
        assertFalse(entityStates.isLoaded(user, "updateTs"));
        assertTrue(entityStates.isLoaded(user, "login"));

        user = dataManager.load(LoadContext.create(User.class).setId(userId).setView(View.INSTANCE_NAME));
        assertFalse(entityStates.isLoaded(user, "updateTs"));
        assertTrue(entityStates.isLoaded(user, "login"));

    }

    @Test
    public void testEntityManager() throws Exception {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            FetchPlan view = new View(User.class)
                    .addProperty("name")
                    .addProperty("login")
                    .addProperty("createTs")
                    .addProperty("group",
                            new View(Group.class)
                                    .addProperty("name")
                                    .addProperty("createTs")
                    ).setLoadPartialEntities(true);

            User user = em.find(User.class, userId, view);

            tx.commit();
            user = testSupport.reserialize(user);

            try {
                user.getPassword();
                fail();
            } catch (Exception ignored) {
            }
            assertNotNull(user.getGroup().getName());
            assertNotNull(user.getCreateTs());
            assertNotNull(user.getGroup().getCreateTs());
        } finally {
            tx.end();
        }
    }

    @Test
    public void testViewWithoutSystemProperties() throws Exception {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            FetchPlan view = new View(User.class, false)
                    .addProperty("name")
                    .addProperty("login")
                    .addProperty("group",
                            new View(Group.class, false)
                                    .addProperty("name")
                    ).setLoadPartialEntities(true);

            User user = em.find(User.class, userId, view);

            tx.commit();
            user = testSupport.reserialize(user);

            try {
                user.getPassword();
                fail();
            } catch (Exception ignored) {
            }
            try {
                user.getCreateTs();
                fail();
            } catch (Exception ignored) {
            }
            assertNotNull(user.getGroup().getName());
            try {
                user.getGroup().getCreateTs();
                fail();
            } catch (Exception ignored) {
            }
        } finally {
            tx.end();
        }
    }

    @Test
    public void testViewWithoutSystemProperties_update() throws Exception {

        View view = new View(User.class, false)
                .addProperty("name")
                .addProperty("login")
                .addProperty("loginLowerCase")
                .addProperty("group",
                        new View(Group.class, false)
                                .addProperty("name")
                );
        view.setLoadPartialEntities(true);

        try (Transaction tx = persistence.createTransaction()) {
            // First stage: change managed

            long ts = timeSource.currentTimeMillis();
            Thread.sleep(200);

            EntityManager em = persistence.getEntityManager();
            User user = em.find(User.class, userId, view);

            assertFalse(entityStates.isLoaded(user, "updateTs"));

            user.setName(new Date().toString());

            tx.commitRetaining();

            assertTrue(getCurrentUpdateTs() > ts);

            // Second stage: change detached

            ts = timeSource.currentTimeMillis();
            Thread.sleep(1000);

            em = persistence.getEntityManager();
            user = em.find(User.class, userId, view);

            tx.commitRetaining();

            assertFalse(entityStates.isLoaded(user, "updateTs"));

            user.setName(new Date().toString());
            em = persistence.getEntityManager();
            em.merge(user);

            tx.commit();

            assertTrue(getCurrentUpdateTs() > ts);
        }

        // test _minimal
        try (Transaction tx = persistence.createTransaction()) {
            long ts = timeSource.currentTimeMillis();
            Thread.sleep(1000);

            View minimalView = (View) metadata.getViewRepository().getView(User.class, FetchPlan.INSTANCE_NAME);
            minimalView.setLoadPartialEntities(true);

            EntityManager em = persistence.getEntityManager();
            User user = em.find(User.class, userId, minimalView);

            tx.commitRetaining();

            assertFalse(entityStates.isLoaded(user, "updateTs"));

            user.setName(new Date().toString());
            em = persistence.getEntityManager();
            em.merge(user);

            tx.commit();

            assertTrue(getCurrentUpdateTs() > ts);

            tx.commit();
        }

        // test DataManager
        long ts = timeSource.currentTimeMillis();
        Thread.sleep(1000);

        User user = dataManager.load(LoadContext.create(User.class).setId(userId).setView(view));

        assertFalse(entityStates.isLoaded(user, "updateTs"));

        user.setName(new Date().toString());
        dataManager.commit(user);

        assertTrue(getCurrentUpdateTs() > ts);
    }

    /*
     * Pre 6.0: Test that entity which is loaded with view, can lazily fetch not-loaded attributes until transaction ends.
     *
     * 6.0: Not loaded (unfetched) attributes cannot be loaded lazily. Otherwise, previously loaded reference attributes are lost.
     */
    @Test
    public void testLazyLoadAfterLoadWithView() throws Exception {
        FetchPlan view = new View(User.class, false)
                .addProperty("name")
                .addProperty("group", new View(Group.class)
                        .addProperty("name"))
                .setLoadPartialEntities(true);

        User user;
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            user = em.find(User.class, userId, view);
            tx.commit();
        } finally {
            tx.end();
        }

        user = testSupport.reserialize(user);
        assertNotNull(user.getGroup().getName());

        // login is not loaded after transaction is finished
        try {
            user.getLogin();
            fail();
        } catch (Exception ignored) {
        }

        tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            user = em.find(User.class, userId, view);
            assertNotNull(user);

            // field is loaded lazily but the object becomes not partial and references loaded by previous view are cleared
            user.getLogin();

            tx.commit();
        } finally {
            tx.end();
        }
        user = testSupport.reserialize(user);
        try {
            user.getGroup();
            fail();
        } catch (Exception e) {
        }
    }


    @Test
    public void testLazyProperty() throws Exception {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            Query q = em.createQuery("select u from test$User u where u.id = ?1");
            q.setParameter(1, userId);

            FetchPlan userRoleView = new View(UserRole.class).addProperty("role", new View(Role.class).addProperty("name"));
            FetchPlan view = new View(User.class)
                    .addProperty("name")
                    .addProperty("login")
                    .addProperty("userRoles", userRoleView, true)
                    .addProperty("group",
                            new View(Group.class)
                                    .addProperty("name")
                    ).setLoadPartialEntities(true);
            q.setView(view);

            User user = (User) q.getSingleResult();

            tx.commit();
            user = testSupport.reserialize(user);

            user.getUserRoles().size();

            try {
                user.getPassword();
                fail();
            } catch (Exception ignored) {
            }
            assertNotNull(user.getGroup().getName());
        } finally {
            tx.end();
        }
    }

    private long getCurrentUpdateTs() {
        String sql = "select UPDATE_TS from TEST_USER where ID = '" + userId.toString() + "'";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(persistence.getDataSource());
        return Objects.requireNonNull(jdbcTemplate.queryForObject(sql, Date.class)).getTime();
    }

    @Test
    public void testNoTransientPropertiesInLocalView() throws Exception {
        FetchPlan view = metadata.getViewRepository().getView(EntitySnapshot.class, FetchPlan.LOCAL);
        FetchPlanProperty prop = view.getProperty("label");
        assertNull(prop);
    }

    @Test
    public void testViewCopy() throws Exception {
        ViewRepository viewRepository = metadata.getViewRepository();
        View view = (View) viewRepository.getView(User.class, FetchPlan.LOCAL);
        view.addProperty("group", viewRepository.getView(Group.class, FetchPlan.INSTANCE_NAME));

        assertNotNull(view.getProperty("group"));
        assertNull(viewRepository.getView(User.class, FetchPlan.LOCAL).getProperty("group"));
    }

    @Test
    public void testFetchGroupIsAbsentIfViewIsFull() throws Exception {
        ViewRepository viewRepository = metadata.getViewRepository();
        View view = (View) viewRepository.getView(User.class, FetchPlan.LOCAL);
        view.addProperty("group", new View(Group.class)
                .addProperty("name"))
                .addProperty("userRoles", new View(UserRole.class)
                        .addProperty("role", new View(Role.class)
                                .addProperty("name")))
                .addProperty("substitutions", new View(UserSubstitution.class)
                        .addProperty("startDate")
                        .addProperty("substitutedUser", new View(User.class)
                                .addProperty("login")
                                .addProperty("name")));

        User u;
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            u = em.find(User.class, userId, view);
            tx.commit();
        }
        assertNotNull(u);
        assertTrue(entityStates.isLoaded(u, "login"));
        assertTrue(entityStates.isLoaded(u, "group"));
        assertTrue(entityStates.isLoaded(u, "userRoles"));
        assertTrue(entityStates.isLoaded(u, "substitutions"));

        assertTrue(u instanceof FetchGroupTracker);
        assertNull(((FetchGroupTracker) u)._persistence_getFetchGroup());
    }

    @Test
    public void testSelfReferenceInView() {
        ViewRepository viewRepository = metadata.getViewRepository();
        View view = (View) viewRepository.getView(RootEntity.class, FetchPlan.LOCAL);
        view.addProperty("entity", new View(ChildEntity.class)
                .addProperty("name").addProperty("description"), FetchMode.AUTO);
        RootEntity e;
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            e = em.find(RootEntity.class, rootEntity.getId(), view);
            tx.commit();
        }
        assertNotNull(e);
        assertNotNull(e.getEntity());
        assertEquals("rootEntityDescription", e.getDescription());
        assertEquals("childEntityDescription", e.getEntity().getDescription());
        assertEquals("childEntityName", e.getEntity().getName());
    }

    @Test
    public void testNestedCollectionInJoinedInheritance() throws Exception {
        View childEntityView = new View(ChildEntity.class, false)
                .addProperty("description")
                .addProperty("name")
                .addProperty("details", new View(RootEntityDetail.class, false)
                        .addProperty("info"));
        childEntityView.setLoadPartialEntities(true);

        ChildEntity loaded;
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            TypedQuery<ChildEntity> query = em.createQuery("select e from test$ChildEntity e where e.id = ?1", ChildEntity.class);
            query.setParameter(1, childEntity.getId());
            query.setView(childEntityView);
            loaded = query.getSingleResult();
            tx.commit();
        }
        assertEquals(childEntity, loaded);
        assertNotNull(loaded.getDetails());
        assertEquals(2, loaded.getDetails().size());
    }

    @Test
    public void testMultiLinkInEntity() throws Exception {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            FetchPlan linkView = new View(LinkEntity.class).addProperty("name");
            FetchPlan view = new View(MultiLinkEntity.class)
                    .addProperty("a", linkView)
                    .addProperty("b", linkView)
                    .addProperty("c", linkView)
                    .setLoadPartialEntities(true);

            MultiLinkEntity reloadEntity = em.find(MultiLinkEntity.class, multiLinkEntity.getId(), view);

            tx.commit();
            reloadEntity = testSupport.reserialize(reloadEntity);
            assertEquals("A", reloadEntity.getA().getName());
            assertEquals("B", reloadEntity.getB().getName());
            assertEquals("C", reloadEntity.getC().getName());
        } finally {
            tx.end();
        }
    }

    @Test
    public void viewClassTest() {
        FetchPlanBuilder builder = fetchPlans.builder(User.class);
        FetchPlan plan = builder.build();
        assertTrue(ViewBuilder.class.isAssignableFrom(builder.getClass()));
        assertTrue(View.class.isAssignableFrom(plan.getClass()));
    }
}
