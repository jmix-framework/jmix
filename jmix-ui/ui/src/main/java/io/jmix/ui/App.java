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

package io.jmix.ui;

import com.google.common.base.Strings;
import com.vaadin.server.AbstractClientConnector;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import io.jmix.core.CoreProperties;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.annotation.Internal;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.action.DialogAction;
import io.jmix.ui.component.RootWindow;
import io.jmix.ui.event.screen.CloseWindowsInternalEvent;
import io.jmix.ui.exception.ExceptionHandlers;
import io.jmix.ui.executor.IllegalConcurrentAccessException;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.settings.UiSettingsCache;
import io.jmix.ui.sys.*;
import io.jmix.ui.theme.ThemeConstants;
import io.jmix.ui.theme.ThemeConstantsRepository;
import io.jmix.ui.util.OperationResult;
import io.jmix.ui.util.UnknownOperationResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nullable;
import javax.servlet.ServletContext;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Central class of the web application. An instance of this class is created for each client's session and is bound
 * to {@link VaadinSession}.
 * <br>
 * Use {@link #getInstance()} static method to obtain the reference to the current App instance.
 */
public abstract class App {

    public static final String DEFAULT_THEME_NAME = "halo";

    public static final String USER_SESSION_ATTR = "userSessionId";

    public static final String APP_THEME_COOKIE_PREFIX = "APP_THEME_NAME_";

    public static final String COOKIE_LOCALE = "LAST_LOCALE";
    public static final String COOKIE_REMEMBER_ME = "rememberMe";
    public static final String COOKIE_LOGIN = "rememberMe.Login";
    public static final String COOKIE_PASSWORD = "rememberMe.Password";

    private static final Logger log = LoggerFactory.getLogger(App.class);

    static {
        AbstractClientConnector.setIncorrectConcurrentAccessHandler(() -> {
            throw new IllegalConcurrentAccessException();
        });
    }

    protected ExceptionHandlers exceptionHandlers;

    @Autowired
    protected CoreProperties coreProperties;
    @Autowired(required = false)
    protected ServletContext servletContext;
    @Autowired
    protected WindowConfig windowConfig;
    @Autowired
    protected ThemeConstantsRepository themeConstantsRepository;
    @Autowired
    protected MessageTools messageTools;
    @Autowired(required = false)
    protected UiSettingsCache settingsCache;

    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    protected UiProperties uiProperties;
    @Autowired
    protected UiThemeProperties uiThemeProperties;

    protected AppCookies cookies;

    protected BackgroundTaskManager backgroundTaskManager = new BackgroundTaskManager();

    protected ThemeConstants themeConstants;

    public App() {
        log.trace("Creating application {}", this);
    }

    protected ThemeConstants loadTheme() {
        String appWindowTheme = uiThemeProperties.getName();
        String userAppTheme = cookies.getCookieValue(APP_THEME_COOKIE_PREFIX + getContextPathName());
        if (userAppTheme != null) {
            if (!Objects.equals(userAppTheme, appWindowTheme)) {
                // check theme support
                Set<String> supportedThemes = themeConstantsRepository.getAvailableThemes();
                if (supportedThemes.contains(userAppTheme)) {
                    appWindowTheme = userAppTheme;
                }
            }
        }

        ThemeConstants theme = themeConstantsRepository.getConstants(appWindowTheme);
        if (theme == null) {
            // fallback to default
            theme = themeConstantsRepository.getConstants(DEFAULT_THEME_NAME);
        }

        return theme;
    }

    protected void applyTheme(String appWindowTheme) {
        ThemeConstants theme = themeConstantsRepository.getConstants(appWindowTheme);
        if (theme == null) {
            // fallback to default
            theme = themeConstantsRepository.getConstants(DEFAULT_THEME_NAME);
        }

        if (theme == null) {
            log.warn("Unable to use theme constants '{}'", appWindowTheme);
        } else {
            this.themeConstants = theme;
            setUserAppTheme(appWindowTheme);
        }
    }

    /**
     * Initializes exception handlers immediately after login and logout.
     * Can be overridden in descendants to manipulate exception handlers programmatically.
     *
     * @param isConnected true after login, false after logout
     */
    protected void initExceptionHandlers(boolean isConnected) {
        if (isConnected) {
            exceptionHandlers.createByConfiguration();
        } else {
            exceptionHandlers.removeAll();
        }
    }

    public ThemeConstants getThemeConstants() {
        return themeConstants;
    }

    public List<AppUI> getAppUIs() {
        List<AppUI> list = new ArrayList<>();
        for (UI ui : VaadinSession.getCurrent().getUIs()) {
            if (ui instanceof AppUI)
                list.add((AppUI) ui);
            else
                log.warn("Invalid UI in the session: {}", ui);
        }
        return list;
    }

    public abstract void loginOnStart();

    /**
     * Called when <em>the first</em> UI of the session is initialized.
     */
    protected void init(Locale requestLocale) {
        VaadinSession vSession = VaadinSession.getCurrent();
        vSession.setAttribute(App.class, this);

        vSession.setLocale(messageTools.getDefaultLocale());

        // set root error handler for all session
        vSession.setErrorHandler(event -> {
            try {
                getExceptionHandlers().handle(event);
            } catch (Throwable e) {
                log.error("Error handling exception\nOriginal exception:\n{}\nException in handlers:\n{}",
                        ExceptionUtils.getStackTrace(event.getThrowable()), ExceptionUtils.getStackTrace(e)
                );
            }
        });

        log.debug("Initializing application");

        exceptionHandlers = new ExceptionHandlers(this, applicationContext);
        initExceptionHandlers(true); //TODO Connection is not created now
        cookies = new AppCookies();

        themeConstants = loadTheme();

        // get default locale from config
        Locale targetLocale = resolveLocale(requestLocale);
        setLocale(targetLocale);
    }

    protected Locale resolveLocale(@Nullable Locale requestLocale) {
        List<Locale> locales = coreProperties.getAvailableLocales();

        if (uiProperties.isLocaleSelectVisible()) {
            String lastLocale = getCookieValue(COOKIE_LOCALE);
            if (lastLocale != null) {
                for (Locale locale : locales) {
                    if (locale.toLanguageTag().equals(lastLocale)) {
                        return locale;
                    }
                }
            }
        }

        if (requestLocale != null) {
            Locale requestTrimmedLocale = requestLocale.stripExtensions();
            if (locales.contains(requestTrimmedLocale)) {
                return requestTrimmedLocale;
            }

            // if not found and application locale contains country, try to match by language only
            if (!StringUtils.isEmpty(requestLocale.getCountry())) {
                Locale appLocale = Locale.forLanguageTag(requestLocale.getLanguage());
                for (Locale locale : locales) {
                    if (Locale.forLanguageTag(locale.getLanguage()).equals(appLocale)) {
                        return locale;
                    }
                }
            }
        }

        // return default locale
        return messageTools.getDefaultLocale();
    }

    /**
     * Called on each browser tab initialization.
     */
    public void createTopLevelWindow(AppUI ui) {
        String topLevelWindowId = routeTopLevelWindowId();

        Screens screens = ui.getScreens();

        Screen screen = screens.create(topLevelWindowId, OpenMode.ROOT);
        screens.show(screen);
    }

    protected abstract String routeTopLevelWindowId();

    // todo move to UI
    public void createTopLevelWindow() {
        createTopLevelWindow(AppUI.getCurrent());
    }

    /**
     * Initialize new TopLevelWindow and replace current.
     *
     * @param topLevelWindowId target top level window id
     * @deprecated Use {@link Screens#create(Class, OpenMode)} with {@link OpenMode#ROOT}
     */
    @Deprecated
    public void navigateTo(String topLevelWindowId) {
        AppUI ui = AppUI.getCurrent();

        WindowInfo windowInfo = windowConfig.getWindowInfo(topLevelWindowId);

        Screens screens = ui.getScreens();

        Screen screen = screens.create(windowInfo.asScreen(), OpenMode.ROOT);
        screens.show(screen);
    }

    /**
     * Called from heartbeat request. <br>
     * Used for ping middleware session and show session messages
     */
    public void onHeartbeat() {
        // todo do we need this ?
        /*Connection connection = getConnection();

        boolean sessionIsAlive = false;
        if (connection.isAuthenticated()) {
            // Ping middleware session if connected and show messages
            log.debug("Ping middleware session");

            try {
                String message = userSessionService.getMessages();

                sessionIsAlive = true;

                if (message != null) {
                    message = message.replace("\n", "<br/>");

                    // todo implement
                    // getWindowManager().showNotification(message, Frame.NotificationType.ERROR_HTML);
                }
            } catch (NoUserSessionException ignored) {
                // ignore no user session exception
            } catch (Exception e) {
                log.warn("Exception while session ping", e);
            }
        }

        if (sessionIsAlive) {
            events.publish(new SessionHeartbeatEvent(this));
        }*/
    }

    /**
     * @return Current App instance. Can be invoked anywhere in application code.
     * @throws IllegalStateException if no application instance is bound to the current {@link VaadinSession}
     */
    public static App getInstance() {
        VaadinSession vSession = VaadinSession.getCurrent();
        if (vSession == null) {
            throw new IllegalStateException("No VaadinSession found");
        }
        if (!vSession.hasLock()) {
            throw new IllegalStateException("VaadinSession is not owned by the current thread");
        }
        App app = vSession.getAttribute(App.class);
        if (app == null) {
            throw new IllegalStateException("No App is bound to the current VaadinSession");
        }
        return app;
    }

    /**
     * @return true if an {@link App} instance is currently bound and can be safely obtained by {@link #getInstance()}
     */
    public static boolean isBound() {
        VaadinSession vSession = VaadinSession.getCurrent();
        return vSession != null
                && vSession.hasLock()
                && vSession.getAttribute(App.class) != null;
    }

    /**
     * @return WindowManagerImpl instance or null if the current UI has no MainWindow
     * @deprecated Get screens API from {@link AppUI} instead.
     */
    @Deprecated
    @Nullable
    public ScreensImpl getWindowManager() {
        AppUI ui = AppUI.getCurrent();
        if (ui == null) {
            return null;
        }

        RootWindow topLevelWindow = ui.getTopLevelWindow();
        if (topLevelWindow == null) {
            return null;
        } else {
            return (ScreensImpl) UiControllerUtils.getScreenContext(topLevelWindow.getFrameOwner())
                    .getScreens();
        }
    }

    public ExceptionHandlers getExceptionHandlers() {
        return exceptionHandlers;
    }

    @Nullable
    public String getCookieValue(String name) {
        return cookies.getCookieValue(name);
    }

    public int getCookieMaxAge(String name) {
        return cookies.getCookieMaxAge(name);
    }

    public void addCookie(String name, String value, int maxAge) {
        cookies.addCookie(name, value, maxAge);
    }

    public void addCookie(String name, String value) {
        cookies.addCookie(name, value);
    }

    public void removeCookie(String name) {
        cookies.removeCookie(name);
    }

    public Locale getLocale() {
        return VaadinSession.getCurrent().getLocale();
    }

    public void setLocale(Locale locale) {
        AppUI currentUi = AppUI.getCurrent();
        // it can be null if we handle request in a custom RequestHandler
        if (currentUi != null) {
            currentUi.setLocale(locale);
            currentUi.updateClientSystemMessages(locale);
        }

        VaadinSession.getCurrent().setLocale(locale);

        for (AppUI ui : getAppUIs()) {
            if (ui != currentUi) {
                ui.accessSynchronously(() -> {
                    ui.setLocale(locale);
                    ui.updateClientSystemMessages(locale);
                });
            }
        }
    }

    public void setUserAppTheme(String themeName) {
        addCookie(APP_THEME_COOKIE_PREFIX + getContextPathName(), themeName);
    }

    public void addBackgroundTask(Future task) {
        backgroundTaskManager.addTask(task);
    }

    public void removeBackgroundTask(Future task) {
        backgroundTaskManager.removeTask(task);
    }

    public void cleanupBackgroundTasks() {
        backgroundTaskManager.cleanupTasks();
    }

    /**
     * Removes all windows from all UIs.
     */
    public void removeAllWindows() {
        List<AppUI> authenticatedUIs = getAppUIs()
                .stream()
                .filter(AppUI::hasAuthenticatedSession)
                .collect(Collectors.toList());

        removeAllWindows(authenticatedUIs);
    }

    /**
     * Removes all windows in the given {@code uis}.
     *
     * @param uis {@link AppUI} instances
     */
    protected void removeAllWindows(List<AppUI> uis) {
        log.debug("Closing all windows in all UIs");
        try {
            for (AppUI ui : uis) {
                Screens screens = ui.getScreens();
                if (screens != null) {
                    Screen rootScreen = screens.getOpenedScreens().getRootScreenOrNull();
                    if (rootScreen != null) {
                        screens.removeAll();

                        screens.remove(rootScreen);
                    }
                }

                // also remove all native Vaadin windows, that is not under Jmix control
                Window[] windows = ui.getWindows().toArray(new Window[0]);

                for (Window win : windows) {
                    ui.removeWindow(win);
                }

                List<Notification> notifications = ui.getExtensions()
                        .stream()
                        .filter(ext -> ext instanceof Notification)
                        .map(ext -> (Notification) ext)
                        .collect(Collectors.toList());

                notifications.forEach(Notification::close);

            }
        } catch (Throwable e) {
            log.error("Error closing all windows", e);
        }
    }

    /**
     * Try to perform logout. If there are unsaved changes in opened windows then logout will not be performed and
     * unsaved changes dialog will appear.
     *
     * @return operation result object
     */
    public OperationResult logout() {
        AppUI ui = AppUI.getCurrent();

        try {
            RootWindow topLevelWindow = ui != null ? ui.getTopLevelWindow() : null;
            if (topLevelWindow != null) {
                Screens screens = ui.getScreens();

                if (!screens.hasUnsavedChanges()) {
                    performStandardLogout(ui);

                    return OperationResult.success();
                }

                UnknownOperationResult result = new UnknownOperationResult();

                Messages messages = applicationContext.getBean(Messages.class);
                Icons icons = applicationContext.getBean(Icons.class);

                Dialogs dialogs = ui.getDialogs();

                dialogs.createOptionDialog()
                        .withCaption(messages.getMessage("closeUnsaved.caption"))
                        .withMessage(messages.getMessage("discardChangesOnClose"))
                        .withActions(
                                new BaseAction("closeApplication")
                                        .withCaption(messages.getMessage("closeApplication"))
                                        .withIcon(icons.get(JmixIcon.DIALOG_OK))
                                        .withHandler(event -> {
                                            performStandardLogout(ui);

                                            result.success();
                                        }),
                                new DialogAction(DialogAction.Type.CANCEL, Action.Status.PRIMARY)
                                        .withHandler(event ->
                                                result.fail())
                        )
                        .show();

                return OperationResult.fail();
            } else {
                performForceLogout();

                return OperationResult.success();
            }
        } catch (Exception e) {
            log.error("Error on logout", e);

            String url = ControllerUtils.getLocationWithoutParams() + "?restartApp";
            if (ui != null) {
                ui.getPage().open(url, "_self");
            }
            return new UnknownOperationResult();
        }
    }

    protected void performStandardLogout(AppUI ui) {
        closeWindowsInternal(true);
        clearSettingsCache();

        forceLogout();
    }

    protected void performForceLogout() {
        closeWindowsInternal(false);
        clearSettingsCache();

        forceLogout();
    }

    protected void forceLogout() {
        String contextPath = servletContext == null ? null : servletContext.getContextPath();
        String logoutPath = Strings.isNullOrEmpty(contextPath) ? "/logout" : contextPath + "/logout";

        AppUI.getCurrent().getPage().setLocation(logoutPath);
    }

    /**
     * Removes all windows from all UIs and fires {@link CloseWindowsInternalEvent} application event.
     *
     * @param fireEvent fire event or not
     */
    public void closeWindowsInternal(boolean fireEvent) {
        if (fireEvent) {
            AppUI ui = AppUI.getCurrent();
            applicationContext.publishEvent(new CloseWindowsInternalEvent(ui.getScreens()));
        }

        removeAllWindows();
    }

    @Nullable
    protected String getContextPathName() {
        String contextPath = servletContext == null ? null : servletContext.getContextPath();
        if (Strings.isNullOrEmpty(contextPath)) {
            return "ROOT";
        }

        return contextPath.substring(1);
    }

    protected void clearSettingsCache() {
        if (settingsCache != null) {
            settingsCache.clear();
        }
    }

    @Internal
    public void forceRefreshUIsExceptCurrent() {
        AppUI current = AppUI.getCurrent();
        if (current == null) {
            // The current AppUI may be null in case REST API auth token is requested
            return;
        }

        List<AppUI> uis = getAppUIs()
                .stream()
                .filter(ui -> ui != current)
                .collect(Collectors.toList());

        removeAllWindows(uis);

        for (AppUI ui : uis) {
            ui.getSession().removeUI(ui);
        }
    }
}
