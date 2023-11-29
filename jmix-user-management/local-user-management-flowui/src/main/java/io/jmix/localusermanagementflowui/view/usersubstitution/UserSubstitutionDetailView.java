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

import com.vaadin.flow.data.provider.Query;
import io.jmix.core.EntityStates;
import io.jmix.core.security.UserRepository;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.view.*;
import io.jmix.localusermanagement.entity.UserSubstitutionEntity;
import io.jmix.localusermanagementflowui.security.UserManagementRole;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.stream.Stream;

@RolesAllowed(UserManagementRole.CODE)
@ViewController("sec_UserSubstitution.detail")
@ViewDescriptor("user-substitution-detail-view.xml")
@EditedEntityContainer("userSubstitutionDc")
@DialogMode(width = "37.5em")
public class UserSubstitutionDetailView extends StandardDetailView<UserSubstitutionEntity> {

    @ViewComponent
    protected JmixComboBox<String> substitutedUsernameField;

    @Autowired
    protected EntityStates entityStates;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected MessageBundle messagesBundle;

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        substitutedUsernameField.setReadOnly(!entityStates.isNew(getEditedEntity()));
    }

    @Install(to = "substitutedUsernameField", subject = "itemsFetchCallback")
    protected Stream<String> substitutedUsernameFieldItemsFetchCallback(Query<String, String> query) {
        String enteredValue = query.getFilter()
                .orElse("");

        return userRepository.getByUsernameLike(enteredValue).stream()
                .map(UserDetails::getUsername)
                .filter(name -> !name.equals(getEditedEntity().getUsername()))
                .skip(query.getOffset())
                .limit(query.getLimit());
    }

    @Subscribe
    protected void onValidation(ValidationEvent event) {
        Date startDate = getEditedEntity().getStartDate();
        Date endDate = getEditedEntity().getEndDate();

        if (startDate != null && endDate != null && startDate.after(endDate)) {
            event.getErrors().add(messagesBundle.getMessage("userSubstitutionDetailView.dateOrderError"));
        }
    }
}
