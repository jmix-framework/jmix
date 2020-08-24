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
import com.haulmont.cuba.core.model.common.Constraint;
import com.haulmont.cuba.core.model.common.Group;
import com.haulmont.cuba.core.model.common.User;
import com.haulmont.cuba.core.testsupport.CoreTest;
import com.haulmont.cuba.core.testsupport.TestSupport;
import io.jmix.core.FetchMode;
import io.jmix.core.FetchPlan;
import io.jmix.core.Metadata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@CoreTest
public class EclipseLinkQueriesTest {
    @Autowired
    private Persistence persistence;
    @Autowired
    private Metadata metadata;
    @Autowired
    private TestSupport testSupport;

    private User user1;
    private User user2;
    private Group rootGroup;
    private Group group;

    @BeforeEach
    public void setUp() throws Exception {
        persistence.createTransaction().execute(em -> {
            rootGroup = new Group();
            rootGroup.setName("rootGroup");
            em.persist(rootGroup);

            user1 = metadata.create(User.class);
            user1.setName("testUser");
            user1.setLogin("testLogin");
            user1.setGroup(rootGroup);
            em.persist(user1);

            group = metadata.create(Group.class);
            group.setParent(rootGroup);
            group.setName("testGroup" + group.getId());
            em.persist(group);

            user2 = metadata.create(User.class);
            user2.setName("testUser2");
            user2.setLogin("testLogin2");
            user2.setGroup(rootGroup);
            em.persist(user2);

            return null;
        });
    }

    @AfterEach
    public void tearDown() throws Exception {
        testSupport.deleteRecord("TEST_GROUP_HIERARCHY", "GROUP_ID", group.getId());
        testSupport.deleteRecord(user1, user2, group, rootGroup);
    }

    // cross join, view has ToMany reference
    @Test
    public void testCrossJoinWithToManyView() throws Exception {
        List<Group> result;
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            FetchPlan view = new View(Group.class).addProperty("constraints");
            TypedQuery<Group> query = em.createQuery("select g from test$Group g, test$User u where u.group = g", Group.class);
            query.setView(view);
            result = query.getResultList();
            tx.commit();
        }
        for (Group group : result) {
            group = testSupport.reserialize(group);
            group.getConstraints().size();
        }
    }

    // cross join, view with the reference to the parent entity
    @Test
    public void testCrossJoinViewParentReference() throws Exception {
        List<Group> result;
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            FetchPlan view = new View(Group.class).addProperty("parent");
            TypedQuery<Group> query = em.createQuery("select g from test$Group g, test$User u where u.group = g", Group.class);
            query.setView(view);
            result = query.getResultList();
            tx.commit();
        }
        for (Group g : result) {
            g = testSupport.reserialize(g);
            if (g.equals(rootGroup))
                assertNull(g.getParent());
            else if (g.equals(group))
                assertEquals(rootGroup, g.getParent());
        }
    }

    // join on, view contains ToMany attribute
    @Test
    public void testJoinOnWithToManyView() throws Exception {
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            FetchPlan view = new View(Group.class).addProperty("constraints");
            TypedQuery<Group> query = em.createQuery("select g from test$Group g join test$QueryResult qr on qr.entityId = g.id where qr.queryKey = 1", Group.class);
            query.setView(view);
            List<Group> result = query.getResultList();
            tx.commit();
        }
    }

    // join on, view contains parent attribute
    @Test
    public void testJoinOnWithParentReference() throws Exception {
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            FetchPlan view = new View(Group.class).addProperty("parent");
            TypedQuery<Group> query = em.createQuery("select g from test$Group g join test$QueryResult qr on qr.entityId = g.id where qr.queryKey = 1", Group.class);
            query.setView(view);
            List<Group> result = query.getResultList();
            tx.commit();
        }
    }

    // join on, view contains ToMany attribute, fetch = JOIN
    @Test
    public void testJoinOnWithToManyView2() throws Exception {
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            FetchPlan view = new View(Group.class).addProperty("constraints", new View(Constraint.class, FetchPlan.LOCAL), FetchMode.JOIN);
            TypedQuery<Group> query = em.createQuery("select g from test$Group g join test$QueryResult qr on qr.entityId = g.id where qr.queryKey = 1", Group.class);
            query.setView(view);
            List<Group> result = query.getResultList();
            tx.commit();
        }
    }

    @Test
    public void testSeveralEntriesInSelectClause() {
        Object resultList = persistence.createTransaction().execute((em) -> {
            return em.createQuery("select u.group, u.login from test$User u where u.name like :mask")
                    .setParameter("mask", "%ser")
                    .getResultList();
        });
        List<Object[]> list = (List<Object[]>) resultList;
        Object[] row = list.get(0);

        assertEquals(rootGroup.getId(), ((Group) row[0]).getId());
        assertEquals("testLogin", row[1]);
    }
}
