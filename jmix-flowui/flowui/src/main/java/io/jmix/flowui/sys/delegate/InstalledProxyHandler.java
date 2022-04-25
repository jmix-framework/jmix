package io.jmix.flowui.sys.delegate;

import io.jmix.flowui.SameAsUi;
import io.jmix.flowui.screen.Screen;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SameAsUi
public class InstalledProxyHandler implements InvocationHandler {

    private final Screen screen;
    private final Method method;

    public InstalledProxyHandler(Screen screen, Method method) {
        this.screen = screen;
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
                return this.method.invoke(screen, args);
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
                "frameOwner=" + screen.getClass() +
                ", method=" + method +
                '}';
    }
}
