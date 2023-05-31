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

package io.jmix.flowui.component.genericfilter.registration;

import io.jmix.core.Metadata;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.component.filter.FilterComponent;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.genericfilter.converter.AbstractFilterComponentConverter;
import io.jmix.flowui.component.genericfilter.converter.FilterConverter;
import io.jmix.flowui.entity.filter.FilterCondition;
import io.jmix.flowui.view.ViewRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Registers UI filter components that should be used by the framework.
 * <p>
 * For instance, in the spring {@link Configuration} class create
 * {@link FilterComponentRegistration} bean.
 * <pre>
 * &#64;Configuration
 * public class FilterComponentConfiguration {
 *
 *      &#64;Bean
 *      public FilterComponentRegistration registerPropertyFilterComponent() {
 *          return FilterComponentRegistrationBuilder.create(PropertyFilter.class,
 *          PropertyFilterCondition.class,
 *          PropertyFilterConverter.class)
 *          .build();
 *      }
 * }
 * </pre>
 *
 * <p><br>
 * Note, the order of providing {@link FilterComponentRegistration} beans is very important
 * because filter components with the same filter component class or the same model class
 * will be filtered if they have lower priority. For instance, the configuration provides
 * two {@link FilterComponentRegistration} with the same UI filter component class:
 * <pre>
 * &#64;Bean
 * &#64;Order(100)
 * public FilterComponentRegistration extPropertyFilter() {
 *      return FilterComponentRegistrationBuilder.create(ExtPropertyFilter.class,
 *      ExtPropertyFilterCondition.class,
 *      ExtPropertyFilterConverter.class)
 *      .build();
 * }
 * &#64;Bean
 * &#64;Order(200)
 * public FilterComponentRegistration extPropertyFilter1() {
 *      return FilterComponentRegistrationBuilder.create(ExtPropertyFilter.class,
 *      ExtPropertyFilterCondition.class,
 *      ExtPropertyFilterConverter.class)
 *      .withDetailViewId("extPropertyFilterCondition.detail")
 *      .build();
 * }
 * </pre>
 * The second filter component with detail view id will be filtered as it has a lower
 * priority.
 * <p>
 * Another example, the configuration provides {@link FilterComponentRegistration} that
 * overrides registration from some add-on. In this case, if the component from the
 * add-on has lower priority it will not be registered at all. It means that our filter
 * component registration must provide full information: UI filter component class,
 * model class, converter class and detail view id (optional).
 */
@Component("flowui_FilterComponents")
public class FilterComponents implements InitializingBean {

    private final Logger log = LoggerFactory.getLogger(FilterComponents.class);

    @Autowired(required = false)
    protected List<FilterComponentRegistration> filterComponentRegistrations;
    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    protected ViewRegistry viewRegistry;
    @Autowired
    protected Metadata metadata;

    protected Set<FilterComponentRegistration> registrations = new LinkedHashSet<>();

    @Override
    public void afterPropertiesSet() {
        registerComponents();
    }

    /**
     * Returns a converter class by filter component class.
     *
     * @param componentClass UI filter component class
     * @param filter         a filter with which the converter will be used
     * @return a converter class
     */
    public FilterConverter<? extends FilterComponent, ? extends FilterCondition> getConverterByComponentClass(
            Class<? extends FilterComponent> componentClass, GenericFilter filter) {
        FilterComponentRegistration registration = resolveRegistrationByComponentClass(componentClass);
        Class<? extends FilterConverter> converterClass = registration.getConverterClass();

        ObjectProvider<?> converterProvider = applicationContext.getBeanProvider(converterClass);
        if (AbstractFilterComponentConverter.class.isAssignableFrom(converterClass)) {
            return (FilterConverter<? extends FilterComponent, ? extends FilterCondition>)
                    converterProvider.getObject(filter);
        } else {
            return (FilterConverter<? extends FilterComponent, ? extends FilterCondition>)
                    converterProvider.getObject();
        }
    }

