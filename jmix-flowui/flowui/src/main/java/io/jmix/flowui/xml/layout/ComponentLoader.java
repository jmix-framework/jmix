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
import io.jmix.flowui.component.composite.CompositeComponent;
import io.jmix.flowui.kit.component.HasActions;
import io.jmix.flowui.model.ViewData;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewActions;
import io.jmix.flowui.xml.layout.support.LoaderSupport;
import org.dom4j.Element;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.util.Optional;

public interface ComponentLoader<T extends Component> {

    interface Context {

        Component getOrigin();

        String getMessageGroup();

        void addInitTask(InitTask task);
        void executeInitTasks();

        HasActions getActionsHolder();
    }

    interface ComponentContext extends Context {

        ViewData getViewData();

        /**
         * @return actions holder object
         * @deprecated Use {@link #getActionsHolder()} instead
         */
        @Deprecated(since = "2.3", forRemoval = true)
        ViewActions getViewActions();

        Optional<ComponentContext> getParent();

        String getFullFrameId();

        String getCurrentFrameId();

        View<?> getView();

        void addPreInitTask(InitTask task);
        void executePreInitTasks();
    }

    interface CompositeComponentContext extends Context {

        CompositeComponent<?> getComposite();

        String getDescriptorPath();
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
         * @param context   loader context
         */
        default void execute(Context context) {
            if (context instanceof ComponentContext componentContext) {
                execute(componentContext, componentContext.getView());
            } else {
                throw new IllegalArgumentException("'context' must implement " + ComponentContext.class.getName());
            }
        }
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
