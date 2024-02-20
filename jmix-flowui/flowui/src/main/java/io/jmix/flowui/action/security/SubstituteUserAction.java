/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.action.security;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.internal.AnnotationReader;
import com.vaadin.flow.router.Route;
import io.jmix.core.Messages;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.usersubstitution.UserSubstitutionManager;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.UiProperties;
import io.jmix.flowui.UiViewProperties;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.action.ExecutableAction;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.action.BaseAction;
import io.jmix.flowui.util.WebBrowserTools;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewRegistry;
import jakarta.servlet.ServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Makes UI-specific preparations to user substitution and performs it using {@link UserSubstitutionManager}
 * Checks if there are unsaved changes on the current view and shows dialog with options:
 * <ol>
 *     <li><b>Discard changes</b> (close current view, cleanups background tasks, then performs
 *     substitution and navigates to {@code MainView})</li>
 *     <li><b>Cancel</b> (invokes all {@code cancelAction}s)</li>
 * </ol>
 */
@ActionType(SubstituteUserAction.ID)
public class SubstituteUserAction extends BaseAction implements ExecutableAction, ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(SubstituteUserAction.class);

    public static final String ID = "substituteUser";

    protected ApplicationContext applicationContext;
    protected ServletContext servletContext;
    protected UserSubstitutionManager userSubstitutionManager;
    protected ViewRegistry viewRegistry;
    protected Dialogs dialogs;
    protected Messages messages;

    protected String mainViewId;

    protected List<Consumer<UserDetails>> cancelHandlers = new ArrayList<>();
    protected UserDetails newSubstitutedUser;
    protected UserDetails prevSubstitutedUser;

    protected boolean preventBrowserTabClosing;

    public SubstituteUserAction() {
        this(ID);
    }

    public SubstituteUserAction(String id) {
        super(id);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Autowired
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Autowired
    public void setUserSubstitutionManager(UserSubstitutionManager userSubstitutionManager) {
        this.userSubstitutionManager = userSubstitutionManager;
    }

    @Autowired
    public void setViewRegistry(ViewRegistry viewRegistry) {
        this.viewRegistry = viewRegistry;
    }

    @Autowired
    public void setDialogs(Dialogs dialogs) {
        this.dialogs = dialogs;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setUiProperties(UiProperties uiProperties) {
        mainViewId = uiProperties.getMainViewId();
    }

    @Autowired
    public void setUiViewProperties(UiViewProperties uiViewProperties) {
        preventBrowserTabClosing = uiViewProperties.isPreventBrowserTabClosing();
    }

    @Override
    public void actionPerform(Component component) {
        // if standard behaviour
        if (!hasListener(ActionPerformedEvent.class)) {
            execute();
        } else {
            super.actionPerform(component);
        }
    }

    @Override
    public void execute() {
        Preconditions.checkNotNullArgument(prevSubstitutedUser);
        Preconditions.checkNotNullArgument(newSubstitutedUser);

        findCurrentOpenedView()
                .ifPresentOrElse(this::prepareViewToClose, this::doSubstituteUser);
    }

    protected void prepareViewToClose(View<?> view) {
        // Suppress page reload warning if necessary
        if (preventBrowserTabClosing) {
            // Required to open the dialog only after making an asynchronous js-function call
            WebBrowserTools.allowBrowserTabClosing(view)
                    .then(__ -> openDiscardCurrentViewDialog(view));
        } else {
            openDiscardCurrentViewDialog(view);
        }
    }

    protected void openDiscardCurrentViewDialog(View<?> view) {
        if (view instanceof StandardDetailView<?> standardDetailView && standardDetailView.hasUnsavedChanges()) {
            dialogs.createOptionDialog()
                    .withHeader(messages.getMessage("dialogs.closeUnsaved.title"))
                    .withText(messages.getMessage("dialogs.closeUnsaved.message"))
                    .withActions(
                            new DialogAction(DialogAction.Type.YES)
                                    .withHandler(__ -> {
                                        standardDetailView.closeWithDiscard();
                                        doSubstituteUser();
                                    }),
                            new DialogAction(DialogAction.Type.CANCEL)
                                    .withHandler(__ -> {
                                        // Page reload warning suppression rollback
                                        if (preventBrowserTabClosing) {
                                            WebBrowserTools.preventBrowserTabClosing(standardDetailView);
                                        }

                                        doCancel();
                                    })
                                    .withVariant(ActionVariant.PRIMARY)
                    )
                    .open();
        } else {
            doSubstituteUser();
        }
    }

    protected Optional<View<?>> findCurrentOpenedView() {
        View<?> currentView = null;

        try {
            currentView = (View<?>) UI.getCurrent().getCurrentView();
        } catch (Exception e) {
            log.debug("Failed to find current view", e);
        }

        return Optional.ofNullable(currentView);
    }

    protected void doSubstituteUser() {
        userSubstitutionManager.substituteUser(newSubstitutedUser.getUsername());
        UI.getCurrent().getPage().setLocation(getMainViewLocation());
    }

    protected void doCancel() {
        for (Consumer<UserDetails> action : cancelHandlers) {
            action.accept(prevSubstitutedUser);
        }
    }

    protected String getMainViewLocation() {
        Class<? extends View<?>> mainViewControllerClass = viewRegistry.getViewInfo(mainViewId).getControllerClass();
        String mainViewRouteValue = AnnotationReader.getAnnotationFor(mainViewControllerClass, Route.class)
                .map(Route::value)
                .orElse("");

        String contextPath = servletContext == null ? null : servletContext.getContextPath();
        return Strings.isNullOrEmpty(contextPath)
                ? "/" + mainViewRouteValue
                : contextPath + "/" + mainViewRouteValue;
    }

    public SubstituteUserAction withUsers(UserDetails prevSubstitutedUser, UserDetails newSubstitutedUser) {
        this.prevSubstitutedUser = prevSubstitutedUser;
        this.newSubstitutedUser = newSubstitutedUser;

        return this;
    }

    public SubstituteUserAction withCancelHandler(Consumer<UserDetails> cancelHandler) {
        cancelHandlers.add(cancelHandler);
        return this;
    }
}
