/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowui.app.propertyfilter.dateinterval.component;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.SelectVariant;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.jmix.core.Messages;
import io.jmix.core.annotation.Internal;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.app.propertyfilter.dateinterval.model.BaseDateInterval;
import io.jmix.flowui.app.propertyfilter.dateinterval.model.DateInterval;
import io.jmix.flowui.component.SupportsStatusChangeHandler;
import io.jmix.flowui.component.SupportsValidation;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.component.textfield.JmixIntegerField;
import io.jmix.flowui.component.validation.PositiveOrZeroValidator;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.kit.component.button.JmixButton;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

@Internal
public abstract class AbstractIntervalField extends CustomField<DateInterval>
        implements SupportsValidation<DateInterval>, ApplicationContextAware, InitializingBean {

    protected BaseDateInterval.Type type;

    protected HorizontalLayout root;
    protected HorizontalLayout fieldBox;
    protected JmixIntegerField numberField;
    protected JmixSelect<DateInterval.TimeUnit> timeUnitSelect;
    protected JmixCheckbox includingCurrentCheckbox;
    protected JmixButton includingCurrentHelperBtn;

    protected ApplicationContext applicationContext;

    protected UiComponents uiComponents;
    protected Messages messages;
    protected Dialogs dialogs;

    protected String requiredMessage;

    protected AbstractIntervalField(BaseDateInterval.Type type) {
        this.type = type;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        autowireDependencies();
        initComponent();
    }

    protected void autowireDependencies() {
        uiComponents = applicationContext.getBean(UiComponents.class);
        messages = applicationContext.getBean(Messages.class);
        dialogs = applicationContext.getBean(Dialogs.class);
    }

    protected void initComponent() {
        initRoot();
        initNumberField();
        initTimeUnitSelect();
        initIncludingCurrentLayout();
        updateInvalidState();
    }

    protected void initRoot() {
        root = createLayout();
        root.addClassName(LumoUtility.FlexWrap.WRAP);

        fieldBox = createLayout();
        root.add(fieldBox);
        root.setFlexGrow(1D, fieldBox);

        add(root);
    }

    protected HorizontalLayout createLayout() {
        HorizontalLayout layout = uiComponents.create(HorizontalLayout.class);

        layout.setPadding(false);
        layout.addClassNames(LumoUtility.AlignItems.BASELINE);
        return layout;
    }

    protected void initNumberField() {
        numberField = uiComponents.create(JmixIntegerField.class);

        numberField.setStatusChangeHandler(this::onValidationStatusChange);
        numberField.addValidationStatusChangeListener(__ -> updateInvalidState());
        numberField.addClientValidatedEventListener(__ -> updateInvalidState());
        numberField.setRequiredMessage(
                messages.getMessage(getClass(), "NextLastIntervalField.numberField.requiredMessage"));
        numberField.setRequired(true);

        numberField.addThemeVariants(TextFieldVariant.LUMO_ALIGN_CENTER);
        numberField.setWidth("5em");

        //noinspection unchecked
        numberField.addValidator(applicationContext.getBean(PositiveOrZeroValidator.class));

        fieldBox.add(numberField);
    }

    protected void initTimeUnitSelect() {
        //noinspection unchecked
        timeUnitSelect = uiComponents.create(JmixSelect.class);

        timeUnitSelect.setStatusChangeHandler(this::onValidationStatusChange);
        timeUnitSelect.addValidationStatusChangeListener(__ -> updateInvalidState());
        timeUnitSelect.addClientValidatedEventListener(__ -> updateInvalidState());
        timeUnitSelect.setRequiredMessage(
                messages.getMessage(getClass(), "NextLastIntervalField.timeUnitSelect.requiredMessage"));
        timeUnitSelect.setRequired(true);
        // WA: min-width (in a flexbox) defaults not to 0 but to the element's intrinsic width,
        // which in this case is the default width
        timeUnitSelect.setMinWidth("1px");

        timeUnitSelect.addThemeVariants(SelectVariant.LUMO_ALIGN_CENTER);

        fieldBox.add(timeUnitSelect);
        fieldBox.setFlexGrow(1D, timeUnitSelect);
    }

    public void setTimeUnitItemsMap(Map<DateInterval.TimeUnit, String> localizationMap) {
        ComponentUtils.setItemsMap(timeUnitSelect, localizationMap);
    }

    protected void initIncludingCurrentLayout() {
        HorizontalLayout includingCurrentBox = uiComponents.create(HorizontalLayout.class);
        includingCurrentBox.setPadding(false);
        includingCurrentBox.setSpacing(false);
        includingCurrentBox.addClassNames(LumoUtility.AlignItems.BASELINE);

        includingCurrentCheckbox = uiComponents.create(JmixCheckbox.class);
        includingCurrentCheckbox.setLabel(messages.getMessage(getClass(),
                "NextLastIntervalField.includingCurrentCheckbox.label"));

        includingCurrentBox.add(includingCurrentCheckbox);
        initIncludingCurrentHelperBtn(includingCurrentBox);

        root.add(includingCurrentBox);
        root.setFlexShrink(0D, includingCurrentBox);
    }

    protected void initIncludingCurrentHelperBtn(HorizontalLayout includingCurrentBox) {
        includingCurrentHelperBtn = uiComponents.create(JmixButton.class);
        includingCurrentHelperBtn.setIcon(VaadinIcon.QUESTION_CIRCLE.create());
        includingCurrentHelperBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);

        includingCurrentHelperBtn.addClickListener(this::onIncludingCurrentHelperBtnClick);

        includingCurrentBox.add(includingCurrentHelperBtn);
    }

    protected void onIncludingCurrentHelperBtnClick(ClickEvent<Button> event) {
        String header = messages.getMessage(getClass(),
                "NextLastIntervalField.includingCurrentHelperBtn.messageDialog.header");
        Html content = new Html(messages.getMessage(getClass(),
                "NextLastIntervalField.includingCurrentHelperBtn.messageDialog.content"));

        dialogs.createMessageDialog()
                .withHeader(header)
                .withContent(content)
                .withWidth("37.5em")
                .open();
    }

    @Override
    protected DateInterval generateModelValue() {
        return new DateInterval(
                type,
                numberField.getValue(),
                timeUnitSelect.getValue(),
                includingCurrentCheckbox.getValue()
        );
    }

    @Override
    protected void setPresentationValue(DateInterval newPresentationValue) {
        type = newPresentationValue.getType();
        numberField.setValue(newPresentationValue.getNumber());
        timeUnitSelect.setValue(newPresentationValue.getTimeUnit());
        includingCurrentCheckbox.setValue(Boolean.TRUE.equals(newPresentationValue.getIncludingCurrent()));
    }

    @Override
    public Registration addValidator(Validator<? super DateInterval> validator) {
        throw new UnsupportedOperationException("%s has a predefined validators".formatted(getClass().getSimpleName()));
    }

    protected void onValidationStatusChange(SupportsStatusChangeHandler.StatusContext<? extends Component> context) {
        setErrorMessage(context.getDescription());
    }

    protected void updateInvalidState() {
        setInvalid(numberField.isInvalid() || timeUnitSelect.isInvalid());
    }

    @Override
    public void executeValidators() throws ValidationException {
        numberField.executeValidators();
        timeUnitSelect.executeValidators();
    }

    @Override
    public void setInvalid(boolean invalid) {
        super.setInvalid(invalid);
    }
}
