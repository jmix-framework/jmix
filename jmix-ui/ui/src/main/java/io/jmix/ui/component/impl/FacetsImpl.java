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

package io.jmix.ui.component.impl;

import io.jmix.ui.component.Facet;
import io.jmix.ui.Facets;
import io.jmix.ui.xml.FacetProvider;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Component("ui_Facets")
@ParametersAreNonnullByDefault
public class FacetsImpl implements Facets {

    @Autowired
    protected ApplicationContext applicationContext;

    protected Map<Class<? extends Facet>, FacetProvider> registrations = new HashMap<>();

    @Autowired(required = false)
    protected void setFacetRegistrations(List<FacetProvider> registrations) {
        this.registrations = registrations.stream()
                .collect(toMap(FacetProvider::getFacetClass, identity()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Facet> T create(Class<T> facetClass) {
        FacetProvider<T> registration = registrations.get(facetClass);
        if (registration == null) {
            throw new IllegalStateException("There is no such facet " + facetClass);
        }

        T instance = registration.create();
        autowireContext(instance);
        return instance;
    }

    protected void autowireContext(Facet instance) {
        AutowireCapableBeanFactory autowireBeanFactory = applicationContext.getAutowireCapableBeanFactory();
        autowireBeanFactory.autowireBean(instance);

        if (instance instanceof ApplicationContextAware) {
            ((ApplicationContextAware) instance).setApplicationContext(applicationContext);
        }

        if (instance instanceof InitializingBean) {
            try {
                ((InitializingBean) instance).afterPropertiesSet();
            } catch (Exception e) {
                throw new RuntimeException(
                        "Unable to initialize UI Component - calling afterPropertiesSet for " +
                                instance.getClass(), e);
            }
        }
    }
}