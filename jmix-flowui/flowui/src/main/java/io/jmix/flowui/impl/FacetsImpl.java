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

package io.jmix.flowui.impl;

import io.jmix.flowui.Facets;
import io.jmix.flowui.facet.Facet;
import io.jmix.flowui.sys.BeanUtil;
import io.jmix.flowui.xml.facet.FacetProvider;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Component("fowui_Facets")
public class FacetsImpl implements Facets, ApplicationContextAware {

    protected ApplicationContext applicationContext;

    protected Map<Class<? extends Facet>, FacetProvider> registrations = new HashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Autowired(required = false)
    protected void setFacetRegistrations(List<FacetProvider<?>> registrations) {
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
        BeanUtil.autowireContext(applicationContext, instance);
        return instance;
    }
}
