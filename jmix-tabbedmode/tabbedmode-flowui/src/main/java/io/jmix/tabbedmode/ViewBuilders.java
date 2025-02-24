/*
 * Copyright 2025 Haulmont.
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

package io.jmix.tabbedmode;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.component.*;
import io.jmix.flowui.data.DataUnit;
import io.jmix.flowui.data.HasType;
import io.jmix.flowui.view.View;
import io.jmix.tabbedmode.builder.*;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkArgument;
import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * Provides fluent interface for opening views.
 */
@org.springframework.stereotype.Component("tabmod_ViewBuilders")
public class ViewBuilders {

    protected final Views views;

    protected final ViewBuilderProcessor viewBuilderProcessor;
    protected final DetailViewBuilderProcessor detailViewBuilderProcessor;
    protected final LookupViewBuilderProcessor lookupViewBuilderProcessor;

    public ViewBuilders(Views views,
                        ViewBuilderProcessor viewBuilderProcessor,
                        DetailViewBuilderProcessor detailViewBuilderProcessor,
                        LookupViewBuilderProcessor lookupViewBuilderProcessor) {
        this.views = views;
        this.viewBuilderProcessor = viewBuilderProcessor;
        this.detailViewBuilderProcessor = detailViewBuilderProcessor;
        this.lookupViewBuilderProcessor = lookupViewBuilderProcessor;
    }

    /**
     * Creates a detail view builder for entity class.
     * <p>
     * Example of opening a view for editing an entity:
     * <pre>{@code
     * viewBuilders.detail(this, User.class)
     *         .editEntity(user)
     *         .open();
     * }</pre>
     * <p>
     * Example of opening a view for creating a new entity instance:
     * <pre>{@code
     * viewBuilders.detail(this, User.class)
     *         .newEntity()
     *         .open();
     * }</pre>
     *
     * @param origin      calling view
     * @param entityClass edited entity class
     * @param <E>         entity type
     * @return detail view builder
     * @see #detail(View, Class, String)
     * @see #detail(View, Class, Class)
     */
    public <E> DetailViewBuilder<E, ?> detail(View<?> origin, Class<E> entityClass) {
        checkNotNullArgument(origin);
        checkNotNullArgument(entityClass);

        return new DetailViewBuilder<>(origin, entityClass,
                detailViewBuilderProcessor::build, this::openView);
    }

    /**
     * Creates a detail view builder for entity class. The opened view is
     * defined by the passed view id.
     * <p>
     * Example of opening a view for editing an entity:
     * <pre>{@code
     * viewBuilders.detail(this, User.class, "User.detail")
     *         .editEntity(user)
     *         .open();
     * }</pre>
     * <p>
     * Example of opening a view for creating a new entity instance:
     * <pre>{@code
     * viewBuilders.detail(this, User.class, "User.detail")
     *         .newEntity()
     *         .open();
     * }</pre>
     *
     * @param origin      calling view
     * @param entityClass edited entity class
     * @param viewId      opened view id
     * @param <E>         entity type
     * @return detail view builder
     * @see #detail(View, Class)
     * @see #detail(View, Class, Class)
     */
    public <E> DetailViewBuilder<E, ?> detail(View<?> origin,
                                              Class<E> entityClass, String viewId) {
        checkNotNullArgument(origin);
        checkNotNullArgument(entityClass);
        checkNotNullArgument(viewId);

        return new DetailViewBuilder<>(origin, entityClass, viewId,
                detailViewBuilderProcessor::build, this::openView);
    }

