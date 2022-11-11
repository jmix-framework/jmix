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

import com.vaadin.flow.component.HasValue;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.component.EntityPickerComponent;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.data.DataUnit;
import io.jmix.flowui.data.HasType;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.navigation.*;
import org.springframework.stereotype.Component;

import static com.google.common.base.Preconditions.checkState;
import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * Provides fluent interface for navigating to views.
 */
@Component("flowui_ViewNavigators")
public class ViewNavigators {

    protected DetailViewNavigationProcessor detailViewNavigationProcessor;
    protected ListViewNavigationProcessor listViewNavigationProcessor;
    protected ViewNavigationProcessor viewNavigationProcessor;

    public ViewNavigators(DetailViewNavigationProcessor detailViewNavigationProcessor,
                          ListViewNavigationProcessor listViewNavigationProcessor,
                          ViewNavigationProcessor viewNavigationProcessor) {
        this.detailViewNavigationProcessor = detailViewNavigationProcessor;
        this.listViewNavigationProcessor = listViewNavigationProcessor;
        this.viewNavigationProcessor = viewNavigationProcessor;
    }

    /**
     * Creates a detail view navigator for an entity class.
     * <p>
     * Example of navigating to a view for editing an entity:
     * <pre>{@code
     * viewNavigators.detailView(Customer.class)
     *         .editEntity(customersTable.getSingleSelectedItem())
     *         .withViewClass(CustomerDetailView.class)
     *         .navigate();
     * }</pre>
     * <p>
     * Example of navigating to a view for creating a new entity instance:
     * <pre>{@code
     * viewNavigators.detailView(Customer.class)
     *         .newEntity()
     *         .withViewClass(CustomerDetailView.class)
     *         .navigate();
     * }</pre>
     *
     * @param entityClass edited entity class
     */
    public <E> DetailViewNavigator<E> detailView(Class<E> entityClass) {
        checkNotNullArgument(entityClass);

        return new DetailViewNavigator<>(entityClass, detailViewNavigationProcessor::processNavigation)
                .withBackwardNavigation(true);
    }

    /**
     * Creates a detail view navigator to edit an entity selected in the list component.
     *
     * @param listDataComponent the component which provides a selected entity to edit
     *
     * @see #detailView(Class)
     */
    public <E> DetailViewNavigator<E> detailView(ListDataComponent<E> listDataComponent) {
        checkNotNullArgument(listDataComponent);

        Class<E> beanType = getBeanType(listDataComponent);

        DetailViewNavigator<E> navigator =
                new DetailViewNavigator<>(beanType, detailViewNavigationProcessor::processNavigation);

        E selected = listDataComponent.getSingleSelectedItem();
        if (selected != null) {
            navigator.editEntity(selected);
        }

        return navigator
                .withBackwardNavigation(true);
    }

    /**
     * Creates a detail view navigator to edit an entity selected in the picker component.
     *
     * @param picker the component which provides an entity to edit
     *
     * @see #detailView(Class)
     */
    public <E> DetailViewNavigator<E> detailView(EntityPickerComponent<E> picker) {
        checkNotNullArgument(picker);
        checkState(picker instanceof HasValue,
                "A component must implement " + HasValue.class.getSimpleName());

        Class<E> beanType = getBeanType(picker);

        DetailViewNavigator<E> navigator =
                new DetailViewNavigator<>(beanType, detailViewNavigationProcessor::processNavigation);

        //noinspection unchecked
        E value = ((HasValue<?, E>) picker).getValue();
        if (value != null) {
            navigator.editEntity(value);
        }

        return navigator
                .withBackwardNavigation(true);
    }

    /**
     * Creates a list view navigator for an entity class.
     * <p>
     * Example of navigating to a view for editing an entity and returning to the calling view:
     * <pre>{@code
     * viewNavigators.listView(Customer.class)
     *         .withViewClass(CustomerListView.class)
     *         .withBackwardNavigation(true)
     *         .navigate();
     * }</pre>
     *
     * @param entityClass edited entity class
     */
    public <E> ListViewNavigator<E> listView(Class<E> entityClass) {
        checkNotNullArgument(entityClass);

        return new ListViewNavigator<>(entityClass, listViewNavigationProcessor::processNavigation);
    }

    /**
     * Creates a view navigator.
     *
     * @param viewClass class of the view to navigate to
     */
    public ViewNavigator view(Class<? extends View> viewClass) {
        return new ViewNavigator(viewNavigationProcessor::processNavigation)
                .withViewClass(viewClass);
    }

    /**
     * Creates a view navigator.
     *
     * @param viewId id of the view to navigate to (as set in the {@link ViewController} annotation)
     */
    public ViewNavigator view(String viewId) {
        return new ViewNavigator(viewNavigationProcessor::processNavigation)
                .withViewId(viewId);
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
