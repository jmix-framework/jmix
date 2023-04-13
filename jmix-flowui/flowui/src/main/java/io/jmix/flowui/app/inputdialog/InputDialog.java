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

package io.jmix.flowui.app.inputdialog;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.flowui.Actions;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.Dialogs.InputDialogBuilder.LabelsPosition;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.action.inputdialog.InputDialogAction;
import io.jmix.flowui.component.HasRequired;
import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.component.UiComponentsGenerator;
import io.jmix.flowui.component.factory.InputDialogGenerationContext;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

@ViewController("inputDialog")
public class InputDialog extends StandardView {

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

    protected List<InputParameter> parameters = new ArrayList<>(2);
    protected List<Action> actionsList = new ArrayList<>(2);

    @Autowired
    protected ViewValidation viewValidation;

    @Autowired
    protected UiComponents uiComponents;

    @Autowired
    protected Messages messages;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected DatatypeRegistry datatypeRegistry;

    @Autowired
    protected Actions actions;

    @Autowired
    protected UiComponentsGenerator uiComponentsGenerator;

    protected FormLayout formLayout;
    protected HorizontalLayout actionBox;

    protected List<ResponsiveStep> responsiveSteps = Collections.emptyList();
    protected LabelsPosition labelsPosition = LabelsPosition.ASIDE;

    protected DialogActions dialogActions = DialogActions.OK_CANCEL;
    protected Map<String, Component> idToContentMap;

    protected Consumer<InputDialogResult> resultHandler;
    protected Function<ValidationContext, ValidationErrors> validator;

    protected String pageTitle;

    @Subscribe
    public void onInit(InitEvent event) {
        initFormLayout();
        initActionBox();
    }

    protected void initFormLayout() {
        formLayout = uiComponents.create(FormLayout.class);

        formLayout.setWidthFull();

        getContent().add(formLayout);
    }

