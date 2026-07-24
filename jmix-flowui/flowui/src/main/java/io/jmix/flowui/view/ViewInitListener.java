/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.view;

/**
 * Interface to be implemented by beans that need to be notified when any view is initialized.
 * <p>
 * Listeners are invoked at the end of view initialization: after the XML descriptor is processed,
 * dependencies are injected, and the {@link View.InitEvent} is fired. At this point the view's
 * component tree is fully built, so listeners can look up components and subscribe to their events.
 */
public interface ViewInitListener {

    /**
     * Invoked when the given view has been initialized.
     *
     * @param view the initialized view
     */
    void onViewInit(View<?> view);
}
