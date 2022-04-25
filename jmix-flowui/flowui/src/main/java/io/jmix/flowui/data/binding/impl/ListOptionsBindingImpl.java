package io.jmix.flowui.data.binding.impl;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.data.provider.HasListDataView;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.Entity;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.data.binding.ListOptionsBinding;
import io.jmix.flowui.data.BindingState;
import io.jmix.flowui.data.EntityOptions;
import io.jmix.flowui.data.Options;
import io.jmix.flowui.component.SupportsTypedValue;

import javax.annotation.Nullable;
import java.util.stream.Collectors;

public class ListOptionsBindingImpl<V> implements ListOptionsBinding<V> {

    protected Options<V> optionsSource;
    protected HasListDataView<V, ?> component;
    protected ListOptionsTarget<V> optionsTarget;

    protected Registration componentValueChangeRegistration;

    protected Subscription sourceStateChangeSubscription;
    protected Subscription sourceOptionsChangeSubscription;
    protected Subscription sourceValueChangeSubscription;

    public ListOptionsBindingImpl(Options<V> optionsSource, HasListDataView<V, ?> component,
                                  ListOptionsTarget<V> optionsTarget) {
        Preconditions.checkNotNullArgument(optionsSource);
        Preconditions.checkNotNullArgument(component);
        Preconditions.checkNotNullArgument(optionsTarget);

        this.optionsSource = optionsSource;
        this.component = component;
        this.optionsTarget = optionsTarget;
    }

    @Override
    public Options<V> getSource() {
        return optionsSource;
    }

    @Override
    public HasListDataView<V, ?> getComponent() {
        return component;
    }

    @Override
    public void activate() {
        if (optionsSource.getState() == BindingState.ACTIVE) {
            optionsTarget.setOptions(optionsSource.getOptions().collect(Collectors.toList()));
        }
    }

    @Override
    public void bind() {
        if (optionsSource instanceof EntityOptions && component instanceof HasValue) {
            this.componentValueChangeRegistration = addComponentValueChangeListener();

            this.sourceValueChangeSubscription =
                    ((EntityOptions<V>) optionsSource).addValueChangeListener(this::optionsSourceValueChanged);
        }

        this.sourceStateChangeSubscription = optionsSource.addStateChangeListener(this::optionsSourceStateChanged);
        this.sourceOptionsChangeSubscription = optionsSource.addOptionsChangeListener(this::optionsSourceOptionsChanged);
    }

    protected void componentValueChanged(@Nullable V value) {
        // value could be List / Set / something else
        if (value instanceof Entity || value == null) {
            EntityOptions<V> entityOptionsSource = (EntityOptions<V>) this.optionsSource;
            entityOptionsSource.setSelectedItem(value);
        }
    }

    protected void optionsSourceValueChanged(EntityOptions.ValueChangeEvent<?> event) {
        if (optionsTarget instanceof SupportsTypedValue) {
            ((SupportsTypedValue) optionsTarget).setTypedValue(event.getValue());
        } else if (optionsTarget instanceof HasValue) {
            ((HasValue) optionsTarget).setValue(event.getValue());
        }
    }

    protected void optionsSourceStateChanged(Options.StateChangeEvent event) {
        if (event.getState() == BindingState.ACTIVE) {
            optionsTarget.setOptions(optionsSource.getOptions().collect(Collectors.toList()));
        }
    }

    protected void optionsSourceOptionsChanged(@SuppressWarnings("unused") Options.OptionsChangeEvent<V> event) {
        optionsTarget.setOptions(optionsSource.getOptions().collect(Collectors.toList()));
    }

    @Override
    public void unbind() {
        if (this.componentValueChangeRegistration != null) {
            this.componentValueChangeRegistration.remove();
            this.componentValueChangeRegistration = null;
        }
        if (this.sourceValueChangeSubscription != null) {
            this.sourceValueChangeSubscription.remove();
            this.sourceValueChangeSubscription = null;
        }
        if (this.sourceOptionsChangeSubscription != null) {
            this.sourceOptionsChangeSubscription.remove();
            this.sourceOptionsChangeSubscription = null;
        }
        if (this.sourceStateChangeSubscription != null) {
            this.sourceStateChangeSubscription.remove();
            this.sourceStateChangeSubscription = null;
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected Registration addComponentValueChangeListener() {
        if (component instanceof SupportsTypedValue) {
            ((SupportsTypedValue<?, ?, V, ?>) component).addTypedValueChangeListener(event -> componentValueChanged(event.getValue()));
        } else if (component instanceof HasValue) {
            ((HasValue<?, V>) component).addValueChangeListener(event -> componentValueChanged(event.getValue()));
        }
        return null;
    }
}
