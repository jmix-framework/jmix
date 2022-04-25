package io.jmix.flowui.data.binding;


import io.jmix.flowui.data.Options;

public interface OptionsBinding<V> extends JmixBinding {

    Options<V> getSource();

    <T> T getComponent();

    void activate();
}
