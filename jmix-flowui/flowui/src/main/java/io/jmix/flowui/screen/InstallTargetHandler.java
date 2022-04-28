package io.jmix.flowui.screen;

import io.jmix.flowui.SameAsUi;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

@SameAsUi
public interface InstallTargetHandler {

    @Nullable
    Object createInstallHandler(Class<?> targetObjectType, Screen controller, Method method);
}