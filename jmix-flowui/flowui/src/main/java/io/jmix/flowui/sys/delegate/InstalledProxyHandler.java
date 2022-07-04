package io.jmix.flowui.sys.delegate;

import io.jmix.flowui.view.View;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class InstalledProxyHandler implements InvocationHandler {

    private final View<?> view;
    private final Method method;

    public InstalledProxyHandler(View<?> view, Method method) {
        this.view = view;
        this.method = method;
    }

    @Override
    public Object invoke(Object proxy, Method invokedMethod, Object[] args) throws Throwable {
        if ("toString".equals(invokedMethod.getName())) {
            return this.toString();
        }
        if ("equals".equals(invokedMethod.getName())) {
            return args.length == 1 && args[0] == proxy;
        }
        if ("hashCode".equals(invokedMethod.getName())) {
            return this.hashCode();
        }

        if (invokedMethod.getParameterCount() == method.getParameterCount()) {
            try {
                return this.method.invoke(view, args);
            } catch (InvocationTargetException e) {
                if (e.getTargetException() instanceof RuntimeException) {
                    throw e.getTargetException();
                }

                throw e.getTargetException();
            }
        }

        throw new UnsupportedOperationException(
                String.format("%s does not support method %s. Check types and number of parameters",
                        InstalledProxyHandler.class.getSimpleName(), invokedMethod));
    }

    @Override
    public String toString() {
        return "InstalledProxyHandler{" +
                "frameOwner=" + view.getClass() +
                ", method=" + method +
                '}';
    }
}
