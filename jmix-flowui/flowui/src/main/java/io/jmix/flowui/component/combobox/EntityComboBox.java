package io.jmix.flowui.component.combobox;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.binder.ActionBinders;
import io.jmix.flowui.action.valuepicker.PickerAction;
import io.jmix.flowui.component.LookupComponent;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.delegate.AbstractFieldDelegate;
import io.jmix.flowui.component.delegate.EntityFieldDelegate;
import io.jmix.flowui.component.delegate.impl.BaseHasActionsDelegate;
import io.jmix.flowui.component.EntityPickerComponent;
import io.jmix.flowui.component.PickerComponent;
import io.jmix.flowui.component.valuepicker.ValuePickerButton;
import io.jmix.flowui.kit.action.Action;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Tag("jmix-entity-combo-box")
@JsModule("./src/entity-combo-box/jmix-entity-combo-box.js")
@JsModule("./flow-component-renderer.js")
@JsModule("./src/entity-combo-box/comboBoxConnector.js")
public class EntityComboBox<V> extends JmixComboBox<V>
        implements EntityPickerComponent<V>, LookupComponent<V> {

    protected static final String SLOT_ACTIONS = "actions";
    protected static final String ATTRIBUTE_HAS_ACTIONS = "has-actions";

    protected MetaClass metaClass;

    protected Div actionsLayout;

    protected BaseHasActionsDelegate<EntityComboBox<V>> hasActionsDelegate;

    @Override
    protected void initComponent() {
        super.initComponent();

        hasActionsDelegate = createHasActionsDelegate();
    }

    @Override
    public void setValue(@Nullable V value) {
        checkValueType(value);
        super.setValue(value);
    }

    @Override
    public void setValueFromClient(@Nullable V value) {
        checkValueType(value);
        setModelValue(value, true);
    }

    protected void checkValueType(@Nullable V value) {
        if (value != null) {
            getFieldDelegate().checkValueType(value);
        }
    }

    @Nullable
    @Override
    public MetaClass getMetaClass() {
        return getFieldDelegate().getMetaClass();
    }

    @Override
    public void setMetaClass(@Nullable MetaClass metaClass) {
        getFieldDelegate().setMetaClass(metaClass);
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

    @Override
    public Set<V> getSelectedItems() {
        return isEmpty() ? Collections.emptySet() : Collections.singleton(getValue());
    }

    protected <T extends Component> T createComponent(Class<T> aClass) {
        UiComponents uiComponents = applicationContext.getBean(UiComponents.class);
        return uiComponents.create(aClass);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected AbstractFieldDelegate<? extends JmixComboBox<V>, V, V> createFieldDelegate() {
        return applicationContext.getBean(EntityFieldDelegate.class, this);
    }

    @SuppressWarnings("unchecked")
    protected EntityFieldDelegate<EntityComboBox<V>, V, V> getFieldDelegate() {
        return (EntityFieldDelegate<EntityComboBox<V>, V, V>) fieldDelegate;
    }

    @SuppressWarnings("unchecked")
    protected BaseHasActionsDelegate<EntityComboBox<V>> createHasActionsDelegate() {
        ActionBinders actionBinders = applicationContext.getBean(ActionBinders.class);
        return applicationContext.getBean(BaseHasActionsDelegate.class, actionBinders.binder(this));
    }
}
