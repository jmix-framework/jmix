package io.jmix.flowui;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.data.DataUnit;
import io.jmix.flowui.data.HasType;
import io.jmix.flowui.component.EntityPickerComponent;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.builder.*;

import static com.google.common.base.Preconditions.checkState;
import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

@org.springframework.stereotype.Component("flowui_DialogWindowBuilders")
public class DialogWindowBuilders {

    protected WindowBuilderProcessor windowBuilderProcessor;
    protected DetailWindowBuilderProcessor detailBuilderProcessor;
    protected LookupWindowBuilderProcessor lookupBuilderProcessor;

    public DialogWindowBuilders(WindowBuilderProcessor windowBuilderProcessor,
                                DetailWindowBuilderProcessor detailBuilderProcessor,
                                LookupWindowBuilderProcessor lookupBuilderProcessor) {
        this.windowBuilderProcessor = windowBuilderProcessor;
        this.detailBuilderProcessor = detailBuilderProcessor;
        this.lookupBuilderProcessor = lookupBuilderProcessor;
    }

    public <E, S extends View<?>> DetailWindowBuilder<E, S> detail(View<?> origin, Class<E> entityClass) {
        checkNotNullArgument(origin);
        checkNotNullArgument(entityClass);

        return new DetailWindowBuilder<>(origin, entityClass, detailBuilderProcessor::build);
    }

    public <E, S extends View<?>> DetailWindowBuilder<E, S> detail(ListDataComponent<E> listDataComponent) {
        checkNotNullArgument(listDataComponent);

        View<?> origin = getView((Component) listDataComponent);
        Class<E> beanType = getBeanType(listDataComponent);

        DetailWindowBuilder<E, S> builder =
                new DetailWindowBuilder<>(origin, beanType, detailBuilderProcessor::build);

        builder.withListDataComponent(listDataComponent);

        E selected = listDataComponent.getSingleSelectedItem();
        if (selected != null) {
            builder.editEntity(selected);
        }

        return builder;
    }

    @SuppressWarnings("unchecked")
    public <E, S extends View<?>> DetailWindowBuilder<E, S> detail(EntityPickerComponent<E> picker) {
        checkNotNullArgument(picker);
        checkState(picker instanceof HasValue,
                "A component must implement " + HasValue.class.getSimpleName());

        View<?> origin = getView((Component) picker);
        Class<E> beanType = getBeanType(picker);

        DetailWindowBuilder<E, S> builder =
                new DetailWindowBuilder<>(origin, beanType, detailBuilderProcessor::build);

        builder.withField(((HasValue<?, E>) picker));

        E value = ((HasValue<?, E>) picker).getValue();
        if (value != null) {
            builder.editEntity(value);
        }

        return builder;
    }

    public <E, S extends View<?>> LookupWindowBuilder<E, S> lookup(View<?> origin, Class<E> entityClass) {
        checkNotNullArgument(origin);
        checkNotNullArgument(entityClass);

        return new LookupWindowBuilder<>(origin, entityClass, lookupBuilderProcessor::build);
    }

    public <E, S extends View<?>> LookupWindowBuilder<E, S> lookup(ListDataComponent<E> listDataComponent) {
        checkNotNullArgument(listDataComponent);

        View<?> origin = getView((Component) listDataComponent);
        Class<E> beanType = getBeanType(listDataComponent);

        LookupWindowBuilder<E, S> builder =
                new LookupWindowBuilder<>(origin, beanType, lookupBuilderProcessor::build);

        builder.withListDataComponent(listDataComponent);

        return builder;
    }

    @SuppressWarnings("unchecked")
    public <E, S extends View<?>> LookupWindowBuilder<E, S> lookup(EntityPickerComponent<E> picker) {
        checkNotNullArgument(picker);
        checkState(picker instanceof HasValue,
                "A component must implement " + HasValue.class.getSimpleName());

        View<?> origin = getView((Component) picker);
        Class<E> beanType = getBeanType(picker);

        LookupWindowBuilder<E, S> builder =
                new LookupWindowBuilder<>(origin, beanType, lookupBuilderProcessor::build);


        builder.withField(((HasValue<?, E>) picker));

        return builder;
    }

    public <S extends View<?>> WindowBuilder<S> view(View<?> origin, Class<S> viewClass) {
        return new WindowBuilder<>(origin, viewClass, windowBuilderProcessor::build);
    }

    public WindowBuilder<View<?>> view(View<?> origin, String viewId) {
        return new WindowBuilder<>(origin, viewId, windowBuilderProcessor::build);
    }

    protected View<?> getView(Component component) {
        View<?> view = UiComponentUtils.findView(component);
        if (view == null) {
            throw new IllegalStateException(String.format("A component '%s' is not attached to a view",
                    component.getClass().getSimpleName()));
        }

        return view;
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
