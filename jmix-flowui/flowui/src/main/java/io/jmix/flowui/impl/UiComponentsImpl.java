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

package io.jmix.flowui.impl;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.di.Instantiator;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.SupportsDatatype;
import io.jmix.flowui.component.accordion.JmixAccordion;
import io.jmix.flowui.component.accordion.JmixAccordionPanel;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.checkboxgroup.JmixCheckboxGroup;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.datetimepicker.TypedDateTimePicker;
import io.jmix.flowui.component.details.JmixDetails;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.TreeDataGrid;
import io.jmix.flowui.component.image.JmixImage;
import io.jmix.flowui.component.listbox.JmixListBox;
import io.jmix.flowui.component.listbox.JmixMultiSelectListBox;
import io.jmix.flowui.component.loginform.JmixLoginForm;
import io.jmix.flowui.component.multiselectcombobox.JmixMultiSelectComboBox;
import io.jmix.flowui.component.radiobuttongroup.JmixRadioButtonGroup;
import io.jmix.flowui.component.scroller.JmixScroller;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.component.splitlayout.JmixSplitLayout;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.textfield.JmixBigDecimalField;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.timepicker.TypedTimePicker;
import io.jmix.flowui.component.upload.JmixUpload;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.component.menubar.JmixMenuBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.lang.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@org.springframework.stereotype.Component("flowui_UiComponents")
public class UiComponentsImpl implements UiComponents {
    private static final Logger log = LoggerFactory.getLogger(UiComponentsImpl.class);

    protected DatatypeRegistry datatypeRegistry;

    public UiComponentsImpl(DatatypeRegistry datatypeRegistry) {
        this.datatypeRegistry = datatypeRegistry;
    }

    protected Set<ComponentInfo> components = ConcurrentHashMap.newKeySet();

    {
        register(DataGrid.class, Grid.class);
        register(TreeDataGrid.class, TreeGrid.class);
        register(JmixAccordion.class, Accordion.class);
        register(JmixAccordionPanel.class, AccordionPanel.class);
        register(JmixCheckbox.class, Checkbox.class);
        register(JmixCheckboxGroup.class, CheckboxGroup.class);
        register(JmixRadioButtonGroup.class, RadioButtonGroup.class);
        register(JmixImage.class, Image.class);
        register(JmixListBox.class, ListBox.class);
        register(JmixMultiSelectListBox.class, MultiSelectListBox.class);
        register(JmixDetails.class, Details.class);
        register(JmixScroller.class, Scroller.class);
        register(JmixSplitLayout.class, SplitLayout.class);
        register(JmixButton.class, Button.class);
        register(JmixSelect.class, Select.class);
        register(JmixComboBox.class, ComboBox.class);
        register(JmixMultiSelectComboBox.class, MultiSelectComboBox.class);
        register(JmixTextArea.class, TextArea.class);
        register(TypedTextField.class, TextField.class);
        register(TypedTimePicker.class, TimePicker.class);
        register(TypedDateTimePicker.class, DateTimePicker.class);
        register(TypedDatePicker.class, DatePicker.class);
        register(JmixBigDecimalField.class, BigDecimalField.class);
        register(JmixLoginForm.class, LoginForm.class);
        register(JmixUpload.class, Upload.class);
        register(JmixMenuBar.class, MenuBar.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Component> T create(Class<T> type) {
        Class<? extends Component> componentToCreate = type;

        ComponentInfo componentInfo = getComponentInfo(type).orElse(null);
        if (componentInfo != null) {
            componentToCreate = getComponentToCreate(componentInfo);
        }

        log.trace("Creating {} component", componentToCreate.getName());

        return (T) Instantiator.get(UI.getCurrent()).getOrCreate(componentToCreate);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends Component> T create(ParameterizedTypeReference<T> typeReference) {
        ParameterizedType type = (ParameterizedType) typeReference.getType();
        T component = create((Class<T>) type.getRawType());
        if (component instanceof SupportsDatatype<?> supportsDataTypeComponent) {
            Type[] actualTypeArguments = type.getActualTypeArguments();

            if (actualTypeArguments.length == 1 && actualTypeArguments[0] instanceof Class actualTypeArgument) {
                supportsDataTypeComponent.setDatatype(datatypeRegistry.find(actualTypeArgument));
            }
        }
        return component;
    }

    public void register(Class<? extends Component> component, Class<? extends Component> replacedComponent) {
        if (getComponentInfo(component).isPresent()) {
            log.trace("Component with `{}` class has already registered", component);
            return;
        }

        ComponentInfo replacedComponentInfo = getComponentInfo(replacedComponent)
                .orElseGet(() -> {
                    ComponentInfo componentInfo = new ComponentInfo(replacedComponent, null);
                    components.add(componentInfo);
                    return componentInfo;
                });


        ComponentInfo componentInfo = new ComponentInfo(component, replacedComponentInfo);
        components.add(componentInfo);

        replacedComponentInfo.setReplacement(componentInfo);
    }

    protected Optional<ComponentInfo> getComponentInfo(Class<? extends Component> component) {
        return components.stream()
                .filter(info -> info.getOriginal().equals(component))
                .findFirst();
    }

    protected Class<? extends Component> getComponentToCreate(ComponentInfo componentInfo) {
        ComponentInfo currentReplacement = componentInfo.getReplacement();
        if (currentReplacement == null) {
            return componentInfo.getOriginal();
        }

        Class<? extends Component> typeToCreate = currentReplacement.getOriginal();

        while (currentReplacement != null) {
            ComponentInfo replacement = currentReplacement.getReplacement();
            if (replacement == null) {
                typeToCreate = currentReplacement.getOriginal();
            }
            currentReplacement = replacement;
        }

        return typeToCreate;
    }

    protected static class ComponentInfo {

        protected Class<? extends Component> original;

        protected ComponentInfo replacedComponent;

        protected ComponentInfo replacement;

        public ComponentInfo(Class<? extends Component> original, @Nullable ComponentInfo replacedComponent) {
            this.original = original;
            this.replacedComponent = replacedComponent;
        }

        /**
         * @return the original component
         */
        public Class<? extends Component> getOriginal() {
            return original;
        }

        /**
         * @return the component that should be replaced by {@link #original} or {@code null} if not set
         */
        @Nullable
        public ComponentInfo getReplacedComponent() {
            return replacedComponent;
        }

        /**
         * @return the component that should be created for the {@link #original} or {@code null} if not set
         */
        @Nullable
        public ComponentInfo getReplacement() {
            return replacement;
        }

        /**
         * Sets the component that should be created for the {@link #original}.
         *
         * @param replacement replacement
         */
        public void setReplacement(@Nullable ComponentInfo replacement) {
            this.replacement = replacement;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            return original.equals(((ComponentInfo) obj).getOriginal());
        }

        @Override
        public int hashCode() {
            return original.hashCode();
        }

        @Override
        public String toString() {
            return "{\"original\": \"" + original.getName() + "\"}";
        }
    }
}
