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

package io.jmix.flowui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Paragraph;
import io.jmix.flowui.action.inputdialog.InputDialogAction;
import io.jmix.flowui.app.inputdialog.DialogActions;
import io.jmix.flowui.app.inputdialog.InputDialog;
import io.jmix.flowui.app.inputdialog.InputParameter;
import io.jmix.flowui.backgroundtask.BackgroundTask;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.View;

import org.springframework.lang.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Provides fluent interface for configuring and displaying dialogs.
 */
public interface Dialogs {

    /**
     * Creates option dialog builder.
     * <br>
     * Example of showing an option dialog:
     * <pre>{@code
     * dialogs.createOptionDialog()
     *         .withHeader("Confirm")
     *         .withText("Do you want to discard data?")
     *         .withActions(
     *                 new DialogAction(DialogAction.Type.YES).withHandler(e -> {
     *                     // YES option selected
     *                 }),
     *                 new DialogAction(DialogAction.Type.NO).withHandler(e -> {
     *                     // NO option selected
     *                 })
     *         )
     *         .open();
     * }</pre>
     */
    OptionDialogBuilder createOptionDialog();

    /**
     * Creates message dialog builder.
     * <br>
     * Example of showing a message dialog:
     * <pre>{@code
     * dialogs.createMessageDialog()
     *         .withHeader("Attention")
     *         .withText("Report has been saved")
     *         .open();
     * }</pre>
     *
     * @return builder
     */
    MessageDialogBuilder createMessageDialog();

    /**
     * Creates input dialog builder.
     * <br>
     * Example of showing an input dialog:
     * <pre>{@code
     * dialogs.createInputDialog(this)
     *         .withParameters(
     *                 stringParameter("name").withLabel("Name"),
     *                 intParameter("count").withLabel("Count"))
     *         .withActions(DialogActions.OK_CANCEL)
     *         .withCloseListener(closeEvent ->
     *                 notifications.create("Dialog is closed")
     *                      .show()
     *                 )
     *         .withHeader("Goods")
     *         .open();
     * }</pre>
     *
     * @param origin origin view from input dialog is invoked
     * @return builder
     */
    InputDialogBuilder createInputDialog(View<?> origin);

    /**
     * Creates background task dialog builder.
     * <br>
     * Example of showing a background task dialog:
     * <pre>
     * dialogs.createBackgroundTaskDialog(backgroundTask)
     *         .withHeader("Task")
     *         .withText("My Task is Running")
     *         .withTotal(10)
     *         .withShowProgressInPercentage(true)
     *         .withCancelAllowed(true)
     *         .open();
     * </pre>
     *
     * @param backgroundTask background task to run
     * @return builder
     */
    <T extends Number, V> BackgroundTaskDialogBuilder<T, V> createBackgroundTaskDialog(BackgroundTask<T, V> backgroundTask);

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

    interface InputDialogBuilder extends DialogBuilder<InputDialogBuilder> {

        /**
         * Adds input parameter to the dialog. InputParameter describes field which will be used in the input dialog.
         * <br>
         * Example:
         * <pre>{@code
         *  dialogs.createInputDialog(this)
         *         .withParameter(
         *                 entityParameter("userField", User.class)
         *                         .withLabel("User field")
         *                         .withRequired(true)
         *         )
         *         .open();
         * } </pre>
         *
         * @param parameter input parameter that will be added to the dialog
         * @return builder
         * @see InputParameter#entityParameter(String, Class)
         */
        InputDialogBuilder withParameter(InputParameter parameter);

        /**
         * Sets input parameters. InputParameter describes field which will be used in the input dialog.
         * <br>
         * Example:
         * <pre>{@code
         *  dialogs.createInputDialog(this)
         *          .withParameters(
         *                  stringParameter("nameField")
         *                          .withLabel("Name field label")
         *                          .withDefaultValue("Default value"),
         *                  intParameter("countField")
         *                          .withLabel("Count field label")
         *                          .withRequired(true))
         *          .open();
         *  } </pre>
         *
         * @param parameters input parameters
         * @return builder
         * @see InputParameter#stringParameter(String)
         * @see InputParameter#intParameter(String)
         */
        InputDialogBuilder withParameters(InputParameter... parameters);

        /**
         * Sets responsive steps. Responsive steps used in describing the responsive layouting behavior of a
         * {@link FormLayout}.
         *
         * @param responsiveSteps responsive steps
         * @return builder
         */
        InputDialogBuilder withResponsiveSteps(List<ResponsiveStep> responsiveSteps);

        /**
         * Sets labels position for default responsive steps.
         *
         * @param labelsPosition position of labels
         * @return builder
         * @see #withResponsiveSteps(List)
         */
        InputDialogBuilder withLabelsPosition(LabelsPosition labelsPosition);

        /**
         * Add close listener to the dialog. See close actions for {@link DialogActions} in {@link InputDialog}.
         *
         * @param listener close listener to add
         * @return builder
         */
        InputDialogBuilder withCloseListener(ComponentEventListener<InputDialog.InputDialogCloseEvent> listener);

