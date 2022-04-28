package io.jmix.flowui;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.data.DataUnit;
import io.jmix.flowui.data.HasType;
import io.jmix.flowui.component.EntityPickerComponent;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.screen.Screen;
import io.jmix.flowui.screen.builder.*;

import static com.google.common.base.Preconditions.checkState;
import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

@org.springframework.stereotype.Component("flowui_DialogWindowBuilders")
public class DialogWindowBuilders {

    protected WindowBuilderProcessor windowBuilderProcessor;
    protected EditorWindowBuilderProcessor editorBuilderProcessor;
    protected LookupWindowBuilderProcessor lookupBuilderProcessor;

    public DialogWindowBuilders(WindowBuilderProcessor windowBuilderProcessor,
                                EditorWindowBuilderProcessor editorBuilderProcessor,
                                LookupWindowBuilderProcessor lookupBuilderProcessor) {
        this.windowBuilderProcessor = windowBuilderProcessor;
        this.editorBuilderProcessor = editorBuilderProcessor;
        this.lookupBuilderProcessor = lookupBuilderProcessor;
    }

    public <E, S extends Screen> EditorWindowBuilder<E, S> editor(Screen origin, Class<E> entityClass) {
        checkNotNullArgument(origin);
        checkNotNullArgument(entityClass);

        return new EditorWindowBuilder<>(origin, entityClass, editorBuilderProcessor::buildScreen);
    }

    public <E, S extends Screen> EditorWindowBuilder<E, S> editor(ListDataComponent<E> listDataComponent) {
        checkNotNullArgument(listDataComponent);

        Screen origin = getScreen((Component) listDataComponent);
        Class<E> beanType = getBeanType(listDataComponent);

        EditorWindowBuilder<E, S> builder =
                new EditorWindowBuilder<>(origin, beanType, editorBuilderProcessor::buildScreen);

        builder.withListDataComponent(listDataComponent);

        E selected = listDataComponent.getSingleSelectedItem();
        if (selected != null) {
            builder.editEntity(selected);
        }

        return builder;
    }

    @SuppressWarnings("unchecked")
    public <E, S extends Screen> EditorWindowBuilder<E, S> editor(EntityPickerComponent<E> picker) {
        checkNotNullArgument(picker);
        checkState(picker instanceof HasValue,
                "A component must implement " + HasValue.class.getSimpleName());

        Screen origin = getScreen((Component) picker);
        Class<E> beanType = getBeanType(picker);

        EditorWindowBuilder<E, S> builder =
                new EditorWindowBuilder<>(origin, beanType, editorBuilderProcessor::buildScreen);

        builder.withField(((HasValue<?, E>) picker));

        E value = ((HasValue<?, E>) picker).getValue();
        if (value != null) {
            builder.editEntity(value);
        }

        return builder;
    }

    public <E, S extends Screen> LookupWindowBuilder<E, S> lookup(Screen origin, Class<E> entityClass) {
        checkNotNullArgument(origin);
        checkNotNullArgument(entityClass);

        return new LookupWindowBuilder<>(origin, entityClass, lookupBuilderProcessor::buildScreen);
    }

    public <E, S extends Screen> LookupWindowBuilder<E, S> lookup(ListDataComponent<E> listDataComponent) {
        checkNotNullArgument(listDataComponent);

        Screen origin = getScreen((Component) listDataComponent);
        Class<E> beanType = getBeanType(listDataComponent);

        LookupWindowBuilder<E, S> builder =
                new LookupWindowBuilder<>(origin, beanType, lookupBuilderProcessor::buildScreen);

        builder.withListDataComponent(listDataComponent);

        return builder;
    }

    @SuppressWarnings("unchecked")
    public <E, S extends Screen> LookupWindowBuilder<E, S> lookup(EntityPickerComponent<E> picker) {
        checkNotNullArgument(picker);
        checkState(picker instanceof HasValue,
                "A component must implement " + HasValue.class.getSimpleName());

        Screen origin = getScreen((Component) picker);
        Class<E> beanType = getBeanType(picker);

        LookupWindowBuilder<E, S> builder =
                new LookupWindowBuilder<>(origin, beanType, lookupBuilderProcessor::buildScreen);


        builder.withField(((HasValue<?, E>) picker));

        return builder;
    }

    public <S extends Screen> WindowBuilder<S> screen(Screen origin, Class<S> screenClass) {
        return new WindowBuilder<>(origin, screenClass, windowBuilderProcessor::buildScreen);
    }

    public WindowBuilder<Screen> screen(Screen origin, String screenId) {
        return new WindowBuilder<>(origin, screenId, windowBuilderProcessor::buildScreen);
    }

    protected Screen getScreen(Component component) {
        Screen screen = UiComponentUtils.findScreen(component);
        if (screen == null) {
            throw new IllegalStateException(String.format("A component '%s' is not attached to a screen",
                    component.getClass().getSimpleName()));
        }

        return screen;
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
