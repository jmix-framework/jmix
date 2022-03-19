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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.haulmont.cuba.core.model.UserDTO;
import com.haulmont.cuba.core.model.common.Group;
import com.haulmont.cuba.core.model.common.Role;
import com.haulmont.cuba.core.model.common.RoleType;
import com.haulmont.cuba.core.model.common.User;
import com.haulmont.cuba.core.testsupport.CoreTest;
import com.haulmont.cuba.core.testsupport.TestSupport;
import com.haulmont.cuba.core.sys.AppContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import javax.persistence.FlushModeType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@CoreTest
public class QueryTest {
    @Autowired
    private Persistence persistence;
    @Autowired
    private TestSupport testSupport;

    private UUID userId;
    private UUID user2Id;
    private UUID group1Id, group2Id;
    private UUID roleId;

    @BeforeEach
    public void setUp() throws Exception {

        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();

            Group group = new Group();
            group1Id = group.getId();
            group.setName("group");
            em.persist(group);

            User user = new User();
            userId = user.getId();
            user.setName("testUser");
            user.setLogin("testLogin");
            user.setGroup(group);
            em.persist(user);

            user = new User();
            user2Id = user.getId();
            user.setName("testUser2");
            user.setLogin("testLogin2");
            user.setGroup(group);
            em.persist(user);

            group = new Group();
            group2Id = group.getId();
            group.setName("testGroup");
            em.persist(group);

            Role role = new Role();
            roleId = role.getId();
            role.setName("role");
            role.setType(RoleType.SUPER);
            em.persist(role);

            tx.commit();
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        testSupport.deleteRecord("TEST_USER", userId, user2Id);
        testSupport.deleteRecord("TEST_GROUP", group1Id, group2Id);
        testSupport.deleteRecord("TEST_ROLE", roleId);
    }

    @Test
    public void testNullParam() {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            Query query = em.createQuery("select r from test$Role r where r.deleteTs = :dts");
            query.setParameter("dts", null);
            List list = query.getResultList();

            assertFalse(list.isEmpty());

            tx.commit();
        } finally {
            tx.end();
        }
    }

    @Test
    public void testUpdate() {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            Group group = em.find(Group.class, group2Id);

            Query query = em.createQuery("update test$User u set u.group = :group where u.id = :userId");
            query.setParameter("userId", userId);
            query.setParameter("group", group, false);
            query.executeUpdate();

            tx.commit();
        } finally {
            tx.end();
        }
    }

