package io.jmix.flowui.data.binding;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.HasDataView;

public interface DataViewBinding<C extends Component & HasDataView<V, ?, ?>, V> extends JmixBinding {

    C getComponent();

    DataProvider<V, ?> getDataProvider();
}
