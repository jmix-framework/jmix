package io.jmix.flowui.impl;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.di.Instantiator;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.SupportsDatatype;
import org.springframework.core.ParameterizedTypeReference;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@org.springframework.stereotype.Component("flowui_UiComponents")
public class UiComponentsImpl implements UiComponents {

    protected DatatypeRegistry datatypeRegistry;

    public UiComponentsImpl(DatatypeRegistry datatypeRegistry) {
        this.datatypeRegistry = datatypeRegistry;
    }

    @Override
    public <T extends Component> T create(Class<T> type) {
        // TODO: gg, custom logic?
        return Instantiator.get(UI.getCurrent()).getOrCreate(type);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T extends Component> T create(ParameterizedTypeReference<T> typeReference) {
        ParameterizedType type = (ParameterizedType) typeReference.getType();
        T component = create((Class<T>) type.getRawType());
        if (component instanceof SupportsDatatype) {
            Type[] actualTypeArguments = type.getActualTypeArguments();
            if (actualTypeArguments.length == 1 && actualTypeArguments[0] instanceof Class) {
                Class actualTypeArgument = (Class) actualTypeArguments[0];

                ((SupportsDatatype<?>) component).setDatatype(datatypeRegistry.find(actualTypeArgument));
            }
        }
        return component;
    }
}