    /**
     * Returns a converter class by model class.
     *
     * @param modelClass a model class
     * @param filter     a filter with which the converter will be used
     * @return a converter class
     */
    public FilterConverter<? extends FilterComponent, ? extends FilterCondition> getConverterByModelClass(
            Class<? extends FilterCondition> modelClass, GenericFilter filter) {
        FilterComponentRegistration registration = resolveRegistrationByModelClass(modelClass);
        Class<? extends FilterConverter> converterClass = registration.getConverterClass();

        ObjectProvider<?> converterProvider = applicationContext.getBeanProvider(converterClass);
        if (AbstractFilterComponentConverter.class.isAssignableFrom(converterClass)) {
            return (FilterConverter<? extends FilterComponent, ? extends FilterCondition>)
                    converterProvider.getObject(filter);
        } else {
            return (FilterConverter<? extends FilterComponent, ? extends FilterCondition>)
                    converterProvider.getObject();
        }
    }

    /**
     * Returns a model detail view id by model class.
     *
     * @param modelClass a model class
     * @return a model detail view id by model class
     */
    public String getDetailViewId(Class<? extends FilterCondition> modelClass) {
        FilterComponentRegistration registration = resolveRegistrationByModelClass(modelClass);
        String detailViewId = registration.getDetailViewId();
        if (detailViewId == null) {
            MetaClass metaClass = metadata.getClass(modelClass);
            detailViewId = viewRegistry.getDetailViewId(metaClass);
        }

        return detailViewId;
    }

    /**
     * @return a set of registered filter component classes
     */
    public Set<Class<? extends FilterComponent>> getRegisteredFilterComponentClasses() {
        return registrations.stream()
                .map(FilterComponentRegistration::getComponentClass)
                .collect(Collectors.toSet());
    }

    /**
     * @return a set of registered model classes
     */
    public Set<Class<? extends FilterCondition>> getRegisteredModelClasses() {
        return registrations.stream()
                .map(FilterComponentRegistration::getModelClass)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Returns a model class by UI filter component class.
     *
     * @param componentClass a filter component class
     * @return a model class
     */
    public Class<? extends FilterCondition> getModelClass(Class<? extends FilterComponent> componentClass) {
        FilterComponentRegistration registration = resolveRegistrationByComponentClass(componentClass);
        return registration.getModelClass();
    }

    /**
     * Returns UI filter component class by model class.
     *
     * @param modelClass a model class
     * @return a filter component class
     */
    public Class<? extends FilterComponent> getComponentClass(Class<? extends FilterCondition> modelClass) {
        FilterComponentRegistration registration = resolveRegistrationByModelClass(modelClass);
        return registration.getComponentClass();
    }

    protected void registerComponents() {
        if (filterComponentRegistrations == null) {
            return;
        }

        Set<Class<? extends FilterComponent>> registeredComponentSet = new HashSet<>(filterComponentRegistrations.size());
        Set<Class<? extends FilterCondition>> registeredModelSet = new HashSet<>(filterComponentRegistrations.size());
        for (FilterComponentRegistration registration : filterComponentRegistrations) {
            Class<? extends FilterComponent> componentClass = registration.getComponentClass();
            if (registeredComponentSet.contains(componentClass)) {
                log.debug("Filter component '{}' with higher priority has already been added to the configuration. " +
                        "Skip: {}.", componentClass.getCanonicalName(), registration);
                continue;
            }

            Class<? extends FilterCondition> modelClass = registration.getModelClass();
            if (registeredModelSet.contains(modelClass)) {
                log.debug("Filter model class '{}' with higher priority has already been added to the configuration. " +
                        "Skip: {}.", modelClass.getCanonicalName(), registration);
                continue;
            }

            register(registration);
            registeredComponentSet.add(componentClass);
            registeredModelSet.add(modelClass);
        }
    }

    protected void register(FilterComponentRegistration registration) {
        registrations.add(registration);
    }

    protected FilterComponentRegistration resolveRegistrationByComponentClass(
            Class<? extends FilterComponent> componentClass) {
        Preconditions.checkNotNullArgument(componentClass,
                "'%s' filter component class cannot be null",
                componentClass.getCanonicalName());
        return registrations.stream()
                .filter(registration -> registration.getComponentClass().isAssignableFrom(componentClass))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format(
                        "Unable to resolve filter component with %s class ", componentClass.getCanonicalName())));
    }

    protected FilterComponentRegistration resolveRegistrationByModelClass(
            Class<? extends FilterCondition> modelClass) {
        Preconditions.checkNotNullArgument(modelClass,
                "'%s' filter model class cannot be null",
                modelClass.getCanonicalName());
        return registrations.stream()
                .filter(registration -> registration.getModelClass().isAssignableFrom(modelClass))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format(
                        "Unable to resolve filter model with %s class ", modelClass.getCanonicalName())));
    }
}
