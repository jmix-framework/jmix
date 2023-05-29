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

import com.haulmont.cuba.core.model.common.User;
import io.jmix.core.EntityStates;
import io.jmix.core.Entity;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.listener.AfterCompleteTransactionListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.Collection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Component("cuba_TestAfterCommitTxListener")
public class TestAfterCompleteTxListener implements AfterCompleteTransactionListener {

    public static String test;

    @Autowired
    private Persistence persistence;
    @Autowired
    private EntityStates entityStates;

    @Override
    public void afterComplete(boolean committed, Collection<Entity> detachedEntities) {
        if (test != null) {
            switch (test) {
                case "testCommit":
                    testCommit(committed, detachedEntities);
                    break;
                case "testRollback":
                    testRollback(committed, detachedEntities);
                    break;
                case "accessName":
                    testAccessName(committed, detachedEntities);
                    break;
                case "accessGroup":
                    testAccessGroup(committed, detachedEntities);
                    break;
                case "accessUserRoles":
                    testAccessUserRoles(committed, detachedEntities);
                    break;
            }
        }
    }

    private void testCommit(boolean committed, Collection<Entity> detachedEntities) {
        assertTrue(committed);

        User user = null;
        for (Entity entity : detachedEntities) {
            assertTrue(entityStates.isDetached(entity));
            if (entity instanceof User && ((User) entity).getLogin().startsWith("TxLstnrTst-1-"))
                user = (User) entity;
        }

        if (user != null) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(persistence.getDataSource());
            jdbcTemplate.update("update TEST_USER set NAME = 'updated by TestAfterCompleteTxListener' where ID = ?", user.getId().toString());
        }
    }

    private void testRollback(boolean committed, Collection<Entity> detachedEntities) {
        assertFalse(committed);

        User user = null;
        for (Entity entity : detachedEntities) {
            if (entity instanceof User && ((User) entity).getLogin().startsWith("TxLstnrTst-1-"))
                user = (User) entity;
        }

        if (user != null) {
            assertEquals("updated by testRollback", user.getName());
            JdbcTemplate jdbcTemplate = new JdbcTemplate(persistence.getDataSource());
            jdbcTemplate.update("update TEST_USER set NAME = 'updated by TestAfterCompleteTxListener' where ID = ?", user.getId().toString());
        }
    }

    private void testAccessName(boolean committed, Collection<Entity> detachedEntities) {
        for (Entity entity : detachedEntities) {
            if (entity instanceof User) {
                try {
                    System.out.println("User name: " + ((User) entity).getName());
                } catch (IllegalStateException e) {
                    System.out.println("An exception has been thrown: " + e);
                }
            }
        }
    }

    private void testAccessGroup(boolean committed, Collection<Entity> detachedEntities) {
        for (Entity entity : detachedEntities) {
            if (entity instanceof User) {
                try {
                    System.out.println("User group: " + ((User) entity).getGroup());
                } catch (IllegalStateException e) {
                    System.out.println("An exception has been thrown: " + e);
                }
            }
        }
    }

    private void testAccessUserRoles(boolean committed, Collection<Entity> detachedEntities) {
        for (Entity entity : detachedEntities) {
            if (entity instanceof User) {
                try {
                    System.out.println("User roles size: " + ((User) entity).getUserRoles().size());
                } catch (IllegalStateException e) {
                    System.out.println("An exception has been thrown: " + e);
                }
            }
        }
    }
}
