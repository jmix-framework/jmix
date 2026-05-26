/*
 * Copyright 2026 Haulmont.
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

package io.jmix.security.user;

import io.jmix.core.entity.annotation.MetaAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a Boolean field of a user entity that indicates whether the user is required to change
 * the password at the next logon.
 * <p>
 * When this annotation is present on a field:
 * <ul>
 *   <li>{@code AbstractDatabaseUserRepository} resets the field to {@code false} when the user changes
 *       the password, and sets it to {@code true} when the administrator resets the password.</li>
 *   <li>UI shows a modal forced change-password dialog after the user logs in.</li>
 * </ul>
 * Only one field of a class can have this annotation.
 */
@Target({FIELD})
@Retention(RUNTIME)
@MetaAnnotation
public @interface PasswordChangeRequired {
}
