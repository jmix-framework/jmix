/*
 * Copyright 2023 Haulmont.
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

import com.google.common.base.Preconditions;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.accesscontext.UiEntityContext;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.AdjustWhenViewReadOnly;
import io.jmix.flowui.action.list.SecuredListDataComponentAction;
import io.jmix.flowui.data.EntityDataUnit;
import io.jmix.flowui.view.navigation.RouteSupport;
import io.jmix.securitydata.entity.UserSubstitutionEntity;
import io.jmix.securityflowui.view.usersubstitution.UserSubstitutionView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Standard action for displaying the user substitutions. Opens {@link UserSubstitutionView} that provides CRUD
 * operations for the user substitutions.
 * <p>
 * Should be defined for a list component ({@code Table}, {@code DataGrid}, etc.).
 * @param <E> type of the user entity
 */
@ActionType(ShowUserSubstitutionsAction.ID)
public class ShowUserSubstitutionsAction<E extends UserDetails>
        extends SecuredListDataComponentAction<ShowUserSubstitutionsAction<E>, E>
        implements AdjustWhenViewReadOnly {

    public static final String ID = "sec_showUserSubstitutions";

    protected ViewNavigators viewNavigators;
    protected RouteSupport routeSupport;

    public ShowUserSubstitutionsAction() {
        this(ID);
    }

    public ShowUserSubstitutionsAction(String id) {
        super(id);
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
    protected void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.showUserSubstitutions");
    }

    @Override
    protected boolean isPermitted() {
        if (target == null || !(target.getItems() instanceof EntityDataUnit entityDataUnitItems)) {
            return false;
        }

        MetaClass metaClass = entityDataUnitItems.getEntityMetaClass();
        UiEntityContext entityContext = new UiEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(entityContext);

        if (!entityContext.isViewPermitted()) {
            return false;
        }

        UiEntityContext userSubstitutionContext = new UiEntityContext(metadata.getClass(UserSubstitutionEntity.class));
        accessManager.applyRegisteredConstraints(userSubstitutionContext);

        if (!userSubstitutionContext.isViewPermitted()) {
            return false;
        }

        return super.isPermitted();
    }

    @Override
    public void execute() {
        checkTarget();
        checkTargetItems(EntityDataUnit.class);

        E selectedItem = target.getSingleSelectedItem();
        Preconditions.checkState(selectedItem != null,
                "There is not selected item in %s target",
                getClass().getSimpleName());

        navigate(selectedItem);
    }

    protected void navigate(E selectedItem) {
        viewNavigators.view(UserSubstitutionView.class)
                .withRouteParameters(routeSupport.createRouteParameters("username", selectedItem.getUsername()))
                .withBackwardNavigation(true)
                .navigate();
    }
}