    /**
     * Creates a detail view builder for entity class. The opened view is
     * defined by the passed view class.
     * <p>
     * Example of opening a view for editing an entity:
     * <pre>{@code
     * viewBuilders.detail(this, User.class, UserDetailView.class)
     *         .editEntity(user)
     *         .withAfterCloseListener(closeEvent -> {
     *             if (closeEvent.closedWith(StandardOutcome.SAVE)) {
     *                 User editedEntity = closeEvent.getSource().getEditedEntity();
     *                 // ...
     *             }
     *         })
     *         .open();
     * }</pre>
     * <p>
     * Example of opening a view for creating a new entity instance:
     * <pre>{@code
     * viewBuilders.detail(this, User.class, UserDetailView.class)
     *         .newEntity()
     *         .withAfterCloseListener(closeEvent -> {
     *             if (closeEvent.closedWith(StandardOutcome.SAVE)) {
     *                 User editedEntity = closeEvent.getSource().getEditedEntity();
     *                 // ...
     *             }
     *         })
     *         .open();
     * }</pre>
     *
     * @param origin      calling view
     * @param entityClass edited entity class
     * @param viewClass   opened view class
     * @param <E>         entity type
     * @param <V>         view type
     * @return detail view builder
     * @see #detail(View, Class)
     * @see #detail(View, Class, String)
     */
    public <E, V extends View<?>> DetailViewBuilder<E, V> detail(View<?> origin,
                                                                 Class<E> entityClass, Class<V> viewClass) {
        checkNotNullArgument(origin);
        checkNotNullArgument(entityClass);
        checkNotNullArgument(viewClass);

        return new DetailViewBuilder<>(origin, entityClass, viewClass,
                detailViewBuilderProcessor::build, this::openView);
    }

    /**
     * Creates a detail view builder using the list component.
     * <p>
     * Example of building a view for editing a currently selected entity:
     * <pre>{@code
     * viewBuilders.detail(usersDataGrid)
     *         .open();
     * }</pre>
     * Example of building a view for creating a new entity instance:
     * <pre>{@code
     * viewBuilders.detail(usersDataGrid)
     *         .newEntity()
     *         .open();
     * }</pre>
     *
     * @param listDataComponent a component containing the list of entities
     * @param <E>               entity type
     * @return detail view builder
     * @see #detail(ListDataComponent, String)
     * @see #detail(ListDataComponent, Class)
     */
    public <E> DetailViewBuilder<E, ?> detail(ListDataComponent<E> listDataComponent) {
        checkNotNullArgument(listDataComponent);
        checkArgument(listDataComponent instanceof Component,
                "A component must implement '%s'", Component.class.getName());

        View<?> origin = UiComponentUtils.getView((Component) listDataComponent);
        Class<E> beanType = getBeanType(listDataComponent);

        DetailViewBuilder<E, ?> builder = new DetailViewBuilder<>(origin, beanType,
                detailViewBuilderProcessor::build, this::openView);

        initDetailBuilder(builder, listDataComponent);

        return builder;
    }

    /**
     * Creates a detail view builder using the list component. The opened view is
     * defined by the passed view id.
     * <p>
     * Example of building a view for editing a currently selected entity:
     * <pre>{@code
     * viewBuilders.detail(usersDataGrid, "User.detail")
     *         .open();
     * }</pre>
     * Example of building a view for creating a new entity instance:
     * <pre>{@code
     * viewBuilders.detail(usersDataGrid, "User.detail")
     *         .newEntity()
     *         .open();
     * }</pre>
     *
     * @param listDataComponent a component containing the list of entities
     * @param viewId            opened view id
     * @param <E>               entity type
     * @return detail view builder
     * @see #detail(ListDataComponent)
     * @see #detail(ListDataComponent, Class)
     */
    public <E> DetailViewBuilder<E, ?> detail(ListDataComponent<E> listDataComponent, String viewId) {
        checkNotNullArgument(listDataComponent);
        checkArgument(listDataComponent instanceof Component,
                "A component must implement '%s'", Component.class.getName());
        checkNotNullArgument(viewId);

        View<?> origin = UiComponentUtils.getView((Component) listDataComponent);
        Class<E> beanType = getBeanType(listDataComponent);

        DetailViewBuilder<E, ?> builder = new DetailViewBuilder<>(origin, beanType, viewId,
                detailViewBuilderProcessor::build, this::openView);

        initDetailBuilder(builder, listDataComponent);

        return builder;
    }

