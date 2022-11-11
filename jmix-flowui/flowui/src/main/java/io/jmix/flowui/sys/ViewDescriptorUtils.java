/*
 * Copyright 2022 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.flowui.sys;

import com.google.common.base.Strings;
import io.jmix.core.DevelopmentException;
import io.jmix.flowui.view.*;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

public final class ViewDescriptorUtils {

    private ViewDescriptorUtils() {
    }

    @Nullable
    public static String resolveTemplatePath(Class<? extends View<?>> controllerClass) {
        ViewDescriptor annotation = controllerClass.getAnnotation(ViewDescriptor.class);
        if (annotation == null) {
            return null;
        } else {
            String templatePath = ViewDescriptorUtils.getInferredTemplate(annotation, controllerClass);
            if (!templatePath.startsWith("/")) {
                String packageName = ViewControllerUtils.getPackage(controllerClass);
                if (StringUtils.isNotEmpty(packageName)) {
                    String relativePath = packageName.replace('.', '/');
                    templatePath = "/" + relativePath + "/" + templatePath;
                }
            }

            return templatePath;
        }
    }

    public static String getInferredTemplate(ViewDescriptor viewDescriptor,
                                             Class<?> annotatedViewClass) {
        checkNotNullArgument(viewDescriptor);
        checkNotNullArgument(annotatedViewClass);

        String template = viewDescriptor.value();
        if (Strings.isNullOrEmpty(template)) {
            template = viewDescriptor.path();

            if (Strings.isNullOrEmpty(template)) {
                throw new DevelopmentException("View class annotated with @" +
                        ViewDescriptor.class.getSimpleName() + " without template: " + annotatedViewClass);
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

        ViewController viewController = annotatedViewClass.getAnnotation(ViewController.class);
        if (viewController == null) {
            throw new IllegalArgumentException("No @" + ViewController.class.getSimpleName() +
                    " annotation for class " + annotatedViewClass);
        }

        return ViewDescriptorUtils.getInferredViewId(viewController, annotatedViewClass);
    }

    public static String getInferredViewId(ViewController viewController,
                                           Class<?> annotatedViewClass) {
        checkNotNullArgument(viewController);
        checkNotNullArgument(annotatedViewClass);

        return getInferredViewId(viewController.id(), viewController.value(), annotatedViewClass.getName());
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
