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
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.AppUI;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.CompositeComponent;
import io.jmix.ui.component.CssLayout;
import io.jmix.ui.component.Label;
import io.jmix.ui.component.formatter.Formatter;
import io.jmix.ui.component.mainwindow.UserIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Nullable;

public class UserIndicatorImpl extends CompositeComponent<CssLayout> implements UserIndicator {

    protected static final String USER_INDICATOR_STYLENAME = "c-userindicator";

    protected MetadataTools metadataTools;
    protected UiComponents uiComponents;

    protected Label<UserDetails> userNameLabel;

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

    @Override
    public void refreshUser() {
        root.removeAll();

        CurrentAuthentication authentication = applicationContext.getBean(CurrentAuthentication.class);
        UserDetails user = authentication.getUser();

        userNameLabel = uiComponents.create(Label.of(UserDetails.class));
        userNameLabel.setStyleName("c-user-select-label");
        userNameLabel.setFormatter(this::generateUserCaption);
        userNameLabel.setValue(user);

        AppUI ui = AppUI.getCurrent();
        if (ui != null && ui.isTestMode()) {
            userNameLabel.unwrap(Component.class).setJTestId("currentUserLabel");
        }

        root.add(userNameLabel);
        root.setDescription(generateUserCaption(user));

        adjustWidth();
        adjustHeight();
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