        /**
         * Sets dialog actions.
         * <p>
         * Note, if there is no actions are set input dialog will use {@link DialogActions#OK_CANCEL} by default.
         * </p>
         * Example:
         * <pre>{@code
         *  dialogs.createInputDialog(this)
         *         .withHeader("Dialog header")
         *         .withParameter(parameter("nameField").withLabel("Name"))
         *         .withActions(
         *                 action("okAction")
         *                         .withText("OK")
         *                         .withIcon(VaadinIcon.CHECK.create())
         *                         .withHandler(event -> {
         *                             InputDialog inputDialog = event.getInputDialog();
         *                             // do logic
         *                             inputDialog.close(InputDialog.INPUT_DIALOG_OK_ACTION);
         *                         }),
         *                 action("cancelAction")
         *                         .withText("Cancel")
         *                         .withIcon(VaadinIcon.CANCEL.create())
         *                         .withValidationRequired(false)
         *                         .withHandler(event -> {
         *                             // do logic
         *                         }))
         *         .open();
         * }
         * </pre>
         *
         * @param actions actions
         * @return builder
         * @see InputDialogAction#action(String)
         */
        InputDialogBuilder withActions(InputDialogAction... actions);

        /**
         * Sets predefined dialog actions. "OK" and "YES" actions always check fields validation before close the dialog.
         * By default, if there is no actions are set input dialog will use {@link DialogActions#OK_CANCEL}.
         *
         * @param actions actions
         * @return builder
         */
        InputDialogBuilder withActions(DialogActions actions);

        /**
         * Sets dialog actions and result handler. "OK" and "YES" actions always check fields validation before close
         * the dialog. Handler is invoked after close event and can be used instead of
         * {@link #withCloseListener(ComponentEventListener)}.
         * Example
         * <pre>{@code
         *  dialogs.createInputDialog(this)
         *         .withHeader("Dialog header")
         *         .withParameter(parameter("nameField").withLabel("Name"))
         *         .withActions(DialogActions.OK_CANCEL, result -> {
         *             switch (result.getCloseActionType()) {
         *                 case OK:
         *                     // do logic
         *                     break;
         *                 case CANCEL:
         *                     // do logic
         *                     break;
         *             }
         *         })
         *         .open();
         * } </pre>
         *
         * @param actions       dialog actions
         * @param resultHandler result handler
         * @return builder
         */
        InputDialogBuilder withActions(DialogActions actions, Consumer<InputDialog.InputDialogResult> resultHandler);

        /**
         * Sets additional handler for field validation. It receives input dialog context and must return {@link ValidationErrors}
         * instance. Returned validation errors will be shown with another errors from fields.
         * Example
         * <pre>{@code
         *  dialogs.createInputDialog(this)
         *         .withParameters(
         *                 stringParameter("phoneField").withLabel("Phone"),
         *                 stringParameter("addressField").withLabel("Address"))
         *         .withValidator(context -> {
         *             String phone = context.getValue("phoneField");
         *             String address = context.getValue("addressField");
         *             if (Strings.isNullOrEmpty(phone) && Strings.isNullOrEmpty(address)) {
         *                 return ValidationErrors.of("Phone or Address should be filled");
         *             }
         *             return ValidationErrors.none();
         *         })
         *         .open();
         *  }</pre>
         *
         * @param validator validator
         * @return builder
         */
        InputDialogBuilder withValidator(Function<InputDialog.ValidationContext, ValidationErrors> validator);

        /**
         * Opens the dialog.
         *
         * @return opened input dialog
         */
        InputDialog open();

        /**
         * Builds the input dialog.
         *
         * @return input dialog
         */
        DialogWindow<InputDialog> build();

        /**
         * Enum for describing the position of label components in a
         * {@link InputDialog}.
         */
        enum LabelsPosition {

            /**
             * Labels are displayed on the left hand side of the wrapped
             * component.
             */
            ASIDE,

            /**
             * Labels are displayed atop the wrapped component.
             */
            TOP
        }
    }

    /**
     * Builder of background task dialog.
     */
    interface BackgroundTaskDialogBuilder<T extends Number, V> extends
            HasHeader<BackgroundTaskDialogBuilder<T, V>>,
            HasText<BackgroundTaskDialogBuilder<T, V>>,
            HasTheme<BackgroundTaskDialogBuilder<T, V>>,
            HasStyle<BackgroundTaskDialogBuilder<T, V>>,
            Draggable<BackgroundTaskDialogBuilder<T, V>>,
            Resizable<BackgroundTaskDialogBuilder<T, V>> {

        /**
         * Determines whether the dialog can be closed.
         * <p>
         * The default value is {@code false}.
         *
         * @param cancelAllowed {@code true} if dialog is closeable
         * @return builder
         */
        BackgroundTaskDialogBuilder<T, V> withCancelAllowed(boolean cancelAllowed);

        /**
         * @return {@code true} if the dialog can be closed
         */
        boolean isCancelAllowed();

        /**
         * Sets amount of items to be processed by background task.
         * <br>
         * Use {@link io.jmix.flowui.backgroundtask.TaskLifeCycle#publish(Object[])} to notify the dialog about progress
         * completion.
         *
         * @param total amount of items to be processed by background task,
         * @return builder
         */
        BackgroundTaskDialogBuilder<T, V> withTotal(Number total);

        /**
         * @return amount of items to be processed by background task
         */
        Number getTotal();

        /**
         * Sets whether progress should be represented as percentage (rather than as raw number).
         *
         * @param percentProgress {@code true} to show progress in percents
         * @return builder
         */
        BackgroundTaskDialogBuilder<T, V> withShowProgressInPercentage(boolean percentProgress);

        /**
         * @return {@code true} if progress should is shown in percents
         */
        boolean isShowProgressInPercentage();

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
