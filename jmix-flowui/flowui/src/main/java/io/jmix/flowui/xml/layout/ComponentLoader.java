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

package io.jmix.flowui.xml.layout;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.HasDataComponents;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.kit.component.HasActions;
import io.jmix.flowui.model.ViewData;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewActions;
import io.jmix.flowui.xml.layout.support.LoaderSupport;
import org.dom4j.Element;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;

import java.util.Optional;

/**
 * Defines the contract for loading and initializing UI components from XML descriptors.
 *
 * @param <T> the type of component being loaded
 */
public interface ComponentLoader<T extends Component> {

    /**
     * Represents a context for loading and initializing UI components. The context
     * provides access to the component being processed, its lifecycle tasks, and
     * associated data or actions holders.
     */
    interface Context {

        /**
         * @return a target component for which an XML descriptor is processed
         */
        Component getOrigin();

        /**
         * @return a target component id, including parent component ids
         */
        String getFullOriginId();

        /**
         * @return parent loader context
         */
        @Nullable
        Context getParentContext();

        /**
         * @return a message group associated with XML descriptor
         */
        String getMessageGroup();

        /**
         * Adds {@link InitTask} that will be executed according to the
         * origin component lifecycle.
         *
         * @param task a task to add
         */
        void addInitTask(InitTask task);

        /**
         * Executes all added {@link InitTask}s
         */
        void executeInitTasks();

        /**
         * @return an object that stores actions associated with the origin component
         */
        HasActions getActionsHolder();

        /**
         * @return an object that stores data components associated with the origin component
         */
        HasDataComponents getDataHolder();
    }

    /**
     * Represents a context for loading and initializing UI components in {@link View}.
     */
    interface ComponentContext extends Context {

        /**
         * @return an instance of {@link ViewData} object associated with the origin view
         * @deprecated Use {@link #getDataHolder()} instead
         */
        @Deprecated(since = "2.3", forRemoval = true)
        ViewData getViewData();

        /**
         * @return actions holder object
         * @deprecated Use {@link #getActionsHolder()} instead
         */
        @Deprecated(since = "2.3", forRemoval = true)
        ViewActions getViewActions();

        /**
         * @return parent loader context
         * @deprecated Use {@link #getParentContext()} instead
         */
        @Deprecated(since = "2.3", forRemoval = true)
        Optional<ComponentContext> getParent();

        /**
         * @deprecated Use {@link #getFullOriginId()}
         */
        @Deprecated(since = "2.3", forRemoval = true)
        String getFullFrameId();

        /**
         * @deprecated Use {@link #getFullOriginId()}
         */
        @Deprecated(since = "2.3", forRemoval = true)
        String getCurrentFrameId();

        /**
         * @return an origin view
         */
        View<?> getView();

        /**
         * Adds Pre {@link InitTask} that will be executed according to the
         * origin component lifecycle.
         * <p>
         * Note: Pre InitTasks will be executed before DependencyManager
         * invocation to have precedence over @Subscribe methods
         *
         * @param task a task to add
         */
        void addPreInitTask(InitTask task);

        /**
         * Executes all added {@link InitTask}s
         */
        void executePreInitTasks();

        /**
         * Add {@link ComponentLoader.AutowireTask} that will be executed according to the origin component lifecycle.
         *
         * @param task a task to add
         */
        void addAutowireTask(AutowireTask task);

        /**
         * Executed all added {@link AutowireTask}s
         */
        void executeAutowireTasks();
    }

    /**
     * Represents a context for loading and initializing UI components in {@link Fragment}.
     */
    interface FragmentContext extends Context {

        /**
         * @return an origin fragment
         */
        Fragment<?> getFragment();
    }

    /**
     * InitTasks are used to perform deferred initialization of visual components.
     */
    interface InitTask {
        /**
         * This method will be invoked after view initialization.
         *
         * @param context loader context
         * @param view    view
         * @deprecated Use {@link #execute(Context)} instead
         */
        @Deprecated(since = "2.3", forRemoval = true)
        void execute(ComponentContext context, View<?> view);

        /**
         * This method will be invoked after component's content initialization.
         *
         * @param context loader context
         */
        default void execute(Context context) {
            if (context instanceof ComponentContext componentContext) {
                execute(componentContext, componentContext.getView());
            } else {
                throw new IllegalArgumentException("'context' must implement " + ComponentContext.class.getName());
            }
        }
    }

    /**
     * AutowireTasks are used to perform autowiring of nested fragments in a view.
     */
    interface AutowireTask {

        /**
         * This method will be invoked after origin view autowiring.
         *
         * @param componentContext loader context
         */
        void execute(ComponentContext componentContext);
    }

    /**
     * Returns the context associated with the current component loading and initialization process.
     *
     * @return the {@link Context} instance
     */
    Context getContext();

    /**
     * Sets the context for loading and initializing UI components.
     *
     * @param context the {@link Context} instance to set
     */
    void setContext(Context context);

    /**
     * Returns the factory for creating UI components.
     *
     * @return the {@link UiComponents} instance
     */
    UiComponents getFactory();

    /**
     * Sets the factory used for creating UI components.
     *
     * @param factory the {@link UiComponents} instance to set
     */
    void setFactory(UiComponents factory);

    /**
     * Returns the {@link LoaderResolver} instance used to resolve component and view loaders
     * during the XML-based component initialization process.
     *
     * @return the {@link LoaderResolver} instance
     */
    LoaderResolver getLoaderResolver();

    /**
     * Sets the {@link LoaderResolver} instance used to resolve component and view loaders
     * during the XML-based component initialization process.
     *
     * @param loaderResolver the {@link LoaderResolver} instance to set
     */
    void setLoaderResolver(LoaderResolver loaderResolver);

    /**
     * Returns the {@link LoaderSupport} instance used to provide support functionalities
     * for loading and initializing components.
     *
     * @return the {@link LoaderSupport} instance
     */
    LoaderSupport getLoaderSupport();

    /**
     * Sets the {@link LoaderSupport} instance used to provide supporting functionalities
     * for loading and initializing components.
     *
     * @param loaderSupport the {@link LoaderSupport} instance to set
     */
    void setLoaderSupport(LoaderSupport loaderSupport);

    @Deprecated(since = "2.6", forRemoval = true)
    Element getElement(Element element);

    /**
     * Sets the specified XML element to be associated with the component loader.
     *
     * @param element the {@link Element} instance representing the XML configuration
     *                for a UI component
     */
    void setElement(Element element);

    /**
     * Sets the application context for the current component loader.
     *
     * @param applicationContext the {@link ApplicationContext} instance to set
     */
    void setApplicationContext(ApplicationContext applicationContext);

    /**
     * @deprecated unused
     */
    // don't forget to remove corresponding spotbugs exclusion
    @Deprecated(since = "2.5", forRemoval = true)
    void setEnvironment(Environment environment);

    /**
     * Creates result component by XML-element
     */
    void initComponent();

    /**
     * Loads component properties by XML definition.
     *
     * @see #getElement(Element)
     */
    void loadComponent();

    /**
     * Returns previously created instance of component.
     *
     * @see #initComponent()
     */
    T getResultComponent();
}
