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

import io.jmix.core.EntityStates;
import io.jmix.core.MetadataTools;
import io.jmix.core.security.UserRepository;
import io.jmix.securitydata.entity.UserSubstitutionEntity;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.SuggestionField;
import io.jmix.ui.component.TextField;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UiController("sec_UserSubstitutionEntity.edit")
@UiDescriptor("user-substitution-edit.xml")
@EditedEntityContainer("userSubstitutionDc")
public class UserSubstitutionEdit extends StandardEditor<UserSubstitutionEntity> {

    @Autowired
    protected EntityStates entityStates;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected SuggestionField<String> substitutedUsernameField;

    @Autowired
    private TextField<String> usernameField;

    @Autowired
    private DataContext dataContext;

    public void setParentDataContext(DataContext parentDataContext) {
        dataContext.setParent(parentDataContext);
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        usernameField.setValue(getEditedEntity().getUsername());
        substitutedUsernameField.setSearchExecutor((searchString, searchParams) -> {
            List<? extends UserDetails> users = userRepository.getByUsernameLike(searchString);
            return users.stream()
                    .map(UserDetails::getUsername)
                    .collect(Collectors.toList());
        });
        substitutedUsernameField.setEditable(entityStates.isNew(getEditedEntity()));
    }
}
