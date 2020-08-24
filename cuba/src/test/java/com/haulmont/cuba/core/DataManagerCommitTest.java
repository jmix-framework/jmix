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

import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.core.model.common.Group;
import com.haulmont.cuba.core.model.common.Role;
import com.haulmont.cuba.core.model.common.User;
import com.haulmont.cuba.core.model.common.UserRole;
import com.haulmont.cuba.core.testsupport.CoreTest;
import com.haulmont.cuba.core.testsupport.TestSupport;
import io.jmix.core.EntityStates;
import io.jmix.core.FetchPlan;
import io.jmix.core.Metadata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@CoreTest
public class DataManagerCommitTest {
    @Autowired
    private Persistence persistence;
    @Autowired
    private Metadata metadata;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private EntityStates entityStates;
    @Autowired
    private TestSupport testSupport;

    private UUID userId;
    private UUID userRoleId;
    private Group group;
    private Role role;
    private FetchPlan view;

    @BeforeEach
    public void setUp() throws Exception {
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();

            group = new Group();
            group.setName("Group-" + group.getId());
            em.persist(group);

            role = new Role();
            role.setName("role1");
            em.persist(role);

            User user = new User();
            userId = user.getId();
            user.setName("testUser");
            user.setLogin("login" + userId);
            user.setPassword("000");
            user.setGroup(group);
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
        testSupport.deleteRecord(group);
    }

    @Test
    public void testViewAfterCommit() throws Exception {
        LoadContext<User> loadContext = LoadContext.create(User.class).setId(userId).setView(view);
        User user = dataManager.load(loadContext);
        assertNotNull(user);
        user = testSupport.reserialize(user);
        assertEquals(group.getId(), user.getGroup().getId());
        assertEquals(1, user.getUserRoles().size());
        assertEquals(userRoleId, user.getUserRoles().get(0).getId());

        Integer version = user.getVersion();
        user.setName("testUser-changed");
        user = dataManager.commit(user, view);
        assertNotNull(user);

        //do check loaded before serialization
        assertTrue(entityStates.isLoaded(user, "group"));
        assertTrue(entityStates.isLoaded(user, "userRoles"));
        assertTrue(!entityStates.isLoaded(user, "substitutions"));
        //do second check to make sure isLoaded did not affect attribute fetch status
        assertTrue(!entityStates.isLoaded(user, "substitutions"));

        user = testSupport.reserialize(user);

        assertTrue(entityStates.isDetached(user));
        assertTrue(!entityStates.isNew(user));
        assertTrue(!entityStates.isManaged(user));

        assertEquals(version + 1, (int) user.getVersion());
        assertEquals("testUser-changed", user.getName());
        assertEquals(group.getId(), user.getGroup().getId());
        assertEquals(1, user.getUserRoles().size());
        assertEquals(userRoleId, user.getUserRoles().get(0).getId());

        //do check loaded after serialization
        assertTrue(entityStates.isLoaded(user, "group"));
        assertTrue(entityStates.isLoaded(user, "userRoles"));
        assertTrue(!entityStates.isLoaded(user, "substitutions"));
        //do second check to make sure isLoaded did not affect attribute fetch status
        assertTrue(!entityStates.isLoaded(user, "substitutions"));
    }
}
