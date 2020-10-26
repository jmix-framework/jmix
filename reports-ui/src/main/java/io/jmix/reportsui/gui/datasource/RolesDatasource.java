/*
 * Copyright (c) 2008-2020 Haulmont.
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

package io.jmix.reportsui.gui.datasource;

//TODO get all roles
//public class RolesDatasource extends CollectionDatasourceImpl<Role, UUID> {

//    protected RolesService rolesService = AppBeans.get(RolesService.NAME);

//    @Override
//    protected void loadData(Map<String, Object> params) {

//        Collection<Role> entities = rolesService.getAllRoles();
//
//        detachListener(data.values());
//        data.clear();
//
//        List<Role> sortedEntities = new ArrayList<>(entities);
//        sortedEntities.sort(Comparator.comparing(Role::getName));
//
//        for (Role entity : sortedEntities) {
//            data.put(entity.getId(), entity);
//            attachListener(entity);
//        }
//    }
//}
