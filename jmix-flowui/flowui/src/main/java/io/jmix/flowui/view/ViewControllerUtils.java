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
import org.springframework.context.ApplicationListener;
import org.springframework.lang.Nullable;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Utility class providing helper methods related to view controllers.
 */
public final class ViewControllerUtils {

    private ViewControllerUtils() {
    }

    /**
     * Retrieves the page title of a given component.
     * <ol>
     *  <li>If the component implements the {@code View} interface, the title will be fetched
     *  from the {@code getPageTitle} method of the view.</li>
     *  <li>If the component implements the {@code HasDynamicTitle} interface, the title will
     *  be dynamically retrieved and converted to a non-null string.</li>
     *  <li>If a {@code PageTitle} annotation is present on the component or its class, its
     *  value will be returned.</li>
     *  <li>Returns an empty string if no title can be determined or if the component is null.</li>
     * </ol>
     *
     * @param component the component whose page title is to be retrieved; may be {@code null}
     * @return the page title of the component, or an empty string if no title is available
     */
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

    /**
     * Searches for a specific annotation of the given type on the provided component.
     *
     * @param component       the component to search for the annotation; must not be null
     * @param annotationClass the class of the annotation to search for; must not be null
     * @param <A>             the type of the annotation
     * @return an {@code Optional} containing the annotation if found, or an empty {@code Optional} if not present
     */
    public static <A extends Annotation> Optional<A> findAnnotation(Component component, Class<A> annotationClass) {
        return findAnnotation(component.getClass(), annotationClass);
    }

    /**
     * Searches for a specific annotation of the given type on the provided component class.
     *
     * @param componentClass  the component class to search for the annotation; must not be null
     * @param annotationClass the class of the annotation to search for; must not be null
     * @param <A>             the type of the annotation
     * @return an {@code Optional} containing the annotation if found, or an empty {@code Optional} if not present
     */
    public static <A extends Annotation> Optional<A> findAnnotation(Class<? extends Component> componentClass,
                                                                    Class<A> annotationClass) {
        return Optional.ofNullable(componentClass.getAnnotation(annotationClass));
    }

    /**
     * Returns the package of a given class.
     *
     * @param controllerClass the class whose package is to be retrieved; must not be null
     * @return the package name of the given class, or an empty string if the class has no package
     */
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

    /**
     * Returns the {@link ViewData} associated with the specified {@link View}.
     *
     * @param view the {@link View} for which the {@link ViewData} is to be retrieved; must not be null
     * @return the {@link ViewData} associated with the specified {@link View}
     */
    public static ViewData getViewData(View<?> view) {
        return view.getViewData();
    }

    /**
     * Sets the specified {@link ViewData} with the given {@link View}.
     *
     * @param view     the {@link View} to which the {@link ViewData} is to be set; must not be null
     * @param viewData the {@link ViewData} to be associated with the {@link View}; must not be null
     */
    public static void setViewData(View<?> view, ViewData viewData) {
        view.setViewData(viewData);
        viewData.setViewId(view.getId().orElse(null));
    }

    /**
     * Returns the {@link ViewActions} associated with the specified {@link View}.
     *
     * @param view the {@link View} for which the {@link ViewActions} are to be retrieved; must not be null
     * @return the {@link ViewActions} associated with the specified {@link View}
     */
    public static ViewActions getViewActions(View<?> view) {
        return view.getViewActions();
    }

    /**
     * Sets the specified {@link ViewActions} with the given {@link View}.
     *
     * @param view        the {@link View} to which the {@link ViewActions} should be associated; must not be null
     * @param viewActions the {@link ViewActions} to be set for the {@link View}; must not be null
     */
    public static void setViewActions(View<?> view, ViewActions viewActions) {
        view.setViewActions(viewActions);
    }

    /**
     * Returns the {@link ViewFacets} associated with the specified {@link View}.
     *
     * @param view the {@link View} from which to retrieve the associated {@link ViewFacets}; must not be null
     * @return the {@link ViewFacets} associated with the given {@link View}
     */
    public static ViewFacets getViewFacets(View<?> view) {
        return view.getViewFacets();
    }

