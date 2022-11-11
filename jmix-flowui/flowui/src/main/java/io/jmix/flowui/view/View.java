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
import io.jmix.core.annotation.Internal;
import io.jmix.flowui.model.ViewData;
import io.jmix.flowui.sys.ViewSupport;
import io.jmix.flowui.sys.event.UiEventsManager;
import io.jmix.flowui.util.OperationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Base class for UI views.
 * <p>
 * A view is a building block of application UI. It contains UI components, data components and facets, that are
 * usually defined in linked XML descriptors.
 * <p>
 * A view can be shown either as a web page with its own URL (using {@link io.jmix.flowui.ViewNavigators}), or
 * inside a dialog window (using {@link io.jmix.flowui.DialogWindows}).
 * <p>
 * Views are registered in the application using the {@link ViewController} annotation on the view class.
 * <p>
 * A view sends lifecycle events: {@link InitEvent}, {@link BeforeShowEvent}, {@link ReadyEvent},
 * {@link BeforeCloseEvent}, {@link AfterCloseEvent}.
 *
 * @param <T> type of the root UI component
 */
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
        return __ -> getViewSupport().close(this, getReturnParameters());
    }

    protected QueryParameters getReturnParameters() {
        return QueryParameters.empty();
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
        fireEvent(new QueryParametersChangeEvent(this, event.getLocation().getQueryParameters()));
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
                    event.postpone();
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
        getViewSupport().unregisterBackwardNavigation(this);
    }

    private void removeApplicationListeners() {
        getApplicationContext().getBean(UiEventsManager.class).removeApplicationListeners(this);
    }

    private void removeViewAttributes() {
        getViewAttributes().removeAllAttributes();
    }

    /**
     * Requests closing the view with the {@code close} action.
     *
     * @return result of close request
     * @see #close(CloseAction)
     */
    public OperationResult closeWithDefaultAction() {
        return close(StandardOutcome.CLOSE);
    }

    /**
     * Requests closing the view with the given outcome.
     *
     * @param outcome {@link StandardOutcome}
     * @return result of close request
     */
    public OperationResult close(StandardOutcome outcome) {
        return close(outcome.getCloseAction());
    }

    /**
     * Requests closing of the view caused by the given action.
     *
     * @param closeAction close action which is propagated to {@link BeforeCloseEvent}, {@link AfterCloseEvent} and,
     *                    if the view has been opened in a dialog, to {@link DialogWindow.AfterCloseEvent}.
     * @return result of close request
     */
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
        String viewId = getId().orElseThrow(() -> new IllegalStateException(
                View.class.getSimpleName() + " should have an id"));
        return getApplicationContext().getBean(ViewAttributes.class, viewId);
    }

    protected ViewSupport getViewSupport() {
        return getApplicationContext().getBean(ViewSupport.class);
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
     *    protected void onInit(InitEvent event) {
     *       // handle event here
     *    }
     * </pre>
     *
     * @param listener the listener to add, not {@code null}
     * @return a registration object that can be used for removing the listener
     */
    protected Registration addInitListener(ComponentEventListener<InitEvent> listener) {
        return getEventBus().addListener(InitEvent.class, listener);
    }

    /**
     * Adds {@link BeforeShowEvent} listener.
     * <p>
     * You can also add an event listener declaratively using a controller method annotated with {@link Subscribe}:
     * <pre>
     *    &#64;Subscribe
     *    protected void onBeforeShow(BeforeShowEvent event) {
     *       // handle event here
     *    }
     * </pre>
     *
     * @param listener the listener to add, not {@code null}
     * @return a registration object that can be used for removing the listener
     */
    protected Registration addBeforeShowListener(ComponentEventListener<BeforeShowEvent> listener) {
        return getEventBus().addListener(BeforeShowEvent.class, listener);
    }

    /**
     * Adds {@link ReadyEvent} listener.
     * <p>
     * You can also add an event listener declaratively using a controller method annotated with {@link Subscribe}:
     * <pre>
     *    &#64;Subscribe
     *    protected void onReady(ReadyEvent event) {
     *       // handle event here
     *    }
     * </pre>
     *
     * @param listener the listener to add, not {@code null}
     * @return a registration object that can be used for removing the listener
     */
    protected Registration addReadyListener(ComponentEventListener<ReadyEvent> listener) {
        return getEventBus().addListener(ReadyEvent.class, listener);
    }

    /**
     * Adds {@link BeforeCloseEvent} listener.
     * <p>
     * You can also add an event listener declaratively using a controller method annotated with {@link Subscribe}:
     * <pre>
     *    &#64;Subscribe
     *    protected void onBeforeClose(BeforeCloseEvent event) {
     *       // handle event here
     *    }
     * </pre>
     *
     * @param listener the listener to add, not {@code null}
     * @return a registration object that can be used for removing the listener
     */
    protected Registration addBeforeCloseListener(ComponentEventListener<BeforeCloseEvent> listener) {
        return getEventBus().addListener(BeforeCloseEvent.class, listener);
    }

    /**
     * Adds {@link AfterCloseEvent} listener.
     * <p>
     * You can also add an event listener declaratively using a controller method annotated with {@link Subscribe}:
     * <pre>
     *    &#64;Subscribe
     *    protected void onAfterClose(AfterCloseEvent event) {
     *       // handle event here
     *    }
     * </pre>
     *
     * @param listener the listener to add, not {@code null}
     * @return a registration object that can be used for removing the listener
     */
    protected Registration addAfterCloseListener(ComponentEventListener<AfterCloseEvent> listener) {
        return getEventBus().addListener(AfterCloseEvent.class, listener);
    }

    /**
     * Adds {@link QueryParametersChangeEvent} listener.
     *
     * @param listener the listener to add, not {@code null}
     * @return a registration object that can be used for removing the listener
     */
    @Internal
    Registration addQueryParametersChangeListener(ComponentEventListener<QueryParametersChangeEvent> listener) {
        return getEventBus().addListener(QueryParametersChangeEvent.class, listener);
    }

    /**
     * The first event in the view opening process.
     * <p>
     * The view and all its declaratively defined components are created, and dependency injection is completed.
     * Some visual components are not fully initialized, for example buttons are not yet linked with actions.
     * <p>
     * In this event listener, you can create visual and data components, for example:
     * <pre>
     *     &#64;Subscribe
     *     protected void onInit(InitEvent event) {
     *         Label label = uiComponents.create(Label.class);
     *         label.setText("Hello World");
     *         getContent().add(label);
     *     }
     * </pre>
     *
     * @see #addInitListener(ComponentEventListener)
     */
    public static class InitEvent extends ComponentEvent<View<?>> {

        public InitEvent(View<?> source) {
            super(source, false);
        }
    }

    /**
     * The second (after {@link InitEvent}) event in the view opening process.
     * All components have completed their internal initialization procedures.
     * Data loaders have been triggered by the automatically configured {@code DataLoadCoordinator} facet.
     * <p>
     * In this event listener, you can load data, check permissions and modify UI components. For example:
     * <pre>
     *     &#64;Subscribe
     *     protected void onBeforeShow(BeforeShowEvent event) {
     *         customersDl.load();
     *     }
     * </pre>
     * <p>
     * You can abort the process of opening the view by throwing an exception.
     *
     * @see #addBeforeShowListener(ComponentEventListener)
     */
    public static class BeforeShowEvent extends ComponentEvent<View<?>> {

        public BeforeShowEvent(View<?> source) {
            super(source, false);
        }
    }

    /**
     * The last (after {@link BeforeShowEvent}) event in the view opening process.
     * <p>
     * In this event listener, you can make final configuration of the view according to loaded data and
     * show notifications or dialogs:
     * <pre>
     *     &#64;Subscribe
     *     protected void onReady(ReadyEvent event) {
     *         notifications.show("Just opened");
     *     }
     * </pre>
     *
     * @see #addReadyListener(ComponentEventListener)
     */
    public static class ReadyEvent extends ComponentEvent<View<?>> {

        public ReadyEvent(View<?> source) {
            super(source, false);
        }
    }

    /**
     * The first event in the view closing process.
     * The view is still displayed and fully functional.
     * <p>
     * In this event listener, you can check any conditions and prevent closing using the
     * {@link #preventClose()} method of the event, for example:
     * <pre>
     *     &#64;Subscribe
     *     protected void onBeforeClose(BeforeCloseEvent event) {
     *         if (Strings.isNullOrEmpty(textField.getTypedValue())) {
     *             notifications.show("Input required");
     *             event.preventClose();
     *         }
     *     }
     * </pre>
     *
     * @see #addBeforeCloseListener(ComponentEventListener)
     */
    public static class BeforeCloseEvent extends ComponentEvent<View<?>> {

        protected final CloseAction closeAction;

        protected OperationResult closeResult;
        protected boolean closePrevented = false;

        public BeforeCloseEvent(View<?> source, CloseAction closeAction) {
            super(source, false);
            this.closeAction = closeAction;
        }

        /**
         * @return action passed to the {@link #close(CloseAction)} method or {@link NavigateCloseAction}
         */
        public CloseAction getCloseAction() {
            return closeAction;
        }

        /**
         * Prevents closing of the view.
         */
        public void preventClose() {
            this.closePrevented = true;
        }

        /**
         * Prevents closing of the view.
         *
         * @param closeResult result object returned from the {@link #close(CloseAction)} method
         */
        public void preventClose(OperationResult closeResult) {
            this.closePrevented = true;
            this.closeResult = closeResult;
        }

        /**
         * @return whether the closing was prevented by invoking {@link #preventClose()} method
         */
        public boolean isClosePrevented() {
            return closePrevented;
        }

        /**
         * @return result passed to the {@link #preventClose(OperationResult)} method
         */
        public Optional<OperationResult> getCloseResult() {
            return Optional.ofNullable(closeResult);
        }

        /**
         * Checks that view was closed with the given {@code outcome}.
         */
        public boolean closedWith(StandardOutcome outcome) {
            return outcome.getCloseAction().equals(closeAction);
        }
    }

    /**
     * The second (after {@link BeforeCloseEvent}) event in the view closing process.
     * <p>
     * In this event listener, you can show notifications or dialogs after closing the view, for example:
     * <pre>
     *     &#64;Subscribe
     *     protected void onAfterClose(AfterCloseEvent event) {
     *         notifications.show("Just closed");
     *     }
     * </pre>
     *
     * @see #addAfterCloseListener(ComponentEventListener)
     */
    public static class AfterCloseEvent extends ComponentEvent<View<?>> {

        protected final CloseAction closeAction;

        public AfterCloseEvent(View<?> source, CloseAction closeAction) {
            super(source, false);
            this.closeAction = closeAction;
        }

        /**
         * @return action passed to the {@link #close(CloseAction)} method or {@link NavigateCloseAction}
         */
        public CloseAction getCloseAction() {
            return closeAction;
        }

        /**
         * Checks that view was closed with the given {@code outcome}.
         */
        public boolean closedWith(StandardOutcome outcome) {
            return outcome.getCloseAction().equals(closeAction);
        }
    }

    /**
     * An event informing which query parameters the view is opened with.
     * For internal use only. Can be changed or removed in future releases.
     *
     * @see #addQueryParametersChangeListener(ComponentEventListener)
     */
    @Internal
    public static class QueryParametersChangeEvent extends ComponentEvent<View<?>> {

        protected QueryParameters queryParameters;

        public QueryParametersChangeEvent(View<?> source, QueryParameters queryParameters) {
            super(source, true);
            this.queryParameters = queryParameters;
        }

        /**
         * @return query parameters with which the view is opened
         */
        public QueryParameters getQueryParameters() {
            return queryParameters;
        }
    }
}
