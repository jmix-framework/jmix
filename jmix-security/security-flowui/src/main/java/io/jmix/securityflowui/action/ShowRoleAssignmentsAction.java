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

package io.jmix.securityflowui.action;

import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.Messages;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.AdjustWhenViewReadOnly;
import io.jmix.flowui.action.list.SecuredListDataComponentAction;
import io.jmix.flowui.data.EntityDataUnit;
import io.jmix.flowui.kit.component.FlowuiComponentUtils;
import io.jmix.flowui.view.navigation.RouteSupport;
import io.jmix.securityflowui.view.roleassignment.RoleAssignmentView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

@ActionType(ShowRoleAssignmentsAction.ID)
public class ShowRoleAssignmentsAction<E extends UserDetails>
        extends SecuredListDataComponentAction<ShowRoleAssignmentsAction<E>, E>
        implements AdjustWhenViewReadOnly {

    public static final String ID = "showRoleAssignments";

    protected ViewNavigators viewNavigators;
    protected RouteSupport routeSupport;

    public ShowRoleAssignmentsAction() {
        this(ID);
    }

    public ShowRoleAssignmentsAction(String id) {
        super(id);
    }

    @Override
    protected void initAction() {
        super.initAction();

        this.icon = FlowuiComponentUtils.convertToIcon(VaadinIcon.SHIELD);
    }

    @Autowired
    public void setViewNavigators(ViewNavigators viewNavigators) {
        this.viewNavigators = viewNavigators;
    }

    @Autowired
    public void setRouteSupport(RouteSupport routeSupport) {
        this.routeSupport = routeSupport;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.ShowRoleAssignments");
    }

    @Override
    public void execute() {
        checkTarget();
        checkTargetItems(EntityDataUnit.class);

        E selectedItem = target.getSingleSelectedItem();
        if (selectedItem == null) {
            throw new IllegalStateException(String.format("There is not selected item in %s target",
                    getClass().getSimpleName()));
        }

        navigate(selectedItem);
    }

    protected void navigate(E selectedItem) {
        viewNavigators.view(RoleAssignmentView.class)
                .withRouteParameters(routeSupport.createRouteParameters("username", selectedItem.getUsername()))
                .withBackwardNavigation(true)
                .navigate();
    }
}
