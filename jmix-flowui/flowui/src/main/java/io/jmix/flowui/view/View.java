/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.view;

import com.vaadin.flow.component.*;
import com.vaadin.flow.router.*;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.model.ViewData;
import io.jmix.flowui.sys.ViewSupport;
import io.jmix.flowui.sys.event.UiEventsManager;
import io.jmix.flowui.util.OperationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Optional;
import java.util.function.Consumer;

public class View<T extends Component> extends Composite<T>
        implements BeforeEnterObserver, AfterNavigationObserver, BeforeLeaveObserver, HasDynamicTitle {

    private ApplicationContext applicationContext;

    private ViewData viewData;
    private ViewActions viewActions;
    private ViewFacets viewFacets;

    private Consumer<View<T>> closeDelegate;

    private boolean closeActionPerformed = false;

    public View() {
        closeDelegate = createDefaultViewDelegate();
    }

    private Consumer<View<T>> createDefaultViewDelegate() {
        return __ -> getViewSupport().close(this);
    }

    protected ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Autowired
    protected void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setId(String id) {
        super.setId(id);
    }

    @Override
    public Optional<String> getId() {
        return super.getId();
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        fireEvent(new ReadyEvent(this));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        fireEvent(new BeforeShowEvent(this));
    }

    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        if (!event.isPostponed()) {
            if (!closeActionPerformed) {
                CloseAction closeAction = new NavigateCloseAction(event);
                BeforeCloseEvent beforeCloseEvent = new BeforeCloseEvent(this, closeAction);
                fireEvent(beforeCloseEvent);

                if (beforeCloseEvent.isClosePrevented()) {
                    closeActionPerformed = false;
                    return;
                }

                AfterCloseEvent afterCloseEvent = new AfterCloseEvent(this, closeAction);
                fireEvent(afterCloseEvent);
            }
        }

        closeActionPerformed = false;
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);

        removeApplicationListeners();
        removeViewAttributes();
        unregisterBackNavigation();
    }

    private void unregisterBackNavigation() {
        getViewSupport().unregisterBackNavigation(this);
    }

    private void removeApplicationListeners() {
        getApplicationContext().getBean(UiEventsManager.class).removeApplicationListeners(this);
    }

    private void removeViewAttributes() {
        getViewAttributes().removeAllAttributes();
    }

    public OperationResult closeWithDefaultAction() {
        return close(StandardOutcome.CLOSE);
    }

    public OperationResult close(StandardOutcome outcome) {
        return close(outcome.getCloseAction());
    }

    public OperationResult close(CloseAction closeAction) {
        BeforeCloseEvent beforeCloseEvent = new BeforeCloseEvent(this, closeAction);
        fireEvent(beforeCloseEvent);
        if (beforeCloseEvent.isClosePrevented()) {
            return beforeCloseEvent.getCloseResult()
                    .orElse(OperationResult.fail());
        }


        closeActionPerformed = true;

        closeDelegate.accept(this);

        removeApplicationListeners();
        removeViewAttributes();

        AfterCloseEvent afterCloseEvent = new AfterCloseEvent(this, closeAction);
        fireEvent(afterCloseEvent);

        return OperationResult.success();
    }

    Consumer<View<T>> getCloseDelegate() {
        return closeDelegate;
    }

    void setCloseDelegate(Consumer<View<T>> closeDelegate) {
        this.closeDelegate = closeDelegate;
    }

    protected ViewData getViewData() {
        return viewData;
    }

    protected void setViewData(ViewData viewData) {
        this.viewData = viewData;
    }

    protected ViewActions getViewActions() {
        return viewActions;
    }

    protected void setViewActions(ViewActions viewActions) {
        this.viewActions = viewActions;
    }

    protected ViewFacets getViewFacets() {
        return viewFacets;
    }

    protected void setViewFacets(ViewFacets viewFacets) {
        this.viewFacets = viewFacets;
    }

    protected ViewAttributes getViewAttributes() {
        String viewId = getId().orElseThrow(() -> new IllegalStateException("View should have an id"));
        return getApplicationContext().getBean(ViewAttributes.class, viewId);
    }

    @Override
    public String getPageTitle() {
        // return not cached value in case of hot deploy
        return getViewSupport().getLocalizedTitle(this, false);
    }

    /**
     * Adds {@link InitEvent} listener.
     * <p>
     * You can also add an event listener declaratively using a controller method annotated with {@link Subscribe}:
     * <pre>
     *    &#64;Subscribe
     *    public void onInit(InitEvent event) {
     *       // handle event here
     *    }
     * </pre>
     *
     * @param listener the listener to add, not {@code null}
     * @return a handle that can be used for removing the listener
     */
    protected Registration addInitListener(ComponentEventListener<InitEvent> listener) {
        return getEventBus().addListener(InitEvent.class, listener);
    }

    protected Registration addBeforeShowListener(ComponentEventListener<BeforeShowEvent> listener) {
        return getEventBus().addListener(BeforeShowEvent.class, listener);
    }

    protected Registration addReadyListener(ComponentEventListener<ReadyEvent> listener) {
        return getEventBus().addListener(ReadyEvent.class, listener);
    }

    protected Registration addBeforeCloseListener(ComponentEventListener<BeforeCloseEvent> listener) {
        return getEventBus().addListener(BeforeCloseEvent.class, listener);
    }

    protected Registration addAfterCloseListener(ComponentEventListener<AfterCloseEvent> listener) {
        return getEventBus().addListener(AfterCloseEvent.class, listener);
    }

    //    @TriggerOnce
    public static class InitEvent extends ComponentEvent<View<?>> {

        public InitEvent(View<?> source) {
            super(source, false);
        }
    }

    //    @TriggerOnce
    public static class BeforeShowEvent extends ComponentEvent<View<?>> {

        public BeforeShowEvent(View<?> source) {
            super(source, false);
        }
    }

    //    @TriggerOnce
    public static class ReadyEvent extends ComponentEvent<View<?>> {

        public ReadyEvent(View<?> source) {
            super(source, false);
        }
    }

    public static class BeforeCloseEvent extends ComponentEvent<View<?>> {

        protected final CloseAction closeAction;

        protected OperationResult closeResult;
        protected boolean closePrevented = false;

        public BeforeCloseEvent(View<?> source, CloseAction closeAction) {
            super(source, false);
            this.closeAction = closeAction;
        }

        public CloseAction getCloseAction() {
            return closeAction;
        }

        public void preventWindowClose() {
            this.closePrevented = true;
        }

        public void preventWindowClose(OperationResult closeResult) {
            this.closePrevented = true;
            this.closeResult = closeResult;
        }

        public boolean isClosePrevented() {
            return closePrevented;
        }

        public Optional<OperationResult> getCloseResult() {
            return Optional.ofNullable(closeResult);
        }

        public boolean closedWith(StandardOutcome outcome) {
            return outcome.getCloseAction().equals(closeAction);
        }
    }

    public static class AfterCloseEvent extends ComponentEvent<View<?>> {

        protected final CloseAction closeAction;

        public AfterCloseEvent(View<?> source, CloseAction closeAction) {
            super(source, false);
            this.closeAction = closeAction;
        }

        public CloseAction getCloseAction() {
            return closeAction;
        }

        public boolean closedWith(StandardOutcome outcome) {
            return outcome.getCloseAction().equals(closeAction);
        }
    }

    private ViewSupport getViewSupport() {
        return applicationContext.getBean(ViewSupport.class);
    }
}
