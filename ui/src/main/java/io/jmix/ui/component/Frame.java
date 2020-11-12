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

import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenFragment;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Stream;

/**
 * Root class of UI components having controller.
 *
 * @see Window
 * @see Fragment
 */
public interface Frame
        extends ExpandingLayout,
                OrderedContainer,
                Component.BelongToFrame,
                HasSpacing,
                HasMargin,
                ActionsHolder,
                Component.HasIcon,
                Component.HasCaption,
                SupportsExpandRatio {

    /**
     * @return UI controller of the frame
     *
     * @see Screen
     * @see ScreenFragment
     */
    FrameOwner getFrameOwner();

    /**
     * @return current frame context
     */
    FrameContext getContext();

    /**
     * Check validity by invoking validators on all components which support them.
     * @return true if all components are in valid state
     */
    boolean isValid();

    /**
     * Check validity by invoking validators on all components which support them.
     * @throws ValidationException if some components are currently in invalid state
     */
    void validate() throws ValidationException;

    /**
     * Check validity by invoking validators on specified components which support them
     * and show validation result notification.
     * @return true if the validation was successful, false if there were any problems
     */
    boolean validate(List<Validatable> fields);

    /**
     * Check validity by invoking validators on all components which support them
     * and show validation result notification.
     * @return true if the validation was successful, false if there were any problems
     */
    boolean validateAll();

    /**
     * Registers the passed non-visual component in the frame.
     *
     * @param facet facet
     */
    void addFacet(Facet facet);

    /**
     * Finds registered facet by ID.
     *
     * @param id facet ID
     * @return facet instance or null
     */
    @Nullable
    Facet getFacet(String id);

    /**
     * Finds registered facet by ID. Throws {@link IllegalArgumentException} if not found.
     *
     * @param id facet ID
     * @return facet instance
     */
    default Facet getFacetNN(String id) {
        Facet facet = getFacet(id);
        if (facet == null) {
            throw new IllegalArgumentException("No facet with id " + id);
        }
        return facet;
    }

    /**
     * Removes the non-visual component from the frame.
     *
     * @param facet facet
     */
    void removeFacet(Facet facet);

    /**
     * @return stream of registered non-visual components
     */
    Stream<Facet> getFacets();
}
