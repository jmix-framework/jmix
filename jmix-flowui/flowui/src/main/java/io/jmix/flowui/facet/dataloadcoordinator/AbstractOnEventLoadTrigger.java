/*
 * Copyright 2025 Haulmont.
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
import io.jmix.flowui.facet.FacetOwner;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.sys.autowire.ReflectionCacheManager;

import java.lang.invoke.MethodHandle;

/**
 * An abstract implementation of the {@link DataLoadCoordinator.Trigger} interface. It registers an event listener on a
 * specified {@link FacetOwner FacetOwner's} event and invokes the associated {@link DataLoader} when the event occurs,
 * triggering data loading operations.
 */
public class AbstractOnEventLoadTrigger implements DataLoadCoordinator.Trigger {

    protected final DataLoader loader;

    protected AbstractOnEventLoadTrigger(FacetOwner owner, ReflectionCacheManager reflectionCacheManager,
                                         DataLoader loader, Class<?> eventClass) {
        initTrigger(reflectionCacheManager, owner, eventClass);

        this.loader = loader;
    }

    protected void initTrigger(ReflectionCacheManager reflectionCacheManager, FacetOwner owner, Class<?> eventClass) {
        MethodHandle addListenerMethod = reflectionCacheManager.getTargetAddListenerMethod(
                owner.getClass(), eventClass, null
        );
        if (addListenerMethod == null) {
            throw new IllegalStateException("Cannot find addListener method for " + eventClass);
        }

        try {
            addListenerMethod.invoke(owner, (ComponentEventListener<?>) event -> load());
        } catch (Error e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException("Unable to add listener for " + eventClass, e);
        }
    }

    protected void load() {
        loader.load();
    }

    @Override
    public DataLoader getLoader() {
        return loader;
    }
}
