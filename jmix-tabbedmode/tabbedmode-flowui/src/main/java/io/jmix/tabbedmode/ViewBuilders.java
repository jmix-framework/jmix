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

    public <E> DetailViewBuilder<E, ?> detail(View<?> origin, Class<E> entityClass) {
        checkNotNullArgument(origin);
        checkNotNullArgument(entityClass);

        return new DetailViewBuilder<>(origin, entityClass,
                detailViewBuilderProcessor::build, this::openView);
    }

    public <E> DetailViewBuilder<E, ?> detail(View<?> origin,
                                              Class<E> entityClass, String viewId) {
        checkNotNullArgument(origin);
        checkNotNullArgument(entityClass);
        checkNotNullArgument(viewId);

        return new DetailViewBuilder<>(origin, entityClass, viewId,
                detailViewBuilderProcessor::build, this::openView);
    }

    public <E, V extends View<?>> DetailViewBuilder<E, V> detail(View<?> origin,
                                                                 Class<E> entityClass, Class<V> viewClass) {
        checkNotNullArgument(origin);
        checkNotNullArgument(entityClass);
        checkNotNullArgument(viewClass);

        return new DetailViewBuilder<>(origin, entityClass, viewClass,
                detailViewBuilderProcessor::build, this::openView);
    }

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

    public <E> LookupViewBuilder<E, ?> lookup(View<?> origin, Class<E> entityClass) {
        checkNotNullArgument(origin);
        checkNotNullArgument(entityClass);

        return new LookupViewBuilder<>(origin, entityClass,
                lookupViewBuilderProcessor::build, this::openView);
    }

    public <E> LookupViewBuilder<E, ?> lookup(View<?> origin,
                                              Class<E> entityClass, String viewId) {
        checkNotNullArgument(origin);
        checkNotNullArgument(entityClass);
        checkNotNullArgument(viewId);

        return new LookupViewBuilder<>(origin, entityClass, viewId,
                lookupViewBuilderProcessor::build, this::openView);
    }

    public <E, V extends View<?>> LookupViewBuilder<E, V> lookup(View<?> origin,
                                                                 Class<E> entityClass, Class<V> viewClass) {
        checkNotNullArgument(origin);
        checkNotNullArgument(entityClass);
        checkNotNullArgument(viewClass);

        return new LookupViewBuilder<>(origin, entityClass, viewClass,
                lookupViewBuilderProcessor::build, this::openView);
    }

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

    public <V extends View<?>> ViewBuilder<V> view(View<?> origin, Class<V> viewClass) {
        checkNotNullArgument(origin);
        checkNotNullArgument(viewClass);

        return new ViewBuilder<>(origin, viewClass, viewBuilderProcessor::build, this::openView);
    }

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
