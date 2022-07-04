package io.jmix.flowui.sys;

import com.google.common.base.Strings;
import io.jmix.core.DevelopmentException;
import io.jmix.flowui.view.*;

import javax.annotation.Nullable;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

public final class UiDescriptorUtils {

    private UiDescriptorUtils() {
    }

    public static String getInferredTemplate(UiDescriptor uiDescriptor,
                                             Class<?> annotatedViewClass) {
        checkNotNullArgument(uiDescriptor);
        checkNotNullArgument(annotatedViewClass);

        String template = uiDescriptor.value();
        if (Strings.isNullOrEmpty(template)) {
            template = uiDescriptor.path();

            if (Strings.isNullOrEmpty(template)) {
                throw new DevelopmentException("View class annotated with @" +
                        UiDescriptor.class.getSimpleName() + " without template: " + annotatedViewClass);
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

    public static String getInferredViewId(Class<?> annotatedViewClass) {
        checkNotNullArgument(annotatedViewClass);

        UiController uiController = annotatedViewClass.getAnnotation(UiController.class);
        if (uiController == null) {
            throw new IllegalArgumentException("No @" + UiController.class.getSimpleName() +
                    " annotation for class " + annotatedViewClass);
        }

        return UiDescriptorUtils.getInferredViewId(uiController, annotatedViewClass);
    }

    public static String getInferredViewId(UiController uiController,
                                           Class<?> annotatedViewClass) {
        checkNotNullArgument(uiController);
        checkNotNullArgument(annotatedViewClass);

        return getInferredViewId(uiController.id(), uiController.value(), annotatedViewClass.getName());
    }

    public static String getInferredViewId(@Nullable String idAttribute,
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
