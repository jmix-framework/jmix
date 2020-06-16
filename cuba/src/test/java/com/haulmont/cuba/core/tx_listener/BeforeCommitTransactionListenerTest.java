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

package com.haulmont.cuba.core.tx_listener;

import com.haulmont.cuba.core.model.common.Group;
import com.haulmont.cuba.core.model.common.User;
import com.haulmont.cuba.core.testsupport.CoreTest;
import com.haulmont.cuba.core.testsupport.TestSupport;
import io.jmix.core.Metadata;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@CoreTest
public class BeforeCommitTransactionListenerTest {
    @Autowired
    private Persistence persistence;
    @Autowired
    private Metadata metadata;
    @Autowired
    private TestSupport testSupport;

    private Group companyGroup;

    @BeforeEach
    public void setUp() throws Exception {
        try (Transaction tx = persistence.createTransaction()) {
            companyGroup = new Group();
            companyGroup.setName("Company");
            persistence.getEntityManager().persist(companyGroup);
            tx.commit();
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(persistence.getDataSource());
        jdbcTemplate.update("delete from TEST_USER");
        testSupport.deleteRecord(companyGroup);
    }

    @Test
    public void testChangeEntity() throws Exception {
        User u1 = metadata.create(User.class);
        u1.setLogin("TxLstnrTst-1-" + u1.getId());
        u1.setGroup(companyGroup);

        TestBeforeCommitTxListener.test = "testChangeEntity";
        try (Transaction tx = persistence.createTransaction()) {
            persistence.getEntityManager().persist(u1);
            tx.commit();
        } finally {
            TestBeforeCommitTxListener.test = null;
        }

        try (Transaction tx = persistence.createTransaction()) {
            User user = persistence.getEntityManager().find(User.class, u1.getId());
            assertEquals("set by tx listener", user.getName());
        }
    }

    @Test
    public void testCreateEntity() throws Exception {
        User u = metadata.create(User.class);
        u.setLogin("u-" + u.getId());
        u.setGroup(companyGroup);

        TestBeforeCommitTxListener.test = "testCreateEntity";
        try (Transaction tx = persistence.createTransaction()) {
            persistence.getEntityManager().persist(u);
            tx.commit();
        } finally {
            TestBeforeCommitTxListener.test = null;
        }

        try (Transaction tx = persistence.createTransaction()) {
            User user = persistence.getEntityManager().find(User.class, TestBeforeCommitTxListener.createdEntityId);
            assertNotNull(user);
        }
    }

    @Test
    public void testQueryWithFlush() throws Exception {
        User u1 = metadata.create(User.class);
        u1.setLogin("TxLstnrTst-2-" + u1.getId());
        u1.setGroup(companyGroup);

        TestBeforeCommitTxListener.test = "testQueryWithFlush";
        try (Transaction tx = persistence.createTransaction()) {
            persistence.getEntityManager().persist(u1);
            tx.commit();
        } finally {
            TestBeforeCommitTxListener.test = null;
        }

        try (Transaction tx = persistence.createTransaction()) {
            User user = persistence.getEntityManager().find(User.class, u1.getId());
            assertEquals(u1, user);
        }
    }
}