    protected void initActionBox() {
        actionBox = uiComponents.create(HorizontalLayout.class);

        actionBox.setWidthFull();
        actionBox.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        getContent().add(actionBox);
    }

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        initParameters();
        initResponsiveSteps();
        if (actionsList.isEmpty()) {
            initDialogActions();
        } else {
            initActions(actionsList);
        }
    }

    @Subscribe
    protected void onAfterClose(AfterCloseEvent event) {
        InputDialogCloseEvent closeEvent =
                new InputDialogCloseEvent((InputDialog) event.getSource(), getValues(), event.getCloseAction());
        getEventBus().fireEvent(closeEvent);
    }

    /**
     * Returns value from parameter by its id.
     *
     * @param parameterId parameter id
     * @return parameter value
     * @throws IllegalArgumentException exception if wrong parameter id is sent
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Nullable
    public <T> T getValue(String parameterId) {
        Component component = getFormLayoutComponentById(parameterId);

        if (component instanceof SupportsTypedValue) {
            return (T) ((SupportsTypedValue) component).getTypedValue();
        }

        if (component instanceof HasValue<?, ?>) {
            return (T) ((HasValue) component).getValue();
        }

        throw new IllegalArgumentException("InputDialog doesn't contains parameter with id: " + parameterId);
    }

    /**
     * Returns values from parameters. String - parameter id, Object - parameter value.
     *
     * @return values
     */
    public Map<String, Object> getValues() {
        return idToContentMap.entrySet().stream()
                .collect(HashMap::new, this::idToValueMapper, HashMap::putAll);
    }

    private void idToValueMapper(HashMap<String, Object> map, Map.Entry<String, Component> entry) {
        map.put(entry.getKey(), getValue(entry.getKey()));
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
     * Sets responsive steps. Responsive steps used in describing the responsive layouting behavior of a
     * {@link FormLayout}.
     *
     * @param responsiveSteps - responsive steps
     */
    public void setResponsiveSteps(List<ResponsiveStep> responsiveSteps) {
        this.responsiveSteps = responsiveSteps;
    }

    /**
     * @return responsive steps of FormLayout
     */
    public List<ResponsiveStep> getResponsiveSteps() {
        return responsiveSteps;
    }

    /**
     * Sets labels position for default responsiveSteps.
     *
     * @param labelsPosition position of labels
     */
    public void setLabelsPosition(LabelsPosition labelsPosition) {
        this.labelsPosition = labelsPosition;
    }

    /**
     * @return labels position for default responsiveSteps
     */
    public LabelsPosition getLabelsPosition() {
        return labelsPosition;
    }

    /**
     * Adds a close listener to the input dialog.
     *
     * @param listener close listener to add
     * @return registration
     */
    public Registration addCloseListener(ComponentEventListener<InputDialogCloseEvent> listener) {
        return getEventBus().addListener(InputDialogCloseEvent.class, listener);
    }

    /**
     * Sets dialog actions. If there is no actions are set input dialog will use {@link DialogActions#OK_CANCEL}.
     *
     * @param actions actions
     * @see InputDialogAction
     */
    public void setActions(InputDialogAction... actions) {
        this.actionsList.addAll(List.of(actions));
    }

    /**
     * @return actions list
     */
    public List<Action> getActions() {
        return actionsList;
    }

    /**
     * Sets predefined dialog actions. By default, if there is no actions are input dialog will use
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
     * close event and can be used instead of {@link #addCloseListener(ComponentEventListener)}.
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
        ValidationErrors validationErrors = viewValidation.validateUiComponents(formLayout);
        if (validator != null) {
            ValidationErrors errors = validator.apply(new ValidationContext(getValues(), this));
            validationErrors.addAll(errors == null ? ValidationErrors.none() : errors);
        }

        if (!validationErrors.isEmpty()) {
            viewValidation.showValidationErrors(validationErrors);
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

    protected void initParameters() {
        idToContentMap = new HashMap<>();

        for (InputParameter parameter : parameters) {
            if (idToContentMap.containsKey(parameter.getId())) {
                throw new IllegalArgumentException(
                        "InputDialog cannot contain parameters with the same id: '" + parameter.getId() + "'");
            }

            Component field;
            if (parameter.getField() != null) {
                field = parameter.getField().get();
            } else {
                field = createField(parameter);
                fillFieldAttributes(field, parameter);
            }

            field.setId(parameter.getId());

            idToContentMap.put(parameter.getId(), field);
            formLayout.addFormItem(field, parameter.getLabel());
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void fillFieldAttributes(Component field, InputParameter parameter) {
        if (field instanceof SupportsTypedValue) {
            ((SupportsTypedValue) field).setTypedValue(parameter.getDefaultValue());
        } else if (field instanceof HasValue) {
            ((HasValue) field).setValue(parameter.getDefaultValue());
        }

        if (field instanceof HasRequired) {
            ((HasRequired) field).setRequired(parameter.isRequired());
            ((HasRequired) field).setRequiredMessage(parameter.getRequiredMessage());
        }
    }

    @Nullable
    protected Component getFormLayoutComponentById(String parameterId) {
        return idToContentMap.get(parameterId);
    }

    @SuppressWarnings("DataFlowIssue")
    protected Component createField(InputParameter parameter) {
        InputDialogGenerationContext context = new InputDialogGenerationContext(null, null, parameter);
        context.setTargetClass(this.getClass());

        return Objects.requireNonNull(uiComponentsGenerator.generate(context));
    }

    protected void initActions(List<Action> actions) {
        for (Action action : actions) {
            JmixButton button = uiComponents.create(JmixButton.class);
            button.setAction(action);

            if (action instanceof DialogAction) {
                DialogAction.Type type = ((DialogAction) action).getType();
                button.setText(messages.getMessage(type.getMsgKey()));
                button.setIcon(type.getVaadinIcon().create());
            }

            actionBox.add(button);
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

    protected void initResponsiveSteps() {
        if (responsiveSteps.isEmpty()) {
            formLayout.setResponsiveSteps(
                    new ResponsiveStep("0", 1, labelsPositionFromModel(labelsPosition)),
                    new ResponsiveStep("40em", 2, labelsPositionFromModel(labelsPosition)),
                    new ResponsiveStep("60em", 3, labelsPositionFromModel(labelsPosition))
            );
        } else {
            formLayout.setResponsiveSteps(responsiveSteps);
        }
    }

    protected DialogAction createDialogAction(DialogAction.Type type, CloseAction closeAction) {
        DialogAction dialogAction = new DialogAction(type);
        if (type == DialogAction.Type.OK || type == DialogAction.Type.YES) {
            dialogAction.withVariant(ActionVariant.PRIMARY)
                    .withHandler(event -> {
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

    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    @Override
    public String getPageTitle() {
        return StringUtils.isNotEmpty(pageTitle)
                ? pageTitle
                : super.getPageTitle();
    }

    public FormLayout getFormLayout() {
        return formLayout;
    }

    public static class InputDialogCloseEvent extends ComponentEvent<InputDialog> {
        protected CloseAction closeAction;
        protected Map<String, Object> values;

        public InputDialogCloseEvent(InputDialog source, Map<String, Object> values, CloseAction closeAction) {
            super(source, false);
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
     * Describes result of handler that can be used with {@link DialogAction} in the input dialog.
     *
     * @see Dialogs.InputDialogBuilder#withActions(DialogActions, Consumer)
     */
    public static class InputDialogResult {

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
         */
        public CloseAction getCloseAction() {
            return closeAction;
        }

        /**
         * Returns result action which was clicked in the dialog, e.g. OK, CANCEL, etc.
         *
         * @return dialog result
         */
        public DialogAction.Type getCloseActionType() {
            if (closeAction.equals(INPUT_DIALOG_OK_ACTION)) {
                return DialogAction.Type.OK;
            } else if (closeAction.equals(INPUT_DIALOG_NO_ACTION)) {
                return DialogAction.Type.NO;
            } else if (closeAction.equals(INPUT_DIALOG_YES_ACTION)) {
                return DialogAction.Type.YES;
            } else {
                return DialogAction.Type.CANCEL;
            }
        }

        /**
         * Checks that the dialog was closed with the given {@code outcome}.
         */
        public boolean closedWith(StandardOutcome outcome) {
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

    protected ResponsiveStep.LabelsPosition labelsPositionFromModel(LabelsPosition labelsPosition) {
        if (labelsPosition == LabelsPosition.TOP) {
            return ResponsiveStep.LabelsPosition.TOP;
        }

        return ResponsiveStep.LabelsPosition.ASIDE;
    }
}
