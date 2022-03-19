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
import com.haulmont.cuba.core.model.common.ScheduledTask;
import com.haulmont.cuba.core.model.common.User;
import com.haulmont.cuba.core.model.common.UserRole;
import com.haulmont.cuba.core.testsupport.CoreTest;
import com.haulmont.cuba.core.testsupport.TestSupport;
import io.jmix.core.EntityStates;
import io.jmix.core.FetchPlan;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@CoreTest
public class PersistenceAttributeLoadedCheckTest {
    @Autowired
    private DataManager dataManager;
    @Autowired
    private Persistence persistence;
    @Autowired
    private EntityStates entityStates;
    @Autowired
    private TestSupport testSupport;

    private UUID taskId;
    private UUID userId;
    private UUID groupId;
    private FetchPlan taskView;
    private FetchPlan userView;

    @BeforeEach
    public void setUp() {

        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();

            ScheduledTask task = new ScheduledTask();
            task.setBeanName("BeanName");
            task.setMethodName("MethodName");
            taskId = task.getId();

            Group group = new Group();
            group.setName("group");
            groupId = group.getId();
            em.persist(group);

            User user = new User();
            userId = user.getId();
            user.setName("testUser");
            user.setLogin("login" + userId);
            user.setPassword("000");
            user.setGroup(em.find(Group.class, groupId));
            em.persist(user);

            em.persist(task);
            em.persist(user);
            tx.commit();
        }

        taskView = new View(ScheduledTask.class, true)
                .addProperty("beanName");

        userView = new View(User.class, true)
                .addProperty("login")
                .addProperty("loginLowerCase")
                .addProperty("name")
                .addProperty("password")
                .addProperty("group", new View(Group.class).addProperty("name"))
                .addProperty("userRoles", new View(UserRole.class));
    }

    @AfterEach
    public void tearDown() {
        testSupport.deleteRecord("TEST_USER", userId);
        testSupport.deleteRecord("TEST_GROUP", groupId);
        testSupport.deleteRecord("TEST_SCHEDULED_TASK", taskId);
    }

    @Test
    public void testIsLoadedLogic() throws Exception {
        LoadContext<User> userContext = LoadContext.create(User.class).setId(userId).setView(userView);
        LoadContext<ScheduledTask> taskContext = LoadContext.create(ScheduledTask.class).setId(taskId).setView(taskView);
        User user = dataManager.load(userContext);
        ScheduledTask task = dataManager.load(taskContext);

        assertNotNull(user);
        assertNotNull(task);

        assertTrue(entityStates.isLoaded(user, "group"));//if attribute is in the view - it should be loaded
        assertTrue(entityStates.isLoaded(user, "userRoles"));//if attribute is in the view - it should be loaded
        assertTrue(!entityStates.isLoaded(user, "substitutions"));//if attribute is not in the view - it should not be loaded
        try {
            entityStates.isLoaded(user, "notExistingAttribute");
            Assertions.fail("Should throw an exception for not existing attribute");
        } catch (Exception ignored) {
        }

        assertTrue(entityStates.isLoaded(task, "beanName"));//if attribute is in the view - it should be loaded
        assertTrue(!entityStates.isLoaded(task, "methodName"));//if attribute is not in the view - it should not be loaded
        assertTrue(entityStates.isLoaded(task, "methodParametersString"));//meta properties should be marked as loaded

        user = testSupport.reserialize(user);
        task = testSupport.reserialize(task);

        assertTrue(entityStates.isLoaded(user, "group"));//if attribute is in the view - it should be loaded
        assertTrue(entityStates.isLoaded(user, "userRoles"));//if attribute is in the view - it should be loaded
        assertTrue(!entityStates.isLoaded(user, "substitutions"));//if attribute is not in the view - it should not be loaded
        try {
            entityStates.isLoaded(user, "notExistingAttribute");
            Assertions.fail("Should throw an exception for not existing attribute");
        } catch (Exception ignored) {
        }

        assertTrue(entityStates.isLoaded(task, "beanName"));//if attribute is in the view - it should be loaded
        assertTrue(!entityStates.isLoaded(task, "methodName"));//if attribute is not in the view - it should not be loaded
        assertTrue(entityStates.isLoaded(task, "methodParametersString"));//meta properties should be marked as loaded
    }

    @Test
    public void testManagedInstance() throws Exception {
        try (Transaction tx = persistence.createTransaction()) {
            User user = persistence.getEntityManager().find(User.class, userId);

            assertTrue(entityStates.isLoaded(user, "name"));
            assertFalse(entityStates.isLoaded(user, "group"));

            tx.commit();
        }
    }
}
