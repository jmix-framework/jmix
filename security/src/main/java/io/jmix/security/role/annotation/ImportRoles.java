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

package io.jmix.security.role.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines set of role codes of roles to aggregate into annotated role.
 * Policies from these roles will be aggregated into annotated role policies.
 * <p>
 * Example:
 * <pre>
 * &#064;Role(name = "My Role", code = "myRole")
 * public interface MyRole {
 *
 *     &#064;RolesToAggregate(roleCodes = {"minimal", "manager"})
 *     void roles();
 * }
 * </pre>
 *
 * @see io.jmix.security.role.annotation.Role
 * @see io.jmix.security.model.ResourcePolicy
 * @see io.jmix.security.model.RowLevelPolicy
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ImportRoles {

    String[] roleCodes();

}
