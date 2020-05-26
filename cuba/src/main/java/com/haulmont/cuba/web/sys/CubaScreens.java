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

package com.haulmont.cuba.web.sys;

import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.WindowParams;
import com.haulmont.cuba.gui.components.AbstractEditor;
import com.haulmont.cuba.gui.components.compatibility.LegacyFragmentAdapter;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.screen.compatibility.ScreenEditorWrapper;
import com.haulmont.cuba.gui.screen.compatibility.ScreenFragmentWrapper;
import com.haulmont.cuba.gui.screen.compatibility.ScreenLookupWrapper;
import com.haulmont.cuba.gui.screen.compatibility.ScreenWrapper;
import io.jmix.core.Entity;
import io.jmix.ui.AppUI;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Fragments;
import io.jmix.ui.Notifications.NotificationType;
import io.jmix.ui.WindowInfo;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ComponentContainer;
import io.jmix.ui.component.ContentMode;
import io.jmix.ui.component.Frame;
import io.jmix.ui.component.SizeUnit;
import io.jmix.ui.component.Window;
import io.jmix.ui.component.compatibility.SelectHandlerAdapter;
import io.jmix.ui.component.impl.WebAppWorkArea;
import io.jmix.ui.gui.OpenType;
import io.jmix.ui.screen.EditorScreen;
import io.jmix.ui.screen.LookupScreen;
import io.jmix.ui.screen.MapScreenOptions;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.sys.WebScreens;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings({"rawtypes", "unchecked", "DeprecatedIsStillUsed", "deprecation"})
@Deprecated
public final class CubaScreens extends WebScreens implements WindowManager {

    public CubaScreens(AppUI ui) {
        super(ui);
    }

    @Override
    public Collection<Window> getOpenWindows() {
        return getOpenedScreens().getAll().stream()
                .map(Screen::getWindow)
                .collect(Collectors.toList());
    }

    @Override
    public void selectWindowTab(Window window) {
        WebAppWorkArea workArea = getConfiguredWorkArea();

        Collection<WindowStack> workAreaStacks = getWorkAreaStacks(workArea);

        Screen screen = window.getFrameOwner();
        workAreaStacks.stream()
                .filter(ws -> ws.getBreadcrumbs().contains(screen))
                .findFirst()
                .ifPresent(WindowStack::select);
    }

    @Override
    public Window openWindow(WindowInfo windowInfo, OpenType openType, Map<String, Object> params) {
        params = createParametersMap(windowInfo, params);
        MapScreenOptions options = new MapScreenOptions(params);

        Screen screen = createScreen(windowInfo, openType.getOpenMode(), options);
        applyOpenTypeParameters(screen.getWindow(), openType);

        show(screen);
        return screen instanceof Window ? (Window) screen : new ScreenWrapper(screen);
    }

    @Override
    public Window openWindow(WindowInfo windowInfo, OpenType openType) {
        Map<String, Object> params = createParametersMap(windowInfo, Collections.emptyMap());
        MapScreenOptions options = new MapScreenOptions(params);

        Screen screen = createScreen(windowInfo, openType.getOpenMode(), options);
        applyOpenTypeParameters(screen.getWindow(), openType);

        show(screen);
        return screen instanceof Window ? (Window) screen : new ScreenWrapper(screen);
    }


    @Override
    public Window.Editor openEditor(WindowInfo windowInfo, Entity item, OpenType openType, Datasource parentDs) {
        Map<String, Object> params = createParametersMap(windowInfo,
                Collections.singletonMap(WindowParams.ITEM.name(), item)
        );
        MapScreenOptions options = new MapScreenOptions(params);

        Screen screen = createScreen(windowInfo, openType.getOpenMode(), options);
        applyOpenTypeParameters(screen.getWindow(), openType);

        EditorScreen editorScreen = (EditorScreen) screen;
        if (editorScreen instanceof AbstractEditor) {
            ((AbstractEditor) editorScreen).setParentDs(parentDs);
        }
        editorScreen.setEntityToEdit(item);
        show(screen);
        return screen instanceof Window.Editor ? (Window.Editor) screen : new ScreenEditorWrapper(screen);
    }


    @Override
    public Window.Editor openEditor(WindowInfo windowInfo, Entity item, OpenType openType) {
        Map<String, Object> params = createParametersMap(windowInfo,
                Collections.singletonMap(WindowParams.ITEM.name(), item)
        );

        MapScreenOptions options = new MapScreenOptions(params);

        Screen screen = createScreen(windowInfo, openType.getOpenMode(), options);
        applyOpenTypeParameters(screen.getWindow(), openType);

        EditorScreen editorScreen = (EditorScreen) screen;
        editorScreen.setEntityToEdit(item);
        show(screen);
        return screen instanceof Window.Editor ? (Window.Editor) screen : new ScreenEditorWrapper(screen);
    }

