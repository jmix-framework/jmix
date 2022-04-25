package io.jmix.flowui.sys;

import com.google.common.base.Strings;
import io.jmix.core.DevelopmentException;
import io.jmix.flowui.screen.Install;
import io.jmix.flowui.screen.Subscribe;
import io.jmix.flowui.screen.UiController;
import io.jmix.flowui.screen.UiDescriptor;

import javax.annotation.Nullable;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

public final class UiDescriptorUtils {

    private UiDescriptorUtils() {
    }

    public static String getInferredTemplate(UiDescriptor uiDescriptor,
                                             Class<?/* extends Screen*/> annotatedScreenClass) {
        checkNotNullArgument(uiDescriptor);
        checkNotNullArgument(annotatedScreenClass);

        String template = uiDescriptor.value();
        if (Strings.isNullOrEmpty(template)) {
            template = uiDescriptor.path();

            if (Strings.isNullOrEmpty(template)) {
                throw new DevelopmentException("Screen class annotated with @" +
                        UiDescriptor.class.getSimpleName() + " without template: " + annotatedScreenClass);
            }
        }

        return template;
    }

    public static String getInferredSubscribeId(Subscribe subscribe) {
        checkNotNullArgument(subscribe);

        String target = subscribe.value();
        if (Strings.isNullOrEmpty(target)) {
            target = subscribe.id();
        }

        return target;
    }

    public static String getInferredScreenId(UiController uiController,
                                             Class<?/* extends Screen*/> annotatedScreenClass) {
        checkNotNullArgument(uiController);
        checkNotNullArgument(annotatedScreenClass);

        return getInferredScreenId(uiController.id(), uiController.value(), annotatedScreenClass.getName());
    }

    public static String getInferredScreenId(@Nullable String idAttribute,
                                             @Nullable String valueAttribute,
                                             String className) {
        String id = valueAttribute;
        if (Strings.isNullOrEmpty(id)) {
            id = idAttribute;

            if (Strings.isNullOrEmpty(id)) {
                int indexOfDot = className.lastIndexOf('.');
                if (indexOfDot < 0) {
                    id = className;
                } else {
                    id = className.substring(indexOfDot + 1);
                }
            }
        }

        return id;
    }

    public static String getInferredProvideId(Install install) {
        checkNotNullArgument(install);

        return install.to();
    }
}
