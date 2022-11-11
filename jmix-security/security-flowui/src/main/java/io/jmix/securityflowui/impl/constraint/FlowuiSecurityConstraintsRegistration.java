/*
 * Copyright 2020 Haulmont.
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

package io.jmix.securityflowui.impl.constraint;

import io.jmix.core.AccessConstraintsRegistry;
import io.jmix.securityflowui.constraint.FlowuiEntityConstraint;
import io.jmix.securityflowui.constraint.FlowuiMenuConstraint;
import io.jmix.securityflowui.constraint.FlowuiShowViewConstraint;
import org.springframework.beans.factory.BeanFactory;

import javax.annotation.PostConstruct;

public class FlowuiSecurityConstraintsRegistration {

    protected BeanFactory beanFactory;
    protected AccessConstraintsRegistry accessConstraintsRegistry;

    public FlowuiSecurityConstraintsRegistration(BeanFactory beanFactory,
                                                 AccessConstraintsRegistry accessConstraintsRegistry) {
        this.beanFactory = beanFactory;
        this.accessConstraintsRegistry = accessConstraintsRegistry;
    }

    @PostConstruct
    public void registerConstraints() {
        accessConstraintsRegistry.register(beanFactory.getBean(FlowuiShowViewConstraint.class));

        accessConstraintsRegistry.register(beanFactory.getBean(FlowuiMenuConstraint.class));

        accessConstraintsRegistry.register(beanFactory.getBean(FlowuiEntityConstraint.class));
    }
}
