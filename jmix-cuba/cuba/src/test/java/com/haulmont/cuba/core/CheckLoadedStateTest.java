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

import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.core.model.UserRelatedNews;
import com.haulmont.cuba.core.model.common.Group;
import com.haulmont.cuba.core.model.common.User;
import com.haulmont.cuba.core.testsupport.CoreTest;
import com.haulmont.cuba.core.testsupport.TestSupport;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlanRepository;
import io.jmix.core.Metadata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@CoreTest
public class CheckLoadedStateTest {
    @Autowired
    private FetchPlanRepository viewRepository;
    @Autowired
    private Persistence persistence;
    @Autowired
    private Metadata metadata;
    @Autowired
    private TestSupport testSupport;

    private UUID userRelatedNewsId = null;
    private List<UUID> recursiveUserRelatedIds = null;
    private UUID userId, groupId;


    @BeforeEach
    public void setUp() {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            Group group = metadata.create(Group.class);
            groupId = group.getId();
            group.setName("Group-" + group.getId());
            em.persist(group);

            User user = metadata.create(User.class);
            userId = user.getId();
            user.setName("testUser");
            user.setLogin("login" + userId);
            user.setPassword("000");
            user.setGroup(group);
            em.persist(user);

            tx.commit();
        } finally {
            tx.end();
        }

    }

    @Test
    public void checkLocalProperties() {
        DataManager dataManager = AppBeans.get(DataManager.class);
        EntityStates entityStates = AppBeans.get(EntityStates.class);

        User user = dataManager.load(LoadContext.create(User.class)
                .setId(userId).setFetchPlan(FetchPlan.LOCAL));

        entityStates.checkLoaded(user, "login", "name", "active");

        try {
            entityStates.checkLoaded(user, "group");

            fail("user.group is not loaded");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("group is not loaded"));
        }
    }

    @Test
    public void checkMinimalProperties() {
        DataManager dataManager = AppBeans.get(DataManager.class);
        EntityStates entityStates = AppBeans.get(EntityStates.class);

        User user = dataManager.load(LoadContext.create(User.class)
                .setId(userId).setFetchPlan(View.MINIMAL));

        entityStates.checkLoaded(user, "login", "name");

        try {
            entityStates.checkLoaded(user, "group");

            fail("user.group is not loaded");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("group is not loaded"));
        }

        try {
            entityStates.checkLoaded(user, "password");

            fail("user.password is not loaded");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("password is not loaded"));
        }

        try {
            entityStates.checkLoaded(user, "email");

            fail("user.email is not loaded");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("email is not loaded"));
        }
    }

    @Test
    public void checkInstanceNameProperties() {
        DataManager dataManager = AppBeans.get(DataManager.class);
        EntityStates entityStates = AppBeans.get(EntityStates.class);

        User user = dataManager.load(LoadContext.create(User.class)
                .setId(userId).setFetchPlan(FetchPlan.INSTANCE_NAME));

        entityStates.checkLoaded(user, "login", "name");

        try {
            entityStates.checkLoaded(user, "group");

            fail("user.group is not loaded");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("group is not loaded"));
        }

        try {
            entityStates.checkLoaded(user, "password");

            fail("user.password is not loaded");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("password is not loaded"));
        }

        try {
            entityStates.checkLoaded(user, "email");

            fail("user.email is not loaded");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("email is not loaded"));
        }
    }

    @Test
    public void checkLocalView() {
        DataManager dataManager = AppBeans.get(DataManager.class);
        EntityStates entityStates = AppBeans.get(EntityStates.class);

        User user = dataManager.load(LoadContext.create(User.class)
                .setId(userId).setFetchPlan(FetchPlan.LOCAL));

        assertTrue(entityStates.isLoadedWithView(user, FetchPlan.LOCAL));

        entityStates.checkLoadedWithFetchPlan(user, FetchPlan.LOCAL);

        User userMinimal = dataManager.load(LoadContext.create(User.class)
                .setId(userId).setFetchPlan(FetchPlan.INSTANCE_NAME));

        try {
            assertFalse(entityStates.isLoadedWithView(userMinimal, FetchPlan.LOCAL));

            entityStates.checkLoadedWithFetchPlan(userMinimal, FetchPlan.LOCAL);

            fail("user local attributes are not loaded");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains(" is not loaded"));
        }
    }

    @Test
    public void checkDeepView() {
        DataManager dataManager = AppBeans.get(DataManager.class);
        EntityStates entityStates = AppBeans.get(EntityStates.class);

        User user = dataManager.load(LoadContext.create(User.class)
                .setId(userId).setFetchPlan("user.browse"));

        try {
            assertFalse(entityStates.isLoadedWithView(user, "user.edit"));

            entityStates.checkLoadedWithFetchPlan(user, "user.edit");

            fail("user edit attributes are not loaded");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains(" is not loaded"));
        }

        User userEdit = dataManager.load(LoadContext.create(User.class)
                .setId(userId).setFetchPlan("user.edit"));

        assertTrue(entityStates.isLoadedWithView(userEdit, "user.edit"));

        entityStates.checkLoadedWithFetchPlan(userEdit, "user.edit");
    }

    @Test
    public void checkRelatedView() {
        DataManager dataManager = AppBeans.get(DataManager.class);
        EntityStates entityStates = AppBeans.get(EntityStates.class);

        User user = dataManager.load(LoadContext.create(User.class)
                .setId(userId).setFetchPlan(View.MINIMAL));

        UserRelatedNews record = metadata.create(UserRelatedNews.class);

        userRelatedNewsId = record.getId();

        record.setName("Test");
        record.setUser(user);

        UserRelatedNews testRecord = dataManager.commit(record);

        View view = new View(UserRelatedNews.class, false);
        view.addProperty("userLogin");
        view.addProperty("name");

        entityStates.checkLoadedWithFetchPlan(testRecord, view);

        assertTrue(entityStates.isLoadedWithFetchPlan(testRecord, view));

        UserRelatedNews minimalRecord = dataManager.load(LoadContext.create(UserRelatedNews.class)
                .setId(userRelatedNewsId).setFetchPlan(View.MINIMAL));

        try {
            assertFalse(entityStates.isLoadedWithFetchPlan(minimalRecord, view));

            entityStates.checkLoadedWithFetchPlan(minimalRecord, view);

            fail("minimal record attributes are not loaded");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("userLogin is not loaded"));
        }
    }

    @Test
    public void checkRecursiveView() {
        DataManager dataManager = AppBeans.get(DataManager.class);
        EntityStates entityStates = AppBeans.get(EntityStates.class);

        User user = dataManager.load(LoadContext.create(User.class)
                .setId(userId).setFetchPlan(FetchPlan.INSTANCE_NAME));

        recursiveUserRelatedIds = new ArrayList<>();

        UserRelatedNews parentRecord = metadata.create(UserRelatedNews.class);
        parentRecord.setName("Test");
        parentRecord.setUser(user);

        UserRelatedNews committedParentRecord = dataManager.commit(parentRecord);
        recursiveUserRelatedIds.add(committedParentRecord.getId());

        UserRelatedNews record = metadata.create(UserRelatedNews.class);
        record.setName("Test");
        record.setUser(user);
        record.setParent(committedParentRecord);

        UserRelatedNews committedRecord = dataManager.commit(record);
        recursiveUserRelatedIds.add(committedRecord.getId());

        View view = new View(UserRelatedNews.class, false);
        view.addProperty("parent");
        view.addProperty("name");

        assertTrue(entityStates.isLoadedWithFetchPlan(committedRecord, view));

        entityStates.checkLoadedWithFetchPlan(committedRecord, view);

        UserRelatedNews localRecord = dataManager.load(LoadContext.create(UserRelatedNews.class)
                .setId(committedRecord.getId()).setFetchPlan(FetchPlan.LOCAL));

        try {
            assertFalse(entityStates.isLoadedWithFetchPlan(localRecord, view));

            entityStates.checkLoadedWithFetchPlan(localRecord, view);

            fail("local record attributes are not loaded");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("parent is not loaded"));
        }
    }

    @Test
    public void testGetCurrentFetchPlan() {
        DataManager dataManager = AppBeans.get(DataManager.class);
        EntityStates entityStates = AppBeans.get(EntityStates.class);

        User user = dataManager.load(User.class)
                .id(userId)
                .fetchPlan("user.edit")
                .one();

        FetchPlan fetchPlan = entityStates.getCurrentFetchPlan(user);

        User user1 = dataManager.load(User.class)
                .id(userId)
                .fetchPlan(fetchPlan)
                .one();

        entityStates.checkLoadedWithFetchPlan(user1, "user.edit");
    }

    @AfterEach
    public void tearDown() {
        if (userRelatedNewsId != null) {
            testSupport.deleteRecord("TEST_USER_RELATED_NEWS", userRelatedNewsId);
            userRelatedNewsId = null;
        }

        if (recursiveUserRelatedIds != null) {
            Collections.reverse(recursiveUserRelatedIds);

            for (UUID id : recursiveUserRelatedIds) {
                testSupport.deleteRecord("TEST_USER_RELATED_NEWS", id);
            }
        }
        testSupport.deleteRecord("TEST_USER", userId);
        testSupport.deleteRecord("TEST_GROUP", groupId);
    }
}
