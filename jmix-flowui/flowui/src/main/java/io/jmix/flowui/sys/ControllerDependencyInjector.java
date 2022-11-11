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

package io.jmix.flowui.sys;

import io.jmix.flowui.view.View;

/**
 * Implementations of the interface are used for wiring of fields/setters to the view controllers. It defines
 * additional dependency injectors to the base {@link ViewControllerDependencyInjector}.
 *
 * @see ViewControllerDependencyManager
 */
public interface ControllerDependencyInjector {

    /**
     * The method is invoked when the view instance is created.
     *
     * @param injectionContext injection context
     */
    void inject(InjectionContext injectionContext);

    /**
     * Class describes injection context that contains view controller and options.
     */
    class InjectionContext {

        protected final View<?> view;

        public InjectionContext(View<?> view) {
            this.view = view;
        }

        public View<?> getView() {
            return view;
        }
    }
}
