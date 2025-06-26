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
import io.jmix.core.annotation.Internal;
import io.jmix.flowui.view.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * Utility class for resolving and inferring various attributes related to view descriptors.
 */
@Internal
public final class ViewDescriptorUtils {

    private ViewDescriptorUtils() {
    }

    /**
     * Resolves the template path for a specified controller class based on its {@link ViewDescriptor} annotation
     * and inferred path details.
     *
     * @param controllerClass the controller class annotated with {@link ViewDescriptor}
     * @return the resolved template path
     */
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

    /**
     * Resolves the inferred template path for a specified view class based on its {@link ViewDescriptor} annotation.
     *
     * @param viewDescriptor     the {@link ViewDescriptor} annotation instance associated with the view class
     * @param annotatedViewClass the view class annotated with {@link ViewDescriptor}
     * @return the resolved template path for the specified view class
     * @throws DevelopmentException if the view class does not have a valid template path
     */
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

    /**
     * Infers the subscribe identifier for a given {@link Subscribe} annotation instance.
     * Resolves the identifier using the {@code value} attribute of the annotation, and falls back
     * to the {@code id} attribute if the {@code value} is empty or {@code null}.
     *
     * @param subscribe the {@link Subscribe} annotation instance from which the subscribe
     *                  identifier is inferred
     * @return the inferred subscribe identifier
     */
    public static String getInferredSubscribeId(Subscribe subscribe) {
        checkNotNullArgument(subscribe);

        String target = subscribe.value();
        if (Strings.isNullOrEmpty(target)) {
            target = subscribe.id();
        }

        return target;
    }

    /**
     * Infers the view identifier for the specified annotated view class.
     *
     * @param annotatedViewClass the class annotated with {@link ViewController} for which
     *                           the view identifier is inferred
     * @return the inferred view identifier
     */
    public static String getInferredViewId(Class<?> annotatedViewClass) {
        checkNotNullArgument(annotatedViewClass);

        ViewController viewController = annotatedViewClass.getAnnotation(ViewController.class);
        if (viewController == null) {
            throw new IllegalArgumentException("No @" + ViewController.class.getSimpleName() +
                    " annotation for class " + annotatedViewClass);
        }

        return ViewDescriptorUtils.getInferredViewId(viewController, annotatedViewClass);
    }

    /**
     * Infers the view identifier for a given {@link ViewController} annotation
     * and an annotated view class.
     *
     * @param viewController     the {@link ViewController} annotation instance
     *                           containing the view metadata
     * @param annotatedViewClass the class annotated with {@link ViewController}
     *                           for which the view identifier is inferred
     * @return the inferred view identifier
     */
    public static String getInferredViewId(ViewController viewController,
                                           Class<?> annotatedViewClass) {
        checkNotNullArgument(viewController);
        checkNotNullArgument(annotatedViewClass);

        return getInferredViewId(viewController.id(), viewController.value(), annotatedViewClass.getName());
    }

    /**
     * Infers a view identifier based on provided attributes and class name.
     *
     * @param idAttribute    an optional identifier attribute
     * @param valueAttribute an optional value attribute
     * @param className      the fully-qualified name of the class
     * @return the inferred view identifier
     */
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

    /**
     * Infers the identifier from the {@link Install} annotation.
     *
     * @param install the {@link Install} annotation instance
     * @return the identifier of the {@link Install} annotation
     */
    public static String getInferredProvideId(Install install) {
        checkNotNullArgument(install);

        return install.to();
    }

    /**
     * Infers the identifier from the {@link Supply} annotation.
     *
     * @param supply the {@link Supply} annotation instance
     * @return the identifier of the {@link Supply} annotation
     */
    public static String getInferredProvideId(Supply supply) {
        checkNotNullArgument(supply);

        return supply.to();
    }
}
