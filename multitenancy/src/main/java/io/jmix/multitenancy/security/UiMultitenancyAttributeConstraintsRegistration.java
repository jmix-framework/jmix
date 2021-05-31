/*
 * Copyright 2021 Haulmont.
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

package io.jmix.multitenancy.security;


import io.jmix.core.AccessConstraintsRegistry;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


@Component
public class UiMultitenancyAttributeConstraintsRegistration {

    private final AccessConstraintsRegistry accessConstraintsRegistry;
    private final BeanFactory beanFactory;

    public UiMultitenancyAttributeConstraintsRegistration(AccessConstraintsRegistry accessConstraintsRegistry,
                                                          BeanFactory beanFactory) {
        this.accessConstraintsRegistry = accessConstraintsRegistry;
        this.beanFactory = beanFactory;
    }

    @PostConstruct
    public void registerConstraints() {
        accessConstraintsRegistry.register(beanFactory.getBean(MultitenancyAttributeConstraint.class));
        accessConstraintsRegistry.register(beanFactory.getBean(MultitenancyNonTenantEntityConstraint.class));
    }
}
