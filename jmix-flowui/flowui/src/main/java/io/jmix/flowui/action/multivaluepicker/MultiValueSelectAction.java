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
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.provider.DataProvider;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.model.Range;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.UiComponentProperties;
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
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.sys.ActionViewInitializer;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.OpenMode;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.builder.WindowBuilder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

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
    protected UiComponentProperties uiComponentProperties;
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

        this.icon = ComponentUtils.convertToIcon(VaadinIcon.ELLIPSIS_DOTS_H);
    }

    @Autowired
    protected void setUiComponentProperties(UiComponentProperties uiComponentProperties) {
        this.uiComponentProperties = uiComponentProperties;
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
        setShortcutCombination(KeyCombination.create(uiComponentProperties.getPickerLookupShortcut()));

        if (getShortcutCombination() != null) {
            setDescription(messages.getMessage("actions.multiValuePicker.select.description")
                    + " (" + getShortcutCombination().format() + ")");
        } else {
            setDescription(messages.getMessage("actions.multiValuePicker.select.description"));
        }

        setViewId(DEFAULT_MULTI_VALUE_SELECT_VIEW);
    }

    /**
     * Determines whether the current state of {@link MultiValueSelectContext} passed
     * to the view opened by this action is read-only.
     *
     * @return {@code true} if the action is in a read-only state, {@code false} otherwise
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Sets the read-only state of {@link MultiValueSelectContext} passed
     * to the view opened by this action.
     *
     * @param readOnly a boolean value indicating the desired read-only state.
     */
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

    /**
     * Returns the identifier of the view to be opened. If the view identifier
     * is not explicitly set via the view initializer, a default identifier is returned.
     *
     * @return the view identifier, or a default identifier if no
     * specific identifier is set.
     */
    public String getViewId() {
        return viewInitializer.getViewId() == null
                ? DEFAULT_MULTI_VALUE_SELECT_VIEW
                : viewInitializer.getViewId();
    }

    /**
     * Sets the identifier for the view to be opened.
     *
     * @param viewId the unique identifier of the view
     */
    public void setViewId(@Nullable String viewId) {
        viewInitializer.setViewId(viewId);
    }

    /**
     * Returns the class of the view to be opened.
     *
     * @return the class of the view if defined, or {@code null} if no class is set.
     */
    @SuppressWarnings("rawtypes")
    @Nullable
    public Class<? extends View> getViewClass() {
        return viewInitializer.getViewClass();
    }

    /**
     * Sets the class of the view to be opened by this action.
     *
     * @param viewClass the class of the view to be opened; can
     *                  be {@code null} if no specific class is set
     */
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

    @Override
    public <V extends View<?>> Consumer<DialogWindow.AfterCloseEvent<V>> getAfterCloseHandler() {
        return viewInitializer.getAfterCloseHandler();
    }

    @Override
    public <V extends View<?>> void setViewConfigurer(@Nullable Consumer<V> viewConfigurer) {
        viewInitializer.setViewConfigurer(viewConfigurer);
    }

    @Override
    public <V extends View<?>> Consumer<V> getViewConfigurer() {
        return viewInitializer.getViewConfigurer();
    }

    /**
     * Returns the identifier for the lookup view. This identifier is obtained
     * from the {@link MultiValueSelectContext} associated with this action.
     * May return {@code null} if no lookup view identifier is set.
     *
     * @return the lookup view identifier, or {@code null} if not set
     */
    @Nullable
    public String getLookupViewId() {
        return multiValueSelectContext.getLookupViewId();
    }

    /**
     * Sets the identifier for the lookup view to be opened. The identifier is passed
     * to the {@link MultiValueSelectContext} associated with this action.
     *
     * @param lookupViewId the unique identifier of the lookup view; can be {@code null}
     *                     if no specific lookup view is set
     */
    public void setLookupViewId(@Nullable String lookupViewId) {
        multiValueSelectContext.setLookupViewId(lookupViewId);
    }

    /**
     * Returns the name of the entity associated with this action.
     *
     * @return the entity name as a {@code String}, or {@code null} if no entity name is set.
     */
    @Nullable
    public String getEntityName() {
        return multiValueSelectContext.getEntityName();
    }

    /**
     * Sets the name of the entity associated with the {@link MultiValueSelectContext}.
     * This name is used to identify and configure the entity for the current action.
     *
     * @param entityName the name of the entity as a {@code String}; may be {@code null}
     *                   if no specific entity is being set
     */
    public void setEntityName(@Nullable String entityName) {
        multiValueSelectContext.setEntityName(entityName);
    }

    /**
     * Returns the Java class associated with the current instance of {@link MultiValueSelectContext}.
     * This is used for defining the type of data handled by the component.
     *
     * @return the Java class if it is defined, or {@code null} if no class is set
     */
    @Nullable
    public Class<?> getJavaClass() {
        return multiValueSelectContext.getJavaClass();
    }

    /**
     * Sets the Java class associated with the current instance of {@link MultiValueSelectContext}.
     * This is used for defining the type of data handled by the component.
     *
     * @param javaClass the Java class to be associated with the current context;
     *                  may be {@code null} if no specific class is set
     */
    public void setJavaClass(@Nullable Class<?> javaClass) {
        multiValueSelectContext.setJavaClass(javaClass);
    }

    /**
     * Returns the enumeration class associated with the current {@link MultiValueSelectContext}.
     * This class defines the type of enums that can be selected or processed by the component.
     *
     * @return the class of the enumeration if defined, or {@code null} if no enumeration class is set
     */
    @Nullable
    public Class<? extends Enum<?>> getEnumClass() {
        return multiValueSelectContext.getEnumClass();
    }

    /**
     * Sets the enumeration class associated with the current {@link MultiValueSelectContext}.
     * This class specifies the type of enumeration usable within the multi-value select action.
     *
     * @param enumClass the class of the enumeration to be set; it may be {@code null} if no specific enumeration class is desired
     */
    public void setEnumClass(@Nullable Class<? extends Enum<?>> enumClass) {
        multiValueSelectContext.setEnumClass(enumClass);
    }

    /**
     * Determines whether the {@link MultiValueSelectContext} is configured to use a {@link ComboBox}
     * as its primary selection component.
     *
     * @return {@code true} if a {@link ComboBox} is used for selection, {@code false} otherwise
     */
    public boolean isUseComboBox() {
        return multiValueSelectContext.isUseComboBox();
    }

    /**
     * Configures whether to use a {@link ComboBox} as the selection component
     * in the associated {@link MultiValueSelectContext}.
     *
     * @param useComboBox a boolean indicating whether a {@link ComboBox} should be used
     *                    for selection ({@code true}) or another component ({@code false})
     */
    public void setUseComboBox(boolean useComboBox) {
        multiValueSelectContext.setUseComboBox(useComboBox);
    }

    /**
     * Returns the data provider associated with the {@link MultiValueSelectContext}.
     *
     * @return the data provider instance if available; otherwise, null
     */
    @Nullable
    public DataProvider<?, ?> getItems() {
        return multiValueSelectContext.getItems();
    }

    /**
     * Sets the items to be used by the {@link MultiValueSelectContext}.
     *
     * @param items the data provider supplying the items to be set
     */
    public void setItems(DataProvider<?, ?> items) {
        multiValueSelectContext.setItems(items);
    }

    /**
     * Returns the current time zone associated with {@link MultiValueSelectContext}.
     *
     * @return the {@code TimeZone} object if defined, or {@code null} if no time zone is set
     */
    @Nullable
    public TimeZone getTimeZone() {
        return multiValueSelectContext.getTimeZone();
    }

    /**
     * Sets the time zone to be associated with the current {@link MultiValueSelectContext}.
     *
     * @param timeZone the {@link TimeZone} object to be set; may be {@code null}
     *                 if no specific time zone is to be associated
     */
    public void setTimeZone(@Nullable TimeZone timeZone) {
        multiValueSelectContext.setTimeZone(timeZone);
    }

    /**
     * Returns the current {@link ItemLabelGenerator} associated with the multi-value select context.
     * The {@link ItemLabelGenerator} is used to generate display labels for items in the component.
     *
     * @return the {@link ItemLabelGenerator} instance if set, or {@code null} if no generator is defined
     */
    @Nullable
    public ItemLabelGenerator<E> getItemLabelGenerator() {
        return multiValueSelectContext.getItemLabelGenerator();
    }

    /**
     * Sets the item label generator for the component. The item label generator is used to define
     * how the items  are represented as strings in the user interface.
     *
     * @param itemLabelGenerator the item label generator to set, or {@code null} to unset and use the default
     *                           item label generator
     */
    public void setItemLabelGenerator(@Nullable ItemLabelGenerator<E> itemLabelGenerator) {
        multiValueSelectContext.setItemLabelGenerator(itemLabelGenerator);
    }

    /**
     * Returns the list of validators associated with the {@link MultiValueSelectContext}.
     *
     * @return a list of Validator objects used for validation
     */
    public List<Validator<E>> getValidators() {
        return multiValueSelectContext.getValidators();
    }

    /**
     * Sets the list of validators for {@link MultiValueSelectContext}.
     *
     * @param validators the list of validators to be set. Can be null to indicate no validators.
     */
    public void setValidators(@Nullable List<Validator<E>> validators) {
        multiValueSelectContext.setValidators(validators);
    }

    /**
     * Adds a validator to @link MultiValueSelectContext}.
     *
     * @param validator the validator to be added for validating the values
     */
    public void addValidator(Validator<E> validator) {
        multiValueSelectContext.addValidator(validator);
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public void execute() {
        checkTarget();

        View<?> origin = UiComponentUtils.getView((Component) target);
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

    /**
     * @see #setViewConfigurer(Consumer)
     */
    public <V extends View<?>> MultiValueSelectAction<E> withViewConfigurer(@Nullable Consumer<V> viewConfigurer) {
        setViewConfigurer(viewConfigurer);
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
