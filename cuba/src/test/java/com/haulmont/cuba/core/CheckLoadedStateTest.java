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

import com.haulmont.cuba.core.model.UserRelatedNews;
import com.haulmont.cuba.core.model.common.Group;
import com.haulmont.cuba.core.model.common.User;
import com.haulmont.cuba.core.testsupport.CoreTest;
import com.haulmont.cuba.core.testsupport.TestSupport;
import io.jmix.core.*;
import io.jmix.data.EntityManager;
import io.jmix.data.Persistence;
import io.jmix.data.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@CoreTest
public class CheckLoadedStateTest {
    @Inject
    private ViewRepository viewRepository;
    @Inject
    private Persistence persistence;
    @Inject
    private Metadata metadata;

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
                .setId(userId)
                .setView(View.LOCAL));

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
                .setId(userId)
                .setView(View.MINIMAL));

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
                .setId(userId)
                .setView(View.LOCAL));

        assertTrue(entityStates.isLoadedWithView(user, View.LOCAL));

        entityStates.checkLoadedWithView(user, View.LOCAL);

        User userMinimal = dataManager.load(LoadContext.create(User.class)
                .setId(userId)
                .setView(View.MINIMAL));

        try {
            assertFalse(entityStates.isLoadedWithView(userMinimal, View.LOCAL));

            entityStates.checkLoadedWithView(userMinimal, View.LOCAL);

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
                .setId(userId)
                .setView("user.browse"));

        try {
            assertFalse(entityStates.isLoadedWithView(user, "user.edit"));

            entityStates.checkLoadedWithView(user, "user.edit");

            fail("user edit attributes are not loaded");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains(" is not loaded"));
        }

        User userEdit = dataManager.load(LoadContext.create(User.class)
                .setId(userId)
                .setView("user.edit"));

        assertTrue(entityStates.isLoadedWithView(userEdit, "user.edit"));

        entityStates.checkLoadedWithView(userEdit, "user.edit");
    }

    @Test
    public void checkRelatedView() {
        DataManager dataManager = AppBeans.get(DataManager.class);
        EntityStates entityStates = AppBeans.get(EntityStates.class);

        User user = dataManager.load(LoadContext.create(User.class)
                .setId(userId)
                .setView(View.MINIMAL));

        UserRelatedNews record = metadata.create(UserRelatedNews.class);

        userRelatedNewsId = record.getId();

        record.setName("Test");
        record.setUser(user);

        UserRelatedNews testRecord = dataManager.commit(record);

        View view = new View(UserRelatedNews.class, false);
        view.addProperty("userLogin");
        view.addProperty("name");

        entityStates.checkLoadedWithView(testRecord, view);

        assertTrue(entityStates.isLoadedWithView(testRecord, view));

        UserRelatedNews minimalRecord = dataManager.load(LoadContext.create(UserRelatedNews.class)
                .setId(userRelatedNewsId)
                .setView(View.MINIMAL));

        try {
            assertFalse(entityStates.isLoadedWithView(minimalRecord, view));

            entityStates.checkLoadedWithView(minimalRecord, view);

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
                .setId(userId)
                .setView(View.MINIMAL));

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

        assertTrue(entityStates.isLoadedWithView(committedRecord, view));

        entityStates.checkLoadedWithView(committedRecord, view);

        UserRelatedNews localRecord = dataManager.load(LoadContext.create(UserRelatedNews.class)
                .setId(committedRecord.getId())
                .setView(View.LOCAL));

        try {
            assertFalse(entityStates.isLoadedWithView(localRecord, view));

            entityStates.checkLoadedWithView(localRecord, view);

            fail("local record attributes are not loaded");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("parent is not loaded"));
        }
    }

    @AfterEach
    public void tearDown() {
        if (userRelatedNewsId != null) {
            TestSupport.deleteRecord("TEST_USER_RELATED_NEWS", userRelatedNewsId);
            userRelatedNewsId = null;
        }

        if (recursiveUserRelatedIds != null) {
            Collections.reverse(recursiveUserRelatedIds);

            for (UUID id : recursiveUserRelatedIds) {
                TestSupport.deleteRecord("TEST_USER_RELATED_NEWS", id);
            }
        }
        TestSupport.deleteRecord("TEST_USER", userId);
        TestSupport.deleteRecord("TEST_GROUP", groupId);
    }
}