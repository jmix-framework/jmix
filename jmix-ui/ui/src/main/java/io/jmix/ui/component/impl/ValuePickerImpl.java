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

package io.jmix.ui.component.impl;

import com.google.common.base.Strings;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.Resource;
import com.vaadin.shared.Registration;
import io.jmix.core.MetadataTools;
import io.jmix.core.common.event.Subscription;
import io.jmix.ui.AppUI;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.*;
import io.jmix.ui.component.data.meta.EntityValueSource;
import io.jmix.ui.component.formatter.Formatter;
import io.jmix.ui.UiComponentProperties;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.sys.TestIdManager;
import io.jmix.ui.theme.ThemeClassNames;
import io.jmix.ui.widget.JmixButton;
import io.jmix.ui.widget.JmixPickerField;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.beans.PropertyChangeEvent;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.base.Strings.nullToEmpty;
import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;
import static io.jmix.ui.component.ComponentsHelper.findActionById;

public class ValuePickerImpl<V>
        extends AbstractField<JmixPickerField<V>, V, V>
        implements ValuePicker<V>, SecuredActionsHolder, InitializingBean {

    protected MetadataTools metadataTools;

    protected Formatter<? super V> formatter;

    protected List<Action> actions = new ArrayList<>(4);
    protected Map<Action, JmixButton> actionButtons = new HashMap<>(4);

    protected ActionsPermissions actionsPermissions = new ActionsPermissions(this);
    protected WebValuePickerActionHandler actionHandler;

    protected Consumer<PropertyChangeEvent> actionPropertyChangeListener = this::actionPropertyChanged;

    protected Function<? super V, String> fieldIconProvider;

    protected Registration fieldListenerRegistration;

    public ValuePickerImpl() {
        component = createComponent();

        attachValueChangeListener(component);
    }

    protected JmixPickerField<V> createComponent() {
        return new JmixPickerField<>();
    }

    @Autowired
    public void setMetadataTools(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @Autowired
    protected void setUiComponentProperties(UiComponentProperties componentProperties) {
        actionHandler = new WebValuePickerActionHandler(componentProperties);
        component.addActionHandler(actionHandler);
    }


    @Override
    public void afterPropertiesSet() {
        initComponent(component);
    }

    protected void initComponent(JmixPickerField<V> component) {
        component.setTextFieldValueProvider(this::formatValue);
    }

    protected String formatValue(@Nullable V value) {
        if (formatter != null) {
            return nullToEmpty(formatter.apply(value));
        }

        return applyDefaultValueFormat(value);
    }

    @SuppressWarnings("rawtypes")
    protected String applyDefaultValueFormat(@Nullable V value) {
        if (valueBinding != null
                && valueBinding.getSource() instanceof EntityValueSource) {
            EntityValueSource entityValueSource = (EntityValueSource) valueBinding.getSource();
            return metadataTools.format(value, entityValueSource.getMetaPropertyPath().getMetaProperty());
        }

        return metadataTools.format(value);
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
    public void setValue(@Nullable V value) {
        super.setValue(value);
        refreshActionsState();
    }

    @Override
    public void setValueFromUser(@Nullable V value) {
        setValueToPresentation(convertToPresentation(value));

        V oldValue = internalValue;
        this.internalValue = value;

        if (!fieldValueEquals(value, oldValue)) {
            ValueChangeEvent<V> event = new ValueChangeEvent<>(this, oldValue, value, true);
            publish(ValueChangeEvent.class, event);
        }

        refreshActionsState();
    }

    @Override
    public void addAction(Action action) {
        checkNotNullArgument(action, "Action must be non null");

        int index = findActionById(actions, action.getId());
        if (index < 0) {
            index = actions.size();
        }

        addAction(action, index);
    }

    @Override
    public void addAction(Action action, int index) {
        checkNotNullArgument(action, "Action must be non null");

        int oldIndex = findActionById(actions, action.getId());
        if (oldIndex >= 0) {
            removeAction(actions.get(oldIndex));
            if (index > oldIndex) {
                index--;
            }
        }

        actions.add(index, action);
        actionHandler.addAction(action, index);

        JmixButton vButton = new JmixButton();
        setupButtonAction(vButton, action);

        action.addPropertyChangeListener(actionPropertyChangeListener);

        component.addButton(vButton, index);
        actionButtons.put(action, vButton);

        if (StringUtils.isNotEmpty(getDebugId()) && AppUI.getCurrent() != null) {
            TestIdManager testIdManager = AppUI.getCurrent().getTestIdManager();
            // Set debug id
            vButton.setId(testIdManager.getTestId(getDebugId() + "_" + action.getId()));
        }

        if (action instanceof ValuePickerAction) {
            ValuePickerAction pickerAction = (ValuePickerAction) action;
            pickerAction.setPicker(this);
            if (!isEditable()) {
                pickerAction.editableChanged(isEditable());
            }
        }

        actionsPermissions.apply(action);

        refreshActionsState();
    }

    protected void setupButtonAction(JmixButton button, Action action) {
        String description = action.getDescription();
        if (description == null && action.getShortcutCombination() != null) {
            description = action.getShortcutCombination().format();
        }
        if (description != null) {
            button.setDescription(description);
        }

        button.setEnabled(action.isEnabled());
        button.setVisible(action.isVisible());

        if (action.getIcon() != null) {
            setButtonIcon(button, action.getIcon());
        }

        AppUI ui = AppUI.getCurrent();
        if (ui != null && ui.isTestMode()) {
            button.setJTestId(action.getId());
        }

        button.setClickHandler(event -> {
            this.focus();
            action.actionPerform(this);
        });
    }

    protected void setButtonIcon(JmixButton button, @Nullable String icon) {
        if (!Strings.isNullOrEmpty(icon)) {
            Resource iconResource = getIconResource(icon);
            button.setIcon(iconResource);
        } else {
            button.setIcon(null);
        }
    }

    protected void actionPropertyChanged(PropertyChangeEvent evt) {
        Action action = (Action) evt.getSource();
        JmixButton button = actionButtons.get(action);

        switch (evt.getPropertyName()) {
            case Action.PROP_ICON:
                setButtonIcon(button, action.getIcon());
                break;
            case Action.PROP_CAPTION:
                button.setCaption(action.getCaption());
                break;
            case Action.PROP_DESCRIPTION:
                button.setDescription(action.getDescription());
                break;
            case Action.PROP_ENABLED:
                button.setEnabled(action.isEnabled());
                break;
            case Action.PROP_VISIBLE:
                button.setVisible(action.isVisible());
                break;
            case ValuePickerAction.PROP_EDITABLE:
                if (action instanceof ValuePickerAction) {
                    button.setVisible(((ValuePickerAction) action).isEditable());
                }
                break;
            default:
                // do nothing
                break;
        }
    }

    @Override
    public void removeAction(Action action) {
        if (actions.remove(action)) {
            actionHandler.removeAction(action);

            JmixButton button = actionButtons.remove(action);
            component.removeButton(button);

            action.removePropertyChangeListener(actionPropertyChangeListener);

            if (action instanceof ValuePickerAction) {
                ((ValuePickerAction) action).setPicker(null);
            }
        }
    }

    @Override
    public void removeAction(String id) {
        Action action = getAction(id);
        if (action != null) {
            removeAction(action);
        }
    }

    @Override
    public void removeAllActions() {
        for (Action action : new ArrayList<>(actions)) {
            removeAction(action);
        }
    }

    @Override
    public Collection<Action> getActions() {
        return Collections.unmodifiableList(actions);
    }

    @Nullable
    @Override
    public Action getAction(String id) {
        return actions.stream()
                .filter(action ->
                        Objects.equals(id, action.getId()))
                .findFirst()
                .orElse(null);
    }

    protected void refreshActionsState() {
        getActions().forEach(Action::refreshState);
    }

    @Override
    public void focus() {
        component.focus();
    }

    @Override
    public int getTabIndex() {
        return component.getTabIndex();
    }

    @Override
    public void setTabIndex(int tabIndex) {
        component.setTabIndex(tabIndex);
    }

    @Override
    public ActionsPermissions getActionsPermissions() {
        return actionsPermissions;
    }

    @Nullable
    @Override
    public Function<? super V, String> getFieldIconProvider() {
        return fieldIconProvider;
    }

    @Override
    public void setFieldIconProvider(@Nullable Function<? super V, String> iconProvider) {
        if (this.fieldIconProvider != iconProvider) {
            this.fieldIconProvider = iconProvider;

            component.setStyleName("jmix-has-field-icon", iconProvider != null);
            component.getField().setStyleName(ThemeClassNames.TEXTFIELD_INLINE_ICON, iconProvider != null);

            component.setIconGenerator(iconProvider != null
                    ? this::generateOptionIcon
                    : null);
        }
    }

    @Nullable
    protected Resource generateOptionIcon(@Nullable V item) {
        if (fieldIconProvider == null) {
            return null;
        }

        String resourceId;
        try {
            resourceId = fieldIconProvider.apply(item);
        } catch (Exception e) {
            LoggerFactory.getLogger(EntityPickerImpl.class)
                    .warn("Error invoking optionIconProvider apply method", e);
            return null;
        }

        return getIconResource(resourceId);
    }

    @Override
    public boolean isFieldEditable() {
        return !component.isFieldReadOnly();
    }

    @Override
    public void setFieldEditable(boolean editable) {
        component.setFieldReadOnly(!editable);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Subscription addFieldValueChangeListener(Consumer<FieldValueChangeEvent<V>> listener) {
        if (fieldListenerRegistration == null) {
            fieldListenerRegistration = component.addFieldListener(this::onFieldValueChange);
        }

        return getEventHub().subscribe(FieldValueChangeEvent.class, (Consumer) listener);
    }

    protected void onFieldValueChange(JmixPickerField.FieldValueChangeEvent<V> e) {
        FieldValueChangeEvent<V> event = new FieldValueChangeEvent<>(this, e.getText(), e.getPrevValue());
        publish(FieldValueChangeEvent.class, event);

        refreshActionsState();
    }

    @Override
    public void commit() {
        super.commit();
    }

    @Override
    public void discard() {
        super.discard();
    }

    @Override
    public boolean isBuffered() {
        return super.isBuffered();
    }

    @Override
    public void setBuffered(boolean buffered) {
        super.setBuffered(buffered);
    }

    @Override
    public boolean isModified() {
        return super.isModified();
    }

    @Override
    public void setFrame(@Nullable Frame frame) {
        super.setFrame(frame);

        if (frame != null) {
            UiControllerUtils.getScreen(frame.getFrameOwner())
                    .addAfterShowListener(afterShowEvent ->
                            refreshActionsState());
        }
    }

    @Override
    protected void setEditableToComponent(boolean editable) {
        super.setEditableToComponent(editable);

        for (Action action : getActions()) {
            if (action instanceof ValuePickerAction) {
                ((ValuePickerAction) action).editableChanged(editable);
            }
        }
    }

    @Override
    public void setDebugId(@Nullable String id) {
        super.setDebugId(id);

        if (id != null && AppUI.getCurrent() != null) {
            String debugId = getDebugId();

            TestIdManager testIdManager = AppUI.getCurrent().getTestIdManager();

            for (Action action : actions) {
                JmixButton button = actionButtons.get(action);
                if (button != null && Strings.isNullOrEmpty(button.getId())) {
                    button.setId(testIdManager.getTestId(debugId + "_" + action.getId()));
                }
            }
        }
    }

    @Nullable
    @Override
    public String getInputPrompt() {
        return component.getPlaceholder();
    }

    @Override
    public void setInputPrompt(@Nullable String inputPrompt) {
        component.setPlaceholder(inputPrompt);
    }

    public class WebValuePickerActionHandler implements com.vaadin.event.Action.Handler {

        protected int[] modifiers;

        protected Map<ShortcutAction, Action> actionsMap = new HashMap<>(4);
        protected List<com.vaadin.event.Action> shortcuts = new ArrayList<>(4);
        protected List<ShortcutAction> orderedShortcuts = new ArrayList<>(4);

        protected List<Action> actionList = new ArrayList<>(4);

        public WebValuePickerActionHandler(UiComponentProperties properties) {
            String[] strModifiers = StringUtils.split(properties.getPickerShortcutModifiers().toUpperCase(), "-");
            modifiers = new int[strModifiers.length];

            for (int i = 0; i < modifiers.length; i++) {
                modifiers[i] = KeyCombination.Modifier.valueOf(strModifiers[i]).getCode();
            }
        }

        @Override
        public com.vaadin.event.Action[] getActions(Object target, Object sender) {
            return shortcuts.toArray(new com.vaadin.event.Action[0]);
        }

        public void addAction(Action action, int index) {
            actionList.add(index, action);

            updateOrderedShortcuts();

            KeyCombination combination = action.getShortcutCombination();
            if (combination != null && combination.getKey() != null) {
                int key = combination.getKey().getCode();
                int[] modifiers = KeyCombination.Modifier.codes(combination.getModifiers());

                ShortcutAction providedShortcut = new ShortcutAction(action.getCaption(), key, modifiers);
                shortcuts.add(providedShortcut);
                actionsMap.put(providedShortcut, action);
            }
        }

        public void removeAction(Action action) {
            List<ShortcutAction> existActions = new ArrayList<>(4);
            for (Map.Entry<ShortcutAction, Action> entry : actionsMap.entrySet()) {
                if (entry.getValue().equals(action)) {
                    existActions.add(entry.getKey());
                }
            }

            shortcuts.removeAll(existActions);
            for (ShortcutAction shortcut : existActions) {
                actionsMap.remove(shortcut);
            }
            actionList.remove(action);

            updateOrderedShortcuts();
        }

        protected void updateOrderedShortcuts() {
            shortcuts.removeAll(orderedShortcuts);
            for (ShortcutAction orderedShortcut : orderedShortcuts) {
                actionsMap.remove(orderedShortcut);
            }

            for (int i = 0; i < actionList.size(); i++) {
                int keyCode = ShortcutAction.KeyCode.NUM1 + i;

                Action orderedAction = actionList.get(i);

                ShortcutAction orderedShortcut = new ShortcutAction(orderedAction.getCaption(), keyCode, modifiers);
                shortcuts.add(orderedShortcut);
                orderedShortcuts.add(orderedShortcut);
                actionsMap.put(orderedShortcut, orderedAction);
            }
        }

        @Override
        public void handleAction(com.vaadin.event.Action action, Object sender, Object target) {
            //noinspection SuspiciousMethodCalls
            Action pickerAction = actionsMap.get(action);
            if (pickerAction != null) {
                pickerAction.actionPerform(ValuePickerImpl.this);
            }
        }
    }
}
