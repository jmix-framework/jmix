/*
 * Copyright (c) 2008-2017 Haulmont.
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

import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.core.model.common.*;
import com.haulmont.cuba.core.testsupport.CoreTest;
import com.haulmont.cuba.core.testsupport.TestSupport;
import com.haulmont.cuba.security.entity.ConstraintOperationType;
import io.jmix.core.EntityStates;
import io.jmix.core.FetchPlan;
import io.jmix.core.Metadata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PL-9325 For new entity, DataManager.commit() does not fetch attributes of related entity by supplied view
 */
@CoreTest
public class DataManagerCommit2Test {
    @Autowired
    private Persistence persistence;
    @Autowired
    private Metadata metadata;
    @Autowired
    private EntityStates entityStates;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private TestSupport testSupport;

    private UUID userId;
    private UUID userRoleId;
    private Group group1, group2;
    private Role role;
    private Constraint constraint;
    private FetchPlan view;

    @BeforeEach
    public void setUp() throws Exception {
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();

            group1 = metadata.create(Group.class);
            group1.setName("Group1-" + group1.getId());
            em.persist(group1);

            role = metadata.create(Role.class);
            role.setName("role1");
            em.persist(role);

            group2 = metadata.create(Group.class);
            group2.setName("Group2-" + group2.getId());
            em.persist(group2);

            constraint = metadata.create(Constraint.class);
            constraint.setCheckType(ConstraintCheckType.MEMORY);
            constraint.setOperationType(ConstraintOperationType.READ);
            constraint.setEntityName("sec$User");
            constraint.setGroup(group2);
            em.persist(constraint);

            User user = metadata.create(User.class);
            userId = user.getId();
            user.setName("testUser");
            user.setLogin("login" + userId);
            user.setPassword("000");
            user.setGroup(group1);
            em.persist(user);

            UserRole userRole = new UserRole();
            userRoleId = userRole.getId();
            userRole.setRole(role);
            userRole.setUser(user);
            em.persist(userRole);

            tx.commit();
        }

        view = new View(User.class, true)
                .addProperty("login")
                .addProperty("loginLowerCase")
                .addProperty("name")
                .addProperty("password")
                .addProperty("group", new View(Group.class).addProperty("name"))
                .addProperty("userRoles", new View(UserRole.class));
    }

    @AfterEach
    public void tearDown() throws Exception {
        testSupport.deleteRecord("TEST_USER_ROLE", userRoleId);
        testSupport.deleteRecord("TEST_USER", userId);
        testSupport.deleteRecord(role);
        testSupport.deleteRecord(constraint);
        testSupport.deleteRecord(group1, group2);
    }

    @Test
    public void testViewAfterCommitNew() throws Exception {
        Group group = dataManager.load(LoadContext.create(Group.class).setId(group1.getId()).setFetchPlan(FetchPlan.INSTANCE_NAME));
        assertFalse(entityStates.isLoaded(group, "createTs"));

        User user = metadata.create(User.class);
        try {
            user.setName("testUser");
            user.setLogin("login" + user.getId());
            user.setGroup(group);

            FetchPlan userView = new View(User.class, true)
                    .addProperty("login")
                    .addProperty("name")
                    .addProperty("group", new View(Group.class, false)
                            .addProperty("name")
                            .addProperty("createTs"));

            User committedUser = dataManager.commit(user, userView);
            assertTrue(entityStates.isLoaded(committedUser.getGroup(), "createTs"));
        } finally {
            testSupport.deleteRecord(user);
        }
    }

    @Test
    public void testViewOnSecondLevelAfterCommitNew() throws Exception {
        FetchPlan groupView = new View(Group.class, false)
                .addProperty("createTs")
                .addProperty("constraints", new View(Constraint.class, false)
                        .addProperty("createTs"))
                .setLoadPartialEntities(true);

        Group group = dataManager.load(LoadContext.create(Group.class).setId(this.group2.getId()).setView(groupView));
        assertNotNull(group);
        assertFalse(entityStates.isLoaded(group.getConstraints().iterator().next(), "entityName"));

        User user = metadata.create(User.class);
        try {
            user.setName("testUser");
            user.setLogin("login" + user.getId());
            user.setGroup(group);

            FetchPlan userView = new View(User.class, true)
                    .addProperty("login")
                    .addProperty("name")
                    .addProperty("group", new View(Group.class, false)
                            .addProperty("createTs")
                            .addProperty("constraints",
                                    new View(Constraint.class, false)
                                            .addProperty("entityName")));

            User committedUser = dataManager.commit(user, userView);
            assertTrue(entityStates.isLoaded(committedUser.getGroup(), "createTs"));
            assertTrue(entityStates.isLoaded(committedUser.getGroup().getConstraints().iterator().next(), "entityName"));
        } finally {
            testSupport.deleteRecord(user);
        }
    }

    @Test
    public void testViewAfterCommitModified() throws Exception {
        Group group2 = dataManager.load(LoadContext.create(Group.class).setId(this.group2.getId()).setFetchPlan(FetchPlan.INSTANCE_NAME));
        assertFalse(entityStates.isLoaded(group2, "createTs"));

        LoadContext<User> loadContext = LoadContext.create(User.class).setId(userId).setView(view);
        User user = dataManager.load(loadContext);

        user.setName("testUser-changed");
        user.setGroup(group2);

        FetchPlan userView = new View(User.class, true)
                .addProperty("login")
                .addProperty("name")
                .addProperty("group", new View(Group.class)
                        .addProperty("name")
                        .addProperty("createTs"));

        User committedUser = dataManager.commit(user, userView);
        assertTrue(entityStates.isLoaded(committedUser.getGroup(), "createTs"));
    }
}
