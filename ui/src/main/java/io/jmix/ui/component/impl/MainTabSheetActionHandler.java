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
import io.jmix.core.AppBeans;
import io.jmix.core.Messages;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.UiProperties;
import io.jmix.ui.component.Window;
import io.jmix.ui.widget.HasTabSheetBehaviour;
import io.jmix.ui.widget.TabSheetBehaviour;
import io.jmix.ui.widget.WindowBreadCrumbs;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MainTabSheetActionHandler implements Action.Handler {

    protected Action closeAllTabs;
    protected Action closeOtherTabs;
    protected Action closeCurrentTab;

    protected Action showInfo;

    protected Action analyzeLayout;

    protected boolean initialized = false;
    protected HasTabSheetBehaviour tabSheet;

    public MainTabSheetActionHandler(HasTabSheetBehaviour tabSheet) {
        this.tabSheet = tabSheet;
    }

    @Override
    public Action[] getActions(Object target, Object sender) {
        if (!initialized) {
            Messages messages = AppBeans.get(Messages.NAME);

            closeAllTabs = new Action(messages.getMessage("actions.closeAllTabs"));
            closeOtherTabs = new Action(messages.getMessage("actions.closeOtherTabs"));
            closeCurrentTab = new Action(messages.getMessage("actions.closeCurrentTab"));
            showInfo = new Action(messages.getMessage("actions.showInfo"));
            analyzeLayout = new Action(messages.getMessage("actions.analyzeLayout"));

            initialized = true;
        }

        List<Action> actions = new ArrayList<>(5);
        actions.add(closeCurrentTab);
        actions.add(closeOtherTabs);
        actions.add(closeAllTabs);

        if (target != null) {
            UiProperties properties = AppBeans.get(UiProperties.class);

            // CurrentAuthentication currentAuthentication = AppBeans.get(CurrentAuthentication.NAME);
            // todo permissions
//            if (userSession.isSpecificPermitted(ShowInfoAction.ACTION_PERMISSION) &&
//                    findEditor((Layout) target) != null) {
//                actions.add(showInfo);
//            }
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
                tabSheetBehaviour.closeTab((Component) target);
            } else if (closeOtherTabs == action) {
                tabSheetBehaviour.closeOtherTabs((Component) target);
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
        // todo show info
        /*Window.Editor editor = findEditor((Layout) target);
        Entity entity = editor.getItem();

        Metadata metadata = AppBeans.get(Metadata.NAME);
        MetaClass metaClass = metadata.getSession().getClass(entity.getClass());

        new ShowInfoAction().showInfo(entity, metaClass, editor);*/
    }

    protected void analyzeLayout(Object target) {
        // todo analyzer
        /*Window window = findWindow((Layout) target);
        if (window != null) {
            LayoutAnalyzer analyzer = new LayoutAnalyzer();
            List<LayoutTip> tipsList = analyzer.analyze(window);

            if (tipsList.isEmpty()) {
                Notifications notifications = ComponentsHelper.getScreenContext(window).getNotifications();

                notifications.create(NotificationType.HUMANIZED)
                        .withCaption("No layout problems found")
                        .show();
            } else {
                WindowManager wm = (WindowManager) ComponentsHelper.getScreenContext(window).getScreens();
                WindowInfo windowInfo = AppBeans.get(WindowConfig.class).getWindowInfo("layoutAnalyzer");

                wm.openWindow(windowInfo, OpenType.DIALOG, ParamsMap.of("tipsList", tipsList));
            }
        }*/
    }

    @Nullable
    protected Window getWindow(Object target) {
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

    // todo
    /*protected Window.Editor findEditor(Layout layout) {
        for (Object component : layout) {
            if (component instanceof WindowBreadCrumbs) {
                WindowBreadCrumbs breadCrumbs = (WindowBreadCrumbs) component;
                if (breadCrumbs.getCurrentWindow() != null) {
                    Screen frameOwner = breadCrumbs.getCurrentWindow().getFrameOwner();
                    if (frameOwner instanceof Window.Editor) {
                        return (Window.Editor) frameOwner;
                    }

                    if (frameOwner instanceof EditorScreen) {
                        return new ScreenEditorWrapper(frameOwner);
                    }
                }
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
    }*/
}