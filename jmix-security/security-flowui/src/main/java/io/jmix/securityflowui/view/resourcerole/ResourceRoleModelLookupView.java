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

package io.jmix.securityflowui.view.resourcerole;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.*;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.securityflowui.component.rolefilter.RoleFilter;
import io.jmix.securityflowui.component.rolefilter.RoleFilterChangeEvent;
import io.jmix.securityflowui.model.ResourceRoleModel;
import io.jmix.securityflowui.model.RoleModelConverter;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "sec/resourcerolemodelslookup", layout = DefaultMainViewParent.class)
@ViewController("sec_ResourceRoleModel.lookup")
@ViewDescriptor("resource-role-model-lookup-view.xml")
@LookupComponent("roleModelsTable")
@DialogMode(width = "50em", height = "37.5em")
public class ResourceRoleModelLookupView extends StandardListView<ResourceRoleModel> {

    @ViewComponent
    private CollectionContainer<ResourceRoleModel> roleModelsDc;

    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private RoleModelConverter roleModelConverter;
    @Autowired
    private ResourceRoleRepository roleRepository;

    @Subscribe
    public void onInit(InitEvent event) {
        initFilter();
    }

    private void initFilter() {
        RoleFilter filter = uiComponents.create(RoleFilter.class);
        filter.setSourceFilterVisible(false);
        filter.addRoleFilterChangeListener(this::onRoleFilterChange);

        getContent().addComponentAsFirst(filter);
    }

    private void onRoleFilterChange(RoleFilterChangeEvent event) {
        loadRoles(event);
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        loadRoles(null);
    }

    protected void loadRoles(@Nullable RoleFilterChangeEvent event) {
        List<ResourceRoleModel> roleModels = roleRepository.getAllRoles().stream()
                .filter(role -> event == null || event.matches(role))
                .map(roleModelConverter::createResourceRoleModel)
                .sorted(Comparator.comparing(ResourceRoleModel::getName))
                .collect(Collectors.toList());
        roleModelsDc.setItems(roleModels);
    }
}
