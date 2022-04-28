package io.jmix.flowui.component.delegate;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.provider.HasListDataView;
import io.jmix.flowui.data.binding.ListOptionsBinding.ListOptionsTarget;
import io.jmix.flowui.data.binding.OptionsBinding;
import io.jmix.flowui.data.Options;

import javax.annotation.Nullable;

public abstract class AbstractListOptionsDelegate<C extends Component & HasListDataView<V, ?>, V>
        extends AbstractComponentDelegate<C> {

    protected OptionsBinding<V> optionsBinding;

    public AbstractListOptionsDelegate(C component) {
        super(component);
    }

    @Nullable
    public Options<V> getListOptions() {
        return optionsBinding != null ? optionsBinding.getSource() : null;
    }

    public void setListOptions(@Nullable Options<V> options) {
        setListOptions(options, null);
    }

    public void setListOptions(@Nullable Options<V> options,
                               @Nullable ListOptionsTarget<V> optionsTarget) {
        if (optionsBinding != null) {
            optionsBinding.unbind();
            optionsBinding = null;
        }

        if (options != null) {
            optionsBinding = createOptionsBinding(options, optionsTarget);

            optionsBinding.bind();
            optionsBinding.activate();
        }
    }

    protected abstract OptionsBinding<V> createOptionsBinding(Options<V> options,
                                                              @Nullable ListOptionsTarget<V> optionsTarget);
}
