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

import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.di.InstantiatorFactory;
import com.vaadin.flow.server.VaadinService;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component("flowui_JmixInstantiatorFactory")
public class JmixInstantiatorFactory implements InstantiatorFactory {

    protected ApplicationContext applicationContext;

    public JmixInstantiatorFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Instantiator createInstantitor(VaadinService service) {
        return new JmixSpringInstantiator(service, applicationContext);
    }
}
