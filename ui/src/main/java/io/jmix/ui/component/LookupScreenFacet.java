/*
 * Copyright 2020 Haulmont.
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

import io.jmix.core.JmixEntity;
import io.jmix.ui.meta.StudioDelegate;
import io.jmix.ui.meta.StudioFacet;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;
import io.jmix.ui.screen.LookupScreen;
import io.jmix.ui.screen.Screen;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Prepares and shows lookup screens.
 */
@StudioFacet(
        caption = "Lookup Screen",
        description = "Prepares and shows lookup screens"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "id", required = true)
        }
)
public interface LookupScreenFacet<E extends JmixEntity, S extends Screen>
        extends ScreenFacet<S>, EntityAwareScreenFacet<E> {

    /**
     * Sets select handler for the lookup screen.
     */
    @StudioDelegate
    void setSelectHandler(@Nullable Consumer<Collection<E>> selectHandler);

    /**
     * @return lookup screen select handler
     */
    @Nullable
    Consumer<Collection<E>> getSelectHandler();

    /**
     * Sets select validator for the lookup screen.
     */
    @StudioDelegate
    void setSelectValidator(Predicate<LookupScreen.ValidationContext<E>> selectValidator);

    /**
     * @return lookup screen select validator
     */
    Predicate<LookupScreen.ValidationContext<E>> getSelectValidator();

    /**
     * Sets code to transform entities after selection.
     * <p>
     * Applied only if either field or container or listComponent is assigned.
     */
    @StudioDelegate
    void setTransformation(Function<Collection<E>, Collection<E>> transformation);

    /**
     * @return selected entities transformation
     */
    Function<Collection<E>, Collection<E>> getTransformation();
}
