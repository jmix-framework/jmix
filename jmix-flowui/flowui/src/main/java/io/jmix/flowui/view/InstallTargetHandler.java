package io.jmix.flowui.view;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

public interface InstallTargetHandler {

    @Nullable
    Object createInstallHandler(Class<?> targetObjectType, View<?> controller, Method method);
}