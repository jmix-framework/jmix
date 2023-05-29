/*
 * Copyright 2019 Haulmont.
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
package com.haulmont.cuba.core;

import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.core.model.SoftDeleteOneToOneA;
import com.haulmont.cuba.core.model.SoftDeleteOneToOneB;
import com.haulmont.cuba.core.model.common.*;
import com.haulmont.cuba.core.testsupport.CoreTest;
import com.haulmont.cuba.core.testsupport.TestSupport;
import io.jmix.core.FetchMode;
import io.jmix.core.FetchPlan;
import io.jmix.core.Metadata;
import io.jmix.data.PersistenceHints;
import org.eclipse.persistence.jpa.JpaEntityManager;
import org.eclipse.persistence.platform.database.DatabasePlatform;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@CoreTest
public class SoftDeleteTest {
    @Autowired
    private Persistence persistence;
    @Autowired
    private Metadata metadata;
    @Autowired
    private TestSupport testSupport;

    private UUID groupId;
    private UUID userId, user1Id, user2Id;
    private UUID role1Id;
    private UUID role2Id;
    private UUID role3Id;
    private UUID role4Id;
    private UUID userRole1Id;
    private UUID userRole2Id;
    private UUID userRole3Id;
    private UUID userRole4Id;
    private UUID oneToOneA1Id, oneToOneA2Id, oneToOneA3Id;
    private UUID oneToOneB1Id, oneToOneB2Id;
    private UUID group1Id, groupHierarchyId, constraint1Id, constraint2Id;


    @BeforeEach
    public void setUp() throws Exception {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            Group group = new Group();
            groupId = group.getId();
            group.setName("testGroup");
            em.persist(group);

            User user = new User();
            userId = user.getId();
            user.setName("testUser");
            user.setLogin("testLogin");
            user.setGroup(group);
            em.persist(user);

            User user1 = new User();
            user1Id = user1.getId();
            user1.setName("testUser1");
            user1.setLogin("testLogin1");
            user1.setGroup(group);
            em.persist(user1);

            User user2 = new User();
            user2Id = user2.getId();
            user2.setName("testUser2");
            user2.setLogin("testLogin2");
            user2.setGroup(group);
            em.persist(user2);

            Role role1 = new Role();
            role1Id = role1.getId();
            role1.setName("role1");
            em.persist(role1);

            UserRole userRole1 = new UserRole();
            userRole1Id = userRole1.getId();
            userRole1.setUser(user);
            userRole1.setRole(role1);
            em.persist(userRole1);

            Role role2 = new Role();
            role2Id = role2.getId();
            role2.setName("roleToBeDeleted");
            em.persist(role2);

            Role role3 = new Role();
            role3Id = role3.getId();
            role3.setName("roleToBeDeleted3");
            em.persist(role3);

            Role role4 = new Role();
            role4Id = role4.getId();
            role4.setName("role4");
            em.persist(role4);

            UserRole userRole2 = new UserRole();
            userRole2Id = userRole2.getId();
            userRole2.setUser(user);
            userRole2.setRole(role2);
            em.persist(userRole2);

            UserRole userRole3 = new UserRole();
            userRole3Id = userRole3.getId();
            userRole3.setUser(user1);
            userRole3.setRole(role3);
            em.persist(userRole3);

            UserRole userRole4 = new UserRole();
            userRole4Id = userRole4.getId();
            userRole4.setUser(user2);
            userRole4.setRole(role4);
            em.persist(userRole4);

            SoftDeleteOneToOneB oneToOneB1 = metadata.create(SoftDeleteOneToOneB.class);
            oneToOneB1.setName("oneToOneB1");
            em.persist(oneToOneB1);
            oneToOneB1Id = oneToOneB1.getId();

            SoftDeleteOneToOneB oneToOneB2 = metadata.create(SoftDeleteOneToOneB.class);
            oneToOneB2.setName("oneToOneB2");
            em.persist(oneToOneB2);
            oneToOneB2Id = oneToOneB2.getId();

            SoftDeleteOneToOneA oneToOneA1 = metadata.create(SoftDeleteOneToOneA.class);
            oneToOneA1.setName("oneToOneA1");
            oneToOneA1.setB(oneToOneB1);
            em.persist(oneToOneA1);
            oneToOneA1Id = oneToOneA1.getId();

            SoftDeleteOneToOneA oneToOneA2 = metadata.create(SoftDeleteOneToOneA.class);
            oneToOneA2.setName("oneToOneA2");
            oneToOneA2.setB(oneToOneB2);
            em.persist(oneToOneA2);
            oneToOneA2Id = oneToOneA2.getId();

            Group group1 = new Group();
            group1Id = group1.getId();
            group1.setName("testGroup1");
            em.persist(group1);

            GroupHierarchy groupHierarchy = new GroupHierarchy();
            groupHierarchyId = groupHierarchy.getId();
            groupHierarchy.setGroup(group1);
            groupHierarchy.setParent(group1);
            groupHierarchy.setLevel(1);
            em.persist(groupHierarchy);

            Constraint constraint1 = new Constraint();
            constraint1Id = constraint1.getId();
            constraint1.setCode("constraint1");
            constraint1.setEntityName("test$Constraint");
            constraint1.setGroup(group1);
            em.persist(constraint1);

            Constraint constraint2 = new Constraint();
            constraint2Id = constraint2.getId();
            constraint2.setCode("constraint2");
            constraint2.setEntityName("test$Constraint");
            constraint2.setGroup(group1);
            em.persist(constraint2);

            tx.commitRetaining();

            em = persistence.getEntityManager();

            UserRole ur = em.find(UserRole.class, userRole2Id);
            em.remove(ur);

            Role r = em.find(Role.class, role2Id);
            em.remove(r);

            SoftDeleteOneToOneA oneToOneA = em.find(SoftDeleteOneToOneA.class, oneToOneA1Id);
            em.remove(oneToOneA);

            SoftDeleteOneToOneB oneToOneB = em.find(SoftDeleteOneToOneB.class, oneToOneB2Id);
            em.remove(oneToOneB);

            Constraint constraint = em.find(Constraint.class, constraint2Id);
            em.remove(constraint);

            //remove from db to prevent cascade delete user role
            JdbcTemplate jdbcTemplate = new JdbcTemplate(persistence.getDataSource());
            jdbcTemplate.update("update TEST_ROLE set DELETE_TS = CREATE_TS, DELETED_BY = CREATED_BY where name = 'roleToBeDeleted3'");

            tx.commit();
        } finally {
            tx.end();
        }

        JdbcTemplate jdbcTemplate = new JdbcTemplate(persistence.getDataSource());
        jdbcTemplate.update("update TEST_USER set DELETE_TS = current_timestamp, DELETED_BY = 'admin' where ID = ?", user2Id.toString());
    }

    @AfterEach
    public void tearDown() throws Exception {
        testSupport.deleteRecord("TEST_USER_ROLE", userRole1Id, userRole2Id, userRole3Id, userRole4Id);
        testSupport.deleteRecord("TEST_GROUP_HIERARCHY", groupHierarchyId);
        testSupport.deleteRecord("TEST_CONSTRAINT", constraint1Id, constraint2Id);
        testSupport.deleteRecord("TEST_ROLE", role1Id, role2Id, role3Id, role4Id);
        testSupport.deleteRecord("TEST_USER", userId, user1Id, user2Id);
        testSupport.deleteRecord("TEST_GROUP", groupId, group1Id);
        testSupport.deleteRecord("TEST_SOFT_DELETE_OTO_A", oneToOneA1Id, oneToOneA2Id);
        if (oneToOneA3Id != null) {
            testSupport.deleteRecord("TEST_SOFT_DELETE_OTO_A", oneToOneA3Id);
        }
        testSupport.deleteRecord("TEST_SOFT_DELETE_OTO_B", oneToOneB1Id, oneToOneB2Id);
    }

    @Test
    public void testMultipleTransactions() throws Exception {
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            assertTrue(PersistenceHints.isSoftDeletion(em.getDelegate()));

            em.setSoftDeletion(false);
            assertFalse(PersistenceHints.isSoftDeletion(em.getDelegate()));

            tx.commit();
        }

        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            assertTrue(PersistenceHints.isSoftDeletion(em.getDelegate()));

            em.setSoftDeletion(false);
            assertFalse(PersistenceHints.isSoftDeletion(em.getDelegate()));

            try (Transaction tx1 = persistence.createTransaction()) {
                assertTrue(PersistenceHints.isSoftDeletion(persistence.getEntityManager().getDelegate()));

                tx1.commit();
            }
            assertFalse(PersistenceHints.isSoftDeletion(persistence.getEntityManager().getDelegate()));

            tx.commit();
        }
    }

    @Test
    public void testNormalMode() {
        System.out.println("===================== BEGIN testNormalMode =====================");

        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            Role role = em.find(Role.class, role2Id);
            assertNull(role);

            tx.commit();
        } finally {
            tx.end();
        }
        System.out.println("===================== END testNormalMode =====================");
    }

    @Test
    public void testCleanupMode() {
        System.out.println("===================== BEGIN testCleanupMode =====================");

        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            em.setSoftDeletion(false);

            Role role = em.find(Role.class, role2Id);
            assertNotNull(role);
            assertTrue(role.isDeleted());

            tx.commit();
        } finally {
            tx.end();
        }
        System.out.println("===================== END testCleanupMode =====================");
    }

    @Test
    public void testManyToOne() throws SQLException {
        System.out.println("===================== BEGIN testManyToOne =====================");

        JdbcTemplate jdbcTemplate = new JdbcTemplate(persistence.getDataSource());
        jdbcTemplate.update("update TEST_GROUP set DELETE_TS = current_timestamp, DELETED_BY = 'admin' where ID = ?", groupId.toString());

        // test without view
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            User user = em.find(User.class, userId);
            Group group = user.getGroup();

            tx.commit();

            assertNotNull(group);
            assertTrue(group.isDeleted());
        }

        FetchPlan view;

        // test fetchMode = AUTO (JOIN is used)
        view = new View(User.class, "testView")
                .addProperty("name")
                .addProperty("login")
                .addProperty("group", new View(Group.class).addProperty("name"));
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            User user = em.find(User.class, userId, view);
            Group group = user.getGroup();

            tx.commit();

            assertNotNull(group);
            assertTrue(group.isDeleted());
        }

        // test fetchMode = UNDEFINED
        view = new View(User.class, "testView")
                .addProperty("name")
                .addProperty("login")
                .addProperty("group", new View(Group.class).addProperty("name"), FetchMode.UNDEFINED);
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            User user = em.find(User.class, userId, view);
            Group group = user.getGroup();

            tx.commit();

            assertNotNull(group);
            assertTrue(group.isDeleted());
        }

        // test fetchMode = BATCH
        view = new View(User.class, "testView")
                .addProperty("name")
                .addProperty("login")
                .addProperty("group", new View(Group.class).addProperty("name"), FetchMode.BATCH);
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            User user = em.find(User.class, userId, view);
            Group group = user.getGroup();

            tx.commit();

            assertNotNull(group);
            assertTrue(group.isDeleted());
        }

        System.out.println("===================== END testManyToOne =====================");
    }

    @Test
    public void testManyToOne_InnerJoinOnClause() throws SQLException {
        System.out.println("===================== BEGIN testManyToOne =====================");

        JdbcTemplate jdbcTemplate = new JdbcTemplate(persistence.getDataSource());
        jdbcTemplate.update("update TEST_GROUP set DELETE_TS = current_timestamp, DELETED_BY = 'admin' where ID = ?", groupId.toString());

        // test without view
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            boolean prevValue = setPrintInnerJoinInWhereClause(em, false);
            Group group = null;
            try {
                User user = em.find(User.class, userId);
                group = user.getGroup();
            } finally {
                setPrintInnerJoinInWhereClause(em, prevValue);
            }

            tx.commit();

            assertNotNull(group);
            assertTrue(group.isDeleted());
        }

        FetchPlan view;

        // test fetchMode = AUTO (JOIN is used)
        view = new View(User.class, "testView")
                .addProperty("name")
                .addProperty("login")
                .addProperty("group", new View(Group.class).addProperty("name"));
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            boolean prevValue = setPrintInnerJoinInWhereClause(em, false);
            Group group;
            try {
                User user = em.find(User.class, userId, view);
                group = user.getGroup();
            } finally {
                setPrintInnerJoinInWhereClause(em, prevValue);
            }

            tx.commit();

            assertNotNull(group);
            assertTrue(group.isDeleted());
        }

        // test fetchMode = UNDEFINED
        view = new View(User.class, "testView")
                .addProperty("name")
                .addProperty("login")
                .addProperty("group", new View(Group.class).addProperty("name"), FetchMode.UNDEFINED);
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            boolean prevValue = setPrintInnerJoinInWhereClause(em, false);
            Group group;
            try {
                User user = em.find(User.class, userId, view);
                group = user.getGroup();
            } finally {
                setPrintInnerJoinInWhereClause(em, prevValue);
            }

            tx.commit();

            assertNotNull(group);
            assertTrue(group.isDeleted());
        }

        // test fetchMode = BATCH
        view = new View(User.class, "testView")
                .addProperty("name")
                .addProperty("login")
                .addProperty("group", new View(Group.class).addProperty("name"), FetchMode.BATCH);
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            boolean prevValue = setPrintInnerJoinInWhereClause(em, false);
            Group group;
            try {
                User user = em.find(User.class, userId, view);
                group = user.getGroup();
            } finally {
                setPrintInnerJoinInWhereClause(em, prevValue);
            }

            tx.commit();

            assertNotNull(group);
            assertTrue(group.isDeleted());
        }

        System.out.println("===================== END testManyToOne =====================");
    }

    @Test
    public void testOneToMany() {
        System.out.println("===================== BEGIN testOneToMany =====================");
        // test fetchMode = AUTO
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            FetchPlan view = new View(User.class, "testView")
                    .addProperty("name")
                    .addProperty("login")
                    .addProperty("userRoles",
                            new View(UserRole.class, "testView")
                                    .addProperty("role",
                                            new View(Role.class, "testView")
                                                    .addProperty("name")));
            User user = em.find(User.class, userId, view);

            List<UserRole> userRoles = user.getUserRoles();
            assertEquals(1, userRoles.size());
            for (UserRole ur : userRoles) {
                assertNotNull(ur.getRole());
            }

            tx.commit();
        } finally {
            tx.end();
        }

        // test fetchMode = JOIN
        tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            FetchPlan view = new View(User.class, "testView")
                    .addProperty("name")
                    .addProperty("login")
                    .addProperty("userRoles", new View(UserRole.class, "testView"), FetchMode.JOIN);
            User user = em.find(User.class, userId, view);

            List<UserRole> userRoles = user.getUserRoles();
            assertEquals(1, userRoles.size());
            for (UserRole ur : userRoles) {
                assertNotNull(ur.getRole());
            }

            tx.commit();
        } finally {
            tx.end();
        }

        System.out.println("===================== END testOneToMany =====================");
    }

    @Test
    public void testOneToManyLazy() {
        System.out.println("===================== BEGIN testOneToManyLazy =====================");
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            User user = em.find(User.class, userId);

            List<UserRole> userRoles = user.getUserRoles();
            assertEquals(1, userRoles.size());
            for (UserRole ur : userRoles) {
                assertNotNull(ur.getRole());
            }

            tx.commit();
        } finally {
            tx.end();
        }
        System.out.println("===================== END testOneToManyLazy =====================");
    }

    @Test
    public void testOneToManyLazy2() {
        System.out.println("===================== BEGIN testOneToManyLazy2 =====================");
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            em.setSoftDeletion(false);

            User user = em.find(User.class, userId);

            List<UserRole> userRoles = user.getUserRoles();
            assertEquals(2, userRoles.size());
            for (UserRole ur : userRoles) {
                assertNotNull(ur.getRole());
            }

            tx.commit();
        }

        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();

            User user = em.find(User.class, userId);

            List<UserRole> userRoles = user.getUserRoles();
            assertEquals(1, userRoles.size());
            for (UserRole ur : userRoles) {
                assertNotNull(ur.getRole());
            }

            tx.commit();
        }

        System.out.println("===================== END testOneToManyLazy2 =====================");
    }

    @Test
    public void testOneToMany_CleanupMode() {
        System.out.println("===================== BEGIN testOneToMany_CleanupMode =====================");

        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            em.setSoftDeletion(false);

            FetchPlan view = new View(User.class, "testView")
                    .addProperty("name")
                    .addProperty("login")
                    .addProperty("userRoles",
                            new View(UserRole.class, "testView")
                                    .addProperty("role",
                                            new View(Role.class, "testView")
                                                    .addProperty("name")));
            User user = em.find(User.class, userId, view);

            List<UserRole> userRoles = user.getUserRoles();
            assertEquals(2, userRoles.size());
            for (UserRole ur : userRoles) {
                assertNotNull(ur.getRole());
            }

            tx.commit();
        } finally {
            tx.end();
        }
        System.out.println("===================== END testOneToMany_CleanupMode =====================");
    }

    @Test
    public void testOneToMany_Query() {
        System.out.println("===================== BEGIN testOneToMany_Query =====================");

        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            Query q = em.createQuery("select u from test$User u where u.id = ?1");
            q.setParameter(1, userId);
            User user = (User) q.getSingleResult();

            List<UserRole> userRoles = user.getUserRoles();
            assertEquals(1, userRoles.size());
            for (UserRole ur : userRoles) {
                assertNotNull(ur.getRole());
            }

            tx.commit();
        } finally {
            tx.end();
        }
        System.out.println("===================== END testOneToMany_Query =====================");
    }

    @Test
    public void testOneToMany_JoinFetchQuery() {
        System.out.println("===================== BEGIN testOneToMany_JoinFetchQuery =====================");

        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            Query q = em.createQuery("select u from test$User u join fetch u.userRoles where u.id = ?1");
            q.setParameter(1, userId);
            User user = (User) q.getSingleResult();

            List<UserRole> userRoles = user.getUserRoles();
            assertEquals(1, userRoles.size());
            for (UserRole ur : userRoles) {
                assertNotNull(ur.getRole());
            }

            tx.commit();
        } finally {
            tx.end();
        }
        System.out.println("===================== END testOneToMany_JoinFetchQuery =====================");
    }

    @Test
    public void testQuery() {
        System.out.println("===================== BEGIN testQuery =====================");

        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            Query query = em.createQuery("select r from test$Role r where r.name = ?1");
            query.setParameter(1, "roleToBeDeleted");

            List<Role> list = query.getResultList();
            assertTrue(list.isEmpty());

            tx.commit();
        } finally {
            tx.end();
        }
        System.out.println("===================== END testQuery =====================");
    }

    @Test
    public void testQuery_CleanupMode() {
        System.out.println("===================== BEGIN testQuery_CleanupMode =====================");

        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            em.setSoftDeletion(false);
            Query query = em.createQuery("select r from test$Role r where r.name = ?1");
            query.setParameter(1, "roleToBeDeleted");

            List<Role> list = query.getResultList();
            assertTrue(!list.isEmpty());

            tx.commit();
        } finally {
            tx.end();
        }
        System.out.println("===================== END testQuery_CleanupMode =====================");
    }

    @Test
    public void testUpdateQuery_CleanupMode() {
        System.out.println("===================== BEGIN testUpdateQuery_CleanupMode =====================");

        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            em.setSoftDeletion(false);
            Query query = em.createQuery("update test$Role r set r.description = ?1 where r.name = ?2");
            query.setParameter(1, "Updated");
            query.setParameter(2, "roleToBeDeleted");
            int updated = query.executeUpdate();

            assertEquals(1, updated);
            tx.commit();
        } finally {
            tx.end();
        }
        System.out.println("===================== END testUpdateQuery_CleanupMode =====================");
    }

    @Test
    public void testQueryWithoutConditions() {
        System.out.println("===================== BEGIN testQueryWithoutConditions =====================");

        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            Query query = em.createQuery("select r from test$Role r");

            List<Role> list = query.getResultList();
            for (Role role : list) {
                if (role.getId().equals(role2Id)) {
                    fail();
                }
            }

            tx.commit();
        } finally {
            tx.end();
        }
        System.out.println("===================== END testQueryWithoutConditions =====================");
    }

    @Test
    public void testRemoveNotManaged() {
        System.out.println("===================== BEGIN testRemoveNotManaged =====================");

        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            UserRole userRole = em.find(UserRole.class, userRole1Id);

            tx.commitRetaining();

            em = persistence.getEntityManager();
            em.remove(userRole);

            tx.commitRetaining();

            em = persistence.getEntityManager();
            UserRole deletedUserRole = em.find(UserRole.class, userRole1Id);
            assertNull(deletedUserRole);

            tx.commit();
        } finally {
            tx.end();
        }
        System.out.println("===================== END testRemoveNotManaged =====================");
    }

    @Test
    public void testGlobalSoftDeleteSwitch() throws Exception {
        EntityManager em;
        Transaction tx;
        tx = persistence.createTransaction();
        try {
            em = persistence.getEntityManager();
            assertTrue(em.isSoftDeletion());
            tx.commit();
        } finally {
            tx.end();
        }

        persistence.setSoftDeletion(false);
        try {
            tx = persistence.createTransaction();
            try {
                em = persistence.getEntityManager();
                assertFalse(em.isSoftDeletion());

                persistence.setSoftDeletion(true);
                em = persistence.getEntityManager();
                assertFalse(em.isSoftDeletion()); // persistence.setSoftDeletion affects only EMs created in a new tx

                tx.commit();
            } finally {
                tx.end();
            }
        } finally {
            persistence.setSoftDeletion(true);
        }

        tx = persistence.createTransaction();
        try {
            em = persistence.getEntityManager();
            assertTrue(em.isSoftDeletion());
            tx.commit();
        } finally {
            tx.end();
        }
    }

    @Test
    public void testSoftDeletionIsolation() throws Exception {
        try (Transaction tx = persistence.createTransaction()) {
            User u = persistence.getEntityManager().find(User.class, userId);
            persistence.getEntityManager().remove(u);
            tx.commit();
        }

        try (Transaction tx = persistence.createTransaction()) {
            persistence.getEntityManager().setSoftDeletion(false);
            User u = persistence.getEntityManager().find(User.class, userId);
            assertNotNull(u);

            Thread thread = new Thread(this::loadDeletedUser);
            thread.start();
            thread.join();

            tx.commit();
        }

        loadDeletedUser();
    }

    @Test
    public void testOneToOneMappedBy() {
        System.out.println("===================== BEGIN testOneToOneMappedBy =====================");
        // test fetchMode = AUTO
        System.out.println("===================== BEGIN testOneToOneMappedBy fetchMode = AUTO =====================");
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            FetchPlan view = new View(SoftDeleteOneToOneB.class, "testView")
                    .addProperty("name")
                    .addProperty("a",
                            new View(SoftDeleteOneToOneA.class, "testView").addProperty("name"));
            SoftDeleteOneToOneB oneToOneB = em.find(SoftDeleteOneToOneB.class, oneToOneB1Id, view);
            assertNotNull(oneToOneB);
            assertNull(oneToOneB.getA());

            tx.commit();
        } finally {
            tx.end();
        }

        // test fetchMode = BATCH
        System.out.println("===================== BEGIN testOneToOneMappedBy fetchMode = BATCH =====================");
        tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            FetchPlan view = new View(SoftDeleteOneToOneB.class, "testView")
                    .addProperty("name")
                    .addProperty("a",
                            new View(SoftDeleteOneToOneA.class, "testView").addProperty("name"), FetchMode.BATCH);
            SoftDeleteOneToOneB oneToOneB = em.find(SoftDeleteOneToOneB.class, oneToOneB1Id, view);
            assertNotNull(oneToOneB);
            assertNull(oneToOneB.getA());

            tx.commit();
        } finally {
            tx.end();
        }

        // test fetchMode = UNDEFINED
        System.out.println("===================== BEGIN testOneToOneMappedBy fetchMode = UNDEFINED =====================");
        tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            FetchPlan view = new View(SoftDeleteOneToOneB.class, "testView")
                    .addProperty("name")
                    .addProperty("a",
                            new View(SoftDeleteOneToOneA.class, "testView").addProperty("name"), FetchMode.UNDEFINED);
            SoftDeleteOneToOneB oneToOneB = em.find(SoftDeleteOneToOneB.class, oneToOneB1Id, view);
            assertNotNull(oneToOneB);
            assertNull(oneToOneB.getA());

            tx.commit();
        } finally {
            tx.end();
        }

        System.out.println("===================== END testOneToOneMappedBy =====================");
    }

    @Test
    public void testOneToOneMappedByLazy() {
        System.out.println("===================== BEGIN testOneToOneMappedByLazy =====================");
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            SoftDeleteOneToOneB oneToOneB = em.find(SoftDeleteOneToOneB.class, oneToOneB1Id);
            assertNotNull(oneToOneB);
            assertNull(oneToOneB.getA());

            tx.commit();
        } finally {
            tx.end();
        }
        System.out.println("===================== END testOneToOneMappedByLazy =====================");
    }

    @Test
    public void testOneToOneMappedByQuery() {
        System.out.println("===================== BEGIN testOneToOneMappedByQuery =====================");
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            SoftDeleteOneToOneA oneToOneA3 = metadata.create(SoftDeleteOneToOneA.class);
            oneToOneA3.setName("oneToOneA3");
            oneToOneA3.setB(em.find(SoftDeleteOneToOneB.class, oneToOneB1Id));
            em.persist(oneToOneA3);
            oneToOneA3Id = oneToOneA3.getId();

            tx.commit();
        } finally {
            tx.end();
        }

        tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            FetchPlan view = new View(SoftDeleteOneToOneB.class, "testView")
                    .addProperty("name")
                    .addProperty("a",
                            new View(SoftDeleteOneToOneA.class, "testView").addProperty("name"));

            List<SoftDeleteOneToOneB> r = em.createQuery("select b from test$SoftDeleteOneToOneB b where b.name = :name",
                    SoftDeleteOneToOneB.class)
                    .setParameter("name", "oneToOneB1")
                    .setView(view)
                    .getResultList();

            assertEquals(1, r.size());
            assertEquals(r.get(0).getA().getId(), oneToOneA3Id);
            assertEquals(r.get(0).getA().getName(), "oneToOneA3");

            tx.commit();
        } finally {
            tx.end();
        }

        System.out.println("===================== END testOneToOneMappedByQuery =====================");
    }

    @Test
    public void testOneToOneLazy() {
        System.out.println("===================== BEGIN testOneToOneLazy =====================");
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            SoftDeleteOneToOneA oneToOneA = em.find(SoftDeleteOneToOneA.class, oneToOneA2Id);
            assertNotNull(oneToOneA);
            assertNotNull(oneToOneA.getB());
            assertEquals(oneToOneA.getB().getId(), oneToOneB2Id);
            tx.commit();
        } finally {
            tx.end();
        }
        System.out.println("===================== END testOneToOneLazy =====================");
    }

    @Test
    public void testOneToOne() {
        System.out.println("===================== BEGIN testOneToOne =====================");
        // test fetchMode = AUTO
        System.out.println("===================== BEGIN testOneToOne fetchMode = AUTO =====================");
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            FetchPlan view = new View(SoftDeleteOneToOneA.class, "testView")
                    .addProperty("name")
                    .addProperty("b",
                            new View(SoftDeleteOneToOneB.class, "testView").addProperty("name"));
            SoftDeleteOneToOneA oneToOneA = em.find(SoftDeleteOneToOneA.class, oneToOneA2Id, view);
            assertNotNull(oneToOneA);
            assertNotNull(oneToOneA.getB());
            assertEquals(oneToOneA.getB().getId(), oneToOneB2Id);

            tx.commit();
        } finally {
            tx.end();
        }

        // test fetchMode = BATCH
        System.out.println("===================== BEGIN testOneToOneBy fetchMode = BATCH =====================");
        tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            FetchPlan view = new View(SoftDeleteOneToOneA.class, "testView")
                    .addProperty("name")
                    .addProperty("b",
                            new View(SoftDeleteOneToOneB.class, "testView").addProperty("name"), FetchMode.BATCH);
            SoftDeleteOneToOneA oneToOneA = em.find(SoftDeleteOneToOneA.class, oneToOneA2Id, view);
            assertNotNull(oneToOneA);
            assertNotNull(oneToOneA.getB());
            assertEquals(oneToOneA.getB().getId(), oneToOneB2Id);

            tx.commit();
        } finally {
            tx.end();
        }

        // test fetchMode = UNDEFINED
        System.out.println("===================== BEGIN testOneToOneBy fetchMode = UNDEFINED =====================");
        tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            FetchPlan view = new View(SoftDeleteOneToOneA.class, "testView")
                    .addProperty("name")
                    .addProperty("b",
                            new View(SoftDeleteOneToOneB.class, "testView").addProperty("name"), FetchMode.UNDEFINED);
            SoftDeleteOneToOneA oneToOneA = em.find(SoftDeleteOneToOneA.class, oneToOneA2Id, view);
            assertNotNull(oneToOneA);
            assertNotNull(oneToOneA.getB());
            assertEquals(oneToOneA.getB().getId(), oneToOneB2Id);

            tx.commit();
        } finally {
            tx.end();
        }

        System.out.println("===================== END testOneToOne =====================");
    }

    @Test
    public void testOneToOne_InnerJoinOnClause() {
        System.out.println("===================== BEGIN testOneToOne_InnerJoinOnClause =====================");
        // test fetchMode = AUTO
        System.out.println("===================== BEGIN testOneToOne_InnerJoinOnClause fetchMode = AUTO =====================");
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            boolean prevValue = setPrintInnerJoinInWhereClause(em, false);

            try {
                FetchPlan view = new View(SoftDeleteOneToOneA.class, "testView")
                        .addProperty("name")
                        .addProperty("b",
                                new View(SoftDeleteOneToOneB.class, "testView").addProperty("name"));
                SoftDeleteOneToOneA oneToOneA = em.find(SoftDeleteOneToOneA.class, oneToOneA2Id, view);
                assertNotNull(oneToOneA);
                assertNotNull(oneToOneA.getB());
                assertEquals(oneToOneA.getB().getId(), oneToOneB2Id);
            } finally {
                setPrintInnerJoinInWhereClause(em, prevValue);
            }

            tx.commit();
        } finally {
            tx.end();
        }

        // test fetchMode = BATCH
        System.out.println("===================== BEGIN testOneToOne_InnerJoinOnClause fetchMode = BATCH =====================");
        tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            boolean prevValue = setPrintInnerJoinInWhereClause(em, false);

            try {
                FetchPlan view = new View(SoftDeleteOneToOneA.class, "testView")
                        .addProperty("name")
                        .addProperty("b",
                                new View(SoftDeleteOneToOneB.class, "testView").addProperty("name"), FetchMode.BATCH);
                SoftDeleteOneToOneA oneToOneA = em.find(SoftDeleteOneToOneA.class, oneToOneA2Id, view);
                assertNotNull(oneToOneA);
                assertNotNull(oneToOneA.getB());
                assertEquals(oneToOneA.getB().getId(), oneToOneB2Id);
            } finally {
                setPrintInnerJoinInWhereClause(em, prevValue);
            }

            tx.commit();
        } finally {
            tx.end();
        }

        // test fetchMode = UNDEFINED
        System.out.println("===================== BEGIN testOneToOne_InnerJoinOnClause fetchMode = UNDEFINED =====================");
        tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            boolean prevValue = setPrintInnerJoinInWhereClause(em, false);

            try {
                FetchPlan view = new View(SoftDeleteOneToOneA.class, "testView")
                        .addProperty("name")
                        .addProperty("b",
                                new View(SoftDeleteOneToOneB.class, "testView").addProperty("name"), FetchMode.UNDEFINED);
                SoftDeleteOneToOneA oneToOneA = em.find(SoftDeleteOneToOneA.class, oneToOneA2Id, view);
                assertNotNull(oneToOneA);
                assertNotNull(oneToOneA.getB());
                assertEquals(oneToOneA.getB().getId(), oneToOneB2Id);
            } finally {
                setPrintInnerJoinInWhereClause(em, prevValue);
            }

            tx.commit();
        } finally {
            tx.end();
        }

        System.out.println("===================== END testOneToOne =====================");
    }

    @Test
    public void testOneToOneQuery() {
        System.out.println("===================== BEGIN testOneToOneQuery =====================");
        // test fetchMode = AUTO
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            FetchPlan view = new View(SoftDeleteOneToOneA.class, "testView")
                    .addProperty("name")
                    .addProperty("b",
                            new View(SoftDeleteOneToOneB.class, "testView").addProperty("name"));

            List<SoftDeleteOneToOneA> r = em.createQuery("select a from test$SoftDeleteOneToOneA a where a.name = :name",
                    SoftDeleteOneToOneA.class)
                    .setParameter("name", "oneToOneA2")
                    .setView(view)
                    .getResultList();

            assertEquals(1, r.size());
            assertEquals(r.get(0).getB().getId(), oneToOneB2Id);

            tx.commit();
        } finally {
            tx.end();
        }
        System.out.println("===================== END testOneToOneQuery =====================");
    }

    @Test
    public void testOneToOneQuery_InnerJoinOnClause() {
        System.out.println("===================== BEGIN testOneToOneQuery_InnerJoinOnClause =====================");
        // test fetchMode = AUTO
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            boolean prevValue = setPrintInnerJoinInWhereClause(em, false);

            try {
                FetchPlan view = new View(SoftDeleteOneToOneA.class, "testView")
                        .addProperty("name")
                        .addProperty("b",
                                new View(SoftDeleteOneToOneB.class, "testView").addProperty("name"));

                List<SoftDeleteOneToOneA> r = em.createQuery("select a from test$SoftDeleteOneToOneA a where a.name = :name",
                        SoftDeleteOneToOneA.class)
                        .setParameter("name", "oneToOneA2")
                        .setView(view)
                        .getResultList();

                assertEquals(1, r.size());
                assertEquals(r.get(0).getB().getId(), oneToOneB2Id);
            } finally {
                setPrintInnerJoinInWhereClause(em, prevValue);
            }
            tx.commit();
        } finally {
            tx.end();
        }
        System.out.println("===================== END testOneToOneQuery_InnerJoinOnClause =====================");
    }

    private void loadDeletedUser() {
        try (Transaction tx = persistence.createTransaction()) {
            User u = persistence.getEntityManager().find(User.class, userId);
            assertNull(u);
            tx.commit();
        }
    }

    @Test
    public void testReferenceToDeletedEntityThroughOneToMany() throws Exception {
        FetchPlan userRoleView = new View(UserRole.class).addProperty("role", new View(Role.class).addProperty("deleteTs"));
        FetchPlan userView = new View(User.class).addProperty("userRoles", userRoleView);

        Role deleted = persistence.callInTransaction((em) -> em.find(Role.class, role3Id));
        assertNull(deleted);

        UserRole userRole = persistence.callInTransaction((em) -> em.find(UserRole.class, userRole3Id, userRoleView));
        assertNotNull(userRole.getRole());
        assertEquals(role3Id, userRole.getRole().getId());
        assertTrue(userRole.getRole().isDeleted());

        User user = persistence.callInTransaction((em) -> em.find(User.class, user1Id, userView));
        assertEquals(role3Id, user.getUserRoles().iterator().next().getRole().getId());
        Assertions.assertTrue(user.getUserRoles().iterator().next().getRole().isDeleted());
    }

    @Test
    public void testReferenceToDeletedEntityOneToManyThroughManyToOne() {
        FetchPlan constraintView = new View(Constraint.class)
                .addProperty("code");
        FetchPlan groupView = new View(Group.class)
                .addProperty("name")
                .addProperty("constraints", constraintView, FetchMode.BATCH);
        FetchPlan groupHierarchyView = new View(GroupHierarchy.class).addProperty("group", groupView, FetchMode.BATCH);

        GroupHierarchy groupHierarchy = persistence.callInTransaction((em) -> em.find(GroupHierarchy.class, groupHierarchyId, groupHierarchyView));
        assertNotNull(groupHierarchy);
        assertNotNull(groupHierarchy.getGroup());
        assertEquals(1, groupHierarchy.getGroup().getConstraints().size());
    }

    @Test
    public void testSoftDeleteWithJPQLJoin_InnerJoinOnClause() {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            boolean prevValue = setPrintInnerJoinInWhereClause(em, false);

            try {
                Query query = em.createQuery("select r from test$UserRole r join r.user u where u.login = :user");
                query.setParameter("user", "testLogin2");
                List list = query.getResultList();
                assertTrue(list.isEmpty());

                query = em.createQuery("select r from test$UserRole r where r.user.login = :user");
                query.setParameter("user", "testLogin2");
                list = query.getResultList();
                assertTrue(list.isEmpty());
            } finally {
                setPrintInnerJoinInWhereClause(em, prevValue);
            }
            tx.commit();
        } finally {
            tx.end();
        }
    }

    protected boolean setPrintInnerJoinInWhereClause(EntityManager entityManager, boolean value) {
        JpaEntityManager jpaEntityManager = entityManager.getDelegate().unwrap(JpaEntityManager.class);
        DatabasePlatform platform = jpaEntityManager.getActiveSession().getPlatform();
        boolean prevValue = platform.shouldPrintInnerJoinInWhereClause();
        platform.setPrintInnerJoinInWhereClause(value);
        return prevValue;
    }
}
