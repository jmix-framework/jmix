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
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.core.security.UserSubstitutionManager;
import io.jmix.ui.AppUI;
import io.jmix.ui.Dialogs;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.DialogAction;
import io.jmix.ui.component.*;
import io.jmix.ui.component.formatter.Formatter;
import io.jmix.ui.component.mainwindow.UserIndicator;
import io.jmix.ui.icon.Icons;
import org.springframework.beans.factory.annotation.Autowired;
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

    protected HasValue<UserDetails> userComponent;

    protected Formatter<? super UserDetails> userNameFormatter;

    public UserIndicatorImpl() {
        addCreateListener(this::onCreate);
    }

    protected void onCreate(CreateEvent createEvent) {
        root = createRootComponent();
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

    @Autowired(required = false)
    public void setSubstitutionManager(UserSubstitutionManager substitutionManager) {
        this.substitutionManager = substitutionManager;
    }

    @Autowired
    public void setDialogs(Dialogs dialogs) {
        this.dialogs = dialogs;
    }

    @Override
    public void refreshUser() {
        root.removeAll();

        CurrentAuthentication authentication = applicationContext.getBean(CurrentAuthentication.class);
        UserDetails user = authentication.getUser();

        List<UserDetails> currentAndSubstitutedUsers = new LinkedList<>();
        currentAndSubstitutedUsers.add(user);

        List<UserDetails> additionalUsers = substitutionManager != null
                ? substitutionManager.getSubstitutedUsers(user.getUsername()) : Collections.emptyList();

        currentAndSubstitutedUsers.addAll(additionalUsers);

        if (additionalUsers.size() > 0) {
            userComponent = createUserSelectionField(authentication, currentAndSubstitutedUsers);
        } else {
            userComponent = createUserIndicator(authentication.getUser());
        }
        root.add(userComponent);
        root.setDescription(generateUserCaption(user));

        adjustWidth();
        adjustHeight();
    }

    protected HasValue<UserDetails> createUserSelectionField(CurrentAuthentication authentication, List<UserDetails> currentAndSubstitutedUsers) {
        ComboBox<UserDetails> userCombobox = uiComponents.create(ComboBox.of(UserDetails.class));
        userCombobox.setOptionsList(currentAndSubstitutedUsers);
        userCombobox.setStyleName("jmix-user-select-combobox");
        userCombobox.setOptionCaptionProvider(this::generateUserCaption);
        userCombobox.setNullOptionVisible(false);
        userCombobox.setValue(authentication.getCurrentOrSubstitutedUser());

        userCombobox.addValueChangeListener(valueChangedEvent -> {
            Objects.requireNonNull(valueChangedEvent.getPrevValue());
            Objects.requireNonNull(valueChangedEvent.getValue());

            if (authentication.getCurrentOrSubstitutedUser().equals(valueChangedEvent.getValue())) {
                return;
            }

            SubstituteUserAction substituteUserAction = new SubstituteUserAction(valueChangedEvent.getValue(),
                    valueChangedEvent.getPrevValue(),
                    messages,
                    icons,
                    substitutionManager)
                    .withCancelAction(this::revertSelection);

            dialogs.createOptionDialog()
                    .withCaption(messages.getMessage("substitutionConfirmation.caption"))
                    .withMessage(messages.formatMessage("", "substitutionConfirmation.message",
                            generateUserCaption(valueChangedEvent.getValue())))
                    .withActions(
                            substituteUserAction,
                            new DialogAction(DialogAction.Type.CANCEL, Action.Status.PRIMARY)
                                    .withHandler(event -> revertSelection(valueChangedEvent.getPrevValue()))
                    )
                    .show();
        });

        return userCombobox;
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
}
