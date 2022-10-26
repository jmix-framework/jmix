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

import com.google.common.base.Strings;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.dialog.GeneratedVaadinDialog.OpenedChangeEvent;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.dom.ClassList;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.Messages;
import io.jmix.core.common.event.EventHub;
import io.jmix.core.common.event.Subscription;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.View.BeforeShowEvent;
import io.jmix.flowui.view.View.ReadyEvent;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nullable;
import java.util.EventObject;
import java.util.Optional;
import java.util.function.Consumer;

public class DialogWindow<V extends View<?>> implements HasSize, HasTheme, HasStyle,
        ApplicationContextAware, InitializingBean {

    protected static final String BASE_CLASS_NAME = "jmix-dialog-window";

    protected Dialog dialog;
    protected V view;

    protected ApplicationContext applicationContext;

    // private, lazily initialized
    private EventHub eventHub = null;

    public DialogWindow(V view) {
        this.view = view;
        this.dialog = createDialog();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        initView(view);
        initDialog(dialog);
    }

    protected void initView(V view) {
        view.setCloseDelegate(__ -> closeInternal());
        view.addAfterCloseListener(this::onViewAfterClosed);
    }

    protected Dialog createDialog() {
        return new Dialog();
    }

    protected void initDialog(Dialog dialog) {
        String title = view.getPageTitle();

        dialog.setHeaderTitle(title);
        dialog.getHeader().add(createHeaderCloseButton());

        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        dialog.setDraggable(true);

        dialog.addThemeVariants(DialogVariant.LUMO_NO_PADDING);
        dialog.getElement().setAttribute("aria-label", title);
        dialog.addOpenedChangeListener(this::onDialogOpenedChanged);
        dialog.addDialogCloseActionListener(this::onDialogCloseAction);

        applyDialogModeSettings(view);

        Component wrapper = createViewWrapper(view);
        dialog.add(wrapper);
    }

    protected void postInitDialog(Dialog dialog) {
        String title = view.getPageTitle();

        dialog.setHeaderTitle(title);
        dialog.getElement().setAttribute("aria-label", title);
    }

    protected void applyDialogModeSettings(V view) {
        DialogMode dialogMode = view.getClass().getAnnotation(DialogMode.class);
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
    }

    protected void setValueIfPresent(@Nullable String value, Consumer<String> setter) {
        if (!Strings.isNullOrEmpty(value)) {
            setter.accept(value);
        }
    }

    protected Button createHeaderCloseButton() {
        JmixButton closeButton = uiComponents().create(JmixButton.class);
        closeButton.setIcon(new Icon(VaadinIcon.CLOSE_SMALL));
        closeButton.addThemeVariants(
                ButtonVariant.LUMO_TERTIARY_INLINE,
                ButtonVariant.LUMO_ICON,
                ButtonVariant.LUMO_CONTRAST
        );
        closeButton.setClassName(BASE_CLASS_NAME + "-close-button");
        closeButton.setTitle(messages().getMessage("dialogWindow.closeButton.description"));
        closeButton.addClickListener(this::onCloseButtonClicked);
        return closeButton;
    }

    protected void onCloseButtonClicked(ClickEvent<Button> event) {
        view.closeWithDefaultAction();
    }

    protected Component createViewWrapper(V view) {
        Scroller scroller = new Scroller(view);
        scroller.setHeightFull();
        scroller.setClassName(BASE_CLASS_NAME + "-view-wrapper");
        return scroller;
    }

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

    /**
     * @return a view witch is opened in this dialog window
     */
    public V getView() {
        return view;
    }

    /**
     * Opens the dialog.
     */
    public void open() {
        fireViewBeforeShowEvent(view);
        // In case of dynamic title, we can obtain it after
        // all possible dependant properties are set
        postInitDialog(dialog);

        dialog.open();
    }

    /**
     * Requests closing the dialog.
     */
    public void close() {
        close(false);
    }

    /**
     * Requests closing the dialog.
     *
     * @param force {@code true} to close the dialog without checking the state
     *              (e.g. unsaved changes), {@code false} otherwise.
     */
    public void close(boolean force) {
        view.close(force ? StandardOutcome.DISCARD : StandardOutcome.CLOSE);
    }

    protected void closeInternal() {
        dialog.close();
    }

    protected void onDialogOpenedChanged(OpenedChangeEvent<Dialog> openedChangeEvent) {
        if (openedChangeEvent.isOpened()) {
            fireViewReadyEvent(view);

            AfterOpenEvent<V> event = new AfterOpenEvent<>(this);
            publish(AfterOpenEvent.class, event);
        }
    }

    protected void onViewAfterClosed(View.AfterCloseEvent closeEvent) {
        AfterCloseEvent<V> event = new AfterCloseEvent<>(this, closeEvent.getCloseAction());
        publish(AfterCloseEvent.class, event);
    }

    protected void fireViewBeforeShowEvent(View<?> view) {
        ViewControllerUtils.fireEvent(view, new BeforeShowEvent(view));
    }

    protected void fireViewReadyEvent(View<?> view) {
        ViewControllerUtils.fireEvent(view, new ReadyEvent(view));
    }

    protected void onDialogCloseAction(Dialog.DialogCloseActionEvent event) {
        if (event.isFromClient()) {
            view.closeWithDefaultAction();
        }
    }

    /**
     * Adds {@link AfterOpenEvent} listener.
     *
     * @param listener the listener to add
     * @return a Registration for removing the event listener
     */
    @SuppressWarnings("unchecked")
    public Registration addAfterOpenListener(Consumer<AfterOpenEvent<V>> listener) {
        Subscription subscription = getEventHub().subscribe(AfterOpenEvent.class, ((Consumer) listener));
        return Registration.once(subscription::remove);
    }

    /**
     * Adds {@link AfterCloseEvent} listener.
     *
     * @param listener the listener to add
     * @return a Registration for removing the event listener
     */
    @SuppressWarnings("unchecked")
    public Registration addAfterCloseListener(Consumer<AfterCloseEvent<V>> listener) {
        Subscription subscription = getEventHub().subscribe(AfterCloseEvent.class, ((Consumer) listener));
        return Registration.once(subscription::remove);
    }

    public static class AfterOpenEvent<V extends View<?>> extends EventObject {

        public AfterOpenEvent(DialogWindow<V> source) {
            super(source);
        }

        @SuppressWarnings("unchecked")
        @Override
        public DialogWindow<V> getSource() {
            return (DialogWindow<V>) super.getSource();
        }

        public V getView() {
            return getSource().getView();
        }
    }

    public static class AfterCloseEvent<V extends View<?>> extends EventObject {

        protected final CloseAction closeAction;

        public AfterCloseEvent(DialogWindow<V> source, CloseAction closeAction) {
            super(source);

            this.closeAction = closeAction;
        }

        @SuppressWarnings("unchecked")
        @Override
        public DialogWindow<V> getSource() {
            return (DialogWindow<V>) super.getSource();
        }

        public V getView() {
            return getSource().getView();
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

    /**
     * @return whether this dialog can be closed by hitting the esc-key or not.
     */
    public boolean isCloseOnEsc() {
        return dialog.isCloseOnEsc();
    }

    /**
     * Sets whether this dialog can be closed by hitting the esc-key or not.
     *
     * @param closeOnEsc {@code true} to enable closing this dialog with the esc-key,
     *                   {@code false} to disable it
     */
    public void setCloseOnEsc(boolean closeOnEsc) {
        dialog.setCloseOnEsc(closeOnEsc);
    }

    /**
     * @return whether this dialog can be closed by clicking outside of it or not.
     */
    public boolean isCloseOnOutsideClick() {
        return dialog.isCloseOnOutsideClick();
    }

    /**
     * Sets whether this dialog can be closed by clicking outside of it or not.
     *
     * @param closeOnOutsideClick {@code true} to enable closing this dialog with an outside
     *                            click, {@code false} to disable it
     */
    public void setCloseOnOutsideClick(boolean closeOnOutsideClick) {
        dialog.setCloseOnOutsideClick(closeOnOutsideClick);
    }

    /**
     * @return whether component is set as modal or modeless dialog.
     */
    public boolean isModal() {
        return dialog.isModal();
    }

    /**
     * Sets whether component will open modal or modeless dialog.
     *
     * @param modal {@code false} to enable dialog to open as modeless modal, {@code true} otherwise
     */
    public void setModal(boolean modal) {
        dialog.setModal(modal);
    }

    /**
     * Sets whether dialog is enabled to be dragged by the user or not.
     *
     * @return whether dialog is enabled to be dragged or not.
     */
    public boolean isDraggable() {
        return dialog.isDraggable();
    }

    /**
     * @param draggable {@code true} to enable dragging of the dialog, {@code false} otherwise
     */
    public void setDraggable(boolean draggable) {
        dialog.setDraggable(draggable);
    }

    /**
     * @return whether dialog is enabled to be resized or not.
     */
    public boolean isResizable() {
        return dialog.isResizable();
    }

    /**
     * Sets whether dialog can be resized by user or not.
     *
     * @param resizable {@code true} to enabled resizing of the dialog, {@code false} otherwise.
     */
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

    @Override
    public void addClassName(String className) {
        dialog.addClassName(className);
    }

    @Override
    public boolean removeClassName(String className) {
        return dialog.removeClassName(className);
    }

    @Override
    public void setClassName(String className) {
        dialog.setClassName(className);
    }

    @Override
    public String getClassName() {
        return dialog.getClassName();
    }

    @Override
    public ClassList getClassNames() {
        return dialog.getClassNames();
    }

    @Override
    public void setClassName(String className, boolean set) {
        dialog.setClassName(className, set);
    }

    @Override
    public boolean hasClassName(String className) {
        return dialog.hasClassName(className);
    }

    @Override
    public Style getStyle() {
        return dialog.getStyle();
    }

    @Override
    public void addClassNames(String... classNames) {
        dialog.addClassNames(classNames);
    }

    @Override
    public void removeClassNames(String... classNames) {
        dialog.removeClassNames(classNames);
    }

    protected Messages messages() {
        return applicationContext.getBean(Messages.class);
    }

    protected UiComponents uiComponents() {
        return applicationContext.getBean(UiComponents.class);
    }

    // TODO: gg, override attache, detach, etc.?
}
