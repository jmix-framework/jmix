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

package io.jmix.flowui.sys;

import io.jmix.flowui.screen.Screen;

/**
 * Implementations of the interface are used for wiring of fields/setters to the screen controllers. It defines
 * additional dependency injectors to the base {@link UiControllerDependencyInjector}.
 *
 * @see UiControllerDependencyManager
 */
public interface ControllerDependencyInjector {

    /**
     * The method is invoked when the screen instance is created.
     *
     * @param injectionContext injection context
     */
    void inject(InjectionContext injectionContext);

    /**
     * Class describes injection context that contains screen controller and options.
     */
    class InjectionContext {

        protected final Screen screen;

        public InjectionContext(Screen screen) {
            this.screen = screen;
        }

        public Screen getScreen() {
            return screen;
        }
    }
}
