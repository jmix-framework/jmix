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

package io.jmix.flowui.action.multivaluepicker;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.provider.DataProvider;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.model.Range;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.FlowuiComponentProperties;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.ViewOpeningAction;
import io.jmix.flowui.action.valuepicker.PickerAction;
import io.jmix.flowui.app.multivaluepicker.MultiValueSelectView;
import io.jmix.flowui.app.multivaluepicker.MultiValueSelectView.MultiValueSelectContext;
import io.jmix.flowui.component.PickerComponent;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.component.valuepicker.JmixMultiValuePicker;
import io.jmix.flowui.data.EntityValueSource;
import io.jmix.flowui.kit.component.FlowuiComponentUtils;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.sys.ActionViewInitializer;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.OpenMode;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.builder.WindowBuilder;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.function.Consumer;

import static io.jmix.flowui.view.StandardOutcome.SELECT;

/**
 * Standard multi value select action for selection the field value.
 * <p>
 * Should be defined for {@link JmixMultiValuePicker} or its subclass
 */
@ActionType(MultiValueSelectAction.ID)
public class MultiValueSelectAction<E>
        extends PickerAction<MultiValueSelectAction<E>, PickerComponent<Collection<E>>, Collection<E>>
        implements InitializingBean, ViewOpeningAction {

    public static final String ID = "multi_value_select";
    public static final String DEFAULT_MULTI_VALUE_SELECT_VIEW = "multiValueSelectDialog";

    protected Messages messages;
    protected FlowuiComponentProperties flowuiComponentProperties;
    protected DialogWindows dialogWindows;

    protected boolean readOnly = false;

    protected ActionViewInitializer viewInitializer = new ActionViewInitializer();
    protected MultiValueSelectContext<E> multiValueSelectContext = new MultiValueSelectContext<>();

    public MultiValueSelectAction() {
        super(ID);
    }

    public MultiValueSelectAction(String id) {
        super(id);
    }

    @Override
    protected void initAction() {
        super.initAction();

        this.icon = FlowuiComponentUtils.convertToIcon(VaadinIcon.ELLIPSIS_DOTS_H);
    }

    @Autowired
    protected void setFlowuiComponentProperties(FlowuiComponentProperties flowuiComponentProperties) {
        this.flowuiComponentProperties = flowuiComponentProperties;
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setDialogWindows(DialogWindows dialogWindows) {
        this.dialogWindows = dialogWindows;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        setShortcutCombination(KeyCombination.create(flowuiComponentProperties.getPickerLookupShortcut()));

        if (getShortcutCombination() != null) {
            setDescription(messages.getMessage("actions.multiValuePicker.select.description")
                    + " (" + getShortcutCombination().format() + ")");
        } else {
            setDescription(messages.getMessage("actions.multiValuePicker.select.description"));
        }

        setViewId(DEFAULT_MULTI_VALUE_SELECT_VIEW);
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    protected void setReadOnly(boolean readOnly) {
        if (this.readOnly != readOnly) {
            multiValueSelectContext.setReadOnly(readOnly);
        }
    }

    @Nullable
    @Override
    public OpenMode getOpenMode() {
        // Lookup view opens in a dialog window only
        return OpenMode.DIALOG;
    }

    @Override
    public void setOpenMode(@Nullable OpenMode openMode) {
        throw new UnsupportedOperationException("Lookup view opens in a dialog window only");
    }

    public String getViewId() {
        return viewInitializer.getViewId() == null
                ? DEFAULT_MULTI_VALUE_SELECT_VIEW
                : viewInitializer.getViewId();
    }

    public void setViewId(@Nullable String viewId) {
        viewInitializer.setViewId(viewId);
    }

    @SuppressWarnings("rawtypes")
    @Nullable
    public Class<? extends View> getViewClass() {
        return viewInitializer.getViewClass();
    }

    @SuppressWarnings("rawtypes")
    public void setViewClass(@Nullable Class<? extends View> viewClass) {
        viewInitializer.setViewClass(viewClass);
    }

    @Nullable
    @Override
    public RouteParametersProvider getRouteParametersProvider() {
        // Select view opens in a dialog window only
        return null;
    }

    @Override
    public void setRouteParametersProvider(@Nullable RouteParametersProvider provider) {
        throw new UnsupportedOperationException("Select view opens in a dialog window only");
    }

    @Nullable
    @Override
    public QueryParametersProvider getQueryParametersProvider() {
        // Select view opens in a dialog window only
        return null;
    }

    @Override
    public void setQueryParametersProvider(@Nullable QueryParametersProvider provider) {
        throw new UnsupportedOperationException("Select view opens in a dialog window only");
    }

    @Override
    public <V extends View<?>> void setAfterCloseHandler(@Nullable Consumer<DialogWindow.AfterCloseEvent<V>> afterCloseHandler) {
        viewInitializer.setAfterCloseHandler(afterCloseHandler);
    }

    @Nullable
    public String getLookupViewId() {
        return multiValueSelectContext.getLookupViewId();
    }

    public void setLookupViewId(@Nullable String lookupViewId) {
        multiValueSelectContext.setLookupViewId(lookupViewId);
    }

    @Nullable
    public String getEntityName() {
        return multiValueSelectContext.getEntityName();
    }

    public void setEntityName(@Nullable String entityName) {
        multiValueSelectContext.setEntityName(entityName);
    }

    @Nullable
    public Class<?> getJavaClass() {
        return multiValueSelectContext.getJavaClass();
    }

    public void setJavaClass(@Nullable Class<?> javaClass) {
        multiValueSelectContext.setJavaClass(javaClass);
    }

    @Nullable
    public Class<? extends Enum<?>> getEnumClass() {
        return multiValueSelectContext.getEnumClass();
    }

    public void setEnumClass(@Nullable Class<? extends Enum<?>> enumClass) {
        multiValueSelectContext.setEnumClass(enumClass);
    }

    public boolean isUseComboBox() {
        return multiValueSelectContext.isUseComboBox();
    }

    public void setUseComboBox(boolean useComboBox) {
        multiValueSelectContext.setUseComboBox(useComboBox);
    }

    @Nullable
    public DataProvider<?, ?> getItems() {
        return multiValueSelectContext.getItems();
    }

    public void setItems(DataProvider<?, ?> items) {
        multiValueSelectContext.setItems(items);
    }

    @Nullable
    public TimeZone getTimeZone() {
        return multiValueSelectContext.getTimeZone();
    }

    public void setTimeZone(@Nullable TimeZone timeZone) {
        multiValueSelectContext.setTimeZone(timeZone);
    }

    @Nullable
    public ItemLabelGenerator<E> getItemLabelGenerator() {
        return multiValueSelectContext.getItemLabelGenerator();
    }

    public void setItemLabelGenerator(@Nullable ItemLabelGenerator<E> itemLabelGenerator) {
        multiValueSelectContext.setItemLabelGenerator(itemLabelGenerator);
    }

    public List<Validator<E>> getValidators() {
        return multiValueSelectContext.getValidators();
    }

    public void setValidators(@Nullable List<Validator<E>> validators) {
        multiValueSelectContext.setValidators(validators);
    }

    public void addValidator(Validator<E> validator) {
        multiValueSelectContext.addValidator(validator);
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public void execute() {
        checkTarget();

        View<?> origin = UiComponentUtils.findView((Component) target);
        if (origin == null) {
            String message = String.format("%s component is not bound to a view", target.getClass().getSimpleName());
            throw new IllegalStateException(message);
        }

        WindowBuilder<View<?>> builder = dialogWindows.view(origin, Objects.requireNonNull(getViewId()));

        builder = viewInitializer.initWindowBuilder(builder);

        DialogWindow<View<?>> build = builder.build();
        View<?> view = build.getView();

        if (!(view instanceof MultiValueSelectView)) {
            String message = String.format("Select value screen must implement '%s'",
                    MultiValueSelectView.class.getSimpleName());
            throw new IllegalArgumentException(message);
        }

        multiValueSelectContext.setTargetClass(view.getClass());
        multiValueSelectContext.setReadOnly(((HasValueAndElement<?, ?>) target).isReadOnly());

        if (target.getValueSource() instanceof EntityValueSource) {
            initMultiValuePickerComponentValueType((EntityValueSource<?, ?>) target.getValueSource());
        }

        multiValueSelectContext.setInitialValues(((HasValueAndElement<?, Collection<E>>) target).getValue());

        ((MultiValueSelectView<E>) view).setMultiValueSelectContext(multiValueSelectContext);

        build.addAfterCloseListener(event -> {
            if (event.closedWith(SELECT)) {
                MultiValueSelectView<E> selectValueView = (MultiValueSelectView<E>) view;
                target.setValueFromClient((selectValueView).getValue());
            }
        });

        build.open();
    }

    public MultiValueSelectAction<E> withViewId(@Nullable String viewId) {
        setViewId(viewId);
        return this;
    }

    @SuppressWarnings("rawtypes")
    public MultiValueSelectAction<E> withViewClass(@Nullable Class<? extends View> viewClass) {
        setViewClass(viewClass);
        return this;
    }

    public <V extends View<?>> MultiValueSelectAction<E> withAfterCloseHandler(
            @Nullable Consumer<DialogWindow.AfterCloseEvent<V>> afterCloseHandler) {
        setAfterCloseHandler(afterCloseHandler);
        return this;
    }

    public MultiValueSelectAction<E> withValidators(@Nullable List<Validator<E>> validators) {
        setValidators(validators);
        return this;
    }

    public MultiValueSelectAction<E> withEntityName(@Nullable String entityName) {
        setEntityName(entityName);
        return this;
    }

    public MultiValueSelectAction<E> withEnumClass(@Nullable Class<? extends Enum<?>> enumClass) {
        setEnumClass(enumClass);
        return this;
    }

    public MultiValueSelectAction<E> withJavaClass(@Nullable Class<?> javaClass) {
        setJavaClass(javaClass);
        return this;
    }

    public MultiValueSelectAction<E> withReadOnly(boolean readOnly) {
        setReadOnly(readOnly);
        return this;
    }

    public MultiValueSelectAction<E> withLookupViewId(@Nullable String lookupViewId) {
        setLookupViewId(lookupViewId);
        return this;
    }

    public MultiValueSelectAction<E> withUseComboBox(boolean useComboBox) {
        setUseComboBox(useComboBox);
        return this;
    }

    public MultiValueSelectAction<E> withItems(DataProvider<?, ?> items) {
        setItems(items);
        return this;
    }

    public MultiValueSelectAction<E> withItemLabelGenerator(@Nullable ItemLabelGenerator<E> itemLabelGenerator) {
        setItemLabelGenerator(itemLabelGenerator);
        return this;
    }

    public MultiValueSelectAction<E> withTimeZone(@Nullable TimeZone timeZone) {
        setTimeZone(timeZone);
        return this;
    }

    @SuppressWarnings("unchecked")
    protected void initMultiValuePickerComponentValueType(EntityValueSource<?, ?> valueSource) {
        Range range = valueSource.getMetaPropertyPath()
                .getRange();

        if (getEntityName() == null && range.isClass()) {
            setEntityName(range.asClass().getName());
        } else if (getJavaClass() == null && range.isDatatype()) {
            setJavaClass(range.asDatatype().getJavaClass());
        } else if (getEnumClass() == null && range.isEnum()) {
            setEnumClass(range.asEnumeration().getJavaClass());
        }
    }
}
