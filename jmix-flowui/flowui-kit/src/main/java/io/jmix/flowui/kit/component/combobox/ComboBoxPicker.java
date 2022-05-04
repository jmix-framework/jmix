package io.jmix.flowui.kit.component.combobox;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.JsModule;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.HasActions;
import io.jmix.flowui.kit.component.HasTitle;
import io.jmix.flowui.kit.component.SupportsUserAction;
import io.jmix.flowui.kit.component.valuepicker.ValuePickerActionSupport;

import javax.annotation.Nullable;
import java.util.Collection;

// TODO: gg, rename
@Tag("jmix-combo-box-picker")
@JsModule("./src/combo-box-picker/jmix-combo-box-picker.js")
@JsModule("./flow-component-renderer.js")
@JsModule("./src/combo-box-picker/comboBoxConnector.js")
public class ComboBoxPicker<V> extends ComboBox<V>
        implements SupportsUserAction<V>, HasActions, HasTitle {

    protected ValuePickerActionSupport actionsSupport;

    @Override
    public void setValueFromClient(@Nullable V value) {
        setModelValue(value, true);
    }

    @Override
    public void addAction(Action action, int index) {
        getActionsSupport().addAction(action, index);
    }

    @Override
    public void removeAction(Action action) {
        getActionsSupport().removeAction(action);
    }

    @Override
    public Collection<Action> getActions() {
        return getActionsSupport().getActions();
    }

    @Nullable
    @Override
    public Action getAction(String id) {
        return getActionsSupport().getAction(id).orElse(null);
    }

    protected ValuePickerActionSupport getActionsSupport() {
        if (actionsSupport == null) {
            actionsSupport = createActionsSupport();
        }

        return actionsSupport;
    }

    protected ValuePickerActionSupport createActionsSupport() {
        return new ValuePickerActionSupport(this);
    }
}
