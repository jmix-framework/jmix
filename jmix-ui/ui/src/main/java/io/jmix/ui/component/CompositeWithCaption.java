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

import javax.annotation.Nullable;

/**
 * {@link CompositeComponent} having a caption.
 * Default implementations delegate their execution to {@link CompositeComponent#getComposition()}.
 */
public interface CompositeWithCaption extends CompositeWithDescription, Component.HasCaption {

    @Nullable
    @Override
    default String getCaption() {
        Component.HasCaption hasCaption = (Component.HasCaption) ((CompositeComponent) this).getComposition();
        return hasCaption.getCaption();
    }

    @Override
    default void setCaption(@Nullable String caption) {
        Component.HasCaption hasCaption = (Component.HasCaption) ((CompositeComponent) this).getComposition();
        hasCaption.setCaption(caption);
    }

}
