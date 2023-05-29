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
import io.jmix.core.EntityStates;
import io.jmix.core.Metadata;
import io.jmix.core.Entity;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.TypedQuery;
import com.haulmont.cuba.core.listener.BeforeCommitTransactionListener;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;
import javax.persistence.FlushModeType;
import java.util.Collection;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Component("cuba_TestBeforeCommitTxListener")
public class TestBeforeCommitTxListener implements BeforeCommitTransactionListener {

    public static String test;
    public static UUID createdEntityId;

    @Autowired
    private Metadata metadata;

    @Autowired
    private Persistence persistence;

    @Autowired
    private EntityStates entityStates;

    @Override
    public void beforeCommit(EntityManager entityManager, Collection<Entity> managedEntities) {
        if (test != null) {
            System.out.println("beforeCommit: managedEntities=" + managedEntities);
            switch (test) {
                case "testChangeEntity":
                    changeEntity(managedEntities);
                    break;
                case "testCreateEntity":
                    createEntity(entityManager);
                    break;
                case "testQueryWithFlush":
                    queryWithFlush(managedEntities, entityManager);
                    break;
                case "testCreateEntityInNewTransaction":
                    createEntityInNewTransaction();
                    break;
                case "testCreateEntityInNewTransactionAndRollback":
                    createEntityInNewTransactionAndRollback();
                    break;
                case "testCreateEntityInSameTransaction":
                    createEntityInSameTransaction();
                    break;
                case "testCreateEntityInSameTransactionAndRollback":
                    createEntityInSameTransactionAndRollback();
                    break;
            }
        }
    }

    private void createEntityInNewTransaction() {
        test = null;
        try (Transaction tx = persistence.createTransaction()) {
            createEntity(persistence.getEntityManager());
            System.out.println("Created in new transaction: " + createdEntityId);
            tx.commit();
        }
    }

    private void createEntityInSameTransaction() {
        test = null;
        try (Transaction tx = persistence.getTransaction()) {
            createEntity(persistence.getEntityManager());
            System.out.println("Created in same transaction: " + createdEntityId);
            tx.commit();
        }
    }

    private void createEntityInNewTransactionAndRollback() {
        test = null;
        try (Transaction tx = persistence.createTransaction()) {
            createEntity(persistence.getEntityManager());
            System.out.println("Created in new transaction: " + createdEntityId);
            tx.commit();
        }
        throw new RuntimeException("some error");
    }

    private void createEntityInSameTransactionAndRollback() {
        test = null;
        try (Transaction tx = persistence.getTransaction()) {
            createEntity(persistence.getEntityManager());
            System.out.println("Created in same transaction: " + createdEntityId);
            tx.commit();
        }
        throw new RuntimeException("some error");
    }

    private void changeEntity(Collection<Entity> managedEntities) {
        for (Object entity : managedEntities) {
            if (entity instanceof User && ((User) entity).getLogin().startsWith("TxLstnrTst-")) {
                User user = (User) entity;
                if (entityStates.isNew(user)) {
                    assertEquals(user.getLogin().toLowerCase(), user.getLoginLowerCase()); // user listener has worked
                    user.setName("set by tx listener");
                }
            }
        }
    }

    private void createEntity(EntityManager entityManager) {
        Group companyGroup = (Group) entityManager.createQuery("select g from test$Group g where g.name ='Company'")
                .getFirstResult();
        User u = metadata.create(User.class);
        createdEntityId = u.getId();
        u.setLogin("TxLstnrTst-" + u.getId());
        u.setLoginLowerCase(u.getLogin().toLowerCase());
        u.setGroup(companyGroup);
        entityManager.persist(u);
    }

    private void queryWithFlush(Collection<Entity> managedEntities, EntityManager entityManager) {
        if (!managedEntities.stream().anyMatch(e -> e instanceof User && ((User) e).getLogin().startsWith("TxLstnrTst-")))
            return;

        TypedQuery<User> query = entityManager.createQuery("select u from test$User u where u.login like ?1", User.class);
        query.setParameter(1, "TxLstnrTst-2-%");
        query.setFlushMode(FlushModeType.AUTO);
        User result = query.getFirstResult();
        assertNotNull(result);
    }
}
