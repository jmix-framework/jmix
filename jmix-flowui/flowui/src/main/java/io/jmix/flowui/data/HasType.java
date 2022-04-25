package io.jmix.flowui.data;

public interface HasType<T> {

    /**
     * @return type of value
     */
    Class<T> getType();
}
