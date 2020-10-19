/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui;

import io.jmix.ui.action.Action;
import io.jmix.ui.app.backgroundwork.BackgroundWorkDialog;
import io.jmix.ui.app.inputdialog.DialogActions;
import io.jmix.ui.app.inputdialog.InputDialog;
import io.jmix.ui.app.inputdialog.InputParameter;
import io.jmix.ui.component.ContentMode;
import io.jmix.ui.component.SizeUnit;
import io.jmix.ui.component.ValidationErrors;
import io.jmix.ui.component.WindowMode;
import io.jmix.ui.component.inputdialog.InputDialogAction;
import io.jmix.ui.executor.BackgroundTask;
import io.jmix.ui.screen.FrameOwner;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Utility dialogs API.
 */
public interface Dialogs {

    /**
     * Creates option dialog builder.
     * <br>
     * Example of showing an option dialog:
     * <pre>{@code
     * dialogs.createOptionDialog()
     *         .withCaption("Question")
     *         .withMessage("Do you want to discard data?")
     *         .withActions(
     *                 new DialogAction(DialogAction.Type.YES).withHandler(e -> {
     *                     // YES option selected
     *                 }),
     *                 new DialogAction(DialogAction.Type.NO).withHandler(e -> {
     *                     // NO option selected
     *                 })
     *         )
     *         .show();
     * }</pre>
     *
     * @return builder
     */
    OptionDialogBuilder createOptionDialog();

    /**
     * Creates message dialog builder.
     * <br>
     * Example of showing a message dialog:
     * <pre>{@code
     * dialogs.createMessageDialog()
     *         .withCaption("Alert")
     *         .withMessage("Report has been saved")
     *         .show();
     * }</pre>
     *
     * @return builder
     */
    MessageDialogBuilder createMessageDialog();

    /**
     * Creates exception dialog builder.
     * <br>
     * Example of showing an exception dialog:
     * <pre>{@code
     * dialogs.createExceptionDialog()
     *         .withCaption("Alert")
     *         .withMessage("Report has been saved")
     *         .withThrowable(exception)
     *         .show();
     * }</pre>
     *
     * @return builder
     */
    ExceptionDialogBuilder createExceptionDialog();

    /**
     * Creates input dialog builder.
     * <p>
     * Example of showing an input dialog:
     * <pre>{@code
     * dialogs.createInputDialog(this)
     *         .withParameters(
     *                 stringParameter("name").withCaption("Name"),
     *                 intParameter("count").withCaption("Count"))
     *         .withActions(DialogActions.OK_CANCEL)
     *         .withCloseListener(closeEvent ->
     *                 notifications.create(Notifications.NotificationType.TRAY)
     *                         .withCaption("Dialog is closed")
     *                         .show())
     *         .withCaption("Goods")
     *         .show();
     * }</pre>
     *
     * @param owner origin screen from input dialog is invoked
     * @return builder
     */
    InputDialogBuilder createInputDialog(FrameOwner owner);

    /**
     * Creates background work dialog builder.
     * <p>
     * Example of showing a background work dialog:
     * <pre>{@code
     * dialogs.createBackgroundWorkDialog(this, backgroundTask)
     *         .withCaption("Task")
     *         .withMessage("My Task is Running")
     *         .withTotal(total)
     *         .withShowProgressInPercentage(true)
     *         .withCancelAllowed(true)
     *         .show();
     * }</pre>
     *
     * @param owner origin screen from the dialog is invoked
     * @return builder
     */
    <T extends Number, V> BackgroundWorkDialogBuilder<T, V> createBackgroundWorkDialog(FrameOwner owner, BackgroundTask<T, V> backgroundTask);

    /**
     * Builder of dialog with option buttons.
     */
    interface OptionDialogBuilder
            extends DialogBuilder<OptionDialogBuilder>,
            HasMessage<OptionDialogBuilder>,
            HasContentMode<OptionDialogBuilder>,
            HasWindowMode<OptionDialogBuilder>,
            HasStyleName<OptionDialogBuilder>,
            HasHtmlSanitizer<OptionDialogBuilder> {
        /**
         * Sets dialog actions.
         *
         * @param actions actions
         * @return builder
         */
        OptionDialogBuilder withActions(Action... actions);

        /**
         * @return dialog actions
         */
        Action[] getActions();

        /**
         * Shows the dialog.
         */
        void show();
    }

