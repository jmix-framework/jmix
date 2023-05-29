/*
 * Copyright (c) 2008-2019 Haulmont.
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

import com.google.common.base.Strings;
import com.haulmont.cuba.core.entity.contracts.Id;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.core.model.sales.Product;
import com.haulmont.cuba.core.testsupport.CoreTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;

@CoreTest
public class TransactionalActionUsageTest {

    private static String messageFromOnFailAction;
    private static String messageFromAfterCommitAction;

    @Autowired
    private TransactionalActionFactory transactionalActionFactory;
    @Autowired
    private TransactionalDataManager transactionalDataManager;
    @Autowired
    private Metadata metadata;
    @Autowired
    private Persistence persistence;

    @BeforeEach
    public void setUp() throws Exception {
        try (Transaction tx = persistence.createTransaction()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(persistence.getDataSource());
            jdbcTemplate.update("delete from TEST_ORDER_LINE");
            jdbcTemplate.update("delete from TEST_PRODUCT");
        }
    }

    @Test
    public void transactionalActionsAreDifferent() {
        assertNotEquals(transactionalActionFactory.getTransactionalAction(), transactionalActionFactory.getTransactionalAction());
    }

    @Test
    public void testOnSuccessAction() {
        EntitySet entities = transactionalActionFactory
                .getTransactionalAction()
                .withCommitContext(() -> {
                    CommitContext cc = new CommitContext();
                    Product p = metadata.create(Product.class);
                    p.setName("test");
                    p.setQuantity(100);
                    cc.addInstanceToCommit(p);
                    return cc;
                })
                .onSuccess(es -> {
                    es.stream()
                            .filter(e -> Product.class.equals(metadata.getClassNN(e.getClass()).getJavaClass()))
                            .forEach(e -> ((Product) e).setName("newName"));
                })
                .perform();

        Product product = (Product) entities.stream()
                .filter(e -> Product.class.equals(metadata.getClassNN(e.getClass()).getJavaClass()))
                .findFirst().orElse(null);

        assertNotNull(product);
        assertEquals("newName", product.getName());

        Product productFromDb = transactionalDataManager.load(Id.of(product)).one();

        assertNotNull(productFromDb);
        assertEquals("test", productFromDb.getName());
    }

    @Test
    public void testOnFailAction() {
        EntitySet entities = transactionalActionFactory
                .getTransactionalAction()
                .withCommitContext(() -> {
                    CommitContext cc = new CommitContext();
                    Product p = metadata.create(Product.class);
                    p.setName(Strings.repeat("a", 300)); // to cause truncation error in DB
                    p.setQuantity(100);
                    cc.addInstanceToCommit(p);
                    return cc;
                })
                .onFail((cc, t) -> {
                    messageFromOnFailAction = "[testOnFailAction] commit failed";
                })
                .perform();

        assertNull(entities);
        assertEquals("[testOnFailAction] commit failed", messageFromOnFailAction);
    }

    @Test
    public void testAfterCommitAction() {
        TransactionalAction transactionalAction = transactionalActionFactory
                .getTransactionalAction()
                .withCommitContext(() -> {
                    CommitContext cc = new CommitContext();
                    Product p = metadata.create(Product.class);
                    p.setName(Strings.repeat("a", 300)); // to cause truncation error in DB
                    p.setQuantity(100);
                    cc.addInstanceToCommit(p);
                    return cc;
                })
                .afterCompletion(cc -> {
                    messageFromAfterCommitAction = "[testAfterCommitAction] transaction ended";
                });

        Throwable t = null;
        try {
            transactionalAction.perform();
        } catch (Throwable throwable) {
            t = throwable;
        }

        assertNotNull(t);
        assertEquals("[testAfterCommitAction] transaction ended", messageFromAfterCommitAction);
    }

    @Test
    public void testAllActions() {
        EntitySet entities = transactionalDataManager
                .commitAction(() -> {
                    CommitContext cc = new CommitContext();
                    Product p = transactionalDataManager.create(Product.class);
                    p.setName("allActionsTest");
                    p.setQuantity(100);
                    cc.addInstanceToCommit(p);
                    return cc;
                })
                .onSuccess(es -> {
                    es.stream()
                            .filter(e -> Product.class.equals(metadata.getClassNN(e.getClass()).getJavaClass()))
                            .forEach(e -> ((Product) e).setName("onSuccessName"));
                })
                .onFail((cc, t) -> {
                    messageFromOnFailAction = "[testAllActions] commit failed";
                })
                .afterCompletion(cc -> {
                    messageFromAfterCommitAction = "[testAllActions] transaction ended";
                })
                .perform();

        Product product = (Product) entities.stream()
                .filter(e -> Product.class.equals(metadata.getClassNN(e.getClass()).getJavaClass()))
                .findFirst().orElse(null);

        assertNotNull(product);
        assertEquals("onSuccessName", product.getName());

        Product productFromDb = transactionalDataManager.load(Id.of(product)).one();

        assertNotNull(productFromDb);
        assertEquals("allActionsTest", productFromDb.getName());

        assertNotEquals("[testAllActions] commit failed", messageFromOnFailAction);

        assertEquals("[testAllActions] transaction ended", messageFromAfterCommitAction);
    }
}
