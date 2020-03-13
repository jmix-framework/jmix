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

import com.vaadin.server.AbstractClientConnector;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import io.jmix.core.*;
import io.jmix.core.security.LoginException;
import io.jmix.core.security.UserSession;
import io.jmix.core.security.UserSessionSource;
import io.jmix.ui.actions.Action;
import io.jmix.ui.actions.BaseAction;
import io.jmix.ui.actions.DialogAction;
import io.jmix.ui.components.RootWindow;
import io.jmix.ui.exception.ExceptionHandlers;
import io.jmix.ui.executors.IllegalConcurrentAccessException;
import io.jmix.ui.icons.CubaIcon;
import io.jmix.ui.icons.Icons;
import io.jmix.ui.logging.AppLog;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.sys.*;
import io.jmix.ui.theme.ThemeConstants;
import io.jmix.ui.theme.ThemeConstantsRepository;
import io.jmix.ui.util.OperationResult;
import io.jmix.ui.util.UnknownOperationResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
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

    public static final String NAME = "jmix_App";
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

    protected AppLog appLog;

    protected Connection connection;

    protected ExceptionHandlers exceptionHandlers;

    @Inject
    protected WindowConfig windowConfig;
    @Inject
    protected ThemeConstantsRepository themeConstantsRepository;
    @Inject
    protected UserSessionSource userSessionSource;
    @Inject
    protected MessageTools messageTools;
    /*@Inject
    protected SettingsClient settingsClient;*/ // todo settings

    @Inject
    protected Events events;
    @Inject
    protected BeanLocator beanLocator;
    @Inject
    protected ConfigInterfaces configInterfaces;

    protected AppCookies cookies;

    protected LinkHandler linkHandler;

    protected BackgroundTaskManager backgroundTaskManager = new BackgroundTaskManager();

    protected ThemeConstants themeConstants;

    public App() {
        log.trace("Creating application {}", this);
    }

    protected ThemeConstants loadTheme() {
        WebConfig webConfig = configInterfaces.getConfig(WebConfig.class);
        GlobalConfig globalConfig = configInterfaces.getConfig(GlobalConfig.class);

        String appWindowTheme = webConfig.getAppWindowTheme();
        String userAppTheme = cookies.getCookieValue(APP_THEME_COOKIE_PREFIX + globalConfig.getWebContextName());
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

    public abstract void loginOnStart() throws LoginException;

    protected Connection createConnection() {
        return beanLocator.getPrototype(Connection.NAME);
    }

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
                getAppLog().log(event);
            } catch (Throwable e) {
                log.error("Error handling exception\nOriginal exception:\n{}\nException in handlers:\n{}",
                        ExceptionUtils.getStackTrace(event.getThrowable()), ExceptionUtils.getStackTrace(e)
                );
            }
        });

        log.debug("Initializing application");

        WebConfig webConfig = configInterfaces.getConfig(WebConfig.class);

        appLog = new AppLog(webConfig.getAppLogMaxItemsCount(), beanLocator.get(TimeSource.NAME));

        connection = createConnection();
        exceptionHandlers = new ExceptionHandlers(this, beanLocator);
        cookies = new AppCookies();

        themeConstants = loadTheme();

        // get default locale from config
        Locale targetLocale = resolveLocale(requestLocale);
        setLocale(targetLocale);
    }

    protected Locale resolveLocale(@Nullable Locale requestLocale) {
        GlobalConfig globalConfig = configInterfaces.getConfig(GlobalConfig.class);

        Map<String, Locale> locales = globalConfig.getAvailableLocales();

        if (globalConfig.getLocaleSelectVisible()) {
            String lastLocale = getCookieValue(COOKIE_LOCALE);
            if (lastLocale != null) {
                for (Locale locale : locales.values()) {
                    if (locale.toLanguageTag().equals(lastLocale)) {
                        return locale;
                    }
                }
            }
        }

        if (requestLocale != null) {
            Locale requestTrimmedLocale = messageTools.trimLocale(requestLocale);
            if (locales.containsValue(requestTrimmedLocale)) {
                return requestTrimmedLocale;
            }

            // if not found and application locale contains country, try to match by language only
            if (!StringUtils.isEmpty(requestLocale.getCountry())) {
                Locale appLocale = Locale.forLanguageTag(requestLocale.getLanguage());
                for (Locale locale : locales.values()) {
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
     * @deprecated Use {@link Screens#create(Class, Screens.LaunchMode)} with {@link OpenMode#ROOT}
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
     * @return Current connection object
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * @return WindowManagerImpl instance or null if the current UI has no MainWindow
     * @deprecated Get screens API from {@link AppUI} instead.
     */
    @Deprecated
    public WebScreens getWindowManager() {
        AppUI ui = AppUI.getCurrent();
        if (ui == null) {
            return null;
        }

        RootWindow topLevelWindow = ui.getTopLevelWindow();
        if (topLevelWindow == null) {
            return null;
        } else {
            return (WebScreens) UiControllerUtils.getScreenContext(topLevelWindow.getFrameOwner())
                    .getScreens();
        }
    }

    public AppLog getAppLog() {
        return appLog;
    }

    public ExceptionHandlers getExceptionHandlers() {
        return exceptionHandlers;
    }

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
        UserSession session = getConnection().getSession();
        if (session != null) {
            session.setLocale(locale);
        }

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
        GlobalConfig globalConfig = configInterfaces.getConfig(GlobalConfig.class);

        addCookie(APP_THEME_COOKIE_PREFIX + globalConfig.getWebContextName(), themeName);
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
        UserSession currentSession = AppUI.getCurrent().getUserSession();
        WebConfig webConfig = configInterfaces.getConfig(WebConfig.class);

        List<AppUI> authenticatedUIs = getAppUIs()
                .stream()
                .filter(ui ->
                        ui.hasAuthenticatedSession()
                                && (Objects.equals(ui.getUserSession(), currentSession)
                                || webConfig.getForceRefreshAuthenticatedTabs()))
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

                // also remove all native Vaadin windows, that is not under CUBA control
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
     * Sets UserSession from {@link Connection#getSession()}
     * and re-initializes the given {@code ui}.
     */
    public void recreateUi(AppUI ui) {
        ui.setUserSession(connection.getSession());

        removeAllWindows(Collections.singletonList(ui));
        createTopLevelWindow(ui);

        ui.getPage().open(ControllerUtils.getLocationWithoutParams(), "_self");
    }

    protected void clearSettingsCache() {
        // ((WebSettingsClient) settingsClient).clearCache(); todo settings
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

                Messages messages = beanLocator.get(Messages.NAME);
                Icons icons = beanLocator.get(Icons.NAME);

                Dialogs dialogs = ui.getDialogs();

                dialogs.createOptionDialog()
                        .withCaption(messages.getMessage("closeUnsaved.caption"))
                        .withMessage(messages.getMessage("discardChangesOnClose"))
                        .withActions(
                                new BaseAction("closeApplication")
                                        .withCaption(messages.getMessage("closeApplication"))
                                        .withIcon(icons.get(CubaIcon.DIALOG_OK))
                                        .withHandler(event -> {
                                            performStandardLogout(ui);

                                            result.success();
                                        }),
                                new DialogAction(DialogAction.Type.CANCEL, Action.Status.PRIMARY)
                                        .withHandler(event -> {

                                            result.fail();
                                        })
                        )
                        .show();

                return OperationResult.fail();
            } else {
                forceLogout();

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
//        todo implement
//        ((WebScreens) ui.getScreens()).saveScreenHistory();
//
//        ((WebScreens) ui.getScreens()).saveScreenSettings();

        forceLogout();
    }

    protected void forceLogout() {
        removeAllWindows();

        Connection connection = getConnection();
        connection.logout();
    }
}
