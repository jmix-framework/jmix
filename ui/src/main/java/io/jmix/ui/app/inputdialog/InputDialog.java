/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.app.inputdialog;

import com.google.common.collect.ImmutableList;
import io.jmix.core.DateTimeTransformations;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.datatype.impl.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.Actions;
import io.jmix.ui.Dialogs;
import io.jmix.ui.UiComponents;
import io.jmix.ui.UiProperties;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.DialogAction;
import io.jmix.ui.component.*;
import io.jmix.ui.component.impl.EntityFieldCreationSupport;
import io.jmix.ui.component.inputdialog.InputDialogAction;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.model.DataComponents;
import io.jmix.ui.screen.*;
import io.jmix.ui.theme.ThemeConstants;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

@UiDescriptor("inputdialog.xml")
@UiController("inputDialog")
public class InputDialog extends Screen {

    /**
     * A {@link CloseAction} used when the user clicks "OK" button and fields validation is successful.
     */
    public static final CloseAction INPUT_DIALOG_OK_ACTION = new StandardCloseAction("inputDialogOk");

    /**
     * A {@link CloseAction} used when the user clicks "CANCEL" button.
     */
    public static final CloseAction INPUT_DIALOG_CANCEL_ACTION = new StandardCloseAction("inputDialogCancel");

    /**
     * A {@link CloseAction} used when the user clicks "YES" button and fields validation is successful.
     */
    public static final CloseAction INPUT_DIALOG_YES_ACTION = new StandardCloseAction("inputDialogYes");

    /**
     * A {@link CloseAction} used when the user clicks "NO" button.
     */
    public static final CloseAction INPUT_DIALOG_NO_ACTION = new StandardCloseAction("inputDialogNo");

    protected static final List<Class<?>> dateTimeDatatypes = ImmutableList.of(DateTimeDatatype.class,
            LocalDateTimeDatatype.class, OffsetDateTimeDatatype.class);

    protected static final List<Class<?>> dateDatatypes = ImmutableList.of(DateDatatype.class,
            LocalDateDatatype.class);

    protected static final List<Class<?>> timeDatatypes = ImmutableList.of(TimeDatatype.class, LocalTimeDatatype.class,
            OffsetTimeDatatype.class);

    @Autowired
    protected UiComponents uiComponents;

    @Autowired
    protected DatatypeRegistry datatypeRegistry;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected Actions actions;

    @Autowired
    protected Messages messages;

    @Autowired
    protected Icons icons;

    @Autowired
    protected ScreenValidation screenValidation;

    @Autowired
    protected ThemeConstants theme;

    @Autowired
    protected UiProperties uiProperties;

    @Autowired
    protected EntityFieldCreationSupport entityFieldCreationSupport;

    @Autowired
    protected DataComponents dataComponents;

    @Autowired
    protected Form form;

    @Autowired
    protected HBoxLayout actionsLayout;

    @Autowired
    protected CurrentAuthentication currentAuthentication;

    @Autowired
    protected DateTimeTransformations dateTimeTransformations;

    protected List<InputParameter> parameters = new ArrayList<>(2);
    protected List<Action> actionsList = new ArrayList<>(2);

    protected DialogActions dialogActions = DialogActions.OK_CANCEL;
    protected List<String> fieldIds;