    /**
     * Sets the {@link ViewFacets} for the specified {@link View}.
     *
     * @param view       the {@link View} for which the {@link ViewFacets} are to be set; must not be null
     * @param viewFacets the {@link ViewFacets} to associate with the specified {@link View}; must not be null
     */
    public static void setViewFacets(View<?> view, ViewFacets viewFacets) {
        view.setViewFacets(viewFacets);
    }

    /**
     * Returns a specific type of {@link Facet} associated with the given {@link View}.
     *
     * @param view       the {@link View} from which the facet is to be retrieved; must not be null
     * @param facetClass the class type of the facet to retrieve; must not be null
     * @param <T>        the type of the facet
     * @return the facet of the specified type if found; otherwise, {@code null}
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public static <T extends Facet> T getViewFacet(View<?> view, Class<T> facetClass) {
        return (T) view.getViewFacets()
                .getFacets()
                .filter(facet -> facetClass.isAssignableFrom(facet.getClass()))
                .findAny()
                .orElse(null);
    }

    /**
     * Fires the specified {@link ComponentEvent} for the given {@link View}.
     *
     * @param view  the target {@link View} on which the event should be fired; must not be null
     * @param event the {@link ComponentEvent} to be fired; must not be null
     */
    public static void fireEvent(View<?> view, ComponentEvent<?> event) {
        ComponentUtil.fireEvent(view, event);
    }

    /**
     * Checks if the save action has been performed on the provided {@link StandardDetailView}.
     *
     * @param detailView the {@link StandardDetailView} instance for which to check the save action
     *                   status; must not be null
     * @return {@code true} if the save action has been performed, {@code false} otherwise
     */
    public static boolean isSaveActionPerformed(StandardDetailView<?> detailView) {
        return detailView.isSaveActionPerformed();
    }

    /**
     * Registers a listener to be informed of query parameter changes in the specified view.
     *
     * @param view     the view to which the query parameter change listener will be added
     * @param listener the listener to add
     * @return a {@link Registration} object that can be used to remove the listener
     */
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

    /**
     * Adds a listener for the {@link InitEntityEvent} to the specified {@link StandardDetailView}.
     *
     * @param <T>      the type of the entity being initialized
     * @param view     the detail view where the entity is being initialized
     * @param listener the listener to add
     * @return a {@link Registration} instance that allows the removal of the listener
     */
    public static <T> Registration addInitEntityEventListener(StandardDetailView<T> view,
                                                              ComponentEventListener<InitEntityEvent<T>> listener) {
        return view.addInitEntityListener(listener);
    }

    /**
     * Adds a {@link View.BeforeShowEvent} listener to the specified view.
     *
     * @param view     the view to which the listener is added
     * @param listener the listener to add
     * @return a {@link Registration} object for removing the listener
     */
    public static Registration addBeforeShowEventListener(View<?> view,
                                                          ComponentEventListener<View.BeforeShowEvent> listener) {
        return view.addBeforeShowListener(listener);
    }

    /**
     * Adds a {@link DetachEvent} listener to the specified view.
     *
     * @param view     the view to which the detach listener is to be added
     * @param listener the listener to add
     * @return a {@link Registration} object for removing the listener
     */
    public static Registration addDetachListener(View<?> view,
                                                 ComponentEventListener<DetachEvent> listener) {
        return view.addDetachListener(listener);
    }

    /**
     * Adds a {@link View.AfterCloseEvent} listener to the specified view.
     *
     * @param view     the view to which the listener is added
     * @param listener the listener to add
     * @return a {@link Registration} object for removing the listener
     */
    public static Registration addAfterCloseListener(View<?> view,
                                                     ComponentEventListener<View.AfterCloseEvent> listener) {
        return view.addAfterCloseListener(listener);
    }

    /**
     * Adds a {@link ValidationEvent} listener to the specified view.
     *
     * @param view     the view to which the listener is added
     * @param listener the listener to add
     * @return a {@link Registration} object for removing the listener
     */
    public static Registration addValidationEventListener(StandardDetailView<?> view,
                                                          ComponentEventListener<ValidationEvent> listener) {
        return view.addValidationEventListener(listener);
    }