    /**
     * Creates a detail view builder using the list component. The opened view is
     * defined by the passed view class.
     * <p>
     * Example of building a view for editing a currently selected entity:
     * <pre>{@code
     * viewBuilders.detail(usersDataGrid, UserDetailView.class)
     *         .withAfterCloseListener(closeEvent -> {
     *             if (closeEvent.closedWith(StandardOutcome.SAVE)) {
     *                 User editedEntity = closeEvent.getSource().getEditedEntity();
     *                 // ...
     *             }
     *         })
     *         .open();
     * }</pre>
     * Example of building a view for creating a new entity instance:
     * <pre>{@code
     * viewBuilders.detail(usersDataGrid, UserDetailView.class)
     *         .newEntity()
     *         .withAfterCloseListener(closeEvent -> {
     *             if (closeEvent.closedWith(StandardOutcome.SAVE)) {
     *                 User editedEntity = closeEvent.getSource().getEditedEntity();
     *                 // ...
     *             }
     *         })
     *         .open();
     * }</pre>
     *
     * @param listDataComponent a component containing the list of entities
     * @param viewClass         opened view class
     * @param <E>               entity type
     * @param <V>               view type
     * @return detail view builder
     * @see #detail(ListDataComponent)
     * @see #detail(ListDataComponent, String)
     */
    public <E, V extends View<?>> DetailViewBuilder<E, V> detail(ListDataComponent<E> listDataComponent,
                                                                 Class<V> viewClass) {
        checkNotNullArgument(listDataComponent);
        checkArgument(listDataComponent instanceof Component,
                "A component must implement '%s'", Component.class.getName());
        checkNotNullArgument(viewClass);

        View<?> origin = UiComponentUtils.getView((Component) listDataComponent);
        Class<E> beanType = getBeanType(listDataComponent);

        DetailViewBuilder<E, V> builder = new DetailViewBuilder<>(origin, beanType, viewClass,
                detailViewBuilderProcessor::build, this::openView);

        initDetailBuilder(builder, listDataComponent);

        return builder;
    }

    protected <E, V extends View<?>> void initDetailBuilder(DetailViewBuilder<E, V> builder,
                                                            ListDataComponent<E> listDataComponent) {
        builder.withListDataComponent(listDataComponent);

        E selected = listDataComponent.getSingleSelectedItem();
        if (selected != null) {
            builder.editEntity(selected);
        }
    }

    /**
     * Creates a detail view builder using the entity picker component.
     * <p>
     * Example of building a view for editing a currently selected entity:
     * <pre>{@code
     * viewBuilders.detail(userPicker)
     *         .open();
     * }</pre>
     * Example of building a view for creating a new entity instance:
     * <pre>{@code
     * viewBuilders.detail(userPicker)
     *         .newEntity()
     *         .open();
     * }</pre>
     *
     * @param picker entity picker component
     * @param <E>    entity type
     * @return detail view builder
     * @see #detail(EntityPickerComponent, String)
     * @see #detail(EntityPickerComponent, Class)
     */
    @SuppressWarnings("unchecked")
    public <E> DetailViewBuilder<E, ?> detail(EntityPickerComponent<E> picker) {
        checkNotNullArgument(picker);
        checkArgument(picker instanceof HasValue,
                "A component must implement '%s'", HasValue.class.getName());
        checkArgument(picker instanceof Component,
                "A component must implement '%s'", Component.class.getName());

        View<?> origin = UiComponentUtils.getView((Component) picker);
        Class<E> beanType = getBeanType(picker);

        DetailViewBuilder<E, ?> builder = new DetailViewBuilder<>(origin, beanType,
                detailViewBuilderProcessor::build, this::openView);

        initDetailBuilder(builder, (HasValue<?, E>) picker);

        return builder;
    }

