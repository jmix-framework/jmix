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

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.*;
import com.vaadin.shared.ui.ui.Transport;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import io.jmix.core.*;
import io.jmix.core.security.LoginException;
import io.jmix.core.security.NoUserSessionException;
import io.jmix.core.security.UserSession;
import io.jmix.core.security.UserSessionSource;
import io.jmix.ui.components.RootWindow;
import io.jmix.ui.events.AppInitializedEvent;
import io.jmix.ui.events.SessionHeartbeatEvent;
import io.jmix.ui.events.UIRefreshEvent;
import io.jmix.ui.exception.UiExceptionHandler;
import io.jmix.ui.icons.IconResolver;
import io.jmix.ui.navigation.*;
import io.jmix.ui.sys.*;
import io.jmix.ui.sys.events.UiEventsMulticaster;
import io.jmix.ui.theme.ThemeConstantsRepository;
import io.jmix.ui.widgets.AppUIUtils;
import io.jmix.ui.widgets.CubaTimer;
import io.jmix.ui.widgets.JmixFileDownloader;
import io.jmix.ui.widgets.JmixTimer;
import io.jmix.ui.widgets.client.ui.AppUIClientRpc;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Widgetset("${cuba.web.widgetSet:io.jmix.ui.widgets.WidgetSet}")
@Theme("${cuba.web.theme:halo}")
@Push(transport = Transport.WEBSOCKET_XHR)
@SpringUI
public class AppUI extends UI implements ErrorHandler, UiExceptionHandler.UiContext {

    private static final Logger log = LoggerFactory.getLogger(AppUI.class);

    protected App app;

    public static final String LAST_REQUEST_ACTION_ATTR = "lastRequestAction";
    public static final String LAST_REQUEST_PARAMS_ATTR = "lastRequestParams";

    @Inject
    protected Messages messages;
    @Inject
    protected Events events;
    @Inject
    protected ConfigInterfaces configuration;

//    @Inject
//    protected UserSettingsTools userSettingsTools; todo settings
    @Inject
    protected ThemeConstantsRepository themeConstantsRepository;

    @Inject
    protected UserSessionSource userSessionSource;
//    @Inject
//    protected UserSessionService userSessionService; todo ping session ?

    @Inject
    protected UiEventsMulticaster uiEventsMulticaster;

    @Inject
    protected IconResolver iconResolver;
    @Inject
    protected WebJarResourceResolver webJarResourceResolver;

    @Inject
    protected BeanLocator beanLocator;

    protected TestIdManager testIdManager = new TestIdManager();

    protected boolean testMode = false;
    protected boolean performanceTestMode = false;

    protected JmixFileDownloader fileDownloader;

    protected RootWindow topLevelWindow;

    protected Fragments fragments;
    protected Screens screens;
    protected Dialogs dialogs;
    protected Notifications notifications;
    protected WebBrowserTools webBrowserTools;


    protected UrlChangeHandler urlChangeHandler;
    protected UrlRouting urlRouting;
    protected History history;

    protected UserSession userSession;

    public UserSession getUserSession() {
        return userSession;
    }

    public void setUserSession(UserSession userSession) {
        this.userSession = userSession;
    }

    /**
     * Dynamically init external JS libraries.
     * You should create JavaScriptExtension class and extend UI object here. <br>
     * <p>
     * Example: <br>
     * <pre><code>
     * JavaScriptExtension:
     *
     * {@literal @}JavaScript("resources/jquery/jquery-1.10.2.min.js")
     * public class JQueryIntegration extends AbstractJavaScriptExtension {
     *
     *     {@literal @}Override
     *     public void extend(AbstractClientConnector target) {
     *         super.extend(target);
     *     }
     *
     *     {@literal @}Override
     *     protected Class&lt;? extends ClientConnector&gt; getSupportedParentType() {
     *         return UI.class;
     *     }
     * }
     *
     * AppUI:
     *
     * protected void initJsLibraries() {
     *     new JQueryIntegration().extend(this);
     * }</code></pre>
     * <p>
     * If you want to include scripts to generated page statically see todo CubaBootstrapListener
     */
    protected void initJsLibraries() {
    }

    protected void initInternalComponents() {
        fileDownloader = new JmixFileDownloader();
        fileDownloader.extend(this);
    }

    protected App createApplication() {
        return beanLocator.getPrototype(App.NAME);
    }

    @Override
    public Screens getScreens() {
        return screens;
    }

    protected void setScreens(Screens screens) {
        this.screens = screens;
    }

    @Override
    public Dialogs getDialogs() {
        return dialogs;
    }

