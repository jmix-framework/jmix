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

package io.jmix.flowui.xml.layout.loader;

import com.vaadin.flow.component.Component;
import io.jmix.core.ClassManager;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.flowui.Actions;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.LoaderResolver;
import io.jmix.flowui.xml.layout.support.ComponentLoaderSupport;
import io.jmix.flowui.xml.layout.support.LoaderSupport;
import io.micrometer.core.instrument.MeterRegistry;
import org.dom4j.Element;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.util.Optional;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkState;

public abstract class AbstractComponentLoader<T extends Component> implements ComponentLoader<T> {

    protected Context context;

    protected UiComponents factory;
    protected LoaderResolver loaderResolver;
    protected LoaderSupport loaderSupport;
    private ComponentLoaderSupport componentLoaderSupport;

    protected Element element;

    protected T resultComponent;

    protected ApplicationContext applicationContext;
    protected Environment environment;

    protected AbstractComponentLoader() {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    protected ComponentContext getComponentContext() {
        checkState(context instanceof ComponentContext,
                "'context' must implement " + ComponentContext.class.getName());

        return (ComponentContext) context;
    }

    protected ComponentLoaderSupport componentLoader() {
        if (componentLoaderSupport == null) {
            componentLoaderSupport = applicationContext.getBean(ComponentLoaderSupport.class, context);
        }
        return componentLoaderSupport;
    }

    protected abstract T createComponent();

    @Override
    public void initComponent() {
        resultComponent = createComponent();
        loadId(resultComponent, element);
        loadVisible(resultComponent, element);
    }

    /*protected CompositeComponentContext getCompositeComponentContext() {
        checkState(context instanceof CompositeComponentContext,
                "'context' must implement io.jmix.ui.xml.layout.ComponentLoader.CompositeComponentContext");

        return (CompositeComponentContext) context;
    }*/

    @Override
    public UiComponents getFactory() {
        return factory;
    }

    @Override
    public void setFactory(UiComponents factory) {
        this.factory = factory;
    }

    @Override
    public void setElement(Element element) {
        this.element = element;
    }

    @Override
    public Element getElement(Element element) {
        return element;
    }

    @Override
    public T getResultComponent() {
        return resultComponent;
    }

    @Override
    public LoaderResolver getLoaderResolver() {
        return loaderResolver;
    }

    @Override
    public void setLoaderResolver(LoaderResolver loaderResolver) {
        this.loaderResolver = loaderResolver;
    }

    @Override
    public LoaderSupport getLoaderSupport() {
        return loaderSupport;
    }

    @Override
    public void setLoaderSupport(LoaderSupport loaderSupport) {
        this.loaderSupport = loaderSupport;
    }

    protected Messages getMessages() {
        return applicationContext.getBean(Messages.class);
    }

    protected Actions getActions() {
        return applicationContext.getBean(Actions.class);
    }

    protected MessageTools getMessageTools() {
        return applicationContext.getBean(MessageTools.class);
    }

    protected ClassManager getClassManager() {
        return applicationContext.getBean(ClassManager.class);
    }

    /*protected UiProperties getProperties() {
        return applicationContext.getBean(UiProperties.class);
    }*/

    protected MeterRegistry getMeterRegistry() {
        return applicationContext.getBean(MeterRegistry.class);
    }

    protected LayoutLoader getLayoutLoader() {
        return applicationContext.getBean(LayoutLoader.class, context);
    }

    protected LayoutLoader getLayoutLoader(Context context) {
        return applicationContext.getBean(LayoutLoader.class, context);
    }

    protected void loadId(Component component, Element element) {
        loaderSupport.loadString(element, "id", component::setId);
    }

    protected void loadVisible(Component component, Element element) {
        loaderSupport.loadBoolean(element, "visible", component::setVisible);
    }

    protected Optional<Boolean> loadBoolean(Element element, String attributeName) {
        return loaderSupport.loadBoolean(element, attributeName);
    }

    protected String loadResourceString(String message, String messageGroup) {
        return loaderSupport.loadResourceString(message, messageGroup);
    }

    protected Optional<String> loadString(Element element, String attributeName) {
        return loaderSupport.loadString(element, attributeName);
    }

    protected Optional<Integer> loadInteger(Element element, String attributeName) {
        return loaderSupport.loadInteger(element, attributeName);
    }

    protected Optional<Double> loadDouble(Element element, String attributeName) {
        return loaderSupport.loadDouble(element, attributeName);
    }

    protected <E extends Enum<E>> Optional<E> loadEnum(Element element, Class<E> type, String attributeName) {
        return loaderSupport.loadEnum(element, type, attributeName);
    }

    protected void loadBoolean(Element element, String attributeName, Consumer<Boolean> setter) {
        loaderSupport.loadBoolean(element, attributeName, setter);
    }

    protected void loadResourceString(String message, String messageGroup, Consumer<String> setter) {
        loaderSupport.loadResourceString(message, messageGroup, setter);
    }

    protected void loadString(Element element, String attributeName, Consumer<String> setter) {
        loaderSupport.loadString(element, attributeName, setter);
    }

    protected void loadInteger(Element element, String attributeName, Consumer<Integer> setter) {
        loaderSupport.loadInteger(element, attributeName, setter);
    }

    protected void loadDouble(Element element, String attributeName, Consumer<Double> setter) {
        loaderSupport.loadDouble(element, attributeName, setter);
    }

    protected <E extends Enum<E>> void loadEnum(Element element, Class<E> type, String attributeName,
                                                Consumer<E> setter) {
        loaderSupport.loadEnum(element, type, attributeName, setter);
    }
}