    /**
     * Creates a detail view builder using the entity picker component. The opened view is
     * defined by the passed view id.
     * <p>
     * Example of building a view for editing a currently selected entity:
     * <pre>{@code
     * viewBuilders.detail(userPicker, "User. detail")
     *         .open();
     * }</pre>
     * Example of building a view for creating a new entity instance:
     * <pre>{@code
     * viewBuilders.detail(userPicker, "User. detail")
     *         .newEntity()
     *         .open();
     * }</pre>
     *
     * @param picker entity picker component
     * @param viewId opened view id
     * @param <E>    entity type
     * @return detail view builder
     * @see #detail(EntityPickerComponent)
     * @see #detail(EntityPickerComponent, Class)
     */
    @SuppressWarnings("unchecked")
    public <E> DetailViewBuilder<E, ?> detail(EntityPickerComponent<E> picker, String viewId) {
        checkNotNullArgument(picker);
        checkArgument(picker instanceof HasValue,
                "A component must implement '%s'", HasValue.class.getName());
        checkArgument(picker instanceof Component,
                "A component must implement '%s'", Component.class.getName());
        checkNotNullArgument(viewId);

        View<?> origin = UiComponentUtils.getView((Component) picker);
        Class<E> beanType = getBeanType(picker);

        DetailViewBuilder<E, ?> builder = new DetailViewBuilder<>(origin, beanType, viewId,
                detailViewBuilderProcessor::build, this::openView);

        initDetailBuilder(builder, (HasValue<?, E>) picker);

        return builder;
    }

    /**
     * Creates a detail view builder using the entity picker component. The opened view is
     * defined by the passed view class.
     * <p>
     * Example of building a view for editing a currently selected entity:
     * <pre>{@code
     * viewBuilders.detail(userPicker, UserDetailView.class)
     *         .withAfterCloseListener(closeEvent -> {
     *             if (closeEvent.closedWith(StandardOutcome.SAVE)) {
     *                 User editedEntity = closeEvent.getSource().getEditedEntity();
     *                 // ...
     *             }
     *         })
     *         .open();
     * }</pre>
     * Example of building a view for creating a new entity instance:
     * <pre>{@code
     * viewBuilders.detail(userPicker, UserDetailView.class)
     *         .newEntity()
     *         .withAfterCloseListener(closeEvent -> {
     *             if (closeEvent.closedWith(StandardOutcome.SAVE)) {
     *                 User editedEntity = closeEvent.getSource().getEditedEntity();
     *                 // ...
     *             }
     *         })
     *         .open();
     * }</pre>
     *
     * @param picker    entity picker component
     * @param viewClass opened view class
     * @param <E>       entity type
     * @param <V>       view type
     * @return detail view builder
     * @see #detail(EntityPickerComponent)
     * @see #detail(EntityPickerComponent, String)
     */
    @SuppressWarnings("unchecked")
    public <E, V extends View<?>> DetailViewBuilder<E, V> detail(EntityPickerComponent<E> picker,
                                                                 Class<V> viewClass) {
        checkNotNullArgument(picker);
        checkArgument(picker instanceof HasValue,
                "A component must implement '%s'", HasValue.class.getName());
        checkArgument(picker instanceof Component,
                "A component must implement '%s'", Component.class.getName());
        checkNotNullArgument(viewClass);

        View<?> origin = UiComponentUtils.getView((Component) picker);
        Class<E> beanType = getBeanType(picker);

        DetailViewBuilder<E, V> builder = new DetailViewBuilder<>(origin, beanType, viewClass,
                detailViewBuilderProcessor::build, this::openView);

        initDetailBuilder(builder, (HasValue<?, E>) picker);

        return builder;
    }

    protected <E, V extends View<?>> void initDetailBuilder(DetailViewBuilder<E, V> builder,
                                                            HasValue<?, E> valueComponent) {
        builder.withField(valueComponent);

        E value = valueComponent.getValue();
        if (value != null) {
            builder.editEntity(value);
        }
    }

