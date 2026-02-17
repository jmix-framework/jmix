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
import com.vaadin.flow.router.RouteAlias;
import io.jmix.core.EntityStates;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.*;
import io.jmix.security.model.BaseRoleModel;
import io.jmix.security.model.ResourceRole;
import io.jmix.security.model.ResourceRoleModel;
import io.jmix.security.model.RoleModelConverter;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.securityflowui.component.rolefilter.RoleFilter;
import io.jmix.securityflowui.component.rolefilter.RoleFilterChangeEvent;
import io.jmix.securityflowui.util.RoleAssignmentCandidatePredicate;
import io.jmix.securityflowui.util.RoleHierarchyCandidatePredicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static io.jmix.securityflowui.util.PredicateUtils.combineRoleAssignmentPredicates;
import static io.jmix.securityflowui.util.PredicateUtils.combineRoleHierarchyPredicates;

@RouteAlias(value = "sec/resourcerolemodelslookup", layout = DefaultMainViewParent.class)
@Route(value = "sec/resource-role-models-lookup", layout = DefaultMainViewParent.class)
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
    @Autowired
    private EntityStates entityStates;

    @Autowired(required = false)
    protected List<RoleAssignmentCandidatePredicate> roleAssignmentCandidatePredicates = Collections.emptyList();
    @Autowired(required = false)
    protected List<RoleHierarchyCandidatePredicate> roleHierarchyCandidatePredicates = Collections.emptyList();

    protected RoleAssignmentCandidatePredicate compositeRoleAssignmentCandidatePredicate = (userDetails, baseRole) -> true;
    protected RoleHierarchyCandidatePredicate compositeRoleHierarchyCandidatePredicate = (baseRole, candidateRole) -> true;

    private List<String> excludedRolesCodes = Collections.emptyList();

    private UserDetails user;
    private BaseRoleModel currentRoleModel;

    @Subscribe
    public void onInit(InitEvent event) {
        initFilter();
        compositeRoleAssignmentCandidatePredicate = combineRoleAssignmentPredicates(roleAssignmentCandidatePredicates);
        compositeRoleHierarchyCandidatePredicate = combineRoleHierarchyPredicates(roleHierarchyCandidatePredicates);
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
                .filter(role -> {
                    boolean allowed = true;
                    if (currentRoleModel != null) {
                        // apply hierarchy predicates to find available base role candidates
                        boolean isNewRole = entityStates.isNew(currentRoleModel);
                        if (isNewRole) {
                            allowed = compositeRoleHierarchyCandidatePredicate.test(null, role);
                        } else if (currentRoleModel.getCode() != null) {
                            ResourceRole currentRole = roleRepository.findRoleByCode(currentRoleModel.getCode());
                            allowed = compositeRoleHierarchyCandidatePredicate.test(currentRole, role);
                        }
                    }
                    if (allowed && user != null) {
                        // apply user-based predicates to find roles available for user
                        allowed = compositeRoleAssignmentCandidatePredicate.test(user, role);
                    }
                    return allowed;
                })
                .map(roleModelConverter::createResourceRoleModel)
                .sorted(Comparator.comparing(ResourceRoleModel::getName))
                .collect(Collectors.toList());

        roleModelsDc.setItems(roleModels);
    }

    public void setExcludedRoles(List<String> excludedRolesCodes) {
        this.excludedRolesCodes = excludedRolesCodes;
    }

    public void setUser(UserDetails user) {
        this.user = user;
    }

    public void setCurrentRoleModel(BaseRoleModel currentRoleModel) {
        this.currentRoleModel = currentRoleModel;
    }
}
