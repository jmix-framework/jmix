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

package io.jmix.core.entity;

import io.jmix.core.Entity;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

/**
 * Base class for users. In most cases projects will use {@code User} implementation from the {@code security} module.
 * If the project needs custom user class, it must implement this interface;
 */
public interface BaseUser extends UserDetails, Entity<UUID> {

    /**
     * Returns unique representation of the user. It may be a string with user identifier, a combination of tenant id
     * and login, etc.
     * <p>
     * This key will be user by framework mechanisms to refer the user.
     */
    String getKey();

}
