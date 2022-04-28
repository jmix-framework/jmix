package io.jmix.flowui.screen;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.router.*;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.component.layout.ScreenLayout;
import io.jmix.flowui.model.ScreenData;
import io.jmix.flowui.sys.ScreenSupport;
import io.jmix.flowui.util.OperationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Optional;
import java.util.function.Consumer;

public class Screen extends Composite<ScreenLayout>
        implements BeforeEnterObserver, AfterNavigationObserver, BeforeLeaveObserver {

    private ApplicationContext applicationContext;

    private ScreenData screenData;
    private ScreenActions screenActions;

    private Consumer<Screen> closeDelegate;

    public Screen() {
        closeDelegate = createDefaultScreenDelegate();
    }

    private Consumer<Screen> createDefaultScreenDelegate() {
        return screen -> getScreenSupport().close(this);
    }

    protected ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Autowired
    protected void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    protected ScreenLayout initContent() {
        ScreenLayout content = super.initContent();
        content.setSizeFull();

        return content;
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        fireEvent(new AfterShowEvent(this));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        fireEvent(new BeforeShowEvent(this));
    }

    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        if (!event.isPostponed()) {
            unregisterBackNavigation();
        }
    }

    protected void unregisterBackNavigation() {
        ScreenSupport screenSupport = getScreenSupport();
        screenSupport.unregisterBackNavigation(this);
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

        closeDelegate.accept(this);

        AfterCloseEvent afterCloseEvent = new AfterCloseEvent(this, closeAction);
        fireEvent(afterCloseEvent);

        return OperationResult.success();
    }

    Consumer<Screen> getCloseDelegate() {
        return closeDelegate;
    }

    void setCloseDelegate(Consumer<Screen> closeDelegate) {
        this.closeDelegate = closeDelegate;
    }

    protected ScreenData getScreenData() {
        return screenData;
    }

    protected void setScreenData(ScreenData screenData) {
        this.screenData = screenData;
    }

    protected ScreenActions getScreenActions() {
        return screenActions;
    }

    protected void setScreenActions(ScreenActions screenActions) {
        this.screenActions = screenActions;
    }

    @Override
    public void setId(String id) {
        super.setId(id);
    }

    @Override
    public Optional<String> getId() {
        return super.getId();
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

    protected Registration addAfterShowListener(ComponentEventListener<AfterShowEvent> listener) {
        return getEventBus().addListener(AfterShowEvent.class, listener);
    }

    protected Registration addBeforeCloseListener(ComponentEventListener<BeforeCloseEvent> listener) {
        return getEventBus().addListener(BeforeCloseEvent.class, listener);
    }

    protected Registration addAfterCloseListener(ComponentEventListener<AfterCloseEvent> listener) {
        return getEventBus().addListener(AfterCloseEvent.class, listener);
    }

    //    @TriggerOnce
    public static class InitEvent extends ComponentEvent<Screen> {

        public InitEvent(Screen source) {
            super(source, false);
        }
    }

    //    @TriggerOnce
    public static class BeforeShowEvent extends ComponentEvent<Screen> {

        public BeforeShowEvent(Screen source) {
            super(source, false);
        }
    }

    //    @TriggerOnce
    public static class AfterShowEvent extends ComponentEvent<Screen> {

        public AfterShowEvent(Screen source) {
            super(source, false);
        }
    }

    public static class BeforeCloseEvent extends ComponentEvent<Screen> {

        protected final CloseAction closeAction;

        protected OperationResult closeResult;
        protected boolean closePrevented = false;

        public BeforeCloseEvent(Screen source, CloseAction closeAction) {
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

    public static class AfterCloseEvent extends ComponentEvent<Screen> {

        protected final CloseAction closeAction;

        public AfterCloseEvent(Screen source, CloseAction closeAction) {
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

    private ScreenSupport getScreenSupport() {
        return applicationContext.getBean(ScreenSupport.class);
    }
}
