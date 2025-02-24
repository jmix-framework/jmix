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

package io.jmix.flowui.view;

import com.google.common.base.Strings;
import com.vaadin.flow.component.*;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.annotation.Internal;
import io.jmix.flowui.facet.Facet;
import io.jmix.flowui.model.ViewData;
import io.jmix.flowui.view.StandardDetailView.InitEntityEvent;
import io.jmix.flowui.view.StandardDetailView.ValidationEvent;
import io.jmix.flowui.view.View.QueryParametersChangeEvent;
import io.jmix.flowui.view.View.RestoreComponentsStateEvent;
import org.springframework.lang.Nullable;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.function.Consumer;

public final class ViewControllerUtils {

    private ViewControllerUtils() {
    }

    public static String getPageTitle(@Nullable Component component) {
        if (component == null) {
            return "";
        }

        if (component instanceof View) {
            return ((View<?>) component).getPageTitle();
        }

        if (component instanceof HasDynamicTitle) {
            return Strings.nullToEmpty(((HasDynamicTitle) component).getPageTitle());
        } else {
            return findAnnotation(component, PageTitle.class)
                    .map(PageTitle::value)
                    .orElse("");
        }
    }

    public static <A extends Annotation> Optional<A> findAnnotation(Component component, Class<A> annotationClass) {
        return findAnnotation(component.getClass(), annotationClass);
    }

    public static <A extends Annotation> Optional<A> findAnnotation(Class<? extends Component> componentClass,
                                                                    Class<A> annotationClass) {
        return Optional.ofNullable(componentClass.getAnnotation(annotationClass));
    }

    public static String getPackage(Class<?> controllerClass) {
        Package javaPackage = controllerClass.getPackage();
        if (javaPackage != null) {
            return javaPackage.getName();
        }

        // infer from FQN, hot-deployed classes do not have package
        // see JDK-8189231
        String canonicalName = controllerClass.getCanonicalName();
        int dotIndex = canonicalName.lastIndexOf('.');

        if (dotIndex >= 0) {
            return canonicalName.substring(0, dotIndex);
        }

        return "";
    }

    public static ViewData getViewData(View<?> view) {
        return view.getViewData();
    }

    public static void setViewData(View<?> view, ViewData viewData) {
        view.setViewData(viewData);
        viewData.setViewId(view.getId().orElse(null));
    }

    public static ViewActions getViewActions(View<?> view) {
        return view.getViewActions();
    }

    public static void setViewActions(View<?> view, ViewActions viewActions) {
        view.setViewActions(viewActions);
    }

    public static ViewFacets getViewFacets(View<?> view) {
        return view.getViewFacets();
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T extends Facet> T getViewFacet(View<?> view, Class<T> facetClass) {
        return (T) view.getViewFacets()
                .getFacets()
                .filter(facet -> facetClass.isAssignableFrom(facet.getClass()))
                .findAny()
                .orElse(null);
    }

    public static void setViewFacets(View<?> view, ViewFacets viewFacets) {
        view.setViewFacets(viewFacets);
    }

    public static void fireEvent(View<?> view, ComponentEvent<?> event) {
        ComponentUtil.fireEvent(view, event);
    }

    public static boolean isSaveActionPerformed(StandardDetailView<?> detailView) {
        return detailView.isSaveActionPerformed();
    }

    public static Registration addQueryParametersChangeListener(View<?> view,
                                                                ComponentEventListener<QueryParametersChangeEvent> listener) {
        return view.addQueryParametersChangeListener(listener);
    }

    /**
     * Adds {@link RestoreComponentsStateEvent} listener.
     *
     * @param listener the listened to add, not {@code null}
     * @return a registration object that can be used for removing the listener
     */
    public static Registration addRestoreComponentsStateEventListener(View<?> view,
                                                                      ComponentEventListener<RestoreComponentsStateEvent> listener) {
        return view.addRestoreComponentsStateEventListener(listener);
    }

    public static <T> Registration addInitEntityEventListener(StandardDetailView<T> view,
                                                              ComponentEventListener<InitEntityEvent<T>> listener) {
        return view.addInitEntityListener(listener);
    }

    public static Registration addBeforeShowEventListener(View<?> view,
                                                          ComponentEventListener<View.BeforeShowEvent> listener) {
        return view.addBeforeShowListener(listener);
    }

    public static Registration addDetachListener(View<?> view,
                                                 ComponentEventListener<DetachEvent> listener) {
        return view.addDetachListener(listener);
    }

    public static Registration addAfterCloseListener(View<?> view,
                                                     ComponentEventListener<View.AfterCloseEvent> listener) {
        return view.addAfterCloseListener(listener);
    }

    public static Registration addValidationEventListener(StandardDetailView<?> view,
                                                          ComponentEventListener<ValidationEvent> listener) {
        return view.addValidationEventListener(listener);
    }

    public static Registration addReadyListener(View<?> view,
                                                ComponentEventListener<View.ReadyEvent> listener) {
        return view.addReadyListener(listener);
    }

    public static Registration addPostReadyListener(View<?> view,
                                                    ComponentEventListener<View.PostReadyEvent> listener) {
        return view.getEventBus().addListener(View.PostReadyEvent.class, listener);
    }

    /**
     * @deprecated use {@link ViewControllerUtils#addInitEntityEventListener(StandardDetailView, ComponentEventListener)} instead
     */
    @Deprecated(since = "2.2", forRemoval = true)
    public static <T> Registration addInitEntityEvent(StandardDetailView<T> view,
                                                      ComponentEventListener<InitEntityEvent<T>> listener) {
        return view.addInitEntityListener(listener);
    }

    /**
     * @deprecated use {@link ViewControllerUtils#addBeforeShowEventListener(View, ComponentEventListener)} instead
     */
    @Deprecated(since = "2.2", forRemoval = true)
    public static Registration addBeforeShowEvent(View<?> view,
                                                  ComponentEventListener<View.BeforeShowEvent> listener) {
        return view.addBeforeShowListener(listener);
    }

    public static void setAfterNavigationHandler(View<?> view, Runnable operation) {
        view.setAfterNavigationHandler(operation);
    }

    public static <T extends Component> void setViewCloseDelegate(View<T> view,
                                                                  Consumer<View<T>> closeDelegate) {
        view.setCloseDelegate(closeDelegate);
    }

    public static void setPageTitleDelegate(View<?> view, Consumer<String> pageTitleDelegate) {
        view.setPageTitleDelegate(pageTitleDelegate);
    }

    public static boolean isSameView(View<?> view, View<?> other) {
        return view.getClass() == other.getClass()
                && view.getId().equals(other.getId());
    }

    public static String getRouteParamName(StandardDetailView<?> detailView) {
        return detailView.getRouteParamName();
    }

    @Internal
    public static void processBeforeEnterInternal(View<?> view, BeforeEnterEvent event) {
        view.processBeforeEnterInternal(event);
    }
}
