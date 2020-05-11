/*
 * Copyright 2020 Haulmont.
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

package io.jmix.security.impl;

import io.jmix.core.FetchPlanRepository;
import io.jmix.core.entity.BaseUser;
import io.jmix.core.security.UserRepository;
import io.jmix.data.PersistenceHints;
import io.jmix.security.OnStandardSecurityImplementation;
import io.jmix.security.entity.User;
import org.springframework.context.annotation.Conditional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;

@Component(UserRepository.NAME)
@Conditional(OnStandardSecurityImplementation.class)
public class StandardUserRepository implements UserRepository {

    private User systemUser;
    private User anonymousUser;

    @Inject
    private EntityManagerFactory entityManagerFactory;

    @Inject
    private FetchPlanRepository fetchPlanRepository;

    public StandardUserRepository() {
        systemUser = createSystemUser();
        anonymousUser = createAnonymousUser();
    }

    private User createSystemUser() {
        User user = new User();
        user.setUsername("system");
        user.setName("system");
        return user;
    }

    private User createAnonymousUser() {
        User user = new User();
        user.setUsername("anonymous");
        user.setName("anonymous");
        return user;
    }

    @Override
    public BaseUser getSystemUser() {
        return systemUser;
    }

    @Override
    public BaseUser getAnonymousUser() {
        return anonymousUser;
    }

    @Override
    public List<User> getAll() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        return entityManager.createQuery("select u from sec_User u", User.class)
                .getResultList();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        List<User> users = entityManager.createQuery("select u from sec_User u where u.username = ?1", User.class)
                .setParameter(1, username)
                .setHint(PersistenceHints.FETCH_PLAN, fetchPlanRepository.getFetchPlan(User.class, "user-login"))
                .getResultList();

        if (!users.isEmpty()) {
            return users.get(0);
        } else {
            throw new UsernameNotFoundException("User not found");
        }
    }
}