    protected void setDialogs(Dialogs dialogs) {
        this.dialogs = dialogs;
    }

    @Override
    public Notifications getNotifications() {
        return notifications;
    }

    protected void setNotifications(Notifications notifications) {
        this.notifications = notifications;
    }

    @Override
    public WebBrowserTools getWebBrowserTools() {
        return webBrowserTools;
    }

    protected void setWebBrowserTools(WebBrowserTools webBrowserTools) {
        this.webBrowserTools = webBrowserTools;
    }

    @Override
    public Fragments getFragments() {
        return fragments;
    }

    protected void setFragments(Fragments fragments) {
        this.fragments = fragments;
    }


    public UrlRouting getUrlRouting() {
        return urlRouting;
    }

    public void setUrlRouting(UrlRouting urlRouting) {
        this.urlRouting = urlRouting;
    }

    public UrlChangeHandler getUrlChangeHandler() {
        return urlChangeHandler;
    }

    public void setUrlChangeHandler(UrlChangeHandler urlChangeHandler) {
        this.urlChangeHandler = urlChangeHandler;
    }

    public History getHistory() {
        return history;
    }

    public void setHistory(History history) {
        this.history = history;
    }

    @Override
    protected void init(VaadinRequest request) {
        log.trace("Initializing UI {}", this);


        NavigationState requestedState = getUrlRouting().getState();

        try {
            GlobalConfig globalConfig = configuration.getConfig(GlobalConfig.class);

            this.testMode = globalConfig.getTestMode();
            this.performanceTestMode = globalConfig.getPerformanceTestMode();
            // init error handlers
            setErrorHandler(this);

            // do not grab focus
            setTabIndex(-1);

            initJsLibraries();

            initInternalComponents();

            if (!App.isBound()) {
                App app = createApplication();
                app.init(request.getLocale());

                this.app = app;

                publishAppInitializedEvent(app);
            } else {
                this.app = App.getInstance();
            }

            Connection connection = app.getConnection();
            if (connection != null && !isUserSessionAlive(connection)) {
                connection.logout();

                Notification.show(
                        messages.getMessage("app.sessionExpiredCaption"),
                        messages.getMessage("app.sessionExpiredMessage"),
                        Notification.Type.HUMANIZED_MESSAGE);
            }

            if (connection != null) {
                setUserSession(connection.getSession());
            }

            setupUI();
        } catch (Exception e) {
            log.error("Unable to init ui", e);

            // unable to connect to middle ware
            showCriticalExceptionMessage(e);
            return;
        }


        processExternalLink(request, requestedState);
    }

    @Inject
    protected void setApplicationContext(ApplicationContext applicationContext) {
        Dialogs dialogs = new WebDialogs(this);
        autowireContext(dialogs, applicationContext);
        setDialogs(dialogs);

        Notifications notifications = new WebNotifications(this);
        autowireContext(notifications, applicationContext);
        setNotifications(notifications);

        WebBrowserTools webBrowserTools = new WebBrowserToolsImpl(this);
        autowireContext(webBrowserTools, applicationContext);
        setWebBrowserTools(webBrowserTools);

        Fragments fragments = new WebFragments(this);
        autowireContext(fragments, applicationContext);
        setFragments(fragments);

        Screens screens = new WebScreens(this);
        autowireContext(screens, applicationContext);
        setScreens(screens);


        UrlRouting urlRouting = new WebUrlRouting(this);
        autowireContext(urlRouting, applicationContext);
        setUrlRouting(urlRouting);

        History history = new WebHistory(this);
        autowireContext(history, applicationContext);
        setHistory(history);

        UrlChangeHandler urlChangeHandler = new UrlChangeHandler(this);
        autowireContext(urlChangeHandler, applicationContext);
        setUrlChangeHandler(urlChangeHandler);

        getPage().addPopStateListener(urlChangeHandler::handleUrlChange);
    }

    protected void autowireContext(Object instance, ApplicationContext applicationContext) {
        AutowireCapableBeanFactory autowireBeanFactory = applicationContext.getAutowireCapableBeanFactory();
        autowireBeanFactory.autowireBean(instance);

        if (instance instanceof ApplicationContextAware) {
            ((ApplicationContextAware) instance).setApplicationContext(applicationContext);
        }

        if (instance instanceof InitializingBean) {
            try {
                ((InitializingBean) instance).afterPropertiesSet();
            } catch (Exception e) {
                throw new RuntimeException(
                        "Unable to initialize UI Component - calling afterPropertiesSet for " +
                                instance.getClass(), e);
            }
        }
    }

