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

import com.vaadin.event.Action;
import com.vaadin.ui.Component;
import com.vaadin.ui.Layout;
import io.jmix.core.AppBeans;
import io.jmix.core.Messages;
import io.jmix.core.security.Security;
import io.jmix.ui.UiProperties;
import io.jmix.ui.component.Window;
import io.jmix.ui.sys.ShowInfoAction;
import io.jmix.ui.widget.HasTabSheetBehaviour;
import io.jmix.ui.widget.TabSheetBehaviour;
import io.jmix.ui.widget.WindowBreadCrumbs;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MainTabSheetActionHandler implements Action.Handler {

    protected com.vaadin.event.Action closeAllTabs;
    protected com.vaadin.event.Action closeOtherTabs;
    protected com.vaadin.event.Action closeCurrentTab;

    protected com.vaadin.event.Action showInfo;

    protected com.vaadin.event.Action analyzeLayout;

    protected boolean initialized = false;
    protected HasTabSheetBehaviour tabSheet;

    public MainTabSheetActionHandler(HasTabSheetBehaviour tabSheet) {
        this.tabSheet = tabSheet;
    }

    @Override
    public Action[] getActions(Object target, Object sender) {
        if (!initialized) {
            Messages messages = AppBeans.get(Messages.NAME);

            closeAllTabs = new com.vaadin.event.Action(messages.getMessage("actions.closeAllTabs"));
            closeOtherTabs = new com.vaadin.event.Action(messages.getMessage("actions.closeOtherTabs"));
            closeCurrentTab = new com.vaadin.event.Action(messages.getMessage("actions.closeCurrentTab"));
            showInfo = new com.vaadin.event.Action(messages.getMessage("actions.showInfo"));
            analyzeLayout = new com.vaadin.event.Action(messages.getMessage("actions.analyzeLayout"));

            initialized = true;
        }

        List<Action> actions = new ArrayList<>(5);
        actions.add(closeCurrentTab);
        actions.add(closeOtherTabs);
        actions.add(closeAllTabs);

        if (target != null) {
            UiProperties properties = AppBeans.get(UiProperties.class);

            Security security = AppBeans.get(Security.NAME);
            if (security.isSpecificPermitted(ShowInfoAction.ACTION_PERMISSION) &&
                    findEditor((Layout) target) != null) {
                actions.add(showInfo);
            }
            if (properties.isLayoutAnalyzerEnabled()) {
                actions.add(analyzeLayout);
            }
        }

        return actions.toArray(new Action[0]);
    }

    @Override
    public void handleAction(Action action, Object sender, Object target) {
        TabSheetBehaviour tabSheetBehaviour = tabSheet.getTabSheetBehaviour();
        if (initialized) {
            if (closeCurrentTab == action) {
                tabSheetBehaviour.closeTab((com.vaadin.ui.Component) target);
            } else if (closeOtherTabs == action) {
                tabSheetBehaviour.closeOtherTabs((com.vaadin.ui.Component) target);
            } else if (closeAllTabs == action) {
                tabSheetBehaviour.closeAllTabs();
            } else if (showInfo == action) {
                showInfo(target);
            } else if (analyzeLayout == action) {
                analyzeLayout(target);
            }
        }
    }

    protected void showInfo(Object target) {
        /*
        TODO: legacy-ui
        AbstractEditor editor = (AbstractEditor) findEditor((Layout) target);
        Entity entity = editor.getItem();

        Metadata metadata = AppBeans.get(Metadata.NAME);
        MetaClass metaClass = metadata.getSession().getClass(entity.getClass());

        new ShowInfoAction().showInfo(entity, metaClass, editor);*/
    }

    protected void analyzeLayout(Object target) {

    // todo layout analyzer

//        Window window = findWindow((Layout) target);
//        if (window != null) {
//            LayoutAnalyzer analyzer = new LayoutAnalyzer();
//            List<LayoutTip> tipsList = analyzer.analyze(window);
//
//            if (tipsList.isEmpty()) {
//                Notifications notifications = ComponentsHelper.getScreenContext(window).getNotifications();
//
//                notifications.create(Notifications.NotificationType.HUMANIZED)
//                        .withCaption("No layout problems found")
//                        .show();
//            } else {
//                WindowManager wm = (WindowManager) ComponentsHelper.getScreenContext(window).getScreens();
//                WindowInfo windowInfo = AppBeans.get(WindowConfig.class).getWindowInfo("layoutAnalyzer");
//
//                wm.openWindow(windowInfo, OpenType.DIALOG, ParamsMap.of("tipsList", tipsList));
//            }
//        }
    }

    @Nullable
    protected io.jmix.ui.component.Window getWindow(Object target) {
        if (target instanceof Layout) {
            Layout layout = (Layout) target;
            for (Component component : layout) {
                if (component instanceof WindowBreadCrumbs) {
                    WindowBreadCrumbs breadCrumbs = (WindowBreadCrumbs) component;
                    return breadCrumbs.getCurrentWindow();
                }
            }
        }

        return null;
    }

    protected io.jmix.ui.component.Window.Editor findEditor(Layout layout) {
        for (Object component : layout) {
            if (component instanceof WindowBreadCrumbs) {
                WindowBreadCrumbs breadCrumbs = (WindowBreadCrumbs) component;
                if (breadCrumbs.getCurrentWindow() instanceof Window.Editor)
                    return (Window.Editor) breadCrumbs.getCurrentWindow();
            }
        }
        return null;
    }

    protected io.jmix.ui.component.Window findWindow(Layout layout) {
        for (Object component : layout) {
            if (component instanceof WindowBreadCrumbs) {
                WindowBreadCrumbs breadCrumbs = (WindowBreadCrumbs) component;
                if (breadCrumbs.getCurrentWindow() != null) {
                    return breadCrumbs.getCurrentWindow();
                }
            }
        }
        return null;
    }
}
