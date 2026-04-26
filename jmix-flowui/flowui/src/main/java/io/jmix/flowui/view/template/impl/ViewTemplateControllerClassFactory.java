/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.view.template.impl;

import com.vaadin.flow.router.Route;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.view.*;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FixedValue;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * Generates dedicated controller classes for template-based views.
 */
@Component("flowui_ViewTemplateControllerClassFactory")
public class ViewTemplateControllerClassFactory {

    protected static final String GENERATED_PACKAGE_SUFFIX = ".generated_view";
    protected static final Pattern NON_ROUTE_CHARS = Pattern.compile("[^a-z0-9]+");
    protected static final Pattern EDGE_SEPARATORS = Pattern.compile("(^[-_]+|[-_]+$)");

    protected Map<String, Class<? extends View<?>>> controllerClasses = new ConcurrentHashMap<>();

    /**
     * Returns a generated controller class for the specified list template view.
     *
     * @param entityMetaClass entity meta-class that owns the generated view
     * @param viewId          view id to expose through {@link ViewController}
     * @param descriptorPath  descriptor path exposed through {@link ViewDescriptor}
     * @param routePath       route path exposed through {@link Route}
     * @param lookupComponentId lookup component id used by the generated controller
     * @return generated controller class
     */
    public Class<? extends View<?>> createListViewControllerClass(MetaClass entityMetaClass,
                                                                  String viewId,
                                                                  String descriptorPath,
                                                                  String routePath,
                                                                  String lookupComponentId) {
        String key = ViewTemplateType.LIST.name() + ":" + viewId + ":" + routePath + ":" + lookupComponentId;
        return controllerClasses.computeIfAbsent(key, __ ->
                createListViewControllerClassInternal(entityMetaClass, viewId, descriptorPath, routePath,
                        lookupComponentId));
    }

    /**
     * Returns a generated controller class for the specified detail template view.
     *
     * @param entityMetaClass entity meta-class that owns the generated view
     * @param viewId          view id to expose through {@link ViewController}
     * @param descriptorPath  descriptor path exposed through {@link ViewDescriptor}
     * @param routePath       route path exposed through {@link Route}
     * @param editedEntityContainerId edited entity container id used by the generated controller
     * @return generated controller class
     */
    public Class<? extends View<?>> createDetailViewControllerClass(MetaClass entityMetaClass,
                                                                    String viewId,
                                                                    String descriptorPath,
                                                                    String routePath,
                                                                    String editedEntityContainerId) {
        String key = ViewTemplateType.DETAIL.name() + ":" + viewId + ":" + routePath + ":"
                + editedEntityContainerId;
        return controllerClasses.computeIfAbsent(key, __ ->
                createDetailViewControllerClassInternal(entityMetaClass, viewId, descriptorPath, routePath,
                        editedEntityContainerId));
    }

    @SuppressWarnings("unchecked")
    protected Class<? extends View<?>> createListViewControllerClassInternal(MetaClass entityMetaClass,
                                                                             String viewId,
                                                                             String descriptorPath,
                                                                             String routePath,
                                                                             String lookupComponentId) {
        String className = createClassName(entityMetaClass, ViewTemplateType.LIST);
        Class<?> loadedClass = findLoadedClass(className, TemplateListView.class.getClassLoader());
        if (loadedClass != null) {
            return (Class<? extends View<?>>) loadedClass;
        }

        DynamicType.Builder<? extends View<?>> builder = createControllerClassBuilder(
                entityMetaClass,
                ViewTemplateType.LIST,
                TemplateListView.class,
                viewId,
                descriptorPath,
                routePath
        ).method(named("getLookupComponentId"))
                .intercept(FixedValue.value(lookupComponentId));

        try (DynamicType.Unloaded<? extends View<?>> unloaded = builder.make()) {
            return unloaded.load(TemplateListView.class.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                    .getLoaded();
        }
    }

    @SuppressWarnings("unchecked")
    protected Class<? extends View<?>> createDetailViewControllerClassInternal(MetaClass entityMetaClass,
                                                                               String viewId,
                                                                               String descriptorPath,
                                                                               String routePath,
                                                                               String editedEntityContainerId) {
        String className = createClassName(entityMetaClass, ViewTemplateType.DETAIL);
        Class<?> loadedClass = findLoadedClass(className, TemplateDetailView.class.getClassLoader());
        if (loadedClass != null) {
            return (Class<? extends View<?>>) loadedClass;
        }

        DynamicType.Builder<? extends View<?>> builder = createControllerClassBuilder(
                entityMetaClass,
                ViewTemplateType.DETAIL,
                TemplateDetailView.class,
                viewId,
                descriptorPath,
                routePath
        ).method(named("getEditedEntityContainerId"))
                .intercept(FixedValue.value(editedEntityContainerId));

        try (DynamicType.Unloaded<? extends View<?>> unloaded = builder.make()) {
            return unloaded.load(TemplateDetailView.class.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                    .getLoaded();
        }
    }

    protected DynamicType.Builder<? extends View<?>> createControllerClassBuilder(MetaClass entityMetaClass,
                                                                                  ViewTemplateType type,
                                                                                  Class<? extends View<?>> superclass,
                                                                                  String viewId,
                                                                                  String descriptorPath,
                                                                                  String routePath) {
        String className = createClassName(entityMetaClass, type);
        AnnotationDescription viewController = AnnotationDescription.Builder.ofType(ViewController.class)
                .define(ViewController.ID_ATTRIBUTE, viewId)
                .build();
        AnnotationDescription viewDescriptor = AnnotationDescription.Builder.ofType(ViewDescriptor.class)
                .define("path", descriptorPath)
                .build();
        AnnotationDescription route = AnnotationDescription.Builder.ofType(Route.class)
                .define("value", routePath)
                .define("layout", DefaultMainViewParent.class)
                .build();

        return new ByteBuddy()
                .subclass(superclass)
                .name(className)
                .annotateType(viewController, viewDescriptor, route);
    }

    /**
     * Creates the default route path for a template view when the annotation does not provide one.
     *
     * @param viewId template view id
     * @param type   template view type
     * @return generated route path
     */
    public String createDefaultRoutePath(String viewId, ViewTemplateType type) {
        String routePath = createRouteSlug(viewId);
        return type == ViewTemplateType.DETAIL
                ? routePath + "/:" + StandardDetailView.DEFAULT_ROUTE_PARAM
                : routePath;
    }

    protected String createClassName(MetaClass entityMetaClass, ViewTemplateType type) {
        Class<?> entityClass = entityMetaClass.getJavaClass();
        String controllerType = type == ViewTemplateType.LIST ? "ListView" : "DetailView";
        return entityClass.getPackageName()
                + GENERATED_PACKAGE_SUFFIX
                + "."
                + entityClass.getSimpleName()
                + controllerType;
    }

    protected String createRouteSlug(String value) {
        String slug = NON_ROUTE_CHARS.matcher(value.toLowerCase(Locale.ROOT)).replaceAll("-");
        slug = EDGE_SEPARATORS.matcher(slug).replaceAll("");

        if (slug.isEmpty()) {
            slug = "view";
        }

        return slug;
    }

    @Nullable
    protected Class<?> findLoadedClass(String className, ClassLoader classLoader) {
        try {
            return Class.forName(className, false, classLoader);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
