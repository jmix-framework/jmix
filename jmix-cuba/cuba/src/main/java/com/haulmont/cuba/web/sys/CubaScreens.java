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

import com.google.common.base.Strings;
import com.haulmont.cuba.gui.Screens;
import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.WindowParams;
import com.haulmont.cuba.gui.components.AbstractEditor;
import com.haulmont.cuba.gui.components.Frame.MessageMode;
import com.haulmont.cuba.gui.components.Frame.MessageType;
import com.haulmont.cuba.gui.components.Frame.NotificationType;
import com.haulmont.cuba.gui.components.compatibility.LegacyFragmentAdapter;
import com.haulmont.cuba.gui.components.compatibility.SelectHandlerAdapter;
import com.haulmont.cuba.gui.data.DataSupplier;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.DsContext;
import com.haulmont.cuba.gui.data.impl.DatasourceImplementation;
import com.haulmont.cuba.gui.data.impl.DsContextImplementation;
import com.haulmont.cuba.gui.data.impl.GenericDataSupplier;
import com.haulmont.cuba.gui.model.impl.CubaScreenDataImpl;
import com.haulmont.cuba.gui.screen.compatibility.LegacyFrame;
import com.haulmont.cuba.gui.screen.compatibility.ScreenEditorWrapper;
import com.haulmont.cuba.gui.screen.compatibility.ScreenFragmentWrapper;
import com.haulmont.cuba.gui.screen.compatibility.ScreenLookupWrapper;
import com.haulmont.cuba.gui.screen.compatibility.ScreenWrapper;
import com.haulmont.cuba.gui.sys.ScreenViewsLoader;
import com.haulmont.cuba.gui.xml.data.DsContextLoader;
import com.haulmont.cuba.gui.xml.layout.CubaLoaderConfig;
import com.haulmont.cuba.settings.CubaLegacySettings;
import com.haulmont.cuba.settings.CubaSettingsFacet;
import com.haulmont.cuba.settings.Settings;
import com.haulmont.cuba.settings.SettingsImpl;
import io.jmix.core.Entity;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.ui.*;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.*;
import io.jmix.ui.component.impl.AppWorkAreaImpl;
import io.jmix.ui.screen.EditorScreen;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.LookupScreen;
import io.jmix.ui.screen.MapScreenOptions;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.screen.ScreenOptions;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.sys.ScreenXmlLoader;
import io.jmix.ui.sys.ScreensImpl;
import io.jmix.ui.sys.WindowContextImpl;
import io.jmix.ui.xml.layout.loader.ComponentLoaderContext;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings({"rawtypes", "unchecked", "deprecation"})
@Deprecated
public final class CubaScreens extends ScreensImpl implements Screens, WindowManager {

    @Autowired
    protected ScreenViewsLoader screenViewsLoader;
    @Autowired
    protected UiComponents cubaUiComponents;
    @Autowired
    protected ScreenXmlLoader screenXmlLoader;
    @Autowired
    protected Facets facets;

    protected DataSupplier defaultDataSupplier = new GenericDataSupplier();

    @Override
    public Collection<Window> getOpenWindows() {
        return getOpenedScreens().getAll().stream()
                .map(Screen::getWindow)
                .collect(Collectors.toList());
    }

    @Override
    public void selectWindowTab(Window window) {
        AppWorkAreaImpl workArea = getConfiguredWorkArea();

        Collection<WindowStack> workAreaStacks = getWorkAreaStacks(workArea);

        Screen screen = window.getFrameOwner();
        workAreaStacks.stream()
                .filter(ws -> ws.getBreadcrumbs().contains(screen))
                .findFirst()
                .ifPresent(WindowStack::select);
    }

    @Override
    public com.haulmont.cuba.gui.components.Window openWindow(WindowInfo windowInfo,
                                                              OpenType openType,
                                                              Map<String, Object> params) {
        params = createParametersMap(windowInfo, params);
        MapScreenOptions options = new MapScreenOptions(params);

        Screen screen = createScreen(windowInfo, openType.getOpenMode(), options);
        applyOpenTypeParameters(screen.getWindow(), openType);

        show(screen);
        return screen instanceof com.haulmont.cuba.gui.components.Window
                ? (com.haulmont.cuba.gui.components.Window) screen
                : new ScreenWrapper(screen);
    }

    @Override
    public com.haulmont.cuba.gui.components.Window openWindow(WindowInfo windowInfo,
                                                              OpenType openType) {
        Map<String, Object> params = createParametersMap(windowInfo, Collections.emptyMap());
        MapScreenOptions options = new MapScreenOptions(params);

        Screen screen = createScreen(windowInfo, openType.getOpenMode(), options);
        applyOpenTypeParameters(screen.getWindow(), openType);

        show(screen);
        return screen instanceof com.haulmont.cuba.gui.components.Window
                ? (com.haulmont.cuba.gui.components.Window) screen
                : new ScreenWrapper(screen);
    }


