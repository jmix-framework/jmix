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

public interface ComponentLoader<T extends Component> {

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

    Context getContext();

    void setContext(Context context);

    UiComponents getFactory();

    void setFactory(UiComponents factory);

    LoaderResolver getLoaderResolver();

    void setLoaderResolver(LoaderResolver loaderResolver);

    LoaderSupport getLoaderSupport();

    void setLoaderSupport(LoaderSupport loaderSupport);

    Element getElement(Element element);

    void setElement(Element element);

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
