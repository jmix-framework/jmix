/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.app.multivaluepicker;


import com.google.common.base.Strings;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.core.Messages;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.datatype.TimeZoneAwareDatatype;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.entitypicker.EntityLookupAction;
import io.jmix.flowui.component.SupportsValidation;
import io.jmix.flowui.component.UiComponentsGenerator;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.component.valuepicker.EntityPicker;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.action.BaseAction;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.builder.LookupWindowBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@ViewController("multiValueSelectDialog")
@ViewDescriptor("multi-value-select-dialog.xml")
@DialogMode(height = "30em", minWidth = "25em", resizable = true)
public class MultiValueSelectDialog<E> extends StandardListView<E> implements MultiValueSelectView<E> {

    private static final Logger log = LoggerFactory.getLogger(MultiValueSelectDialog.class);

    @ViewComponent
    protected JmixButton selectBtn;
    @ViewComponent
    protected HorizontalLayout addItemLayout;
    @ViewComponent
    protected VerticalLayout multiValueLayout;
    @ViewComponent
    private BaseAction select;

    @Autowired
    protected Messages messages;
    @Autowired
    protected UiComponentsGenerator uiComponentsGenerator;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected DialogWindows dialogWindows;
    @Autowired
    protected DatatypeRegistry datatypeRegistry;
    @Autowired
    protected CurrentAuthentication currentAuthentication;
    @Autowired
    protected MetadataTools metadataTools;

    protected MultiValueSelectContext<E> context;
    protected List<E> values = new ArrayList<>();

    @Override
    public void setMultiValueSelectContext(MultiValueSelectContext<E> context) {
        if (this.context != null) {
            throw new IllegalStateException("Screen has been initialized this SelectValueContext");
        }

        context.setValueExistsHandler(this::valueExists);
        context.setAddItemToLayoutHandler(this::addValueToLayout);
        context.setEntityPickerActionPerformedEventHandler(this::lookupActionPerformed);
        context.setAddValueInternalHandler(this::addValueInternal);

        this.context = context;

        initAddComponentLayout();
        initValues();

        select.setEnabled(!context.isReadOnly());
        if (context.isReadOnly()) {
            selectBtn.focus();
        }
    }

    @Override
    public List<E> getValue() {
        return Collections.unmodifiableList(values);
    }

    @SuppressWarnings("unchecked")
    protected void initAddComponentLayout() {
        addItemLayout.removeAll();
        Component component = uiComponentsGenerator.generate(context);
        component.setId("listValueComponent");

        if (CollectionUtils.isNotEmpty(context.getValidators())) {
            for (Validator<E> validator : context.getValidators()) {
                if (component instanceof SupportsValidation) {
                    ((SupportsValidation<E>) component).addValidator(validator);
                }
            }
        }

        addItemLayout.add(component);
        addItemLayout.expand(component);

        if (component instanceof HasValueAndElement) {
            ((HasValueAndElement<?, ?>) component).setReadOnly(context.isReadOnly());
        }

        if (context.isReadOnly()) {
            if (component instanceof Focusable) {
                ((Focusable<?>) component).focus();
            }
        }

        if (context.getJavaClass() != null) {
            JmixButton addButton = uiComponents.create(JmixButton.class);
            addButton.setId("add");
            addButton.setText(messages.getMessage("actions.Add"));
            addButton.setIcon(VaadinIcon.PLUS.create());
            addButton.addClickListener(e -> {
                if (component instanceof HasValue) {
                    addValueInternal((HasValue<?, E>) component);
                }
            });

            addItemLayout.add(addButton);
            addButton.setEnabled(!context.isReadOnly());
        }
    }

    @SuppressWarnings("unchecked")
    protected void addValueInternal(HasValue<?, E> component) {
        E value = component.getValue();

        if (value != null
                && (!(component instanceof SupportsValidation) || isValid((SupportsValidation<E>) component))) {
            component.clear();

            if (!valueExists(value)) {
                addValueToLayout(value);
            }
        }
    }

    protected boolean isValid(SupportsValidation<E> component) {
        try {
            component.executeValidators();
        } catch (ValidationException e) {
            if (log.isTraceEnabled()) {
                log.trace("Validation failed", e);
            } else if (log.isDebugEnabled()) {
                log.debug("Validation failed: " + e);
            }
            return false;
        }

        return true;
    }

    protected boolean valueExists(E value) {
        return values.contains(value);
    }

    protected void initValues() {
        for (E value : context.getInitialValues()) {
            addValueToLayout(value);
        }
    }

    protected void addValueToLayout(E value) {
        String label = getGeneratedLabel(value);

        Div item = uiComponents.create(Div.class);
        item.setText(label);

        if (!context.isReadOnly()) {
            JmixButton deleteItemButton = uiComponents.create(JmixButton.class);
            deleteItemButton.setIcon(VaadinIcon.CLOSE_SMALL.create());

            deleteItemButton.addThemeVariants(
                    ButtonVariant.LUMO_SMALL,
                    ButtonVariant.LUMO_TERTIARY_INLINE,
                    ButtonVariant.LUMO_ICON
            );
            deleteItemButton.addClickListener(e -> {
                values.remove(value);
                item.getElement().removeFromParent();
            });

            item.add(deleteItemButton);
        }

        multiValueLayout.add(item);
        values.add(value);
    }

    protected void lookupActionPerformed(ActionPerformedEvent event) {
        //noinspection unchecked
        EntityPicker<E> entityPicker = (EntityPicker<E>) ((EntityLookupAction<E>) event.getSource()).getTarget();

        LookupWindowBuilder<E, View<?>> lookupBuilder = dialogWindows.lookup(Objects.requireNonNull(entityPicker))
                .withSelectHandler(items -> {
                    if (CollectionUtils.isNotEmpty(items)) {
                        for (E item : items) {
                            if (item != null && !valueExists(item)) {
                                this.addValueToLayout(item);
                            }
                        }
                    }

                    entityPicker.clear();
                });

        if (!Strings.isNullOrEmpty(context.getLookupViewId())) {
            lookupBuilder = lookupBuilder.withViewId(context.getLookupViewId());
        }

        lookupBuilder.open();
    }

    protected String getGeneratedLabel(E value) {
        ItemLabelGenerator<E> itemLabelGenerator = context.getItemLabelGenerator();
        if (itemLabelGenerator != null) {
            return itemLabelGenerator.apply(value);
        }

        TimeZone timeZone = context.getTimeZone();
        Class<?> javaClass = context.getJavaClass();
        if (timeZone != null
                && javaClass != null) {
            Datatype<?> datatype = datatypeRegistry.get(javaClass);
            if (datatype instanceof TimeZoneAwareDatatype) {
                return ((TimeZoneAwareDatatype) datatype).format(value,
                        currentAuthentication.getLocale(), timeZone);
            }
        }

        return metadataTools.format(value);
    }

    @Subscribe("select")
    public void onSelect(ActionPerformedEvent event) {
        close(StandardOutcome.SELECT);
    }

    @Subscribe("cancel")
    public void onCancel(ActionPerformedEvent event) {
        close(StandardOutcome.DISCARD);
    }
}
