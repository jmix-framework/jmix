package io.jmix.flowui.screen;

import com.google.common.base.Strings;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.dialog.GeneratedVaadinDialog.OpenedChangeEvent;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.Messages;
import io.jmix.core.common.event.EventHub;
import io.jmix.core.common.event.Subscription;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.screen.Screen.AfterShowEvent;
import io.jmix.flowui.screen.Screen.BeforeShowEvent;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nullable;
import java.util.EventObject;
import java.util.Optional;
import java.util.function.Consumer;

public class DialogWindow<S extends Screen> implements HasSize, HasTheme,
        ApplicationContextAware, InitializingBean {

    protected static final String BASE_STYLE_NAME = "jmix-dialog-window";

    protected Dialog dialog;
    protected S screen;

    protected ApplicationContext applicationContext;

    // private, lazily initialized
    private EventHub eventHub = null;

    public DialogWindow(S screen) {
        this.screen = screen;
        this.dialog = createDialog();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        initScreen(screen);
        initDialog(dialog);
    }

    protected void initScreen(S screen) {
        screen.setCloseDelegate(__ -> closeInternal());
        screen.addAfterCloseListener(this::onScreenAfterClosed);
    }

    protected Dialog createDialog() {
        return new Dialog();
    }

    protected void initDialog(Dialog dialog) {
        String screenTitle = UiControllerUtils.getTitle(screen);

        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        dialog.setDraggable(true);

        dialog.addThemeVariants(DialogVariant.LUMO_NO_PADDING);
        dialog.getElement().setAttribute("aria-label", screenTitle);
        dialog.addOpenedChangeListener(this::onDialogOpenedChanged);
        dialog.addDialogCloseActionListener(this::onDialogCloseAction);

        applyDialogModeSettings(screen);

        Header header = createHeader(screenTitle);
        Component screenWrapper = createScreenWrapper(screen);
//        Footer footer = createFooter(screen);

        VerticalLayout dialogOverlay = createDialogOverlay();
        dialogOverlay.add(header, screenWrapper);

        dialog.add(dialogOverlay);
    }

    protected void applyDialogModeSettings(S screen) {
        DialogMode dialogMode = screen.getClass().getAnnotation(DialogMode.class);
        if (dialogMode != null) {
            setModal(dialogMode.modal());
            setDraggable(dialogMode.draggable());
            setResizable(dialogMode.resizable());
            setCloseOnOutsideClick(dialogMode.closeOnOutsideClick());
            setCloseOnEsc(dialogMode.closeOnEsc());

            setValueIfPresent(dialogMode.width(), this::setWidth);
            setValueIfPresent(dialogMode.maxWidth(), this::setMaxWidth);
            setValueIfPresent(dialogMode.minWidth(), this::setMinWidth);

            setValueIfPresent(dialogMode.height(), this::setHeight);
            setValueIfPresent(dialogMode.maxHeight(), this::setMaxHeight);
            setValueIfPresent(dialogMode.minHeight(), this::setMinHeight);
        }

        // TODO: gg, apply dialog mode setting from XML
    }

    protected void setValueIfPresent(@Nullable String value, Consumer<String> setter) {
        if (!Strings.isNullOrEmpty(value)) {
            setter.accept(value);
        }
    }

    protected Header createHeader(String screenTitle) {
        Header header = new Header();
        header.addClassNames(BASE_STYLE_NAME + "-header", "draggable");

        H2 title = new H2(screenTitle);
        title.setClassName(BASE_STYLE_NAME + "-title");
        header.add(title);

        Button closeButton = createHeaderCloseButton();
        header.add(closeButton);

        return header;
    }

    protected Button createHeaderCloseButton() {
        JmixButton closeButton = uiComponents().create(JmixButton.class);
        closeButton.setIcon(new Icon(VaadinIcon.CLOSE_SMALL));
        closeButton.addThemeVariants(
                ButtonVariant.LUMO_TERTIARY_INLINE,
                ButtonVariant.LUMO_ICON,
                ButtonVariant.LUMO_CONTRAST
        );
        closeButton.setClassName(BASE_STYLE_NAME + "-close-button");
        closeButton.setTitle(messages().getMessage("dialogWindow.closeButton.description"));
        closeButton.addClickListener(this::onCloseButtonClicked);
        return closeButton;
    }

    protected void onCloseButtonClicked(ClickEvent<Button> event) {
        screen.closeWithDefaultAction();
    }

    protected Component createScreenWrapper(S screen) {
        Scroller scroller = new Scroller(screen);
        scroller.setHeightFull();
        scroller.setClassName(BASE_STYLE_NAME + "-screen-wrapper");
        return scroller;
    }

    protected VerticalLayout createDialogOverlay() {
        VerticalLayout dialogContent = new VerticalLayout();
        dialogContent.setPadding(false);
        dialogContent.setSpacing(false);
        dialogContent.setHeightFull();
        dialogContent.getStyle().remove("width");
        dialogContent.setAlignItems(FlexComponent.Alignment.STRETCH);
        dialogContent.setClassName(BASE_STYLE_NAME + "-overlay");
        return dialogContent;
    }

    // TODO: gg, get from a screen
    /*protected Footer createFooter(Screen screen) {
        Button cancelButton = new Button("Cancel", e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        Button saveButton = new Button("Save", e -> dialog.close());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, saveButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        return new Footer(buttonLayout);
    }*/

    protected EventHub getEventHub() {
        if (eventHub == null) {
            eventHub = new EventHub();
        }

        return eventHub;
    }

    protected <E> void publish(Class<E> eventType, E event) {
        if (eventHub != null) {
            eventHub.publish(eventType, event);
        }
    }

    protected boolean hasSubscriptions(Class<?> eventClass) {
        return eventHub != null && eventHub.hasSubscriptions(eventClass);
    }

    public S getScreen() {
        return screen;
    }

    public void open() {
        fireScreenBeforeShowEvent(screen);
        dialog.open();
    }

    public void close() {
        close(false);
    }

    public void close(boolean force) {
        screen.close(force ? StandardOutcome.DISCARD : StandardOutcome.CLOSE);
    }

    protected void closeInternal() {
        dialog.close();
    }

    protected void onDialogOpenedChanged(OpenedChangeEvent<Dialog> openedChangeEvent) {
        if (openedChangeEvent.isOpened()) {
            fireScreenAfterShowEvent(screen);

            AfterOpenEvent<S> event = new AfterOpenEvent<>(this);
            publish(AfterOpenEvent.class, event);
        }
    }

    protected void onScreenAfterClosed(Screen.AfterCloseEvent closeEvent) {
        AfterCloseEvent<S> event = new AfterCloseEvent<>(this, closeEvent.getCloseAction());
        publish(AfterCloseEvent.class, event);
    }

    protected void fireScreenBeforeShowEvent(Screen screen) {
        UiControllerUtils.fireEvent(screen, new BeforeShowEvent(screen));
    }

    protected void fireScreenAfterShowEvent(Screen screen) {
        UiControllerUtils.fireEvent(screen, new AfterShowEvent(screen));
    }

    protected void onDialogCloseAction(Dialog.DialogCloseActionEvent event) {
        if (event.isFromClient()) {
            screen.closeWithDefaultAction();
        }
    }

    @SuppressWarnings("unchecked")
    public Registration addAfterOpenListener(Consumer<AfterOpenEvent<S>> listener) {
        Subscription subscription = getEventHub().subscribe(AfterOpenEvent.class, ((Consumer) listener));
        return Registration.once(subscription::remove);
    }

    @SuppressWarnings("unchecked")
    public Registration addAfterCloseListener(Consumer<AfterCloseEvent<S>> listener) {
        Subscription subscription = getEventHub().subscribe(AfterCloseEvent.class, ((Consumer) listener));
        return Registration.once(subscription::remove);
    }

    //    @TriggerOnce
    public static class AfterOpenEvent<S extends Screen> extends EventObject {

        public AfterOpenEvent(DialogWindow<S> source) {
            super(source);
        }

        @SuppressWarnings("unchecked")
        @Override
        public DialogWindow<S> getSource() {
            return (DialogWindow<S>) super.getSource();
        }

        public S getScreen() {
            return getSource().getScreen();
        }
    }

    //    @TriggerOnce
    public static class AfterCloseEvent<S extends Screen> extends EventObject {

        protected final CloseAction closeAction;

        public AfterCloseEvent(DialogWindow<S> source, CloseAction closeAction) {
            super(source);

            this.closeAction = closeAction;
        }

        @SuppressWarnings("unchecked")
        @Override
        public DialogWindow<S> getSource() {
            return (DialogWindow<S>) super.getSource();
        }

        public S getScreen() {
            return getSource().getScreen();
        }

        public CloseAction getCloseAction() {
            return closeAction;
        }

        public boolean closedWith(StandardOutcome outcome) {
            return outcome.getCloseAction().equals(closeAction);
        }
    }

    @Override
    public Element getElement() {
        return dialog.getElement();
    }

    public boolean isCloseOnEsc() {
        return dialog.isCloseOnEsc();
    }

    public void setCloseOnEsc(boolean closeOnEsc) {
        dialog.setCloseOnEsc(closeOnEsc);
    }

    public boolean isCloseOnOutsideClick() {
        return dialog.isCloseOnOutsideClick();
    }

    public void setCloseOnOutsideClick(boolean closeOnOutsideClick) {
        dialog.setCloseOnOutsideClick(closeOnOutsideClick);
    }

    public boolean isModal() {
        return dialog.isModal();
    }

    public void setModal(boolean modal) {
        dialog.setModal(modal);
    }

    public boolean isDraggable() {
        return dialog.isDraggable();
    }

    public void setDraggable(boolean draggable) {
        dialog.setDraggable(draggable);
    }

    public boolean isResizable() {
        return dialog.isResizable();
    }

    public void setResizable(boolean resizable) {
        dialog.setResizable(resizable);
    }

    @Override
    public void setWidth(String width) {
        dialog.setWidth(width);
    }

    @Override
    public void setWidth(float width, Unit unit) {
        dialog.setWidth(width, unit);
    }

    @Override
    public void setMinWidth(String minWidth) {
        dialog.setMinWidth(minWidth);
    }

    @Override
    public void setMinWidth(float minWidth, Unit unit) {
        dialog.setMinWidth(minWidth, unit);
    }

    @Override
    public void setMaxWidth(String maxWidth) {
        dialog.setMaxWidth(maxWidth);
    }

    @Override
    public void setMaxWidth(float maxWidth, Unit unit) {
        dialog.setMaxWidth(maxWidth, unit);
    }

    @Override
    public String getWidth() {
        return dialog.getWidth();
    }

    @Override
    public String getMinWidth() {
        return dialog.getMinWidth();
    }

    @Override
    public String getMaxWidth() {
        return dialog.getMaxWidth();
    }

    @Override
    public Optional<Unit> getWidthUnit() {
        return dialog.getWidthUnit();
    }

    @Override
    public void setHeight(String height) {
        dialog.setHeight(height);
    }

    @Override
    public void setHeight(float height, Unit unit) {
        dialog.setHeight(height, unit);
    }

    @Override
    public void setMinHeight(String minHeight) {
        dialog.setMinHeight(minHeight);
    }

    @Override
    public void setMinHeight(float minHeight, Unit unit) {
        dialog.setMinHeight(minHeight, unit);
    }

    @Override
    public void setMaxHeight(String maxHeight) {
        dialog.setMaxHeight(maxHeight);
    }

    @Override
    public void setMaxHeight(float maxHeight, Unit unit) {
        dialog.setMaxHeight(maxHeight, unit);
    }

    @Override
    public String getHeight() {
        return dialog.getHeight();
    }

    @Override
    public String getMinHeight() {
        return dialog.getMinHeight();
    }

    @Override
    public String getMaxHeight() {
        return dialog.getMaxHeight();
    }

    @Override
    public Optional<Unit> getHeightUnit() {
        return dialog.getHeightUnit();
    }

    @Override
    public void setSizeFull() {
        dialog.setSizeFull();
    }

    @Override
    public void setWidthFull() {
        dialog.setWidthFull();
    }

    @Override
    public void setHeightFull() {
        dialog.setHeightFull();
    }

    @Override
    public void setSizeUndefined() {
        dialog.setSizeUndefined();
    }

    @Override
    public void addThemeName(String themeName) {
        dialog.addThemeName(themeName);
    }

    @Override
    public boolean removeThemeName(String themeName) {
        return dialog.removeThemeName(themeName);
    }

    @Override
    public void setThemeName(String themeName) {
        dialog.setThemeName(themeName);
    }

    @Override
    public String getThemeName() {
        return dialog.getThemeName();
    }

    @Override
    public ThemeList getThemeNames() {
        return dialog.getThemeNames();
    }

    @Override
    public void setThemeName(String themeName, boolean set) {
        dialog.setThemeName(themeName, set);
    }

    @Override
    public boolean hasThemeName(String themeName) {
        return dialog.hasThemeName(themeName);
    }

    @Override
    public void addThemeNames(String... themeNames) {
        dialog.addThemeNames(themeNames);
    }

    @Override
    public void removeThemeNames(String... themeNames) {
        dialog.removeThemeNames(themeNames);
    }

    protected Messages messages() {
        return applicationContext.getBean(Messages.class);
    }

    protected UiComponents uiComponents() {
        return applicationContext.getBean(UiComponents.class);
    }

    // TODO: gg, override attache, detach, etc.?
}
