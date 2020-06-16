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
import com.haulmont.cuba.core.model.common.User;
import com.haulmont.cuba.core.testsupport.CoreTest;
import com.haulmont.cuba.core.testsupport.TestSupport;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import io.jmix.core.FetchPlan;
import io.jmix.core.Entity;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.beans.factory.annotation.Autowired;
import java.sql.SQLException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@CoreTest
@Disabled
public class QueryResultTest {
    @Autowired
    private Persistence persistence;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private TestSupport testSupport;

    private List<UUID> userIds = new ArrayList<>();
    private Group group;

    @BeforeEach
    public void setUp() throws Exception {
        createEntities();
    }

    @AfterEach
    public void tearDown() throws Exception {
        for (UUID userId : userIds) {
            testSupport.deleteRecord("SEC_USER", userId);
        }
        testSupport.deleteRecord(group);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(persistence.getDataSource());
        jdbcTemplate.update("delete from SYS_QUERY_RESULT");
    }

    private void createEntities() {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            User user;

            group = new Group();
            group.setName("group");
            em.persist(group);

            int k = 0;
            for (String domain : Arrays.asList("@aaa.com", "@bbb.com")) {
                for (String name : Arrays.asList("A-", "B-")) {
                    for (String firstName : Arrays.asList("C-", "D-")) {
                        for (int i = 0; i < 5; i++) {
                            user = new User();
                            user.setGroup(group);

                            userIds.add(user.getId());
                            user.setLogin("user" + StringUtils.leftPad(String.valueOf(k++), 2, '0'));
                            user.setName(name + "User" + i);
                            user.setFirstName(firstName + "User" + i);
                            user.setEmail(user.getLogin() + domain);

                            em.persist(user);
                        }
                    }
                }
            }

            tx.commit();
        } finally {
            tx.end();
        }
    }


    @Test
    public void testFirstQuery() throws SQLException {
        LoadContext<?> context = new LoadContext<>(User.class).setFetchPlan(FetchPlan.LOCAL);
        context.setQueryString("select u from test$User u where u.name like :name").setParameter("name", "A-%");
        List entities = dataManager.loadList(context);
        assertEquals(20, entities.size());

        List<Map<String, Object>> queryResults = getQueryResults();
        assertEquals(0, queryResults.size());
    }

    @Test
    public void testSecondQuery() throws SQLException {
        LoadContext<?> context = new LoadContext<>(User.class).setFetchPlan(FetchPlan.LOCAL);
        context.setQueryString("select u from test$User u where u.email like :email").setParameter("email", "%aaa.com");

        LoadContext.Query prevQuery = new LoadContext.Query("select u from test$User u where u.name like :name")
                .setParameter("name", "A-%");
        context.getPrevQueries().add(prevQuery);
        context.setQueryKey(111);

        List<? extends Entity> entities = dataManager.loadList(context);
        assertEquals(10, entities.size());

        List<Map<String, Object>> queryResults = getQueryResults();
        assertEquals(20, queryResults.size());
    }

    @Test
    public void testThirdQuery() throws SQLException {
        LoadContext context;
        List<Entity> entities;

        context = new LoadContext(User.class).setFetchPlan(FetchPlan.LOCAL);
        LoadContext.Query query1 = context.setQueryString("select u from test$User u where u.email like :email")
                .setParameter("email", "%aaa.com");
        entities = dataManager.loadList(context);
        assertEquals(20, entities.size());

        context = new LoadContext(User.class).setFetchPlan(FetchPlan.LOCAL);
        LoadContext.Query query2 = context.setQueryString("select u from test$User u where u.name like :name")
                .setParameter("name", "A-%");
        context.getPrevQueries().add(query1);
        context.setQueryKey(111);

        entities = dataManager.loadList(context);
        assertEquals(10, entities.size());

        context = new LoadContext(User.class).setFetchPlan(FetchPlan.LOCAL);
        context.setQueryString("select u from test$User u where u.firstName like :firstName")
                .setParameter("firstName", "C-%");
        context.getPrevQueries().add(query1);
        context.getPrevQueries().add(query2);
        context.setQueryKey(111);

        entities = dataManager.loadList(context);
        assertEquals(5, entities.size());
    }

    private List<Map<String, Object>> getQueryResults() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(persistence.getDataSource());
        return jdbcTemplate.queryForList("select * from SYS_QUERY_RESULT");
    }
}
