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

package com.haulmont.cuba.core.model;

import com.haulmont.cuba.core.model.common.User;
import io.jmix.data.entity.StandardEntity;
import io.jmix.core.metamodel.annotation.ModelProperty;
import com.haulmont.chile.core.annotations.NamePattern;

import javax.persistence.*;

@Entity(name = "test$UserRelatedNews")
@Table(name = "TEST_USER_RELATED_NEWS")
@NamePattern("%s|name")
public class UserRelatedNews extends StandardEntity {
    @Column(name = "NAME")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_ID")
    private UserRelatedNews parent;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UserRelatedNews getParent() {
        return parent;
    }

    public void setParent(UserRelatedNews parent) {
        this.parent = parent;
    }

    @ModelProperty(related = "user")
    public String getUserLogin() {
        return user.getLogin();
    }
}