    /**
     * Creates a lookup view builder for entity class.
     * <p>
     * Example of building a lookup view for adding an instance to a data container:
     * <pre>{@code
     * viewBuilders.lookup(this, User.class)
     *         .withContainer(usersDc)
     *         .open();
     * }</pre>
     *
     * @param origin      calling view
     * @param entityClass entity class
     * @param <E>         entity type
     * @return lookup builder
     * @see #lookup(View, Class, String)
     * @see #lookup(View, Class, Class)
     */
    public <E> LookupViewBuilder<E, ?> lookup(View<?> origin, Class<E> entityClass) {
        checkNotNullArgument(origin);
        checkNotNullArgument(entityClass);

        return new LookupViewBuilder<>(origin, entityClass,
                lookupViewBuilderProcessor::build, this::openView);
    }

    /**
     * Creates a lookup view builder for entity class. The opened view is
     * defined by the passed view id.
     * <p>
     * Example of building a lookup view for adding an instance to a data container:
     * <pre>{@code
     * viewBuilders.lookup(this, User.class, "User.list")
     *         .withContainer(usersDc)
     *         .open();
     * }</pre>
     *
     * @param origin      calling view
     * @param entityClass entity class
     * @param viewId      opened view id
     * @param <E>         entity type
     * @return lookup builder
     * @see #lookup(View, Class)
     * @see #lookup(View, Class, Class)
     */
    public <E> LookupViewBuilder<E, ?> lookup(View<?> origin,
                                              Class<E> entityClass, String viewId) {
        checkNotNullArgument(origin);
        checkNotNullArgument(entityClass);
        checkNotNullArgument(viewId);

        return new LookupViewBuilder<>(origin, entityClass, viewId,
                lookupViewBuilderProcessor::build, this::openView);
    }

    /**
     * Creates a lookup view builder for entity class. The opened view is
     * defined by the passed view class.
     * <p>
     * Example of building a lookup view for adding an instance to a data container:
     * <pre>{@code
     * viewBuilders.lookup(this, User.class, UserListView.class)
     *         .withContainer(usersDc)
     *         .open();
     * }</pre>
     *
     * @param origin      calling view
     * @param entityClass entity class
     * @param viewClass   opened view class
     * @param <E>         entity type
     * @param <V>         view type
     * @return lookup builder
     * @see #lookup(View, Class)
     * @see #lookup(View, Class, String)
     */
    public <E, V extends View<?>> LookupViewBuilder<E, V> lookup(View<?> origin,
                                                                 Class<E> entityClass, Class<V> viewClass) {
        checkNotNullArgument(origin);
        checkNotNullArgument(entityClass);
        checkNotNullArgument(viewClass);

        return new LookupViewBuilder<>(origin, entityClass, viewClass,
                lookupViewBuilderProcessor::build, this::openView);
    }

    /**
     * Creates a lookup view builder using the list component.
     * <p>
     * Example of building a lookup view for adding an instance to a list component:
     * <pre>{@code
     * viewBuilders.lookup(usersDataGrid)
     *         .open();
     * }</pre>
     *
     * @param listDataComponent a component containing the list of entities
     * @param <E>               entity type
     * @return lookup builder
     * @see #lookup(ListDataComponent, String)
     * @see #lookup(ListDataComponent, Class)
     */
    public <E> LookupViewBuilder<E, ?> lookup(ListDataComponent<E> listDataComponent) {
        checkNotNullArgument(listDataComponent);
        checkArgument(listDataComponent instanceof Component,
                "A component must implement '%s'", Component.class.getName());

        View<?> origin = UiComponentUtils.getView((Component) listDataComponent);
        Class<E> beanType = getBeanType(listDataComponent);

        LookupViewBuilder<E, ?> builder =
                new LookupViewBuilder<>(origin, beanType,
                        lookupViewBuilderProcessor::build, this::openView);

        builder.withListDataComponent(listDataComponent);

        return builder;
    }

