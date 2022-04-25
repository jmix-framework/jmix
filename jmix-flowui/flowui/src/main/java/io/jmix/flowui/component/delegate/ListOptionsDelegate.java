package io.jmix.flowui.component.delegate;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.provider.HasListDataView;
import io.jmix.flowui.data.binding.ListOptionsBinding.ListOptionsTarget;
import io.jmix.flowui.data.binding.OptionsBinding;
import io.jmix.flowui.data.binding.impl.ListOptionsBindingImpl;
import io.jmix.flowui.data.Options;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import javax.annotation.Nullable;

@org.springframework.stereotype.Component("flowui_BaseListOptionsDelegate")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ListOptionsDelegate<C extends Component & HasListDataView<V, ?>, V> extends AbstractListOptionsDelegate<C, V> {

    public ListOptionsDelegate(C component) {
        super(component);
    }

    @Override
    protected OptionsBinding<V> createOptionsBinding(Options<V> options,
                                                     @Nullable ListOptionsTarget<V> optionsTarget) {
        if (optionsTarget == null) {
            optionsTarget = component::setItems;
        }

        return new ListOptionsBindingImpl<>(options, component, optionsTarget);
    }
}
