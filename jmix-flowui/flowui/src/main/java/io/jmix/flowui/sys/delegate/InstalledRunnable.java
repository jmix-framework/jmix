package io.jmix.flowui.sys.delegate;

import io.jmix.flowui.SameAsUi;
import io.jmix.flowui.screen.Install;
import io.jmix.flowui.screen.Screen;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SameAsUi
public class InstalledRunnable implements Runnable {

    private final Screen controller;
    private final Method method;

    public InstalledRunnable(Screen controller, Method method) {
        this.controller = controller;
        this.method = method;
    }

    @Override
    public void run() {
        try {
            method.invoke(controller);
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
        return "InstalledRunnable{" +
                "target=" + controller.getClass() +
                ", method=" + method +
                '}';
    }
}
