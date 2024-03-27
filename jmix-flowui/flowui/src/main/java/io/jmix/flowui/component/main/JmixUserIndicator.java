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

package io.jmix.flowui.component.main;

import com.google.common.base.Strings;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.server.VaadinSession;
import io.jmix.core.Messages;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.security.UserRepository;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import io.jmix.core.usersubstitution.UserSubstitutionManager;
import io.jmix.core.usersubstitution.event.UiUserSubstitutionsChangedEvent;
import io.jmix.flowui.Actions;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.action.security.SubstituteUserAction;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.component.main.UserIndicator;
import io.jmix.flowui.sys.event.UiEventsManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class JmixUserIndicator extends UserIndicator<UserDetails> implements ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;

    protected CurrentUserSubstitution currentUserSubstitution;
    protected UserSubstitutionManager substitutionManager;

    protected MetadataTools metadataTools;
    protected UiComponents uiComponents;
    protected Dialogs dialogs;
    protected Messages messages;
    protected Actions actions;
    protected UserRepository userRepository;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        autowireDependencies();

        initUiUserSubstitutionChangeListener();
    }

    protected void autowireDependencies() {
        currentUserSubstitution = applicationContext.getBean(CurrentUserSubstitution.class);
        substitutionManager = applicationContext.getBeanProvider(UserSubstitutionManager.class)
                .getIfAvailable();
        metadataTools = applicationContext.getBean(MetadataTools.class);
        uiComponents = applicationContext.getBean(UiComponents.class);
        dialogs = applicationContext.getBean(Dialogs.class);
        messages = applicationContext.getBean(Messages.class);
        actions = applicationContext.getBean(Actions.class);
        userRepository = applicationContext.getBean(UserRepository.class);
    }

    protected void initUiUserSubstitutionChangeListener() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            return;
        }

        UiEventsManager uiEventsManager = session.getAttribute(UiEventsManager.class);

        if (uiEventsManager != null) {
            uiEventsManager.addApplicationListener(this, this::onApplicationEvent);

            // Remove on detach event
            addDetachListener(event -> uiEventsManager.removeApplicationListeners(this));
        }
    }

    @Override
    public void refreshUser() {
        getContent().removeAll();

        UserDetails user = currentUserSubstitution.getAuthenticatedUser();
        UserDetails updatedUser = userRepository.loadUserByUsername(user.getUsername());

        List<UserDetails> currentAndSubstitutedUsers = new LinkedList<>();
        currentAndSubstitutedUsers.add(updatedUser);

        List<UserDetails> additionalUsers = substitutionManager != null
                ? substitutionManager.getCurrentSubstitutedUsers()
                : Collections.emptyList();
        currentAndSubstitutedUsers.addAll(additionalUsers);

        userComponent = !additionalUsers.isEmpty()
                ? createUserSelectionField(currentAndSubstitutedUsers)
                : createUserIndicator();

        updateUserIndicatorLabel(currentUserSubstitution.getEffectiveUser());

        getContent().add(userComponent);
        getContent().setTitle(generateUserTitle(updatedUser));
    }

    protected Component createUserSelectionField(List<UserDetails> currentAndSubstitutedUsers) {
        //noinspection unchecked
        JmixComboBox<UserDetails> userComboBox = uiComponents.create(JmixComboBox.class);
        userComboBox.setWidthFull();

        userComboBox.setItems(currentAndSubstitutedUsers);
        userComboBox.setRenderer(new TextRenderer<>(this::generateUserTitle));
        userComboBox.setValue(currentUserSubstitution.getEffectiveUser());

        userComboBox.addValueChangeListener(this::substituteUser);

        return userComboBox;
    }

    protected void substituteUser(ComponentValueChangeEvent<ComboBox<UserDetails>, UserDetails> event) {
        UserDetails newUser = event.getValue();
        UserDetails prevUser = event.getOldValue();

        if (newUser == null || prevUser == null) {
            return;
        }

        if (currentUserSubstitution.getEffectiveUser().equals(newUser)) {
            return;
        }

        dialogs.createOptionDialog()
                .withHeader(messages.getMessage(getClass(), "substitutionConfirmation.header"))
                .withText(messages.formatMessage(getClass(), "substitutionConfirmation.text",
                        generateUserTitle(newUser)))
                .withActions(
                        ((SubstituteUserAction) actions.create(SubstituteUserAction.ID))
                                .withUsers(prevUser, newUser)
                                .withCancelHandler(this::updateUserIndicatorLabel)
                                .withText(messages.getMessage("actions.Ok"))
                                .withIcon(VaadinIcon.CHECK)
                                .withVariant(ActionVariant.PRIMARY),
                        new DialogAction(DialogAction.Type.CANCEL)
                                .withHandler(cancelEvent -> updateUserIndicatorLabel(prevUser))
                )
                .open();
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void updateUserIndicatorLabel(UserDetails user) {
        if (userComponent instanceof HasText hasTextComponent) {
            String userTitle = generateUserTitle(user);
            hasTextComponent.setText(userTitle);
        } else if (userComponent instanceof HasValue hasValueComponent) {
            UiComponentUtils.setValue(hasValueComponent, user);
        }
    }

    protected String generateUserTitle(UserDetails user) {
        String userTitle = super.generateUserTitle(user);

        if (!Strings.isNullOrEmpty(userTitle)) {
            return userTitle;
        } else if (EntityValues.isEntity(user)) {
            return metadataTools.getInstanceName(user);
        } else {
            return user.getUsername();
        }
    }

    protected void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof UiUserSubstitutionsChangedEvent uiUserSubstitutionsChangedEvent) {
            onUserSubstitutionsChanged(uiUserSubstitutionsChangedEvent);
        }
    }

    protected void onUserSubstitutionsChanged(UiUserSubstitutionsChangedEvent event) {
        UserDetails authenticatedUser = currentUserSubstitution.getAuthenticatedUser();
        if (Objects.equals(authenticatedUser.getUsername(), event.getSource())) {
            refreshUser();
        }
    }
}
