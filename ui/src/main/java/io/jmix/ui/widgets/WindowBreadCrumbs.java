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
package io.jmix.ui.widgets;

import com.vaadin.server.Resource;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.themes.ValoTheme;
import io.jmix.core.BeanLocator;
import io.jmix.core.ConfigInterfaces;
import io.jmix.core.Messages;
import io.jmix.ui.AppUI;
import io.jmix.ui.WebConfig;
import io.jmix.ui.components.AppWorkArea.Mode;
import io.jmix.ui.components.CloseOriginType;
import io.jmix.ui.components.Window;
import io.jmix.ui.components.impl.WebWindow;
import io.jmix.ui.icons.CubaIcon;
import io.jmix.ui.icons.IconResolver;
import io.jmix.ui.icons.Icons;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.sys.TestIdManager;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public class WindowBreadCrumbs extends CssLayout {

    protected static final String BREADCRUMBS_VISIBLE_WRAP_STYLE = "c-breadcrumbs-visible";
    protected static final String C_HEADLINE_CONTAINER = "c-headline-container";

    protected boolean visibleExplicitly = true;

    protected Mode workAreaMode;

    protected Deque<Window> windows = new ArrayDeque<>(8);

    protected Layout linksLayout;
    protected Button closeBtn;

    protected WindowNavigateHandler windowNavigateHandler = null;

    protected AppUI ui;

    public WindowBreadCrumbs(Mode workAreaMode) {
        this.workAreaMode = workAreaMode;
    }

    public void setUI(AppUI ui) {
        this.ui = ui;
    }

    public void setBeanLocator(BeanLocator beanLocator) {
        setWidth(100, Unit.PERCENTAGE);
        setHeightUndefined();
        setPrimaryStyleName(C_HEADLINE_CONTAINER);

        if (workAreaMode == Mode.TABBED) {
            super.setVisible(false);
        }

        Layout logoLayout = createLogoLayout();

        linksLayout = createLinksLayout();
        linksLayout.setSizeUndefined();

        if (workAreaMode == Mode.SINGLE) {
            Messages messages = beanLocator.get(Messages.NAME);

            JmixButton closeBtn = new JmixButton("");
            closeBtn.setDescription(messages.getMessage("windowBreadCrumbs.closeButton.description"));
            closeBtn.setClickHandler(this::onCloseWindowButtonClick);
            closeBtn.setIcon(resolveIcon(beanLocator, CubaIcon.CLOSE));
            closeBtn.setStyleName("c-closetab-button");

            this.closeBtn = closeBtn;
        }

        Layout enclosingLayout = createEnclosingLayout();
        enclosingLayout.addComponent(linksLayout);

        addComponent(logoLayout);
        addComponent(enclosingLayout);

        boolean controlsVisible = beanLocator.get(ConfigInterfaces.class)
                .getConfig(WebConfig.class)
                .getShowBreadCrumbs();

        enclosingLayout.setVisible(controlsVisible);

        if (closeBtn != null) {
            addComponent(closeBtn);
        }
    }

    protected Resource resolveIcon(BeanLocator beanLocator, CubaIcon icon) {
        String iconName = beanLocator.get(Icons.class).get(icon);
        return beanLocator.get(IconResolver.class).getIconResource(iconName);
    }

    protected void onCloseWindowButtonClick(@SuppressWarnings("unused") MouseEventDetails meDetails) {
        Window window = getCurrentWindow();
        if (!window.isCloseable()) {
            return;
        }

        if (!isCloseWithCloseButtonPrevented(window)) {
            window.getFrameOwner()
                    .close(FrameOwner.WINDOW_CLOSE_ACTION);
        }
    }

    protected boolean isCloseWithCloseButtonPrevented(Window currentWindow) {
        WebWindow webWindow = (WebWindow) currentWindow;

        if (webWindow != null) {
            Window.BeforeCloseEvent event = new Window.BeforeCloseEvent(webWindow, CloseOriginType.CLOSE_BUTTON);
            webWindow.fireBeforeClose(event);
            return event.isClosePrevented();
        }

        return false;
    }

    protected Layout createEnclosingLayout() {
        Layout enclosingLayout = new CssLayout();
        enclosingLayout.setPrimaryStyleName("c-breadcrumbs-container");
        return enclosingLayout;
    }

    protected Layout createLinksLayout() {
        CssLayout linksLayout = new CssLayout();
        linksLayout.setPrimaryStyleName("c-breadcrumbs");
        return linksLayout;
    }

    protected Layout createLogoLayout() {
        CssLayout logoLayout = new CssLayout();
        logoLayout.setPrimaryStyleName("c-breadcrumbs-logo");
        return logoLayout;
    }

    public Window getCurrentWindow() {
        if (windows.isEmpty()) {
            return null;
        } else {
            return windows.getLast();
        }
    }

    public void addWindow(Window window) {
        windows.add(window);
        update();

        if (windows.size() > 1 && workAreaMode == Mode.TABBED) {
            super.setVisible(visibleExplicitly);
        }

        if (getParent() != null) {
            adjustParentStyles();
        }
    }

    public void removeWindow() {
        if (!windows.isEmpty()) {
            windows.removeLast();
            update();
        }

        if (windows.size() <= 1 && workAreaMode == Mode.TABBED) {
            super.setVisible(false);
        }

        if (getParent() != null) {
            adjustParentStyles();
        }
    }

    @Override
    public void setVisible(boolean visible) {
        this.visibleExplicitly = visible;

        super.setVisible(isVisible() && visibleExplicitly);

        if (getParent() != null) {
            adjustParentStyles();
        }
    }

    protected void adjustParentStyles() {
        if (isVisible()) {
            getParent().addStyleName(BREADCRUMBS_VISIBLE_WRAP_STYLE);
        } else {
            getParent().removeStyleName(BREADCRUMBS_VISIBLE_WRAP_STYLE);
        }
    }

    public void setWindowNavigateHandler(WindowNavigateHandler handler) {
        this.windowNavigateHandler = handler;
    }

    protected void fireListeners(Window window) {
        if (windowNavigateHandler != null) {
            windowNavigateHandler.windowNavigate(this, window);
        }
    }

    public void update() {
        boolean isTestMode = ui.isTestMode();

        linksLayout.removeAllComponents();
        for (Iterator<Window> it = windows.iterator(); it.hasNext();) {
            Window window = it.next();

            Button button = new NavigationButton(window);
            button.setCaption(StringUtils.trimToEmpty(window.getCaption()));
            button.addClickListener(this::navigationButtonClicked);
            button.setSizeUndefined();
            button.setStyleName(ValoTheme.BUTTON_LINK);
            button.setTabIndex(-1);

            if (isTestMode) {
                button.setCubaId("breadCrubms_Button_" + window.getId());
            }

            if (ui.isPerformanceTestMode()) {
                button.setId(ui.getTestIdManager().getTestId("breadCrubms_Button_" + window.getId()));
            }

            if (it.hasNext()) {
                linksLayout.addComponent(button);

                Label separatorLab = new Label("&nbsp;&gt;&nbsp;");
                separatorLab.setStyleName("c-breadcrumbs-separator");
                separatorLab.setSizeUndefined();
                separatorLab.setContentMode(ContentMode.HTML);
                linksLayout.addComponent(separatorLab);
            } else {
                Label captionLabel = new Label(window.getCaption());
                captionLabel.setStyleName("c-breadcrumbs-win-caption");
                captionLabel.setSizeUndefined();
                linksLayout.addComponent(captionLabel);
            }
        }
    }

    @Override
    public void attach() {
        super.attach();

        componentAttachedToUI();
    }

    protected void componentAttachedToUI() {
        adjustParentStyles();

        if (ui.isTestMode()) {
            linksLayout.setCubaId("breadCrumbs");

            if (closeBtn != null) {
                closeBtn.setCubaId("closeBtn");
            }
        }

        if (ui.isPerformanceTestMode()) {
            TestIdManager testIdManager = ui.getTestIdManager();
            linksLayout.setId(testIdManager.getTestId("breadCrumbs"));

            if (closeBtn != null) {
                closeBtn.setId(testIdManager.getTestId("closeBtn"));
            }
        }
    }

    public Deque<Window> getWindows() {
        return windows;
    }

    protected void navigationButtonClicked(Button.ClickEvent event) {
        Window win = ((NavigationButton) event.getButton()).getWindow();
        if (win != null) {
            fireListeners(win);
        }
    }

    @FunctionalInterface
    public interface WindowNavigateHandler {
        void windowNavigate(WindowBreadCrumbs breadCrumbs, Window window);
    }

    public static class NavigationButton extends JmixButton {
        protected final Window window;

        public NavigationButton(Window window) {
            this.window = window;
        }

        public Window getWindow() {
            return window;
        }
    }
}