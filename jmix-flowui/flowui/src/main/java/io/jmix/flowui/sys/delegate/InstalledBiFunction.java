package io.jmix.flowui.sys.delegate;

import io.jmix.flowui.view.Install;
import io.jmix.flowui.view.View;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.BiFunction;

public class InstalledBiFunction implements BiFunction<Object, Object, Object> {

    private final View<?> controller;
    private final Method method;

    public InstalledBiFunction(View<?> controller, Method method) {
        this.controller = controller;
        this.method = method;
    }

    @Override
    public Object apply(Object o1, Object o2) {
        try {
            return method.invoke(controller, o1, o2);
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
        return "InstalledBiFunction{" +
                "frameOwner=" + controller.getClass() +
                ", method=" + method +
                '}';
    }
}
