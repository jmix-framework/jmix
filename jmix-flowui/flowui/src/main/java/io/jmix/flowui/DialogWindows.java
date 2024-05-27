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

package io.jmix.flowui;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.component.EntityMultiPickerComponent;
import io.jmix.flowui.component.EntityPickerComponent;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.data.DataUnit;
import io.jmix.flowui.data.HasType;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.builder.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkState;
import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * Provides fluent interface for opening views in dialog windows.
 */
@org.springframework.stereotype.Component("flowui_DialogWindows")
public class DialogWindows {

    protected WindowBuilderProcessor windowBuilderProcessor;
    protected DetailWindowBuilderProcessor detailBuilderProcessor;
    protected LookupWindowBuilderProcessor lookupBuilderProcessor;
    protected ObjectProvider<OpenedDialogWindows> openedDialogWindows;

    public DialogWindows(WindowBuilderProcessor windowBuilderProcessor,
                         DetailWindowBuilderProcessor detailBuilderProcessor,
                         LookupWindowBuilderProcessor lookupBuilderProcessor,
                         @Autowired(required = false) ObjectProvider<OpenedDialogWindows> openedDialogWindows) {
        this.windowBuilderProcessor = windowBuilderProcessor;
        this.detailBuilderProcessor = detailBuilderProcessor;
        this.lookupBuilderProcessor = lookupBuilderProcessor;
        this.openedDialogWindows = openedDialogWindows;
    }

    /**
     * Creates a detail view builder for entity class.
     * <p>
     * Example of opening a view for editing an entity:
     * <pre>{@code
     * DialogWindow<CustomerDetailView> dialogWindow = dialogWindows.detail(this, Customer.class)
     *         .withViewClass(CustomerDetailView.class)
     *         .editEntity(customer)
     *         .withAfterCloseListener(closeEvent -> {
     *             if (closeEvent.closedWith(StandardOutcome.SAVE)) {
     *                 Customer editedCustomer = closeEvent.getView().getEditedEntity();
     *                 // ...
     *             }
     *         })
     *         .open();
     * }</pre>
     * <p>
     * Example of opening a view for creating a new entity instance:
     * <pre>{@code
     * DialogWindow<CustomerDetailView> dialogWindow = dialogWindows.detail(this, Customer.class)
     *         .withViewClass(CustomerDetailView.class)
     *         .newEntity()
     *         .withAfterCloseListener(closeEvent -> {
     *             if (closeEvent.closedWith(StandardOutcome.SAVE)) {
     *                 Customer newCustomer = closeEvent.getView().getEditedEntity();
     *                 // ...
     *             }
     *         })
     *         .open();
     * }</pre>
     *
     * @param origin      calling view
     * @param entityClass edited entity class
     */
    public <E, V extends View<?>> DetailWindowBuilder<E, V> detail(View<?> origin, Class<E> entityClass) {
        checkNotNullArgument(origin);
        checkNotNullArgument(entityClass);

        return new DetailWindowBuilder<>(origin, entityClass, detailBuilderProcessor::build);
    }

    /**
     * Creates a detail view builder to edit an entity selected in the list component.
     *
     * @param listDataComponent the component which provides a selected entity to edit
     * @see #detail(View, Class)
     */
    public <E, V extends View<?>> DetailWindowBuilder<E, V> detail(ListDataComponent<E> listDataComponent) {
        checkNotNullArgument(listDataComponent);

        View<?> origin = UiComponentUtils.getView((Component) listDataComponent);
        Class<E> beanType = getBeanType(listDataComponent);

        DetailWindowBuilder<E, V> builder =
                new DetailWindowBuilder<>(origin, beanType, detailBuilderProcessor::build);

        builder.withListDataComponent(listDataComponent);

        E selected = listDataComponent.getSingleSelectedItem();
        if (selected != null) {
            builder.editEntity(selected);
        }

        return builder;
    }

    /**
     * Creates a detail view builder to edit an entity selected in the picker component.
     *
     * @param picker the component which provides an entity to edit
     * @see #detail(View, Class)
     */
    @SuppressWarnings("unchecked")
    public <E, V extends View<?>> DetailWindowBuilder<E, V> detail(EntityPickerComponent<E> picker) {
        checkNotNullArgument(picker);
        checkState(picker instanceof HasValue,
                "A component must implement " + HasValue.class.getSimpleName());

        View<?> origin = UiComponentUtils.getView((Component) picker);
        Class<E> beanType = getBeanType(picker);

        DetailWindowBuilder<E, V> builder =
                new DetailWindowBuilder<>(origin, beanType, detailBuilderProcessor::build);

        builder.withField(((HasValue<?, E>) picker));

        E value = ((HasValue<?, E>) picker).getValue();
        if (value != null) {
            builder.editEntity(value);
        }

        return builder;
    }