    /**
     * Creates a lookup view builder using the list component. The opened view is
     * defined by the passed view id.
     * <p>
     * Example of building a lookup view for adding an instance to a list component:
     * <pre>{@code
     * viewBuilders.lookup(usersDataGrid, "User.list")
     *         .open();
     * }</pre>
     *
     * @param listDataComponent a component containing the list of entities
     * @param viewId            opened view id
     * @param <E>               entity type
     * @return lookup builder
     * @see #lookup(ListDataComponent)
     * @see #lookup(ListDataComponent, Class)
     */
    public <E> LookupViewBuilder<E, ?> lookup(ListDataComponent<E> listDataComponent, String viewId) {
        checkNotNullArgument(listDataComponent);
        checkArgument(listDataComponent instanceof Component,
                "A component must implement '%s'", Component.class.getName());
        checkNotNullArgument(viewId);

        View<?> origin = UiComponentUtils.getView((Component) listDataComponent);
        Class<E> beanType = getBeanType(listDataComponent);

        LookupViewBuilder<E, ?> builder = new LookupViewBuilder<>(origin, beanType, viewId,
                lookupViewBuilderProcessor::build, this::openView);

        builder.withListDataComponent(listDataComponent);

        return builder;
    }

    /**
     * Creates a lookup view builder using the list component. The opened view is
     * defined by the passed view class.
     * <p>
     * Example of building a lookup view for adding an instance to a list component:
     * <pre>{@code
     * viewBuilders.lookup(usersDataGrid, UserListView.class)
     *         .open();
     * }</pre>
     *
     * @param listDataComponent a component containing the list of entities
     * @param viewClass         opened view class
     * @param <E>               entity type
     * @param <V>               view type
     * @return lookup builder
     * @see #lookup(ListDataComponent)
     * @see #lookup(ListDataComponent, String)
     */
    public <E, V extends View<?>> LookupViewBuilder<E, V> lookup(ListDataComponent<E> listDataComponent,
                                                                 Class<V> viewClass) {
        checkNotNullArgument(listDataComponent);
        checkArgument(listDataComponent instanceof Component,
                "A component must implement '%s'", Component.class.getName());
        checkNotNullArgument(viewClass);

        View<?> origin = UiComponentUtils.getView((Component) listDataComponent);
        Class<E> beanType = getBeanType(listDataComponent);

        LookupViewBuilder<E, V> builder = new LookupViewBuilder<>(origin, beanType, viewClass,
                lookupViewBuilderProcessor::build, this::openView);

        builder.withListDataComponent(listDataComponent);

        return builder;
    }

    /**
     * Creates a lookup view builder using the entity picker component.
     * <p>
     * Example of building a lookup view for adding an instance to a list component:
     * <pre>{@code
     * viewBuilders.lookup(userPicker)
     *         .open();
     * }</pre>
     *
     * @param picker entity picker component
     * @param <E>    entity type
     * @return lookup builder
     * @see #lookup(EntityPickerComponent, String)
     * @see #lookup(EntityPickerComponent, Class)
     */
    public <E> LookupViewBuilder<E, ?> lookup(EntityPickerComponent<E> picker) {
        checkNotNullArgument(picker);
        checkArgument(picker instanceof HasValue,
                "A component must implement '%s'", HasValue.class.getName());
        checkArgument(picker instanceof Component,
                "A component must implement '%s'", Component.class.getName());

        View<?> origin = UiComponentUtils.getView((Component) picker);
        Class<E> beanType = getBeanType(picker);

        LookupViewBuilder<E, ?> builder = new LookupViewBuilder<>(origin, beanType,
                lookupViewBuilderProcessor::build, this::openView);

        //noinspection unchecked
        builder.withField(((HasValue<?, E>) picker));

        return builder;
    }