    protected boolean isUserSessionAlive(Connection connection) {
        try {
            UserSession session = connection.getSession();

            // todo do we need this ?
            /*if (session.isAuthenticated()) {
                userSessionService.getUserSession(session.getId());
            }*/
            return true;
        } catch (NoUserSessionException e) {
            return false;
        }
    }

    public boolean hasAuthenticatedSession() {
        return userSession.isAuthenticated();
    }

    protected void publishAppInitializedEvent(App app) {
        events.publish(new AppInitializedEvent(app));
    }

    protected void showCriticalExceptionMessage(@SuppressWarnings("unused") Exception exception) {
        String initErrorCaption = messages.getMessage("app.initErrorCaption");
        String initErrorMessage = messages.getMessage("app.initErrorMessage");

        VerticalLayout content = new VerticalLayout();
        content.setMargin(false);
        content.setSpacing(false);
        content.setStyleName("c-init-error-view");
        content.setSizeFull();

        VerticalLayout errorPanel = new VerticalLayout();
        errorPanel.setStyleName("c-init-error-panel");
        errorPanel.setWidthUndefined();
        errorPanel.setMargin(false);
        errorPanel.setSpacing(true);

        Label captionLabel = new Label(initErrorCaption);
        captionLabel.setWidthUndefined();
        captionLabel.setStyleName("c-init-error-caption");
        captionLabel.addStyleName("h2");
        captionLabel.setValue(initErrorCaption);

        errorPanel.addComponent(captionLabel);

        Label messageLabel = new Label(initErrorCaption);
        messageLabel.setWidthUndefined();
        messageLabel.setStyleName("c-init-error-message");
        messageLabel.setValue(initErrorMessage);

        errorPanel.addComponent(messageLabel);

        Button retryButton = new Button(messages.getMessage("app.initRetry"));
        retryButton.setStyleName("c-init-error-retry");
        retryButton.addClickListener(event -> {
            // always restart UI
            String url = ControllerUtils.getLocationWithoutParams() + "?restartApp";
            getPage().open(url, "_self");
        });

        errorPanel.addComponent(retryButton);
        errorPanel.setComponentAlignment(retryButton, Alignment.MIDDLE_CENTER);

        content.addComponent(errorPanel);
        content.setComponentAlignment(errorPanel, Alignment.MIDDLE_CENTER);

        setContent(content);
    }

    protected void setupUI() throws LoginException {
        if (!app.getConnection().isConnected()) {
            app.loginOnStart();
        } else {
            app.createTopLevelWindow(this);
        }
    }

    @Override
    protected void refresh(VaadinRequest request) {
        super.refresh(request);

        boolean sessionIsAlive = true;

        Connection connection = app.getConnection();

        if (connection.isAuthenticated()) {
            // Ping middleware session if connected
            log.debug("Ping middleware session");

            try {
                UserSession session = connection.getSession();
                if (session != null && session.isAuthenticated()) {
                    // todo do we need this ?
                    // userSessionService.getUserSession(session.getId());

                    if (hasAuthenticatedSession()
                            && !Objects.equals(userSession, session)) {
                        setUserSession(session);
                    }
                }
            } catch (Exception e) {
                sessionIsAlive = false;

                app.exceptionHandlers.handle(new com.vaadin.server.ErrorEvent(e));
            }

            if (sessionIsAlive) {
                events.publish(new SessionHeartbeatEvent(app));
            }
        }

        urlChangeHandler.restoreState();

        if (sessionIsAlive) {
            events.publish(new UIRefreshEvent(this));
        }
    }

    @Override
    public void handleRequest(VaadinRequest request) {
        // on refresh page call
         processExternalLink(request, getUrlRouting().getState());
    }

    /**
     * @return current AppUI
     */
    public static AppUI getCurrent() {
        return (AppUI) UI.getCurrent();
    }

    /**
     * @return this App instance
     */
    public App getApp() {
        return app;
    }

    /**
     * @return currently displayed top-level window
     */
    @Nullable
    public RootWindow getTopLevelWindow() {
        return topLevelWindow;
    }

    @Nonnull
    public RootWindow getTopLevelWindowNN() {
        if (topLevelWindow == null) {
            throw new IllegalStateException("UI topLevelWindow is null");
        }

        return topLevelWindow;
    }

    /**
     * INTERNAL.
     * Set currently displayed top-level window.
     */
    public void setTopLevelWindow(RootWindow window) {
        if (this.topLevelWindow != window) {
            this.topLevelWindow = window;

            if (window != null) {
                setContent(topLevelWindow.unwrapComposition(Component.class));
            } else {
                setContent(null);
            }
        }
    }

