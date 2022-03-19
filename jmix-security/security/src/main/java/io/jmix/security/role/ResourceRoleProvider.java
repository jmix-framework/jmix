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

package io.jmix.security.role;

import io.jmix.security.model.ResourceRole;

/**
 * Interface must be implemented by classes that provide resource roles from a particular source type. A source type may be a
 * database, annotated interfaces, etc.
 * <p>
 * Role providers are used by {@link ResourceRoleRepository}
 */
public interface ResourceRoleProvider extends RoleProvider<ResourceRole> {
}
