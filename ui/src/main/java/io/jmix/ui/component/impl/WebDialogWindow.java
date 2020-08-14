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

package io.jmix.ui.component.impl;

import com.vaadin.event.Action;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Component;
import org.springframework.context.ApplicationContext;
import io.jmix.core.Messages;
import io.jmix.ui.AppUI;
import io.jmix.ui.UiProperties;
import io.jmix.ui.component.*;
import io.jmix.ui.icon.IconResolver;
import io.jmix.ui.screen.StandardCloseAction;
import io.jmix.ui.theme.ThemeConstants;
import io.jmix.ui.theme.ThemeConstantsManager;
import io.jmix.ui.widget.JmixWindow;
import io.jmix.ui.widget.ShortcutListenerDelegate;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static io.jmix.ui.component.impl.WebWrapperUtils.toSizeUnit;

public class WebDialogWindow extends WebWindow implements DialogWindow, InitializingBean {
    protected JmixWindow dialogWindow;

    protected ApplicationContext applicationContext;

    public WebDialogWindow() {
        this.dialogWindow = new GuiDialogWindow(this);
        this.dialogWindow.setContent(component);
        this.dialogWindow.addPreCloseListener(this::onCloseButtonClick);
    }

    @Override
    public void afterPropertiesSet() {
        setupDialogShortcuts();
        setupContextMenu();
        setupDefaultSize();
    }

    protected void setupDefaultSize() {
        ThemeConstantsManager themeConstantsManager = (ThemeConstantsManager) applicationContext.getBean(ThemeConstantsManager.NAME);
        ThemeConstants theme = themeConstantsManager.getConstants();

        dialogWindow.setWidth(theme.get("cuba.web.WebWindowManager.dialog.width"));
        dialogWindow.setHeightUndefined();

        component.setWidth(100, Sizeable.Unit.PERCENTAGE);
        component.setHeightUndefined();
    }

    protected void setupContextMenu() {
        dialogWindow.addContextActionHandler(new DialogWindowActionHandler());
    }

    protected void setupDialogShortcuts() {
        String closeShortcut = getUiProperties().getCloseShortcut();
        KeyCombination closeCombination = KeyCombination.create(closeShortcut);

        ShortcutListenerDelegate exitAction = new ShortcutListenerDelegate(
                "closeShortcutAction",
                closeCombination.getKey().getCode(),
                KeyCombination.Modifier.codes(closeCombination.getModifiers())
        );

        exitAction.withHandler(this::onCloseShortcutTriggered);

        dialogWindow.addActionHandler(new Action.Handler() {
            @Override
            public Action[] getActions(Object target, Object sender) {
                return new ShortcutAction[]{exitAction};
            }

            @Override
            public void handleAction(Action action, Object sender, Object target) {
                if (action == exitAction) {
                    exitAction.handleAction(sender, target);
                }
            }
        });
    }

    protected UiProperties getUiProperties() {
        return applicationContext.getBean(UiProperties.class);
    }

    protected void onCloseButtonClick(JmixWindow.PreCloseEvent preCloseEvent) {
        preCloseEvent.setPreventClose(true);

        Component component = getComponent();
        AppUI ui = (AppUI) component.getUI();
        if (!ui.isAccessibleForUser(component)) {
            LoggerFactory.getLogger(WebWindow.class)
                    .debug("Ignore close button click because Window is inaccessible for user");
            return;
        }

        BeforeCloseEvent event = new BeforeCloseEvent(this, CloseOriginType.CLOSE_BUTTON);
        fireBeforeClose(event);

        if (!event.isClosePrevented()) {
            // user has clicked on X
            getFrameOwner().close(new StandardCloseAction(Window.CLOSE_ACTION_ID));
        }
    }

    protected void onCloseShortcutTriggered(@SuppressWarnings("unused") Object sender,
                                            @SuppressWarnings("unused") Object target) {
        if (this.isCloseable()) {
            Component component = getComponent();
            AppUI ui = (AppUI) component.getUI();
            if (!ui.isAccessibleForUser(component)) {
                LoggerFactory.getLogger(WebWindow.class)
                        .debug("Ignore shortcut action because Window is inaccessible for user");
                return;
            }

            BeforeCloseEvent event = new BeforeCloseEvent(this, CloseOriginType.SHORTCUT);
            fireBeforeClose(event);

            if (!event.isClosePrevented()) {
                getFrameOwner().close(new StandardCloseAction(Window.CLOSE_ACTION_ID));
            }
        }
    }

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setIcon(@Nullable String icon) {
        super.setIcon(icon);

        if (icon == null) {
            dialogWindow.setIcon(null);
        } else {
            IconResolver iconResolver = (IconResolver) applicationContext.getBean(IconResolver.NAME);
            dialogWindow.setIcon(iconResolver.getIconResource(icon));
        }
    }

    @Override
    public void setCaption(@Nullable String caption) {
        super.setCaption(caption);

        this.dialogWindow.setCaption(caption);
    }

    @Override
    public void setDescription(@Nullable String description) {
        super.setDescription(description);

        this.dialogWindow.setDescription(description);
    }

    @Override
    public Component getComposition() {
        return dialogWindow;
    }

