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

package io.jmix.flowui.facet.dataloadcoordinator;

import com.vaadin.flow.component.ComponentEventListener;
import io.jmix.flowui.facet.DataLoadCoordinator;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.sys.autowire.ReflectionCacheManager;
import io.jmix.flowui.view.View;

import java.lang.invoke.MethodHandle;

/**
 * Implementation of the {@link DataLoadCoordinator.Trigger} interface.
 * It registers an event listener on a specified view's event and invokes the associated
 * {@link DataLoader} when the event occurs, triggering data loading operations.
 */
public class OnViewEventLoadTrigger implements DataLoadCoordinator.Trigger {

    protected final DataLoader loader;

    public OnViewEventLoadTrigger(View<?> view, ReflectionCacheManager reflectionCacheManager,
                                  DataLoader loader, Class<?> eventClass) {
        MethodHandle addListenerMethod = reflectionCacheManager.getTargetAddListenerMethod(
                view.getClass(), eventClass, null
        );
        if (addListenerMethod == null) {
            throw new IllegalStateException("Cannot find addListener method for " + eventClass);
        }

        try {
            addListenerMethod.invoke(view, (ComponentEventListener<?>) event -> load());
        } catch (Error e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException("Unable to add listener for " + eventClass, e);
        }

        this.loader = loader;
    }

    protected void load() {
        loader.load();
    }

    @Override
    public DataLoader getLoader() {
        return loader;
    }
}
