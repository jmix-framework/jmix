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

package io.jmix.simplesecurityflowui.constraint;

import io.jmix.core.AccessConstraintsRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Component;

/**
 * Class registers {@link io.jmix.core.constraint.AccessConstraint}s required for simple-security module.
 */
@Component("simsec_SecurityConstraintRegistration")
public class SecurityConstraintRegistration {

    protected AccessConstraintsRegistry accessConstraintsRegistry;

    protected BeanFactory beanFactory;

    public SecurityConstraintRegistration(AccessConstraintsRegistry accessConstraintsRegistry, BeanFactory beanFactory) {
        this.accessConstraintsRegistry = accessConstraintsRegistry;
        this.beanFactory = beanFactory;
    }

    @PostConstruct
    public void registerConstraints() {
        accessConstraintsRegistry.register(beanFactory.getBean(UiMenuConstraint.class));
        accessConstraintsRegistry.register(beanFactory.getBean(UiShowViewConstraint.class));
    }
}
