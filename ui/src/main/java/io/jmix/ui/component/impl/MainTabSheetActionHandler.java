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
import com.vaadin.ui.Component;
import com.vaadin.ui.Layout;
import io.jmix.core.Messages;
import io.jmix.ui.Notifications;
import io.jmix.ui.Notifications.NotificationType;
import io.jmix.ui.Screens;
import io.jmix.ui.action.Action.MainTabSheetAction;
import io.jmix.ui.action.MainTabSheetActionProvider;
import io.jmix.ui.app.core.dev.LayoutAnalyzer;
import io.jmix.ui.app.core.dev.LayoutAnalyzerScreen;
import io.jmix.ui.app.core.dev.LayoutTip;
import io.jmix.ui.component.Window;
import io.jmix.ui.UiScreenProperties;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenContext;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.widget.HasTabSheetBehaviour;
import io.jmix.ui.widget.TabSheetBehaviour;
import io.jmix.ui.widget.WindowBreadCrumbs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MainTabSheetActionHandler implements Action.Handler {

    private final Logger log = LoggerFactory.getLogger(MainTabSheetActionHandler.class);

    protected Action closeAllTabs;
    protected Action closeOtherTabs;
    protected Action closeCurrentTab;
    protected Action analyzeLayout;

    protected boolean initialized = false;
    protected HasTabSheetBehaviour tabSheet;

    protected ApplicationContext applicationContext;
    protected Messages messages;
    protected UiScreenProperties screenProperties;

    public MainTabSheetActionHandler(HasTabSheetBehaviour tabSheet, ApplicationContext applicationContext) {
        this.tabSheet = tabSheet;
        this.applicationContext = applicationContext;
        this.messages = applicationContext.getBean(Messages.class);
        this.screenProperties = applicationContext.getBean(UiScreenProperties.class);
    }

    @Override
    public Action[] getActions(Object target, Object sender) {
        if (!initialized) {
            closeAllTabs = new Action(messages.getMessage("actions.closeAllTabs"));
            closeOtherTabs = new Action(messages.getMessage("actions.closeOtherTabs"));
            closeCurrentTab = new Action(messages.getMessage("actions.closeCurrentTab"));
            analyzeLayout = new Action(messages.getMessage("actions.analyzeLayout"));

            initialized = true;
        }

        List<Action> actions = new ArrayList<>(5);
        actions.add(closeCurrentTab);
        actions.add(closeOtherTabs);
        actions.add(closeAllTabs);

        if (target != null) {
            if (screenProperties.isLayoutAnalyzerEnabled()) {
                actions.add(analyzeLayout);
            }

            actions.addAll(collectAdditionalActions(target));
        }

        return actions.toArray(new Action[0]);
    }

    protected List<Action> collectAdditionalActions(Object target) {
        Screen screen = findScreen(((Layout) target));
        if (screen == null) {
            return Collections.emptyList();
        }

        Map<String, MainTabSheetActionProvider> beans =
                applicationContext.getBeansOfType(MainTabSheetActionProvider.class);
        if (beans.isEmpty()) {
            return Collections.emptyList();
        }

        return beans.values().stream()
                .flatMap(provider ->
                        provider.getActions().stream())
                .filter(action -> {
                    action.refreshState();
                    return action.isVisible()
                            && action.isApplicable(screen);
                })
                .map(MainTabSheetActionWrapper::new)
                .collect(Collectors.toList());
    }

    @Override
    public void handleAction(Action action, Object sender, Object target) {
        TabSheetBehaviour tabSheetBehaviour = tabSheet.getTabSheetBehaviour();
        if (initialized) {
            if (closeCurrentTab == action) {
                tabSheetBehaviour.closeTab((Component) target);
            } else if (closeOtherTabs == action) {
                tabSheetBehaviour.closeOtherTabs((Component) target);
            } else if (closeAllTabs == action) {
                tabSheetBehaviour.closeAllTabs();
            } else if (analyzeLayout == action) {
                analyzeLayout(target);
            }

            if (action instanceof MainTabSheetActionWrapper) {
                Screen screen = findScreen(((Layout) target));
                if (screen != null) {
                    ((MainTabSheetActionWrapper) action).execute(screen);
                }
            } else {
                log.warn("'{}' action isn't handled", action.getCaption());
            }
        }
    }

    protected void analyzeLayout(Object target) {
        Screen screen = findScreen((Layout) target);
        if (screen == null) {
            return;
        }

        LayoutAnalyzer analyzer = new LayoutAnalyzer();
        List<LayoutTip> layoutTips = analyzer.analyze(screen);

        ScreenContext screenContext = UiControllerUtils.getScreenContext(screen);
        if (layoutTips.isEmpty()) {
            Notifications notifications = screenContext.getNotifications();
            notifications.create(NotificationType.HUMANIZED)
                    .withCaption("No layout problems found")
                    .show();
        } else {
            Screens screens = screenContext.getScreens();
            LayoutAnalyzerScreen analyzerScreen = screens.create(LayoutAnalyzerScreen.class, OpenMode.DIALOG);
            analyzerScreen.setLayoutTips(layoutTips);
            analyzerScreen.show();
        }
    }

    @Nullable
    protected Screen findScreen(Layout layout) {
        for (Object component : layout) {
            if (component instanceof WindowBreadCrumbs) {
                WindowBreadCrumbs breadCrumbs = (WindowBreadCrumbs) component;
                if (breadCrumbs.getCurrentWindow() != null) {
                    Window currentWindow = breadCrumbs.getCurrentWindow();
                    return UiControllerUtils.getScreen(currentWindow.getFrameOwner());
                }
            }
        }

        return null;
    }

    protected static class MainTabSheetActionWrapper extends Action {
        protected MainTabSheetAction action;

        public MainTabSheetActionWrapper(MainTabSheetAction action) {
            super(action.getCaption());
            this.action = action;
        }

        public void execute(Screen screen) {
            action.execute(screen);
        }
    }
}
