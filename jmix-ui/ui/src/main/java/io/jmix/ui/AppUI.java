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
import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.*;
import com.vaadin.shared.ui.ui.Transport;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import io.jmix.core.Messages;
import io.jmix.core.annotation.Internal;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.component.RootWindow;
import io.jmix.ui.component.impl.WindowImpl;
import io.jmix.ui.event.AppInitializedEvent;
import io.jmix.ui.event.UIRefreshEvent;
import io.jmix.ui.exception.UiExceptionHandler;
import io.jmix.ui.icon.IconResolver;
import io.jmix.ui.navigation.*;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.settings.UserSettingsTools;
import io.jmix.ui.sys.ControllerUtils;
import io.jmix.ui.sys.TestIdManager;
import io.jmix.ui.sys.WebJarResourceResolver;
import io.jmix.ui.sys.event.UiEventsMulticaster;
import io.jmix.ui.theme.ThemeConstantsRepository;
import io.jmix.ui.widget.AppUIUtils;
import io.jmix.ui.widget.EnhancedUI;
import io.jmix.ui.widget.JmixFileDownloader;
import io.jmix.ui.widget.JmixTimer;
import io.jmix.ui.widget.client.ui.AppUIClientRpc;
import io.jmix.ui.widget.client.ui.AppUIConstants;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AnonymousAuthenticationToken;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@Widgetset("${jmix.ui.widget-set:io.jmix.ui.widget.WidgetSet}")
@Theme("${jmix.ui.theme.name:helium}")
@Push(transport = Transport.WEBSOCKET_XHR)
@SpringUI
@PreserveOnRefresh
public class AppUI extends UI implements ErrorHandler, EnhancedUI, UiExceptionHandler.UiContext {

    private static final Logger log = LoggerFactory.getLogger(AppUI.class);

    protected App app;

    public static final String LAST_REQUEST_PARAMS_ATTR = "lastRequestParams";

    @Autowired
    protected Messages messages;

    @Autowired
    protected ApplicationEventPublisher eventPublisher;

    @Autowired
    protected UiEventPublisher uiEventPublisher;

    @Autowired
    protected UiProperties uiProperties;

    @Autowired
    protected ThemeConstantsRepository themeConstantsRepository;

    @Autowired
    protected CurrentAuthentication currentAuthentication;

    @Autowired
    protected UiEventsMulticaster uiEventsMulticaster;

    @Autowired
    protected IconResolver iconResolver;

    @Autowired
    protected WebJarResourceResolver webJarResourceResolver;

    @Autowired
    protected BeanFactory beanFactory;

    @Autowired(required = false)
    protected UserSettingsTools userSettingsTools;

    protected TestIdManager testIdManager = new TestIdManager();

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
     * If you want to include scripts to generated page statically implement {@link BootstrapListener}
     */
    protected void initJsLibraries() {
    }

    protected void initInternalComponents() {
        fileDownloader = new JmixFileDownloader();
        fileDownloader.extend(this);
    }

    protected App createApplication() {
        return beanFactory.getBean(App.class);
    }

    @Override
    public Screens getScreens() {
        if (screens == null) {
            screens = beanFactory.getBean(Screens.class);
        }
        return screens;
    }

    @Override
    public Dialogs getDialogs() {
        if (dialogs == null) {
            dialogs = beanFactory.getBean(Dialogs.class);
        }
        return dialogs;
    }

    @Override
    public Notifications getNotifications() {
        if (notifications == null) {
            notifications = beanFactory.getBean(Notifications.class);
        }
        return notifications;
    }

    @Override
    public WebBrowserTools getWebBrowserTools() {
        if (webBrowserTools == null) {
            webBrowserTools = beanFactory.getBean(WebBrowserTools.class);
        }
        return webBrowserTools;
    }

    @Override
    public Fragments getFragments() {
        if (fragments == null) {
            fragments = beanFactory.getBean(Fragments.class);
        }
        return fragments;
    }

    public UrlRouting getUrlRouting() {
        return urlRouting;
    }

    @Autowired
    @Lazy
    public void setUrlRouting(UrlRouting urlRouting) {
        this.urlRouting = urlRouting;
    }

    public UrlChangeHandler getUrlChangeHandler() {
        return urlChangeHandler;
    }

