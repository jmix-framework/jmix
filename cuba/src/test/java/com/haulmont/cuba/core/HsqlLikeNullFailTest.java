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
import com.haulmont.cuba.core.model.common.Group;
import com.haulmont.cuba.core.model.common.User;
import com.haulmont.cuba.core.testsupport.CoreTest;
import com.haulmont.cuba.core.testsupport.TestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@CoreTest
public class HsqlLikeNullFailTest {
    private User user;
    private Group group;

    @Autowired
    private TestSupport testSupport;
    @Autowired
    private DataManager dataManager;

    @BeforeEach
    public void setUp() {

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
        testSupport.deleteRecord(user, group);
    }

    @Test
    public void testLoadListCaseInsensitive() {
        LoadContext<User> loadContext = LoadContext.create(User.class);
        loadContext.setQueryString("select u from test$User u " +
                "where u.name like :custom_searchString or u.login like :custom_searchString")
                .setParameter("custom_searchString", null);

        List<User> list = dataManager.loadList(loadContext);
        assertEquals(0, list.size());
    }
}
