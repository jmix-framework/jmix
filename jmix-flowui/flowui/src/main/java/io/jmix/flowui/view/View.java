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
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.annotation.Internal;
import io.jmix.flowui.UiViewProperties;
import io.jmix.flowui.event.view.ViewClosedEvent;
import io.jmix.flowui.event.view.ViewOpenedEvent;
import io.jmix.flowui.model.ViewData;
import io.jmix.flowui.sys.ViewSupport;
import io.jmix.flowui.sys.event.UiEventsManager;
import io.jmix.flowui.util.OperationResult;
import io.jmix.flowui.util.WebBrowserTools;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Optional;
import java.util.function.Consumer;

import static io.jmix.flowui.monitoring.UiMonitoring.startTimerSample;
import static io.jmix.flowui.monitoring.UiMonitoring.stopViewTimerSample;
import static io.jmix.flowui.monitoring.ViewLifeCycle.*;
import static io.micrometer.core.instrument.Timer.Sample;
import static io.micrometer.core.instrument.Timer.start;

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
    private MeterRegistry meterRegistry;

    private ViewData viewData;
    private ViewActions viewActions;
    private ViewFacets viewFacets;

    private Consumer<View<T>> closeDelegate;

    private boolean closeActionPerformed = false;
    private boolean preventBrowserTabClosing = false;

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

    @Autowired
    protected void setMeterRegistry(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
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
        Sample sample = start(meterRegistry);
        fireEvent(new ReadyEvent(this));
        stopViewTimerSample(sample, meterRegistry, READY, getId().orElse(null));

        ViewOpenedEvent viewOpenedEvent = new ViewOpenedEvent(this);
        applicationContext.publishEvent(viewOpenedEvent);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        fireEvent(new QueryParametersChangeEvent(this, event.getLocation().getQueryParameters()));

        Sample sample = startTimerSample(meterRegistry);
        fireEvent(new BeforeShowEvent(this));
        stopViewTimerSample(sample, meterRegistry, BEFORE_SHOW, getId().orElse(null));
    }

    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        if (!event.isPostponed()) {
            if (!closeActionPerformed) {
                CloseAction closeAction = new NavigateCloseAction(event);
                BeforeCloseEvent beforeCloseEvent = new BeforeCloseEvent(this, closeAction);

                Sample beforeCloseSample = startTimerSample(meterRegistry);
                fireEvent(beforeCloseEvent);
                stopViewTimerSample(beforeCloseSample, meterRegistry, BEFORE_CLOSE, getId().orElse(null));

                if (beforeCloseEvent.isClosePrevented()) {
                    closeActionPerformed = false;
                    return;
                }

                AfterCloseEvent afterCloseEvent = new AfterCloseEvent(this, closeAction);
                Sample afterCloseSample = startTimerSample(meterRegistry);
                fireEvent(afterCloseEvent);
                stopViewTimerSample(afterCloseSample, meterRegistry, AFTER_CLOSE, getId().orElse(null));

                ViewClosedEvent viewClosedEvent = new ViewClosedEvent(this);
                applicationContext.publishEvent(viewClosedEvent);
            }
        }

        closeActionPerformed = false;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        if (isPreventBrowserTabClosing()) {
            WebBrowserTools.preventBrowserTabClosing(this);
        }
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);

        onDetachInternal();

        if (isPreventBrowserTabClosing()) {
            WebBrowserTools.allowBrowserTabClosing(this);
        }
    }

    @Internal
    protected void onDetachInternal() {
        removeApplicationListeners();
        removeViewAttributes();
        unregisterBackNavigation();
    }

    protected void unregisterBackNavigation() {
        getViewSupport().unregisterBackwardNavigation(this);
    }

    protected void removeApplicationListeners() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            session.getAttribute(UiEventsManager.class).removeApplicationListeners(this);
        }
    }

    protected void removeViewAttributes() {
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

        AfterCloseEvent afterCloseEvent = new AfterCloseEvent(this, closeAction);
        fireEvent(afterCloseEvent);

        ViewClosedEvent viewClosedEvent = new ViewClosedEvent(this);
        applicationContext.publishEvent(viewClosedEvent);

        return OperationResult.success();
    }

    /**
     * @return whether this view prevents browser tab from accidentally closing
     */
    public boolean isPreventBrowserTabClosing() {
        UiViewProperties properties = applicationContext.getBean(UiViewProperties.class);
        return properties.isPreventBrowserTabClosing() && preventBrowserTabClosing;
    }

    /**
     * Sets whether this view must prevent browser tab from
     * accidentally closing. Enabled by default.
     *
     * @param preventBrowserTabClosing whether this details view must prevent
     *                                 browser tab from accidentally closing
     */
    public void setPreventBrowserTabClosing(boolean preventBrowserTabClosing) {
        this.preventBrowserTabClosing = preventBrowserTabClosing;
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
    protected Registration addQueryParametersChangeListener(ComponentEventListener<QueryParametersChangeEvent> listener) {
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
     * <strong>Note</strong> that the event is triggered only once after {@link View} creation. It means if the
     * navigation to {@link View} performed at first time, event triggered. Then if navigation performed to the same
     * {@link View}, which currently opened, second and more times event is not triggered because the {@link View}
     * instance has been already created.
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
     * <p>
     * <strong>Note</strong> consequent navigation to the same {@link View}, which currently opened, leads to
     * triggering {@link BeforeShowEvent} once more for the same {@link View} instance. For example, the user
     * navigates to the {@link View} first time: {@link View} instance is created, {@link BeforeShowEvent} is
     * triggered. Then the user navigates to the same {@link View}, which currently opened: we have the same
     * {@link View} instance, but {@link BeforeShowEvent} is triggered again.
     * <p>
     * If {@link BeforeShowEvent} method listener contains logic of adding components or loading data, it will be
     * performed again, which can lead to adding duplicated components or reloading data.
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
     * <p>
     * <strong>Note</strong> consequent navigation to the same {@link View}, which currently opened, leads to
     * triggering {@link ReadyEvent} once more for the same {@link View} instance. For example, the user
     * navigates to the {@link View} first time: {@link View} instance is created, {@link ReadyEvent} is
     * triggered. Then the user navigates to the same {@link View}, which currently opened: we have the same
     * {@link View} instance, but {@link ReadyEvent} is triggered again.
     * <p>
     * If {@link ReadyEvent} method listener contains logic of adding components or loading data, it will be
     * performed again, which can lead to adding duplicated components or reloading data.
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
     * <p>
     * <strong>Note</strong> the event can be triggered few times for one {@link View} instance. It may happen if
     * the user tries to navigate to the same {@link View}, which is currently opened. In this case,
     * {@link BeforeCloseEvent} is triggered, because we "close" the {@link View}, however due to navigation
     * the same instance of {@link View} will be opened. It means {@link BeforeCloseEvent} will be triggered again
     * for the same {@link View} instance, when user close the View or navigates to another one.
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
     * <p>
     * <strong>Note</strong> the event can be triggered few times for one {@link View} instance. It may happen if
     * the user tries to navigate to the same {@link View}, which is currently opened. In this case,
     * {@link AfterCloseEvent} is triggered, because we "close" the {@link View}, however due to navigation
     * the same instance of {@link View} will be opened. It means {@link AfterCloseEvent} will be triggered again
     * for the same {@link View} instance, when user close the View or navigates to another one.
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
     *
     * @see #addQueryParametersChangeListener(ComponentEventListener)
     */
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
