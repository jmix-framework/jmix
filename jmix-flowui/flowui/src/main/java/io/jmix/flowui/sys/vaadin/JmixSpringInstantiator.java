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

import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.spring.SpringInstantiator;
import io.jmix.flowui.view.View;
import io.jmix.flowui.sys.ViewSupport;
import org.springframework.context.ApplicationContext;

public class JmixSpringInstantiator extends SpringInstantiator {

    protected ApplicationContext applicationContext;

    /**
     * Creates a new spring instantiator instance.
     *
     * @param service the service to use
     * @param context the application context
     */
    public JmixSpringInstantiator(VaadinService service, ApplicationContext context) {
        super(service, context);
        this.applicationContext = context;
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

    private ViewSupport getViewSupport() {
        return applicationContext.getBean(ViewSupport.class);
    }
}