    @Override
    public com.haulmont.cuba.gui.components.Window.Editor openEditor(WindowInfo windowInfo,
                                                                     Entity item,
                                                                     OpenType openType,
                                                                     Datasource parentDs) {
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
        return screen instanceof com.haulmont.cuba.gui.components.Window.Editor
                ? (com.haulmont.cuba.gui.components.Window.Editor) screen
                : new ScreenEditorWrapper(screen);
    }


    @Override
    public com.haulmont.cuba.gui.components.Window.Editor openEditor(WindowInfo windowInfo,
                                                                     Entity item,
                                                                     OpenType openType) {
        Map<String, Object> params = createParametersMap(windowInfo,
                Collections.singletonMap(WindowParams.ITEM.name(), item)
        );

        MapScreenOptions options = new MapScreenOptions(params);

        Screen screen = createScreen(windowInfo, openType.getOpenMode(), options);
        applyOpenTypeParameters(screen.getWindow(), openType);

        EditorScreen editorScreen = (EditorScreen) screen;
        editorScreen.setEntityToEdit(item);
        show(screen);
        return screen instanceof com.haulmont.cuba.gui.components.Window.Editor
                ? (com.haulmont.cuba.gui.components.Window.Editor) screen
                : new ScreenEditorWrapper(screen);
    }

