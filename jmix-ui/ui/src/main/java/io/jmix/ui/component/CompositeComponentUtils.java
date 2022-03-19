/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.component;

import org.springframework.context.ApplicationListener;

import java.util.List;

public final class CompositeComponentUtils {

    private CompositeComponentUtils() {
    }

    @SuppressWarnings("unchecked")
    public static void setRoot(CompositeComponent compositeComponent,
                               io.jmix.ui.component.Component root) {
        compositeComponent.setComposition(root);
    }

    public static <E> void fireEvent(CompositeComponent compositeComponent,
                                     Class<E> eventType, E event) {
        compositeComponent.publish(eventType, event);
    }

    public static void setUiEventListeners(CompositeComponent compositeComponent,
                                           List<ApplicationListener> listeners) {
        compositeComponent.setUiEventListeners(listeners);
    }
}
