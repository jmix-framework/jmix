/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.component.mainwindow.impl;

import com.vaadin.ui.Component;
import io.jmix.core.Messages;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.security.UserRepository;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import io.jmix.core.usersubstitution.UserSubstitutionManager;
import io.jmix.core.usersubstitution.event.UiUserSubstitutionsChangedEvent;
import io.jmix.ui.AppUI;
import io.jmix.ui.Dialogs;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.DialogAction;
import io.jmix.ui.component.*;
import io.jmix.ui.component.formatter.Formatter;
import io.jmix.ui.component.mainwindow.UserIndicator;
import io.jmix.ui.icon.Icons;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class UserIndicatorImpl extends CompositeComponent<CssLayout> implements UserIndicator {

    protected static final String USER_INDICATOR_STYLENAME = "jmix-userindicator";

    protected MetadataTools metadataTools;
    protected UiComponents uiComponents;
    protected Messages messages;
    protected Icons icons;
    protected UserSubstitutionManager substitutionManager;
    protected Dialogs dialogs;
    protected CurrentUserSubstitution currentUserSubstitution;
    protected UserRepository userRepository;

    protected HasValue<UserDetails> userComponent;

    protected Formatter<? super UserDetails> userNameFormatter;

    public UserIndicatorImpl() {
        addCreateListener(this::onCreate);
    }

    protected void onCreate(CreateEvent createEvent) {
        CssLayout root = createRootComponent();
        setComposition(root);
        initRootComponent(root);
    }

    protected CssLayout createRootComponent() {
        return uiComponents.create(CssLayout.class);
    }

    protected void initRootComponent(CssLayout root) {
        root.unwrap(Component.class).setPrimaryStyleName(USER_INDICATOR_STYLENAME);
    }

    @Autowired
    public void setMetadataTools(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @Autowired
    public void setUiComponents(UiComponents uiComponents) {
        this.uiComponents = uiComponents;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setIcons(Icons icons) {
        this.icons = icons;
    }

    @Autowired
    public void setCurrentUserSubstitution(CurrentUserSubstitution currentUserSubstitution) {
        this.currentUserSubstitution = currentUserSubstitution;
    }

    @Autowired(required = false)
    public void setSubstitutionManager(UserSubstitutionManager substitutionManager) {
        this.substitutionManager = substitutionManager;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setDialogs(Dialogs dialogs) {
        this.dialogs = dialogs;
    }

    @Override
    public void refreshUser() {
        root.removeAll();

        UserDetails user = currentUserSubstitution.getAuthenticatedUser();
        UserDetails updatedUser = userRepository.loadUserByUsername(user.getUsername());

        List<UserDetails> currentAndSubstitutedUsers = new LinkedList<>();
        currentAndSubstitutedUsers.add(updatedUser);

        List<UserDetails> additionalUsers = substitutionManager != null
                ? substitutionManager.getCurrentSubstitutedUsers() : Collections.emptyList();

        currentAndSubstitutedUsers.addAll(additionalUsers);

        if (!additionalUsers.isEmpty()) {
            userComponent = createUserSelectionField(currentAndSubstitutedUsers);
        } else {
            userComponent = createUserIndicator(updatedUser);
        }
        root.add(userComponent);
        root.setDescription(generateUserCaption(updatedUser));

        adjustWidth();
        adjustHeight();
    }

    protected HasValue<UserDetails> createUserSelectionField(List<UserDetails> currentAndSubstitutedUsers) {
        ComboBox<UserDetails> userCombobox = uiComponents.create(ComboBox.of(UserDetails.class));
        userCombobox.setOptionsList(currentAndSubstitutedUsers);
        userCombobox.setStyleName("jmix-user-select-combobox");
        userCombobox.setOptionCaptionProvider(this::generateUserCaption);
        userCombobox.setNullOptionVisible(false);
        userCombobox.setValue(currentUserSubstitution.getEffectiveUser());

        userCombobox.addValueChangeListener(this::substituteUser);

        return userCombobox;
    }

    protected void substituteUser(HasValue.ValueChangeEvent<UserDetails> valueChangedEvent) {
        UserDetails newUser = valueChangedEvent.getValue();
        UserDetails prevUser = valueChangedEvent.getPrevValue();

        if (newUser == null || prevUser == null) {//should not happen
            return;
        }

        if (currentUserSubstitution.getEffectiveUser().equals(newUser)) {
            return;
        }

        SubstituteUserAction substituteUserAction = new SubstituteUserAction(newUser,
                prevUser,
                messages,
                icons,
                substitutionManager)
                .withCancelAction(this::revertSelection);
        substituteUserAction.setPrimary(true);

        dialogs.createOptionDialog()
                .withCaption(messages.getMessage("substitutionConfirmation.caption"))
                .withMessage(messages.formatMessage("", "substitutionConfirmation.message",
                        generateUserCaption(newUser)))
                .withActions(
                        substituteUserAction,
                        new DialogAction(DialogAction.Type.CANCEL)
                                .withHandler(event -> revertSelection(prevUser))
                )
                .show();
    }

    protected HasValue<UserDetails> createUserIndicator(UserDetails user) {
        Label<UserDetails> userNameLabel = uiComponents.create(Label.of(UserDetails.class));
        userNameLabel.setStyleName("jmix-user-select-label");
        userNameLabel.setFormatter(this::generateUserCaption);
        userNameLabel.setValue(user);

        AppUI ui = AppUI.getCurrent();
        if (ui != null && ui.isTestMode()) {
            userNameLabel.unwrap(Component.class).setJTestId("currentUserLabel");
        }
        return userNameLabel;
    }

    protected void revertSelection(UserDetails oldUser) {
        userComponent.setValue(oldUser);
    }

    protected String generateUserCaption(UserDetails user) {
        if (userNameFormatter != null) {
            return userNameFormatter.apply(user);
        } else if (EntityValues.isEntity(user)) {
            return metadataTools.getInstanceName(user);
        } else {
            return user.getUsername();
        }
    }

    @Override
    public void setWidth(@Nullable String width) {
        super.setWidth(width);

        adjustWidth();
    }

    @Override
    public void setHeight(@Nullable String height) {
        super.setHeight(height);

        adjustHeight();
    }

    protected void adjustWidth() {
        if (getWidth() < 0) {
            if (userComponent != null) {
                userComponent.setWidthAuto();
            }
        } else {
            if (userComponent != null) {
                userComponent.setWidthFull();
            }
        }
    }

    protected void adjustHeight() {
        if (getHeight() < 0) {
            if (userComponent != null) {
                userComponent.setHeightAuto();
            }
        } else {
            if (userComponent != null) {
                userComponent.setHeightFull();
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public Formatter<UserDetails> getFormatter() {
        return (Formatter<UserDetails>) userNameFormatter;
    }

    @Override
    public void setFormatter(@Nullable Formatter<? super UserDetails> formatter) {
        this.userNameFormatter = formatter;
        refreshUser();
    }

    @EventListener
    protected void onUserSubstitutionsChanged(UiUserSubstitutionsChangedEvent event) {
        UserDetails authenticatedUser = currentUserSubstitution.getAuthenticatedUser();
        if (Objects.equals(authenticatedUser.getUsername(), event.getSource())) {
            refreshUser();
        }
    }
}
