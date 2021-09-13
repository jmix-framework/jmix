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
import io.jmix.ui.AppUI;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.DialogAction;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.CompositeComponent;
import io.jmix.ui.component.CssLayout;
import io.jmix.ui.component.Label;
import io.jmix.ui.component.formatter.Formatter;
import io.jmix.ui.component.mainwindow.UserIndicator;
import io.jmix.ui.icon.Icons;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Nullable;
import java.util.*;

public class UserIndicatorImpl extends CompositeComponent<CssLayout> implements UserIndicator {

    protected static final String USER_INDICATOR_STYLENAME = "jmix-userindicator";

    protected MetadataTools metadataTools;
    protected UiComponents uiComponents;
    protected Messages messages;
    protected Icons icons;


    protected Label<UserDetails> userNameLabel;
    protected ComboBox<UserDetails> userCombobox;


    protected List<UserDetails> additionalUsers = new LinkedList<>();
    protected List<SubstituteUserAction.SubstituteStep> substituteSteps = new LinkedList<>();


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

    @Override
    public void refreshUser() {
        root.removeAll();

        CurrentAuthentication authentication = applicationContext.getBean(CurrentAuthentication.class);
        UserDetails user = authentication.getUser();

        List<UserDetails> currentAndSubstitutedUsers = new LinkedList<>();
        currentAndSubstitutedUsers.add(user);
        currentAndSubstitutedUsers.addAll(additionalUsers);

        if (additionalUsers.size() > 0) {
            userNameLabel = null;
            userCombobox = uiComponents.create(ComboBox.of(UserDetails.class));
            userCombobox.setOptionsList(currentAndSubstitutedUsers);
            userCombobox.setStyleName("jmix-user-select-label");
            userCombobox.setWidth("150px");
            userCombobox.setOptionCaptionProvider(this::generateUserCaption);
            userCombobox.setNullOptionVisible(false);
            userCombobox.setValue(user);

            userCombobox.addValueChangeListener(valueChangedEvent -> {
                Objects.requireNonNull(valueChangedEvent.getPrevValue());
                Objects.requireNonNull(valueChangedEvent.getValue());

                if (authentication.getCurrentOrSubstitutedUser().equals(valueChangedEvent.getValue())) {
                    return;
                }

                SubstituteUserAction substituteUserAction = new SubstituteUserAction(valueChangedEvent.getValue(),
                        valueChangedEvent.getPrevValue(),
                        messages,
                        icons)
                        .withCancelStep(this::revertSelection);

                substituteSteps.forEach(substituteUserAction::withSubstituteStep);

                AppUI.getCurrent().getDialogs().createOptionDialog()
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

            root.add(userCombobox);
        } else {
            userCombobox = null;
            userNameLabel = uiComponents.create(Label.of(UserDetails.class));
            userNameLabel.setStyleName("jmix-user-select-label");
            userNameLabel.setFormatter(this::generateUserCaption);
            userNameLabel.setValue(user);

            AppUI ui = AppUI.getCurrent();
            if (ui != null && ui.isTestMode()) {
                userNameLabel.unwrap(Component.class).setJTestId("currentUserLabel");
            }

            root.add(userNameLabel);
        }
        root.setDescription(generateUserCaption(user));

        adjustWidth();
        adjustHeight();
    }

    protected void revertSelection(UserDetails oldUser) {
        userCombobox.setValue(oldUser);
    }

    @Override
    public void setAdditionalUsers(Collection<UserDetails> additionalUsers) {
        this.additionalUsers = new ArrayList<>(additionalUsers);
    }

    @Override
    public void addSubstituteStep(SubstituteUserAction.SubstituteStep step) {
        substituteSteps.add(step);
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
            if (userNameLabel != null) {
                userNameLabel.setWidthAuto();
            }
        } else {
            if (userNameLabel != null) {
                userNameLabel.setWidthFull();
            }
        }
    }

    protected void adjustHeight() {
        if (getHeight() < 0) {
            if (userNameLabel != null) {
                userNameLabel.setHeightAuto();
            }
        } else {
            if (userNameLabel != null) {
                userNameLabel.setHeightFull();
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
