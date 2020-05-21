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
import io.jmix.core.FetchPlan;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

import static com.haulmont.cuba.core.testsupport.TestSupport.reserialize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@CoreTest
public class UnfetchedAttributeTest {
    @Autowired
    private Persistence persistence;

    private Group group, group1;
    private User user;

    @BeforeEach
    public void setUp() throws Exception {

        persistence.runInTransaction(em -> {
            group = new Group();
            group.setName("Some group");
            em.persist(group);

            group1 = new Group();
            group1.setName("Some group 1");
            em.persist(group1);

            user = new User();
            user.setLogin("testUser");
            user.setName("testUser");
            user.setGroup(group);

            em.persist(user);
        });
    }

    @Test
    public void testGet() throws Exception {
        User loadedUser = null;

        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            Query q = em.createQuery("select u from test$User u where u.id = ?1");
            q.setView(
                    new FetchPlan(User.class, false)
                            .addProperty("login")
                            .addProperty("userRoles", new FetchPlan(UserRole.class)
                                    .addProperty("role", new FetchPlan(Role.class)
                                            .addProperty("name")))
            );
            q.setParameter(1, user.getId());
            List<User> list = q.getResultList();
            if (!list.isEmpty()) {
                loadedUser = list.get(0);
                // lazy fetch
                loadedUser.getGroup();
            }

            tx.commit();
        } finally {
            tx.end();
        }
        loadedUser = reserialize(loadedUser);
        assertNotNull(loadedUser);
        assertNotNull(loadedUser.getUserRoles());
        loadedUser.getUserRoles().size();
        assertNotNull(loadedUser.getGroup());
    }

    @Test
    public void testSet() throws Exception {
        User loadedUser = null;

        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            Query q = em.createQuery("select u from test$User u where u.id = ?1");
            q.setView(
                    new FetchPlan(User.class, false)
                            .addProperty("login")
                            .addProperty("userRoles", new FetchPlan(UserRole.class)
                                    .addProperty("role", new FetchPlan(Role.class)
                                            .addProperty("name")))
            );
            q.setParameter(1, user.getId());
            List<User> list = q.getResultList();
            if (!list.isEmpty()) {
                loadedUser = list.get(0);
                // set value to not present in view
                loadedUser.setGroup(group1);
            }

            tx.commit();
        } finally {
            tx.end();
        }
        loadedUser = reserialize(loadedUser);
        assertNotNull(loadedUser);
        assertNotNull(loadedUser.getUserRoles());
        loadedUser.getUserRoles().size();
        assertNotNull(loadedUser.getGroup());
        assertEquals(group1, loadedUser.getGroup());
    }

    @AfterEach
    public void tearDown() {
        TestSupport.deleteRecord(user, group, group1);
    }
}
