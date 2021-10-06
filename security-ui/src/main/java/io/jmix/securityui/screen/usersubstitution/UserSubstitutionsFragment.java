/*
 * Copyright 2021 Haulmont.
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

package io.jmix.securityui.screen.usersubstitution;

import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.security.UserRepository;
import io.jmix.securitydata.entity.UserSubstitutionEntity;
import io.jmix.ui.action.list.CreateAction;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Table;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.inject.Named;

@UiController("sec_UserSubstitutionsFragment")
@UiDescriptor("user-substitutions-fragment.xml")
public class UserSubstitutionsFragment extends ScreenFragment {

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected CollectionLoader<UserSubstitutionEntity> userSubstitutionsDl;

    @Autowired
    protected DataContext dataContext;


    @Named("substitutionTable.create")
    protected CreateAction<UserSubstitutionEntity> createAction;

    protected UserDetails user;

    @Subscribe(target = Target.PARENT_CONTROLLER)
    public void onAfterInit(Screen.AfterInitEvent event) {
        createAction.setScreenConfigurer(screen -> {
            UserSubstitutionEntity substitution = metadata.create(UserSubstitutionEntity.class);
            substitution.setUsername(user.getUsername());
            ((UserSubstitutionEdit) screen).setEntityToEdit(substitution);
            ((UserSubstitutionEdit) screen).setParentDataContext(dataContext);
        });
    }

    @Subscribe(target = Target.PARENT_CONTROLLER)
    public void onAfterShow(Screen.AfterShowEvent event) {
        userSubstitutionsDl.setParameter("username", user.getUsername());
        userSubstitutionsDl.load();

    }

    public void setUser(UserDetails user) {
        this.user = user;
    }

    @Install(to = "substitutionTable.substitutedUsername", subject = "columnGenerator")
    public Component buildUserCaption(UserSubstitutionEntity substitution) {
        String userRepresentation;
        try {
            userRepresentation = metadataTools.getInstanceName(userRepository.loadUserByUsername(substitution.getSubstitutedUsername()));
        } catch (UsernameNotFoundException e) {
            userRepresentation = substitution.getSubstitutedUsername();
        }

        return new Table.PlainTextCell(userRepresentation);
    }
}
