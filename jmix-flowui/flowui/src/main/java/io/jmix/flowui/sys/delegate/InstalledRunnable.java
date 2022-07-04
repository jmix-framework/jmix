package io.jmix.flowui.sys.delegate;

import io.jmix.flowui.view.Install;
import io.jmix.flowui.view.View;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class InstalledRunnable implements Runnable {

    private final View<?> controller;
    private final Method method;

    public InstalledRunnable(View<?> controller, Method method) {
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