    @Override
    public Window.Editor openEditor(WindowInfo windowInfo, Entity item, OpenType openType, Map<String, Object> params) {
        Screen editor = createEditor(windowInfo, item, openType, params);

        editor.show();

        return editor instanceof Window.Editor ? (Window.Editor) editor : new ScreenEditorWrapper(editor);
    }

    @Override
    public Screen createEditor(WindowInfo windowInfo, Entity item, OpenType openType,
                               @Nullable Map<String, Object> params) {
        params = createParametersMap(windowInfo, params);
        params.put(WindowParams.ITEM.name(), item);

        MapScreenOptions options = new MapScreenOptions(params);

        Screen screen = createScreen(windowInfo, openType.getOpenMode(), options);
        applyOpenTypeParameters(screen.getWindow(), openType);

        EditorScreen editorScreen = (EditorScreen) screen;

        //noinspection unchecked
        editorScreen.setEntityToEdit(item);

        return screen;
    }


    @Override
    public Window.Editor openEditor(WindowInfo windowInfo, Entity item, OpenType openType,
                                    @Nullable Map<String, Object> params,
                                    Datasource parentDs) {
        params = createParametersMap(windowInfo, params);
        params.put(WindowParams.ITEM.name(), item);

        MapScreenOptions options = new MapScreenOptions(params);

        Screen screen = createScreen(windowInfo, openType.getOpenMode(), options);
        applyOpenTypeParameters(screen.getWindow(), openType);

        EditorScreen editorScreen = (EditorScreen) screen;
        if (editorScreen instanceof AbstractEditor) {
            ((AbstractEditor) editorScreen).setParentDs(parentDs);
        }
        editorScreen.setEntityToEdit(item);
        show(screen);

        return screen instanceof Window.Editor ? (Window.Editor) screen : new ScreenEditorWrapper(screen);
    }


    @Override
    public Window.Lookup openLookup(WindowInfo windowInfo, Window.Lookup.Handler handler, OpenType openType,
                                    Map<String, Object> params) {
        params = createParametersMap(windowInfo, params);

        MapScreenOptions options = new MapScreenOptions(params);
        Screen screen = createScreen(windowInfo, openType.getOpenMode(), options);
        applyOpenTypeParameters(screen.getWindow(), openType);

        ((LookupScreen) screen).setSelectHandler(new SelectHandlerAdapter(handler));

        show(screen);

        return screen instanceof Window.Lookup ? (Window.Lookup) screen : new ScreenLookupWrapper(screen);
    }


    @Override
    public Window.Lookup openLookup(WindowInfo windowInfo, Window.Lookup.Handler handler, OpenType openType) {
        Map<String, Object> params = createParametersMap(windowInfo, Collections.emptyMap());

        MapScreenOptions options = new MapScreenOptions(params);
        Screen screen = createScreen(windowInfo, openType.getOpenMode(), options);
        applyOpenTypeParameters(screen.getWindow(), openType);

        ((LookupScreen) screen).setSelectHandler(new SelectHandlerAdapter(handler));

        show(screen);

        return screen instanceof Window.Lookup ? (Window.Lookup) screen : new ScreenLookupWrapper(screen);
    }

    @Override
    public Frame openFrame(Frame parentFrame, Component parent, WindowInfo windowInfo) {
        return openFrame(parentFrame, parent, windowInfo, Collections.emptyMap());
    }

    @Override
    public Frame openFrame(Frame parentFrame, Component parent, WindowInfo windowInfo,
                           Map<String, Object> params) {
        return openFrame(parentFrame, parent, null, windowInfo, params);
    }

