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

import com.haulmont.cuba.core.model.common.Group;
import com.haulmont.cuba.core.model.common.User;
import com.haulmont.cuba.core.testsupport.CoreTest;
import com.haulmont.cuba.core.testsupport.TestContainer;
import io.jmix.core.AppBeans;
import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@CoreTest
public class HsqlLikeNullFailTest {

    public static TestContainer cont = TestContainer.Common.INSTANCE;

    private User user;
    private Group group;

    @BeforeEach
    public void setUp() {
        DataManager dataManager = AppBeans.get(DataManager.NAME);

        group = new Group();
        group.setName("group");

        user = new User();
        user.setGroup(group);
        user.setName("Test");
        user.setLogin("tEst");
        user.setLoginLowerCase("test");

        dataManager.commit(user, group);
    }

    @AfterEach
    public void tearDown() {
        cont.deleteRecord(user, group);
    }

    @Test
    public void testLoadListCaseInsensitive() {
        LoadContext<User> loadContext = LoadContext.create(User.class);
        loadContext.setQueryString("select u from test$User u " +
                "where u.name like :custom_searchString or u.login like :custom_searchString")
                .setParameter("custom_searchString", null);

        DataManager dataManager = AppBeans.get(DataManager.NAME);
        List<User> list = dataManager.loadList(loadContext);
        assertEquals(0, list.size());
    }
}