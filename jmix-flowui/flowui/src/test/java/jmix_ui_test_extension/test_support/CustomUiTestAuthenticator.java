/*
 * Copyright 2022 Haulmont.
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

package jmix_ui_test_extension.test_support;

import io.jmix.core.security.InMemoryUserRepository;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.core.security.UserRepository;
import io.jmix.flowui.testassist.UiTestAuthenticator;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

public class CustomUiTestAuthenticator implements UiTestAuthenticator {

    public static final String username = "class_auth_custom_user";

    @Override
    public void setupAuthentication(ApplicationContext context) {
        addUser(context, username);
        context.getBean(SystemAuthenticator.class).begin(username);
    }

    @Override
    public void removeAuthentication(ApplicationContext context) {
        context.getBean(SystemAuthenticator.class).end();
        removeUser(context, username);
    }

    protected void addUser(ApplicationContext context, String username) {
        InMemoryUserRepository userRepository = (InMemoryUserRepository) context.getBean(UserRepository.class);
        userRepository.addUser(new User(username, "", Collections.emptyList()));
    }

    protected void removeUser(ApplicationContext context, String username) {
        InMemoryUserRepository userRepository = (InMemoryUserRepository) context.getBean(UserRepository.class);
        UserDetails userDetails = userRepository.loadUserByUsername(username);
        userRepository.removeUser(userDetails);
    }
}
