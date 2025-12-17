/*
 * Copyright 2025 Haulmont.
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

package io.jmix.flowui.xml.facet.loader;

import io.jmix.flowui.Facets;
import io.jmix.flowui.facet.Facet;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.support.LoaderSupport;
import org.dom4j.Element;
import org.springframework.context.ApplicationContext;

/**
 * Abstract class for {@link FacetLoader} implementations.
 *
 * @param <F> the type of facet being loaded
 */
public abstract class AbstractFacetLoader<F extends Facet> implements FacetLoader<F> {

    protected ComponentLoader.Context context;

    protected Facets facets;
    protected LoaderSupport loaderSupport;
    protected ApplicationContext applicationContext;

    protected F resultFacet;
    protected Element element;

    protected abstract F createFacet();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.facets = applicationContext.getBean(Facets.class);
        this.loaderSupport = applicationContext.getBean(LoaderSupport.class);
    }

    protected ComponentLoader.ComponentContext findHostViewContext(ComponentLoader.Context fragmentContext) {
        ComponentLoader.Context currentContext = fragmentContext;
        while (currentContext.getParentContext() != null) {
            currentContext = currentContext.getParentContext();
        }

        if (currentContext instanceof ComponentLoader.ComponentContext viewContext) {
            return viewContext;
        }

        throw new IllegalStateException("%s has no parent view context"
                .formatted(fragmentContext.getClass().getSimpleName()));
    }

    @Override
    public Element getElement() {
        return element;
    }

    @Override
    public void setElement(Element element) {
        this.element = element;
    }

    @Override
    public ComponentLoader.Context getContext() {
        return context;
    }

    @Override
    public void setContext(ComponentLoader.Context context) {
        this.context = context;
    }

    @Override
    public void initFacet() {
        resultFacet = createFacet();
    }

    @Override
    public F getResultFacet() {
        return resultFacet;
    }
}
