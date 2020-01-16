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
import com.haulmont.cuba.core.testsupport.TestContainer;
import io.jmix.core.Metadata;
import io.jmix.core.commons.db.QueryRunner;
import io.jmix.data.Persistence;
import io.jmix.data.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;

@CoreTest
public class AfterCompleteTransactionListenerTest {

    public static TestContainer cont = TestContainer.Common.INSTANCE;

    @Inject
    private Persistence persistence;
    @Inject
    private Metadata metadata;
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
        QueryRunner runner = new QueryRunner(persistence.getDataSource());
        runner.update("delete from TEST_USER");
        cont.deleteRecord(companyGroup);
    }

    @Test
    public void testCommit() throws Exception {
        User u = metadata.create(User.class);
        u.setLogin("TxLstnrTst-1-" + u.getId());
        u.setGroup(companyGroup);
        TestAfterCompleteTxListener.test = "testCommit";
        try {
            try (Transaction tx = persistence.createTransaction()) {
                persistence.getEntityManager().persist(u);
                tx.commit();
            }
        } finally {
            TestAfterCompleteTxListener.test = null;
        }

        try (Transaction tx = persistence.createTransaction()) {
            User user = persistence.getEntityManager().find(User.class, u.getId());
            assertEquals("updated by TestAfterCompleteTxListener", user.getName());
        }
    }

    @Test
    public void testRollback() throws Exception {
        User u = metadata.create(User.class);
        u.setLogin("TxLstnrTst-1-" + u.getId());
        u.setGroup(companyGroup);
        try (Transaction tx = persistence.createTransaction()) {
            persistence.getEntityManager().persist(u);
            tx.commit();
        }

        TestAfterCompleteTxListener.test = "testRollback";
        try {
            try (Transaction tx = persistence.createTransaction()) {
                User user = persistence.getEntityManager().find(User.class, u.getId());
                user.setName("updated by testRollback");
            }
        } finally {
            TestAfterCompleteTxListener.test = null;
        }

        try (Transaction tx = persistence.createTransaction()) {
            User user = persistence.getEntityManager().find(User.class, u.getId());
            assertEquals("updated by TestAfterCompleteTxListener", user.getName());
        }
    }
}
