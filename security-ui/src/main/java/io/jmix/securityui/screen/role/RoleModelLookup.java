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

package io.jmix.securityui.screen.role;

import io.jmix.security.model.Role;
import io.jmix.security.role.RoleRepository;
import io.jmix.securityui.model.RoleModel;
import io.jmix.securityui.model.RoleModelConverter;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@UiController("sec_RoleModel.lookup")
@UiDescriptor("role-model-lookup.xml")
@LookupComponent("roleModelsTable")
public class RoleModelLookup extends StandardLookup<RoleModel> {

    @Autowired
    private CollectionContainer<RoleModel> roleModelsDc;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RoleModelConverter roleModelConverter;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        Collection<Role> roles = roleRepository.getAllRoles();
        List<RoleModel> roleModels = roles.stream()
                .map(roleModelConverter::createRoleModel)
                .sorted(Comparator.comparing(RoleModel::getName))
                .collect(Collectors.toList());
        roleModelsDc.getMutableItems().addAll(roleModels);
    }
}
