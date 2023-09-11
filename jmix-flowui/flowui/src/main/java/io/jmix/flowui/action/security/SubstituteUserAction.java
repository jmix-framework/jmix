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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import io.jmix.core.Messages;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.usersubstitution.UserSubstitutionManager;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.UiProperties;
import io.jmix.flowui.ViewNavigators;
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

@ActionType(SubstituteUserAction.ID)
public class SubstituteUserAction extends BaseAction implements ExecutableAction, ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(SubstituteUserAction.class);

    public static final String ID = "substituteUser";

    protected ApplicationContext applicationContext;
    protected UserSubstitutionManager userSubstitutionManager;
    protected ViewNavigators viewNavigators;
    protected ViewRegistry viewRegistry;
    protected Dialogs dialogs;
    protected Messages messages;

    protected String mainViewId;

    protected List<Consumer<UserDetails>> cancelHandlers = new ArrayList<>();
    protected UserDetails newSubstitutedUser;
    protected UserDetails prevSubstitutedUser;

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
    public void setUserSubstitutionManager(UserSubstitutionManager userSubstitutionManager) {
        this.userSubstitutionManager = userSubstitutionManager;
    }

    @Autowired
    public void setViewNavigators(ViewNavigators viewNavigators) {
        this.viewNavigators = viewNavigators;
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
        if (view.isPreventBrowserTabClosing()) {
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
                                        standardDetailView.setPreventBrowserTabClosing(false);
                                        standardDetailView.closeWithDiscard();
                                        doSubstituteUser();
                                    }),
                            new DialogAction(DialogAction.Type.CANCEL)
                                    .withHandler(__ -> {
                                        // Page reload warning suppression rollback
                                        if (view.isPreventBrowserTabClosing()) {
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

        viewNavigators.view(viewRegistry.getViewInfo(mainViewId).getControllerClass())
                .withAfterNavigationHandler(__ -> UI.getCurrent().access(() -> UI.getCurrent().getPage().reload()))
                .navigate();
    }

    protected void doCancel() {
        for (Consumer<UserDetails> action : cancelHandlers) {
            action.accept(prevSubstitutedUser);
        }
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