    /**
     * Creates a lookup view builder using the entity picker component. The opened view is
     * defined by the passed view id.
     * <p>
     * Example of building a lookup view for adding an instance to a list component:
     * <pre>{@code
     * viewBuilders.lookup(userPicker, "User.list")
     *         .open();
     * }</pre>
     *
     * @param picker entity picker component
     * @param viewId opened view id
     * @param <E>    entity type
     * @return lookup builder
     * @see #lookup(EntityPickerComponent)
     * @see #lookup(EntityPickerComponent, Class)
     */
    public <E> LookupViewBuilder<E, ?> lookup(EntityPickerComponent<E> picker, String viewId) {
        checkNotNullArgument(picker);
        checkArgument(picker instanceof HasValue,
                "A component must implement '%s'", HasValue.class.getName());
        checkArgument(picker instanceof Component,
                "A component must implement '%s'", Component.class.getName());
        checkNotNullArgument(viewId);

        View<?> origin = UiComponentUtils.getView((Component) picker);
        Class<E> beanType = getBeanType(picker);

        LookupViewBuilder<E, ?> builder = new LookupViewBuilder<>(origin, beanType, viewId,
                lookupViewBuilderProcessor::build, this::openView);

        //noinspection unchecked
        builder.withField(((HasValue<?, E>) picker));

        return builder;
    }

    /**
     * Creates a lookup view builder using the entity picker component. The opened view is
     * defined by the passed view class.
     * <p>
     * Example of building a lookup view for adding an instance to a list component:
     * <pre>{@code
     * viewBuilders.lookup(userPicker, UserListView.class)
     *         .open();
     * }</pre>
     *
     * @param picker    entity picker component
     * @param viewClass opened view class
     * @param <E>       entity type
     * @param <V>       view type
     * @return lookup builder
     * @see #lookup(EntityPickerComponent)
     * @see #lookup(EntityPickerComponent, String)
     */
    public <E, V extends View<?>> LookupViewBuilder<E, V> lookup(EntityPickerComponent<E> picker,
                                                                 Class<V> viewClass) {
        checkNotNullArgument(picker);
        checkArgument(picker instanceof HasValue,
                "A component must implement '%s'", HasValue.class.getName());
        checkArgument(picker instanceof Component,
                "A component must implement '%s'", Component.class.getName());
        checkNotNullArgument(viewClass);

        View<?> origin = UiComponentUtils.getView((Component) picker);
        Class<E> beanType = getBeanType(picker);

        LookupViewBuilder<E, V> builder = new LookupViewBuilder<>(origin, beanType, viewClass,
                lookupViewBuilderProcessor::build, this::openView);

        //noinspection unchecked
        builder.withField(((HasValue<?, E>) picker));

        return builder;
    }

    /**
     * Creates a lookup view builder using the entity multi picker component.
     * <p>
     * Example of building a lookup view for adding an instance to a list component:
     * <pre>{@code
     * viewBuilders.lookup(usersPicker)
     *         .open();
     * }</pre>
     *
     * @param picker entity multi picker component
     * @param <E>    entity type
     * @return lookup builder
     * @see #lookup(EntityMultiPickerComponent, String)
     * @see #lookup(EntityMultiPickerComponent, Class)
     */
    public <E> LookupViewBuilder<E, ?> lookup(EntityMultiPickerComponent<E> picker) {
        checkNotNullArgument(picker);
        checkArgument(picker instanceof HasValue,
                "A component must implement '%s'", HasValue.class.getName());
        checkArgument(picker instanceof Component,
                "A component must implement '%s'", Component.class.getName());

        View<?> origin = UiComponentUtils.getView((Component) picker);
        Class<E> beanType = getBeanType(picker);

        LookupViewBuilder<E, ?> builder = new LookupViewBuilder<>(origin, beanType,
                lookupViewBuilderProcessor::build, this::openView);

        //noinspection unchecked
        builder.withMultiValueField(((HasValue<?, Collection<E>>) picker));

        return builder;
    }

