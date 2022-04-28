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

package io.jmix.flowui.model;

import io.jmix.flowui.RequiresChanges;
import io.jmix.flowui.SameAsUi;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * Interface defining methods for interacting with data API elements of a screen.
 */
@RequiresChanges
@SameAsUi
public interface ScreenData {

    /**
     * Returns screen's {@code DataContext}.
     * @throws IllegalStateException if the DataContext is not defined
     */
    DataContext getDataContext();

    /**
     * Returns screen's {@code DataContext} or null if it is not defined.
     */
    @Nullable
    DataContext getDataContextOrNull();

    /**
     * Sets {@code DataContext} instance for the screen.
     */
    void setDataContext(DataContext dataContext);

    /**
     * Performs {@link DataLoader#load()} for all loaders registered in the screen.
     */
    void loadAll();

    /**
     * Returns a container by its id.
     * @throws IllegalArgumentException if there is no such container in the screen
     */
    <T extends InstanceContainer> T getContainer(String id);

    /**
     * Returns a loader by its id.
     * @throws IllegalArgumentException if there is no such loader in the screen
     */
    <T extends DataLoader> T getLoader(String id);

    /**
     * Returns ids of all registered containers.
     */
    Set<String> getContainerIds();

    /**
     * Returns ids of all registered loaders.
     */
    Set<String> getLoaderIds();

    /**
     * Registers the given container in the screen.
     */
    void registerContainer(String id, InstanceContainer container);

    /**
     * Registers the given loader in the screen.
     */
    void registerLoader(String id, DataLoader loader);
}
