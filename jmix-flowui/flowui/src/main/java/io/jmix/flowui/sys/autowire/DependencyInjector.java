/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowui.sys.autowire;

import com.vaadin.flow.component.Composite;

import java.util.Collection;

/**
 * Implementations of the interface are used for wiring of fields/setters and additional dependency
 * injectors to the UI components.
 *
 * @see AutowireManager
 */
public interface DependencyInjector {

    /**
     * The method is invoked when the component instance is created. Used to autowire dependencies to the UI component.
     *
     * @param autowireContext injection context
     */
    void autowire(AutowireContext<?> autowireContext);

    /**
     * Checks whether this injector can wire the passed injection context.
     *
     * @param autowireContext injection content that need to be checked
     * @return {@code true} if the passed injection context can be wired, {@code false} otherwise
     */
    boolean isApplicable(AutowireContext<?> autowireContext);

    /**
     * Base injection context interface.
     */
    interface AutowireContext<T extends Composite<?>> {

        /**
         * Returns a collection of objects that have already been autowired and that should not be used in
         * the subsequent injection. Implementations of injectors use this collection to avoid
         * overwriting already injected objects. Before injection, the injector checks the
         * element to see if it exists in this collection.
         *
         * @return collection of objects that have already been autowired
         */
        Collection<Object> getAutowired();

        /**
         * @return the autowiring target
         */
        T getTarget();
    }
}
