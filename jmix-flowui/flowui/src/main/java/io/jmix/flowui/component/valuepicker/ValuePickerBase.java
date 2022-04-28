package io.jmix.flowui.component.valuepicker;

import com.google.common.base.Strings;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.dom.PropertyChangeEvent;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.binder.ActionBinders;
import io.jmix.flowui.action.valuepicker.PickerAction;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.component.*;
import io.jmix.flowui.component.delegate.AbstractFieldDelegate;
import io.jmix.flowui.component.delegate.FieldDelegate;
import io.jmix.flowui.component.delegate.impl.BaseHasActionsDelegate;
import io.jmix.flowui.component.formatter.Formatter;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.kit.action.Action;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Strings.nullToEmpty;
import static com.vaadin.flow.data.value.ValueChangeMode.eventForMode;

@Tag("jmix-value-picker")
@JsModule("./src/value-picker/jmix-value-picker.js")
public abstract class ValuePickerBase<C extends ValuePickerBase<C, V>, V>
        extends AbstractField<C, V>
        implements PickerComponent<V>, SupportsValidation<V>, SupportsFormatter<V>,
        HasLabel, HasHelper, HasStyle, HasTheme, HasSize, HasRequired, HasPlaceholder,
        HasTitle, HasAutofocus, Focusable<C>, ApplicationContextAware, InitializingBean {

    protected static final String PROPERTY_VALUE = "value";
    protected static final String PROPERTY_ALLOW_CUSTOM_VALUE = "allowCustomValue";
    protected static final String SLOT_ACTIONS = "actions";
    protected static final String ATTRIBUTE_HAS_ACTIONS = "has-actions";

    protected ApplicationContext applicationContext;

    protected Div actionsLayout;

    protected AbstractFieldDelegate<C, V, V> fieldDelegate;
    protected BaseHasActionsDelegate<C> hasActionsDelegate;

    protected Formatter<? super V> formatter;

    protected ValuePickerBase() {
        super(null);

        // TODO: gg, move to delegate
//        addValueChangeListener(e -> validate());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        initComponent();
    }

    protected void initComponent() {
        fieldDelegate = createFieldDelegate();
        hasActionsDelegate = createHasActionsDelegate();

        setAllowCustomValue(false);
    }

    protected void initFieldValuePropertyChangeListener() {
        String propChangeEvent = eventForMode(ValueChangeMode.ON_CHANGE, PROPERTY_VALUE + "-changed");
        getElement().addPropertyChangeListener(PROPERTY_VALUE, propChangeEvent, this::handlePropertyChange);
    }

    protected void handlePropertyChange(PropertyChangeEvent event) {
        if (!isAllowCustomValueBoolean()) {
            return;
        }

        String text = (String) event.getValue();
        V value = getValue();

        if (Strings.isNullOrEmpty(text)
                || Objects.equals(text, formatValue(value))) {
            return;
        }

        setPresentationValue(value);

        //noinspection unchecked
        CustomValueSetEvent<C, V> valueChangeEvent = new CustomValueSetEvent<>(((C) this), text);
        fireEvent(valueChangeEvent);
    }

    @Override
    public void setValue(@Nullable V value) {
        super.setValue(value);
    }

    @Override
    public void setValueFromClient(@Nullable V value) {
        setModelValue(value, true);
        setPresentationValue(value);
    }

    @Override
    protected void setPresentationValue(@Nullable V newPresentationValue) {
        String newValue = formatValue(newPresentationValue);
        getElement().setProperty(PROPERTY_VALUE, newValue);
    }

    protected String formatValue(@Nullable V value) {
        if (formatter != null) {
            return nullToEmpty(formatter.apply(value));
        }

        return fieldDelegate.applyDefaultValueFormat(value);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public Formatter<V> getFormatter() {
        return (Formatter<V>) formatter;
    }

    @Override
    public void setFormatter(@Nullable Formatter<? super V> formatter) {
        this.formatter = formatter;
    }

    @Override
    public void setRequired(boolean required) {
        HasRequired.super.setRequired(required);
        // TODO: gg, implement
//        getValidationSupport().setRequired(required);
    }

    @Nullable
    @Override
    public String getRequiredMessage() {
        return fieldDelegate.getRequiredMessage();
    }

    @Override
    public void setRequiredMessage(@Nullable String requiredMessage) {
        fieldDelegate.setRequiredMessage(requiredMessage);
    }

    @Nullable
    @Override
    public ValueSource<V> getValueSource() {
        return fieldDelegate.getValueSource();
    }

    @Override
    public void setValueSource(@Nullable ValueSource<V> valueSource) {
        fieldDelegate.setValueSource(valueSource);
    }

    @Override
    public Registration addValidator(Validator<? super V> validator) {
        return fieldDelegate.addValidator(validator);
    }

    @Override
    public void executeValidators() throws ValidationException {
        fieldDelegate.executeValidators();
    }

    @Override
    public void setInvalid(boolean invalid) {
        fieldDelegate.setInvalid(invalid);
    }

    @Override
    public boolean isInvalid() {
        return fieldDelegate.isInvalid();
    }

    @Override
    public void addAction(Action action, int index) {
        hasActionsDelegate.addBinding(action, index, ValuePickerButton.class,
                ValuePickerButton::addClickListener, this::createButton, this::removeButton);
        attachAction(action);
        updateActionsSlot();
    }

    // TODO: gg, move to delegate
    @SuppressWarnings("unchecked")
    protected void attachAction(Action action) {
        if (action instanceof PickerAction) {
            ((PickerAction<?, PickerComponent<V>, ?>) action).setTarget(this);
        }
    }

    @Override
    public void removeAction(Action action) {
        List<ValuePickerButton> buttons = hasActionsDelegate.getComponentsByAction(ValuePickerButton.class, action);
        hasActionsDelegate.removeAction(action);
        buttons.forEach(this::removeButton);

        updateActionsSlot();
    }

    protected ValuePickerButton createButton(int index) {
        ValuePickerButton button = createComponent(ValuePickerButton.class);
        getActionsLayout().addComponentAtIndex(index, button);
        return button;
    }

    protected void removeButton(ValuePickerButton button) {
        getActionsLayout().remove(button);
    }

    @Override
    public Collection<Action> getActions() {
        return hasActionsDelegate.getActions();
    }

    @Nullable
    @Override
    public Action getAction(String id) {
        return hasActionsDelegate.getAction(id).orElse(null);
    }

    protected Div getActionsLayout() {
        if (actionsLayout == null) {
            actionsLayout = createActionsLayout();
        }

        return actionsLayout;
    }

    protected Div createActionsLayout() {
        return createComponent(Div.class);
    }

    protected void updateActionsSlot() {
        if (getActions().isEmpty()) {
            UiComponentUtils.clearSlot(getElement(), SLOT_ACTIONS);
            getElement().removeAttribute(ATTRIBUTE_HAS_ACTIONS);
        } else {
            UiComponentUtils.addComponentsToSlot(getElement(), SLOT_ACTIONS, getActionsLayout());
            getElement().setAttribute(ATTRIBUTE_HAS_ACTIONS, true);
        }
    }

    protected <T extends Component> T createComponent(Class<T> aClass) {
        UiComponents uiComponents = applicationContext.getBean(UiComponents.class);
        return uiComponents.create(aClass);
    }

    @SuppressWarnings("unchecked")
    protected AbstractFieldDelegate<C, V, V> createFieldDelegate() {
        return applicationContext.getBean(FieldDelegate.class, this);
    }

    @SuppressWarnings("unchecked")
    protected BaseHasActionsDelegate<C> createHasActionsDelegate() {
        ActionBinders actionBinders = applicationContext.getBean(ActionBinders.class);
        return applicationContext.getBean(BaseHasActionsDelegate.class, actionBinders.binder(this));
    }

    protected boolean isAllowCustomValueBoolean() {
        return this.getElement().getProperty(PROPERTY_ALLOW_CUSTOM_VALUE, false);
    }

    protected void setAllowCustomValue(boolean allowCustomValue) {
        this.getElement().setProperty(PROPERTY_ALLOW_CUSTOM_VALUE, allowCustomValue);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Registration addCustomValueSetListener(ComponentEventListener<CustomValueSetEvent<C, V>> listener) {
        return getEventBus().addListener(CustomValueSetEvent.class, (ComponentEventListener) listener);
    }
}
