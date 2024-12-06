/*
 * Copyright 2024 Haulmont.
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

import io.jmix.security.model.BaseRoleModel;
import io.jmix.security.model.ResourceRoleModel;
import io.jmix.security.model.RowLevelRoleModel;

import java.util.Collection;
import java.util.List;

/**
 * Interface to be implemented by beans that store roles in a persistent storage.
 */
public interface RolePersistence {

    void save(ResourceRoleModel roleModel);

    void save(RowLevelRoleModel roleModel);

    void removeRoles(Collection<? extends BaseRoleModel> roleModels);

    byte[] exportResourceRoles(List<ResourceRoleModel> roleModels, boolean zip);

    byte[] exportRowLevelRoles(List<RowLevelRoleModel> roleModels, boolean zip);

    List<Object> importResourceRoles(byte[] data, boolean zip);

    List<Object> importRowLevelRoles(byte[] data, boolean zip);
}
