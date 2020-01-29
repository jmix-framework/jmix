/*
 * Copyright 2019 Haulmont.
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
package io.jmix.ui.xml.layout;

import io.jmix.core.BeanLocator;
import io.jmix.core.MessageTools;
import io.jmix.ui.components.Component;
import io.jmix.ui.components.Frame;
import io.jmix.ui.UiComponents;
import io.jmix.ui.model.ScreenData;
import io.jmix.ui.screen.ScreenOptions;
import org.dom4j.Element;

import java.util.Map;

/**
 * Base interface for loaders which create components by XML definitions.
 */
public interface ComponentLoader<T extends Component> {

    interface Context {
        /**
         * @return message pack of the context, should be used with {@link MessageTools#loadString(String)} in loaders
         */
        String getMessagesPack();
    }

    interface ComponentContext extends Context {
        ScreenOptions getOptions();

        ScreenData getScreenData();

        ComponentContext getParent();
        Frame getFrame();

        String getFullFrameId();

        String getCurrentFrameId();

        void addPostInitTask(PostInitTask task);
        void executePostInitTasks();

        void addInjectTask(InjectTask task);
        void executeInjectTasks();

        void addInitTask(InitTask task);
        void executeInitTasks();

        Map<String, Object> getParams();
        Map<String, String> getAliasesMap();
    }

    interface CompositeComponentContext extends Context {
        Class<? extends Component> getComponentClass();

        String getDescriptorPath();
    }

    /**
     * PostInitTasks are used to perform deferred initialization of visual components.
     */
    interface PostInitTask {
        /**
         * This method will be invoked after window initialization.
         *
         * @param context loader context
         * @param window  top-most window
         */
        void execute(ComponentContext context, Frame window);
    }

    /**
     * For internal use only.
     */
    interface InjectTask {
        /**
         * This method will be invoked after window components loading before window initialization.
         *
         * @param context top-most loader context
         * @param window top-most window
         */
        void execute(ComponentContext context, Frame window);
    }

    /**
     * For internal use only.
     */
    interface InitTask {
        /**
         * This method will be invoked after window components loading before window initialization.
         *
         * @param context loader context
         * @param window top-most window
         */
        void execute(ComponentContext context, Frame window);
    }

    Context getContext();
    void setContext(Context context);

    UiComponents getFactory();
    void setFactory(UiComponents factory);

    LoaderResolver getLoaderResolver();
    void setLoaderResolver(LoaderResolver loaderResolver);

    /**
     * @deprecated use {@link #getLoaderResolver()} instead
     */
    @Deprecated
    LayoutLoaderConfig getLayoutLoaderConfig();

    /**
     * @deprecated use {@link #setLoaderResolver(LoaderResolver)} instead
     */
    @Deprecated
    void setLayoutLoaderConfig(LayoutLoaderConfig config);

    Element getElement(Element element);
    void setElement(Element element);

    void setBeanLocator(BeanLocator beanLocator);

    /**
     * Creates result component by XML-element and loads its Id. Also creates all nested components.
     *
     * @see #getResultComponent()
     */
    void createComponent();

    /**
     * Loads component properties by XML definition.
     *
     * @see #getElement(Element)
     */
    void loadComponent();

    /**
     * Returns previously created instance of component.
     *
     * @see #createComponent()
     */
    T getResultComponent();
}