    /**
     * Builder of information dialog.
     */
    interface MessageDialogBuilder
            extends DialogBuilder<MessageDialogBuilder>,
            HasMessage<MessageDialogBuilder>,
            HasContentMode<MessageDialogBuilder>,
            HasModal<MessageDialogBuilder>,
            HasWindowMode<MessageDialogBuilder>,
            HasStyleName<MessageDialogBuilder>,
            HasHtmlSanitizer<MessageDialogBuilder> {
        /**
         * @return true if window can be closed by click outside of window content (by modality curtain)
         */
        boolean isCloseOnClickOutside();

        /**
         * Sets if window can be closed by click outside of window content (by modality curtain).
         *
         * @param closeOnClickOutside true if window to be closed by click outside of window content (by modality curtain)
         * @return builder
         */
        MessageDialogBuilder withCloseOnClickOutside(boolean closeOnClickOutside);

        /**
         * Enables closeOnClickOutside mode for window, so window can be closed by click outside of window content
         * (by modality curtain).
         *
         * @return builder
         */
        MessageDialogBuilder closeOnClickOutside();

        /**
         * Shows the dialog.
         */
        void show();
    }

    /**
     * Builder of unhandled exception dialog.
     */
    interface ExceptionDialogBuilder extends
            HasCaption<ExceptionDialogBuilder>,
            HasMessage<ExceptionDialogBuilder> {
        /**
         * Sets exception object.
         *
         * @param throwable throwable
         * @return builder
         */
        ExceptionDialogBuilder withThrowable(Throwable throwable);

        /**
         * @return throwable
         */
        Throwable getThrowable();

        /**
         * Shows the dialog.
         */
        void show();
    }

    /**
     * Builder for dialogs with inputs.
     */
    interface InputDialogBuilder extends DialogBuilder<InputDialogBuilder> {

        /**
         * Adds input parameter to the dialog. InputParameter describes field which will be used in the input dialog.
         * <p>
         * Example:
         * <pre>{@code
         *  dialogs.createInputDialog(this)
         *         .withParameter(
         *                 entityParameter("userField", User.class)
         *                         .withCaption("User field")
         *                         .withRequired(true)
         *         )
         *         .show();
         * } </pre>
         *
         * @param parameter input parameter that will be added to the dialog
         * @return builder
         * @see InputParameter#entityParameter(String, Class)
         */
        InputDialogBuilder withParameter(InputParameter parameter);

        /**
         * Sets input parameters. InputParameter describes field which will be used in the input dialog.
         * <p>
         * Example:
         * <pre>{@code
         *  dialogs.createInputDialog(this)
         *          .withParameters(
         *                  stringParameter("nameField")
         *                          .withCaption("Name field caption")
         *                          .withDefaultValue("default value"),
         *                  intParameter("countField")
         *                          .withCaption("Count field caption")
         *                          .withRequired(true))
         *          .show();
         *  } </pre>
         *
         * @param parameters input parameters
         * @return builder
         * @see InputParameter#stringParameter(String)
         * @see InputParameter#intParameter(String)
         */
        InputDialogBuilder withParameters(InputParameter... parameters);

        /**
         * Add close listener to the dialog. See close actions for {@link DialogActions} in {@link InputDialog}.
         *
         * @param listener close listener to add
         * @return builder
         */
        InputDialogBuilder withCloseListener(Consumer<InputDialog.InputDialogCloseEvent> listener);

