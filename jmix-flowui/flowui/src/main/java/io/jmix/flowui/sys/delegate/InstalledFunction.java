package io.jmix.flowui.sys.delegate;


import io.jmix.flowui.SameAsUi;
import io.jmix.flowui.screen.Install;
import io.jmix.flowui.screen.Screen;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

@SameAsUi
public class InstalledFunction implements Function<Object, Object> {

    private final Screen controller;
    private final Method method;

    public InstalledFunction(Screen controller, Method method) {
        this.controller = controller;
        this.method = method;
    }

    @Override
    public Object apply(Object o) {
        try {
            return method.invoke(controller, o);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(String.format("Exception on @%s invocation", Install.class.getSimpleName()), e);
        }
    }

    @Override
    public String toString() {
        return "InstalledFunction{" +
                "frameOwner=" + controller.getClass() +
                ", method=" + method +
                '}';
    }
}
