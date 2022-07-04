package io.jmix.flowui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Paragraph;
import io.jmix.flowui.kit.action.Action;

import javax.annotation.Nullable;

public interface Dialogs {

    OptionDialogBuilder createOptionDialog();

    MessageDialogBuilder createMessageDialog();

    interface OptionDialogBuilder extends DialogBuilder<OptionDialogBuilder>,
            HasText<OptionDialogBuilder>,
            HasContent<OptionDialogBuilder>,
            HasTheme<OptionDialogBuilder>,
            HasStyle<OptionDialogBuilder>,
            Draggable<OptionDialogBuilder>,
            Resizable<OptionDialogBuilder> {

        /**
         * Sets dialog actions.
         *
         * @param actions dialog actions
         * @return builder
         */
        OptionDialogBuilder withActions(Action... actions);

        /**
         * @return dialog actions
         */
        Action[] getActions();

        /**
         * Opens the dialog.
         */
        void open();
    }

    interface MessageDialogBuilder extends DialogBuilder<MessageDialogBuilder>,
            HasText<MessageDialogBuilder>,
            HasContent<MessageDialogBuilder>,
            HasModal<MessageDialogBuilder>,
            HasTheme<MessageDialogBuilder>,
            HasStyle<MessageDialogBuilder>,
            Closeable<MessageDialogBuilder>,
            Draggable<MessageDialogBuilder>,
            Resizable<MessageDialogBuilder> {

        /**
         * Opens the dialog.
         */
        void open();
    }

    /**
     * Base class for all Dialog Builders.
     *
     * @param <T> return type of fluent API methods
     */
    interface DialogBuilder<T extends DialogBuilder> extends HasHeader<T>, HasSize<T> {
    }

    /**
     * Represents Dialog Builders that have a header.
     *
     * @param <T> return type of fluent API methods
     */
    interface HasHeader<T> {
        /**
         * Sets a header text.
         *
         * @param header header text
         * @return builder
         */
        T withHeader(String header);

        /**
         * @return header text
         */
        @Nullable
        String getHeader();
    }

    /**
     * Represents Dialog Builders that have size setting.
     *
     * @param <T> return type of fluent API methods
     */
    interface HasSize<T> {
        /**
         * Sets dialog width.
         *
         * @param width width
         * @return builder
         */
        T withWidth(String width);

        /**
         * @return dialog width value
         */
        String getWidth();

        /**
         * Sets dialog height.
         *
         * @param height height
         * @return builder
         */
        T withHeight(String height);

        /**
         * @return dialog height value
         */
        String getHeight();
    }

    /**
     * Represents Dialog Builders that have a text inside {@link Paragraph} component as a content.
     * <p>
     * Note, overrides the content set value that was set using the {@link HasContent#withContent(Component)} method.
     *
     * @param <T> return type of fluent API methods
     */
    interface HasText<T> {
        /**
         * Sets a text.
         *
         * @param text a text
         * @return builder
         */
        T withText(String text);

        /**
         * @return a text
         */
        @Nullable
        String getText();
    }

    /**
     * Represents Dialog Builders that have a text inside {@link Paragraph} component.
     * <p>
     * Note, overrides the content set value that was set using the {@link HasText#withText(String)} method.
     *
     * @param <T> return type of fluent API methods
     */
    interface HasContent<T> {
        /**
         * Sets a content.
         *
         * @param content a content
         * @return builder
         */
        T withContent(Component content);

        /**
         * @return a content
         */
        @Nullable
        Component getContent();
    }

    /**
     * Represents Dialog Builders that have theme setting.
     *
     * @param <T> return type of fluent API methods
     */
    interface HasTheme<T> {
        /**
         * Sets the theme names of the dialog. This method overwrites any previous set theme names.
         *
         * @param themeName a space-separated string of theme names to set, or empty string to remove all theme names
         * @return builder
         */
        T withThemeName(String themeName);

        /**
         * Gets the theme names for this component.
         *
         * @return a space-separated string of theme names, empty string if there are no theme names
         * or <code>null</code> if attribute (theme) is not set at all
         */
        @Nullable
        String getThemeName();
    }