    /**
     * Creates a lookup view builder for an entity class.
     * <p>
     * Example of opening a view for selecting an entity:
     * <pre>{@code
     * DialogWindow<CustomerListView> dialogWindow = dialogWindows.lookup(this, Customer.class)
     *         .withViewClass(CustomerListView.class)
     *         .withSelectHandler(customers -> {
     *             // ...
     *         })
     *         .open();
     * }</pre>
     *
     * @param origin      calling view
     * @param entityClass entity class
     */
    public <E, V extends View<?>> LookupWindowBuilder<E, V> lookup(View<?> origin, Class<E> entityClass) {
        checkNotNullArgument(origin);
        checkNotNullArgument(entityClass);

        return new LookupWindowBuilder<>(origin, entityClass, lookupBuilderProcessor::build);
    }

    /**
     * Creates a lookup view builder to select entities and add them to the data container of the list component.
     *
     * @param listDataComponent the list component
     * @see #detail(View, Class)
     */
    public <E, V extends View<?>> LookupWindowBuilder<E, V> lookup(ListDataComponent<E> listDataComponent) {
        checkNotNullArgument(listDataComponent);

        View<?> origin = UiComponentUtils.getView((Component) listDataComponent);
        Class<E> beanType = getBeanType(listDataComponent);

        LookupWindowBuilder<E, V> builder =
                new LookupWindowBuilder<>(origin, beanType, lookupBuilderProcessor::build);

        builder.withListDataComponent(listDataComponent);

        return builder;
    }

    /**
     * Creates a lookup view builder to select an entity and set it to the picker component.
     *
     * @param picker the picker component
     * @see #detail(View, Class)
     */
    @SuppressWarnings("unchecked")
    public <E, V extends View<?>> LookupWindowBuilder<E, V> lookup(EntityPickerComponent<E> picker) {
        checkNotNullArgument(picker);
        checkState(picker instanceof HasValue,
                "A component must implement " + HasValue.class.getSimpleName());

        View<?> origin = UiComponentUtils.getView((Component) picker);
        Class<E> beanType = getBeanType(picker);

        LookupWindowBuilder<E, V> builder =
                new LookupWindowBuilder<>(origin, beanType, lookupBuilderProcessor::build);

        if (picker instanceof EntityMultiPickerComponent) {
            builder.withMultiValueField(((HasValue<?, Collection<E>>) picker));
        } else {
            builder.withField(((HasValue<?, E>) picker));
        }

        return builder;
    }

    /**
     * Creates a view builder.
     *
     * @param origin    calling view
     * @param viewClass opened view class
     */
    public <V extends View<?>> WindowBuilder<V> view(View<?> origin, Class<V> viewClass) {
        return new WindowBuilder<>(origin, viewClass, windowBuilderProcessor::build);
    }

    /**
     * @return the object that provides information about opened dialogs
     * @throws IllegalStateException if {@link OpenedDialogWindows} is not available
     */
    public OpenedDialogWindows getOpenedDialogWindows() {
        return openedDialogWindows.stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(OpenedDialogWindows.class.getSimpleName() +
                        " is not available since no active Vaadin session"));
    }

    /**
     * Creates a view builder.
     *
     * @param origin calling view
     * @param viewId id of the opened view (as set in the {@link ViewController} annotation)
     */
    public WindowBuilder<View<?>> view(View<?> origin, String viewId) {
        return new WindowBuilder<>(origin, viewId, windowBuilderProcessor::build);
    }

    protected <E> Class<E> getBeanType(ListDataComponent<E> listDataComponent) {
        DataUnit items = listDataComponent.getItems();
        if (items instanceof HasType) {
            //noinspection unchecked
            return ((HasType<E>) items).getType();
        } else {
            throw new IllegalStateException(String.format("Component '%s' is not bound to data " +
                    "or unable to determine type of items", listDataComponent));
        }
    }

    protected <E> Class<E> getBeanType(EntityPickerComponent<E> picker) {
        MetaClass metaClass = picker.getMetaClass();
        if (metaClass == null) {
            throw new IllegalStateException(String.format("Component '%s' is not bound to data " +
                    "or unable to determine type of items", picker));
        }

        return metaClass.getJavaClass();
    }
}
