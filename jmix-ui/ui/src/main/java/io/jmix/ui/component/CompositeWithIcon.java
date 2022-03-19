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

import io.jmix.ui.icon.Icons;

import javax.annotation.Nullable;

/**
 * {@link CompositeComponent} having an icon.
 * Default implementations delegate their execution to {@link CompositeComponent#getComposition()}.
 */
public interface CompositeWithIcon extends Component.HasIcon {

    @Nullable
    @Override
    default String getIcon() {
        Component.HasIcon hasIcon = (Component.HasIcon) ((CompositeComponent) this).getComposition();
        return hasIcon.getIcon();
    }

    @Override
    default void setIcon(@Nullable String icon) {
        Component.HasIcon hasIcon = (Component.HasIcon) ((CompositeComponent) this).getComposition();
        hasIcon.setIcon(icon);
    }

    @Override
    default void setIconFromSet(@Nullable Icons.Icon icon) {
        Component.HasIcon hasIcon = (Component.HasIcon) ((CompositeComponent) this).getComposition();
        hasIcon.setIconFromSet(icon);
    }
}