    public TestIdManager getTestIdManager() {
        return testIdManager;
    }

    /**
     * @return true if UI test mode is enabled and cuba-id attribute should be added to DOM tree
     */
    public boolean isTestMode() {
        return testMode;
    }

    public boolean isPerformanceTestMode() {
        return performanceTestMode;
    }

    @Override
    public void error(com.vaadin.server.ErrorEvent event) {
        try {
            app.getExceptionHandlers().handle(event);
            app.getAppLog().log(event);
        } catch (Throwable e) {
            log.error("Error handling exception\nOriginal exception:\n{}\nException in handlers:\n{}",
                    ExceptionUtils.getStackTrace(event.getThrowable()),
                    ExceptionUtils.getStackTrace(e));
        }
    }


    protected void processExternalLink(VaadinRequest request, NavigationState requestedState) {
        if (isLinkHandlerRequest(request)) {
            processLinkHandlerRequest(request);
        } else {
            processRequest(requestedState);
        }
    }

    protected boolean isLinkHandlerRequest(VaadinRequest request) {
        WrappedSession wrappedSession = request.getWrappedSession();
        if (wrappedSession == null) {
            return false;
        }

        String action = (String) wrappedSession.getAttribute(LAST_REQUEST_ACTION_ATTR);

        WebConfig webConfig = beanLocator.get(ConfigInterfaces.class).getConfig(WebConfig.class);
        return webConfig.getLinkHandlerActions().contains(action);
    }

    protected void processLinkHandlerRequest(VaadinRequest request) {
        WrappedSession wrappedSession = request.getWrappedSession();
        //noinspection unchecked
        Map<String, String> params =
                (Map<String, String>) wrappedSession.getAttribute(LAST_REQUEST_PARAMS_ATTR);
        params = params != null ? params : Collections.emptyMap();

        try {
            String action = (String) wrappedSession.getAttribute(LAST_REQUEST_ACTION_ATTR);
            LinkHandler linkHandler = AppBeans.getPrototype(LinkHandler.NAME, app, action, params);
            if (app.connection.isConnected() && linkHandler.canHandleLink()) {
                linkHandler.handle();
            } else {
                app.linkHandler = linkHandler;
            }
        } catch (Exception e) {
            error(new com.vaadin.server.ErrorEvent(e));
        }
    }

    protected void processRequest(NavigationState navigationState) {
        WebConfig webConfig = beanLocator.get(ConfigInterfaces.class).getConfig(WebConfig.class);
        if (UrlHandlingMode.URL_ROUTES != webConfig.getUrlHandlingMode()
                || navigationState == null) {
            return;
        }

        urlChangeHandler.getScreenNavigator()
                .handleScreenNavigation(navigationState);
    }

    @Override
    public void detach() {
        log.trace("Detaching UI {}", this);
        super.detach();
    }

    protected void updateClientSystemMessages(Locale locale) {
        SystemMessages msgs = new SystemMessages();

        msgs.communicationErrorCaption = messages.getMessage("communicationErrorCaption", locale);
        msgs.communicationErrorMessage = messages.getMessage("communicationErrorMessage", locale);

        msgs.sessionExpiredErrorCaption = messages.getMessage("sessionExpiredErrorCaption", locale);
        msgs.sessionExpiredErrorMessage = messages.getMessage("sessionExpiredErrorMessage", locale);

        msgs.authorizationErrorCaption = messages.getMessage("authorizationErrorCaption", locale);
        msgs.authorizationErrorMessage = messages.getMessage("authorizationErrorMessage", locale);

        updateSystemMessagesLocale(msgs);

        ReconnectDialogConfiguration reconnectDialogConfiguration = getReconnectDialogConfiguration();

        reconnectDialogConfiguration.setDialogText(messages.getMessage("reconnectDialogText", locale));
        reconnectDialogConfiguration.setDialogTextGaveUp(messages.getMessage("reconnectDialogTextGaveUp", locale));
    }

    protected AbstractComponent getTopLevelWindowComposition() {
        if (topLevelWindow == null) {
            throw new IllegalStateException("UI does not have top level window");
        }

        return topLevelWindow.unwrapComposition(AbstractComponent.class);
    }

    public List<CubaTimer> getTimers() {
        AbstractComponent timersHolder = getTopLevelWindowComposition();

        return timersHolder.getExtensions().stream()
                .filter(extension -> extension instanceof CubaTimer)
                .map(extension -> (CubaTimer) extension)
                .collect(Collectors.toList());
    }

