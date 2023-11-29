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

package io.jmix.localusermanagementflowui.view.usersubstitution;

import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Route;
import io.jmix.core.MetadataTools;
import io.jmix.core.security.UserRepository;
import io.jmix.core.usersubstitution.event.UserSubstitutionsChangedEvent;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.view.*;
import io.jmix.localusermanagement.entity.UserSubstitutionEntity;
import io.jmix.localusermanagementflowui.security.UserManagementRole;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@RolesAllowed(UserManagementRole.CODE)
@Route(value = "sec/usersubstitution/:username", layout = DefaultMainViewParent.class)
@ViewController("sec_UserSubstitution.view")
@ViewDescriptor("user-substitution-view.xml")
@DialogMode(width = "64em")
public class UserSubstitutionView extends StandardView {

    public static final String ROUTE_PARAM_NAME = "username";

    @ViewComponent
    protected CollectionLoader<UserSubstitutionEntity> userSubstitutionsDl;

    @Autowired
    protected MessageBundle messageBundle;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected ViewValidation viewValidation;

    protected String username;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        username = event.getRouteParameters().get(ROUTE_PARAM_NAME)
                .orElseThrow(() ->
                        new IllegalStateException(String.format("Entity '%s' not found", ROUTE_PARAM_NAME)));
        userSubstitutionsDl.setParameter("username", username);

        super.beforeEnter(event);
    }

    @Override
    public String getPageTitle() {
        return String.format(
                messageBundle.getMessage("userSubstitutionView.title"),
                metadataTools.getInstanceName(userRepository.loadUserByUsername(username))
        );
    }

    @Subscribe("saveAction")
    protected void onSaveAction(ActionPerformedEvent event) {
        getViewData().getDataContext().save();
        close(StandardOutcome.SAVE);
    }

    @Subscribe("closeAction")
    protected void onCloseAction(ActionPerformedEvent event) {
        if (getViewData().getDataContext().hasChanges()) {
            viewValidation.showUnsavedChangesDialog(this)
                    .onDiscard(() -> close(StandardOutcome.DISCARD));
        } else {
            closeWithDefaultAction();
        }
    }

    @Install(to = "substitutionDataGrid.create", subject = "initializer")
    protected void substitutionDataGridCreateInitializer(UserSubstitutionEntity userSubstitution) {
        userSubstitution.setUsername(username);
    }

    @Supply(to = "substitutionDataGrid.substitutedUsername", subject = "renderer")
    protected Renderer<UserSubstitutionEntity> substitutedUsernameRenderer() {
        return new TextRenderer<>(substitution -> {
            String userRepresentation;
            try {
                UserDetails user = userRepository.loadUserByUsername(substitution.getSubstitutedUsername());
                userRepresentation = metadataTools.getInstanceName(user);
            } catch (UsernameNotFoundException e) {
                userRepresentation = substitution.getSubstitutedUsername();
            }

            return userRepresentation;
        });
    }

    @Subscribe(target = Target.DATA_CONTEXT)
    public void onPostSave(DataContext.PostSaveEvent postCommitEvent) {
        UserSubstitutionsChangedEvent event = new UserSubstitutionsChangedEvent(username);
        getApplicationContext().publishEvent(event);
    }
}