    @Autowired
    @Lazy
    public void setUrlChangeHandler(UrlChangeHandler urlChangeHandler) {
        this.urlChangeHandler = urlChangeHandler;
        getPage().addPopStateListener(urlChangeHandler::handleUrlChange);
    }

    public History getHistory() {
        return history;
    }

    @Autowired
    @Lazy
    public void setHistory(History history) {
        this.history = history;
    }

    @Override
    protected void init(VaadinRequest request) {
        log.trace("Initializing UI {}", this);


        NavigationState requestedState = getUrlRouting().getState();

        try {
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
            setupUI();
            restoreRouteState(requestedState);
        } catch (Exception e) {
            log.error("Unable to init ui", e);

            // unable to connect to middle ware
            showCriticalExceptionMessage(e);
            return;
        }

        processExternalLink(request, requestedState);
    }

    /*
     * During AppUI initialization process the initial URL is replaced
     * with the created Root Window route. In case the requested state
     * contains parameters for the root window, we need to restore it
     * to keep in sync the browser URL and params that can be obtained
     * from UrlParamsChangedEvent
     */
    protected void restoreRouteState(NavigationState requestedState) {
        // Check that the requested state doesn't contain nested route
        // that will be handled by navigation handlers and that there
        // are parameters to restore in the browser URL.
        Map<String, String> requestedParams = requestedState.getParams();
        if (!Strings.isNullOrEmpty(requestedState.getNestedRoute())
                || MapUtils.isEmpty(requestedParams)) {
            return;
        }

        RootWindow topLevelWindow = getTopLevelWindow();
        if (topLevelWindow instanceof WindowImpl) {
            Screen frameOwner = topLevelWindow.getFrameOwner();
            NavigationState resolvedState = ((WindowImpl) topLevelWindow).getResolvedState();
            if (resolvedState == null) {
                return;
            }

            // Check that the actual Root Window and the requested Root Window is the same
            if (Objects.equals(requestedState.getRoot(), resolvedState.getRoot())) {
                urlRouting.replaceState(frameOwner, requestedParams);

                // Because of usage of 'urlRouting.replaceState' UrlParamsChangedEvent
                // won't be fired for the Root Window by ParamsNavigationHandler, hence
                // we need to do it here.
                UiControllerUtils.fireEvent(frameOwner, UrlParamsChangedEvent.class,
                        new UrlParamsChangedEvent(frameOwner, requestedParams));
            }
        }
    }

    /**
     * @return {@code true} if authentication is set and
     * it isn't represented by {@link AnonymousAuthenticationToken}
     */
    public boolean hasAuthenticatedSession() {
        // Think of Anonymous Authentication as no Authentication
        return currentAuthentication.isSet() &&
                !(currentAuthentication.getAuthentication() instanceof AnonymousAuthenticationToken);
    }

    protected void publishAppInitializedEvent(App app) {
        eventPublisher.publishEvent(new AppInitializedEvent(app));
    }