        /**
         * Sets dialog actions. {@link InputDialogAction} provides access to input dialog in {@link InputDialogAction.InputDialogActionPerformed}
         * where it is possible to get values form the fields and implement logic to close dialog.
         * <p>
         * Note, if there is no actions are set input dialog will use {@link DialogActions#OK_CANCEL} by default.
         * </p>
         * Example:
         * <pre>{@code
         *  dialogs.createInputDialog(this)
         *         .withCaption("Dialog caption")
         *         .withParameter(parameter("nameField").withCaption("Name"))
         *         .withActions(
         *                 action("okAction")
         *                         .withCaption("OK")
         *                         .withIcon(JmixIcon.DIALOG_OK)
         *                         .withHandler(event -> {
         *                             InputDialog inputDialog = event.getInputDialog();
         *                             String name = inputDialog.getValue("nameField");
         *                             // do logic
         *                             inputDialog.close(InputDialog.INPUT_DIALOG_OK_ACTION);
         *                         }),
         *                 action("cancelAction")
         *                         .withCaption("Cancel")
         *                         .withIcon(JmixIcon.DIALOG_CANCEL)
         *                         .withValidationRequired(false)
         *                         .withHandler(event -> {
         *                             InputDialog inputDialog = event.getInputDialog();
         *                             inputDialog.close(InputDialog.INPUT_DIALOG_CANCEL_ACTION);
         *                         }))
         *         .show();
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
         * By default if there is no actions are set input dialog will use {@link DialogActions#OK_CANCEL}.
         *
         * @param actions actions
         * @return builder
         */
        InputDialogBuilder withActions(DialogActions actions);

        /**
         * Sets dialog actions and result handler. "OK" and "YES" actions always check fields validation before close
         * the dialog. Handler is invoked after close event and can be used instead of
         * {@link #withCloseListener(Consumer)}.
         * Example
         * <pre>{@code
         *  dialogs.createInputDialog(this)
         *         .withCaption("Dialog caption")
         *         .withParameter(parameter("nameField").withCaption("Name"))
         *         .withActions(DialogActions.OK_CANCEL, result -> {
         *             switch (result.getCloseActionType()) {
         *                 case OK:
         *                     String name = result.getValue("nameField");
         *                     // do logic
         *                     break;
         *                 case CANCEL:
         *                     // do logic
         *                     break;
         *             }
         *         })
         *         .show();
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
         *                 stringParameter("phoneField").withCaption("Phone"),
         *                 stringParameter("addressField").withCaption("Address"))
         *         .withValidator(context -> {
         *             String phone = context.getValue("phoneField");
         *             String address = context.getValue("addressField");
         *             if (Strings.isNullOrEmpty(phone) && Strings.isNullOrEmpty(address)) {
         *                 return ValidationErrors.of("Phone or Address should be filled");
         *             }
         *             return ValidationErrors.none();
         *         })
         *         .show();
         *  }</pre>
         *
         * @param validator validator
         * @return builder
         */
        InputDialogBuilder withValidator(Function<InputDialog.ValidationContext, ValidationErrors> validator);

        /**
         * Shows the dialog.
         *
         * @return opened input dialog
         */
        InputDialog show();

        /**
         * Builds the input dialog.
         *
         * @return input dialog
         */
        InputDialog build();
    }


    /**
     * Builder of background work dialog.
     */
    interface BackgroundWorkDialogBuilder<T extends Number, V> extends
            HasCaption<BackgroundWorkDialogBuilder<T, V>>,
            HasMessage<BackgroundWorkDialogBuilder<T, V>> {

        /**
         * Determines whether the dialog can be closed.
         * False by default.
         *
         * @param cancelAllowed true if dialog is closeable
         * @return builder
         */
        BackgroundWorkDialogBuilder<T, V> withCancelAllowed(boolean cancelAllowed);

        /**
         * @return true if the dialog can be closed
         */
        boolean isCancelAllowed();

        /**
         * @param total amount of items to be processed by background task,
         * use {@link io.jmix.ui.executor.TaskLifeCycle#publish(Object[])} to notify the dialog about progress
         * completion.
         * @return builder
         */
        BackgroundWorkDialogBuilder<T, V> withTotal(Number total);

        /**
         * @return total
         */
        Number getTotal();

        /**
         * @param percentProgress true if progress should be represented as percentage (rather than as raw number)
         * @return builder
         */
        BackgroundWorkDialogBuilder<T, V> withShowProgressInPercentage(boolean percentProgress);

        /**
         * @return true if progress should is shown in percents
         */
        boolean isShowProgressInPercentage();

        /**
         * Shows the dialog.
         *
         * @return background work dialog
         */
        BackgroundWorkDialog<T, V> show();

        /**
         * Builds the dialog.
         *
         * @return background work dialog
         */
        BackgroundWorkDialog<T, V> build();
    }

