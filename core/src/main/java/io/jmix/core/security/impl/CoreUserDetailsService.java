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

package io.jmix.core.security.impl;

import io.jmix.core.entity.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Arrays;
import java.util.List;

public class CoreUserDetailsService implements UserDetailsService {

    private List<User> users = Arrays.asList(
            new CoreUser("system", "{noop}", "System"),
            new CoreUser("anonymous", "{noop}", "Anonymous"),
            new CoreUser("admin", "{noop}admin123", "Administrator")
    );

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return users.stream()
                .filter(user -> user.getLoginLowerCase().equals(username))
                .findAny()
                .orElseThrow(() -> new UsernameNotFoundException("User '" + username + "' not found"));
    }
}
