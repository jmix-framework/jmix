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

package io.jmix.flowui.facet;

import com.vaadin.flow.component.Composite;
import org.springframework.lang.Nullable;

/**
 * Non-visual component of a {@link FacetOwner}.
 */
public interface Facet {

    /**
     * Returns the identifier associated with this oject.
     *
     * @return the ID if it exists, {@code null} otherwise.
     */
    @Nullable
    String getId();

    /**
     * Sets the identifier for this object.
     *
     * @param id the unique identifier to be set
     */
    void setId(@Nullable String id);

    /**
     * @param <T> the type of the owner
     * @return an owner containing this facet
     */
    @Nullable
    <T extends Composite<?> & FacetOwner> T getOwner();

    /**
     * Sets an owner containing this facet.
     *
     * @param owner an owner containing this facet
     * @param <T>   the type of the owner
     */
    <T extends Composite<?> & FacetOwner> void setOwner(@Nullable T owner);
}
