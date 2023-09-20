package io.jmix.flowui.component.formlayout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.formlayout.FormLayout;
import io.jmix.flowui.component.ComponentContainer;
import io.jmix.flowui.data.HasValueSourceProvider;
import io.jmix.flowui.data.SupportsValueSource;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.data.ValueSourceProvider;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.model.impl.InstanceContainerImpl;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.jmix.flowui.component.UiComponentUtils.sameId;

public class JmixFormLayout extends FormLayout implements ComponentContainer, HasValueSourceProvider {

    protected ValueSourceProvider valueSourceProvider;

    @Override
    public Optional<Component> findOwnComponent(String id) {
        return getChildren()
                .filter(component -> sameId(component, id))
                .findFirst();
    }

    @Override
    public Collection<Component> getOwnComponents() {
        return getChildren().sequential().collect(Collectors.toList());
    }

    @Nullable
    @Override
    public ValueSourceProvider getValueSourceProvider() {
        return valueSourceProvider;
    }

    @Override
    public void setValueSourceProvider(@Nullable ValueSourceProvider provider) {
        this.valueSourceProvider = provider;
    }
}
