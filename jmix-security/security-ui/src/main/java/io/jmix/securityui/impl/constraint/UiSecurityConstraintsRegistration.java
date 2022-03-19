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

package io.jmix.securityui.impl.constraint;

import io.jmix.core.AccessConstraintsRegistry;
import io.jmix.securityui.constraint.UiEntityConstraint;
import io.jmix.securityui.constraint.UiMenuConstraint;
import io.jmix.securityui.constraint.UiShowScreenConstraint;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

public class UiSecurityConstraintsRegistration {

    @Autowired
    protected AccessConstraintsRegistry accessConstraintsRegistry;
    @Autowired
    protected BeanFactory beanFactory;

    @PostConstruct
    public void registerConstraints() {
        accessConstraintsRegistry.register(beanFactory.getBean(UiShowScreenConstraint.class));

        accessConstraintsRegistry.register(beanFactory.getBean(UiMenuConstraint.class));

        accessConstraintsRegistry.register(beanFactory.getBean(UiEntityConstraint.class));
    }
}
