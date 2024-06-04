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

package io.jmix.flowui.sys.vaadin;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.router.NavigationEvent;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.spring.SpringInstantiator;
import io.jmix.flowui.sys.BeforeNavigationInitializer;
import io.jmix.flowui.sys.ViewSupport;
import io.jmix.flowui.view.View;
import org.springframework.context.ApplicationContext;

import java.util.Collection;

public class JmixSpringInstantiator extends SpringInstantiator {

    protected ApplicationContext applicationContext;
    protected Collection<BeforeNavigationInitializer> beforeNavigationInitializers;

    /**
     * Creates a new spring instantiator instance.
     *
     * @param service the service to use
     * @param context the application context
     */
    public JmixSpringInstantiator(VaadinService service, ApplicationContext context) {
        super(service, context);
        this.applicationContext = context;
        this.beforeNavigationInitializers = applicationContext.getBeansOfType(BeforeNavigationInitializer.class).values();//todo like for view support, get on demand?
    }

    @Override
    public <T extends HasElement> T createRouteTarget(Class<T> routeTargetType, NavigationEvent event) {
        final T instance = super.createRouteTarget(routeTargetType, event);
        initBeforeNavigation(routeTargetType, event, instance);

        return instance;
    }

    @Override
    public <T> T getOrCreate(Class<T> type) {
        final T instance = super.getOrCreate(type);
        init(type, instance);

        return instance;
    }

    protected <T> void init(Class<T> type, T instance) {
        if (View.class.isAssignableFrom(type)) {
            getViewSupport().initView(((View) instance));
        }
    }

    protected <T> void initBeforeNavigation(Class<T> routeTargetType, NavigationEvent event, T instance) {
        if (View.class.isAssignableFrom(routeTargetType)) {
            for (BeforeNavigationInitializer initializer : beforeNavigationInitializers) {
                initializer.initialize((View<?>) instance, event);
            }
        }
    }

    private ViewSupport getViewSupport() {
        return applicationContext.getBean(ViewSupport.class);
    }
}
