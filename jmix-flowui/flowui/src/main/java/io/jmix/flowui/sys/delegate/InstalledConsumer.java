package io.jmix.flowui.sys.delegate;

import io.jmix.flowui.SameAsUi;
import io.jmix.flowui.screen.Install;
import io.jmix.flowui.screen.Screen;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;

@SameAsUi
public class InstalledConsumer implements Consumer<Object> {

    private final Screen controller;
    private final Method method;

    public InstalledConsumer(Screen controller, Method method) {
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