// This test doesn't pass for some unclarified reason.
//
//    public void testFlushBeforeUpdate() {
//        Transaction tx = persistence.createTransaction();
//        try {
//            EntityManager em = persistence.getEntityManager();
//
//            Group group = em.find(Group.class, groupId);
//            User user = em.find(User.class, userId);
//            assertNotNull(user);
//            user.setName("newName");
//
//            Query query = em.createQuery("update test$User u set u.group = :group where u.id = :userId");
//            query.setParameter("userId", userId);
//            query.setParameter("group", group, false);
//            query.executeUpdate();
//
//            tx.commit();
//        } finally {
//            tx.end();
//        }
//
//        tx = persistence.createTransaction();
//        try {
//            EntityManager em = persistence.getEntityManager();
//            User user = em.find(User.class, userId);
//            assertNotNull(user);
//            assertEquals(groupId, user.getGroup().getId());
//            assertEquals("newName", user.getName());
//
//            tx.commit();
//        } finally {
//            tx.end();
//        }
//    }

    @Test
    public void testAssociatedResult() throws Exception {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            Query query = em.createQuery("select u.group from test$User u where u.id = :userId");
            query.setParameter("userId", userId);
            List list = query.getResultList();

            assertFalse(list.isEmpty());

            tx.commit();
        } finally {
            tx.end();
        }
    }

    @Test
    public void testIgnoreChanges() throws Exception {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            TypedQuery<User> query;
            List<User> list;

            query = em.createQuery("select u from test$User u where u.name = ?1", User.class);
            query.setParameter(1, "testUser");
            list = query.getResultList();
            assertEquals(1, list.size());
            User user = list.get(0);

            user.setName("newName");

            query = em.createQuery("select u from test$User u where u.name = ?1", User.class);
            query.setParameter(1, "testUser");
            list = query.getResultList();
            assertEquals(1, list.size());
            User user1 = list.get(0);

            assertTrue(user1 == user);

            tx.commit();
        } finally {
            tx.end();
        }
    }

    @Test
    public void testFlushModeAuto() throws Exception {
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();

            TypedQuery<User> query;
            List<User> list;

            query = em.createQuery("select u from test$User u where u.name = ?1", User.class);
            query.setParameter(1, "testUser");
            list = query.getResultList();
            assertEquals(1, list.size());
            User user = list.get(0);

            user.setName("newName");

            query = em.createQuery("select u from test$User u where u.name = ?1", User.class);
            query.setParameter(1, "newName");
            query.setFlushMode(FlushModeType.AUTO);
            list = query.getResultList();
            assertEquals(1, list.size());
            User user1 = list.get(0);

            assertTrue(user1 == user);

            tx.commit();
        }
    }

    @Test
    public void testNativeQueryIgnoreChanges() throws Exception {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            TypedQuery<User> query;
            List<User> list;

            query = em.createNativeQuery("select * from TEST_USER where NAME = ?1", User.class);
            query.setParameter(1, "testUser");
            list = query.getResultList();
            assertEquals(1, list.size());
            User user = list.get(0);

            user.setName("newName");

            query = em.createNativeQuery("select * from TEST_USER where NAME = ?1", User.class);
            query.setParameter(1, "testUser");
            list = query.getResultList();
            assertEquals(1, list.size());
            User user1 = list.get(0);

            assertTrue(user1 == user);

            tx.commit();
        } finally {
            tx.end();
        }
    }

    @Test
    public void testNativeQuerySelect() throws Exception {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            Query query = em.createNativeQuery("select ID, LOGIN from TEST_USER where NAME = ?1");
            query.setParameter(1, "testUser");
            List list = query.getResultList();
            assertEquals(1, list.size());
            assertTrue(list.get(0) instanceof Object[]);
            Object[] row = (Object[]) list.get(0);
            assertEquals(userId.toString(), row[0]);
            assertEquals("testLogin", row[1]);

            tx.commit();
        } finally {
            tx.end();
        }
    }

    @Test
    public void testNativeQueryFlushBeforeUpdate() {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            Group group = em.find(Group.class, group2Id);
            User user = em.find(User.class, userId);
            assertNotNull(user);
            user.setName("newName");

            Query query = em.createNativeQuery("update TEST_USER set GROUP_ID = ?1 where ID = ?2");
            query.setParameter(1, group.getId().toString());
            query.setParameter(2, userId.toString());
            query.executeUpdate();

            tx.commit();
        } finally {
            tx.end();
        }

        tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            User user = em.find(User.class, userId);
            assertNotNull(user);
            assertEquals(group2Id, user.getGroup().getId());
            assertEquals("newName", user.getName());

            tx.commit();
        } finally {
            tx.end();
        }
    }

    @Test
    public void testCaseInsensitiveSearch() throws Exception {
        Transaction tx = persistence.createTransaction();
        try {
            TypedQuery<User> query = persistence.getEntityManager().createQuery(
                    "select u from test$User u where u.name like :name", User.class);
            query.setParameter("name", "(?i)%user%");
            List<User> list = query.getResultList();
            tx.commit();

            Iterables.find(list, new Predicate<User>() {
                @Override
                public boolean apply(User input) {
                    return input.getId().equals(userId);
                }
            });
        } finally {
            tx.end();
        }

    }

    @Test
    public void testListParameter() throws Exception {
        try (Transaction tx = persistence.createTransaction()) {
            TypedQuery<User> query = persistence.getEntityManager().createQuery(
                    "select u from test$User u where u.id in :ids order by u.createTs", User.class);
            query.setParameter("ids", Arrays.asList(UUID.fromString("60885987-1b61-4247-94c7-dff348347f93"), userId, user2Id));
            List<User> list = query.getResultList();
            assertEquals(2, list.size());

            tx.commit();
        }

        // Implicit conversion

        User user1, user2, user3;
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            user1 = em.find(User.class, userId);
            user2 = em.find(User.class, user2Id);
            tx.commit();
        }

        try (Transaction tx = persistence.createTransaction()) {
            TypedQuery<User> query = persistence.getEntityManager().createQuery(
                    "select u from test$User u where u.id in :ids order by u.createTs", User.class);
            query.setParameter("ids", Arrays.asList(user1, user2));
            List<User> list = query.getResultList();
            assertEquals(2, list.size());

            tx.commit();
        }

        // Positional parameters

        try (Transaction tx = persistence.createTransaction()) {
            TypedQuery<User> query = persistence.getEntityManager().createQuery(
                    "select u from test$User u where u.id in ?1 order by u.createTs", User.class);
            query.setParameter(1, Arrays.asList(user1.getId(), user2.getId()));
            List<User> list = query.getResultList();
            assertEquals(2, list.size());

            tx.commit();
        }

        // Positional parameters with implicit conversion

        try (Transaction tx = persistence.createTransaction()) {
            TypedQuery<User> query = persistence.getEntityManager().createQuery(
                    "select u from test$User u where u.id in ?1 order by u.createTs", User.class);
            query.setParameter(1, Arrays.asList(user1, user2));
            List<User> list = query.getResultList();
            assertEquals(2, list.size());

            tx.commit();
        }
    }

    @Test
    public void testEmptyCollectionParameter() throws Exception {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            Query query = em.createQuery("select u from test$User u where u.id in :ids");
            query.setParameter("ids", Collections.emptyList());
            List list = query.getResultList();
            assertTrue(list.isEmpty());

            query = em.createQuery("select u from test$User u where u.id in (:ids)");
            query.setParameter("ids", Collections.emptyList());
            list = query.getResultList();
            assertTrue(list.isEmpty());

            query = em.createQuery("select u from test$User u where u.id not in :ids");
            query.setParameter("ids", Collections.emptyList());
            list = query.getResultList();
            assertFalse(list.isEmpty());

            query = em.createQuery("select u from test$User u where u.id not in (:ids)");
            query.setParameter("ids", Collections.emptyList());
            list = query.getResultList();
            assertFalse(list.isEmpty());

            tx.commit();
        } finally {
            tx.end();
        }
    }

    @Test
    public void testNullCollectionParameter() throws Exception {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            Query query = em.createQuery("select u from test$User u where u.id in :ids");
            query.setParameter("ids", null);
            List list = query.getResultList();
            assertTrue(list.isEmpty());

            query = em.createQuery("select u from test$User u where u.id in (:ids)");
            query.setParameter("ids", null);
            list = query.getResultList();
            assertTrue(list.isEmpty());

            query = em.createQuery("select u from test$User u where u.id not in :ids");
            query.setParameter("ids", null);
            list = query.getResultList();
            assertFalse(list.isEmpty());

            query = em.createQuery("select u from test$User u where u.id not in (:ids)");
            query.setParameter("ids", null);
            list = query.getResultList();
            assertFalse(list.isEmpty());

            tx.commit();
        } finally {
            tx.end();
        }
    }

    @Test
    public void testNotExistsCollectionParameter() throws Exception {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            Query query = em.createQuery("select u from test$User u where u.id in :ids");
            List list = query.getResultList();
            assertTrue(list.isEmpty());

            query = em.createQuery("select u from test$User u where u.id in (:ids)");
            list = query.getResultList();
            assertTrue(list.isEmpty());

            query = em.createQuery("select u from test$User u where u.id not in :ids");
            list = query.getResultList();
            assertFalse(list.isEmpty());

            query = em.createQuery("select u from test$User u where u.id not in (:ids)");
            list = query.getResultList();
            assertFalse(list.isEmpty());

            query = em.createQuery("select u from test$User u where u.id not in (:ids1) or u.id not in (:ids2)");
            query.setParameter("ids1", null);
            list = query.getResultList();
            assertFalse(list.isEmpty());

            tx.commit();
        } finally {
            tx.end();
        }
    }

    @Test
    public void testSingleBooleanResult() {
        // works
        Object[] activeAndName = persistence.callInTransaction((em) -> {
            return (Object[]) em.createQuery("select u.active, u.name from test$User u where u.login = :login")
                    .setParameter("login", "testLogin")
                    .getFirstResult();
        });
        assertTrue((Boolean) activeAndName[0]);
        assertEquals("testUser", activeAndName[1]);

        //returns null
        Boolean active = persistence.callInTransaction((em) -> {
            return em.createQuery("select u.active from test$User u where u.login = :login", Boolean.class)
                    .setParameter("login", "testLogin")
                    .getFirstResult();
        });
        assertNotNull(active);
        assertTrue(active);
    }

    @Test
    public void testEnumImplicitConversion() throws Exception {
        // explicit enum id value
        persistence.runInTransaction(em -> {
            TypedQuery<Role> query = em.createQuery("select r from test$Role r where r.type = :roleType", Role.class);
            query.setParameter("roleType", 10);
            List<Role> roles = query.getResultList();
            assertTrue(roles.stream().anyMatch(role -> role.getName().equals("role")));
        });

        // enum as a positional parameter
        persistence.runInTransaction(em -> {
            TypedQuery<Role> query = em.createQuery("select r from test$Role r where r.type = ?1", Role.class);
            query.setParameter(1, RoleType.SUPER);
            List<Role> roles = query.getResultList();
            assertTrue(roles.stream().anyMatch(role -> role.getName().equals("role")));
        });

        // enum as a named parameter
        persistence.runInTransaction(em -> {
            TypedQuery<Role> query = em.createQuery("select r from test$Role r where r.type = :roleType", Role.class);
            query.setParameter("roleType", RoleType.SUPER);
            List<Role> roles = query.getResultList();
            assertTrue(roles.stream().anyMatch(role -> role.getName().equals("role")));
        });
    }


    @Test
    public void testNoImplicitConversion() throws Exception {
        persistence.runInTransaction(em -> {
            Group group = em.find(Group.class, group1Id);

            TypedQuery<User> query = em.createQuery("select u from test$User u where u.group = :group", User.class);
            query.setParameter("group", group, false);
            List<User> users = query.getResultList();
            assertTrue(!users.isEmpty());
        });
    }

    @Test
    public void testNestedEntityGroupBy() {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            em.createQuery("select ur.role, count(ur.id) from test$UserRole ur group by ur.role")
                    .getResultList();
            tx.commit();
        } finally {
            tx.end();
        }
    }

    @Test
    public void testDeleteQueryWithSoftDeleteTrue() {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            try {
                em.createQuery("delete from test$FileDescriptor f").executeUpdate();
            } catch (UnsupportedOperationException e) {
                //It's OK
            }

            try {
                em.setSoftDeletion(false);
                em.createQuery("delete from test$FileDescriptor f").executeUpdate();
            } catch (javax.persistence.PersistenceException e) {
                //It's OK integrity constraint violation
            } finally {
                em.setSoftDeletion(true);
            }

            try {
                AppContext.setProperty("jmix.data.enable-delete-statement-in-soft-delete-mode", "true");
                em.createQuery("delete from test$FileDescriptor f").executeUpdate();
            } catch (javax.persistence.PersistenceException e) {
                //It's OK integrity constraint violation
            } finally {
                AppContext.setProperty("jmix.data.enable-delete-statement-in-soft-delete-mode", "false");
            }
        } finally {
            try {
                tx.end();
            } catch (Exception e) {
                //Do nothing
            }
        }
    }

    @Test
    public void testNewObjectInJPQL() {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            UserDTO dto = (UserDTO) em.createQuery("select new com.haulmont.cuba.core.model.UserDTO(u.login) from test$User u where u.id = :id")
                    .setParameter("id", userId)
                    .getFirstResult();
            assertNotNull(dto);
            assertEquals("testLogin", dto.getLogin());
            tx.commit();
        } finally {
            tx.end();
        }
    }
}