    protected Consumer<InputDialogResult> resultHandler;
    protected Function<ValidationContext, ValidationErrors> validator;

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        initParameters();
        if (actionsList.isEmpty()) {
            initDialogActions();
        } else {
            initActions(actionsList);
        }
    }

    @Subscribe
    protected void onAfterClose(AfterCloseEvent event) {
        InputDialogCloseEvent closeEvent =
                new InputDialogCloseEvent(event.getSource(), getValues(), event.getCloseAction());
        getEventHub().publish(InputDialogCloseEvent.class, closeEvent);
    }

    /**
     * Returns value from parameter by its id.
     *
     * @param parameterId parameter id
     * @return parameter value
     * @throws IllegalArgumentException exception if wrong parameter id is sent
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue(String parameterId) {
        Component component = form.getComponentNN(parameterId);
        if (component instanceof Field) {
            return (T) ((Field) component).getValue();
        }

        throw new IllegalArgumentException("InputDialog doesn't contains parameter with id: " + parameterId);
    }

    /**
     * @return dialog window in which you can set dialog properties (e.g. modal, resizable, etc)
     */
    public DialogWindow getDialogWindow() {
        return (DialogWindow) getWindow();
    }

    /**
     * Returns values from parameters. String - parameter id, Object - parameter value.
     *
     * @return values
     */
    public Map<String, Object> getValues() {
        ParamsMap paramsMap = ParamsMap.of();

        for (String id : fieldIds) {
            Component component = form.getComponentNN(id);
            paramsMap.pair(id, ((Field) component).getValue());
        }

        return paramsMap.create();
    }

    /**
     * Add input parameter to the dialog. Input parameter will be represented as a field.
     *
     * @param parameter input parameter that will be added to the dialog
     */
    public void setParameter(InputParameter parameter) {
        parameters.add(parameter);
    }

    /**
     * Sets input parameters.
     *
     * @param parameters input parameters
     */
    public void setParameters(InputParameter... parameters) {
        this.parameters.addAll(Arrays.asList(parameters));
    }

    /**
     * @return input parameters from dialog
     */
    public List<InputParameter> getParameters() {
        return parameters;
    }

    /**
     * Adds a close listener to the input dialog.
     *
     * @param listener close listener to add
     * @return subscription
     */
    public Subscription addCloseListener(Consumer<InputDialogCloseEvent> listener) {
        return getEventHub().subscribe(InputDialogCloseEvent.class, listener);
    }

    /**
     * Sets dialog actions. If there is no actions are set input dialog will use {@link DialogActions#OK_CANCEL}.
     *
     * @param actions actions
     * @see InputDialogAction
     */
    public void setActions(InputDialogAction... actions) {
        this.actionsList.addAll(Arrays.asList(actions));
    }

    /**
     * @return actions list
     */
    public List<Action> getActions() {
        return actionsList;
    }

    /**
     * Sets predefined dialog actions. By default if there is no actions are input dialog will use
     * {@link DialogActions#OK_CANCEL}.
     *
     * @param actions actions
     */
    public void setDialogActions(DialogActions actions) {
        this.dialogActions = actions;
    }

    /**
     * Returns predefined dialog actions. {@link DialogActions#OK_CANCEL} by default.
     *
     * @return dialog actions
     */
    public DialogActions getDialogActions() {
        return dialogActions;
    }

    /**
     * Sets handler for dialog actions (e.g. OK, CANCEL, etc) that are used in the dialog. Handler is invoked after
     * close event and can be used instead of {@link #addCloseListener(Consumer)}.
     * <p>
     * Note, it is worked only with {@link #setDialogActions(DialogActions)}. Custom actions are not handled.
     *
     * @param resultHandler result handler
     */
    public void setResultHandler(Consumer<InputDialogResult> resultHandler) {
        this.resultHandler = resultHandler;
    }

    /**
     * @return result handler
     */
    @Nullable
    public Consumer<InputDialogResult> getResultHandler() {
        return resultHandler;
    }

    /**
     * Validates form components and conditions from custom validation supplier and show errors.
     *
     * @return true if validation is successful
     */
    public boolean isValid() {
        ValidationErrors validationErrors = screenValidation.validateUiComponents(form);
        if (validator != null) {
            ValidationErrors errors = validator.apply(new ValidationContext(getValues(), this));
            validationErrors.addAll(errors == null ? ValidationErrors.none() : errors);
        }

        if (!validationErrors.isEmpty()) {
            screenValidation.showValidationErrors(this, validationErrors);
            return false;
        }
        return true;
    }

    /**
     * Sets additional handler for field validation. It takes values map and must return {@link ValidationErrors}
     * instance. Returned validation errors will be shown with another errors from fields.
     *
     * @param validator validator
     */
    public void setValidator(Function<ValidationContext, ValidationErrors> validator) {
        this.validator = validator;
    }

    /**
     * @return additional field validator
     */
    public Function<ValidationContext, ValidationErrors> getValidator() {
        return validator;
    }

    @SuppressWarnings("unchecked")
    protected void initParameters() {
        fieldIds = new ArrayList<>(parameters.size());

        for (InputParameter parameter : parameters) {
            if (fieldIds.contains(parameter.getId())) {
                throw new IllegalArgumentException(
                        "InputDialog cannot contain parameters with the same id: '" + parameter.getId() + "'");
            }

            Field field;
            if (parameter.getField() != null) {
                field = parameter.getField().get();
            } else {
                field = createField(parameter);
                field.setCaption(parameter.getCaption());
                field.setValue(parameter.getDefaultValue());
                field.setRequired(parameter.isRequired());
                field.setRequiredMessage(parameter.getRequiredMessage());
            }
            field.setId(parameter.getId());

            fieldIds.add(field.getId());
            form.add(field);
        }

        form.focusFirstComponent();
    }

    @SuppressWarnings("unchecked")
    protected Field createField(InputParameter parameter) {
        if (parameter.getEntityClass() != null) {
            return createEntityField(parameter);
        } else if (parameter.getEnumClass() != null) {
            return createEnumField(parameter);
        }

        Datatype datatype = null;
        if (parameter.getDatatypeJavaClass() != null) {
            datatype = datatypeRegistry.find(parameter.getDatatypeJavaClass());
        } else if (parameter.getDatatype() != null) {
            datatype = parameter.getDatatype();
        }

        if (datatype == null) {
            datatype = datatypeRegistry.get(String.class);
        }

        if (datatype instanceof NumberDatatype
                || datatype instanceof StringDatatype) {
            TextField field = uiComponents.create(TextField.NAME);
            field.setWidthFull();
            field.setDatatype(datatype);
            return field;
        } else if (isDateBasedDatatype(datatype)) {
            DateField dateField = uiComponents.create(DateField.NAME);
            dateField.setDatatype(datatype);
            dateField.setResolution(DateField.Resolution.DAY);
            return dateField;
        } else if (isDateTimeBasedDatatype(datatype)) {
            DateField dateField = uiComponents.create(DateField.NAME);
            dateField.setDatatype(datatype);
            dateField.setResolution(DateField.Resolution.MIN);
            if (dateTimeTransformations.isDateTypeSupportsTimeZones(datatype.getJavaClass())) {
                dateField.setTimeZone(parameter.isUseUserTimeZone()
                        ? currentAuthentication.getTimeZone()
                        : parameter.getTimeZone());
            }
            return dateField;
        } else if (isTimeBasedDatatype(datatype)) {
            TimeField timeField = uiComponents.create(TimeField.NAME);
            timeField.setDatatype(datatype);
            return timeField;
        } else if (datatype instanceof BooleanDatatype) {
            return uiComponents.create(CheckBox.NAME);
        } else if (datatype instanceof FileRefDatatype) {
            FileStorageUploadField fileUploadField = uiComponents.create(FileStorageUploadField.class);
            fileUploadField.setShowFileName(true);
            fileUploadField.setShowClearButton(true);
            return fileUploadField;
        } else if (datatype instanceof ByteArrayDatatype) {
            FileUploadField fileUploadField = uiComponents.create(FileUploadField.class);
            fileUploadField.setShowFileName(true);
            fileUploadField.setShowClearButton(true);
            return fileUploadField;
        } else {
            throw new IllegalArgumentException("InputDialog doesn't support datatype: " + datatype.getClass());
        }
    }

    protected Field createEntityField(InputParameter parameter) {
        MetaClass metaClass = metadata.getClass(parameter.getEntityClass());
        Field field = entityFieldCreationSupport.createEntityField(metaClass, null);
        field.setWidthFull();
        return field;
    }

    @SuppressWarnings("unchecked")
    protected Field createEnumField(InputParameter parameter) {
        ComboBox comboBox = uiComponents.create(ComboBox.NAME);
        comboBox.setOptionsEnum(parameter.getEnumClass());
        comboBox.setWidthFull();
        return comboBox;
    }

    protected void initActions(List<Action> actions) {
        for (Action action : actions) {
            Button button = uiComponents.create(Button.NAME);
            button.setAction(action);

            if (action instanceof DialogAction) {
                DialogAction.Type type = ((DialogAction) action).getType();
                button.setCaption(messages.getMessage(type.getMsgKey()));

                String iconPath = icons.get(type.getIconKey());
                button.setIcon(iconPath);
            }

            actionsLayout.add(button);
        }
    }

    protected void initDialogActions() {
        List<Action> actions = new ArrayList<>(2);
        switch (dialogActions) {
            case OK:
                actions.add(createDialogAction(DialogAction.Type.OK, INPUT_DIALOG_OK_ACTION));
                break;
            case YES_NO:
                actions.add(createDialogAction(DialogAction.Type.YES, INPUT_DIALOG_YES_ACTION));
                actions.add(createDialogAction(DialogAction.Type.NO, INPUT_DIALOG_NO_ACTION));
                break;
            case OK_CANCEL:
                actions.add(createDialogAction(DialogAction.Type.OK, INPUT_DIALOG_OK_ACTION));
                actions.add(createDialogAction(DialogAction.Type.CANCEL, INPUT_DIALOG_CANCEL_ACTION));
                break;
            case YES_NO_CANCEL:
                actions.add(createDialogAction(DialogAction.Type.YES, INPUT_DIALOG_YES_ACTION));
                actions.add(createDialogAction(DialogAction.Type.NO, INPUT_DIALOG_NO_ACTION));
                actions.add(createDialogAction(DialogAction.Type.CANCEL, INPUT_DIALOG_CANCEL_ACTION));
                break;
        }
        initActions(actions);
    }

    protected DialogAction createDialogAction(DialogAction.Type type, CloseAction closeAction) {
        DialogAction dialogAction = new DialogAction(type);
        if (type == DialogAction.Type.OK || type == DialogAction.Type.YES) {
            dialogAction.withHandler(event -> {
                if (isValid()) {
                    fireCloseAndResultEvents(closeAction);
                }
            });
        } else {
            dialogAction.withHandler(event -> fireCloseAndResultEvents(closeAction));
        }
        return dialogAction;
    }

    protected void fireCloseAndResultEvents(CloseAction closeAction) {
        close(closeAction);

        if (resultHandler != null) {
            resultHandler.accept(new InputDialogResult(getValues(), closeAction));
        }
    }

    protected boolean isDateBasedDatatype(Datatype datatype) {
        return dateDatatypes.stream()
                .anyMatch(dateDatatype -> dateDatatype.isAssignableFrom(datatype.getClass()));
    }

    protected boolean isTimeBasedDatatype(Datatype datatype) {
        return timeDatatypes.stream()
                .anyMatch(dateDatatype -> dateDatatype.isAssignableFrom(datatype.getClass()));
    }

    protected boolean isDateTimeBasedDatatype(Datatype datatype) {
        return dateTimeDatatypes.stream()
                .anyMatch(dateDatatype -> dateDatatype.isAssignableFrom(datatype.getClass()));
    }

    /**
     * Event sent to a listener added using {@code withCloseListener()} method of the input dialog builder.
     */
    public static class InputDialogCloseEvent extends EventObject {
        protected CloseAction closeAction;
        protected Map<String, Object> values;

        public InputDialogCloseEvent(Screen source, Map<String, Object> values, CloseAction closeAction) {
            super(source);
            this.values = values;
            this.closeAction = closeAction;
        }

        @Override
        public InputDialog getSource() {
            return (InputDialog) super.getSource();
        }

        /**
         * @return close action
         */
        public CloseAction getCloseAction() {
            return closeAction;
        }

        /**
         * Checks that the dialog was closed with the given {@code outcome}.
         */
        public boolean closedWith(DialogOutcome outcome) {
            return outcome.getCloseAction().equals(closeAction);
        }

        /**
         * Returns values from parameters. Key - parameter id, Value - parameter value.
         *
         * @return values
         */
        public Map<String, Object> getValues() {
            return values;
        }

        /**
         * @param parameterId parameter id
         * @return parameter value
         */
        @SuppressWarnings("unchecked")
        @Nullable
        public <T> T getValue(String parameterId) {
            return (T) values.get(parameterId);
        }
    }

    /**
     * Describes result of handler that can be used with {@link DialogActions} in the input dialog.
     *
     * @see Dialogs.InputDialogBuilder#withActions(DialogActions, Consumer)
     */
    public static class InputDialogResult {

        public enum ActionType {
            OK, CANCEL, YES, NO
        }

        protected Map<String, Object> values;
        protected CloseAction closeAction;

        public InputDialogResult(Map<String, Object> values, CloseAction closeAction) {
            this.values = values;
            this.closeAction = closeAction;
        }

        /**
         * Returns values from parameters. String - parameter id, Object - parameter value.
         *
         * @return values
         */
        public Map<String, Object> getValues() {
            return values;
        }

        /**
         * @param parameterId parameter id
         * @return parameter value
         */
        @SuppressWarnings("unchecked")
        @Nullable
        public <T> T getValue(String parameterId) {
            return (T) values.get(parameterId);
        }

        /**
         * @return close action
         * @see #INPUT_DIALOG_OK_ACTION
         * @see #INPUT_DIALOG_CANCEL_ACTION
         * @see #INPUT_DIALOG_YES_ACTION
         * @see #INPUT_DIALOG_NO_ACTION
         */
        public CloseAction getCloseAction() {
            return closeAction;
        }

        /**
         * Returns result action which was clicked in the dialog, e.g. OK, CANCEL, etc.
         *
         * @return dialog result
         */
        public ActionType getCloseActionType() {
            if (closeAction.equals(INPUT_DIALOG_OK_ACTION)) {
                return ActionType.OK;
            } else if (closeAction.equals(INPUT_DIALOG_NO_ACTION)) {
                return ActionType.NO;
            } else if (closeAction.equals(INPUT_DIALOG_YES_ACTION)) {
                return ActionType.YES;
            } else {
                return ActionType.CANCEL;
            }
        }

        /**
         * Checks that the dialog was closed with the given {@code outcome}.
         */
        public boolean closedWith(DialogOutcome outcome) {
            return outcome.getCloseAction().equals(closeAction);
        }
    }

    /**
     * Describes input dialog validation context.
     */
    public static class ValidationContext {

        protected Map<String, Object> values;
        protected InputDialog source;

        public ValidationContext(Map<String, Object> values, InputDialog source) {
            this.values = values;
            this.source = source;
        }

        /**
         * Returns values from parameters. String - parameter id, Object - parameter value.
         *
         * @return values
         */
        public Map<String, Object> getValues() {
            return values;
        }

        /**
         * @param parameterId parameter id
         * @return parameter value
         */
        @SuppressWarnings("unchecked")
        @Nullable
        public <T> T getValue(String parameterId) {
            return (T) values.get(parameterId);
        }

        /**
         * @return input dialog
         */
        public InputDialog getSource() {
            return source;
        }
    }
}