    /**
     * Adds a {@link View.ReadyEvent} listener to the specified view.
     *
     * @param view     the view to which the listener is added
     * @param listener the listener to add
     * @return a {@link Registration} object for removing the listener
     */
    public static Registration addReadyListener(View<?> view,
                                                ComponentEventListener<View.ReadyEvent> listener) {
        return view.addReadyListener(listener);
    }

    /**
     * Adds a {@link View.PostReadyEvent} listener to the specified view.
     *
     * @param view     the view to which the listener is added
     * @param listener the listener to add
     * @return a {@link Registration} object for removing the listener
     */
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

    /**
     * Sets a handler to be executed after navigation to the given view.
     *
     * @param view      the view for which the after-navigation handler is to be set
     * @param operation the runnable operation to be executed after navigation
     */
    public static void setAfterNavigationHandler(View<?> view, Runnable operation) {
        view.setAfterNavigationHandler(operation);
    }

    /**
     * Sets a close delegate for the specified view. The close delegate defines the action
     * to be performed when the view is closed.
     *
     * @param view          the view instance for which the close delegate will be set
     * @param closeDelegate a consumer that handles the closing logic for the provided view
     * @param <T>           the type of the component that the view is associated with
     */
    public static <T extends Component> void setViewCloseDelegate(View<T> view,
                                                                  Consumer<View<T>> closeDelegate) {
        view.setCloseDelegate(closeDelegate);
    }

    /**
     * Sets the delegate responsible for displaying page titles.
     *
     * @param view              the view instance where the page title delegate will be applied
     * @param pageTitleDelegate a delegate to set
     */
    public static void setPageTitleDelegate(View<?> view, Consumer<String> pageTitleDelegate) {
        view.setPageTitleDelegate(pageTitleDelegate);
    }

    /**
     * Compares two {@link View} objects to determine if they are of the same type
     * and have the same identifier.
     *
     * @param view  the first View object to compare
     * @param other the second View object to compare
     * @return {@code true} if both views are of the same class and have
     * identical identifiers, {@code false} otherwise
     */
    public static boolean isSameView(View<?> view, View<?> other) {
        return view.getClass() == other.getClass()
                && view.getId().equals(other.getId());
    }

    /**
     * Returns the route parameter name associated with the given detail view.
     *
     * @param detailView the detail view instance from which to retrieve the route parameter name
     * @return the route parameter name
     */
    public static String getRouteParamName(StandardDetailView<?> detailView) {
        return detailView.getRouteParamName();
    }

    /**
     * Returns a list of application event listeners associated with a given {@link View}.
     *
     * @param view the {@link View} for which to retrieve the application event listeners
     * @return a list of application event listeners associated with the specified {@link View}
     */
    public static List<ApplicationListener<?>> getApplicationEventListeners(View<?> view) {
        return view.getApplicationEventListeners();
    }

    /**
     * Sets the application event listeners for the specified {@link View}.
     *
     * @param view      the {@link View} for which to set the application event listeners
     * @param listeners a list of application event listeners to be set for the {@link View},
     *                  or {@code null} if no listeners should be associated
     */
    public static void setApplicationEventListeners(View<?> view,
                                                    @Nullable List<ApplicationListener<?>> listeners) {
        view.setApplicationEventListeners(listeners);
    }

    /**
     * Processes logic that should occur before entering a new view.
     *
     * @param view  the view instance for which the processing is performed
     * @param event the event triggered before entering the view
     */
    @Internal
    public static void processBeforeEnterInternal(View<?> view, BeforeEnterEvent event) {
        view.processBeforeEnterInternal(event);
    }

    /**
     * Configures the dialog window header for the specified view.
     *
     * @param view               the view instance for which the dialog window header is to be configured
     * @param dialogWindowHeader the dialog window header object to be configured for the view
     */
    public static void configureDialogWindowHeader(View<?> view, DialogWindowHeader dialogWindowHeader) {
        view.configureDialogWindowHeader(dialogWindowHeader);
    }

    /**
     * Configures the dialog window footer for the specified view.
     *
     * @param view               the view instance for which the dialog window footer is to be configured
     * @param dialogWindowFooter the dialog window footer object to be configured for the view
     */
    public static void configureDialogWindowFooter(View<?> view, DialogWindowFooter dialogWindowFooter) {
        view.configureDialogWindowFooter(dialogWindowFooter);
    }
}
