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

package io.jmix.core.security.impl;

import io.jmix.core.entity.BaseUser;
import io.jmix.core.security.OnCoreSecurityImplementation;
import io.jmix.core.security.UserRepository;
import org.springframework.context.annotation.Conditional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component(UserRepository.NAME)
@Conditional(OnCoreSecurityImplementation.class)
public class CoreUserRepository implements UserRepository {

    private CoreUser systemUser;
    private CoreUser anonymousUser;

    private List<BaseUser> users = Arrays.asList(
            new CoreUser("system", "{noop}", "System"),
            new CoreUser("anonymous", "{noop}", "Anonymous"),
            new CoreUser("admin", "{noop}admin123", "Administrator")
    );

    public CoreUserRepository() {
        systemUser = new CoreUser("system", null, "system");
        anonymousUser = new CoreUser("anonymous", null, "anonymous");
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return users.stream()
                .filter(user -> user.getUsername().equals(username))
                .findAny()
                .orElseThrow(() -> new UsernameNotFoundException("User '" + username + "' not found"));
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
    public List<BaseUser> getAll() {
        return users;
    }
}