    protected void showCriticalExceptionMessage(@SuppressWarnings("unused") Exception exception) {
        String initErrorCaption = messages.getMessage("app.initErrorCaption");
        String initErrorMessage = messages.getMessage("app.initErrorMessage");

        VerticalLayout content = new VerticalLayout();
        content.setMargin(false);
        content.setSpacing(false);
        content.setStyleName("jmix-init-error-view");
        content.setSizeFull();

        VerticalLayout errorPanel = new VerticalLayout();
        errorPanel.setStyleName("jmix-init-error-panel");
        errorPanel.setWidthUndefined();
        errorPanel.setMargin(false);
        errorPanel.setSpacing(true);

        Label captionLabel = new Label(initErrorCaption);
        captionLabel.setWidthUndefined();
        captionLabel.setStyleName("jmix-init-error-caption");
        captionLabel.addStyleName("h2");
        captionLabel.setValue(initErrorCaption);

        errorPanel.addComponent(captionLabel);

        Label messageLabel = new Label(initErrorCaption);
        messageLabel.setWidthUndefined();
        messageLabel.setStyleName("jmix-init-error-message");
        messageLabel.setValue(initErrorMessage);

        errorPanel.addComponent(messageLabel);

        Button retryButton = new Button(messages.getMessage("app.initRetry"));
        retryButton.setStyleName("jmix-init-error-retry");
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

    protected void setupUI() {
        app.loginOnStart();
    }

    @Override
    protected void refresh(VaadinRequest request) {
        super.refresh(request);
        urlChangeHandler.restoreState();
        uiEventPublisher.publishEvent(new UIRefreshEvent(this));
    }

    @Override
    public void handleRequest(VaadinRequest request) {
        // on refresh page call
        processExternalLink(request, getUrlRouting().getState());
    }

    /**
     * @return current AppUI
     */
    @Nullable
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
    @Internal
    public void setTopLevelWindow(@Nullable RootWindow window) {
        if (this.topLevelWindow != window) {
            if (window != null) {
                removePreviousTopLevelWindowConnector();

                this.topLevelWindow = window;

                setContent(topLevelWindow.unwrapComposition(Component.class));
            } else {
                setContent(null);
            }
        }
    }

    public TestIdManager getTestIdManager() {
        return testIdManager;
    }

    protected void removePreviousTopLevelWindowConnector() {
        if (isPerformanceTestMode()
                && topLevelWindow != null
                && uiProperties.getMainScreenId().equals(topLevelWindow.getId())) {
            getConnectorTracker().cleanConnectorMap(true);
        }
    }

    /**
     * @return true if UI test mode is enabled and j-test-id attribute should be added to DOM tree
     */
    public boolean isTestMode() {
        return uiProperties.isTestMode();
    }

    public boolean isPerformanceTestMode() {
        return uiProperties.isPerformanceTestMode();
    }

    @Override
    public void error(com.vaadin.server.ErrorEvent event) {
        try {
            app.getExceptionHandlers().handle(event);
        } catch (Throwable e) {
            log.error("Error handling exception\nOriginal exception:\n{}\nException in handlers:\n{}",
                    ExceptionUtils.getStackTrace(event.getThrowable()),
                    ExceptionUtils.getStackTrace(e));
        }
    }


    protected void processExternalLink(VaadinRequest request, NavigationState requestedState) {
        processRequest(requestedState);
    }

    protected void processRequest(@Nullable NavigationState navigationState) {
        if (UrlHandlingMode.URL_ROUTES != uiProperties.getUrlHandlingMode()
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

    public List<JmixTimer> getTimers() {
        AbstractComponent timersHolder = getTopLevelWindowComposition();

        return timersHolder.getExtensions().stream()
                .filter(extension -> extension instanceof JmixTimer)
                .map(extension -> (JmixTimer) extension)
                .collect(Collectors.toList());
    }

    public void addTimer(JmixTimer timer) {
        AbstractComponent timersHolder = getTopLevelWindowComposition();

        if (!timersHolder.getExtensions().contains(timer)) {
            timer.extend(timersHolder);
        }
    }

    public void removeTimer(JmixTimer timer) {
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
        if (hasAuthenticatedSession()) {
            if (userSettingsTools == null) {
                return;
            }

            // load theme from user settings
            String themeName = userSettingsTools.loadTheme();

            if (!Objects.equals(themeName, getTheme())) {
                // check theme support
                Set<String> supportedThemes = themeConstantsRepository.getAvailableThemes();
                if (supportedThemes.contains(themeName)) {
                    app.applyTheme(themeName);
                    setTheme(themeName);
                }
            }
        }
    }

    public JmixFileDownloader getFileDownloader() {
        return fileDownloader;
    }

    public UiEventsMulticaster getUiEventsMulticaster() {
        return uiEventsMulticaster;
    }

    @Nullable
    @Override
    public Resource createVersionedResource(String value) {
        return iconResolver.getIconResource(value);
    }

    @Override
    public String getWebJarPath(String webjar, String partialPath) {
        return webJarResourceResolver.getWebJarPath(webjar, partialPath);
    }

    @Override
    public String translateToWebPath(String fullWebJarPath) {
        return webJarResourceResolver.translateToWebPath(fullWebJarPath);
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);

        if (isVaadinSessionOpen()) {
            String lastHistoryOp = getUrlRouting().getLastHistoryOperation();
            target.addAttribute(AppUIConstants.LAST_HISTORY_OP, lastHistoryOp);
        }
    }

    protected boolean isVaadinSessionOpen() {
        return getSession() != null
                && getSession().getState() == VaadinSession.State.OPEN;
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
