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

package io.jmix.core.impl;

import org.springframework.beans.factory.BeanFactory;

/**
 * Helper class needed to deserialize classes with beans
 */
public class SerializationContext {

    private static ThreadLocal<BeanFactory> beanFactoryThreadLocal = new ThreadLocal<>();

    /**
     * @return the {@link BeanFactory} instance from the thread local variable, if any stored.
     */
    public static BeanFactory getThreadLocalBeanFactory() {
        return beanFactoryThreadLocal.get();
    }

    public static void setThreadLocalBeanFactory(BeanFactory beanFactory) {
        beanFactoryThreadLocal.set(beanFactory);
    }

    public static void removeThreadLocalBeanFactory() {
        beanFactoryThreadLocal.remove();
    }
}
