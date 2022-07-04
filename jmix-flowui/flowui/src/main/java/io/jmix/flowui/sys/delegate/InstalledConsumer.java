package io.jmix.flowui.sys.delegate;

import io.jmix.flowui.view.Install;
import io.jmix.flowui.view.View;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;

public class InstalledConsumer implements Consumer<Object> {

    private final View<?> controller;
    private final Method method;

    public InstalledConsumer(View<?> controller, Method method) {
        this.controller = controller;
        this.method = method;
    }

    @Override
    public void accept(Object o) {
        try {
            method.invoke(controller, o);
        } catch (IllegalAccessException | InvocationTargetException e) {
            if (e instanceof InvocationTargetException
                    && ((InvocationTargetException) e).getTargetException() instanceof RuntimeException) {
                throw (RuntimeException) ((InvocationTargetException) e).getTargetException();
            }

            throw new RuntimeException(String.format("Exception on @%s invocation", Install.class.getSimpleName()), e);
        }
    }

    @Override
    public String toString() {
        return "InstalledConsumer{" +
                "frameOwner=" + controller.getClass() +
                ", method=" + method +
                '}';
    }
}
