package io.jmix.flowui.data.binding;

import com.vaadin.flow.component.HasValue;
import io.jmix.flowui.data.ValueSource;

public interface ValueBinding<V> extends JmixBinding {

    ValueSource<V> getValueSource();

    HasValue<?, V> getComponent();

    void activate();
}