    @Override
    public void setDialogWidth(@Nullable String dialogWidth) {
        dialogWindow.setWidth(WebWrapperUtils.toVaadinSize(dialogWidth));

        if (dialogWindow.getWidth() < 0) {
            component.setWidthUndefined();
        } else {
            component.setWidth(100, Sizeable.Unit.PERCENTAGE);
        }
    }

    @Override
    public float getDialogWidth() {
        return dialogWindow.getWidth();
    }

    @Override
    public SizeUnit getDialogWidthUnit() {
        return toSizeUnit(dialogWindow.getWidthUnits());
    }

    @Override
    public void setDialogHeight(@Nullable String dialogHeight) {
        dialogWindow.setHeight(WebWrapperUtils.toVaadinSize(dialogHeight));

        if (dialogWindow.getHeight() < 0) {
            component.setHeightUndefined();
        } else {
            component.setHeight(100, Sizeable.Unit.PERCENTAGE);
        }
    }

    @Override
    public float getDialogHeight() {
        return dialogWindow.getHeight();
    }

    @Override
    public SizeUnit getDialogHeightUnit() {
        return toSizeUnit(dialogWindow.getHeightUnits());
    }

    @Override
    public void setDialogStylename(@Nullable String stylename) {
        dialogWindow.setStyleName(stylename);
    }

    @Nullable
    @Override
    public String getDialogStylename() {
        return dialogWindow.getStyleName();
    }

    @Override
    public void setResizable(boolean resizable) {
        dialogWindow.setResizable(resizable);
    }

    @Override
    public boolean isResizable() {
        return dialogWindow.isResizable();
    }

    @Override
    public void setDraggable(boolean draggable) {
        dialogWindow.setDraggable(draggable);
    }

    @Override
    public boolean isDraggable() {
        return dialogWindow.isDraggable();
    }

    @Override
    public void setCloseable(boolean closeable) {
        super.setCloseable(closeable);

        dialogWindow.setClosable(closeable);
    }

    @Override
    public void setModal(boolean modal) {
        dialogWindow.setModal(modal);
    }

    @Override
    public boolean isModal() {
        return dialogWindow.isModal();
    }

    @Override
    public void setCloseOnClickOutside(boolean closeOnClickOutside) {
        dialogWindow.setCloseOnClickOutside(closeOnClickOutside);
    }

    @Override
    public boolean isCloseOnClickOutside() {
        return dialogWindow.getCloseOnClickOutside();
    }

    @Override
    public void setWindowMode(WindowMode mode) {
        dialogWindow.setWindowMode(com.vaadin.shared.ui.window.WindowMode.valueOf(mode.name()));
    }

    @Override
    public WindowMode getWindowMode() {
        return WindowMode.valueOf(dialogWindow.getWindowMode().name());
    }

    @Override
    public void center() {
        dialogWindow.center();
    }

    @Override
    public void setPositionX(int positionX) {
        dialogWindow.setPositionX(positionX);
    }

    @Override
    public int getPositionX() {
        return dialogWindow.getPositionX();
    }

    @Override
    public void setPositionY(int positionY) {
        dialogWindow.setPositionY(positionY);
    }

    @Override
    public int getPositionY() {
        return dialogWindow.getPositionY();
    }

    protected class DialogWindowActionHandler implements Action.Handler {

        protected Action analyzeAction;

        protected boolean initialized = false;

        public DialogWindowActionHandler() {
        }

        @Override
        public Action[] getActions(Object target, Object sender) {
            if (!initialized) {
                Messages messages = (Messages) applicationContext.getBean(Messages.NAME);

                analyzeAction = new Action(messages.getMessage("actions.analyzeLayout"));

                initialized = true;
            }

            List<Action> actions = new ArrayList<>(3);

            UiProperties properties = getUiProperties();
            if (properties.isLayoutAnalyzerEnabled()) {
                actions.add(analyzeAction);
            }

            return actions.toArray(new Action[0]);
        }

        @Override
        public void handleAction(Action action, Object sender, Object target) {
            // todo actions

            /*if (initialized) {
                if (analyzeAction == action) {
                    LayoutAnalyzer analyzer = new LayoutAnalyzer();
                    List<LayoutTip> tipsList = analyzer.analyze(WebDialogWindow.this);

                    if (tipsList.isEmpty()) {
                        getWindowManager().showNotification("No layout problems found", Frame.NotificationType.HUMANIZED);
                    } else {
                        WindowConfig windowConfig = beanLocator.get(WindowConfig.NAME);
                        WindowInfo windowInfo = windowConfig.getWindowInfo("layoutAnalyzer");
                        getWindowManager().openWindow(windowInfo, OpenType.DIALOG, ParamsMap.of("tipsList", tipsList));
                    }
                }
            }*/
        }
    }

    public static class GuiDialogWindow extends JmixWindow {
        protected DialogWindow dialogWindow;

        public GuiDialogWindow(DialogWindow dialogWindow) {
            this.dialogWindow = dialogWindow;

            setStyleName("c-app-dialog-window");
            setModal(true);
            setResizable(false);
            center();
        }

        public DialogWindow getDialogWindow() {
            return dialogWindow;
        }
    }
}