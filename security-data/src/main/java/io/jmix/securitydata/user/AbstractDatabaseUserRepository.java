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

package io.jmix.securitydata.user;

import io.jmix.core.DataManager;
import io.jmix.core.JmixEntity;
import io.jmix.core.Metadata;
import io.jmix.core.entity.BaseUser;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.security.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Abstract {@link UserRepository} that loads User entity from the database. A {@link UserRepository} generated in the
 * project may extend this class. It must override the {@link #getUserClass()} method.
 *
 * @param <T>
 */
public abstract class AbstractDatabaseUserRepository<T extends BaseUser & JmixEntity> implements UserRepository {

    protected T systemUser;
    protected T anonymousUser;

    protected DataManager dataManager;
    protected Metadata metadata;

    @Autowired
    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Autowired
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @PostConstruct
    private void init() {
        systemUser = createSystemUser();
        anonymousUser = createAnonymousUser();
    }

    /**
     * Method returns an actual class of the User entity used in the project
     */
    protected abstract Class<T> getUserClass();

    protected T createSystemUser() {
        T systemUser = metadata.create(getUserClass());
        EntityValues.setValue(systemUser, "username", "system");
        return systemUser;
    }

    protected T createAnonymousUser() {
        T systemUser = metadata.create(getUserClass());
        EntityValues.setValue(systemUser, "username", "system");
        return systemUser;
    }

    @Override
    public T getSystemUser() {
        return systemUser;
    }

    @Override
    public T getAnonymousUser() {
        return anonymousUser;
    }

    @Override
    public List<T> getByUsernameLike(String username) {
        //todo view
        return dataManager.load(getUserClass())
                .query("where e.username like :username")
                .parameter("username", "%" + username + "%")
                .list();
    }

    @Override
    public T loadUserByUsername(String username) throws UsernameNotFoundException {
        //todo view
        List<T> users = dataManager.load(getUserClass())
                .query("where e.username = :username")
                .parameter("username", username)
                .list();
        if (!users.isEmpty()) {
            return users.get(0);
        } else {
            throw new UsernameNotFoundException("User not found");
        }
    }
}
