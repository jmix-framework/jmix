package io.jmix.flowui.action.valuepicker;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.Messages;
import io.jmix.flowui.FlowUiComponentProperties;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.component.PickerComponent;
import io.jmix.flowui.kit.component.FlowUiComponentUtils;
import io.jmix.flowui.kit.component.KeyCombination;
import org.springframework.beans.factory.annotation.Autowired;

import static io.jmix.flowui.component.UiComponentUtils.getEmptyValue;


@ActionType(ValueClearAction.ID)
public class ValueClearAction<V> extends PickerAction<ValueClearAction<V>, PickerComponent<V>, V> {

    public static final String ID = "value_clear";

    public ValueClearAction() {
        this(ID);
    }

    public ValueClearAction(String id) {
        super(id);
    }

    @Override
    protected void initAction() {
        super.initAction();

        this.icon = FlowUiComponentUtils.iconToSting(VaadinIcon.CLOSE);
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.valuePicker.clear.description");
    }

    @Autowired
    protected void setFlowUiComponentProperties(FlowUiComponentProperties flowUiComponentProperties) {
        this.shortcutCombination = KeyCombination.create(flowUiComponentProperties.getPickerClearShortcut());
    }

    // TODO: gg, editable?

    @Override
    public void execute() {
        // Set the value as if the user had set it
        target.setValueFromClient(getEmptyValue((Component) target));
    }
}
