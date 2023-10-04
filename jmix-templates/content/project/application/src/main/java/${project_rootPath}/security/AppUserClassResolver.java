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

package ${project_rootPackage}.security;

import ${project_rootPackage}.entity.User;
import io.jmix.core.security.user.UserClassResolver;
import org.springframework.stereotype.Component;

/**
 * The class is used by the {@link io.jmix.core.security.user.DatabaseUserRepository} for resolving the class that
 * represents a user of the application.
 */
@Component("${normalizedPrefix_underscore}AppUserClassResolver")
public class AppUserClassResolver implements UserClassResolver<User> {
    @Override
    public Class<User> getUserClass() {
        return User.class;
    }
}