    @Override
    public Frame openFrame(Frame parentFrame, Component parent, @Nullable String id,
                           WindowInfo windowInfo, Map<String, Object> params) {
        ScreenFragment screenFragment;

        Fragments fragments = ui.getFragments();

        if (params != null && !params.isEmpty()) {
            screenFragment = fragments.create(parentFrame.getFrameOwner(), windowInfo.getId(), new MapScreenOptions(params));
        } else {
            screenFragment = fragments.create(parentFrame.getFrameOwner(), windowInfo.getId());
        }

        if (id != null) {
            screenFragment.getFragment().setId(id);
        }

        fragments.init(screenFragment);

        if (parent instanceof ComponentContainer) {
            ComponentContainer container = (ComponentContainer) parent;
            for (Component c : container.getComponents()) {
                if (c instanceof Component.Disposable) {
                    Component.Disposable disposable =
                            (Component.Disposable) c;
                    if (!disposable.isDisposed()) {
                        disposable.dispose();
                    }
                }
                container.remove(c);
            }
            container.add(screenFragment.getFragment());
        }

        if (screenFragment instanceof LegacyFragmentAdapter) {
            return ((LegacyFragmentAdapter) screenFragment).getRealScreen();
        }

        return screenFragment instanceof Frame ? (Frame) screenFragment : new ScreenFragmentWrapper(screenFragment);
    }

    @Override
    public void showNotification(String caption) {
        ui.getNotifications().create()
                .withCaption(caption)
                .show();
    }

    @Override
    public void showNotification(String caption, Frame.NotificationType type) {
        ui.getNotifications().create()
                .withCaption(caption)
                .withContentMode(Frame.NotificationType.isHTML(type) ? ContentMode.HTML : ContentMode.TEXT)
                .withType(convertNotificationType(type))
                .show();
    }

    @Override
    public void showNotification(String caption, String description, Frame.NotificationType type) {
        ui.getNotifications().create()
                .withCaption(caption)
                .withDescription(description)
                .withContentMode(Frame.NotificationType.isHTML(type) ? ContentMode.HTML : ContentMode.TEXT)
                .withType(convertNotificationType(type))
                .show();
    }

    protected NotificationType convertNotificationType(Frame.NotificationType type) {
        switch (type) {
            case TRAY:
            case TRAY_HTML:
                return NotificationType.TRAY;

            case ERROR:
            case ERROR_HTML:
                return NotificationType.ERROR;

            case HUMANIZED:
            case HUMANIZED_HTML:
                return NotificationType.HUMANIZED;

            case WARNING:
            case WARNING_HTML:
                return NotificationType.WARNING;

            default:
                throw new UnsupportedOperationException("Unsupported notification type");
        }
    }

    @Override
    public void showMessageDialog(String title, String message, Frame.MessageType messageType) {
        Dialogs.MessageDialogBuilder builder = ui.getDialogs().createMessageDialog()
                .withCaption(title)
                .withMessage(message)
                .withContentMode(
                        Frame.MessageMode.isHTML(messageType.getMessageMode()) ? ContentMode.HTML : ContentMode.TEXT
                );

        if (messageType.getWidth() != null) {
            SizeUnit sizeUnit = messageType.getWidthUnit() != null ? messageType.getWidthUnit() : SizeUnit.PIXELS;
            builder.withWidth(messageType.getWidth() + sizeUnit.getSymbol());
        }
        if (messageType.getModal() != null) {
            builder.withModal(messageType.getModal());
        }
        if (messageType.getCloseOnClickOutside() != null) {
            builder.withCloseOnClickOutside(messageType.getCloseOnClickOutside());
        }
        if (messageType.getMaximized() != null) {
            builder.withMaximized(messageType.getMaximized());
        }

        builder.show();
    }

    @Override
    public void showOptionDialog(String title, String message, Frame.MessageType messageType, Action[] actions) {
        Dialogs.OptionDialogBuilder builder = ui.getDialogs().createOptionDialog()
                .withCaption(title)
                .withMessage(message)
                .withContentMode(
                        Frame.MessageMode.isHTML(messageType.getMessageMode()) ? ContentMode.HTML : ContentMode.TEXT
                )
                .withActions(actions);

        if (messageType.getWidth() != null) {
            SizeUnit sizeUnit = messageType.getWidthUnit() != null ? messageType.getWidthUnit() : SizeUnit.PIXELS;
            builder.withWidth(messageType.getWidth() + sizeUnit.getSymbol());
        }
        if (messageType.getMaximized() != null) {
            builder.withMaximized(messageType.getMaximized());
        }

        builder.show();
    }

    @Override
    public void showExceptionDialog(Throwable throwable) {
        showExceptionDialog(throwable, null, null);
    }

    @Override
    public void showExceptionDialog(Throwable throwable, @Nullable String caption, @Nullable String message) {
        ui.getDialogs().createExceptionDialog()
                .withCaption(caption)
                .withMessage(message)
                .withThrowable(throwable)
                .show();
    }

    @Override
    public void showWebPage(String url, @Nullable Map<String, Object> params) {
        ui.getWebBrowserTools().showWebPage(url, params);
    }
}