    public void addTimer(CubaTimer timer) {
        AbstractComponent timersHolder = getTopLevelWindowComposition();

        if (!timersHolder.getExtensions().contains(timer)) {
            timer.extend(timersHolder);
        }
    }

    public void removeTimer(CubaTimer timer) {
        AbstractComponent timersHolder = getTopLevelWindowComposition();

        timersHolder.removeExtension(timer);
    }

    public void beforeTopLevelWindowInit() {
        updateUiTheme();

        // todo move to login handling
        updateClientSystemMessages(app.getLocale());

        // todo move test id manager into RootWindow ?
        getTestIdManager().reset();
    }

    protected void updateUiTheme() {
        UserSession userSession = userSessionSource.getUserSession();

        if (userSession.isAuthenticated()) {
            // load theme from user settings
            // todo settings
            /*String themeName = userSettingsTools.loadAppWindowTheme();

            if (!Objects.equals(themeName, getTheme())) {
                // check theme support
                Set<String> supportedThemes = themeConstantsRepository.getAvailableThemes();
                if (supportedThemes.contains(themeName)) {
                    app.applyTheme(themeName);
                    setTheme(themeName);
                }
            }*/
        }
    }

    public JmixFileDownloader getFileDownloader() {
        return fileDownloader;
    }

    public UiEventsMulticaster getUiEventsMulticaster() {
        return uiEventsMulticaster;
    }

    public Resource createVersionedResource(String value) {
        return iconResolver.getIconResource(value);
    }

    public String getWebJarPath(String webjar, String partialPath) {
        return webJarResourceResolver.getWebJarPath(webjar, partialPath);
    }

    public String translateToWebPath(String fullWebJarPath) {
        return webJarResourceResolver.translateToWebPath(fullWebJarPath);
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);

        // todo navigation
        /*String lastHistoryOp = ((WebUrlRouting) getUrlRouting()).getLastHistoryOperation();
        target.addAttribute(CubaUIConstants.LAST_HISTORY_OP, lastHistoryOp);*/
    }

    /**
     * Check if users can interact with the component - there are no modal windows that prevent user action.
     *
     * @param component component
     * @return whether it accessible or not
     */
    public boolean isAccessibleForUser(Component component) {
        Collection<Window> windows = this.getWindows();
        if (windows.isEmpty()) {
            // there are no windows - all components are accessible
            return true;
        }

        boolean hasModalWindows = windows.stream().anyMatch(Window::isModal);
        if (!hasModalWindows) {
            // there are no modal windows - all components are accessible
            return true;
        }

        Component windowOrUI = AppUIUtils.getWindowOrUI(component);
        if (windowOrUI == null) {
            // something went wrong
            return false;
        }

        if (windowOrUI instanceof UI) {
            // there are modal windows, component belongs to UI
            return false;
        }

        if (windowOrUI instanceof Window) {
            Window currentWindow = (Window) windowOrUI;

            if (!currentWindow.isModal()) {
                // there are modal windows, component belongs to non-modal window
                return false;
            }

            // CAUTION we cannot sort windows in UI, because they are ordered only on client side
        }

        // we cannot reliably check if access is permitted
        return true;
    }

    public void updateSystemMessagesLocale(SystemMessages msgs) {
        Map<String, String> localeMap = new HashMap<>(8);

        localeMap.put(AppUIClientRpc.COMMUNICATION_ERROR_CAPTION_KEY, msgs.communicationErrorCaption);
        localeMap.put(AppUIClientRpc.COMMUNICATION_ERROR_MESSAGE_KEY, msgs.communicationErrorMessage);

        localeMap.put(AppUIClientRpc.SESSION_EXPIRED_ERROR_CAPTION_KEY, msgs.sessionExpiredErrorCaption);
        localeMap.put(AppUIClientRpc.SESSION_EXPIRED_ERROR_MESSAGE_KEY, msgs.sessionExpiredErrorMessage);

        localeMap.put(AppUIClientRpc.AUTHORIZATION_ERROR_CAPTION_KEY, msgs.authorizationErrorCaption);
        localeMap.put(AppUIClientRpc.AUTHORIZATION_ERROR_MESSAGE_KEY, msgs.authorizationErrorMessage);

        getRpcProxy(AppUIClientRpc.class).updateSystemMessagesLocale(localeMap);
    }

    public static class SystemMessages {
        public String communicationErrorCaption;
        public String communicationErrorMessage;

        public String authorizationErrorCaption;
        public String authorizationErrorMessage;

        public String sessionExpiredErrorCaption;
        public String sessionExpiredErrorMessage;
    }
}
