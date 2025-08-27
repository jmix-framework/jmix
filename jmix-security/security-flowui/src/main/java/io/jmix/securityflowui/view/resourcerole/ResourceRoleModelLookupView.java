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
import io.jmix.security.model.ResourceRoleModel;
import io.jmix.security.model.RoleModelConverter;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.securityflowui.component.rolefilter.RoleFilter;
import io.jmix.securityflowui.component.rolefilter.RoleFilterChangeEvent;
import io.jmix.securityflowui.util.RoleAssignmentCandidatePredicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static io.jmix.securityflowui.util.PredicateUtils.combineRoleAssignmentPredicates;

@Route(value = "sec/resourcerolemodelslookup", layout = DefaultMainViewParent.class)
@ViewController("sec_ResourceRoleModel.lookup")
@ViewDescriptor("resource-role-model-lookup-view.xml")
@LookupComponent("roleModelsTable")
@DialogMode(width = "50em")
public class ResourceRoleModelLookupView extends StandardListView<ResourceRoleModel> {

    @ViewComponent
    private CollectionContainer<ResourceRoleModel> roleModelsDc;

    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private RoleModelConverter roleModelConverter;
    @Autowired
    private ResourceRoleRepository roleRepository;

    @Autowired(required = false)
    protected List<RoleAssignmentCandidatePredicate> roleAssignmentCandidatePredicates = Collections.emptyList();
    protected RoleAssignmentCandidatePredicate compositeRoleAssignmentCandidatePredicate;

    private List<String> excludedRolesCodes = Collections.emptyList();

    private UserDetails user;

    @Subscribe
    public void onInit(InitEvent event) {
        initFilter();
        compositeRoleAssignmentCandidatePredicate = combineRoleAssignmentPredicates(roleAssignmentCandidatePredicates);
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
                .filter(role -> (event == null || event.matches(role))
                        && !excludedRolesCodes.contains(role.getCode())
                )
                .filter(role -> compositeRoleAssignmentCandidatePredicate.test(user, role))
                .map(roleModelConverter::createResourceRoleModel)
                .sorted(Comparator.comparing(ResourceRoleModel::getName))
                .collect(Collectors.toList());
        roleModelsDc.setItems(roleModels);
    }

    public void setExcludedRoles(List<String> excludedRolesCodes) {
        this.excludedRolesCodes = excludedRolesCodes;
    }

    protected RoleAssignmentCandidatePredicate combinePredicates(List<RoleAssignmentCandidatePredicate> predicates) {
        return (user, role) -> {
            /*
                Using loop instead of 'and()' to work with custom type of predicates
                and to mitigate possible stack overflow due to undetermined amount of predicates (low probability)
             */
            for (RoleAssignmentCandidatePredicate p : predicates) {
                if (!p.test(user, role)) {
                    return false;
                }
            }
            return true;
        };
    }

    public void setUser(UserDetails user) {
        this.user = user;
    }
}