    /**
     * Represents Dialog Builders that have style setting.
     *
     * @param <T> return type of fluent API methods
     */
    interface HasStyle<T> {

        /**
         * Sets the CSS class names of this component. This method overwrites any
         * previous set class names.
         *
         * @param className a space-separated string of class names to set, or
         *                  <code>null</code> to remove all class names
         */
        T withClassName(@Nullable String className);

        /**
         * Gets the CSS class names for this component.
         *
         * @return a space-separated string of class names, or <code>null</code> if
         * there are no class names
         */
        @Nullable
        String getClassName();
    }

    /**
     * Represents Dialog Builders with close setting.
     *
     * @param <T> return type of fluent API methods
     */
    interface Closeable<T> {
        /**
         * Sets whether this dialog can be closed by clicking outside.
         * <p>
         * By default, the dialog is closable with an outside click.
         *
         * @param closeOnOutsideClick {@code true} to enable closing this dialog with an outside
         *                            click, {@code false} to disable it
         * @return builder
         */
        T withCloseOnOutsideClick(boolean closeOnOutsideClick);

        /**
         * @return {@code true} if this dialog can be closed by an outside click, {@code false} otherwise
         */
        boolean isCloseOnOutsideClick();

        /**
         * Sets whether this dialog can be closed by hitting the esc-key or not.
         * <p>
         * By default, the dialog is closable with esc.
         *
         * @param closeOnEsc {@code true} to enable closing this dialog with the esc-key, {@code false} to disable it
         * @return builder
         */
        T withCloseOnEsc(boolean closeOnEsc);

        /**
         * @return {@code true} if this dialog can be closed with the esc-key, {@code false} otherwise
         */
        boolean isCloseOnEsc();
    }

    /**
     * Represents Dialog Builders that have modal setting.
     *
     * @param <T> return type of fluent API methods
     */
    interface HasModal<T> {
        /**
         * Sets whether component will open modal or modeless dialog.
         *
         * @param modal {@code false} to enable dialog to open as modeless modal, {@code true} otherwise.
         * @return builder
         */
        T withModal(boolean modal);

        /**
         * @return {@code true} if modal dialog, {@code false} otherwise.
         */
        boolean isModal();
    }

    /**
     * Represents Dialog Builders that can be dragged.
     *
     * @param <T> return type of fluent API methods
     */
    interface Draggable<T> {
        /**
         * Sets whether dialog is enabled to be dragged by the user or not.
         *
         * @param draggable {@code true} to enable dragging of the dialog, {@code false} otherwise
         * @return builder
         */
        T withDraggable(boolean draggable);

        /**
         * @return {@code true} if dragging is enabled, {@code false} otherwise
         */
        boolean isDraggable();
    }

    /**
     * Represents Dialog Builders that can be resized.
     *
     * @param <T> return type of fluent API methods
     */
    interface Resizable<T> {
        /**
         * Sets whether dialog can be resized by user or not.
         * <p>
         * By default, the dialog is not resizable.
         *
         * @param resizable {@code true} to enabled resizing of the dialog, {@code false} otherwise.
         * @return builder
         */
        T withResizable(boolean resizable);

        /**
         * @return {@code true} if resizing is enabled, {@code false} otherwise
         */
        boolean isResizable();

        /**
         * Sets a dialog min width.
         *
         * @param minWidth a dialog min width
         * @return builder
         */
        T withMinWidth(String minWidth);

        /**
         * @return dialog min width value
         */
        String getMinWidth();

        /**
         * Sets a dialog min height.
         *
         * @param minHeight a dialog min height
         * @return builder
         */
        T withMinHeight(String minHeight);

        /**
         * @return dialog min height value
         */
        String getMinHeight();

        /**
         * Sets a dialog max width.
         *
         * @param maxWidth a dialog max width
         * @return builder
         */
        T withMaxWidth(String maxWidth);

        /**
         * @return dialog max width value
         */
        String getMaxWidth();

        /**
         * Sets a dialog max height.
         *
         * @param maxHeight a dialog max height
         * @return builder
         */
        T withMaxHeight(String maxHeight);

        /**
         * @return dialog max height value
         */
        String getMaxHeight();
    }
}