    /**
     * Base class for all Dialog Builders.
     *
     * @param <T> return type of fluent API methods
     */
    interface DialogBuilder<T extends DialogBuilder> extends HasCaption<T>, HasSize<T> {
    }

    /**
     * Marker interface for Dialog Builders that have caption.
     *
     * @param <T> return type of fluent API methods
     */
    interface HasCaption<T> {
        /**
         * Sets caption text.
         *
         * @param caption caption text
         * @return builder
         */
        T withCaption(String caption);

        /**
         * @return caption text
         */
        @Nullable
        String getCaption();
    }

    /**
     * Marker interface for Dialog Builders that have message.
     *
     * @param <T> return type of fluent API methods
     */
    interface HasMessage<T> {
        /**
         * Sets message text.
         *
         * @param message message text
         * @return builder
         */
        T withMessage(String message);

        /**
         * @return message text
         */
        String getMessage();
    }

    /**
     * Marker interface for Dialog Builders that have content mode setting.
     *
     * @param <T> return type of fluent API methods
     */
    interface HasContentMode<T> {

        /**
         * Sets content mode for message, e.g. {@link ContentMode#TEXT}, {@link ContentMode#HTML}
         * or {@link ContentMode#PREFORMATTED}.
         *
         * @param contentMode content mode
         * @return builder
         */
        T withContentMode(ContentMode contentMode);

        /**
         * @return message content mode
         */
        ContentMode getContentMode();
    }

    /**
     * Marker interface for Dialog Builders that have size setting.
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
        float getWidth();

        /**
         * @return dialog width unit
         */
        SizeUnit getWidthSizeUnit();

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
        float getHeight();

        /**
         * @return dialog height unit
         */
        SizeUnit getHeightSizeUnit();
    }

    /**
     * Marker interface for Dialog Builders that have stylename setting.
     *
     * @param <T> return type of fluent API methods
     */
    interface HasStyleName<T> {
        /**
         * Sets custom CSS style name for dialog.
         *
         * @param styleName style name
         * @return builder
         */
        T withStyleName(String styleName);

        /**
         * @return custom style name
         */
        String getStyleName();
    }

    /**
     * Marker interface for Dialog Builders that have modal setting.
     *
     * @param <T> return type of fluent API methods
     */
    interface HasModal<T> {
        /**
         * @return true if window is modal
         */
        boolean isModal();

        /**
         * Sets dialog modality. When a modal window is open, components outside that window cannot be accessed.
         *
         * @param modal modal flag
         * @return builder
         */
        T withModal(boolean modal);

        /**
         * Enables modal mode for dialog.
         *
         * @return builder
         */
        T modal();
    }

    /**
     * Marker interface for Dialog Builders that have window mode for dialog window.
     *
     * @param <T> return type of fluent API methods
     */
    interface HasWindowMode<T> {
        /**
         * Sets the mode of the dialog window.
         *
         * @param windowMode the mode of the dialog window
         * @return builder
         */
        T withWindowMode(WindowMode windowMode);

        /**
         * @return the mode of the dialog window
         */
        WindowMode getWindowMode();
    }

    /**
     * Marker interface for Dialog Builders that have html sanitizer for dialog content.
     *
     * @param <T> return type of fluent API methods
     */
    interface HasHtmlSanitizer<T> {
        /**
         * Sets whether html sanitizer is enabled or not for dialog content.
         *
         * @param htmlSanitizerEnabled specifies whether html sanitizer is enabled
         * @return builder
         */
        T withHtmlSanitizer(boolean htmlSanitizerEnabled);

        /**
         * @return html sanitizer is enabled for dialog content
         */
        boolean isHtmlSanitizerEnabled();
    }
}
