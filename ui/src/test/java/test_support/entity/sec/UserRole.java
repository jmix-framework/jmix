/*
 * Copyright 2019 Haulmont.
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
package test_support.entity.sec;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;
import test_support.entity.TestBaseEntity;

import javax.persistence.*;

/**
 * Link between users and roles.
 */
@Entity(name = "sec$UserRole")
@JmixEntity
@Table(name = "SEC_USER_ROLE")
@SystemLevel
public class UserRole extends TestBaseEntity {

    private static final long serialVersionUID = 8543853035155300992L;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID")
    @OnDeleteInverse(DeletePolicy.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ROLE_ID")
    @OnDeleteInverse(DeletePolicy.CASCADE)
    private Role role;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
