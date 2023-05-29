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

import com.google.common.collect.Iterables;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.model.common.Group;
import com.haulmont.cuba.core.model.common.Role;
import com.haulmont.cuba.core.model.common.User;
import com.haulmont.cuba.core.model.common.UserRole;
import com.haulmont.cuba.core.testsupport.CoreTest;
import io.jmix.data.DataConfigPropertiesAccess;
import io.jmix.data.DataProperties;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@CoreTest
public class DataManagerDistinctResultsTest {
    public static final int QTY = 17;

    public static final String QUERY =
            "select u from test$User u left join u.userRoles r where u.group.id = :groupId order by u.loginLowerCase";

    public static final String DISTINCT_QUERY =
            "select distinct u from test$User u left join u.userRoles r where u.group.id = :groupId order by u.loginLowerCase";

    @Autowired
    private Persistence persistence;

    @Autowired
    private DataProperties dataProperties;

    private UUID groupId;
    private UUID role1Id;
    private UUID role2Id;

    @BeforeEach
    public void setUp() throws Exception {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            Group group = new Group();
            groupId = group.getId();
            group.setName("testGroup");
            em.persist(group);

            Role role1 = new Role();
            role1Id = role1.getId();
            role1.setName("role1");
            em.persist(role1);

            Role role2 = new Role();
            role2Id = role2.getId();
            role2.setName("role2");
            em.persist(role2);

            for (int i = 0; i < QTY; i++) {
                User user = new User();
                user.setName("user" + StringUtils.leftPad(String.valueOf(i), 2, '0'));
                user.setLogin(user.getName());
                user.setGroup(group);
                em.persist(user);

                UserRole userRole1 = new UserRole();
                userRole1.setUser(user);
                userRole1.setRole(role1);
                em.persist(userRole1);

                UserRole userRole2 = new UserRole();
                userRole2.setUser(user);
                userRole2.setRole(role2);
                em.persist(userRole2);
            }

            tx.commit();
        } finally {
            tx.end();
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        Transaction tx = persistence.createTransaction();
        try {
            JdbcTemplate runner = new JdbcTemplate(persistence.getDataSource());

            String sql = "delete from TEST_USER_ROLE where ROLE_ID = '" + role1Id.toString() + "'";
            runner.update(sql);

            sql = "delete from TEST_USER_ROLE where ROLE_ID = '" + role2Id.toString() + "'";
            runner.update(sql);

            sql = "delete from TEST_ROLE where ID = '" + role1Id.toString() + "'";
            runner.update(sql);

            sql = "delete from TEST_ROLE where ID = '" + role2Id.toString() + "'";
            runner.update(sql);

            sql = "delete from TEST_USER where GROUP_ID = '" + groupId.toString() + "'";
            runner.update(sql);

            sql = "delete from TEST_GROUP where ID = '" + groupId.toString() + "'";
            runner.update(sql);

            tx.commit();
        } finally {
            tx.end();
        }
    }

    @Test

    public void testDistinctResults() {
        checkSetup();

        LinkedHashSet<User> set;

        DataConfigPropertiesAccess.setInMemoryDistinct(dataProperties, false);

        set = load(0, 10, QUERY);
        assertEquals(5, set.size());
        assertEquals("user00", Iterables.getFirst(set, null).getLoginLowerCase());
        assertEquals("user04", Iterables.getLast(set, null).getLoginLowerCase());

        set = load(0, 10, DISTINCT_QUERY);
        assertEquals(10, set.size());
        assertEquals("user00", Iterables.getFirst(set, null).getLoginLowerCase());
        assertEquals("user09", Iterables.getLast(set, null).getLoginLowerCase());

        DataConfigPropertiesAccess.setInMemoryDistinct(dataProperties, true);

        set = load(0, 10, QUERY);
        assertEquals(5, set.size());
        assertEquals("user00", Iterables.getFirst(set, null).getLoginLowerCase());
        assertEquals("user04", Iterables.getLast(set, null).getLoginLowerCase());

        set = load(0, 10, DISTINCT_QUERY);
        assertEquals(10, set.size());
        assertEquals("user00", Iterables.getFirst(set, null).getLoginLowerCase());
        assertEquals("user09", Iterables.getLast(set, null).getLoginLowerCase());

        set = load(0, 20, DISTINCT_QUERY);
        assertEquals(17, set.size());
        assertEquals("user00", Iterables.getFirst(set, null).getLoginLowerCase());
        assertEquals("user16", Iterables.getLast(set, null).getLoginLowerCase());

        set = load(0, 17, DISTINCT_QUERY);
        assertEquals(17, set.size());
        assertEquals("user00", Iterables.getFirst(set, null).getLoginLowerCase());
        assertEquals("user16", Iterables.getLast(set, null).getLoginLowerCase());

        set = load(5, 5, DISTINCT_QUERY);
        assertEquals(5, set.size());
        assertEquals("user05", Iterables.getFirst(set, null).getLoginLowerCase());
        assertEquals("user09", Iterables.getLast(set, null).getLoginLowerCase());

        set = load(10, 5, DISTINCT_QUERY);
        assertEquals(5, set.size());
        assertEquals("user10", Iterables.getFirst(set, null).getLoginLowerCase());
        assertEquals("user14", Iterables.getLast(set, null).getLoginLowerCase());

        set = load(15, 5, DISTINCT_QUERY);
        assertEquals(2, set.size());
        assertEquals("user15", Iterables.getFirst(set, null).getLoginLowerCase());
        assertEquals("user16", Iterables.getLast(set, null).getLoginLowerCase());

    }

    private LinkedHashSet<User> load(int firstResult, int maxResults, String queryString) {
        DataManager ds = AppBeans.get(DataManager.NAME);
        LoadContext<User> lc = new LoadContext<>(User.class);
        LoadContext.Query q = lc.setQueryString(queryString);
        q.setParameter("groupId", groupId);
        q.setFirstResult(firstResult);
        q.setMaxResults(maxResults);
        List<User> list = ds.loadList(lc);
        return new LinkedHashSet<>(list);
    }

    private void checkSetup() {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            Query query = em.createQuery("select u from test$User u left join u.userRoles r where u.group.id = ?1");
            query.setParameter(1, groupId);
            List list = query.getResultList();
            tx.commit();
            assertEquals(QTY * 2, list.size());
        } finally {
            tx.end();
        }
    }
}
