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
package io.jmix.ui.component.presentation;

import com.vaadin.ui.*;
import io.jmix.core.AccessManager;
import io.jmix.core.EntityStates;
import io.jmix.core.Messages;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import io.jmix.ui.App;
import io.jmix.ui.AppUI;
import io.jmix.ui.Notifications;
import io.jmix.ui.accesscontext.UiGlobalPresentationContext;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.HasTablePresentations;
import io.jmix.ui.presentation.TablePresentations;
import io.jmix.ui.presentation.model.TablePresentation;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.settings.SettingsHelper;
import io.jmix.ui.settings.component.ComponentSettings;
import io.jmix.ui.settings.component.SettingsWrapperImpl;
import io.jmix.ui.settings.component.binder.ComponentSettingsBinder;
import io.jmix.ui.theme.ThemeConstants;
import io.jmix.ui.widget.JmixButton;
import io.jmix.ui.widget.JmixWindow;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Objects;
import java.util.UUID;

@org.springframework.stereotype.Component("ui_PresentationEditor")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PresentationEditor extends JmixWindow implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(PresentationEditor.class);

    protected TablePresentation presentation;
    protected ComponentSettingsBinder settingsBinder;

    protected FrameOwner frameOwner;

    protected HasTablePresentations component;
    protected TextField nameField;
    protected CheckBox autoSaveField;
    protected CheckBox defaultField;

    protected CheckBox globalField;

    protected boolean isNew;
    protected boolean allowGlobalPresentations;

    @Autowired
    protected Messages messages;
    @Autowired
    protected CurrentUserSubstitution currentUserSubstitution;
    @Autowired
    protected AccessManager accessManager;
    @Autowired
    protected EntityStates entityStates;

    public PresentationEditor(FrameOwner frameOwner, TablePresentation presentation, HasTablePresentations component,
                              ComponentSettingsBinder settingsBinder) {
        this.presentation = presentation;
        this.component = component;
        this.frameOwner = frameOwner;
        this.settingsBinder = settingsBinder;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        isNew = entityStates.isNew(presentation);

        UiGlobalPresentationContext globalPresentationContext = new UiGlobalPresentationContext();
        accessManager.applyRegisteredConstraints(globalPresentationContext);
        allowGlobalPresentations = globalPresentationContext.isPermitted();

        initWindow();
    }

    protected void initWindow() {
        initLayout();

        setWidthUndefined();

        String titleMessageKey = isNew ? "PresentationsEditor.new" : "PresentationsEditor.edit";
        setCaption(getMessage(titleMessageKey));

        setModal(true);
        setResizable(false);
    }

    protected void initLayout() {
        ThemeConstants theme = App.getInstance().getThemeConstants();

        VerticalLayout root = new VerticalLayout();
        root.setWidthUndefined();
        root.setSpacing(true);
        root.setMargin(false);
        setContent(root);

        nameField = new TextField(messages.getMessage("PresentationsEditor.name"));
        nameField.setWidth(theme.get("jmix.ui.PresentationEditor.name.width"));
        nameField.setValue(getPresentationCaption());
        root.addComponent(nameField);

        autoSaveField = new CheckBox();
        autoSaveField.setCaption(messages.getMessage("PresentationsEditor.autoSave"));
        autoSaveField.setValue(BooleanUtils.isTrue(presentation.getAutoSave()));
        root.addComponent(autoSaveField);

        defaultField = new CheckBox();
        defaultField.setCaption(messages.getMessage("PresentationsEditor.default"));
        defaultField.setValue(Objects.equals(EntityValues.<UUID>getId(presentation), (component.getDefaultPresentationId())));
        root.addComponent(defaultField);

        if (allowGlobalPresentations) {
            globalField = new CheckBox();
            globalField.setCaption(messages.getMessage("PresentationsEditor.global"));
            globalField.setValue(!isNew && presentation.getUsername() == null);
            root.addComponent(globalField);
        }

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setMargin(false);
        buttons.setSpacing(true);
        buttons.setWidthUndefined();
        root.addComponent(buttons);
        root.setComponentAlignment(buttons, Alignment.MIDDLE_LEFT);

        Button commitButton = new JmixButton(messages.getMessage("PresentationsEditor.save"));
        commitButton.addClickListener(event -> {
            if (validate()) {
                commit();
                forceClose();
            }
        });
        buttons.addComponent(commitButton);

        Button closeButton = new JmixButton(messages.getMessage("PresentationsEditor.close"));
        closeButton.addClickListener(event ->
                forceClose()
        );
        buttons.addComponent(closeButton);

        nameField.focus();
    }

    protected boolean validate() {
        TablePresentations presentations = component.getPresentations();

        //check that name is empty
        if (StringUtils.isEmpty(nameField.getValue()) && AppUI.getCurrent() != null) {
            AppUI.getCurrent().getNotifications()
                    .create(Notifications.NotificationType.HUMANIZED)
                    .withCaption(messages.getMessage("PresentationsEditor.error"))
                    .withDescription(messages.getMessage("PresentationsEditor.error.nameRequired"))
                    .show();
            return false;
        }

        //check that name is unique
        final TablePresentation pres = presentations.getPresentationByName(nameField.getValue());
        if (pres != null && !pres.equals(presentation) && AppUI.getCurrent() != null) {
            AppUI.getCurrent().getNotifications()
                    .create(Notifications.NotificationType.HUMANIZED)
                    .withCaption(messages.getMessage("PresentationsEditor.error"))
                    .withDescription(messages.getMessage("PresentationsEditor.error.nameAlreadyExists"))
                    .show();
            return false;
        }
        return true;
    }

    protected void commit() {
        TablePresentations presentations = component.getPresentations();

        String stringSettings = getStringSettings();
        presentation.setSettings(stringSettings);

        presentation.setName(nameField.getValue());
        presentation.setAutoSave(autoSaveField.getValue());
        presentation.setIsDefault(defaultField.getValue());

        UserDetails user = currentUserSubstitution.getEffectiveUser();

        boolean userOnly = !allowGlobalPresentations || !BooleanUtils.isTrue(globalField.getValue());
        presentation.setUsername(userOnly ? user.getUsername() : null);

        if (log.isTraceEnabled()) {
            log.trace(String.format("Presentation: %s", stringSettings));
        }

        if (isNew) {
            presentations.add(presentation);
        } else {
            presentations.modify(presentation);
        }
        presentations.commit();

        addCloseListener(e -> {
            if (isNew) {
                component.applyPresentation(EntityValues.<UUID>getId(presentation));
            }
        });
    }

    protected String getStringSettings() {
        ComponentSettings componentSettings = SettingsHelper.createSettings(settingsBinder.getSettingsClass());
        settingsBinder.saveSettings((Component) component, new SettingsWrapperImpl(componentSettings));

        return SettingsHelper.toSettingsString(componentSettings);
    }

    protected String getPresentationCaption() {
        return presentation.getName() == null ? "" : presentation.getName();
    }

    protected String getMessage(String key) {
        return messages.getMessage(key);
    }
}
