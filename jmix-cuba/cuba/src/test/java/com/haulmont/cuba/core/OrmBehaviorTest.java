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
import com.haulmont.cuba.core.model.common.Group;
import com.haulmont.cuba.core.model.common.User;
import com.haulmont.cuba.core.testsupport.CoreTest;
import com.haulmont.cuba.core.testsupport.TestSupport;
import io.jmix.core.Metadata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@CoreTest
public class OrmBehaviorTest {
    @Autowired
    private Persistence persistence;
    @Autowired
    private Metadata metadata;
    @Autowired
    private TestSupport testSupport;

    private UUID userId, groupId;

    private static final Logger log = LoggerFactory.getLogger(OrmBehaviorTest.class);

    @AfterEach
    public void tearDown() throws Exception {
        testSupport.deleteRecord("TEST_USER", userId);
        testSupport.deleteRecord("TEST_GROUP", groupId);
    }

    /*
     * Test that persist with un-managed attribute works (it didn't work in OpenJPA 2.2+ and worked in OpenJPA pre-2.2)
     */
    @Test
    public void testPersistWithUnManagedAttribute() throws Exception {
        Group group = new Group();
        groupId = group.getId();
        group.setName("Old Name");
        Transaction tx = persistence.createTransaction();
        try {
            persistence.getEntityManager().persist(group);
            tx.commit();
        } finally {
            tx.end();
        }

        // Let's imagine that this entity was loaded with MyBatis
        Group g = new Group();
        g.setId(groupId);
        g.setName("Old Name");

        User user = new User();
        userId = user.getId();
        user.setLogin("typednativesqlquery");
        user.setGroup(g);
        user.setName("Test");

        tx = persistence.createTransaction();
        try {
            persistence.getEntityManager().persist(user);
            tx.commitRetaining();

            user = persistence.getEntityManager().find(User.class, userId,
                    new View(User.class).addProperty("group"));
            tx.commit();
        } finally {
            tx.end();
        }

        user = testSupport.reserialize(user);
        assertEquals(groupId, user.getGroup().getId());
    }
}