    /**
     * Creates a lookup view builder using the entity multi picker component. The opened view is
     * defined by the passed view id.
     * <p>
     * Example of building a lookup view for adding an instance to a list component:
     * <pre>{@code
     * viewBuilders.lookup(usersPicker, "User.list")
     *         .open();
     * }</pre>
     *
     * @param picker entity multi picker component
     * @param viewId opened view id
     * @param <E>    entity type
     * @return lookup builder
     * @see #lookup(EntityMultiPickerComponent)
     * @see #lookup(EntityMultiPickerComponent, Class)
     */
    public <E> LookupViewBuilder<E, ?> lookup(EntityMultiPickerComponent<E> picker,
                                              String viewId) {
        checkNotNullArgument(picker);
        checkArgument(picker instanceof HasValue,
                "A component must implement '%s'", HasValue.class.getName());
        checkArgument(picker instanceof Component,
                "A component must implement '%s'", Component.class.getName());
        checkNotNullArgument(viewId);

        View<?> origin = UiComponentUtils.getView((Component) picker);
        Class<E> beanType = getBeanType(picker);

        LookupViewBuilder<E, ?> builder = new LookupViewBuilder<>(origin, beanType, viewId,
                lookupViewBuilderProcessor::build, this::openView);

        //noinspection unchecked
        builder.withMultiValueField(((HasValue<?, Collection<E>>) picker));

        return builder;
    }

    /**
     * Creates a lookup view builder using the entity multi picker component. The opened view is
     * defined by the passed view class.
     * <p>
     * Example of building a lookup view for adding an instance to a list component:
     * <pre>{@code
     * viewBuilders.lookup(usersPicker, UserListView.class)
     *         .open();
     * }</pre>
     *
     * @param picker    entity multi picker component
     * @param viewClass opened view class
     * @param <E>       entity type
     * @param <V>       view type
     * @return lookup builder
     * @see #lookup(EntityMultiPickerComponent)
     * @see #lookup(EntityMultiPickerComponent, String)
     */
    public <E, V extends View<?>> LookupViewBuilder<E, V> lookup(EntityMultiPickerComponent<E> picker,
                                                                 Class<V> viewClass) {
        checkNotNullArgument(picker);
        checkArgument(picker instanceof HasValue,
                "A component must implement '%s'", HasValue.class.getName());
        checkArgument(picker instanceof Component,
                "A component must implement '%s'", Component.class.getName());
        checkNotNullArgument(viewClass);

        View<?> origin = UiComponentUtils.getView((Component) picker);
        Class<E> beanType = getBeanType(picker);

        LookupViewBuilder<E, V> builder = new LookupViewBuilder<>(origin, beanType, viewClass,
                lookupViewBuilderProcessor::build, this::openView);

        //noinspection unchecked
        builder.withMultiValueField(((HasValue<?, Collection<E>>) picker));

        return builder;
    }

    /**
     * Creates a view builder. The opened view is
     * defined by the passed view class.
     * <p>
     * Example of opening a view:
     * <pre>{@code
     * viewBuilders.view(this, SandboxView.class)
     *                 .open();
     * }</pre>
     *
     * @param origin    calling view
     * @param viewClass opened view class
     * @param <V>       view type
     * @return view builder
     */
    public <V extends View<?>> ViewBuilder<V> view(View<?> origin, Class<V> viewClass) {
        checkNotNullArgument(origin);
        checkNotNullArgument(viewClass);

        return new ViewBuilder<>(origin, viewClass, viewBuilderProcessor::build, this::openView);
    }

    /**
     * Creates a view builder. The opened view is
     * defined by the passed view id.
     * <p>
     * Example of opening a view:
     * <pre>{@code
     * viewBuilders.view(this, "FooView")
     *                 .open();
     * }</pre>
     *
     * @param origin calling view
     * @param viewId opened view id
     * @return view builder
     */
    public ViewBuilder<?> view(View<?> origin, String viewId) {
        checkNotNullArgument(origin);
        checkNotNullArgument(viewId);

        return new ViewBuilder<>(origin, viewId, viewBuilderProcessor::build, this::openView);
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

    protected <E> Class<E> getBeanType(SupportsMetaClass component) {
        MetaClass metaClass = component.getMetaClass();
        if (metaClass == null) {
            throw new IllegalStateException(String.format("Component '%s' is not bound to data " +
                    "or unable to determine type of items", component));
        }

        return metaClass.getJavaClass();
    }

    protected void openView(ViewOpeningContext context) {
        views.open(context);
    }
}