    @Override
    public com.haulmont.cuba.gui.components.Window.Editor openEditor(WindowInfo windowInfo,
                                                                     Entity item,
                                                                     OpenType openType,
                                                                     Map<String, Object> params) {
        Screen editor = createEditor(windowInfo, item, openType, params);

        editor.show();

        return editor instanceof com.haulmont.cuba.gui.components.Window.Editor
                ? (com.haulmont.cuba.gui.components.Window.Editor) editor
                : new ScreenEditorWrapper(editor);
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
    public com.haulmont.cuba.gui.components.Window.Editor openEditor(WindowInfo windowInfo,
                                                                     Entity item,
                                                                     OpenType openType,
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

        return screen instanceof com.haulmont.cuba.gui.components.Window.Editor
                ? (com.haulmont.cuba.gui.components.Window.Editor) screen
                : new ScreenEditorWrapper(screen);
    }


    @Override
    public com.haulmont.cuba.gui.components.Window.Lookup openLookup(WindowInfo windowInfo,
                                                                     com.haulmont.cuba.gui.components.Window.Lookup.Handler handler,
                                                                     OpenType openType,
                                                                     Map<String, Object> params) {
        params = createParametersMap(windowInfo, params);

        MapScreenOptions options = new MapScreenOptions(params);
        Screen screen = createScreen(windowInfo, openType.getOpenMode(), options);
        applyOpenTypeParameters(screen.getWindow(), openType);

        ((LookupScreen) screen).setSelectHandler(new SelectHandlerAdapter(handler));

        show(screen);

        return screen instanceof com.haulmont.cuba.gui.components.Window.Lookup
                ? (com.haulmont.cuba.gui.components.Window.Lookup) screen
                : new ScreenLookupWrapper(screen);
    }


    @Override
    public com.haulmont.cuba.gui.components.Window.Lookup openLookup(WindowInfo windowInfo,
                                                                     com.haulmont.cuba.gui.components.Window.Lookup.Handler handler,
                                                                     OpenType openType) {
        Map<String, Object> params = createParametersMap(windowInfo, Collections.emptyMap());

        MapScreenOptions options = new MapScreenOptions(params);
        Screen screen = createScreen(windowInfo, openType.getOpenMode(), options);
        applyOpenTypeParameters(screen.getWindow(), openType);

        ((LookupScreen) screen).setSelectHandler(new SelectHandlerAdapter(handler));

        show(screen);

        return screen instanceof com.haulmont.cuba.gui.components.Window.Lookup
                ? (com.haulmont.cuba.gui.components.Window.Lookup) screen
                : new ScreenLookupWrapper(screen);
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
    public void close(Window window) {
        remove(window.getFrameOwner());
    }

    @Override
    protected DialogWindow createDialogWindow(WindowInfo windowInfo) {
        return isCubaScreen(windowInfo)
                ? cubaUiComponents.create(DialogWindow.NAME)
                : super.createDialogWindow(windowInfo);
    }

    @Override
    protected RootWindow createRootWindow(WindowInfo windowInfo) {
        return isCubaScreen(windowInfo)
                ? cubaUiComponents.create(RootWindow.NAME)
                : super.createRootWindow(windowInfo);
    }

    @Override
    protected TabWindow createTabWindow(WindowInfo windowInfo) {
        return isCubaScreen(windowInfo)
                ? cubaUiComponents.create(TabWindow.NAME)
                : super.createTabWindow(windowInfo);
    }

    protected boolean isCubaScreen(WindowInfo windowInfo) {
        String template = windowInfo.getTemplate();
        if (Strings.isNullOrEmpty(template)) {
            // screen without template considered as Jmix screen
            return false;
        }

        Element windowElement = screenXmlLoader.load(template, windowInfo.getId(), Collections.emptyMap());

        String schema = windowElement.getNamespace().getStringValue();
        return schema.startsWith(CubaLoaderConfig.CUBA_XSD_PREFIX);
    }

    @Override
    protected WindowContextImpl createWindowContext(Window window, ScreenOpenDetails openDetails) {
        return new com.haulmont.cuba.gui.sys.WindowContextImpl(window, openDetails.getOpenMode());
    }

    @Override
    public void showNotification(String caption) {
        ui.getNotifications().create()
                .withCaption(caption)
                .show();
    }

    @Override
    public void showNotification(String caption, NotificationType type) {
        ui.getNotifications().create()
                .withCaption(caption)
                .withContentMode(NotificationType.isHTML(type) ? ContentMode.HTML : ContentMode.TEXT)
                .withType(convertNotificationType(type))
                .show();
    }

    @Override
    public void showNotification(String caption, String description, NotificationType type) {
        ui.getNotifications().create()
                .withCaption(caption)
                .withDescription(description)
                .withContentMode(NotificationType.isHTML(type) ? ContentMode.HTML : ContentMode.TEXT)
                .withType(convertNotificationType(type))
                .show();
    }

    protected Notifications.NotificationType convertNotificationType(NotificationType type) {
        switch (type) {
            case TRAY:
            case TRAY_HTML:
                return Notifications.NotificationType.TRAY;

            case ERROR:
            case ERROR_HTML:
                return Notifications.NotificationType.ERROR;

            case HUMANIZED:
            case HUMANIZED_HTML:
                return Notifications.NotificationType.HUMANIZED;

            case WARNING:
            case WARNING_HTML:
                return Notifications.NotificationType.WARNING;

            default:
                throw new UnsupportedOperationException("Unsupported notification type");
        }
    }

    @Override
    public void showMessageDialog(String title, String message, MessageType messageType) {
        Dialogs.MessageDialogBuilder builder = ui.getDialogs().createMessageDialog()
                .withCaption(title)
                .withMessage(message)
                .withContentMode(
                        MessageMode.isHTML(messageType.getMessageMode()) ? ContentMode.HTML : ContentMode.TEXT
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
            builder.withWindowMode(messageType.getMaximized()
                    ? WindowMode.MAXIMIZED
                    : WindowMode.NORMAL);
        }

        builder.show();
    }

    @Override
    public void showOptionDialog(String title, String message, MessageType messageType, Action[] actions) {
        Dialogs.OptionDialogBuilder builder = ui.getDialogs().createOptionDialog()
                .withCaption(title)
                .withMessage(message)
                .withContentMode(
                        MessageMode.isHTML(messageType.getMessageMode()) ? ContentMode.HTML : ContentMode.TEXT
                )
                .withActions(actions);

        if (messageType.getWidth() != null) {
            SizeUnit sizeUnit = messageType.getWidthUnit() != null ? messageType.getWidthUnit() : SizeUnit.PIXELS;
            builder.withWidth(messageType.getWidth() + sizeUnit.getSymbol());
        }
        if (messageType.getMaximized() != null) {
            builder.withWindowMode(messageType.getMaximized()
                    ? WindowMode.MAXIMIZED
                    : WindowMode.NORMAL);
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

    @Deprecated
    protected void applyOpenTypeParameters(Window window, OpenType openType) {
        if (window instanceof DialogWindow) {
            DialogWindow dialogWindow = (DialogWindow) window;

            if (openType.getCloseOnClickOutside() != null) {
                dialogWindow.setCloseOnClickOutside(openType.getCloseOnClickOutside());
            }
            if (openType.getMaximized() != null) {
                dialogWindow.setWindowMode(openType.getMaximized() ? WindowMode.MAXIMIZED : WindowMode.NORMAL);
            }
            if (openType.getModal() != null) {
                dialogWindow.setModal(openType.getModal());
            }
            if (openType.getResizable() != null) {
                dialogWindow.setResizable(openType.getResizable());
            }
            if (openType.getWidth() != null) {
                dialogWindow.setDialogWidth(openType.getWidthString());
            }
            if (openType.getHeight() != null) {
                dialogWindow.setDialogHeight(openType.getHeightString());
            }
        }

        if (openType.getCloseable() != null) {
            window.setCloseable(openType.getCloseable());
        }
    }

    @Override
    protected ComponentLoaderContext createComponentLoaderContext(ScreenOptions options) {
        return new com.haulmont.cuba.gui.xml.layout.loaders.ComponentLoaderContext(options);
    }

    @Override
    protected <T extends Screen> void loadWindowFromXml(Element element, WindowInfo windowInfo, Window window,
                                                        T controller, ComponentLoaderContext componentLoaderContext) {
        if (controller instanceof LegacyFrame) {
            screenViewsLoader.deployViews(element);

            initDsContext(controller, element, componentLoaderContext);

            DsContext dsContext = ((LegacyFrame) controller).getDsContext();
            if (dsContext != null) {
                dsContext.setFrameContext(controller.getWindow().getContext());
            }
        }

        super.loadWindowFromXml(element, windowInfo, window, controller, componentLoaderContext);
    }

    @Override
    protected void findMessageGroup(Element element, String descriptorPath,
                                    ComponentLoaderContext componentLoaderContext) {
        String messagesPack = element.attributeValue("messagesPack");
        if (messagesPack != null) {
            componentLoaderContext.setMessageGroup(messagesPack);
        } else {
            super.findMessageGroup(element, descriptorPath, componentLoaderContext);
        }
    }

    protected void initDsContext(Screen screen, Element screenDescriptor, ComponentLoaderContext context) {
        DsContext dsContext = loadDsContext(screenDescriptor);
        initDatasources(screen.getWindow(), dsContext, context.getParams());

        ((com.haulmont.cuba.gui.xml.layout.loaders.ComponentLoaderContext) context).setDsContext(dsContext);
    }

    protected DsContext loadDsContext(Element element) {
        DataSupplier dataSupplier;

        String dataSupplierClass = element.attributeValue("dataSupplier");
        if (StringUtils.isEmpty(dataSupplierClass)) {
            dataSupplier = defaultDataSupplier;
        } else {
            Class<Object> aClass = ReflectionHelper.getClass(dataSupplierClass);
            try {
                dataSupplier = (DataSupplier) aClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Unable to create data supplier for screen", e);
            }
        }

        //noinspection UnnecessaryLocalVariable
        DsContext dsContext = new DsContextLoader(dataSupplier).loadDatasources(element.element("dsContext"), null, null);
        return dsContext;
    }

    protected void initDatasources(Window window, DsContext dsContext, @SuppressWarnings("unused") Map<String, Object> params) {
        ((LegacyFrame) window.getFrameOwner()).setDsContext(dsContext);

        for (Datasource ds : dsContext.getAll()) {
            if (Datasource.State.NOT_INITIALIZED.equals(ds.getState()) && ds instanceof DatasourceImplementation) {
                ((DatasourceImplementation) ds).initialized();
            }
        }
    }

    @Override
    protected void fireScreenInitEvent(FrameOwner frameOwner, Class<Screen.InitEvent> eventType, Screen.InitEvent event) {
        Screen screen = (Screen) frameOwner;

        if (!(screen instanceof LegacyFrame)
                && screen.getWindow() instanceof com.haulmont.cuba.gui.components.Window) {
            Window window = screen.getWindow();

            CubaSettingsFacet facet = facets.create(CubaSettingsFacet.class);
            facet.setId("cubaSettingsFacet");
            window.addFacet(facet);
        }

        super.fireScreenInitEvent(screen, eventType, event);
    }

    @Override
    protected void fireScreenAfterShowEvent(FrameOwner frameOwner, Class<Screen.AfterShowEvent> eventType, Screen.AfterShowEvent event) {
        Screen screen = (Screen) frameOwner;

        if (screen instanceof CubaLegacySettings) {
            ((CubaLegacySettings) screen).applySettings(getSettingsImpl(screen.getId()));
        }

        if (screen instanceof LegacyFrame) {
            WindowContext windowContext = screen.getWindow().getContext();
            if (!WindowParams.DISABLE_RESUME_SUSPENDED.getBool(windowContext)) {
                DsContext dsContext = ((LegacyFrame) screen).getDsContext();
                if (dsContext != null) {
                    ((DsContextImplementation) dsContext).resumeSuspended();
                }
            }
        }

        super.fireScreenAfterShowEvent(screen, eventType, event);
    }

    @Override
    protected void fireScreenBeforeShowEvent(FrameOwner frameOwner, Class<Screen.BeforeShowEvent> eventType, Screen.BeforeShowEvent event) {
        Screen screen = (Screen) frameOwner;

        if (screen instanceof CubaLegacySettings) {
            ((CubaLegacySettings) screen).applyDataLoadingSettings(getSettingsImpl(screen.getId()));
        }

        super.fireScreenBeforeShowEvent(screen, eventType, event);

        ((CubaScreenDataImpl) UiControllerUtils.getScreenData(screen))
                .getLoadBeforeShowStrategy().loadData(screen);
    }

    protected Settings getSettingsImpl(String id) {
        return new SettingsImpl(id);
    }
}
