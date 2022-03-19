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

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.CommitContext;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.model.common.Group;
import com.haulmont.cuba.core.model.common.User;
import com.haulmont.cuba.core.testsupport.CoreTest;
import com.haulmont.cuba.core.testsupport.TestSupport;
import io.jmix.core.Metadata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@CoreTest
public class SoftDeleteDataManagerTest {
    @Autowired
    private Persistence persistence;
    @Autowired
    private Metadata metadata;
    @Autowired
    private TestSupport testSupport;

    private User user;
    private Group group;

    @BeforeEach
    public void setUp() throws Exception {
        try (Transaction tx = persistence.createTransaction()) {
            group = metadata.create(Group.class);
            group.setName("group");
            persistence.getEntityManager().persist(group);

            user = metadata.create(User.class);
            user.setGroup(group);
            user.setLogin("user-" + user.getId());
            persistence.getEntityManager().persist(user);
            tx.commit();
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        testSupport.deleteRecord(user, group);
    }

    @Test
    public void testHardDelete() throws Exception {
        DataManager dataManager = AppBeans.get(DataManager.class);
        User loadedUser = dataManager.load(LoadContext.create(User.class).setId(user.getId()));
        CommitContext commitContext = new CommitContext().addInstanceToRemove(loadedUser);
        commitContext.setSoftDeletion(false);
        dataManager.commit(commitContext);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(persistence.getDataSource());
        Long count = jdbcTemplate.queryForObject("select count(*) from test_user where id = ?", Long.class, user.getId().toString());
        assertEquals(0, count);
    }
}
