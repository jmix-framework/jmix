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

package io.jmix.securityflowui.view.usersubstitution;

import com.vaadin.flow.data.provider.Query;
import io.jmix.core.EntityStates;
import io.jmix.core.LoadContext;
import io.jmix.core.SaveContext;
import io.jmix.core.security.UserRepository;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.view.*;
import io.jmix.security.usersubstitution.UserSubstitutionModel;
import io.jmix.security.usersubstitution.UserSubstitutionPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@ViewController("sec_UserSubstitution.detail")
@ViewDescriptor("user-substitution-detail-view.xml")
@EditedEntityContainer("userSubstitutionDc")
@DialogMode(width = "37.5em")
public class UserSubstitutionDetailView extends StandardDetailView<UserSubstitutionModel> {

    @ViewComponent
    protected JmixComboBox<String> substitutedUsernameField;

    @Autowired
    protected EntityStates entityStates;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected MessageBundle messagesBundle;
    @Autowired(required = false)
    protected UserSubstitutionPersistence userSubstitutionPersistence;

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

    @Install(to = "userSubstitutionDl", target = Target.DATA_LOADER)
    protected UserSubstitutionModel userSubstitutionDlLoadDelegate(final LoadContext<UserSubstitutionModel> loadContext) {
        return getUserSubstitutionService().load((UUID) loadContext.getId());
    }

    @Install(target = Target.DATA_CONTEXT)
    protected Set<Object> saveDelegate(final SaveContext saveContext) {
        UserSubstitutionModel saved = getUserSubstitutionService().save(getEditedEntity());
        return Set.of(saved);
    }

    protected UserSubstitutionPersistence getUserSubstitutionService() {
        if (userSubstitutionPersistence == null) {
            throw new IllegalStateException("UserSubstitutionPersistence is not available");
        }
        return userSubstitutionPersistence;
    }
}
